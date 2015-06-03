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

import vanetsim.map.Street;

/**
 * A list of all known vehicles which was discovered through beacons. In contrast to the KnownPenalties-
 * class, an own class is used for storing the information about the vehicles. Although this means slightly
 * more overhead, it should not be a big case and allows better extensibility.<br>
 * A simple hash algorithm based on the vehicle ID is used to get better performance. The hash determines the 
 * corresponding linked list(beginnings of linked lists are found in <code>head_</code>). Known vehicles with the
 * same hash are connected together through their <code>next_</code> and <code>previous_</code> values (see 
 * KnownVehicle-class).
 */
public class IDSProcessorList{
	
	/** How long the timeout is in milliseconds. If a vehicle wasn't updated for this time, 
	 * it is dropped from the list! */
	private static final int VALID_TIME = 1000;
	
	/** How many hash buckets will be used. Increase if you expect lots of known vehicles! */
	private static final int HASH_SIZE = 32;
	
	/** How much time has passed since beginning of the simulation. Stored here as it's really needed often. */
	private static int timePassed_ = 0;
	
	/** The array with all heads of the linked lists */
	private IDSProcessor[] head_ = new IDSProcessor[HASH_SIZE];
	
	/** The amount of items stored. */
	private int size_ = 0;
	
	/** the vehicle this structure belongs to */
	private Vehicle vehicle_;
	/**
	 * Empty constructor.
	 */
	public IDSProcessorList(Vehicle vehicle){
		vehicle_ = vehicle;
		
		for(int i = 0; i < HASH_SIZE; ++i){
			head_[i] = null;
		}
	}
		
	/**
	 * Update a vehicle or add it if it doesn't exist yet.
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
	public synchronized void updateProcessor(long ID, int x, int y, double speed, int lane){
		int hash = (int)(ID % HASH_SIZE);
		if(hash < 0) hash = -hash;
		IDSProcessor next = head_[hash];
		while(next != null){
			if(next.getMonitoredVehicleID_() == ID){	// update of entry possible
				next.addBeacon(lane, x, y, speed, timePassed_);
			}
			next = next.getNext();
		}					
	}
	
	public void fireIDSProcessors(){
		IDSProcessor next;
		for(int i = 0; i < HASH_SIZE; ++i){
			next = head_[i];
			while(next != null){
				if(next.isReady_() && !next.isDeleteProcessor_())next.checkIDS();
				next = next.getNext();	
			}
		}	
		
		vehicle_.setCheckIDSProcessors_(false);
	}
	
	public boolean createIDSProcessor(Vehicle vehicle, long ID, long monitoredVehicleID, int x, int y, Street street, int direction, Vehicle penaltySourceVehicle, String penaltyType, boolean isFake, boolean emergencyVehicle, boolean createBlocking){
		if(!IDSProcessor.ruleIsActive(penaltyType)) return false;
		int hash = (int)(monitoredVehicleID % HASH_SIZE);
		if(hash < 0) hash = -hash;
		IDSProcessor next = head_[hash];


		//check if processor already aktiv
		
		while(next != null){
			if((next.getID_() == ID)){ // remove!
				
			}
			next = next.getNext();	// still works as we didn't change it. Garbage Collector will remove it now.
		}

		next = new IDSProcessor(x, y, street, direction, vehicle, ID, monitoredVehicleID, penaltySourceVehicle, penaltyType, isFake, emergencyVehicle, createBlocking, timePassed_, vehicle_);

		next.setNext(head_[hash]);
		next.setPrevious(null);
		if(head_[hash] != null) head_[hash].setPrevious(next);
		head_[hash] = next;
		++size_;
		
	
		return true;
	}
	
	public boolean createIDSProcessor(Vehicle vehicle, long ID, long monitoredVehicleID, int x, int y, Street street, int direction, Vehicle penaltySourceVehicle, String penaltyType, boolean isFake, boolean emergencyVehicle, boolean createBlocking, boolean instantIDS){
		if(!IDSProcessor.ruleIsActive(penaltyType)) return false;
		int hash = (int)(monitoredVehicleID % HASH_SIZE);
		if(hash < 0) hash = -hash;
		
		IDSProcessor next = head_[hash];

		
		next = new IDSProcessor(x, y, street, direction, vehicle, ID, monitoredVehicleID, penaltySourceVehicle, penaltyType, isFake, emergencyVehicle, createBlocking, timePassed_, vehicle_);
		next.setNext(head_[hash]);
		next.setPrevious(null);
		if(head_[hash] != null) head_[hash].setPrevious(next);
		head_[hash] = next;
		++size_;
		if(instantIDS){
			next.setReady_(true);
			vehicle.setCheckIDSProcessors_(true);
		}
		return true;
	}
	
	/**
	 * Checks if a processor is too old so that it can be removed. Note that this function is not synchronized! You need to make
	 * sure that no other thread uses any function on this object at the same time!
	 */
	public void checkOutdatedProcessors(){
		int timeout = timePassed_ - VALID_TIME;
		IDSProcessor next;
		for(int i = 0; i < HASH_SIZE; ++i){
			next = head_[i];
			while(next != null){
				if((next.getLastUpdate() < timeout) || next.isDeleteProcessor_()){ // remove!
					if(next.getNext() != null) next.getNext().setPrevious(next.getPrevious());
					if(next.getPrevious() != null) next.getPrevious().setNext(next.getNext());
					else { //it is the head!
						head_[i] = next.getNext();
					}
					--size_;
				}
				next = next.getNext();	// still works as we didn't change it. Garbage Collector will remove it now.
			}
		}		
	}
	
	
	/**
	 * create IDS Processor for correct known vehicle
	 */
	/*
	public boolean createIDSProcessor(int x, int y, Street street, int direction, Vehicle vehicle_, long ID, Vehicle penaltySourceVehicle,  String penaltyType, boolean isFake, boolean emergencyVehicle, boolean createBlocking){
		if(penaltyType.equals("EVA_EMERGENCY_ID")) {
			updateVehicle(vehicle_, ID, x, y, 0, 0, false, false);
		}

		if(!IDSProcessor2.ruleIsActive(penaltyType)) return false;
		int hash = (int)(ID % HASH_SIZE);
		if(hash < 0) hash = -hash;
		KnownVehicle next = head_[hash];
		while(next != null){
			if(next.getID() == ID){	
				IDSProcessor2 processor = next.getHead_();
				if(processor == null){
					next.setHead_(new IDSProcessor2(next, x, y, street, direction, vehicle_, ID, penaltySourceVehicle, penaltyType, isFake, emergencyVehicle, createBlocking));
				}
				else{
					while(processor.getNext_() != null ) processor = processor.getNext_();
					processor.setNext_(new IDSProcessor2(next, x, y, street, direction, vehicle_, ID, penaltySourceVehicle, penaltyType, isFake, emergencyVehicle, createBlocking));	
				}
				
				
				
				return true;
			}
			next = next.getNext();
		}
		return false;
	}
	
	*/
	
