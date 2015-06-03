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
package vanetsim.gui.helpers;


import vanetsim.scenario.Vehicle;

public class MonitoredVehicle {
	/** The vehicle associated. */
	private final Vehicle vehicle_;
	
	/** the timestamp of the last received beacon */
	private int lastUpdate_;
	
	/** the ID of the vehicle */
	private long ID;
	
	/** the lane of the current street */
	private int[] lane;
	
	/** the distance to brake */
	private int[] distance;
	
	/** the speed of the vehicle */
	private double[] speed;
	
	/** the index of the container */
	private int actualIndex = 0;
	
	/** Link to the previous object. */
	protected MonitoredVehicle previous_;
	
	/** Link to the next object. */
	protected MonitoredVehicle next_;
	
	public MonitoredVehicle(Vehicle vehicle, int theLane, int x1, int x2, int y1, int y2, double theSpeed, long theID){
		vehicle_ = vehicle;
		ID = theID;
		lane[actualIndex] = theLane;
		distance[actualIndex] = (int) Math.sqrt(x1*x2 + y1*y2);
		speed[actualIndex] = theSpeed;
		actualIndex++;
		actualIndex = actualIndex%3;
	}
	
	public void updateVehicle(int theLane, int x1, int x2, int y1, int y2, double theSpeed){
		lane[actualIndex] = theLane;
		distance[actualIndex] = (int) Math.sqrt(x1*x2 + y1*y2);
		speed[actualIndex] = theSpeed;
		actualIndex++;
		actualIndex = actualIndex%3;
	}

	/**
	 * Updates the last modification time.
	 * 
	 * @param time	the current time
	 */
	public void setLastUpdate(int time){
		lastUpdate_ = time;
	}
	
	/**
	 * @return the iD
	 */
	public long getID() {
		return ID;
	}

	/**
	 * @param iD the iD to set
	 */
	public void setID(long iD) {
		ID = iD;
	}

	/**
	 * @return the lane
	 */
	public int[] getLane() {
		return lane;
	}

	/**
	 * @param lane the lane to set
	 */
	public void setLane(int[] lane) {
		this.lane = lane;
	}

	/**
	 * @return the distance
	 */
	public int[] getDistance() {
		return distance;
	}

	/**
	 * @param distance the distance to set
	 */
	public void setDistance(int[] distance) {
		this.distance = distance;
	}

	/**
	 * @return the speed
	 */
	public double[] getSpeed() {
		return speed;
	}

	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(double[] speed) {
		this.speed = speed;
	}

	/**
	 * @return the actualIndex
	 */
	public int getActualIndex() {
		return actualIndex;
	}

	/**
	 * @param actualIndex the actualIndex to set
	 */
	public void setActualIndex(int actualIndex) {
		this.actualIndex = actualIndex;
	}

	/**
	 * @return the lastUpdate_
	 */
	public int getLastUpdate_() {
		return lastUpdate_;
	}

	/**
	 * @param lastUpdate_ the lastUpdate_ to set
	 */
	public void setLastUpdate_(int lastUpdate_) {
		this.lastUpdate_ = lastUpdate_;
	}

	/**
	 * @return the next_
	 */
	public MonitoredVehicle getNext_() {
		return next_;
	}

	/**
	 * @param next_ the next_ to set
	 */
	public void setNext_(MonitoredVehicle next_) {
		this.next_ = next_;
	}

	/**
	 * @return the previous_
	 */
	public MonitoredVehicle getPrevious_() {
		return previous_;
	}

	/**
	 * @param previous_ the previous_ to set
	 */
	public void setPrevious_(MonitoredVehicle previous_) {
		this.previous_ = previous_;
	}

	/**
	 * @return the vehicle_
	 */
	public Vehicle getVehicle_() {
		return vehicle_;
	}
}
