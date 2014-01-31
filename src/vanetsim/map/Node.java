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

import java.awt.Color;
import java.util.ArrayList;

import vanetsim.scenario.RSU;

/**
 * A node on the map.
 */
public final class Node {
			
	/** A common counter to generate unique IDs */
	private static int counter_ = 0;
	
	/** A unique ID for this node */
	private final int nodeID_;
	
	/** The x coordinate. */
	private int x_;
	
	/** The y coordinate. */
	private int y_;	
	
	/** flag if the node has a traffic signal */
	private boolean hasTrafficSignal_;
	
	/** An array containing all streets going out from this node. */
	private Street[] outgoingStreets_ = new Street[0];		// only needed for storing/iterating of a small amount of streets so an array is by far the fastest solution!

	/** An array containing all streets coming into or going out from this node. */
	private Street[] crossingStreets_ = new Street[0];
	
	/** The region in which this node is. */
	private Region region_;
	
	/** Holds the junction associated with this node or <code>null</code> if this is not a junction. */
	private Junction junction_ = null;
	
	/** Saves the mix zone radius if this node includes a mix zone */
	private int mixZoneRadius_ = 0;
	
	/** Saves the RSU if encrypted beacons are activated */
	private RSU encryptedRSU_ = null;

	/** Traffic Light*/
	private TrafficLight trafficLight_ = null;
	
	/** Traffic Light Collections */
	private int[] streetHasException_ = null;
	
	/** saves amenitys from the osm map */
	private String amenity_ = "";
	
	/** the color of the node */
	private Color nodeColor = Color.black;
	
