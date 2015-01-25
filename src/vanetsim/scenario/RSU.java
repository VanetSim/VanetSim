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

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ArrayDeque;

import vanetsim.gui.Renderer;
import vanetsim.gui.controlpanels.ReportingControlPanel;
import vanetsim.gui.helpers.GeneralLogWriter;
import vanetsim.map.Node;
import vanetsim.map.Region;
import vanetsim.map.Street;
import vanetsim.scenario.messages.Message;
import vanetsim.scenario.positionverification.PositioningHelper;
import vanetsim.scenario.positionverification.PropagationModel;
import vanetsim.map.Map;


/**
 * A Road-Side-Unit to send and receive WiFi signals.
 */

public final class RSU {
	
	/** A reference to the reporting control panel so that we don't need to call this over and over again. */
	private static final ReportingControlPanel REPORT_PANEL = Vehicle.getREPORT_PANEL();
	
	/** A common counter to generate unique IDs */
	private static int counter_ = 1;
	
	/** How long a Road-Side-Unit waits to communicate again (in milliseconds). Also used for cleaning up outdated known messages. */
	private static int communicationInterval_ = Vehicle.getCommunicationInterval();

	/** How long a Road-Side-Unit waits to send its beacons again. */
	private static int beaconInterval_ = Vehicle.getBeaconInterval();
	
	/** If communication is enabled */
	private static boolean communicationEnabled_ = Vehicle.getCommunicationEnabled();	

    /** is sending RSSI Values globally enabled */
    private static boolean sendRssiEnabled_ = false;
    
	/** If beacons are enabled */
	private static boolean beaconsEnabled_ = Vehicle.getBeaconsEnabled();
	
	/** A reference to the map so that we don't need to call this over and over again. */
	private static final Map MAP = Map.getInstance();
	
	/** An array holding all regions of the map. */
	private static Region[][] regions_;
	
	/** When known vehicles are rechecked for outdated entries. Measured in milliseconds. */
	private static final int KNOWN_VEHICLES_TIMEOUT_CHECKINTERVAL = 1000;
	
	/** A unique ID for this Road-Side-Unit */
	private final long rsuID_;
	
	/** The x coordinate. */
	private final int x_;
	
	/** The y coordinate. */
	private final int y_;	
	
	/** The wifi radius */
	private final int wifiRadius_;
	
	/** If the RSU is sending encrypted Messages */
	private final boolean isEncrypted_;
	
	/** The region in which this Road-Side-Unit is. */
	private Region region_;
	
	/** A countdown for sending beacons. */
	private int beaconCountdown_;
	
	/** A reference to the used Propagation Model **/
	private PropagationModel propagationModel_ = null;
	
	/** A countdown for communication. Also used for cleaning up outdated known messages. */
	private int communicationCountdown_;
	
	/** A countdown for rechecking if known vehicles are outdated. */
	private int knownVehiclesTimeoutCountdown_;
	
	/** If monitoring the beacon is enabled or not. */
	private static boolean beaconMonitorEnabled_ = false;
	
	/** The minimum x coordinate which is checked during beacon monitoring. */
	private static int beaconMonitorMinX_ = -1;
	
	/** The maximum x coordinate which is checked during beacon monitoring. */
	private static int beaconMonitorMaxX_ = -1;
	
	/** The minimum y coordinate which is checked during beacon monitoring. */
	private static int beaconMonitorMinY_ = -1;
	
	/** The maximum y coordinate which is checked during beacon monitoring. */
	private static int beaconMonitorMaxY_ = -1;

	/** A class storing messages of different states: execute, forward and old ones. Also used in the Vehicle class */
	private final KnownMessages knownMessages_ = new KnownMessages();

	/** A list of all vehicles currently known because of received beacons. */
	private final KnownVehiclesList knownVehiclesList_ = new KnownVehiclesList();
	
	/** activates demonstration mode of encrypted Mix-Zones */
	private static boolean showEncryptedBeaconsInMix_ = false;
	
	private HashMap<Long, ArrayList<PositionEntity>> positionVerificationEntryMap = new HashMap<Long, ArrayList<PositionEntity>>();
	
    private ArrayList<PositionEntity> receivedFromRSUVehiclelists = new ArrayList<PositionEntity>();
    private ArrayList<PositionEntity> receivedFromVehicleVehiclelists = new ArrayList<PositionEntity>();
    private ArrayList<PositionEntity> ownPositionEntryList =  new ArrayList<PositionEntity>();
    
	/** static array to save the colored vehicles that are behind the marked vehicle */
	private Vehicle[] vehicleBehind_;
	
	/** static array to save the colored vehicles that are front the marked vehicle */
	private Vehicle[] vehicleFront_;
	
	/** static array to save the colored vehicles that are toward the marked vehicle */
	private Vehicle[] vehicleToward_;
	
	/** saves all colored vehicles */
	public static ArrayDeque<Vehicle> coloredVehicles = new ArrayDeque<Vehicle>();
	
	public static RSU lastSender = null;
	
	/** flag to clear vehicle color */
	public static boolean colorCleared = false;
	
	/**
	 * Instantiates a new RSU.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param radius the signal radius
	 * @param isEncrypted if message is encrypted
	 */
	public RSU(int x, int y, int radius, boolean isEncrypted) {
		x_ = x;
		y_ = y;
		wifiRadius_ = radius;
		isEncrypted_ = isEncrypted;
		rsuID_ = counter_;
		++counter_;
		
		//set the countdowns so that not all fire at the same time!
		beaconCountdown_ = (int)Math.round(x_)%beaconInterval_;
		communicationCountdown_ = (int)Math.round(x_)%communicationInterval_;
		knownVehiclesTimeoutCountdown_ = (int)Math.round(x_)%KNOWN_VEHICLES_TIMEOUT_CHECKINTERVAL;
	}
	
