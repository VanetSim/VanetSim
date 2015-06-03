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
package vanetsim.map;

import vanetsim.gui.Renderer;
import vanetsim.scenario.LaneObject;
import vanetsim.scenario.Vehicle;

/**
 * A junction which always belongs to one specific node. If vehicles move from one priority street to another, they don't have to wait.
 * Otherwise one vehicle can pass this junction every JUNCTION_PASS_INTERVAL. Vehicles coming from priority streets are preferred to
 * pass this junction.
 * <br><br>
 * To store the rules, one-dimensional arrays are used as they are very efficient here. Originally, a double-dimensional IdentityHashMap
 * was used but they need more RAM and are in most cases slower as there are only relatively few junction rules and linear searching 
 * through the arrays is faster than the IdentityHashMap-overhead with autoboxing, function calls and so on.
 */
public final class Junction{
	
	/** For passing a node, the needed distance is calculated by multiplying 
	 * the max. speed of the street with this factor. Measured in seconds. */
	private static final double JUNCTION_PASS_TIME_FREE = 1.5; //changed to 1.5 to get more realistic traffic
	
	/** The interval for cleaning up the junction queues. */
	private static final int JUNCTION_QUEUES_CLEANUP_INTERVAL = 1000;
	
	/** The maximum time in milliseconds to wait for a vehicle to pass the junction. If a vehicle
	 * doesn't signal that it has passed the junction within this time, another vehicle gets the permission! */
	private static final int MAXIMUM_TIME_ON_JUNCTION = 1000;
	/** A static reference to the renderer. */
	private static final Renderer renderer_ = Renderer.getInstance();
	
	/** The node this junction belongs to. */
	private final Node node_;
	
	/** An array with all priority street */
	private final Street[] priorityStreets_;
	
	/** The source nodes for the junction rules. */
	private Node[] rulesSourceNodes_ = null;
	
	/** The target nodes for the junction rules. */
	private Node[] rulesTargetNodes_ = null;
	
	/** The priorities for the junction rules. */
	private int[] rulesPriorities_ = null;
		
	/** A queue for the junction if more than one vehicle arrives. This is used for priority 3 streets (left turnoff from priority street). */
	private JunctionQueue junctionQueuePriority3_ = new JunctionQueue();
	
	/** A queue for the junction if more than one vehicle arrives This is used for priority 4 streets (normal turnoff). */
	private JunctionQueue junctionQueuePriority4_ = new JunctionQueue();
	
	/** The vehicle which is allowed to pass the junction in this step. Used against synchronization problems. */
	public Vehicle vehicleAllowedThisStep_ = null;
	
	/** When (in simulation time) the vehicleAllowedThisStep-variable was last set. */
	public int vehicleAllowedSetTime_ = 0;
	
	/** If a vehicle is currently on the junction so that no other one may go over it. */
	private boolean vehicleOnJunction_ = false;
	
	/** Since when (in simulation time) a vehicle is on the junction. */
	private int vehicleOnJunctionSince_ = -1;
	
	/** When (in simulation time) the next cleanup on the junction queues will be done.*/
	private int nextJunctionQueueCleanUp_ = JUNCTION_QUEUES_CLEANUP_INTERVAL;	


	/**
	 * Constructor
	 * 
	 * @param node				the node associated with this junction
	 * @param priorityStreets	the priority streets of this junction
	 */
	public Junction(Node node, Street[] priorityStreets){
		node_ = node;
		priorityStreets_ = priorityStreets;
	}
	
	/**
	 * Adds a junction rule.
	 * 
	 * @param startNode 	the start node
	 * @param targetNode	the target node
	 * @param priority		the priority (see {@link #getJunctionPriority(Node, Node)} for details)
	 */
	public void addJunctionRule(Node startNode, Node targetNode, int priority){
		Node[] newArray;
		int[] newArray2;
		if(rulesSourceNodes_ == null){
			newArray = new Node[1];
			newArray[0] = startNode;
		} else {
			newArray = new Node[rulesSourceNodes_.length + 1];
			System.arraycopy(rulesSourceNodes_,0,newArray,0,rulesSourceNodes_.length);
			newArray[rulesSourceNodes_.length] = startNode;
		}		
		rulesSourceNodes_ = newArray;
		
		if(rulesTargetNodes_ == null){
			newArray = new Node[1];
			newArray[0] = targetNode;
		} else {
			newArray = new Node[rulesTargetNodes_.length + 1];
			System.arraycopy(rulesTargetNodes_,0,newArray,0,rulesTargetNodes_.length);
			newArray[rulesTargetNodes_.length] = targetNode;
		}		
		rulesTargetNodes_ = newArray;
		
		if(rulesPriorities_ == null){
			newArray2 = new int[1];
			newArray2[0] = priority;
		} else {
			newArray2 = new int[rulesPriorities_.length + 1];
			System.arraycopy(rulesPriorities_,0,newArray2,0,rulesPriorities_.length);
			newArray2[rulesPriorities_.length] = priority;
		}		
		rulesPriorities_ = newArray2;
	}
	
