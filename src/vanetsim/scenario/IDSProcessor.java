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



import vanetsim.gui.helpers.IDSLogWriter;
import vanetsim.gui.helpers.LocationInformationLogWriter;
import vanetsim.map.Street;

/**
 * A known vehicle (discovered by receiving a beacon). The variables represent what is known and might
 * differ from the real ones if it hasn't been updated for some time!
 */
public class IDSProcessor{

	/** The time when the vehicle was last updated in milliseconds. */
	private int lastUpdate_;
	
	/** Link to the previous object. */
	protected IDSProcessor previous_;
	
	/** Link to the next object. */
	protected IDSProcessor next_;
	
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
	
	private static String[] activeRules_;
	
	private Vehicle sourceVehicle_;
	private long ID_;
	private long monitoredVehicleID_;
	private String rule_;
	private int[] lane_;
	private int[] x_;
	private int [] y_;
	private int sourceX_;
	private int sourceY_;
	private double[] speed_;
	private int AMOUNT_OF_BEACONS_LOGGED = 16;
	private boolean isFake_;
	private Street street_;
	private int directionAsNumber_;
	private static int[] falsePositiv = new int[6];
	private static int[] falseNegativ = new int[6];
	private static int[] truePositiv = new int[6];
	private static int[] trueNegativ = new int[6];
	private Vehicle penaltySourceVehicle_;
	
	private static int PCNDistance_ = 625;
	private static double PCNFORWARDThreshold_ = 0.5;
	private static double RHCNThreshold_ = 0.5;
	private static double EEBLThreshold_ = 0.5;
	private static double EVAFORWARDThreshold_ = 0.5;
	private static double EVABeaconTimeFactor_ = 2;
	private static double EVABeaconFactor_ = 3;

	/** flag to activate the advanced IDS attack rules*/
	private static boolean advancedIDSRules_ = true;
	
	private boolean ready_ = false;
	
	private boolean deleteProcessor_ = false;
	
	public static int fake = 0;
	public static int noFake = 0;
	
	/** a flag to make no logging (0), compact logging (1) and large logging (2) */
	private int loggingType_ = 1;
	
	/** A JList to save all available IDS rules **/
	private static String[] idsData_ = {"HUANG_EEBL", "HUANG_PCN", "PCN_FORWARD", "HUANG_RHCN", "HUANG_EVA_FORWARD", "EVA_EMERGENCY_ID"};
	
	private static boolean logIDS_ = true;
	
	private int instantIDS_ = -1;
	
	/** the vehicle this structure belongs to */
	private Vehicle vehicle_;

	/** a flag to start in classic HUANG mode (eebl and rhcn) are with static distances */
	private boolean classicMode_ = false;
	
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
	public IDSProcessor(int x, int y, Street street,  int direction, Vehicle sourceVehicle, long ID, long monitoredVehicleID, Vehicle penaltySourceVehicle, String rule, boolean isFake, boolean emergencyVehicle, boolean createBlocking, int time, Vehicle thisVehicle){
		lastUpdate_ = time;
		vehicle_ = thisVehicle;
		monitoredVehicleID_ = monitoredVehicleID;
		sourceVehicle_ = sourceVehicle;
		ID_ = ID;
		rule_ = rule;
		isFake_ = isFake;
		street_ = street;
		directionAsNumber_ = direction;
		
		lane_ = new int[AMOUNT_OF_BEACONS_LOGGED];
		x_ = new int[AMOUNT_OF_BEACONS_LOGGED];
		y_ = new int[AMOUNT_OF_BEACONS_LOGGED];
		sourceX_ = x;
		sourceY_ = y;
		speed_ = new double[AMOUNT_OF_BEACONS_LOGGED];
		penaltySourceVehicle_ = penaltySourceVehicle;
	
		
		for(int i = 0; i < AMOUNT_OF_BEACONS_LOGGED; i++){
			lane_[i] = -1;
			x_[i] = -1;
			y_[i] = -1;
			speed_[i] = -1;
		}
		
		if(loggingType_ > 1)writeLog(sourceVehicle_.getID() +  ":Monitoring:" + monitoredVehicleID_ + ":for:" + rule_);
	}

