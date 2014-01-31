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
package vanetsim.scenario.events;


import vanetsim.map.Street;
import vanetsim.scenario.LaneObject;
import vanetsim.scenario.Vehicle;

public class BlockingObject extends LaneObject{
	
	/**
	 * Instantiates a new blocking object.
	 * 
	 * @param lane the lane number (1..n)
	 * @param direction the direction
	 * @param street the street
	 * @param position the position on the street
	 */
	
	/** The type of penalty */
	private final String penaltyType_;
	private int timestamp_;
	
	public BlockingObject(int lane, boolean direction, Street street, double position, String penaltyType){
		curLane_ = lane;
		curDirection_ = direction;
		curStreet_ = street;
		penaltyType_ = penaltyType;
		if(curLane_ < 1) curLane_ = 1;
		else if (curLane_ > curStreet_.getLanesCount()) curLane_ = curStreet_.getLanesCount();
		curPosition_ = position;
		curStreet_.addLaneObject(this, curDirection_);
	}
	
	public BlockingObject(int lane, boolean direction, Street street, double position, String penaltyType, int timestamp, int x, int y){
		curLane_ = lane;
		curDirection_ = direction;
		curStreet_ = street;
		penaltyType_ = penaltyType;
		if(curLane_ < 1) curLane_ = 1;
		else if (curLane_ > curStreet_.getLanesCount()) curLane_ = curStreet_.getLanesCount();
		curPosition_ = position;
		curStreet_.addLaneObject(this, curDirection_);
		timestamp_ = timestamp;
		curX_ = x;
		curY_ = y;
	}
	
	/**
	 * Remove this BlockingObject from it's current lane.
	 */ 
	public void removeFromLane(){
		curStreet_.delLaneObject(this, curDirection_);
	}
	
	/**
	 * Remove this BlockingObject from it's current lane if enough time is passed.
	 */ 
	public boolean removeFromLane(Vehicle tmp, int timePassed){
		if(timePassed > timestamp_){
			curStreet_.delLaneObject(this, curDirection_);
			return true;
		}
		else return false;
	}

	/**
	 * @return the penaltyType_
	 */
	public String getPenaltyType_() {
		return penaltyType_;
	}
}