	/**
	 * Instantiates a new node.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public Node(int x, int y) {
		x_ = x;
		y_ = y;
		hasTrafficSignal_ = false;
		nodeID_ = counter_;
		++counter_;
	}
	
	/**
	 * Instantiates a new node.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param hasTrafficSignal signals if node has a traffic signal
	 */
	public Node(int x, int y, boolean hasTrafficSignal) {
		x_ = x;
		y_ = y;
		hasTrafficSignal_ = hasTrafficSignal;
		nodeID_ = counter_;
		++counter_;
	}

	
	/**
	 * Calculates if this is a junction and the priorities of all possible ways which go over this junction.
	 */
	public void calculateJunction(){		
		if(crossingStreets_.length < 3) junction_ = null;	//if only 2 incomings, it's a street which is just continuing (or the end of a street)!
		else {
			int size = crossingStreets_.length;
			//check if there are at least 2 incoming streets!
			Street tmpStreet;
			int i, count = 0;
			for(i = 0; i < size; ++i){
				tmpStreet = crossingStreets_[i];
				if(!tmpStreet.isOneway() || tmpStreet.getEndNode() == this) ++count;
			}
			if(count < 2) junction_ = null;
			else {
				//A real junction which means quite a lot of work.
				ArrayList<Street> priorityStreets = new ArrayList<Street>();
				String[] namesArray = new String[size];		//basic arrays and bruteforce-search is best here because there are normally only about 3-4 streets!
				int[] foundCount = new int[size];
				int[] maxSpeedArray = new int[size];
				int j = 0;
				count = 0;
				boolean alreadyExisted, foundContinuing = false;
				ArrayList<Node> sourceNodes = new ArrayList<Node>();
				//Step 1: Try to find continuing streets and sourceNodes
				for(i = 0; i < size; ++i){
					tmpStreet = crossingStreets_[i];
					if(!tmpStreet.isOneway()){	//twowayStreet => always a source to add
						if(tmpStreet.getStartNode() != this) sourceNodes.add(tmpStreet.getStartNode());
						else sourceNodes.add(tmpStreet.getEndNode());
					} else if (tmpStreet.getStartNode() != this) sourceNodes.add(tmpStreet.getStartNode());		//onewayStreet: only add if it is really incoming!
					alreadyExisted = false;
					for(j = 0; j < count; ++j){		//check if we already found this street
						if (namesArray[j].equals(tmpStreet.getName())){
							alreadyExisted = true;
							foundContinuing = true;
							++foundCount[j];
							if(maxSpeedArray[j] < tmpStreet.getSpeed()) maxSpeedArray[j] = tmpStreet.getSpeed();
						}
					}
					if(!alreadyExisted){		//didn't exist previously
						namesArray[count] = tmpStreet.getName();
						foundCount[count] = 1;
						maxSpeedArray[count] = tmpStreet.getSpeed();
						++count;
					}
				}
				//Step 2: Find the priority streets based on 2 rules.
				//1. rule: try to find a street which is continuing (has same name) and take the one with the highest speed if there is more than one
				if(foundContinuing){
					int k = -1, maxSpeed = 0;				
					for(i = 0; i < count; ++i){
						if(foundCount[i] > 1 && maxSpeedArray[i] > maxSpeed){
							k = i;
							maxSpeed = maxSpeedArray[i];
						}
					}
					if(k != -1){
						for(i = 0; i < size; ++i){
							tmpStreet = crossingStreets_[i];
							if(tmpStreet.getName().equals(namesArray[k])) priorityStreets.add(tmpStreet);
						}
					}
				//2. rule: take the two streets with highest speed. If two of the three highest are oneway, add a third one.
				} else {
					Street fastestStreet = null, secondFastestStreet = null, thirdFastestStreet = null;
					int fastestSpeed = 0, secondFastestSpeed = 0, thirdFastestSpeed = 0;
					for(i = 0; i < size; ++i){
						tmpStreet = crossingStreets_[i];
						if(tmpStreet.getSpeed() > fastestSpeed){
							thirdFastestSpeed = secondFastestSpeed;
							thirdFastestStreet = secondFastestStreet;
							secondFastestSpeed = fastestSpeed;
							secondFastestStreet = fastestStreet;
							fastestSpeed = tmpStreet.getSpeed();
							fastestStreet = tmpStreet;
						} else if(tmpStreet.getSpeed() > secondFastestSpeed){
							secondFastestSpeed = fastestSpeed;
							secondFastestStreet = fastestStreet;
							fastestSpeed = tmpStreet.getSpeed();
							fastestStreet = tmpStreet;
						} else if(tmpStreet.getSpeed() > thirdFastestSpeed){
							thirdFastestSpeed = tmpStreet.getSpeed();
							thirdFastestStreet = secondFastestStreet;
						}
					}
					priorityStreets.add(fastestStreet);
					priorityStreets.add(secondFastestStreet);
					if(thirdFastestStreet != null && ((fastestStreet.isOneway() && secondFastestStreet.isOneway()) || (secondFastestStreet.isOneway() && thirdFastestStreet != null && thirdFastestStreet.isOneway()) || (fastestStreet.isOneway() && thirdFastestStreet != null && thirdFastestStreet.isOneway()))){
						priorityStreets.add(thirdFastestStreet);
					}
				}
				//we might have three streets now but three are only valid if there's one incoming oneway, one outgoing oneway and one twoway street. Else only take two.
				count = 2;
				if(priorityStreets.size() > 2){
					Street twoWayStreet = null;
					Street firstOnewayStreet = null;
					Street secondOnewayStreet = null;
					for(i = 0; i < 3; ++i){
						if(priorityStreets.get(i).isOneway()){
							if(firstOnewayStreet == null) firstOnewayStreet = priorityStreets.get(i);
							else if(secondOnewayStreet == null) secondOnewayStreet = priorityStreets.get(i);
						} else if(twoWayStreet == null) twoWayStreet = priorityStreets.get(i);
					}
					if(twoWayStreet != null && firstOnewayStreet != null && secondOnewayStreet != null){
						if((firstOnewayStreet.getEndNode() == this && secondOnewayStreet.getEndNode() != this) || (firstOnewayStreet.getEndNode() != this && secondOnewayStreet.getEndNode() == this)) count = 3;
					}
				}
				while(priorityStreets.size() > count) priorityStreets.remove(priorityStreets.size()-1);	//trim priorityStreets to correct size
				size = sourceNodes.size();
				Node tmpNode, priorityEndNode, turnOffNode;
				Street sourceStreet = null, targetStreet;
				Street priorityEndStreet;
				//Step 3: Now create the junction and fill it's hashmaps with priorities for every possible connection
				junction_ = new Junction(this, priorityStreets.toArray(new Street[1]));
				for(i = 0; i < size; ++i){
					tmpNode = sourceNodes.get(i);
					//find source street
					for(j = 0; j < crossingStreets_.length; ++j){
						if(crossingStreets_[j].getStartNode() == tmpNode || crossingStreets_[j].getEndNode() == tmpNode){
							sourceStreet = crossingStreets_[j];
							break;
						}
					}
					//find end street and end node of priority street if source is already a priority street
					priorityEndNode = null;
					priorityEndStreet = null;
						if(count == 2){
							for(j = 0; j < count; ++j){
								if(priorityStreets.get(j) != sourceStreet) priorityEndStreet = priorityStreets.get(j);				
							}
						} else {	//three priority streets with 2 oneways and 1 twoway
							if(!sourceStreet.isOneway()){	//the twoway street is the source
								for(j = 0; j < count; ++j){
									if(priorityStreets.get(j) != sourceStreet && priorityStreets.get(j).getStartNode() == this){	//take the outgoing oneway street
										priorityEndStreet = priorityStreets.get(j);
									}
								}
							} else {	//the incoming oneway street is the source
								for(j = 0; j < count; ++j){
									if(!priorityStreets.get(j).isOneway()){	//take the one and only twoway street
										priorityEndStreet = priorityStreets.get(j);
									}
								}
							}
						}
						if(priorityEndStreet.getStartNode() != this) priorityEndNode = priorityEndStreet.getStartNode();
						else priorityEndNode = priorityEndStreet.getEndNode();
				
					//look through all outgoing streets
					for(j = 0; j < outgoingStreets_.length; ++j){
						targetStreet = outgoingStreets_[j];	
						if(targetStreet != sourceStreet){
							if(targetStreet.getStartNode() != this) turnOffNode = targetStreet.getStartNode();
							else turnOffNode = targetStreet.getEndNode();
							if(priorityStreets.contains(sourceStreet)){
								if(targetStreet == priorityEndStreet){
									//add to highest prio
									junction_.addJunctionRule(tmpNode, turnOffNode, 1);
								} else if(isLineRight(tmpNode.getX(), tmpNode.getY(), priorityEndNode.getX(), priorityEndNode.getY(), turnOffNode.getX(), turnOffNode.getY())){
									//add to highest prio as a right turnoff
									junction_.addJunctionRule(tmpNode, turnOffNode, 2);
								} else {
									//add to middle prio
									junction_.addJunctionRule(tmpNode, turnOffNode, 3);
								}
							} else {
								if(priorityStreets.contains(targetStreet)){		// target is a priority street. Find the priority street belonging to this
									int k;
									if(count == 2){
										for(k = 0; k < count; ++k){
											if(priorityStreets.get(k) != targetStreet) priorityEndStreet = priorityStreets.get(k);				
										}
									} else {	//three priority streets with 2 oneways and 1 twoway
										if(!sourceStreet.isOneway()){	//the twoway street is the source
											for(k = 0; k < count; ++k){
												if(priorityStreets.get(k) != targetStreet && priorityStreets.get(k).getStartNode() == this){	//take the outgoing oneway street
													priorityEndStreet = priorityStreets.get(k);
												}
											}
										} else {	//the incoming oneway street is the source
											for(k = 0; k < count; ++k){
												if(!priorityStreets.get(k).isOneway()){	//take the one and only twoway street
													priorityEndStreet = priorityStreets.get(k);
												}
											}
										}
									}
									if(priorityEndStreet.getStartNode() != this) priorityEndNode = priorityEndStreet.getStartNode();
									else priorityEndNode = priorityEndStreet.getEndNode();
									if(isLineRight(tmpNode.getX(), tmpNode.getY(), priorityEndNode.getX(), priorityEndNode.getY(), turnOffNode.getX(), turnOffNode.getY())){
										//add lowest priority but without need to check if a vehicle is coming from targetStreet
										junction_.addJunctionRule(tmpNode, turnOffNode, 4);
									} else {
										//add to lowest priority
										junction_.addJunctionRule(tmpNode, turnOffNode, 5);
									}
								} else {
									//add to lowest priority
									junction_.addJunctionRule(tmpNode, turnOffNode, 5);
								}
							}
						}
					}
				}
			}			
		}
	}
	