	/**
	 *  test the persistent contact the vehicle had
	 */
	public int getPersistentContactCount(){
		int beaconInterval = Vehicle.getBeaconInterval();
		int counter = 0;
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
	

	/** adds Beacons to processor */
	public synchronized void addBeacon(int lane, int x, int y, double speed, int timePassed){
		if(loggingType_ > 1)writeLog("Added Beacon:" + sourceVehicle_.getID() +  ":" + monitoredVehicleID_);
		lastUpdate_ = timePassed;
		for(int i = 0; i < speed_.length; i++){
			if(speed_[i] == -1){
				lane_[i] = lane;
				x_[i] = x;
				y_[i] = y;
				speed_[i] = speed;
				
				if(i == (speed_.length - 1) || instantIDS_  == 0){
					ready_ = true;
					sourceVehicle_.setCheckIDSProcessors_(true);

					instantIDS_ = -1;


				}
				if(instantIDS_ > -1) instantIDS_--;
				break;
			}
		}
	}

	
	public String checkIDS(){
		//can be deleted after that
		lastUpdate_ = 0;
		
		ready_ = false;
		deleteProcessor_ = true;
		int lastLoggedBeacon = 0; //in the case where not enough Beacons are logged for IDS because the contact has been lost
		for(;lastLoggedBeacon < AMOUNT_OF_BEACONS_LOGGED; lastLoggedBeacon++) if(speed_[lastLoggedBeacon] == -1) break;
		if(lastLoggedBeacon == 0 && !rule_.equals("EVA_EMERGENCY_ID")) return "";
		
		boolean quit = true;
		for(int j = 0; j < activeRules_.length;j++)if(activeRules_[j].equals(rule_)){
			quit = false;
		}
		if(quit) return "";
		
		double distance = 0;
		double dx = 0;
		double dy = 0;

		
		
		if(rule_.equals("HUANG_PCN")){		
			dx = x_[0] -  x_[(lastLoggedBeacon-1)];
			dy = y_[0] -  y_[(lastLoggedBeacon-1)];
			
			
			distance = Math.sqrt(dx * dx + dy * dy);
			if(advancedIDSRules_){
				//get the time standing in beacon amount
				int beaconsWithZeroSpeed = 0;
				//we do not need the last one
				for(int i = 0; i < AMOUNT_OF_BEACONS_LOGGED-1; i++) if(speed_[i] == 0) beaconsWithZeroSpeed++;
				int[] timeStanding = vehicle_.getKnownVehiclesList().hasBeenSeenWaitingFor(monitoredVehicleID_);
				if((timeStanding[0] - beaconsWithZeroSpeed) < (timeStanding[1] - (AMOUNT_OF_BEACONS_LOGGED-1)) || (timeStanding[0] == 0 && PCNDistance_ < distance)){

					if(loggingType_ > 0){
						LocationInformationLogWriter.log("Type:HUANG_PCN:Source:" + "MDS:RESTNET:"+ x_[0] + ":" + y_[0] + ":Attack:" + true + ":Correct:" + (isFake_==true));
						writeLog("Type:HUANG_PCN:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + PCNDistance_ + ":Distance:" + distance + ":Attack:" + true + ":Correct:" + (isFake_==true)  + timeStanding[0] + ":" + timeStanding[1] + ":" + beaconsWithZeroSpeed);
					}
			
					if(isFake_) truePositiv[0]++;
					else falsePositiv[0]++;
					
					return rule_;
				}
				else{
					if(loggingType_ > 0){
						LocationInformationLogWriter.log("Type:HUANG_PCN:Source:" + "MDS:RESTNET:"+ x_[0] + ":" + y_[0] + ":Attack:" + false + ":Correct:" + (isFake_==false));
						writeLog("Type:HUANG_PCN:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + PCNDistance_ + ":Distance:" + distance + ":Attack:" + false + ":Correct:" + (isFake_==false)  + timeStanding[0] + ":" + timeStanding[1]  + ":" + beaconsWithZeroSpeed);
					}
					
					if(!isFake_) trueNegativ[0]++;
					else  falseNegativ[0]++;
					
					Street[] routeStreets = vehicle_.getRouteStreets();
					boolean[] routeDirections = vehicle_.getRouteDirections();
					int i = vehicle_.getRoutePosition() + 1;	// increased by 1 because a penalty on the street on which the vehicle currently is isn't very helpful
					boolean found = false;
					for(;i < routeStreets.length; ++i){
						if(routeStreets[i] == street_){
							if(routeDirections[i]){		// from startNode to endNode
								if(directionAsNumber_ < 1){
									found = true;
									break;
								}
							} else {		// from endNode to startNode
								if(directionAsNumber_ > -1){
									found = true;
									break;
								}
							}
						}				
					}
					if(found){
						vehicle_.calculateRoute(true, true);
					}
					
					
					return "";
					
				}
			}
			else{
				

				if((PCNDistance_ < distance)){


					if(loggingType_ > 0){
						LocationInformationLogWriter.log("Type:HUANG_PCN:Source:" + "MDS:RUJ:"+ x_[0] + ":" + y_[0] + ":Attack:" + true + ":Correct:" + (isFake_==true));
						writeLog("Type:HUANG_PCN:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + PCNDistance_ + ":Distance:" + distance + ":Attack:" + true + ":Correct:" + (isFake_==true));
					}
			
					if(isFake_) truePositiv[0]++;
					else falsePositiv[0]++;
					
					return rule_;
				}
				else{
					if(loggingType_ > 0){
						LocationInformationLogWriter.log("Type:HUANG_PCN:Source:" + "MDS:RUJ:"+ x_[0] + ":" + y_[0] + ":Attack:" + false + ":Correct:" + (isFake_==false));
						writeLog("Type:HUANG_PCN:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + PCNDistance_ + ":Distance:" + distance + ":Attack:" + false + ":Correct:" + (isFake_==false));
					}
					
					if(!isFake_) trueNegativ[0]++;
					else  falseNegativ[0]++;
					
					
					Street[] routeStreets = vehicle_.getRouteStreets();
					boolean[] routeDirections = vehicle_.getRouteDirections();
					int i = vehicle_.getRoutePosition() + 1;	// increased by 1 because a penalty on the street on which the vehicle currently is isn't very helpful
					boolean found = false;
					for(;i < routeStreets.length; ++i){
						if(routeStreets[i] == street_){
							if(routeDirections[i]){		// from startNode to endNode
								if(directionAsNumber_ < 1){
									found = true;
									break;
								}
							} else {		// from endNode to startNode
								if(directionAsNumber_ > -1){
									found = true;
									break;
								}
							}
						}				
					}
					if(found){
						vehicle_.calculateRoute(true, true);
					}

					return "";
					
				}
			}

		}
		else if(rule_.equals("PCN_FORWARD")){
			if(lastLoggedBeacon < 2) return "";
			
			double ratio = 0;
			if(speed_[0] != 0) ratio = (speed_[lastLoggedBeacon-1]/speed_[0]);
			
			if(ratio > PCNFORWARDThreshold_){
				if(loggingType_ > 0){
					writeLog("Type:PCN_FOWARD:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + PCNFORWARDThreshold_ + ":Distance:" + ratio + ":Attack:" + true + ":Correct:" + (isFake_==true));
				}
				
				if(isFake_) truePositiv[1]++;
				else falsePositiv[1]++;
				
				return rule_;
			}
			else{
				if(loggingType_ > 0)writeLog("Type:PCN_FORWARD:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + PCNFORWARDThreshold_ + ":Distance:" + ratio + ":Attack:" + false + ":Correct:" + (isFake_==false));
				
				if(!isFake_) trueNegativ[1]++;
				else falseNegativ[1]++;
				
				sourceVehicle_.calculateRoute(true, true);
			
			}
		}
		else if(rule_.equals("HUANG_RHCN")){	
			if(lastLoggedBeacon < 2) return "";

			int beaconsWithZeroSpeed = 0;
			for(int i = 0; i < AMOUNT_OF_BEACONS_LOGGED; i++) if(speed_[i] == 0) beaconsWithZeroSpeed++;
			int[] timeStanding = vehicle_.getKnownVehiclesList().hasBeenSeenWaitingFor(monitoredVehicleID_);
			
			
			double ratio = 1;
			
			
			if(advancedIDSRules_){
				double[] advancedSpeedData = vehicle_.getKnownVehiclesList().getSpecificSpeedDataSet(monitoredVehicleID_, 7);

				if(advancedSpeedData[1] != -1 && advancedSpeedData[0] != -1){
					if(advancedSpeedData[0] != 0) ratio = (advancedSpeedData[1]/advancedSpeedData[0]);
				}
				else{
					if(speed_[0] != 0) ratio = (speed_[lastLoggedBeacon-1]/speed_[0]);
				}
				if(speed_[0] == 0 || (speed_[0] > 400 && ratio > RHCNThreshold_)){
					if(loggingType_ > 0){
						LocationInformationLogWriter.log("Type:HUANG_RHCN:Source:" + "MDS:RESTNET:"+ x_[0] + ":" + y_[0] + ":Attack:" + true + ":Correct:" + (isFake_==true));
						writeLog("Type:HUANG_RHCN:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + RHCNThreshold_ + ":Distance:" + ratio + ":Attack:" + true + ":Correct:" + (isFake_==true)  + timeStanding[0] + ":" + timeStanding[1] + ":" + beaconsWithZeroSpeed);
					}
					
					if(isFake_) truePositiv[2]++;
					else falsePositiv[2]++;

					return rule_;
				}
				else{
					if(loggingType_ > 0){
						LocationInformationLogWriter.log("Type:HUANG_RHCN:Source:" + "MDS:RESTNET:"+ x_[0] + ":" + y_[0] + ":Attack:" + false + ":Correct:" + (isFake_==false));
						writeLog("Type:HUANG_RHCN:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + RHCNThreshold_ + ":Distance:" + ratio + ":Attack:" + false + ":Correct:" + (isFake_==false)  + timeStanding[0] + ":" + timeStanding[1] + ":" + beaconsWithZeroSpeed);
					}
					
					if(!isFake_) trueNegativ[2]++;
					else falseNegativ[2]++;
					
				}
			}
			else if(classicMode_){
				dx = x_[0] -  x_[(lastLoggedBeacon-1)];
				dy = y_[0] -  y_[(lastLoggedBeacon-1)];
				
				
				distance = Math.sqrt(dx * dx + dy * dy);
				if(distance > RHCNThreshold_){
					if(loggingType_ > 0){
						LocationInformationLogWriter.log("Type:HUANG_RHCN:Source:" + "MDS:RUJ:"+ x_[0] + ":" + y_[0] + ":Attack:" + true + ":Correct:" + (isFake_==true));
						writeLog("Type:HUANG_RHCN:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + RHCNThreshold_ + ":Distance:" + distance + ":Attack:" + true + ":Correct:" + (isFake_==true)  + timeStanding[0] + ":" + timeStanding[1] + ":" + beaconsWithZeroSpeed);
					}
					
					if(isFake_) truePositiv[2]++;
					else falsePositiv[2]++;

					return rule_;
				}
				else{
					if(loggingType_ > 0){
						LocationInformationLogWriter.log("Type:HUANG_RHCN:Source:" + "MDS:RUJ:"+ x_[0] + ":" + y_[0] + ":Attack:" + false + ":Correct:" + (isFake_==false));
						writeLog("Type:HUANG_RHCN:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + RHCNThreshold_ + ":Distance:" + distance + ":Attack:" + false + ":Correct:" + (isFake_==false)  + timeStanding[0] + ":" + timeStanding[1] + ":" + beaconsWithZeroSpeed);
					}
					
					if(!isFake_) trueNegativ[2]++;
					else falseNegativ[2]++;

				}
			}
			else{
				//calculate how many braking beacons and how much the speed was reduced
				double lastSavedSpeed = speed_[0];
				int amountOfBrakesSave = 0;
				int amountOfBrakes = 0;
				
				for(int i = 1; i < AMOUNT_OF_BEACONS_LOGGED; i++ ){
					if(speed_[i] == -1)break;
					else{
						if((lastSavedSpeed - 50) > speed_[i]) amountOfBrakes++;
						else{
							if(amountOfBrakes > amountOfBrakesSave) amountOfBrakesSave = amountOfBrakes;
							amountOfBrakes = 0;
						}
						lastSavedSpeed = speed_[i];
					}
				}
				if(amountOfBrakes > amountOfBrakesSave) amountOfBrakesSave = amountOfBrakes;
				
				
				
				if(speed_[0] != 0) ratio = (speed_[lastLoggedBeacon-1]/speed_[0]);
				if(speed_[0] == 0 || (speed_[0] > 400 && ratio > RHCNThreshold_)){
					if(loggingType_ > 0)writeLog("Type:HUANG_RHCN:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + RHCNThreshold_ + ":Distance:" + ratio + ":braking:" + amountOfBrakesSave + ":lastLoggedBeacon:" + lastLoggedBeacon + ":startSpeed:" + speed_[0] + ":lastSpeed:" + speed_[lastLoggedBeacon-1] + ":Attack:" + true + ":Correct:" + (isFake_==true)  + timeStanding[0] + ":" + timeStanding[1] + ":" + beaconsWithZeroSpeed);
					
					if(isFake_) truePositiv[2]++;
					else falsePositiv[2]++;

					return rule_;
				}
				else{
					if(loggingType_ > 0)writeLog("Type:HUANG_RHCN:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + RHCNThreshold_ + ":Distance:" + ratio + ":braking:" + amountOfBrakesSave + ":lastLoggedBeacon:" + lastLoggedBeacon + ":startSpeed:" + speed_[0] +  ":lastSpeed:" + speed_[lastLoggedBeacon-1] + ":Attack:" + false + ":Correct:" + (isFake_==false)  + timeStanding[0] + ":" + timeStanding[1] + ":" + beaconsWithZeroSpeed);
					
					if(!isFake_) trueNegativ[2]++;
					else falseNegativ[2]++;

				}
			}
			

		}
		else if(rule_.equals("HUANG_EVA_FORWARD")){		
			if(classicMode_){

				dx = x_[0] -  x_[(lastLoggedBeacon-1)];
				dy = y_[0] -  y_[(lastLoggedBeacon-1)];
				
				
				distance = Math.sqrt(dx * dx + dy * dy);
				if(distance > EVAFORWARDThreshold_){	
					if(loggingType_ > 0){
						LocationInformationLogWriter.log("Type:HUANG_EVA_FORWARD:Source:" + "MDS:RUJ:"+ x_[0] + ":" + y_[0] + ":Attack:" + true + ":Correct:" + (isFake_==true));
						writeLog("Type:HUANG_EVA_FORWARD:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + EVAFORWARDThreshold_ + ":Distance:" + distance + ":Attack:" + true + ":Correct:" + (isFake_==true));
					}
					if(isFake_) truePositiv[3]++;
					else falsePositiv[3]++;
				}
				else{
					if(!sourceVehicle_.isDrivingOnTheSide_() && sourceVehicle_.getKnownPenalties().hasToMoveOutOfTheWay(penaltySourceVehicle_))sourceVehicle_.setMoveOutOfTheWay_(true);
					if(!isFake_) trueNegativ[3]++;
					else falseNegativ[3]++;
					if(loggingType_ > 0){
						LocationInformationLogWriter.log("Type:HUANG_EVA_FORWARD:Source:" + "MDS:RUJ:"+ x_[0] + ":" + y_[0] + ":Attack:" + false + ":Correct:" + (isFake_==false));

						writeLog("Type:HUANG_EVA_FORWARD:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + EVAFORWARDThreshold_ + ":Distance:" + distance + ":Attack:" + false + ":Correct:" + (isFake_==false));
					}
				
					if(!sourceVehicle_.isDrivingOnTheSide_() && hasToMoveOutOfTheWay(penaltySourceVehicle_)){
						sourceVehicle_.setMoveOutOfTheWay_(true);
						sourceVehicle_.setWaitingForVehicle_(penaltySourceVehicle_);
					}
					
				}
			}
			else{

				double ratio = 1;
				
				if(speed_[0] != 0) ratio = (speed_[lastLoggedBeacon-1]/speed_[0]);

				if(ratio > EVAFORWARDThreshold_){	
					if(loggingType_ > 0)writeLog("Type:HUANG_EVA_FORWARD:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + EVAFORWARDThreshold_ + ":Distance:" + ratio + ":Attack:" + true + ":Correct:" + (isFake_==true));
					if(isFake_) truePositiv[3]++;
					else falsePositiv[3]++;
				}
				else{
					if(!sourceVehicle_.isDrivingOnTheSide_() && sourceVehicle_.getKnownPenalties().hasToMoveOutOfTheWay(penaltySourceVehicle_))sourceVehicle_.setMoveOutOfTheWay_(true);
					if(!isFake_) trueNegativ[3]++;
					else falseNegativ[3]++;
					if(loggingType_ > 0)writeLog("Type:HUANG_EVA_FORWARD:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + EVAFORWARDThreshold_ + ":Distance:" + ratio + ":Attack:" + false + ":Correct:" + (isFake_==false));
				
					if(!sourceVehicle_.isDrivingOnTheSide_() && hasToMoveOutOfTheWay(penaltySourceVehicle_)){
						sourceVehicle_.setMoveOutOfTheWay_(true);
						sourceVehicle_.setWaitingForVehicle_(penaltySourceVehicle_);
					}
					
				}
			}

		}
	
		else if(rule_.equals("EVA_EMERGENCY_ID")){
			 //It is important that the known vehicles timeout is smaller than the message interval (otherwise old knownVehicles won't get cleaned up and will be used for the evaluation of the IDS)
			int beaconAmount = KnownVehicle.getAmountOfSavedBeacons_();
			
			if(beaconAmount > 0){
				double[] response = sourceVehicle_.getKnownVehiclesList().getBeaconInformationFromVehicles(monitoredVehicleID_);
			

				double thresholdTime = ((double)(beaconAmount * Vehicle.getBeaconInterval())/EVABeaconFactor_);
				double thresholdBeacon = ((double)beaconAmount/EVABeaconFactor_);
				
				//we have no neighbors and therefore, have to rely on our own data
				if(response[2] == 0){
					response = vehicle_.getKnownVehiclesList().checkBeacons(monitoredVehicleID_);
				}
				if(response != null){

					if((thresholdBeacon > response[1]) && thresholdTime > response[0]){

						if(isFake_) truePositiv[4]++;
						else if(!isFake_) falsePositiv[4]++;
						try{
							if(loggingType_ > 0){
								LocationInformationLogWriter.log("Type:EVA_EMERGENCY_ID:Source:" + "MDS:RESTNET:"+ sourceX_ + ":" + sourceY_ + ":Attack:" + true + ":Correct:" + (isFake_==true));
								writeLog("Type:EVA_EMERGENCY_ID:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Beacon-Threshold:" + thresholdBeacon + ":Beaconvalue:" +  response[1] + ":Time-Threshold:" + thresholdTime + ":Timevalue:" +  response[0]  + "Neighbours:" + response[2] + ":Attack:" + true + ":Correct:" + (isFake_==true));
							}
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					else{
						if(loggingType_ > 0){
							LocationInformationLogWriter.log("Type:EVA_EMERGENCY_ID:Source:" + "MDS:RESTNET:"+ sourceX_ + ":" + sourceY_ + ":Attack:" + false + ":Correct:" + (isFake_==false));
							writeLog("Type:EVA_EMERGENCY_ID:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Beacon-Threshold:" + thresholdBeacon + ":Beaconvalue:" +  response[1] + ":Time-Threshold:" + thresholdTime + ":Timevalue:" +  response[0]  + "Neighbours:" + response[2] + ":Attack:" + false + ":Correct:" + (isFake_==false));
						}
						if(!isFake_) trueNegativ[4]++;
						else if(isFake_) falseNegativ[4]++;
						
						
						if(!sourceVehicle_.isDrivingOnTheSide_() && sourceVehicle_.getKnownPenalties().hasToMoveOutOfTheWay(penaltySourceVehicle_)){
							sourceVehicle_.setMoveOutOfTheWay_(true);
							sourceVehicle_.setWaitingForVehicle_(penaltySourceVehicle_);
						}

							
						sourceVehicle_.setForwardMessage_(true);
					}
				}
				
			}
			else{
				System.out.println("not sufficient Beacons ...");
			}
		}
		
		else if(rule_.equals("HUANG_EEBL")){
			if(lastLoggedBeacon < 2) return "";
			double ratio = 0;
			if(speed_[0] != 0) ratio = (speed_[lastLoggedBeacon-1]/speed_[0]);
			
			
			if(advancedIDSRules_){
				
				double[] advancedSpeedData = vehicle_.getKnownVehiclesList().getSpecificSpeedDataSet(monitoredVehicleID_, 12);
				
				boolean useRule = true;
				if(advancedSpeedData[0] == -1) useRule = false;
				
				
				
				
				if(useRule && advancedSpeedData[0] == 0 || ratio > EEBLThreshold_){
					if(loggingType_ > 0){
						LocationInformationLogWriter.log("Type:HUANG_EEBL:Source:" + "MDS:RESTNET:"+ x_[0] + ":" + y_[0] + ":Attack:" + true + ":Correct:" + (isFake_==true));
						writeLog("Type:HUANG_EEBL:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + EEBLThreshold_ + ":Distance:" + ratio + ":Attack:" + true + ":Correct:" + (isFake_==true));
					}
					if(isFake_) truePositiv[5]++;
					else if(!isFake_) falsePositiv[5]++;

					return rule_;
				
				}
				else {
					if(loggingType_ > 0){
						LocationInformationLogWriter.log("Type:HUANG_EEBL:Source:" + "MDS:RESTNET:"+ x_[0] + ":" + y_[0] + ":Attack:" + false + ":Correct:" + (isFake_==false));
						writeLog("Type:HUANG_EEBL:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + EEBLThreshold_ + ":Distance:" + ratio + ":Attack:" + false + ":Correct:" + (isFake_==false));	
					}
					
					if(!isFake_) trueNegativ[5]++;
					else if(isFake_) falseNegativ[5]++;
					
				}

			}
			else if(classicMode_){
				dx = x_[0] -  x_[(lastLoggedBeacon-1)];
				dy = y_[0] -  y_[(lastLoggedBeacon-1)];
				
				
				distance = Math.sqrt(dx * dx + dy * dy);
				if(distance > EEBLThreshold_){
					if(loggingType_ > 0){
						LocationInformationLogWriter.log("Type:HUANG_EEBL:Source:" + "MDS:RUJ:"+ x_[0] + ":" + y_[0] + ":Attack:" + true + ":Correct:" + (isFake_==true));
						writeLog("Type:HUANG_EEBL:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + EEBLThreshold_ + ":Distance:" + distance + ":Attack:" + true + ":Correct:" + (isFake_==true));
					}
					if(isFake_) truePositiv[5]++;
					else if(!isFake_) falsePositiv[5]++;

					return rule_;
				
				}
				else {
					if(loggingType_ > 0){
						LocationInformationLogWriter.log("Type:HUANG_EEBL:Source:" + "MDS:RUJ:"+ x_[0] + ":" + y_[0] + ":Attack:" + false + ":Correct:" + (isFake_==false));
						writeLog("Type:HUANG_EEBL:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + EEBLThreshold_ + ":Distance:" + distance + ":Attack:" + false + ":Correct:" + (isFake_==false));	
					}
					
					if(!isFake_) trueNegativ[5]++;
					else if(isFake_) falseNegativ[5]++;
				}
			}
			else{
				if(ratio > EEBLThreshold_){
					if(loggingType_ > 0)writeLog("Type:HUANG_EEBL:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + EEBLThreshold_ + ":Distance:" + ratio + ":Attack:" + true + ":Correct:" + (isFake_==true));
					if(isFake_) truePositiv[5]++;
					else if(!isFake_) falsePositiv[5]++;

					return rule_;
				
				}
				else {
					if(loggingType_ > 0)writeLog("Type:HUANG_EEBL:Source:" + sourceVehicle_.getID() +  ":Monitored:" + monitoredVehicleID_ + ":Threshold:" + EEBLThreshold_ + ":Distance:" + ratio + ":Attack:" + false + ":Correct:" + (isFake_==false));	
					
					if(!isFake_) trueNegativ[5]++;
					else if(isFake_) falseNegativ[5]++;
				}

			}
		}	
			return "";

	}
	/**
	 * writes logs and writes to console in debug mode
	 */
	
	/**
	 * checks if a rule is active
	 */
	public static boolean ruleIsActive(String rule){
		boolean returnValue = false;
		for(int i = 0; i < activeRules_.length;i++) if(activeRules_[i].equals(rule)) returnValue = true;
		return returnValue;
	}

	/**
	 * reports stats
	 */
	public static String getReport(){
		return "\nfake: "  + fake + "\nnofake: " + noFake + 	"\n:TP:TN:FP:FN:\n" + "PCN:	:" + truePositiv[0] + ":" + trueNegativ[0] + ":" + falsePositiv[0] + ":" + falseNegativ[0] + "\n" + "PCN_FOWARD:	:" + truePositiv[1] + ":" + trueNegativ[1] + ":" + falsePositiv[1] + ":" + falseNegativ[1] + "\n" +"RHCN:	" + truePositiv[2] + ":" + trueNegativ[2] + ":" + falsePositiv[2] + ":" + falseNegativ[2] + "\n"+"EVA FORWARD	:" + truePositiv[3] + ":" + trueNegativ[3] + ":" + falsePositiv[3] + ":" + falseNegativ[3] + "\n"+"EVA	:" + truePositiv[4] + ":" + trueNegativ[4] + ":" + falsePositiv[4] + ":" + falseNegativ[4] + "\n"+"EEBL	:" + truePositiv[5] + ":" + trueNegativ[5] + ":" + falsePositiv[5] + ":" + falseNegativ[5] + "\n";
	} 
	
	
	public void writeLog(String s){
		try{
			if(logIDS_)IDSLogWriter.log(s);
		}
		catch(Exception e){
			
		}
	}
	
	/**
	 * tests if a vehicle has to move out of the way because of a emergency vehicle approaching. (tests the next 4 streets)
	 */
	
	public boolean hasToMoveOutOfTheWay(Vehicle emergencyVehicle){
		// search if this new information affects the route of the vehicle as route calculation is quite costly
		Street[] routeStreets = emergencyVehicle.getRouteStreets();
		boolean[] routeDirections = emergencyVehicle.getRouteDirections();
		int i = emergencyVehicle.getRoutePosition();
		
		Street curStreet = sourceVehicle_.getCurStreet();
		boolean curDirection = sourceVehicle_.getCurDirection();
		
		for(;i < emergencyVehicle.getRoutePosition() + 5; ++i){
			if(routeStreets.length == i) break;
			if(routeStreets[i] == curStreet && routeDirections[i] == curDirection) return true;				
		}
		
		return false;
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
	public IDSProcessor getNext() {
		return next_;
	}

	/**
	 * Returns the KnownVehicle before this one.
	 * 
	 * @return the previous
	 */
	public IDSProcessor getPrevious() {
		return previous_;
	}

	/**
	 * Sets the KnownVehicle after this one.
	 * 
	 * @param next	the object which comes after this one
	 */
	public void setNext(IDSProcessor next) {
		next_ = next;
	}

	/**
	 * Sets the KnownVehicle before this one.
	 * 
	 * @param previous	the object which comes before this one
	 */
	public void setPrevious(IDSProcessor previous) {
		previous_ = previous;
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
	 * @return the iD_
	 */
	public long getID_() {
		return ID_;
	}

	/**
	 * @param iD_ the iD_ to set
	 */
	public void setID_(long iD_) {
		ID_ = iD_;
	}

	/**
	 * @return the idsData_
	 */
	public static String[] getIdsData_() {
		return idsData_;
	}

	/**
	 * @param idsData_ the idsData_ to set
	 */
	public static void setIdsData_(String[] idsData_) {
		IDSProcessor.idsData_ = idsData_;
	}

	/**
	 * @return the logIDS_
	 */
	public static boolean isLogIDS_() {
		return logIDS_;
	}

	/**
	 * @param logIDS_ the logIDS_ to set
	 */
	public static void setLogIDS_(boolean logIDS_) {
		IDSProcessor.logIDS_ = logIDS_;
	}

	/**
	 * @return the pCNDistance_
	 */
	public static int getPCNDistance_() {
		return PCNDistance_;
	}

	/**
	 * @param pCNDistance_ the pCNDistance_ to set
	 */
	public static void setPCNDistance_(int pCNDistance_) {
		PCNDistance_ = pCNDistance_;
	}

	/**
	 * @return the pCNFORWARDThreshold_
	 */
	public static double getPCNFORWARDThreshold_() {
		return PCNFORWARDThreshold_;
	}

	/**
	 * @param pCNFORWARDThreshold_ the pCNFORWARDThreshold_ to set
	 */
	public static void setPCNFORWARDThreshold_(double pCNFORWARDThreshold_) {
		PCNFORWARDThreshold_ = pCNFORWARDThreshold_;
	}

	/**
	 * @return the rHCNThreshold_
	 */
	public static double getRHCNThreshold_() {
		return RHCNThreshold_;
	}

	/**
	 * @param rHCNThreshold_ the rHCNThreshold_ to set
	 */
	public static void setRHCNThreshold_(double rHCNThreshold_) {
		RHCNThreshold_ = rHCNThreshold_;
	}

	/**
	 * @return the eEBLThreshold_
	 */
	public static double getEEBLThreshold_() {
		return EEBLThreshold_;
	}

	/**
	 * @param eEBLThreshold_ the eEBLThreshold_ to set
	 */
	public static void setEEBLThreshold_(double eEBLThreshold_) {
		EEBLThreshold_ = eEBLThreshold_;
	}

	/**
	 * @return the activeRules_
	 */
	public static String[] getActiveRules_() {
		return activeRules_;
	}

	/**
	 * @param activeRules_ the activeRules_ to set
	 */
	public static void setActiveRules_(String[] activeRules_) {
		IDSProcessor.activeRules_ = activeRules_;
	}

	/**
	 * @return the monitoredVehicleID_
	 */
	public long getMonitoredVehicleID_() {
		return monitoredVehicleID_;
	}

	/**
	 * @param monitoredVehicleID_ the monitoredVehicleID_ to set
	 */
	public void setMonitoredVehicleID_(long monitoredVehicleID_) {
		this.monitoredVehicleID_ = monitoredVehicleID_;
	}

	/**
	 * @return the deleteProcessor_
	 */
	public boolean isDeleteProcessor_() {
		return deleteProcessor_;
	}

	public static double getEVAFORWARDThreshold_() {
		return EVAFORWARDThreshold_;
	}

	public static void setEVAFORWARDThreshold_(double eVAFORWARDThreshold_) {
		EVAFORWARDThreshold_ = eVAFORWARDThreshold_;
	}

	/**
	 * @return the ready_
	 */
	public boolean isReady_() {
		return ready_;
	}

	public void setReady_(boolean ready_) {
		this.ready_ = ready_;
	}

	public int isInstantIDS_() {
		return instantIDS_;
	}

	public void setInstantIDS_(int instantIDS_) {
		this.instantIDS_ = instantIDS_;
	}



	public static double getEVABeaconTimeFactor_() {
		return EVABeaconTimeFactor_;
	}

	public static void setEVABeaconTimeFactor_(double eVABeaconTimeFactor_) {
		EVABeaconTimeFactor_ = eVABeaconTimeFactor_;
	}

	public static double getEVABeaconFactor_() {
		return EVABeaconFactor_;
	}

	public static void setEVABeaconFactor_(double eVABeaconFactor_) {
		EVABeaconFactor_ = eVABeaconFactor_;
	}


	public static boolean isAdvancedIDSRules_() {
		return advancedIDSRules_;
	}

	public static void setAdvancedIDSRules_(boolean advancedIDSRules_) {
		IDSProcessor.advancedIDSRules_ = advancedIDSRules_;
	}



}