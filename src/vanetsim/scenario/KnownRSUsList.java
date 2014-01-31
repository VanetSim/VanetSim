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
import vanetsim.gui.helpers.AttackLogWriter;

/**
 * A list of all known Road-Side-Units which was discovered through beacons. In contrast to the KnownPenalties-
 * class, an own class is used for storing the information about the RSUs. Although this means slightly
 * more overhead, it should not be a big case and allows better extensibility.<br>
 * A simple hash algorithm based on the RSU ID is used to get better performance. The hash determines the 
 * corresponding linked list(beginnings of linked lists are found in <code>head_</code>). Known RSUs with the
 * same hash are connected together through their <code>next_</code> and <code>previous_</code> values (see 
 * KnownRSU-class).
 */
public class KnownRSUsList{
	
	/** How long the timeout is in milliseconds. If a vehicle wasn't updated for this time, 
	 * it is dropped from the list! */
	private static final int VALID_TIME = 2000;
	
	/** How many hash buckets will be used. Increase if you expect lots of known RSUs! */
	private static final int HASH_SIZE = 16;
	
	/** How much time has passed since beginning of the simulation. Stored here as it's really needed often. */
	private static int timePassed_ = 0;
	
	/** The array with all heads of the linked lists */
	private KnownRSU[] head_ = new KnownRSU[HASH_SIZE];
	
	/** The amount of items stored. */
	private int size_ = 0;
	
	/**
	 * Empty constructor.
	 */
	public KnownRSUsList(){
		for(int i = 0; i < HASH_SIZE; ++i){
			head_[i] = null;
		}
	}
		
	/**
	 * Update a RSU or add it if it doesn't exist yet.
	 * 
	 * @param rsu	a reference to the RSU
	 * @param ID		the ID of the RSU
	 * @param x			the x coordinate
	 * @param y			the y coordinate
	 * @param isEncrypted	if Beacon was encrypted
	 */
	public synchronized void updateRSU(RSU rsu, long ID, int x, int y, boolean isEncrypted){
		boolean found = false;
		int hash = (int)(ID % HASH_SIZE);
		if(hash < 0) hash = -hash;
		KnownRSU next = head_[hash];
		while(next != null){
			if(next.getID() == ID){	// update of entry possible
				next.setX(x);
				next.setY(y);
				next.setEncrypted(isEncrypted);
				next.setLastUpdate(timePassed_+VALID_TIME);
				found = true;
				break;
			}
			next = next.getNext();
		}					
		
		if(!found){
			next = new KnownRSU(rsu, ID, x, y, isEncrypted, timePassed_);
			next.setNext(head_[hash]);
			next.setPrevious(null);
			if(head_[hash] != null) head_[hash].setPrevious(next);
			head_[hash] = next;
			++size_;
		}
		
		AttackLogWriter.log(Renderer.getInstance().getTimePassed() + ":Any RSU Communication:" + rsu.getRSUID() + ":Any-Vehicle Data:" + Long.toHexString(ID) + ":" +  x + ":" +  y + ":" + isEncrypted);
	}
	
	/**
	 * Checks if a RSU is too old so that it can be removed. Note that this function is not synchronized! You need to make
	 * sure that no other thread uses any function on this object at the same time!
	 */
	public void checkOutdatedRSUs(){
		int timeout = timePassed_ - VALID_TIME;
		KnownRSU next;
		for(int i = 0; i < HASH_SIZE; ++i){
			next = head_[i];
			while(next != null){
				if(next.getLastUpdate() < timeout){ // remove!
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
	 * Finds the nearest known RSU to a destination. Only searches for non encrypted RSUs
	 * 
	 * @param rsuX		the x coordinate of the calling RSU
	 * @param rsuY		the y coordinate of the calling RSU
	 * @param destX			the x coordinate of the destination
	 * @param destY			the y coordinate of the destination
	 * @param maxDistance	the maximum distance the nearest RSU max have from the calling RSU
	 * 
	 * @return the nearest RSU or <code>null</code> if the calling RSU is the nearest
	 */
	public RSU findNearestRSU(int rsuX, int rsuY, int destX, int destY, int maxDistance){
		double tmpDistance, bestDistance;
		long dx = rsuX - destX;
		long dy = rsuY - destY;
		long maxDistanceSquared = (long)maxDistance * maxDistance;
		bestDistance = dx * dx + dy * dy;		// Pythagorean theorem but without costly sqrt because it's unnecessary
		KnownRSU bestKnownRSU = null;
		KnownRSU next;
		for(int i = 0; i < HASH_SIZE; ++i){
			next = head_[i];
			while(next != null){
				dx = next.getX() - destX;
				dy = next.getY() - destY;
				tmpDistance = dx * dx + dy * dy;
				if(tmpDistance < bestDistance && !next.isEncrypted()){
					dx = next.getX() - rsuX;
					dy = next.getY() - rsuY;
					if((dx * dx + dy *dy) < maxDistanceSquared){	// needs to be inside maximum distance
						bestDistance = tmpDistance;
						bestKnownRSU = next;
					}
				}
				next = next.getNext();
			}
		}		
		if(bestKnownRSU != null) return bestKnownRSU.getRSU();
		else return null;
	}
	
	/**
	 * Gets an hashed array with known RSUs (array length depends on the HASH_SIZE). You can iterate through 
	 * all known RSUs by using <code>getNext()</code> until you get to a <code>null</code> element on all 
	 * elements of this array
	 * 
	 * @return the array with known RSUs
	 */
	public KnownRSU[] getFirstKnownRSU(){
		return head_;
	}
	
	/**
	 * Gets the amount of known RSUs stored.
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
		head_ = new KnownRSU[HASH_SIZE];
		size_ = 0;
	}
}