	/**
	 * send messages to all vehicles in reach. Uses broadcast, because vehicles cannot send beacons
	 * to the RSUs(often to far away)
	 */
	public void sendMessages(){
		communicationCountdown_ += communicationInterval_;

		Message[] messages = knownMessages_.getForwardMessages();
		int messageSize = knownMessages_.getSize();
		
		int i, j, k, size, MapMinX, MapMinY, MapMaxX, MapMaxY, RegionMinX, RegionMinY, RegionMaxX, RegionMaxY;
		Vehicle[] vehicles = null;
		Vehicle vehicle = null;

		// Minimum x coordinate to be considered for sending beacons
		long tmp = x_ - wifiRadius_;
		if (tmp < 0) MapMinX = 0;	// Map stores only positive coordinates
		else if(tmp < Integer.MAX_VALUE) MapMinX = (int) tmp;
		else MapMinX = Integer.MAX_VALUE;

		// Maximum x coordinate to be considered for sending beacons
		tmp = x_ + (long)wifiRadius_;
		if (tmp < 0) MapMaxX = 0;
		else if(tmp < Integer.MAX_VALUE) MapMaxX = (int) tmp;
		else MapMaxX = Integer.MAX_VALUE;

		// Minimum y coordinate to be considered for sending beacons
		tmp = y_ - wifiRadius_;
		if (tmp < 0) MapMinY = 0;
		else if(tmp < Integer.MAX_VALUE) MapMinY = (int) tmp;
		else MapMinY = Integer.MAX_VALUE;

		// Maximum y coordinate to be considered for sending beacons
		tmp = y_ + (long)wifiRadius_;
		if (tmp < 0) MapMaxY = 0;
		else if(tmp < Integer.MAX_VALUE) MapMaxY = (int) tmp;
		else MapMaxY = Integer.MAX_VALUE;

		// Get the regions to be considered for sending beacons
		Region tmpregion = MAP.getRegionOfPoint(MapMinX, MapMinY);
		RegionMinX = tmpregion.getX();
		RegionMinY = tmpregion.getY();

		tmpregion = MAP.getRegionOfPoint(MapMaxX, MapMaxY);
		RegionMaxX = tmpregion.getX();
		RegionMaxY = tmpregion.getY();
		long maxCommDistanceSquared = (long)wifiRadius_ * wifiRadius_;
		long dx, dy;

		int sendCount = 0;

		// only iterate through those regions which are within the distance
		for(i = RegionMinX; i <= RegionMaxX; ++i){
			for(j = RegionMinY; j <= RegionMaxY; ++j){
				vehicles = regions_[i][j].getVehicleArray();	//use the array as it's MUCH faster!
				size = vehicles.length;
				for(k = 0; k < size; ++k){
					vehicle = vehicles[k];
					// precheck if the vehicle is near enough and valid (check is not exact as its a rectangular box and not circle)
					if(vehicle.isWiFiEnabled() && vehicle.isActive() && vehicle.getX() >= MapMinX && vehicle.getX() <= MapMaxX && vehicle.getY() >= MapMinY && vehicle.getY() <= MapMaxY){
						dx = vehicle.getX() - x_;
						dy = vehicle.getY() - y_;
						++sendCount;
						if((dx * dx + dy * dy) <= maxCommDistanceSquared){	// Pythagorean theorem: a^2 + b^2 = c^2 but without the needed Math.sqrt to save a little bit performance
							for(int l = messageSize - 1; l > -1; --l){		
								vehicle.receiveMessage(x_, y_, messages[l]);
							}
						}
					}
				}
			}
		}
		if(sendCount > 0) knownMessages_.deleteForwardMessage(i, true);
	}
	

	/**
	 * Find vehicles in neighborhood and send beacons to them. Please check the following conditions before calling this function:
	 * <ul>
	 * <li>communication is generally enabled</li>
	 * <li>beacons are generally enabled</li>
	 * <li>if the beacon countdown is 0 or less</li>
	 * </ul>
	 */
	public void sendBeacons(){
		beaconCountdown_ += beaconInterval_;

		int i, j, k, size, MapMinX, MapMinY, MapMaxX, MapMaxY, RegionMinX, RegionMinY, RegionMaxX, RegionMaxY;
		Vehicle[] vehicles = null;
		Vehicle vehicle = null;

		// Minimum x coordinate to be considered for sending beacons
		long tmp = x_ - wifiRadius_;
		if (tmp < 0) MapMinX = 0;	// Map stores only positive coordinates
		else if(tmp < Integer.MAX_VALUE) MapMinX = (int) tmp;
		else MapMinX = Integer.MAX_VALUE;

		// Maximum x coordinate to be considered for sending beacons
		tmp = x_ + (long)wifiRadius_;
		if (tmp < 0) MapMaxX = 0;
		else if(tmp < Integer.MAX_VALUE) MapMaxX = (int) tmp;
		else MapMaxX = Integer.MAX_VALUE;

		// Minimum y coordinate to be considered for sending beacons
		tmp = y_ - wifiRadius_;
		if (tmp < 0) MapMinY = 0;
		else if(tmp < Integer.MAX_VALUE) MapMinY = (int) tmp;
		else MapMinY = Integer.MAX_VALUE;

		// Maximum y coordinate to be considered for sending beacons
		tmp = y_ + (long)wifiRadius_;
		if (tmp < 0) MapMaxY = 0;
		else if(tmp < Integer.MAX_VALUE) MapMaxY = (int) tmp;
		else MapMaxY = Integer.MAX_VALUE;

		// Get the regions to be considered for sending beacons
		Region tmpregion = MAP.getRegionOfPoint(MapMinX, MapMinY);
		RegionMinX = tmpregion.getX();
		RegionMinY = tmpregion.getY();

		tmpregion = MAP.getRegionOfPoint(MapMaxX, MapMaxY);
		RegionMaxX = tmpregion.getX();
		RegionMaxY = tmpregion.getY();
		long maxCommDistanceSquared = (long)wifiRadius_ * wifiRadius_;
		long dx, dy;

	
		// only iterate through those regions which are within the distance
		for(i = RegionMinX; i <= RegionMaxX; ++i){
			for(j = RegionMinY; j <= RegionMaxY; ++j){
				vehicles = regions_[i][j].getVehicleArray();	//use the array as it's MUCH faster!
				size = vehicles.length;
				double currentRssi = Double.NaN;
				
				for(k = 0; k < size; ++k){
					vehicle = vehicles[k];
					// precheck if the vehicle is near enough and valid (check is not exact as its a rectangular box and not circle)
					if(vehicle.isWiFiEnabled() && vehicle.isActive() && vehicle.getX() >= MapMinX && vehicle.getX() <= MapMaxX && vehicle.getY() >= MapMinY && vehicle.getY() <= MapMaxY){
						dx = vehicle.getX() - x_;
						dy = vehicle.getY() - y_;
						if((dx * dx + dy * dy) <= maxCommDistanceSquared){	// Pythagorean theorem: a^2 + b^2 = c^2 but without the needed Math.sqrt to save a little bit performance
						    
						 // if sending RSSI Values is globaly enabled we need to calculate the RSSI here
                            if (sendRssiEnabled_) {
                                if (propagationModel_ == null) {
                                    propagationModel_ = PropagationModel.getInstance();
                                }
                                currentRssi = propagationModel_.calculateRSSI(PropagationModel.getGlobalDistanceToRSSPropagationModel(), dx, dy);
                            }
						    
							vehicle.getKnownRSUsList().updateRSU(this, rsuID_, x_, y_, isEncrypted_,currentRssi);
						}
					}
				}
			}
		}

		
		// allow beacon monitoring
		if(beaconMonitorEnabled_){
			if(x_ >= beaconMonitorMinX_ && x_ <= beaconMonitorMaxX_ && y_ >= beaconMonitorMinY_ && y_ <= beaconMonitorMaxY_){
				REPORT_PANEL.addBeacon(this, rsuID_, x_, y_, false);
			}
		}

	}
	
