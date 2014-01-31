/*
 * VANETsim open source project - http://www.vanet-simulator.org
 * Copyright (C) 2008 - 2013  Andreas Tomandl, Florian Scheuer, Bernhard Gruber
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package vanetsim.scenario;


import vanetsim.gui.Renderer;
import vanetsim.scenario.messages.Message;

/**
 * This class stores various messages in multiple separate arrays:
 * <ul>
 * <li>messages which were received but not yet processed</li>
 * <li>messages to be executed</li>
 * <li>messages that need to be forwarded</li>
 * <li>old messages to prevent problems in broadcast mode</li>
 * </ul>
 * The arrays are not resized on every operation. Deletion of an element only changes the corresponding 
 * size variable, addition only leads to a larger array if there's not enough space left.
 */
public class KnownMessages{
	
	/** The timeout for a forward message in milliseconds. If a message could not be forwarded 
	 * within this time, it will get dropped! */
	private static final int MAX_FORWARD_TIME = 2500;
	
	/** The timeout for an old message in milliseconds. After this time it will get deleted. */
	private static final int MAX_OLD_TIME = 5000;
	
	/** A reference to the renderer */
	private static final Renderer renderer_ = Renderer.getInstance();
	
	/** The vehicle this data structure belongs to. */
	private final Vehicle vehicle_;
	
	/** Messages which will get executed. */
	private Message[] executeMessages_;
	
	/** The size of the messages which will be executed. */
	private int executeMessageSize_ = 0;
	
	/** Messages which were received but are not sent out yet. */
	private Message[] unprocessedMessages_;
	
	/** The size of the unprocessed messages. */
	private int unprocessedMessageSize = 0;
	
	/** Messages which shall be forwarded to other vehicles. */
	private Message[] forwardMessages_;
	
	/** The size of the messages which will shall be forwarded. */
	private int forwardMessageSize_ = 0;
	
	/** An array to store when the messages arrived. */
	private int[] forwardArrivalTime_;
	
	/** Messages which were already received. Used so that messages don't get transmitted again and again (especially in flooding mode). */
	private Message[] oldMessages_ ;
	
	/** The size of the messages which were already received. */
	private int oldMessageSize_ = 0;
	
	/** An array to store when an old messages arrived. */
	private int[] oldMessageArrivalTime_;
	
	/** How many forward messages were deleted because they could not be forwarded within time. */
	private int failedToForwardCount_ = 0;
	
	/** If there are new messages which need to be processed */
	private boolean hasNewMessages_ = false;
	
	/**
	 * Constructor.
	 * 
	 * @param vehicle	the vehicle this data structure belongs to
	 */
	public KnownMessages(Vehicle vehicle){
		vehicle_ = vehicle;
		executeMessages_ = new Message[2];
		unprocessedMessages_ = new Message[2];
		forwardMessages_ = new Message[2];
		forwardArrivalTime_ = new int[2];
		oldMessages_ = new Message[2];
		oldMessageArrivalTime_ = new int[2];
	}
	
	public KnownMessages(){
		vehicle_ = null;
		executeMessages_ = new Message[2];
		unprocessedMessages_ = new Message[2];
		forwardMessages_ = new Message[2];
		forwardArrivalTime_ = new int[2];
		oldMessages_ = new Message[2];
		oldMessageArrivalTime_ = new int[2];
	}
	
