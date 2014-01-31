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

/**
 * A LaneContainer contains all LaneObjects in one direction of a street. The objects may be on different lanes.
 * but they must all be in the same direction!<br>
 * It's basically a kind of a queue implemented as a LinkedList. The references to the previous and next
 * elements are stored in the LaneObjects which makes the implementation quite efficient as no search is
 * necessary for removal. Thus, it always takes O(1) to remove an object. Adding is O(n) in worst case but in most
 * cases, insertion is made in O(1) as the object is added at the head. In order to update positions
 * (for example when an overhaul occurs) you don't need to remove and re-add (which could be quite costly) but 
 * rather just call <code>updatePosition()</code>. Checking for the next or previous LaneObject is O(1) and no
 * lookup is necessary (directly stored within the object).<br>
 * Insertion, removal and update is synchronized. As iterating through the objects needs to be done externally 
 * (by calling <code>getNext()</code> or <code>getPrevious()</code>), it is not synchronized!
 */
public class LaneContainer{
	
	/** The direction of this container.<br> <code>true</code> = going from startNode to endNode<br> <code>false</code> = going from endNode to startNode */
	protected final boolean direction_;

	/** The head of the lane container. */
	protected LaneObject head_;

	/** The tail of the lane container. */
	protected LaneObject tail_;

	/** The number of elements in this lane container. */
	protected int size_ = 0;
	
	/**
	 * Instantiates a new lane container.
	 * 
	 * @param direction	<code>true</code> if this lane is from startNode to endNode of the street, else <code>false</code>
	 */
	public LaneContainer(boolean direction){
		head_ = null;
		tail_ = null;
		direction_ = direction;
	}
	/*
	public synchronized void doMagic(LaneObject object, double newPosition, String mode){
		if(mode.equals("addSorted")){
			if (size_ == 0 || head_ == null){	//empty
				head_ = tail_ = object;
			} else {
				if((direction_ && object.getCurPosition() < head_.getCurPosition()) || (!direction_ && object.getCurPosition() > head_.getCurPosition())){
					object.setNext(head_);
					head_.setPrevious(object);
					head_ = object;
				} else {
					LaneObject tmpObject = head_;
					boolean isLargest = true;
					if(direction_){
						for(int i = 1; i < size_; ++i){
							tmpObject = tmpObject.getNext();
							if(tmpObject.getCurPosition() > object.getCurPosition()){
								isLargest = false;
								break;				
							}
						}
					} else {
						for(int i = 1; i < size_; ++i){
							tmpObject = tmpObject.getNext();
							if(tmpObject.getCurPosition() < object.getCurPosition()){
								isLargest = false;
								break;			
							}
						}
					}
					if(isLargest){	//will be the new tail
						object.setNext(null);
						object.setPrevious(tail_);
						tail_.setNext(object);
						tail_ = object;
					} else {	//somewhere in the middle
						object.setNext(tmpObject);
						object.setPrevious(tmpObject.getPrevious());
						tmpObject.getPrevious().setNext(object);
						tmpObject.setPrevious(object);
					}
				}
			}
			++size_;
		}
		else if(mode.equals("updatePosition")){
			object.curPosition_ = newPosition;
			if(size_ > 1){
				LaneObject nextObject = object.getNext();
				LaneObject prevObject = object.getPrevious();

				int needsupdate = 0;
				if(direction_){
					if(nextObject != null && nextObject.getCurPosition() < object.getCurPosition()) needsupdate = 1;
					else if(prevObject != null && prevObject.getCurPosition() > object.getCurPosition()) needsupdate = 2;
				} else {
					if(nextObject != null && nextObject.getCurPosition() > object.getCurPosition()) needsupdate = 1;
					else if(prevObject != null && prevObject.getCurPosition() < object.getCurPosition()) needsupdate = 2;
				}
				if(needsupdate > 0){
					if(needsupdate == 1){
						if(direction_){
							while(nextObject != null){
								if(nextObject.getCurPosition() > object.getCurPosition()) break;
								nextObject = nextObject.getNext();
							}
						} else {
							while(nextObject != null){
								if(nextObject.getCurPosition() < object.getCurPosition()) break;
								nextObject = nextObject.getNext();
							}
						}
						if(object == head_){
							head_ = object.getNext();
							object.getNext().setPrevious(null);
						} else {
							object.getPrevious().setNext(object.getNext());
							object.getNext().setPrevious(object.getPrevious());
						}
						if(nextObject == null){	//will get the last object in the list!
							object.setNext(null);
							object.setPrevious(tail_);
							tail_.setNext(object);
							tail_ = object;
						} else {	//insert before nextObject
							object.setNext(nextObject);
							object.setPrevious(nextObject.getPrevious());
							nextObject.getPrevious().setNext(object);
							nextObject.setPrevious(object);
						}
					} else {
						if(direction_){
							while(prevObject != null){
								if(prevObject.getCurPosition() < object.getCurPosition()) break;
								prevObject = prevObject.getPrevious();
							}
						} else {
							while(prevObject != null){
								if(prevObject.getCurPosition() > object.getCurPosition()) break;
								prevObject = prevObject.getPrevious();
							}
						}
						if(object == tail_){
							tail_ = object.getPrevious();
							object.getPrevious().setNext(null);
						} else {
							object.getPrevious().setNext(object.getNext());
							object.getNext().setPrevious(object.getPrevious());
						}
						if(prevObject == null){	//will get the head in the list!
							object.setNext(head_);
							object.setPrevious(null);
							head_.setPrevious(object);
							head_ = object;
						} else {	//insert after prevObject
							object.setPrevious(prevObject);
							object.setNext(prevObject.getNext());
							prevObject.getNext().setPrevious(object);
							prevObject.setNext(object);
						}
					}
				}	
						
			}
		}
		else if(mode.equals("remove")){
			LaneObject prev = object.getPrevious();
			LaneObject next = object.getNext();
			if (next == null){
				if(prev == null){	//_object is the last one on the lane
					head_ = null;
					tail_ = null;
				} else {
					prev.setNext(null);	//_object is the tail
					tail_ = prev;
					object.setPrevious(null);
				}
			} else if (prev == null){ //_object is the head
				next.setPrevious(null);
				head_ = next;
				object.setNext(null);			
			} else {	// somewhere in the middle
				prev.setNext(next);
				next.setPrevious(prev);
				object.setNext(null);
				object.setPrevious(null);
			}
			--size_;
		}
	}
	
	*/
	/**
	 * Add an element so that it's correctly ordered inside the lane container.
	 * 
	 * @param object	the object to add
	 */
	