	/**
	 * Find vehicles in neighborhood and send beacons to them. Please check the following conditions before calling this function:
	 * <ul>
	 * <li>communication is generally enabled</li>
	 * <li>beacons are generally enabled</li>
	 * <li>if the beacon countdown is 0 or less</li>
	 * </ul>
	 */
	public void sendEncryptedBeacons(){
		beaconCountdown_ += beaconInterval_;
		
		if(lastSender != null && this.equals(lastSender)){
			for(Vehicle v : coloredVehicles) v.setColor(Color.black);			
			coloredVehicles.clear();
		}
		
		// variables to tmp save the different vehicles and information. 
		long[] maxCommDistanceSquaredFront = new long[6];
		long[] maxCommDistanceSquaredBehind = new long[6];
		long[] maxCommDistanceSquaredToward = new long[6];
		
		for(int l = 0; l < maxCommDistanceSquaredFront.length; l++){
			maxCommDistanceSquaredFront[l] = 1000000000;
			maxCommDistanceSquaredBehind[l] = 1000000000;
			maxCommDistanceSquaredToward[l] = 1000000000;
		}
		
		vehicleBehind_ = new Vehicle[6];
		vehicleFront_ = new Vehicle[6];
		vehicleToward_ = new Vehicle[6];
		
		long tmpCommDistanceSquared = 0; 

		long dx, dy;
		
		// these nodes are used to measure distances and to calculate which vehicles are behind and front
		Node nodeJunction;
		Node nodeFront;
		Node nodeBehind;
		Node tmpNode;
		
		long distanceSenderToNodeFront, senderDxFront, senderDyFront, distanceSenderToNodeBehind, senderDxBehind, senderDyBehind, distanceRecipientToNodeFront, recipientDxFront, recipientDyFront, distanceRecipientToNodeBehind, recipientDxBehind, recipientDyBehind, distanceRecipientToNodeJunction, nodeJunctionDx, nodeJunctionDy, tmpDx, tmpDy, dxMix = 0, dyMix = 0;
		
		//lists to compare every vehicle with every vehicle
		KnownVehicle[] senderHeads = knownVehiclesList_.getFirstKnownVehicle();
		Vehicle senderVehicle = null;
		KnownVehicle senderNext;

		
		KnownVehicle[] recipientHeads = knownVehiclesList_.getFirstKnownVehicle();
		Vehicle recipientVehicle = null;
		KnownVehicle recipientNext;


		long radiusSquared = 0;
		int mixRadius;
		
		HashMap<String, Vehicle> tmpVehicles = new HashMap<String, Vehicle>();

		//traverse every vehicle in the mix zone
		for(int j = 0; j < senderHeads.length; ++j){
			senderNext = senderHeads[j];								
			while(senderNext != null){
				senderVehicle = senderNext.getVehicle();
					//clear the data of the previous round
					tmpVehicles.clear();	
	
					
					for(int l = 0; l < maxCommDistanceSquaredFront.length; l++){
						maxCommDistanceSquaredFront[l] = 1000000000;
						maxCommDistanceSquaredBehind[l] = 1000000000;
						maxCommDistanceSquaredToward[l] = 1000000000;
						vehicleBehind_[l] = null;
						vehicleFront_[l] = null;
						vehicleToward_[l] = null;
					}

					//find out where is front and where is behind
					if(senderVehicle.curDirection_){
						nodeFront = senderVehicle.curStreet_.getEndNode();
						nodeBehind = senderVehicle.curStreet_.getStartNode();
					} else {
						nodeFront = senderVehicle.curStreet_.getStartNode();
						nodeBehind = senderVehicle.curStreet_.getEndNode();
					}
					

					nodeJunction = nodeFront;
					boolean junctionFound = false;
					Street[] tmpStreets = senderVehicle.getRouteStreets();

					//calculate mix zones radius and the distances
					if(senderVehicle.getCurMixNode_() != null){
						mixRadius = senderVehicle.getCurMixNode_().getMixZoneRadius();
						
						radiusSquared = mixRadius * mixRadius;
						
						dxMix = senderVehicle.getCurMixNode_().getX() - senderVehicle.getX();
						dyMix = senderVehicle.getCurMixNode_().getY() - senderVehicle.getY();
					}

					//get destinations, start at current position and get the next junction
					for(int i = senderVehicle.getRoutePosition();i < tmpStreets.length; i++){
						if(radiusSquared < (dxMix * dxMix + dyMix * dyMix)) break;
	
						if(nodeJunction.getCrossingStreetsCount() > 2){
							junctionFound = true;
							break;
						}
						if(senderVehicle.curDirection_){
							nodeJunction = tmpStreets[i].getEndNode();
						} else {
							nodeJunction = tmpStreets[i].getStartNode();
						}
					}
					
					if(!junctionFound){
						nodeJunction = null;
					}

					//calculate the distances of the sender
					senderDxFront = senderVehicle.getX() - nodeFront.getX();
					senderDyFront = senderVehicle.getY() - nodeFront.getY();
					
					distanceSenderToNodeFront = senderDxFront * senderDxFront + senderDyFront * senderDyFront;
					
					senderDxBehind = senderVehicle.getX() - nodeBehind.getX();
					senderDyBehind = senderVehicle.getY() - nodeBehind.getY();
					
					distanceSenderToNodeBehind = senderDxBehind * senderDxBehind + senderDyBehind * senderDyBehind;
					
					
					//get second vehicle list and compare to first
					for(int i = 0; i < recipientHeads.length; ++i){

						recipientNext = recipientHeads[i];								
						while(recipientNext != null){

							recipientVehicle = recipientNext.getVehicle();

							//check if the sender is the recipient
							if(!recipientVehicle.equals(senderVehicle)) {

								//calculate distances
								recipientDxFront = recipientVehicle.getX() - nodeFront.getX();
								recipientDyFront = recipientVehicle.getY() - nodeFront.getY();
								
								distanceRecipientToNodeFront = recipientDxFront * recipientDxFront + recipientDyFront * recipientDyFront;

								dx = senderVehicle.getX() - recipientVehicle.getX();
								dy = senderVehicle.getY() - recipientVehicle.getY();
								
								tmpCommDistanceSquared = dx * dx + dy * dy;
								
								
								recipientDxBehind = recipientVehicle.getX() - nodeBehind.getX();
								recipientDyBehind = recipientVehicle.getY() - nodeBehind.getY();
								
								distanceRecipientToNodeBehind = recipientDxBehind * recipientDxBehind + recipientDyBehind * recipientDyBehind;

								// get vehicles behind, in front and the ones that move toward
								
								
								//get vehicles behind
								//check if vehicles are on the same street (name) 
								if(senderVehicle.curStreet_.getName().equals(recipientVehicle.curStreet_.getName())){
									//check if vehicles have the same direction (yes: front, behind; no: toward)
									if(senderVehicle.curDirection_ == recipientVehicle.curDirection_){
										//check if the distance between sender and recipient is smaller that the distance between recipient and front node (vehicle behind)
										if(distanceSenderToNodeFront < distanceRecipientToNodeFront &&
												tmpCommDistanceSquared < distanceRecipientToNodeFront){
											//check if this recipient is nearer than the saved one (one vehicle for every lane is searched)
											if(tmpCommDistanceSquared <= maxCommDistanceSquaredBehind[recipientVehicle.curLane_]){	
												maxCommDistanceSquaredBehind[recipientVehicle.curLane_] = tmpCommDistanceSquared;
												vehicleBehind_[recipientVehicle.curLane_] = recipientVehicle;
											}
										}
										//check if the distance between the vehicles is smaller than the distance between recipient and node behind (vehicle front)
										else if(distanceSenderToNodeBehind < distanceRecipientToNodeBehind &&
										tmpCommDistanceSquared < distanceRecipientToNodeBehind){
											//check if this recipient is nearer than the saved one (one vehicle for every lane is searched)
											if(tmpCommDistanceSquared <= maxCommDistanceSquaredFront[recipientVehicle.curLane_]){	
												maxCommDistanceSquaredFront[recipientVehicle.curLane_] = tmpCommDistanceSquared;
												vehicleFront_[recipientVehicle.curLane_] = recipientVehicle;
											}
										}
									}
									else
									{
										//3. check if the distance between the vehicles is smaller than the distance between recipient and node behind (vehicle toward)
										if(distanceSenderToNodeBehind < distanceRecipientToNodeBehind &&
										tmpCommDistanceSquared < distanceRecipientToNodeBehind){
											//check if this recipient is nearer than the saved one (one vehicle for every lane is searched)
											if(tmpCommDistanceSquared <= maxCommDistanceSquaredToward[recipientVehicle.curLane_]){	
												maxCommDistanceSquaredToward[recipientVehicle.curLane_] = tmpCommDistanceSquared;
												vehicleToward_[recipientVehicle.curLane_] = recipientVehicle;
											}
										}
									}
								}
								
								//a junction in front was found. Check which vehicles need to be notified
								if(junctionFound){
									//calculate distances
									nodeJunctionDx = nodeJunction.getX() - recipientVehicle.getX();
									nodeJunctionDy = nodeJunction.getY() - recipientVehicle.getY();
									
									distanceRecipientToNodeJunction = nodeJunctionDx * nodeJunctionDx + nodeJunctionDy * nodeJunctionDy;

									//check if the recipient will cross the next junction of the sender
									tmpStreets= recipientVehicle.getRouteStreets();
									tmpNode = null;
									Boolean willPassJunction = false;
									for(int p = recipientVehicle.getRoutePosition(); p < tmpStreets.length; p++){
										if(recipientVehicle.curDirection_){
											tmpNode = tmpStreets[p].getEndNode();
										} else {
											tmpNode = tmpStreets[p].getStartNode();
										}
										if(tmpNode.equals(nodeJunction)) willPassJunction = true;
									}
									
									//check if the recipient is already on a crossing street
									boolean isCrossingStreet = false;
									for(Street s:nodeJunction.getCrossingStreets()){
										if(s.getName().equals(recipientVehicle.getCurStreet().getName())) isCrossingStreet = true;
									}
									
									if(isCrossingStreet){
										//1. check if vehicles are on the same street
										//2. check if the distance between sender and node behind is smaller that the distance between recipient and node behind
										//3. check if the distance between the vehicles is smaller than the distance between recipient and node behind
										if(willPassJunction && !(senderVehicle.getCurStreet().getName() + senderVehicle.curDirection_).equals(recipientVehicle.getCurStreet().getName() + recipientVehicle.curDirection_) && 
												(distanceSenderToNodeBehind < distanceRecipientToNodeBehind && tmpCommDistanceSquared < distanceRecipientToNodeBehind)){
											if(tmpVehicles.containsKey(recipientVehicle.getCurStreet().getName() + recipientVehicle.curLane_ + recipientVehicle.curDirection_)){
												tmpDx = nodeJunction.getX() - tmpVehicles.get(recipientVehicle.getCurStreet().getName() + recipientVehicle.curLane_ + recipientVehicle.curDirection_).getX();
												tmpDy = nodeJunction.getY() - tmpVehicles.get(recipientVehicle.getCurStreet().getName() + recipientVehicle.curLane_ + recipientVehicle.curDirection_).getY();
												if((tmpDx * tmpDx + tmpDy * tmpDy) > distanceRecipientToNodeJunction){	
													tmpVehicles.put(recipientVehicle.getCurStreet().getName() + recipientVehicle.curLane_ + recipientVehicle.curDirection_, recipientVehicle);
												}
											}
											else{
												tmpVehicles.put(recipientVehicle.getCurStreet().getName() + recipientVehicle.curLane_ + recipientVehicle.curDirection_, recipientVehicle);
											}
										}
									}
								}
							}

						recipientNext = recipientNext.getNext();
						}	
					}
					//clear colored vehicles
					
					//send beacons and color vehicles
					for(int k = 0; k < vehicleBehind_.length; k++){
						if(vehicleBehind_[k] != null){
                        vehicleBehind_[k].getKnownVehiclesList().updateVehicle(senderVehicle, senderVehicle.getID(), senderVehicle.getX(),
                                senderVehicle.getY(), senderVehicle.getCurSpeed(), rsuID_, true, false,Double.NaN);
							if(senderVehicle.equals(Renderer.getInstance().getMarkedVehicle()) && showEncryptedBeaconsInMix_) {
								coloredVehicles.add(vehicleBehind_[k]);
								vehicleBehind_[k].setColor(Color.red);
								lastSender = this;
							}
						}
						if(vehicleFront_[k] != null){
                        vehicleFront_[k].getKnownVehiclesList().updateVehicle(senderVehicle, senderVehicle.getID(), senderVehicle.getX(),
                                senderVehicle.getY(), senderVehicle.getCurSpeed(), rsuID_, true, false,Double.NaN);
							if(senderVehicle.equals(Renderer.getInstance().getMarkedVehicle()) && showEncryptedBeaconsInMix_){
								coloredVehicles.add(vehicleFront_[k]);
								vehicleFront_[k].setColor(Color.red);
								lastSender = this;
							}
						}
						if(vehicleToward_[k] != null){
                        vehicleToward_[k].getKnownVehiclesList().updateVehicle(senderVehicle, senderVehicle.getID(), senderVehicle.getX(),
                                senderVehicle.getY(), senderVehicle.getCurSpeed(), rsuID_, true, false,Double.NaN);
							if(senderVehicle.equals(Renderer.getInstance().getMarkedVehicle()) && showEncryptedBeaconsInMix_){
								coloredVehicles.add(vehicleToward_[k]);
								vehicleToward_[k].setColor(Color.red);
								lastSender = this;
							}
						}
					}	
					for(Vehicle v : tmpVehicles.values()) {
                    v.getKnownVehiclesList().updateVehicle(senderVehicle, senderVehicle.getID(), senderVehicle.getX(), senderVehicle.getY(),
                            senderVehicle.getCurSpeed(), rsuID_, true, false, Double.NaN);
						if(senderVehicle.equals(Renderer.getInstance().getMarkedVehicle()) && showEncryptedBeaconsInMix_){
							coloredVehicles.add(v);
							v.setColor(Color.red);
							lastSender = this;
						}
					}
				senderNext = senderNext.getNext();
			}		
		}
	}	

	
	/**
	 * Receive a message from a vehicle.
	 * 
	 * @param sourceX	the x coordinate of the other vehicle
	 * @param sourceY	the y coordinate of the other vehicle
	 * @param message	the message
	 */
	public final void receiveMessage(int sourceX, int sourceY, Message message){
		//set broadcast mode, otherwise all vehicles would forward the broadcasted message (performance)
		message.setFloodingMode(true);
		
		//only redirect all messages
		knownMessages_.addMessage(message, false, true);
	}
	
	
	/**
	 * Cleanup all outdated messages
	 * 
	 * @param timePerStep	the actual time per step
	 */
	public void cleanup(int timePerStep){
		//Has to be a special step because in all other steps other communication is done. This could create
		//some synchronization problems!	
		if(communicationEnabled_){
			if(knownMessages_.hasNewMessages()) knownMessages_.processMessages();
			communicationCountdown_ -= timePerStep;
			if(communicationCountdown_ < 1) knownMessages_.checkOutdatedMessages(true);
				
			if(beaconsEnabled_) beaconCountdown_ -= timePerStep;	
		}
		if(beaconsEnabled_){
			beaconCountdown_ -= timePerStep;

			// recheck known vehicles for outdated entries.
			if(knownVehiclesTimeoutCountdown_ < 1){
				knownVehiclesList_.checkOutdatedVehicles();
				knownVehiclesTimeoutCountdown_ += KNOWN_VEHICLES_TIMEOUT_CHECKINTERVAL;
			} else knownVehiclesTimeoutCountdown_ -= timePerStep;
		}
	}
	