	/**
	 * Adds a message.
	 * 
	 * @param message	the message
	 * @param doExecute	<code>true</code> if you want the message to be executed, else <code>false</code>
	 * @param doForward <code>true</code> if you want the message to be forwarded, else <code>false</code>
	 */
	public synchronized void addMessage(Message message, boolean doExecute, boolean doForward){
		boolean foundExecute = false;
		boolean foundForward = false;
		
		int i;

		for(i = 0; i < oldMessageSize_; ++i){
			if(oldMessages_[i] == message){
				foundExecute = true;
				foundForward = true;
				break;
			}
		}


		if(doExecute && !foundExecute){
			for(i = 0; i < executeMessageSize_; ++i){
				if(executeMessages_[i] == message){
					foundExecute = true;
					break;
				}
			}
		}
		
		if(doForward && !foundForward){
			for(i = 0; i < forwardMessageSize_; ++i){
				if(forwardMessages_[i] == message){
					foundForward = true;
					break;
				}
			}
		}
		
		if(doForward && !foundForward){
			for(i = 0; i < unprocessedMessageSize; ++i){
				if(unprocessedMessages_[i] == message){
					foundForward = true;
					break;
				}
			}
		}	

		if(doExecute && !foundExecute){	
			hasNewMessages_ = true;			
			if(executeMessageSize_ < executeMessages_.length){	// array is still large enough that we may use the space
				executeMessages_[executeMessageSize_] = message;
			} else {	// create larger arrays and insert element
				Message[] newArray = new Message[executeMessageSize_ + 2];
				System.arraycopy (executeMessages_,0,newArray,0,executeMessageSize_);
				newArray[executeMessageSize_] = message;
				executeMessages_ = newArray;
			}
			++executeMessageSize_;
		}

		if(doForward && !foundForward){
			hasNewMessages_ = true;
			// add to unprocessed messages
			if(unprocessedMessageSize < unprocessedMessages_.length){	// array is still large enough that we may use the space
				unprocessedMessages_[unprocessedMessageSize] = message;
			} else {	// create larger arrays and insert element
				Message[] newArray = new Message[unprocessedMessageSize + 2];
				System.arraycopy (unprocessedMessages_,0,newArray,0,unprocessedMessageSize);
				newArray[unprocessedMessageSize] = message;
				unprocessedMessages_ = newArray;
			}
			++unprocessedMessageSize;
		}
	}
	
	/**
	 * Deletes a forward message and optionally adds it to the old messages.
	 * 
	 * @param position	the position
	 * @param addToOld	<code>true</code> to add it to the old messages, else <code>false</code>
	 */
	public synchronized void deleteForwardMessage(int position, boolean addToOld){
		if(position > -1 && position < forwardMessageSize_){
			if(addToOld){
				if(oldMessageSize_ + 1 > oldMessages_.length){	//need to resize
					Message[] newArray = new Message[oldMessageSize_ + 3];
					if(oldMessageSize_ > 0) System.arraycopy(oldMessages_,0,newArray,0,oldMessageSize_);
					oldMessages_ = newArray;
				
					int[] newArray2 = new int[oldMessageSize_ + 3];
					if(oldMessageSize_ > 0) System.arraycopy(oldMessageArrivalTime_,0,newArray2,0,oldMessageSize_);
					oldMessageArrivalTime_ = newArray2;				
				}
				oldMessages_[oldMessageSize_] = forwardMessages_[position];
				oldMessageArrivalTime_[oldMessageSize_] = renderer_.getTimePassed();
				++oldMessageSize_;
			}
			// dont' really remove but just change size and copy leftwards
			--forwardMessageSize_;
			System.arraycopy(forwardMessages_,position+1,forwardMessages_,position,forwardMessageSize_-position);
			System.arraycopy(forwardArrivalTime_,position+1,forwardArrivalTime_,position,forwardMessageSize_-position);
		}
	}
	
	/** 
	 * Deletes all forward messages and optionally adds them to the old messages.
	 * 
	 * @param addToOld	<code>true</code> to add all to the old messages, else <code>false</code>
	 */
	public synchronized void deleteAllForwardMessages(boolean addToOld){
		if(addToOld){
			if(oldMessageSize_ + forwardMessageSize_ > oldMessages_.length){	//need to resize
				Message[] newArray = new Message[oldMessageSize_ + 3];
				if(oldMessageSize_ > 0) System.arraycopy(oldMessages_,0,newArray,0,oldMessageSize_);
				oldMessages_ = newArray;
			
				int[] newArray2 = new int[oldMessageSize_ + 3];
				if(oldMessageSize_ > 0) System.arraycopy(oldMessageArrivalTime_,0,newArray2,0,oldMessageSize_);
				oldMessageArrivalTime_ = newArray2;				
			}
			System.arraycopy(forwardMessages_,0,oldMessages_,oldMessageSize_,forwardMessageSize_);
			int time = renderer_.getTimePassed();
			for(int i = oldMessageSize_ + forwardMessageSize_ - 1; i >= oldMessageSize_; --i){
				oldMessageArrivalTime_[i] = time;
			}
			oldMessageSize_ += forwardMessageSize_;
		}
		forwardMessageSize_ = 0;
	}
	