	public synchronized void addSorted(LaneObject object) {
		if (size_ == 0 || head_ == null){	//empty
			head_ = tail_ = object;
		} else {
			if((direction_ && object.getCurPosition() < head_.getCurPosition()) || (!direction_ && object.getCurPosition() > head_.getCurPosition())){
				object.setNext(head_);
				head_.setPrevious(object);
				head_ = object;
			} else {
				LaneObject tmpObject = head_;
				boolean isLargest = true;
				if(direction_){
					for(int i = 1; i < size_; ++i){
						tmpObject = tmpObject.getNext();
						if(tmpObject.getCurPosition() > object.getCurPosition()){
							isLargest = false;
							break;				
						}
					}
				} else {
					for(int i = 1; i < size_; ++i){
						tmpObject = tmpObject.getNext();
						if(tmpObject.getCurPosition() < object.getCurPosition()){
							isLargest = false;
							break;			
						}
					}
				}
				if(isLargest){	//will be the new tail
					object.setNext(null);
					object.setPrevious(tail_);
					tail_.setNext(object);
					tail_ = object;
				} else {	//somewhere in the middle
					object.setNext(tmpObject);
					object.setPrevious(tmpObject.getPrevious());
					tmpObject.getPrevious().setNext(object);
					tmpObject.setPrevious(object);
				}
			}
		}
		++size_;
	}
	
	/**
	 * Updates position of a LaneObject and changes the order in this LaneContainer to guarantee a consistent state.
	 * 
	 * @param object 		the object to check
	 * @param newPosition	the new position of the object
	 */
	