	/**
	 * Resets this rsu so that it can be reused.
	
	public void reset(){
		//reset countdowns and other variables
		communicationCountdown_ = 0;
		knownVehiclesTimeoutCountdown_ = 0;
		beaconCountdown_ = (int)Math.round(x_)%beaconInterval_;
		communicationCountdown_ = (int)Math.round(x_)%communicationInterval_;
		
		//reset communication info
		knownVehiclesList_.clear();
		knownMessages_.clear();
	}
	/*
	
	/**
	 * Returns the Road-Side-Unit id
	 * 
	 * @return the RSU id
	 */
	public long getRSUID() {
		return rsuID_;
	}

	/**
	 * Returns the x coordinate of the RSU
	 * 
	 * @return the x coordinate
	 */
	public int getX() {
		return x_;
	}

	/**
	 * Returns the y coordinate of the RSU
	 * 
	 * @return the y coordinate
	 */
	public int getY() {
		return y_;
	}

	/**
	 * Sets the region the RSU is placed
	 * 
	 * @param region	the region
	 */
	public void setRegion(Region region) {
		region_ = region;
	}

	/**
	 * Returns the region the RSU is placed in
	 * 
	 * @return the region
	 */
	public Region getRegion() {
		return region_;
	}

	/**
	 * Returns the wifi radius
	 * 
	 * @return the wifi radius in cm
	 */
	public int getWifiRadius() {
		return wifiRadius_;
	}

