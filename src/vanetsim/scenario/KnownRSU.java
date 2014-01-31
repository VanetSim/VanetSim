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
 * A known RSU (discovered by receiving a beacon). The variables represent what is known and might
 * differ from the real ones if it hasn't been updated for some time!
 */
public class KnownRSU{
	
	/** The RSU associated. */
	private final RSU rsu_;
	
	/** The ID of the RSU. */
	private final long ID_;
	
	/** The current x coordinate. */
	private int x_;

	/** The current y coordinate. */
	private int y_;	
	
	/** If the beacon is encrypted */
	private boolean isEncrypted_;	
	
	/** The time when the RSU was last updated in milliseconds. */
	private int lastUpdate_;
	
	/** Link to the previous object. */
	protected KnownRSU previous_;
	
	/** Link to the next object. */
	protected KnownRSU next_;
	
	/**
	 * Instantiates a new known RSU
	 * 
	 * @param rsu		the Road-Side-Unit
	 * @param ID		the ID of the RSU
	 * @param x 		the x coordinate
	 * @param y			the y coordinate
	 * @param time		the current time
	 * @param isEncrypted	if RSU sends encrypted
	 */
	public KnownRSU(RSU rsu, long ID, int x, int y, boolean isEncrypted, int time){
		rsu_ = rsu;
		ID_ = ID;
		x_ = x;
		y_ = y;
		isEncrypted_ = isEncrypted;
		lastUpdate_ = time;
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
	 * Gets the RSU.
	 * 
	 * @return the Road-Side-Unit
	 */
	public RSU getRSU(){
		return rsu_;
	}
	
	/**
	 * Gets when this RSU was last updated.
	 * 
	 * @return the last update time in milliseconds
	 */
	public int getLastUpdate(){
		return lastUpdate_;
	}
	
	/**
	 * Returns the KnownRSU after this one.
	 * 
	 * @return the next
	 */
	public KnownRSU getNext() {
		return next_;
	}

	/**
	 * Returns the KnownRSU before this one.
	 * 
	 * @return the previous
	 */
	public KnownRSU getPrevious() {
		return previous_;
	}

	/**
	 * Sets the KnownRSU after this one.
	 * 
	 * @param next	the object which comes after this one
	 */
	public void setNext(KnownRSU next) {
		next_ = next;
	}

	/**
	 * Sets the KnownRSU before this one.
	 * 
	 * @param previous	the object which comes before this one
	 */
	public void setPrevious(KnownRSU previous) {
		previous_ = previous;
	}


	public boolean isEncrypted() {
		return isEncrypted_;
	}


	public void setEncrypted(boolean isEncrypted) {
		this.isEncrypted_ = isEncrypted;
	}
}