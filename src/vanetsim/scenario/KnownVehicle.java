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
 * A known vehicle (discovered by receiving a beacon). The variables represent what is known and might
 * differ from the real ones if it hasn't been updated for some time!
 */
public class KnownVehicle{
	
	/** The vehicle associated. */
	private final Vehicle vehicle_;
	
	/** The ID of the vehicle. */
	private final long ID_;
	
	/** The time of the first contact with the vehicle */
	private final int firstContact_;
	
	/** The current x coordinate. */
	private int x_;

	/** The current y coordinate. */
	private int y_;	
	
	/** The current speed. */
	private double speed_;
	
	/** If the beacon is encrypted */
	private boolean isEncrypted_;
	
	/** The time when the vehicle was last updated in milliseconds. */
	private int lastUpdate_;
	
	/** Link to the previous object. */
	protected KnownVehicle previous_;
	
	/** Link to the next object. */
	protected KnownVehicle next_;
	
	/** Array to save the n last x for the IDS */
	private int[] savedX_;
	
	/** Array to save the n last y for the IDS */
	private int[] savedY_;
	
	/** Array to save the n last speed for the IDS */
	private double[] savedSpeed_;
	
	/** Array to save the n last lastUpdate for the IDS */
	private int[] savedLastUpdate_;
	
	/** Amount of saved beacons (-1 == off) */
	private static int amountOfSavedBeacons_ = 10;
	
	/** counter to fill the array */
	private int arrayCounter = -1;
	
	
	/**
	 * Instantiates a new known vehicle.
	 * 
	 * @param vehicle	the vehicle
	 * @param ID		the ID of the vehicle
	 * @param x 		the x coordinate
	 * @param y			the y coordinate
	 * @param time		the current time
	 * @param speed		the current speed
	 * @param isEncrypted	if Beacon was encrypted
	 */
	public KnownVehicle(Vehicle vehicle, long ID, int x, int y, int time, double speed, boolean isEncrypted, int timePassed){
		vehicle_ = vehicle;
		ID_ = ID;
		x_ = x;
		y_ = y;
		speed_ = speed;
		lastUpdate_ = time;
		isEncrypted_ = isEncrypted;
		firstContact_ = timePassed;
		if(amountOfSavedBeacons_ != -1){
			arrayCounter = -1;
			savedX_ = new int[amountOfSavedBeacons_];
			savedY_ = new int[amountOfSavedBeacons_];
			savedSpeed_ = new double[amountOfSavedBeacons_];
			for(int i = 0; i < amountOfSavedBeacons_;i++) savedSpeed_[i] = -1;
			savedLastUpdate_ = new int[amountOfSavedBeacons_];
		}
		
		
	}

	/**
	 *  test the persistent contact the vehicle had
	 */
	public int getPersistentContactCount(){
		int beaconInterval = Vehicle.getBeaconInterval();
		int counter = 1;
		int savedLastUpdate = savedLastUpdate_[0];
		for(int i = 1; i <  amountOfSavedBeacons_; i++){
			if((savedLastUpdate_[i] - savedLastUpdate) == beaconInterval){
				counter++;
			}
			savedLastUpdate = savedLastUpdate_[i];

		}
		if((savedLastUpdate_[0] - savedLastUpdate_[amountOfSavedBeacons_-1]) == beaconInterval){
			counter++;
		}
		return counter;
	}
	
	/**
	 *  get the time for how long this vehicle has been seen standing around. returns a counter (0:countStanding;1:countNotEmpty)
	 */
	public int[] getTimeStanding(){
		int[] counter = new int[2];
		
		for(int i = 0; i <  amountOfSavedBeacons_; i++){
			//skip empty beacon slots (-1)
			if(savedSpeed_[i] != -1){
				counter[1]++;
				if(savedSpeed_[i] == 0){
					counter[0]++;
				}
			}
		}
		return counter;
	}
	
	
	/**
	 *  get the time for how long this vehicle has been seen standing around. returns a counter (0:countStanding;1:countNotEmpty)
	 */
	public void showSpeedData(){
		int starter = arrayCounter + 1;
		if(starter == amountOfSavedBeacons_) starter = 0;
		for(int i = starter; i <  (amountOfSavedBeacons_ + starter); i++){
			System.out.println("savedspeed:" + savedSpeed_[i%(amountOfSavedBeacons_)]);
		}
	}

	public double[] getSpecificSpeedDataSet(int index){
		double[] returnValue = new double[2];
		returnValue[0] = savedSpeed_[(arrayCounter + 1 + index)%amountOfSavedBeacons_];
		returnValue[1] = savedSpeed_[(amountOfSavedBeacons_ + arrayCounter)%(amountOfSavedBeacons_)];
		return returnValue;
	}
	
