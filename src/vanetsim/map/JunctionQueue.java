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
package vanetsim.map;

import vanetsim.gui.Renderer;
import vanetsim.scenario.Vehicle;

/**
 * A queue for the vehicles waiting on a junction.
 */
public final class JunctionQueue{
	
	/** How long (in milliseconds) a vehicle may stay in the list of waiting vehicles without being updated through
	 * calling the addVehicle-function. */
	private static final int LAST_SEEN_TIMEOUT = 2500;
	
	/** A static reference to the renderer. */
	private static final Renderer renderer_ = Renderer.getInstance();
	
	/** The vehicles in this queue. */
	private Vehicle[] vehicles_;
	
	/** Since when (simulation time) the vehicles in this queue are waiting. */
	private int[] waitingSince_;
	
	/** When (simulation time) the vehicles in this queue were last seen. */
	private int[] lastSeen_;
	
	/** The size of the vehicles in this queue. */
	private int size_ = 0;
	
	/**
	 * Constructor.
	 */
	public JunctionQueue(){
		vehicles_ = new Vehicle[2];
		waitingSince_ = new int[2];
		lastSeen_ = new int[2];
	}
	
	/**
	 * Adds a vehicle to the queue. If it already exists, the lastSeen-time is updated.
	 * 
	 * @param vehicle the vehicle to add
	 * @return <code>true</code> if a vehicle was added, <code>false</code> if it previously existed in this queue.
	 */
	public synchronized boolean addVehicle(Vehicle vehicle){
		int i;
		for(i = 0; i < size_; ++i){
			if(vehicles_[i] == vehicle){
				lastSeen_[i] = renderer_.getTimePassed();
				return false;
			}
		}
		
		if(size_ >= vehicles_.length){	// create larger arrays if necessary
			Vehicle[] newArray = new Vehicle[size_ + 2];
			System.arraycopy (vehicles_,0,newArray,0,size_);
			vehicles_ = newArray;
			
			int[] newArray2 = new int[size_ + 2];
			System.arraycopy (waitingSince_,0,newArray2,0,size_);
			waitingSince_ = newArray2;
			
			newArray2 = new int[size_ + 2];
			System.arraycopy (lastSeen_,0,newArray2,0,size_);
			lastSeen_ = newArray2;
		}
		int curTime = renderer_.getTimePassed();
		// find other vehicles which were inserted in the current step in order to get a thread-safe ordering!
		for(i = size_ - 1; i > -1; --i){
			if(waitingSince_[i] != curTime) break;
			else if (vehicles_[i].getX() > vehicle.getX()) break;
			else if (vehicles_[i].getX() == vehicle.getX()){
				if (vehicles_[i].getY() > vehicle.getY()) break;
				else if (vehicles_[i].getY() > vehicle.getY()){
					if (vehicles_[i].hashCode() > vehicle.hashCode()) break;
				}
			}
		}
		++size_;
		++i;
		vehicles_[i] = vehicle;
		waitingSince_[i] = curTime;
		lastSeen_[i] = curTime;		
		return true;
	}
	
	/**
	 * Removes a vehicle from the queue. 
	 * 
	 * @param vehicle	the vehicles to remove
	 * @return <code>true</code> if a vehicle was found and deleted, else <code>false</code>
	 */
	public synchronized boolean delVehicle(Vehicle vehicle){
		for(int i = 0; i < size_; ++i){
			if(vehicles_[i] == vehicle){
				--size_;
				System.arraycopy(vehicles_,i+1,vehicles_,i,size_-i);
				System.arraycopy(waitingSince_,i+1,waitingSince_,i,size_-i);
				System.arraycopy(lastSeen_,i+1,lastSeen_,i,size_-i);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Deletes the first vehicle in this queue.
	 */
	public synchronized void delFirstVehicle(){
		if(size_ > 0){
			--size_;
			if(size_ > 0){
				System.arraycopy(vehicles_,1,vehicles_,0,size_);
				System.arraycopy(waitingSince_,1,waitingSince_,0,size_);
				System.arraycopy(lastSeen_,1,lastSeen_,0,size_);
			}				
		}
	}
	
	/**
	 * Cleans up. This removes vehicles which haven't been seen for a long time and thus prevents from stalls.
	 */
	public synchronized void cleanUp(){
		int i, checkTime = renderer_.getTimePassed() - LAST_SEEN_TIMEOUT;
		for(i = size_ - 1; i > -1; --i){	// going backwards because it's easier for deletion!
			if(lastSeen_[i] < checkTime){
				--size_;
				System.arraycopy(vehicles_,i+1,vehicles_,i,size_-i);
				System.arraycopy(waitingSince_,i+1,waitingSince_,i,size_-i);
				System.arraycopy(lastSeen_,i+1,lastSeen_,i,size_-i);
			}
		}
	}
	
	/**
	 * Gets the first vehicle in this queue.
	 * 
	 * @return the first vehicle in this queue
	 */
	public Vehicle getFirstVehicle(){
		if(size_ > 0) return vehicles_[0];
		else return null;
	}
	
	/**
	 * Returns the size of this queue.
	 * 
	 * @return size_ the size of this queue
	 */
	public int size(){
		return size_;
	}
}