	/**
	 * Gets the current beacon countdown
	 * 
	 * @return the beacon countdown
	 */
	public int getBeaconCountdown(){
		return beaconCountdown_;
	}
	
	/**
	 * Gets the current communication countdown
	 * 
	 * @return the communication countdown
	 */
	public int getCommunicationCountdown(){
		return communicationCountdown_;
	}
	
	/**
	 * Sets the reference to all regions. Call this on map reload!
	 * 
	 * @param regions	the array with all regions
	 */
	public static void setRegions(Region[][] regions){
		regions_ = regions;
	}

	/**
	 * Sets if beacons are enabled or not. Common to all Road-Side-Units.
	 * 
	 * @param state	<code>true</code> to enable beacons, else <code>false</code> 
	 */
	public static void setBeaconsEnabled(boolean state){
		beaconsEnabled_ = state;
	}
	
	/**
	 * Sets if communication is enabled or not. Common to all Road-Side-Units.
	 * 
	 * @param state	<code>true</code> to enable communication, else <code>false</code> 
	 */
	public static void setCommunicationEnabled(boolean state){
		communicationEnabled_ = state;
	}
	
	/**
	 * Sets a new value for the communication interval. Common to all Road-Side-Units.
	 * 
	 * @param communicationInterval	the new value 
	 */
	public static void setCommunicationInterval(int communicationInterval){
		communicationInterval_ = communicationInterval;
	}

