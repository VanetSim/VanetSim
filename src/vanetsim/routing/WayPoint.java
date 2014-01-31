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
package vanetsim.routing;

import java.text.ParseException;

import vanetsim.localization.Messages;
import vanetsim.map.MapHelper;
import vanetsim.map.Street;

/**
 * A waypoint used for routing.
 */
public final class WayPoint{
	
	/** The x coordinate. */
	private final int x_;
	
	/** The y coordinate. */
	private final int y_;
	
	/** The time to wait BEFORE movement to the next waypoint starts. */
	private int waitTime_;
	
	/** The street on which this waypoint is located. */
	private final Street street_;
	
	/** The position on the street measured from the StartNode in cm. */
	private final double positionOnStreet_;
	
	/**
	 * Instantiates a new waypoint.
	 * 
	 * @param x			the x coordinate
	 * @param y			the y coordinate
	 * @param waitTime	the time to wait BEFORE movement to the next waypoint starts.
	 * 
	 * @throws ParseException if the coordinates supplied couldn't be matched to a street within 100m distance.
	 */
	public WayPoint(int x, int y, int waitTime) throws ParseException{
		// Calculate point on street
		int[] nearestpoint = new int[2];
		street_ = MapHelper.findNearestStreet(x,y, 10000, new double[1], nearestpoint);	// search in 100m radius for the starting street
		if(street_ != null){
			x_ = nearestpoint[0];
			y_ = nearestpoint[1];
			// position on street is measured from startNode to nearestpoint
			long tmp1 = street_.getStartNode().getX() - x_;
			long tmp2 = street_.getStartNode().getY() - y_;
			positionOnStreet_ = Math.sqrt(tmp1 * tmp1 + tmp2 * tmp2); 	// Pythagorean theorem: a^2 + b^2 = c^2	
			waitTime_ = waitTime;
		} else throw new ParseException(Messages.getString("WayPoint.snappingFailed"),0); //$NON-NLS-1$
	}
	
	/**
	 * Gets the position on the street.
	 * 
	 * @return the position in cm measured from the startNode
	 */
	public double getPositionOnStreet(){
		return positionOnStreet_;
	}
	
	/**
	 * Gets the street on which this waypoint is located.
	 * 
	 * @return the street
	 */
	public Street getStreet(){
		return street_;
	}
	
	/**
	 * Gets the time to wait BEFORE movement to the next waypoint begins.
	 * 
	 * @return the time to wait BEFORE movement to the next waypoint starts
	 */
	public int getWaittime(){
		return waitTime_;
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
	 * Sets the wait time
	 * @param waitTime
	 */
	public void setWaittime(int waitTime){
		waitTime_ = waitTime;
	}
}