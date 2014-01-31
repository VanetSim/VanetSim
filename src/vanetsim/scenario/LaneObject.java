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
 * This class represents an object which is on a lane of a street. Use this class as a base for all
 * object which are on a lane (for example with a vehicle).
 */
public class LaneObject{
	
	/** Link to the previous object. */
	protected LaneObject previous_;
	
	/** Link to the next object. */
	protected LaneObject next_;
	
	/** The current X coordinate. */
	protected int curX_;	
	
	/** The current Y coordinate. */
	protected int curY_;
	
	/** The current speed measured in cm/s. */
	protected double curSpeed_ = 0.0;
	
	/** The position on the street measured in cm from the startNode. */
	protected double curPosition_;
	
	/** The current lane (1-n with n=lanecount of one side of the street). */
	protected int curLane_ = 1;
	
	/** The street on which this vehicle currently moves. */
	protected Street curStreet_;
	
	/** The direction of the object on the street.<br> <code>true</code> = moving from startNode to endNode<br> <code>false</code> = moving from endNode to startNode */
	protected boolean curDirection_ = true;
	
	/**
	 * Calculate position on map (curX and curY). Needed for rendering, communication and so on.
	 */
	protected void calculatePosition(){
		double addX=0, addY=0;
		double rightmost;
		// calculate add factors
		if(curStreet_.isOneway()){			
			if(curStreet_.getLanesCount()%2 == 0) rightmost = curStreet_.getLanesCount()/2 + 0.5;	//note: division of first two integers does automatic "rounding"!
			else rightmost = curStreet_.getLanesCount()/2 + 1;
		} else rightmost = curStreet_.getLanesCount() + 0.5;
		if(curDirection_){	//lane on right side
			addX = curStreet_.getXFactor() * (rightmost - curLane_);
			addY = curStreet_.getYFactor() * (rightmost - curLane_);
		} else {	//lane on left side
			addX = - curStreet_.getXFactor() * (rightmost - curLane_);
			addY = - curStreet_.getYFactor() * (rightmost - curLane_);
		}
		// calculate position
		double percentOnStreet = curPosition_ / curStreet_.getLength();
		curX_ = (int) StrictMath.floor(0.5d + addX + curStreet_.getStartNode().getX() + ((curStreet_.getEndNode().getX() - curStreet_.getStartNode().getX())*percentOnStreet));
		curY_ = (int) StrictMath.floor(0.5d + addY + curStreet_.getStartNode().getY() + ((curStreet_.getEndNode().getY() - curStreet_.getStartNode().getY())*percentOnStreet));
	}
	
	/**
	 * Gets the current x coordinate.
	 * 
	 * @return the current x coordinate
	 */
	public int getX() {
		return curX_;
	}

	/**
	 * Gets the current y coordinate.
	 * 
	 * @return the current y coordinate
	 */
	public int getY() {
		return curY_;
	}
	
	/**
	 * Gets the current speed of this object.
	 * 
	 * @return the current speed in cm/s
	 */
	public int getCurSpeed(){
		return (int)Math.round(curSpeed_);
	}
	
	/**
	 * Gets the current relative position of this object.
	 * 
	 * @return the current position measured in cm from startNode
	 */
	public double getCurPosition(){
		return curPosition_;
	}
	
	/**
	 * Gets the current lane.
	 * 
	 * @return the lane measured from the right side of the street
	 */
	public int getCurLane(){
		return curLane_;
	}
	
	/**
	 * Gets the current street.
	 * 
	 * @return the street
	 */
	public Street getCurStreet(){
		return curStreet_;
	}
	
	/**
	 * Gets the current direction on the street.
	 * 
	 * @return <code>true</code> = moving from startNode to endNode<br> <code>false</code> = moving from endNode to startNode
	 */
	public boolean getCurDirection(){
		return curDirection_;
	}
	
	/**
	 * Returns the LaneObject after this one.
	 * 
	 * @return the next
	 */
	public LaneObject getNext() {
		return next_;
	}

	/**
	 * Returns the LaneObject before this one.
	 * 
	 * @return the previous
	 */
	public LaneObject getPrevious() {
		return previous_;
	}

	/**
	 * Sets the LaneObject after this one.
	 * 
	 * @param next	the object which comes after this one
	 */
	public void setNext(LaneObject next) {
		next_ = next;
	}

	/**
	 * Sets the LaneObject before this one.
	 * 
	 * @param previous	the object which comes before this one
	 */
	public void setPrevious(LaneObject previous) {
		previous_ = previous;
	}
}