	/**
	 * return an information how long this vehicle is known and the time of persistent contact (null if it doesn't know this vehicle)
	 */
	/*
	public int[] checkBeacons(long ID){
		int[] returnValue = new int[2];
		int hash = (int)(ID % HASH_SIZE);
		if(hash < 0) hash = -hash;
		KnownVehicle next = head_[hash];
		while(next != null){
			if(next.getID() == ID){	
				returnValue[0] = next.getFirstContact_();
				returnValue[1] = next.getPersistentContactCount();
				return returnValue;
			}
			next = next.getNext();
		}
		return null;
	}
	*/
	/**
	 * contacts all known vehicles for beacon information
	 */
	/*
	public double[] getBeaconInformationFromVehicles(long monitoredID){
		int knownTime = 0;
		int constantContact = 0;
		int counter = 0;
		int[] response;
		for(int i = 0; i < HASH_SIZE; ++i){
			KnownVehicle next = head_[i];
			while(next != null){
				response = next.getVehicle().getKnownVehiclesList().checkBeacons(monitoredID);
				if(response != null){
					knownTime += timePassed_ - response[0];
					constantContact += response[1];
					counter++;
				}
				next = next.getNext();
			}
		}
		double[] responseValue = {((double)knownTime/counter), ((double)constantContact/counter)};
		return responseValue;
	}
	*/
	
	/**
	 * returns  with this vehicle (-1 if it doesn't know this vehicle)
	 */
	/*
	public int getPersistentContactCount(long ID){
		int hash = (int)(ID % HASH_SIZE);
		if(hash < 0) hash = -hash;
		KnownVehicle next = head_[hash];
		while(next != null){
			if(next.getID() == ID){	
				
			}
			next = next.getNext();
		}
		return -1;
	}
	*/
	/**
	 * Gets an hashed array with known vehicles (array length depends on the HASH_SIZE). You can iterate through 
	 * all known vehicles by using <code>getNext()</code> until you get to a <code>null</code> element on all 
	 * elements of this array
	 * 
	 * @return the array with known ids processors
	 */
	public IDSProcessor[] getFirstProcessor(){
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
	
	/**
	 * Clears everything from this data structure.
	 */
	public void clear(){
		head_ = new IDSProcessor[HASH_SIZE];
		size_ = 0;
	}
	

}