	/**
	 * Checks if the turnoff is at the right hand side compared to a (priority) street which goes from start over current node to an end.
	 * 
	 * @param startX 		the x coordinate of the start
	 * @param starty 		the y coordinate of the start
	 * @param endX 			the x coordinate of the end
	 * @param endY 			the y coordinate of the end
	 * @param turnoffX 		the x coordinate of the turnoff
	 * @param turnoffY 		the y coordinate of the turnoff
	 * 
	 * @return <code>true</code>, if line is on right side, else <code>false</code>
	 */
	private boolean isLineRight(int startX, int starty, int endX, int endY, int turnoffX, int turnoffY){
		//get the polar coordinates (root of "virtual" coordinate system is the junction!) 
		double startLineAngle = (Math.atan2(starty - y_ , startX - x_) + Math.PI);	//adding PI to get non-negative values from 0...2PI
		double endLineAngle = (Math.atan2(endY - y_ , endX - x_) + Math.PI);
		double turnoffLineAngle = (Math.atan2(turnoffY - y_ , turnoffX - x_) + Math.PI);
		
		//check if the angle between turnoff and startLine is larger than the angle between endLine and startLine
		double diffTurnoff = turnoffLineAngle-startLineAngle;
		if (diffTurnoff < 0) diffTurnoff += 2*Math.PI;	//calculated angle in wrong orientation => correct
		double diffEnd = endLineAngle-startLineAngle;
		if (diffEnd < 0) diffEnd += 2*Math.PI;
		
		if(diffTurnoff > diffEnd) return true;
		else return false;
	}
	
	
	/**
	 * Returns the junction object associated with this node or <code>null</code> if this is not a junction.
	 * 
	 * @return the junction or <code>null</code> if this is not a junction
	 */
	public Junction getJunction(){
		return junction_;
	}
	
