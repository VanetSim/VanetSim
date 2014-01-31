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
package vanetsim.scenario.messages;


import vanetsim.gui.Renderer;
import vanetsim.scenario.Vehicle;

/**
 * An abstract class for messages.
 */
public abstract class Message{
	
	/** A reference to the renderer. */
	private static final Renderer renderer_ = Renderer.getInstance();
	
	/** The x coordinate of the destination of this message. */
	protected int destinationX_;
	
	/** The y coordinate of the destination of this message. */
	protected int destinationY_;
	
	/** The destination radius in cm. */
	protected int destinationRadius_;
	
	/** The squared destination radius in cm^2 (as it's often needed). */
	protected long destinationRadiusSquared_;
	
	/** If flooding mode is enabled. <code>true</code> if flooding of this message to all vehicles is done, 
	 * <code>false</code> if line-forwarding is done*/
	private boolean floodingMode_ = false;
	
	/** How long this message is valid. */
	protected int validUntil_;	
	
	/** Flags if the message is fake **/
	protected boolean isFake_;
	
	/** The steadyID of the source of the message **/
	protected long ID_;

	
	/**
	 * Checks if the message is still valid.
	 * 
	 * @return <code>true</code> if it's valid, else <code>false</code>
	 */
	public boolean isValid(){
		if(renderer_.getTimePassed() < validUntil_) return true;
		else return false;
	}
	
	/**
	 * Gets the x coordinate of the destination.
	 * 
	 * @return the x destination
	 */
	public int getDestinationX_(){
		return destinationX_;
	}
	
	/**
	 * Gets the y coordinate of the destination.
	 * 
	 * @return the y destination
	 */
	public int getDestinationY_(){
		return destinationY_;
	}
	
	/**
	 * Returns if flooding mode is enabled on this message
	 * 
	 * @return <code>true</code> if flooding of this message to all vehicles is done, 
	 * <code>false</code> if line-forwarding is done
	 */
	public boolean getFloodingMode(){
		return floodingMode_;
	}
	
	/**
	 * Sets if flooding mode is enabled on this message
	 * 
	 * @param mode	<code>true</code> if flooding of this message to all vehicles is done, 
	 * 				<code>false</code> if line-forwarding is done
	 */
	public void setFloodingMode(boolean mode){
		floodingMode_ = mode;
	}
	
	/**
	 * Gets the destination radius. If the message arrives in this circular destination area, it 
	 * should be broadcasted to all vehicles.
	 * 
	 * @return the tolerance in cm
	 */
	public int getDestinationRadius(){
		return destinationRadius_;
	}
	
	/**
	 * Gets the squared destination radius.
	 * 
	 * @return the squared radius of the destination region in cm^2
	 */
	public long getDestinationRadiusSquared(){
		return destinationRadiusSquared_;
	}
	
	/**
	 * Executes something on a vehicle given.
	 * 
	 * @param vehicle	the vehicle
	 */
	public abstract void execute(Vehicle vehicle);

	/**
	 * @return the isFake_
	 */
	public boolean isFake_() {
		return isFake_;
	}

	/**
	 * @param isFake_ the isFake_ to set
	 */
	public void setFake_(boolean isFake_) {
		this.isFake_ = isFake_;
	}

}