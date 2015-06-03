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


import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.EventLogWriter;
import vanetsim.map.Street;
import vanetsim.scenario.events.EventList;
import vanetsim.scenario.events.StartBlocking;
import vanetsim.scenario.events.StopBlocking;

/**
 * Stores all known penalties for streets. The streets are stored together with their direction and a penalty
 * in cm. Arrays are directly used here (in contrast to the KnownVehiclesList) as this allows easier and faster
 * usage in the routing algorithm. Extensibility is not a major concern here.
 * <br><br>
 * Note for developers: You need to make sure, that all used arrays always have the same size!
 */
public class KnownPenalties{

	/** The vehicle this data structure belongs to. */
	private final Vehicle vehicle_;

	/** The streets which have penalties. */
	private Street[] streets_;

	/** An array with directions corresponding to the streets. <code>1</code> means from endNode to startNode, 
	 * <code>0</code> means both directions and <code>-1</code> means from startNode to endNode */
	private int[] directions_;

	/** The penalties values. Stored in cm */
	private int[] penalties_ ;

	/** How long this entry will be valid. Measured in milliseconds from simulation start. */
	private int[] validUntil_;
	
	/** If this is a fake message */
	private boolean[] isFake_;
	
	/** Type of penalty message */
	private String[] penaltyType_;
	
	/** An array to store if a route update is necessary if this route is removed */
	private boolean[] routeUpdateNecessary_;

	/** The current size of the list. */
	private int size = 0;

	/** The x destination where the message occurred */
	private int x_[];
	
	/** The y destination where the message occurred */
	private int y_[];
	
	/** The lane */
	private int lane_[];
	
	/** if events should be logged */
	private static boolean logEvents_ = false;
	
	private Vehicle[] penaltySourceVehicle_;

	/** Alert if spamming */
	private static boolean spamCheck_ = false;
	
	/**
	 * Constructor.
	 * 
	 * @param vehicle	the vehicle this data structure belongs to.
	 */
	public KnownPenalties(Vehicle vehicle){
		vehicle_ = vehicle;
		// just presize so that resizing isn't needed that often
		streets_ = new Street[2];
		directions_ = new int[2];
		penalties_ = new int[2];
		validUntil_ = new int[2];
		routeUpdateNecessary_ = new boolean[2];
		isFake_ = new boolean[2];
		penaltyType_ = new String[2];
		x_ = new int[2];
		y_ = new int[2];
		lane_ = new int[2];
		penaltySourceVehicle_ = new Vehicle[2];
	}