	/**
	 * Adds an outgoing street. If the array already contains the street, nothing is done.
	 * Note that this operation is not thread-safe.
	 * 
	 * @param street The outgoing street to add.
	 */
	public void addOutgoingStreet(Street street) {
		boolean found = false;
		for(int i = 0; i < outgoingStreets_.length; ++i){
			if(outgoingStreets_[i] == street){
				found = true;
				break;
			}			
		}
		if(!found){
			Street[] newArray = new Street[outgoingStreets_.length+1];
			System.arraycopy (outgoingStreets_,0,newArray,0,outgoingStreets_.length);
			newArray[outgoingStreets_.length] = street;
			outgoingStreets_ = newArray;
		}
	}
	
	/**
	 * Removes an outgoing street. If the array doesn't contain the street, nothing is done.
	 * Note that this operation is not thread-safe.
	 * 
	 * @param street The outgoing street to delete.
	 * 
	 * @return <code>true</code> if street was removed, <code>false</code> if the street wasn't in the list
	 */
	public boolean delOutgoingStreet(Street street){
		for(int i = 0; i < outgoingStreets_.length; ++i){
			if(outgoingStreets_[i] == street){
				Street[] newArray = new Street[outgoingStreets_.length-1];
				if(i > 0){
					System.arraycopy (outgoingStreets_,0,newArray,0,i);
					System.arraycopy (outgoingStreets_,i+1,newArray,i,outgoingStreets_.length-i-1);
				} else System.arraycopy (outgoingStreets_,1,newArray,0,outgoingStreets_.length-1);
				outgoingStreets_ = newArray;
				return true;
			}			
		}
		return false;
	}
	
		
	/**
	 * Gets an array of the outgoing streets of this node. You will always get an array (never <code>null</code>)
	 * but it might have zero size.
	 * 
	 * @return the array
	 */
	public Street[] getOutgoingStreets() {
		return outgoingStreets_;
	}
	