	/** 
	 * Deletes all forward messages which are in flooding/broadcast mode and optionally adds them to the old messages.
	 * 
	 * @param addToOld	<code>true</code> to add the removed flooding messages to the old messages, else <code>false</code>
	 */
	public synchronized void deleteAllFloodingForwardMessages(boolean addToOld){
		Message[] newArray;
		int[] newArray2;
		int time = renderer_.getTimePassed();
		for(int i = forwardMessageSize_ - 1; i > -1; --i){
			if(forwardMessages_[i].getFloodingMode()){
				if(addToOld){
					if(oldMessageSize_ + 1 > oldMessages_.length){	//need to resize
						newArray = new Message[oldMessageSize_ + 3];
						if(oldMessageSize_ > 0) System.arraycopy(oldMessages_,0,newArray,0,oldMessageSize_);
						oldMessages_ = newArray;
					
						newArray2 = new int[oldMessageSize_ + 3];
						if(oldMessageSize_ > 0) System.arraycopy(oldMessageArrivalTime_,0,newArray2,0,oldMessageSize_);
						oldMessageArrivalTime_ = newArray2;				
					}
					oldMessages_[oldMessageSize_] = forwardMessages_[i];
					oldMessageArrivalTime_[oldMessageSize_] = time;
					++oldMessageSize_;
				}
				// don't remove. just copy everything 1 step leftwards
				--forwardMessageSize_;
				System.arraycopy(forwardMessages_,i+1,forwardMessages_,i,forwardMessageSize_-i);
				System.arraycopy(forwardArrivalTime_,i+1,forwardArrivalTime_,i,forwardMessageSize_-i);
			}
		}
	}
	
	/**
	 * !!!Process messages. Note that this function is not synchronized! You need to make
	 * sure that no other thread uses any function on this object at the same time!
	 */
	public void processMessages(){
		if(executeMessageSize_ > 0){
			for(int i = 0; i < executeMessageSize_; ++i){
				if(vehicle_ != null)executeMessages_[i].execute(vehicle_);
			}
			executeMessageSize_ = 0;
		}
		
		if(unprocessedMessageSize > 0){
			if(unprocessedMessageSize + forwardMessageSize_ > forwardMessages_.length){	//need to resize
				Message[] newArray = new Message[unprocessedMessageSize + forwardMessageSize_ + 2];
				if(forwardMessageSize_ > 0) System.arraycopy(forwardMessages_,0,newArray,0,forwardMessageSize_);
				forwardMessages_ = newArray;
			
				int[] newArray2 = new int[unprocessedMessageSize + forwardMessageSize_ + 2];
				if(forwardMessageSize_ > 0) System.arraycopy(forwardArrivalTime_,0,newArray2,0,forwardMessageSize_);
				forwardArrivalTime_ = newArray2;				
			}
			// copy unprocessedMessages_ at end of forwardMessages_
			System.arraycopy(unprocessedMessages_,0,forwardMessages_,forwardMessageSize_,unprocessedMessageSize);
				
			int time = renderer_.getTimePassed();
			for(int i = forwardMessageSize_ + unprocessedMessageSize - 1; i >= forwardMessageSize_; --i){
				forwardArrivalTime_[i] = time;
			}
			
			forwardMessageSize_ += unprocessedMessageSize;			
			unprocessedMessageSize = 0;
		}
		hasNewMessages_ = false;
	}
	