	/**
	 * Updates or adds a penalty. If a penalty already existed, the values for penalty and validUntil are
	 * overwritten! If the penalty is new or differs from the last one, a new route calculation is initiated.
	 * 
	 * @param street 		the street
	 * @param direction 	the direction. <code>1</code> means from endNode to startNode, <code>0</code> means
	 * 						both directions and <code>-1</code> means from startNode to endNode
	 * @param penalty		the penalty in cm
	 * @param validUntil	how long this entry will be valid. Measured in milliseconds from simulation start
	 */
	public synchronized void updatePenalty(int x, int y, Street street, int lane, int direction, int penalty, int validUntil, boolean isFake, String penaltyType, long ID, Vehicle penaltySourceVehicle, boolean emergencyVehicle, boolean createBlocking){
		boolean found = false;
		boolean activateIDS = false;
		boolean otherPenaltyValue = false;
		boolean reallySamePenalty = false;
		
		if(spamCheck_){
			//spam check. Please use fake vehicle with eebl messages to measure the spam.
			if(penaltyType.equals("HUANG_EEBL")){
				vehicle_.getKnownEventSourcesList_().update(penaltySourceVehicle, ID, 0, 0, 0, false);
			}
		}


		for(int i = 0; i < streets_.length; ++i){
			if(streets_[i] == street && directions_[i] == direction && isFake_[i] == isFake && penaltyType_[i] == penaltyType){	// update existing value
				found = true;
				if(penalties_[i] != penalty) otherPenaltyValue = true;
				penalties_[i] = penalty;
				validUntil_[i] = validUntil;
				isFake_[i] = isFake;
				penaltyType_[i] = penaltyType;
				
				if(x_[i] == x && y_[i] == y)reallySamePenalty = true;
				break;
			}
		}

		if(!found){		
			if(size < streets_.length){	// arrays are still large enough
				streets_[size] = street;
				directions_[size] = direction;
				penalties_[size] = penalty;
				validUntil_[size] = validUntil;
				isFake_[size] = isFake;
				penaltyType_[size] = penaltyType;
				x_[size] = x;
				y_[size] = y;
				penaltySourceVehicle_[size] = penaltySourceVehicle;
			} else {
				// create larger arrays and insert element
				Street[] newArray = new Street[size + 2];
				System.arraycopy (streets_,0,newArray,0,size);
				newArray[size] = street;
				streets_ = newArray;

				int[] newArray2 = new int[size + 2];
				System.arraycopy (directions_,0,newArray2,0,size);
				newArray2[size] = direction;
				directions_ = newArray2;

				newArray2 = new int[size + 2];
				System.arraycopy (penalties_,0,newArray2,0,size);
				newArray2[size] = penalty;
				penalties_ = newArray2; 

				newArray2 = new int[size + 2];
				System.arraycopy (validUntil_,0,newArray2,0,size);
				newArray2[size] = validUntil;
				validUntil_ = newArray2;
				
				newArray2 = new int[size + 2];
				System.arraycopy (x_,0,newArray2,0,size);
				newArray2[size] = x;
				x_ = newArray2;
				
				newArray2 = new int[size + 2];
				System.arraycopy (y_,0,newArray2,0,size);
				newArray2[size] = y;
				y_ = newArray2;
				
				newArray2 = new int[size + 2];
				System.arraycopy (lane_,0,newArray2,0,size);
				newArray2[size] = lane;
				lane_ = newArray2;
				
				String[] newArray4 = new String[size + 2];
				System.arraycopy (penaltyType_,0,newArray4,0,size);
				newArray4[size] = penaltyType;
				penaltyType_ = newArray4;
						
				
				boolean[] newArray3 = new boolean[size + 2];
				System.arraycopy (newArray3,0,newArray3,0,size);
				newArray3[size] = false;
				routeUpdateNecessary_ = newArray3;
				
				/*
				boolean[] newArray3 = new boolean[size + 2];
				System.arraycopy (routeUpdateNecessary_,0,newArray3,0,size);
				newArray3[size] = false;
				routeUpdateNecessary_ = newArray3;
				*/
				
				newArray3 = new boolean[size + 2];
				System.arraycopy (isFake_,0,newArray3,0,size);
				newArray3[size] = isFake;
				isFake_ = newArray3;
				
				Vehicle[] newArray5 = new Vehicle[size + 2];
				System.arraycopy (penaltySourceVehicle_,0,newArray5,0,size);
				newArray5[size] = penaltySourceVehicle;
				penaltySourceVehicle_ = newArray5;
			}
			++size;			
		}

		// a really new information has arrived!
		if(!found || (otherPenaltyValue && !reallySamePenalty)){	
			
			boolean ruleActive = false;
			if(IDSProcessor.getActiveRules_() != null){
				for(int j = 0; j < IDSProcessor.getActiveRules_().length;j++)if(IDSProcessor.getActiveRules_()[j].equals(penaltyType)){
					ruleActive = true;
				}
			}
			
			
			//log data
			
			//** change event logger to do k-means analysis
			if(logEvents_) EventLogWriter.log(Renderer.getInstance().getTimePassed() + "," + penaltyType + "," + x + "," + y + "," + ID + "," + vehicle_.getID());

			//lets check if it is a false message
			if(Vehicle.isIdsActivated() && ruleActive){
				if(vehicle_.getID() != ID){
					if(penaltyType.equals("EVA_EMERGENCY_ID")) 	{
						activateIDS = vehicle_.getIdsProcessorList_().createIDSProcessor(vehicle_, vehicle_.getID(), ID, x, y, street, direction, penaltySourceVehicle, penaltyType, isFake, emergencyVehicle, createBlocking, true);
					
					}
					else activateIDS = vehicle_.getIdsProcessorList_().createIDSProcessor(vehicle_, vehicle_.getID(), ID, x, y, street, direction, penaltySourceVehicle, penaltyType, isFake, emergencyVehicle, createBlocking);
				}
			}
			else{
				if((penaltyType.equals("HUANG_EVA_FORWARD") || penaltyType.equals("EVA_EMERGENCY_ID")) && !vehicle_.isFakingMessages() &&  vehicle_.getCurStreet().getName().equals(street.getName()) && (((direction == -1) && vehicle_.getCurDirection()) || ((direction == 1) && !vehicle_.getCurDirection()))){	
					if(!vehicle_.isDrivingOnTheSide_() && hasToMoveOutOfTheWay(penaltySourceVehicle)){
						vehicle_.setMoveOutOfTheWay_(true);
						vehicle_.setWaitingForVehicle_(penaltySourceVehicle);
						if(penaltyType.equals("EVA_EMERGENCY_ID")) vehicle_.setForwardMessage_(true);
					}

				}				
			}

			

			// the route is affected => recalculate it!
			if(penaltyType.equals("HUANG_PCN") && vehicle_.getID() != ID){

				
				if(!ruleActive){
				
					// search if this new information affects the route of the vehicle as route calculation is quite costly
					Street[] routeStreets = vehicle_.getRouteStreets();
					boolean[] routeDirections = vehicle_.getRouteDirections();
					int i = vehicle_.getRoutePosition() + 1;	// increased by 1 because a penalty on the street on which the vehicle currently is isn't very helpful
					found = false;
					for(;i < routeStreets.length; ++i){
						if(routeStreets[i] == street){
							if(routeDirections[i]){		// from startNode to endNode
								if(direction < 1){
									found = true;
									break;
								}
							} else {		// from endNode to startNode
								if(direction > -1){
									found = true;
									break;
								}
							}
						}				
					}
					if(found){
						routeUpdateNecessary_[size-1] = true;
						vehicle_.calculateRoute(true, true);
					}

				}
				
			}	
			if(found && penaltyType.equals("HUANG_RHCN")  && !vehicle_.isFakingMessages() && vehicle_.getID() != ID){
				if(!Vehicle.isIdsActivated() || !activateIDS){

					try{
						StartBlocking start = new StartBlocking(Renderer.getInstance().getTimePassed(), x_[0], y_[0], direction, 20, false, "HUANG_RHCN");
						EventList.getInstance().addEvent(start); //$NON-NLS-1$	
						EventList.getInstance().addEvent(new StopBlocking(Renderer.getInstance().getTimePassed() + 10000, x_[0], y_[0], start)); //$NON-NLS-1$
					}
					catch(Exception e){e.printStackTrace();}


				}

				
			}	
			if(found && penaltyType.equals("HUANG_EEBL") && !vehicle_.isFakingMessages() && vehicle_.getID() != ID){
				if(!Vehicle.isIdsActivated() || !activateIDS){
					//braking not needing because the event spot handles this now

				}
			}
		}
		
	}
	