	public synchronized void updatePosition(LaneObject object, double newPosition){
		object.curPosition_ = newPosition;
		if(size_ > 1){
			LaneObject nextObject = object.getNext();
			LaneObject prevObject = object.getPrevious();

			int needsupdate = 0;
			if(direction_){
				if(nextObject != null && nextObject.getCurPosition() < object.getCurPosition()) needsupdate = 1;
				else if(prevObject != null && prevObject.getCurPosition() > object.getCurPosition()) needsupdate = 2;
			} else {
				if(nextObject != null && nextObject.getCurPosition() > object.getCurPosition()) needsupdate = 1;
				else if(prevObject != null && prevObject.getCurPosition() < object.getCurPosition()) needsupdate = 2;
			}
			if(needsupdate > 0){
				if(needsupdate == 1){
					if(direction_){
						while(nextObject != null){
							if(nextObject.getCurPosition() > object.getCurPosition()) break;
							nextObject = nextObject.getNext();
						}
					} else {
						while(nextObject != null){
							if(nextObject.getCurPosition() < object.getCurPosition()) break;
							nextObject = nextObject.getNext();
						}
					}
					if(object == head_){
						head_ = object.getNext();
						object.getNext().setPrevious(null);
					} else {
						object.getPrevious().setNext(object.getNext());
						object.getNext().setPrevious(object.getPrevious());
					}
					if(nextObject == null){	//will get the last object in the list!
						object.setNext(null);
						object.setPrevious(tail_);
						tail_.setNext(object);
						tail_ = object;
					} else {	//insert before nextObject
						object.setNext(nextObject);
						object.setPrevious(nextObject.getPrevious());
						nextObject.getPrevious().setNext(object);
						nextObject.setPrevious(object);
					}
				} else {
					if(direction_){
						while(prevObject != null){
							if(prevObject.getCurPosition() < object.getCurPosition()) break;
							prevObject = prevObject.getPrevious();
						}
					} else {
						while(prevObject != null){
							if(prevObject.getCurPosition() > object.getCurPosition()) break;
							prevObject = prevObject.getPrevious();
						}
					}
					if(object == tail_){
						tail_ = object.getPrevious();
						object.getPrevious().setNext(null);
					} else {
						object.getPrevious().setNext(object.getNext());
						object.getNext().setPrevious(object.getPrevious());
					}
					if(prevObject == null){	//will get the head in the list!
						object.setNext(head_);
						object.setPrevious(null);
						head_.setPrevious(object);
						head_ = object;
					} else {	//insert after prevObject
						object.setPrevious(prevObject);
						object.setNext(prevObject.getNext());
						prevObject.getNext().setPrevious(object);
						prevObject.setNext(object);
					}
				}
			}	
					
		}
	}

	/**
	 * Removes an object. You must make sure that the object is really in this lane container. Otherwise this function might do
	 * very bad things (the size variable is decreased)!
	 * 
	 * @param object the object to remove
	 */
	public synchronized void remove(LaneObject object) {
		
		LaneObject prev = object.getPrevious();
		LaneObject next = object.getNext();
		if (next == null){
			if(prev == null){	//_object is the last one on the lane
				head_ = null;
				tail_ = null;
			} else {
				prev.setNext(null);	//_object is the tail
				tail_ = prev;
				object.setPrevious(null);
			}
		} else if (prev == null){ //_object is the head
			next.setPrevious(null);
			head_ = next;
			object.setNext(null);			
		} else {	// somewhere in the middle
			prev.setNext(next);
			next.setPrevious(prev);
			object.setNext(null);
			object.setPrevious(null);
		}
		--size_;
		
	}
	
	/**
	 * Gets the head.
	 * 
	 * @return the first object
	 */
	public LaneObject getHead(){
		return head_;
	}

	/**
	 * Gets the tail.
	 * 
	 * @return the last object
	 */
	public LaneObject getTail(){
		return tail_;
	}

	/**
	 * Gets the current amount of objects on this lane container.
	 * 
	 * @return the size
	 */
	public int size(){
		return size_;
	}
	
	/**
	 * Removes all elements from this container.
	 */
	public void clear(){
		head_ = null;
		tail_ = null;
		size_ = 0;
	}
}