	/**
	 * Gets the priority for going over this node.
	 * 
	 * @param startNode 	the node you're coming from
	 * @param targetNode the node you're going to
	 * 
	 * @return <code>1</code> if it's possible to go over without any notice, <code>2</code> if it's a right turnoff from a priority street,
	 * <code>3</code> if it's a left turnoff from a priority street or <code>4</code> if it's just a normal street (forced to stop at junction)
	 * with no need to look for vehicles at target street. <code>5</code> is the same as <code>4</code> but with a need to look for vehicles on
	 * target street. 
	 */
	public int getJunctionPriority(Node startNode, Node targetNode){
		int i, length = rulesPriorities_.length;
		for(i = 0; i < length; ++i){
			if(rulesSourceNodes_[i] == startNode && rulesTargetNodes_[i] == targetNode) return rulesPriorities_[i];
		}
		return 5; 		// node not in list of possible combinations...should only happen in very rare instances
	}
	
	
	/**
	 * Adds a waiting vehicle to the junction.<br>
	 * Note: This function sets the vehicle which is allowed to pass in the current simulation step. Thus, it is
	 * absolutely necessary, that this function is the first one in each step which is called on this object!
	 * 
	 * @param vehicle	the vehicle
	 * @param priority	the priority of the vehicle to pass this junction
	 */
	public synchronized void addWaitingVehicle(Vehicle vehicle, int priority){
		int curTime = renderer_.getTimePassed();
		if(curTime > vehicleAllowedSetTime_){		// Sets the vehicle which will be allowed to pass in the current step.
			vehicleAllowedSetTime_ = curTime;
			if(vehicleOnJunction_ && vehicleOnJunctionSince_ > curTime - MAXIMUM_TIME_ON_JUNCTION) vehicleAllowedThisStep_ = null;
			else{
				vehicleAllowedThisStep_ = junctionQueuePriority3_.getFirstVehicle();
				if(vehicleAllowedThisStep_ == null) vehicleAllowedThisStep_ = junctionQueuePriority4_.getFirstVehicle();
			}
		}
		if(curTime >= nextJunctionQueueCleanUp_){
			nextJunctionQueueCleanUp_ = curTime + JUNCTION_QUEUES_CLEANUP_INTERVAL;
			junctionQueuePriority3_.cleanUp();
			junctionQueuePriority4_.cleanUp();
		}
		if(priority == 3) junctionQueuePriority3_.addVehicle(vehicle);
		else junctionQueuePriority4_.addVehicle(vehicle);
	}
	
	/**
	 * Allows another vehicle to pass the junction by setting vehicleOnJunction_ to <code>false</code>.
	 */
	public void allowOtherVehicle(){
		vehicleOnJunction_ = false; 
	}

	
	/**
	 * Returns if a vehicle may pass a Traffic Light.
	 * 
	 * @param vehicle	the vehicle
	 * @param tmpStreet	the street
	 * @param nextNode	the next node the vehicle will go to
	 * 
	 * @return <code>true</code> if passing is allowed, else <code>false</code>
	 */
	public synchronized boolean canPassTrafficLight(Vehicle vehicle, Street tmpStreet, Node nextNode){
		//first check if there is a traffic light on this junction
	//	if(vehicle.getCurDirection() tmpStreet.)
		if(node_.isHasTrafficSignal_()){	
			if(tmpStreet.getStartNode().equals(node_) && tmpStreet.getStartNodeTrafficLightState() < 1) return true;
			else if(tmpStreet.getEndNode().equals(node_) && tmpStreet.getEndNodeTrafficLightState() < 1) return true;
		}
		return false;
	}
	
	
	