	/**
	 * Gets the number of streets going out from this node.
	 * 
	 * @return the amount of streets
	 */
	public int getOutgoingStreetsCount() {
		return outgoingStreets_.length;
	}
	
	/**
	 * Adds a crossing street. If the array already contains the street, nothing is done.
	 * Note that this operation is not thread-safe.
	 * 
	 * @param street	the crossing street to add.
	 */
	public void addCrossingStreet(Street street) {
		boolean found = false;
		for(int i = 0; i < crossingStreets_.length; ++i){
			if(crossingStreets_[i] == street){
				found = true;
				break;
			}			
		}
		if(!found){
			Street[] newArray = new Street[crossingStreets_.length+1];
			System.arraycopy (crossingStreets_,0,newArray,0,crossingStreets_.length);
			newArray[crossingStreets_.length] = street;
			crossingStreets_ = newArray;
		}
	}
	
	/**
	 * Removes a crossing street. If the array doesn't contain the street, nothing is done.
	 * Note that this operation is not thread-safe.
	 * 
	 * @param street	the incoming street to delete.
	 * 
	 * @return <code>true</code> if street was removed, <code>false</code> if the street wasn't in the list
	 */
	public boolean delCrossingStreet(Street street){
		for(int i = 0; i < crossingStreets_.length; ++i){
			if(crossingStreets_[i] == street){
				Street[] newArray = new Street[crossingStreets_.length-1];
				if(i > 0){
					System.arraycopy (crossingStreets_,0,newArray,0,i);
					System.arraycopy (crossingStreets_,i+1,newArray,i,crossingStreets_.length-i-1);
				} else System.arraycopy (crossingStreets_,1,newArray,0,crossingStreets_.length-1);
				crossingStreets_ = newArray;
				return true;
			}			
		}
		return false;
	}
	
	/**
	 * Gets an array of the streets which are crossing in this node. You will always get an array (never <code>null</code>)
	 * but it might have zero size.
	 * 
	 * @return the array
	 */
	public Street[] getCrossingStreets() {
		return crossingStreets_;
	}
	
	/**
	 * Gets the number of streets crossing in this node.
	 * 
	 * @return the amount of streets
	 */
	public int getCrossingStreetsCount() {
		return crossingStreets_.length;
	}
	
	/**
	 * Gets the x coordinate.
	 * 
	 * @return the x coordinate
	 */
	public int getX() {
		return x_;
	}

	/**
	 * Sets the x coordinate
	 * 
	 * @param x	the new coordinate
	 */
	public void setX(int x) {
		x_ = x;
	}
	/**
	 * Gets the y coordinate.
	 * 
	 * @return the y coordinate
	 */
	public int getY() {
		return y_;
	}
	
	/**
	 * Sets the y coordinate
	 * 
	 * @param y	the new coordinate
	 */
	public void setY(int y) {
		y_ = y;
	}

	/**
	 * Sets the region in which this node is found.
	 * 
	 * @param region the region
	 */
	public void setRegion(Region region) {
		region_ = region;
	}
	
	/**
	 * Gets the region in which this node is found.
	 * 
	 * @return the region
	 */
	public Region getRegion() {
		return region_;
	}
	
	/**
	 * Checks if this object is equal to another.
	 * 
	 * @param other the object to compare to
	 * 
	 * @return <code>true</code>, if both are equal
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other){
		if(other == null) return false;
		else if (!this.getClass().equals(other.getClass())) return false;
		else {
			Node othernode = (Node)other;
			if(othernode.getX() == x_ && othernode.getY() == y_) return true;
			else return false;
		}
	}
	
	/**
	 * Returns the unique ID of this node.
	 * 
	 * @return an integer
	 */
	public int getNodeID(){
		return nodeID_;
	}
	
