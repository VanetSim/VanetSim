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

import vanetsim.map.Street;
import vanetsim.scenario.Vehicle;

/**
 * A message which indicates some kind of traffic jam through assigning a penalty to the street on which the jam is.
 */
public class PenaltyMessage extends Message{
	
	/** The penalty street. */
	private final Street penaltyStreet_;
	
	/** The direction. <code>1</code> means from endNode to startNode, <code>0</code> means
	 * 	both directions and <code>-1</code> means from startNode to endNode */
	private final int penaltyDirection_;
	
	/** A value for the penalty in cm. */
	private final int penaltyValue_;
	
	/** Until when this penalty is valid. */
	private final int penaltyValidUntil_;
	
	/** The x destination where the message occured */
	private final int x_;
	
	/** The y destination where the message occured */
	private final int y_;

	/** if sender is a emergency vehicle */
	private boolean emergencyVehicle_;
	
	/** the lane */
	private final int lane_;
	
	/** flag if a blocking will be created */
	private final boolean createBlocking_;
	
	/** the vehicle object (used to compare routes of emergency vehicle) */
	private Vehicle penaltySourceVehicle_ = null;
	
	private boolean logData_ = false;
	
	/** The type of penalty. Emergency Electronic Brake lights (EEBL, 1), Post Crash Notification (PCN, 2), Road Hazard Condition Notification (RHCN, 3), Road Feature Notification (RFN, 4), 
Stopped/Slow Vehicle Advisor (SVA, 5), Cooperative Collision Warning (CCW, 6), Cooperative Violation Warning (CVW, 7), Congested Road Notification(CRN, 8),
Change of Lanes (CL, 9), Emergency Vehicle approaching (EVA, 10). */
	private final String penaltyType_;



	/**
	 * Instantiates a new penalty message.
	 * 
	 * @param destinationX		the x coordinate of the destination of the message
	 * @param destinationY		the y coordinate of the destination of the message
	 * @param destinationRadius	the radius of the destination area in cm
	 * @param validUntil		how long the message is valid in ms (measured from simulation start)
	 * @param penaltyStreet		the penalty street
	 * @param penaltyDirection	the direction to which the penalty corresponds. <code>1</code> means from endNode to startNode, 
	 * 							<code>0</code> means both directions and <code>-1</code> means from startNode to endNode
	 * @param penaltyValue		the penalty value in cm
	 * @param penaltyValidUntil	how long the penalty is valid in ms (measured from simulation start)
	 */
	public PenaltyMessage(int x, int y, int destinationX, int destinationY, int destinationRadius, int validUntil, Street penaltyStreet, int lane, int penaltyDirection, int penaltyValue, int penaltyValidUntil, boolean isFake, long ID, Vehicle penaltySourceVehicle, String penaltyType, boolean emergencyVehicle, boolean createBlocking){
		destinationX_ = destinationX;
		destinationY_ = destinationY;
		x_ = x;
		y_ = y;
		destinationRadius_ = destinationRadius;
		destinationRadiusSquared_ = (long)destinationRadius * destinationRadius;
		validUntil_ = validUntil;
		penaltyStreet_ = penaltyStreet;
		penaltyDirection_ = penaltyDirection;
		penaltyValue_ = penaltyValue;
		penaltyValidUntil_ = penaltyValidUntil;
		isFake_ = isFake;
		ID_ = ID;
		penaltyType_ = penaltyType;
		emergencyVehicle_ = emergencyVehicle;
		lane_ = lane;
		createBlocking_ = createBlocking;
		penaltySourceVehicle_ = penaltySourceVehicle;
		
		if(logData_ && penaltyType.equals("HUANG_PCN")){
			penaltySourceVehicle.setLogBeaconsAfterEvent_(true);
			penaltySourceVehicle.setBeaconString_(penaltyType + "," + isFake);
			penaltySourceVehicle.setAmountOfLoggedBeacons_(0);
		}
	}

	
	
	/**
	 * Executes the message by adding a new penalty value to the known penalties of the vehicle given. 
	 * Includes IDS if activated
	 * 
	 * @param vehicle	the vehicle on which this operation is done
	 * 
	 * @see vanetsim.scenario.messages.Message#execute(vanetsim.scenario.Vehicle)
	 */
	public void execute(Vehicle vehicle){
		vehicle.getKnownPenalties().updatePenalty(x_, y_, penaltyStreet_, lane_, penaltyDirection_, penaltyValue_, penaltyValidUntil_, isFake_, penaltyType_, ID_, penaltySourceVehicle_, emergencyVehicle_, createBlocking_);
	}

}