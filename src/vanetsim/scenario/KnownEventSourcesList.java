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


import vanetsim.gui.helpers.GeneralLogWriter;

/**
 * ...
 */
public class KnownEventSourcesList{
		
	/** How many hash buckets will be used. Increase if you expect lots of known event sources! */
	private static final int HASH_SIZE = 16;
	
	/** How much time has passed since beginning of the simulation. Stored here as it's really needed often. */
	private static int timePassed_ = 0;
	
	/** The array with all heads of the linked lists */
	private KnownEventSource[] head_ = new KnownEventSource[HASH_SIZE];
	
	/** The amount of items stored. */
	private int size_ = 0;

	/** counts updated event sources */
	private int updatedSources = 0;
	
	/** counts created sources */
	private int createdSources = 0;	
	
	/** Save the time between updates */
	private int timeBetweenUpdates = 0; 
	
	/** variable to save spamCounters */
	private int spamCount = 0;
	
	/**
	 * Empty constructor.
	 */
	public KnownEventSourcesList(long ID){
		for(int i = 0; i < HASH_SIZE; ++i){
			head_[i] = null;
		}
	}
		
	/**
	 * Update a event source or add it if it doesn't exist yet.
	 * 
	 * @param vehicle	a reference to the vehicle
	 * @param ID		the ID of the vehicle
	 * @param x			the x coordinate
	 * @param y			the y coordinate
	 * @param speed		the speed
	 * @param sourceID	ID of the source
	 * @param isEncrypted	if Beacon was encrypted
	 * @param isARSU	if Beacon was sent from an ARSU
	 */
	public synchronized void update(Vehicle vehicle, long ID, int x, int y, double speed, boolean isFake){
		boolean found = false;
		int hash = (int)(ID % HASH_SIZE);
		if(hash < 0) hash = -hash;
		KnownEventSource next = head_[hash];
		
		while(next != null){
			if(next.getID() == ID){	// update of entry possible
				updatedSources++;
				next.setX(x);
				next.setY(y);
				timeBetweenUpdates = timeBetweenUpdates + (timePassed_ - next.getLastUpdate());
				next.setLastUpdate(timePassed_);
				next.setSpeed(speed);		
				next.setUpdates_(next.getUpdates_()+1);
				if(isFake)next.setFakeMessageCounter_(next.getFakeMessageCounter_() + 1);
				else next.setRealMessageCounter_(next.getRealMessageCounter_() + 1);
				
				found = true;

				break;
			}
			next = next.getNext();
		}					
		
		if(!found){
			createdSources++;
			next = new KnownEventSource(vehicle, ID, x, y, speed, timePassed_, isFake);
			next.setNext(head_[hash]);
			next.setPrevious(null);
			if(head_[hash] != null) head_[hash].setPrevious(next);
			head_[hash] = next;
			++size_;
		}
	}
	

	
	/**
	 * Gets an hashed array with known vehicles (array length depends on the HASH_SIZE). You can iterate through 
	 * all known vehicles by using <code>getNext()</code> until you get to a <code>null</code> element on all 
	 * elements of this array
	 * 
	 * @return the array with known vehicles
	 */
	public KnownEventSource[] getFirstKnownEventSource(){
		return head_;
	}
	
	/**
	 * Gets the amount of known vehicles stored.
	 * 
	 * @return the size
	 */
	public int getSize(){
		return size_;
	}
	
	/**
	 * Sets the time passed since simulation start.
	 * 
	 * @param time the new time in milliseconds
	 */
	public static void setTimePassed(int time){
		timePassed_ = time;
	}
	


	public int getUpdatedSources() {
		return updatedSources;
	}

	public void setUpdatedSources(int updatedSources) {
		this.updatedSources = updatedSources;
	}

	public int getCreatedSources() {
		return createdSources;
	}

	public void setCreatedSources(int createdSources) {
		this.createdSources = createdSources;
	}

	/**
	 * Clears everything from this data structure.
	 */
	public void clear(){
		//activate this for spam measurements
		writeSpam();
		head_ = new KnownEventSource[HASH_SIZE];
		size_ = 0;
	}
	

	/**
	 * write output file
	 */
	public void writeOutputFile(){
		String output = "";
		KnownEventSource next;
		for(int i = 0; i < HASH_SIZE; ++i){
			next = head_[i];
			while(next != null){			
				output += next.getUpdates_() + "#";
				next.setUpdates_(0);
				next = next.getNext();
				
				
			}
		}	
		if(output.length() > 0) GeneralLogWriter.log("***:" + output.subSequence(0, (output.length()-1)));
	}
	
	/**
	 * write output file
	 */
	public void writeSpam(){
		KnownEventSource next;
		for(int i = 0; i < HASH_SIZE; ++i){
			next = head_[i];
			while(next != null){			
				spamCount += next.getSpamCounter_();
				next.setSpamCounter_(0);
				next = next.getNext();		
			}
		}	
	}
	
	
	public int getTimeBetweenUpdates() {
		return timeBetweenUpdates;
	}


	public void setTimeBetweenUpdates(int timeBetweenUpdates) {
		this.timeBetweenUpdates = timeBetweenUpdates;
	}

	public int getSpamCount() {
		return spamCount;
	}

	public void setSpamCount(int spamCount) {
		this.spamCount = spamCount;
	}
}