	/**
	 * The maximum ID a node has.
	 * 
	 * @return the maximum ID
	 */
	public static int getMaxNodeID(){
		return counter_;
	}
	
	/**
	 * Resets the node ID counter so that newly created nodes begin with an ID of 0.
	 */
	public static void resetNodeID(){
		counter_ = 0;
	}
	
	/**
	 * Creates a hash code (needed for HashMaps or similar structures).
	 * 
	 * @return an Integer
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode(){
		return (x_ - y_);
	}
	
	/**
	 * Sets the mix zone radius
	 * 
	 * @param mixZoneRadius		the new mix zone radius
	 */
	public void setMixZoneRadius(int mixZoneRadius) {
		mixZoneRadius_ = mixZoneRadius;
	}

	/**
	 * The mixZoneRadius
	 * 
	 * @return the max zone radius
	 */
	public int getMixZoneRadius() {
		return mixZoneRadius_;
	}

	public RSU getEncryptedRSU_() {
		return encryptedRSU_;
	}

	public void setEncryptedRSU_(RSU encryptedRSU_) {
		this.encryptedRSU_ = encryptedRSU_;
	}

	public boolean isHasTrafficSignal_() {
		return hasTrafficSignal_;
	}

	public void setHasTrafficSignal_(boolean hasTrafficSignal_) {
		this.hasTrafficSignal_ = hasTrafficSignal_;
	}

	/**
	 * @param trafficLight_ the trafficLight_ to set
	 */
	public void setTrafficLight_(TrafficLight trafficLight_) {
		this.trafficLight_ = trafficLight_;
	}

	/**
	 * @return the trafficLight_
	 */
	public TrafficLight getTrafficLight_() {
		return trafficLight_;
	}


	/**
	 * @return the streetHasException_
	 */
	public int[] getStreetHasException_() {
		return streetHasException_;
	}

	/**
	 * @param streetHasException_ the streetHasException_ to set
	 */
	public void setStreetHasException_(int[] streetHasException_) {
		this.streetHasException_ = streetHasException_;
	}
	
	
	/**
	 * Fill Exception Array of a String.
	 */
	public void addSignalExceptionsOfString(String arrayString) {
		String[] tmpArray = arrayString.split(":");
		streetHasException_ = new int[tmpArray.length];
		
		for(int i = 0; i < tmpArray.length; i++) streetHasException_[i] = Integer.parseInt(tmpArray[i]);
	}


	/**
	 * Check if a Traffic Signal has non-default settings
	 * 
	 * @return true if settings are non-default
	 */
	public boolean hasNonDefaultSettings() {
		if(streetHasException_ == null) return false;
		
		boolean tmpReturn = false;
		
		for(int i : streetHasException_) if(i != 1) tmpReturn = true;
		
		return tmpReturn;
	}
	
	/**
	 * Write exception array in one string. Please check if Signal has exceptions before using.
	 * 
	 * @return string with exceptions
	 */
	public String getSignalExceptionsInString() {
		String tmpReturn = "";
		
		for(int i : streetHasException_) tmpReturn += i + ":";
		
		if(tmpReturn.length() > 0) tmpReturn = tmpReturn.substring(0, tmpReturn.length() - 1);
		
		return tmpReturn;
	}

	/**
	 * @return the amenity_
	 */
	public String getAmenity_() {
		return amenity_;
	}

	/**
	 * @param amenity_ the amenity_ to set
	 */
	public void setAmenity_(String amenity_) {
		this.amenity_ = amenity_;
	}

	/**
	 * @return the nodeColor
	 */
	public Color getNodeColor() {
		return nodeColor;
	}

	/**
	 * @param nodeColor the nodeColor to set
	 */
	public void setNodeColor(Color nodeColor) {
		this.nodeColor = nodeColor;
	}

	
	
}