	/**
	 * Check for outdated entries and remove them. Note that this function is not synchronized! You need to make
	 * sure that no other thread uses any function on this object at the same time!
	 */
	public void checkValidUntil(){
		int timeout = Renderer.getInstance().getTimePassed();
		boolean updateRoute = false;
		for(int i = size - 1; i > -1; --i){	// going backwards because it's easier for deletion!
			if(validUntil_[i] < timeout){
				// check if route might be affected if we remove this
				if(routeUpdateNecessary_[i]) updateRoute = true;


				// Don't really remove. Just make the size smaller and copy everything to the front. The data left is some kind of garbage
				// but that doesn't matter...
				--size;
				
				System.arraycopy(streets_,i+1,streets_,i,size-i);
				System.arraycopy(directions_,i+1,directions_,i,size-i);
				System.arraycopy(penalties_,i+1,penalties_,i,size-i);
				System.arraycopy(validUntil_,i+1,validUntil_,i,size-i);
				System.arraycopy(routeUpdateNecessary_,i+1,routeUpdateNecessary_,i,size-i);
				System.arraycopy(isFake_,i+1,isFake_,i,size-i);
				System.arraycopy(penaltyType_,i+1,penaltyType_,i,size-i);
				System.arraycopy(x_,i+1,x_,i,size-i);
				System.arraycopy(y_,i+1,y_,i,size-i);
				System.arraycopy(lane_,i+1,lane_,i,size-i);		
				System.arraycopy(penaltySourceVehicle_,i+1,penaltySourceVehicle_,i,size-i);	
			}
		}
		// if one was removed, a recalculation is necessary
		if(updateRoute){
			vehicle_.calculateRoute(true, true);
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
		
		Street curStreet = vehicle_.getCurStreet();
		boolean curDirection = vehicle_.getCurDirection();
		
		for(;i < emergencyVehicle.getRoutePosition() + 5; ++i){
			if(routeStreets.length == i) break;
			if(routeStreets[i] == curStreet && routeDirections[i] == curDirection) return true;				
		}
		
		return false;
	}
	
	/**
	 * Gets all streets with known penalties.
	 * 
	 * @return an array with all streets
	 */

	public Street[] getStreets(){
		return streets_;
	}
	/**
	 * Gets an array with the directions corresponding to the getStreets()-function.
	 * <code>1</code> in the array means from endNode to startNode, <code>0</code> means
	 * both directions and <code>-1</code> means from startNode to endNode
	 * 
	 * @return an array with all directions
	 */

	public int[] getDirections(){
		return directions_;
	}

	/**
	 * Gets an array with the penalties corresponding to the getStreets()-function.
	 * Measured in cm. 
	 * 
	 * @return an array with all penalties
	 */
	public int[] getPenalties(){
		return penalties_;
	}

	/**
	 * Gets the amount of known penalties stored.
	 * 
	 * @return the size
	 */
	public int getSize(){
		return size;
	}

	/**
	 * @return the isFake_
	 */
	public boolean[] getIsFake_() {
		return isFake_;
	}

	/**
	 * @param isFake_ the isFake_ to set
	 */
	public void setIsFake_(boolean[] isFake_) {
		this.isFake_ = isFake_;
	}

	/**
	 * @return the penaltyType_
	 */
	public String[] getPenaltyType_() {
		return penaltyType_;
	}

	/**
	 * @param penaltyType_ the penaltyType_ to set
	 */
	public void setPenaltyType_(String[] penaltyType_) {
		this.penaltyType_ = penaltyType_;
	}

	/**
	 * @return the logEvents_
	 */
	public static boolean isLogEvents_() {
		return logEvents_;
	}

	/**
	 * @param logEvents_ the logEvents_ to set
	 */
	public static void setLogEvents_(boolean logEvents) {
		logEvents_ = logEvents;
	}

	public Vehicle getVehicle_() {
		return vehicle_;
	}

	public Vehicle[] getPenaltySourceVehicle_() {
		return penaltySourceVehicle_;
	}

	public void setPenaltySourceVehicle_(Vehicle[] penaltySourceVehicle_) {
		this.penaltySourceVehicle_ = penaltySourceVehicle_;
	}


	public static boolean isSpamcheck() {
		return spamCheck_;
	}

	public static void setSpamCheck_(boolean spamCheck_) {
		KnownPenalties.spamCheck_ = spamCheck_;
	}

	/**
	 * Clears everything from this data structure.
	 */
	public void clear(){
		streets_ = new Street[2];	// just presize so that resizing isn't needed that often
		directions_ = new int[2];
		penalties_ = new int[2];
		validUntil_ = new int[2];
		routeUpdateNecessary_ = new boolean[2];
		isFake_ = new boolean[2];
		penaltyType_ = new String[2];
		size = 0;
		x_ = new int[2];
		y_ = new int[2];
		lane_ = new int[2];
		penaltySourceVehicle_ = new Vehicle[2];
	}
}