	/**
	 * Sets a new value for the beacon interval. Common to all Road-Side-Units.
	 * 
	 * @param beaconInterval	the new value 
	 */
	public static void setBeaconInterval(int beaconInterval){
		beaconInterval_ = beaconInterval;
	}
	
	/**
	 * Sets if beacon zones should be monitored or not. Common to all RSUs.
	 * 
	 * @param beaconMonitorEnabled	<code>true</code> to enable monitoring mix zones, else <code>false</code> 
	 */
	public static void setBeaconMonitorZoneEnabled(boolean beaconMonitorEnabled){
		beaconMonitorEnabled_ = beaconMonitorEnabled;
	}
	
	/**
	 * Sets the values for the monitored beacon zone. A rectangular bounding box within the specified coordinates
	 * is monitored if {@link #setBeaconMonitorZoneEnabled(boolean)} is set to <code>true</code>.
	 * 
	 * @param beaconMonitorMinX	the minimum x coordinate
	 * @param beaconMonitorMaxX	the maximum x coordinate
	 * @param beaconMonitorMinY	the minimum y coordinate
	 * @param beaconMonitorMaxY	the maximum y coordinate
	 */
	public static void setMonitoredMixZoneVariables(int beaconMonitorMinX, int beaconMonitorMaxX, int beaconMonitorMinY, int beaconMonitorMaxY){
		beaconMonitorMinX_ = beaconMonitorMinX;
		beaconMonitorMaxX_ = beaconMonitorMaxX;
		beaconMonitorMinY_ = beaconMonitorMinY;
		beaconMonitorMaxY_ = beaconMonitorMaxY;
	}

	public boolean isEncrypted_() {
		return isEncrypted_;
	}

	public KnownVehiclesList getKnownVehiclesList_() {
		return knownVehiclesList_;
	}

	public static boolean isShowEncryptedBeaconsInMix_() {
		return showEncryptedBeaconsInMix_;
	}

	public static void setShowEncryptedBeaconsInMix_(
			boolean showEncryptedBeaconsInMix_) {
		RSU.showEncryptedBeaconsInMix_ = showEncryptedBeaconsInMix_;
	}
	
    public void sendKnownVehiclesToRSUs() {
        // we don't need to exchange anything if didn't receive anything new yet.
        if (!knownVehiclesList_.isNewBeaconToExchange()) {
            return;
        }
        knownVehiclesList_.setNewBeaconToEchange(false);
       
        if (knownVehiclesList_.getSize() == 0) {
            return;
        }

        // send to neighbor RSUs
        int i, j, size = 0, MapMinX, MapMinY, MapMaxX, MapMaxY, RegionMinX, RegionMinY, RegionMaxX, RegionMaxY;
        RSU[] rsus = null;
        RSU rsu = null;
        long dx, dy;

        // TODO: change to somekind of exchange radius? or just keep comDisntance because data is send like everything else?
        int rssExchangeRadius = wifiRadius_;
        long maxCommDistanceSquared = (long) rssExchangeRadius * rssExchangeRadius;

        // Minimum x coordinate to be considered for sending beacons
        long tmp = x_ - rssExchangeRadius;
        if (tmp < 0)
            MapMinX = 0; // Map stores only positive coordinates
        else if (tmp < Integer.MAX_VALUE)
            MapMinX = (int) tmp;
        else
            MapMinX = Integer.MAX_VALUE;

        // Maximum x coordinate to be considered for sending beacons
        tmp = x_ + (long) rssExchangeRadius;
        if (tmp < 0)
            MapMaxX = 0;
        else if (tmp < Integer.MAX_VALUE)
            MapMaxX = (int) tmp;
        else
            MapMaxX = Integer.MAX_VALUE;

        // Minimum y coordinate to be considered for sending beacons
        tmp = y_ - rssExchangeRadius;
        if (tmp < 0)
            MapMinY = 0;
        else if (tmp < Integer.MAX_VALUE)
            MapMinY = (int) tmp;
        else
            MapMinY = Integer.MAX_VALUE;

        // Maximum y coordinate to be considered for sending beacons
        tmp = y_ + (long) rssExchangeRadius;
        if (tmp < 0)
            MapMaxY = 0;
        else if (tmp < Integer.MAX_VALUE)
            MapMaxY = (int) tmp;
        else
            MapMaxY = Integer.MAX_VALUE;

        // Get the regions to be considered for sending beacons
        Region tmpregion = MAP.getRegionOfPoint(MapMinX, MapMinY);
        RegionMinX = tmpregion.getX();
        RegionMinY = tmpregion.getY();

        tmpregion = MAP.getRegionOfPoint(MapMaxX, MapMaxY);
        RegionMaxX = tmpregion.getX();
        RegionMaxY = tmpregion.getY();

        ownPositionEntryList = createPositionEntityList(knownVehiclesList_.getFirstKnownVehicle());
        
        // iterate through all regions in range
        for (i = RegionMinX; i <= RegionMaxX; ++i) {
            for (j = RegionMinY; j <= RegionMaxY; ++j) {

                rsus = regions_[i][j].getRSUs();
                size = rsus.length;

                for (int index = 0; index < size; ++index) {
                    rsu = rsus[index];
                    if (rsu != this && rsu.getX() >= MapMinX && rsu.getX() <= MapMaxX && rsu.getY() >= MapMinY && rsu.getY() <= MapMaxY) {
                        dx = rsu.getX() - x_;
                        dy = rsu.getY() - y_;
                        if ((dx * dx + dy * dy) <= maxCommDistanceSquared) {
                            // do exchanging here here
                            rsu.receiveKnownVehicles(true, ownPositionEntryList);
                        }
                    }
                }
            }
        }
    }