	/**
	 * !!!Checks for outdated messages and deletes them. Note that this function is not synchronized! You need to make
	 * sure that no other thread uses any function on this object at the same time!
	 * 
	 * @param addToOld	<code>true</code> to add all to outdated forward messages to the old messages, else <code>false</code>
	 */
	public void checkOutdatedMessages(boolean addToOld){
		int timeout = renderer_.getTimePassed() - MAX_FORWARD_TIME;
		// Check forward messages for outdated entries		
		for(int i = forwardMessageSize_ - 1; i > -1; --i){		// going backwards because it's easier for deletion!
			if(forwardArrivalTime_[i] < timeout || !forwardMessages_[i].isValid()){
				if(addToOld){
					if(oldMessageSize_ + 1 > oldMessages_.length){	//need to resize
						Message[] newArray = new Message[oldMessageSize_ + 3];
						if(oldMessageSize_ > 0) System.arraycopy(oldMessages_,0,newArray,0,oldMessageSize_);
						oldMessages_ = newArray;
					
						int[] newArray2 = new int[oldMessageSize_ + 3];
						if(oldMessageSize_ > 0) System.arraycopy(oldMessageArrivalTime_,0,newArray2,0,oldMessageSize_);
						oldMessageArrivalTime_ = newArray2;				
					}
					oldMessages_[oldMessageSize_] = forwardMessages_[i];
					oldMessageArrivalTime_[oldMessageSize_] = renderer_.getTimePassed();
					++oldMessageSize_;
				}				
				
				--forwardMessageSize_;
				System.arraycopy(forwardMessages_,i+1,forwardMessages_,i,forwardMessageSize_-i);
				System.arraycopy(forwardArrivalTime_,i+1,forwardArrivalTime_,i,forwardMessageSize_-i);
				
				++failedToForwardCount_;
			}
		}
		
		timeout = renderer_.getTimePassed() - MAX_OLD_TIME;
		// Check old messages for outdated entries
		for(int i = oldMessageSize_ - 1; i > -1; --i){		// going backwards because it's easier for deletion!
			if(oldMessageArrivalTime_[i] < timeout  || !oldMessages_[i].isValid()){
				--oldMessageSize_;
				System.arraycopy(oldMessages_,i+1,oldMessages_,i,oldMessageSize_-i);
				System.arraycopy(oldMessageArrivalTime_,i+1,oldMessageArrivalTime_,i,oldMessageSize_-i);
			}
		}
	}	
	
	/**
	 * Gets all messages which shall be forwarded. Note that there might be garbage at the end so
	 * use the getSize()-methode to get the real size!
	 * 
	 * @return the array with all messages
	 */
	public Message[] getForwardMessages(){
		return forwardMessages_;
	}
	
	/** Returns if there are news messages which need to be processed.
	 * 
	 * @return <code>true</code> if there are new messages, else <code>false</code>
	 */
	public boolean hasNewMessages(){
		return hasNewMessages_;
	}
	
	/**
	 * Gets the amount of known messages stored (=forward messages). 
	 * 
	 * @return the size
	 */
	public int getSize(){
		return forwardMessageSize_;
	}
	
	/**
	 * Gets the amount of old messages stored. 
	 * 
	 * @return the size
	 */
	public int getOldMessagesSize(){
		return oldMessageSize_;
	}
	
	/**
	 * Gets the amount of messages which failed to be forwarded. 
	 * 
	 * @return the size
	 */
	public int getFailedForwardCount(){
		return failedToForwardCount_;
	}
	
	/**
	 * Clears everything from this data structure.
	 */
	public void clear(){
		executeMessages_ = new Message[2];
		executeMessageSize_ = 0;
		unprocessedMessages_ = new Message[2];
		unprocessedMessageSize = 0;
		forwardMessages_ = new Message[2];
		forwardMessageSize_ = 0;
		forwardArrivalTime_ = new int[2];
		oldMessages_ = new Message[2];
		oldMessageSize_ = 0;
		oldMessageArrivalTime_ = new int[2];
		failedToForwardCount_ = 0;
		hasNewMessages_ = false;
	}
}