	/**
	 * Returns if a vehicle may pass. Passing is only allowed every 2,5s and if there's no vehicle coming from the priority streets.
	 * 
	 * @param vehicle	the vehicle
	 * @param priority	the priority of the vehicle to pass this junction
	 * @param nextNode	the next node the vehicle will go to
	 * 
	 * @return <code>true</code> if passing is allowed, else <code>false</code>
	 */
	public synchronized boolean canPassJunction(Vehicle vehicle, int priority, Node nextNode){
		if(vehicleAllowedThisStep_ == vehicle){
			Street[] outgoingStreets;
			Street tmpStreet, tmpStreet2 = null;
			LaneObject tmpLaneObject;
			Node nextNode2 = null;
			boolean tmpDirection;
			double distance, neededFreeDistance;
			int i;
			
			//check incoming priority streets if they are free

			//check if priority streets have enough space
			for(int j = 0; j < priorityStreets_.length; ++j){
				tmpStreet = priorityStreets_[j];
				if(tmpStreet != vehicle.getCurStreet() && (priority != 4 || (tmpStreet.getStartNode() != nextNode && tmpStreet.getEndNode() != nextNode))){
					if(tmpStreet.getStartNode() == node_) tmpDirection = false;
					else tmpDirection = true;
					neededFreeDistance = JUNCTION_PASS_TIME_FREE * tmpStreet.getSpeed();
					LaneObject previous = tmpStreet.getLastLaneObject(tmpDirection);
					distance = tmpStreet.getLength();
					if(previous != null){
						if(previous.getCurLane() == 1){
							if((tmpDirection && tmpStreet.getLength()-previous.getCurPosition() < neededFreeDistance) || (!tmpDirection && previous.getCurPosition() < neededFreeDistance)){
								if(previous.getCurSpeed() > 400) return false;
							}
						} else {	// need to search
							tmpLaneObject = previous.getPrevious();
							while(tmpLaneObject != null){
								if(tmpLaneObject.getCurLane() == 1){
									if((tmpDirection && tmpStreet.getLength()-tmpLaneObject.getCurPosition() < neededFreeDistance) || (!tmpDirection && tmpLaneObject.getCurPosition() < neededFreeDistance)){
										return false;
									}
									break;	// only check the first on our lane!
								}
								tmpLaneObject = tmpLaneObject.getPrevious();
							}
						}
					}
					if(tmpStreet.getLength() < neededFreeDistance){
						while(true){
							if(tmpDirection) nextNode2 = tmpStreet.getStartNode();
							else nextNode2 = tmpStreet.getEndNode();
							if(nextNode2.getCrossingStreetsCount() != 2) break;		// don't handle junctions!
							else outgoingStreets = nextNode2.getCrossingStreets();
							for(i = 0; i < outgoingStreets.length; ++i){
								tmpStreet2 = outgoingStreets[i];
								if(tmpStreet2 != tmpStreet){
									tmpStreet = tmpStreet2;
									if (tmpStreet2.getStartNode() == nextNode2){
										tmpDirection = true;
										break;		// found street we want to => no need to look through others
									} else {
										tmpDirection = false;
										break;		// found street we want to => no need to look through others
									}
								}					
							}				
		
							tmpLaneObject = tmpStreet.getLastLaneObject(tmpDirection);
							while(tmpLaneObject != null){
								if(tmpLaneObject.getCurLane() == 1){
									if((tmpDirection && tmpStreet.getLength()-tmpLaneObject.getCurPosition()+distance < neededFreeDistance) || (!tmpDirection && tmpLaneObject.getCurPosition()+distance < neededFreeDistance)){
										return false;
									}
									break;
								}
								tmpLaneObject = tmpLaneObject.getNext();
							}
		
							distance += tmpStreet.getLength();
							if(distance > neededFreeDistance) break;
						}
					}
				}
			}
			if(priority == 3) junctionQueuePriority3_.delFirstVehicle();
			else junctionQueuePriority4_.delFirstVehicle();
			vehicleOnJunction_ = true;
			vehicleOnJunctionSince_ = renderer_.getTimePassed();
			
		
			return true;
		} else return false;
	}

	
	/**
	 * Deletes the traffic light on this junction an resets the street values.	 * 
	 */
	public void delTrafficLight(){
		this.getNode().setTrafficLight_(null);
		this.getNode().setStreetHasException_(null);
		Street[] tmpStreets = node_.getCrossingStreets();
		for(int i = 0; i < tmpStreets.length; i++){
			if(tmpStreets[i].getStartNode() == node_) tmpStreets[i].setStartNodeTrafficLightState(-1);
			else tmpStreets[i].setEndNodeTrafficLightState(-1);
		}
		
		node_.setHasTrafficSignal_(false);		
	}
	
	
	public Node getNode() {
		return node_;
	}

	public Street[] getPriorityStreets() {
		return priorityStreets_;
	}

	/**
	 * @param trafficLight_ the trafficLight_ to set
	 */
	/*
	public void setTrafficLight_(TrafficLight trafficLight_) {
		this.trafficLight_ = trafficLight_;
	}
*/
	/**
	 * @return the trafficLight_
	 */
	/*
	public TrafficLight getTrafficLight_() {
		return trafficLight_;
	}
	*/
}