    private ArrayList<PositionEntity> createPositionEntityList(KnownVehicle[] knownVehicleArray) {
        ArrayList<PositionEntity> result = new ArrayList<PositionEntity>();

        KnownVehicle tmpKnownVehicle;
        for (int i = 0; i < knownVehicleArray.length; i++) {
            tmpKnownVehicle = knownVehicleArray[i];
            while (tmpKnownVehicle != null) {

                result.add(new PositionEntity(this.getRSUID(), this.getX(), this.getY(),tmpKnownVehicle,true));
                tmpKnownVehicle = tmpKnownVehicle.getNext();
            }
        }
        return result;
    }

    public synchronized void receiveKnownVehicles(boolean sourceIsRSU, ArrayList<PositionEntity> positionEntityArray) {
        if (sourceIsRSU) {
            receivedFromRSUVehiclelists.addAll(positionEntityArray);
        } else {
            receivedFromVehicleVehiclelists.addAll(positionEntityArray);
        }
    }

    
    public void doPositionVerification() {
        // clear the map of received information
        positionVerificationEntryMap.clear();

        // group received information according to the claimed VehicleID
        if (PositioningHelper.isPositionVerificationVehilceSendRssiToRsu()) {
            groupByID(receivedFromVehicleVehiclelists);
        }
        if (PositioningHelper.isPositionVerificationRsuSendRssiToRsu()) {
            groupByID(receivedFromRSUVehiclelists);
        }

        // RSU Trilateration
        if (PositioningHelper.isPositionVerificationRSU_Trilateration()) {
            // für jedes Fahrzeug das dieser RSU bekannt ist die verifizierung durchführen
            // nur wenn für das Fahrzeug auch meldungen der anderen RSUs vorhanden sind kann eine aussage getroffen werden
            for (PositionEntity entry : ownPositionEntryList) {
                long vehicleID = entry.getSenderID();
                int vehicleX = entry.getSenderX();
                int vehicleY = entry.getSenderY();

                ArrayList<PositionEntity> entryList = positionVerificationEntryMap.get(vehicleID);
                if (entryList == null || (entryList.size() < 2)) {
                    continue;
                }

                // calculate the distances from the RSU to the Vehicle based on the RSSI Measurements
                double[] dist = new double[3];
                dist[0] = PropagationModel.getInstance().calculateDistance(PropagationModel.getGlobalRSSToDistancePropagationModel(),
                        entry.getRSSI());
                dist[1] = PropagationModel.getInstance().calculateDistance(PropagationModel.getGlobalRSSToDistancePropagationModel(),
                        entryList.get(0).getRSSI());
                ;
                dist[2] = PropagationModel.getInstance().calculateDistance(PropagationModel.getGlobalRSSToDistancePropagationModel(),
                        entryList.get(1).getRSSI());

                int[] rsusX = { this.getX(), entryList.get(0).getReceiverX(), entryList.get(1).getReceiverX() };
                int[] rsusY = { this.getY(), entryList.get(0).getReceiverY(), entryList.get(1).getReceiverY() };

                double distanceRSU1_RSU2 = Math.sqrt(calculateSquaredDistance(rsusX[0], rsusY[0], rsusX[1], rsusY[1]));
                double distanceRSU1_RSU3 = Math.sqrt(calculateSquaredDistance(rsusX[0], rsusY[0], rsusX[2], rsusY[2]));
                double distanceRSU2_RSU3 = Math.sqrt(calculateSquaredDistance(rsusX[1], rsusY[1], rsusX[2], rsusY[2]));
                
                System.out.println("This RSU is: " + this.getRSUID() + " distance Vehicle to RSU1 ID1: " + this.getRSUID() + " dist: " + dist[0]
                        + " distance Vehicle to RSU2 ID2: " + entryList.get(0).getReceiverID() + " dist: " + dist[1]
                        + " distance Vehicle to RSU3 ID3: " + entryList.get(1).getReceiverID() + " dist: " + dist[2] + " distance RSU1-RSU2: "
                        + distanceRSU1_RSU2 + " distance RSU1-RSU3: " + distanceRSU1_RSU3 + " distance RSU2-RSU3: " + distanceRSU2_RSU3 + " R1-X: "
                        + this.getX() + " R1-Y: " + this.getY() + " R2-X: " + entryList.get(0).getReceiverX() + " R2-Y: "
                        + entryList.get(0).getReceiverY() + " R3-X: " + entryList.get(1).getReceiverX() + " R3-Y: " + entryList.get(1).getReceiverY()
                        + " Vehicle X: " + entry.getSenderX() + " Y: " + entry.getSenderY());

                boolean c1 = (dist[0] + dist[1] < distanceRSU1_RSU2);
                boolean c2 = (dist[0] + dist[2] < distanceRSU1_RSU3);
                boolean c3 = (dist[1] + dist[2] < distanceRSU2_RSU3);
                if (c1 || c2 || c3) {
                    continue;
                }
                
                // all clear, this RSU may do Verification now
                System.out.println("RSU: " + this.getRSUID() + " do verification");
                int allowedError = PositioningHelper.getAllowedError();

                boolean b1 = calculateSquaredDistance(rsusX[0], rsusY[0], vehicleX, vehicleY) <= ((dist[0] + allowedError) * (dist[0] + allowedError));
                boolean b2 = calculateSquaredDistance(rsusX[1], rsusY[1], vehicleX, vehicleY) <= ((dist[1] + allowedError) * (dist[1] + allowedError));
                boolean b3 = calculateSquaredDistance(rsusX[2], rsusY[2], vehicleX, vehicleY) <= ((dist[2] + allowedError) * (dist[2] + allowedError));

                if (b1 && b2 && b3) {
                    System.out.println("RSU: " + this.getRSUID() + " Vehicle X: " + vehicleX + " Vehicle Y: " + vehicleY + " Position is plausible");
                } else {
                    System.out.println("RSU: " + this.getRSUID() + " Vehicle X: " + vehicleX + " Vehicle Y: " + vehicleY + " Position is suspect"
                            + " real Position X: " + entry.getKnownVehicle().getVehicle().getX() + " real Position Y: "
                            + entry.getKnownVehicle().getVehicle().getY());


                    
                    // TODO: add Vehilce mark
                    GeneralLogWriter.log(
                            "1"+","+ // Source 1=RSU 0=Vehicle
                            this.rsuID_+","+ // Source ID
                            entry.getKnownVehicle().getVehicle().getID() +","+ // Vehicle ID
                            Renderer.getInstance().getTimePassed() +","+// Time Elapsed
                            entry.getKnownVehicle().getVehicle().isSybilVehicle() // is Sybil Vehicle?
                            );
                    
                    // mark Vehicle in GUI
                    entry.getKnownVehicle().getVehicle().setColor(Color.RED);

                }
            }
        }
        if (PositioningHelper.isPositionVerificationRSU_PredictMovement()) {
            ownPositionEntryList = createPositionEntityList(knownVehiclesList_.getFirstKnownVehicle());
            for (PositionEntity posEntry : ownPositionEntryList) {
                long vehicleID = posEntry.getSenderID();
                int vehicleX = posEntry.getSenderX();
                int vehicleY = posEntry.getSenderY();
                KnownVehicle tmpnKownVehicle = posEntry.getKnownVehicle();

                if (posEntry.getKnownVehicle().getArrayCounter() < 0) {
                    continue;
                }
                
                //TODO: maybe iterate through the entries to reach a delta T?
                int lastIndex = tmpnKownVehicle.getArrayCounter();
                
                // get the Time of the first Beacon
                int t0 = tmpnKownVehicle.getSavedLastUpdate_()[lastIndex];// [ms]
               
                // get the Time of the current Beacon
                int t1 = tmpnKownVehicle.getLastUpdate();// [ms]
               
                boolean foo = tmpnKownVehicle.getVehicle().isSybilVehicle();
                
                //TODO: this needs to be changed to support a Vehicle sending Sybil-Data 
                // currently only ARSUs may create Sybil Vehicles
                // es mus shie runterschieden werden zwischen einem sybil vehilce, das über arsu erstellt wird und einem das von einem anderen Vahrzeug kommt
                double v0;
                if (tmpnKownVehicle.getVehicle().isSybilVehicle()) {
                    v0 = 0;
                } else {
                    v0 = tmpnKownVehicle.getSavedSpeed_()[lastIndex];// [cm/s]
                }
                
                double travelDistance = (t1 - t0) * v0 * (1.0d / 100.0d);// [mm]
                double currentRssi = tmpnKownVehicle.getRssi();// [dBm]

                double d0 = PropagationModel.getInstance().calculateDistance(PropagationModel.getGlobalRSSToDistancePropagationModel(),
                        tmpnKownVehicle.getSavedRssi()[lastIndex]);// [cm]

                double rssi_max = PropagationModel.getInstance().calculateRSSI(PropagationModel.getGlobalDistanceToRSSPropagationModel(),
                        d0 + travelDistance);
                double rssi_min = PropagationModel.getInstance().calculateRSSI(PropagationModel.getGlobalDistanceToRSSPropagationModel(),
                        d0 - travelDistance);

                boolean b1 = (currentRssi <= rssi_min);
                boolean b2 = (currentRssi >= rssi_max);
                if (b1 && b2) {
                    System.out.println("RSU: " + this.getRSUID() + " VehicleID: " + vehicleID + " Vehicle X: " + vehicleX + " Vehicle Y: " + vehicleY
                            + " Position is plausible");
                } else {
                    System.out.println("RSU: " + this.getRSUID() + " VehicleID: " + vehicleID + " Vehicle X: " + vehicleX + " Vehicle Y: " + vehicleY
                            + " Position is suspect" + " real Position X: " + tmpnKownVehicle.getVehicle().getX() + " real Position Y: "
                            + tmpnKownVehicle.getVehicle().getY());
                  
                    //TODO: add Vehilce mark
                    GeneralLogWriter.log(
                            "1"+","+ // Source 1=RSU 0=Vehicle
                            this.rsuID_+","+ // Source ID
                            tmpnKownVehicle.getVehicle().getID() +","+ // Vehicle ID
                            Renderer.getInstance().getTimePassed() +","+// Time Elapsed
                            tmpnKownVehicle.getVehicle().isSybilVehicle() // is Sybil Vehicle?
                            );
                    
                    // mark Vehicle in GUi
                    tmpnKownVehicle.getVehicle().setColor(Color.RED);
                }
            }
        }
        // do some cleanup for future phases
        receivedFromVehicleVehiclelists.clear();
        receivedFromRSUVehiclelists.clear();
    }

    /**
     * @param positionEntityArray
     */
    private void groupByID(ArrayList<PositionEntity> positionEntityArray) {
        
        for (PositionEntity positionEntity : positionEntityArray) {
            Long senderID = new Long(positionEntity.getSenderID());
            if (positionVerificationEntryMap.get(senderID) == null) {
                // add entry if ID is not present
                positionVerificationEntryMap.put(senderID, new ArrayList<PositionEntity>());
            }
            // add entry if ID is present
            positionVerificationEntryMap.get(senderID).add(positionEntity);
        }

    }

    /**
     * calculates the squared distance between two points (x1,y1) and (x2,y2).
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return the distance (no unit conversion)
     */
    private double calculateSquaredDistance(int x1, int y1, int x2, int y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
    }
}