	/**
	 * Updates the x coordinate.
	 * 
	 * @param x		the x coordinate
	 */
	public void setX(int x){
		x_ = x;
	}
	
	/**
	 * Updates the y coordinate.
	 * 
	 * @param y		the y coordinate
	 */
	public void setY(int y){
		y_ = y;
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
	 * Updates the speed.
	 * 
	 * @param speed	the current speed
	 */
	public void setSpeed(double speed){
		speed_ = speed;
	}
	
	/**
	 * Gets the x coordinate.
	 * 
	 * @return the x coordinate
	 */
	public int getX(){
		return x_;
	}
	
	/**
	 * Gets the y coordinate.
	 * 
	 * @return the y coordinate
	 */
	public int getY(){
		return y_;
	}
	
	/**
	 * Gets the ID.
	 * 
	 * @return the ID
	 */
	public long getID(){
		return ID_;
	}
	
	/**
	 * Gets the speed
	 * 
	 * @return the speed
	 */
	public double getSpeed(){
		return speed_;
	}
	
	/**
	 * Gets the vehicle.
	 * 
	 * @return the vehicle
	 */
	public Vehicle getVehicle(){
		return vehicle_;
	}
	
	/**
	 * Gets when this vehicle was last updated.
	 * 
	 * @return the last update time in milliseconds
	 */
	public int getLastUpdate(){
		return lastUpdate_;
	}
	
	/**
	 * Returns the KnownVehicle after this one.
	 * 
	 * @return the next
	 */
	public KnownVehicle getNext() {
		return next_;
	}

	/**
	 * Returns the KnownVehicle before this one.
	 * 
	 * @return the previous
	 */
	public KnownVehicle getPrevious() {
		return previous_;
	}

	/**
	 * Sets the KnownVehicle after this one.
	 * 
	 * @param next	the object which comes after this one
	 */
	public void setNext(KnownVehicle next) {
		next_ = next;
	}

	/**
	 * Sets the KnownVehicle before this one.
	 * 
	 * @param previous	the object which comes before this one
	 */
	public void setPrevious(KnownVehicle previous) {
		previous_ = previous;
	}


	public boolean isEncrypted_() {
		return isEncrypted_;
	}


	public void setEncrypted_(boolean isEncrypted_) {
		this.isEncrypted_ = isEncrypted_;
	}


	/**
	 * @return the savedX_
	 */
	public int[] getSavedX_() {
		return savedX_;
	}


	/**
	 * @param savedX_ the savedX_ to set
	 */
	public void setSavedX_(int[] savedX_) {
		this.savedX_ = savedX_;
	}


	/**
	 * @return the savedY_
	 */
	public int[] getSavedY_() {
		return savedY_;
	}


	/**
	 * @param savedY_ the savedY_ to set
	 */
	public void setSavedY_(int[] savedY_) {
		this.savedY_ = savedY_;
	}


	/**
	 * @return the savedSpeed_
	 */
	public double[] getSavedSpeed_() {
		return savedSpeed_;
	}


	/**
	 * @param savedSpeed_ the savedSpeed_ to set
	 */
	public void setSavedSpeed_(double[] savedSpeed_) {
		this.savedSpeed_ = savedSpeed_;
	}


	/**
	 * @return the savedLastUpdate_
	 */
	public int[] getSavedLastUpdate_() {
		return savedLastUpdate_;
	}


	/**
	 * @param savedLastUpdate_ the savedLastUpdate_ to set
	 */
	public void setSavedLastUpdate_(int[] savedLastUpdate_) {
		this.savedLastUpdate_ = savedLastUpdate_;
	}


	/**
	 * @return the aMOUNT_OF_SAVED_BEACONS
	 */
	public static int getAmountOfSavedBeacons_() {
		return amountOfSavedBeacons_;
	}


	/**
	 * @param aMOUNT_OF_SAVED_BEACONS the aMOUNT_OF_SAVED_BEACONS to set
	 */
	public static void setAmountOfSavedBeacons(int amountOfSavedBeacons) {
		amountOfSavedBeacons_ = amountOfSavedBeacons;
	}


	/**
	 * @return the arrayCounter
	 */
	public int getArrayCounter() {
		return arrayCounter;
	}


	/**
	 * @param arrayCounter the arrayCounter to set
	 */
	public void setArrayCounter(int arrayCounter) {
		this.arrayCounter = arrayCounter;
	}
	
	/**
	 * @return the firstContact_
	 */
	public int getFirstContact_() {
		return firstContact_;
	}

}