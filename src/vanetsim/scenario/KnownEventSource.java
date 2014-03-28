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
 * A known event source saves data of vehicles that sent a event to use the data for ids purpose
 */
public class KnownEventSource{
	
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
	
	/** The time when the vehicle was last updated in milliseconds. */
	private int lastUpdate_;
	
	/** The supposed fake messages counter. */
	private int fakeMessageCounter_;
	
	/** The supposed real messages counter. */
	private int realMessageCounter_;
	
	/** Link to the previous object. */
	protected KnownEventSource previous_;
	
	/** Link to the next object. */
	protected KnownEventSource next_;

	/** The updates of the eventsource */
	private int updates_ = 0;
	
	/** Alert if spamming */
	private static boolean spamCheck_ = false;
	
	/** Threshold what message amount is spamming */
	private static int spammingThreshold_ = 3;
	
	/** Threshold in what time the messages where sent in average */
	private static int spammingTimeThreshold_ = 240000;
	
	private int spamCounter_ = 0;
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
	public KnownEventSource(Vehicle vehicle, long ID, int x, int y, double speed, int timePassed, boolean isFake){
		vehicle_ = vehicle;
		ID_ = ID;
		x_ = x;
		y_ = y;
		speed_ = speed;
		lastUpdate_ = timePassed;
		firstContact_ = timePassed;
		
		if(isFake) fakeMessageCounter_++;
		else realMessageCounter_++;
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
	public KnownEventSource getNext() {
		return next_;
	}

	/**
	 * Returns the KnownVehicle before this one.
	 * 
	 * @return the previous
	 */
	public KnownEventSource getPrevious() {
		return previous_;
	}

	/**
	 * Sets the KnownVehicle after this one.
	 * 
	 * @param next	the object which comes after this one
	 */
	public void setNext(KnownEventSource next) {
		next_ = next;
	}

	/**
	 * Sets the KnownVehicle before this one.
	 * 
	 * @param previous	the object which comes before this one
	 */
	public void setPrevious(KnownEventSource previous) {
		previous_ = previous;
	}

	/**
	 * @return the firstContact_
	 */
	public int getFirstContact_() {
		return firstContact_;
	}


	public int getFakeMessageCounter_() {
		return fakeMessageCounter_;
	}


	public void setFakeMessageCounter_(int fakeMessageCounter_) {
		this.fakeMessageCounter_ = fakeMessageCounter_;
	}


	public int getRealMessageCounter_() {
		return realMessageCounter_;
	}


	public void setRealMessageCounter_(int realMessageCounter_) {
		this.realMessageCounter_ = realMessageCounter_;
	}


	public int getUpdates_() {
		return updates_;
	}


	public void setUpdates_(int updates) {
		updates_ = updates;
		
		if(spamCheck_){
			if(updates_ >= (spammingThreshold_-1) && (lastUpdate_ - firstContact_- (updates_*80)) <= updates_*spammingTimeThreshold_){
				spamCounter_++;
			}
		}
	}


	public int getSpamCounter_() {
		return spamCounter_;
	}


	public void setSpamCounter_(int spamCounter_) {
		this.spamCounter_ = spamCounter_;
	}


	public static int getSpammingthreshold() {
		return spammingThreshold_;
	}


	public static int getSpammingtimethreshold() {
		return spammingTimeThreshold_;
	}

	public static void setSpammingThreshold_(int spammingThreshold_) {
		KnownEventSource.spammingThreshold_ = spammingThreshold_;
	}
	
	public static void setSpammingTimeThreshold_(int spammingTimeThreshold_) {
		KnownEventSource.spammingTimeThreshold_ = spammingTimeThreshold_;
	}


	public static boolean isSpamcheck() {
		return spamCheck_;
	}


	public static void setSpamCheck_(boolean spamCheck_) {
		KnownEventSource.spamCheck_ = spamCheck_;
	}



}