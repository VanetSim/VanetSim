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
import java.awt.geom.Point2D;
import java.util.ArrayList;

import vanetsim.scenario.LaneContainer;
import vanetsim.scenario.LaneObject;


/**
 * A street on the map.
 */
public final class Street {
	
	/** The length in cm. Though this is a redundant information, it is cached here in order to improve performance! */
	private final double length_;
	
	/** The x correction factor for lanes. */
	private final double xFactor_; 
	
	/** The y correction factor for lanes. */
	private final double yFactor_;
	
	/** The region this street is primarily assigned to. */
	private final Region mainRegion_;
	
	/** The lane from startNode to endNode. */
	private final LaneContainer startToEndLane_ = new LaneContainer(true);
	
	/** The lane from endNode to startNode. */
	private final LaneContainer endToStartLane_ = new LaneContainer(false);
	
	/** An identifier for this street. */
	private String name_;
	
	/** The start node of the street. */
	private Node startNode_;
	
	/** The end node of the street. */
	private Node endNode_;
	
	/** The maximum speed allowed on this street. */
	private int maxSpeed_;
	
	/** Indicates if this is a oneway-street. */
	private boolean oneway_;
	
	/** Type of Street */
	private String streetType_;
	
	/** Indicates how much lanes this street has per direction. */
	private int laneCount_;
	
	/** The color in which this street is displayed. */
	private Color displayColor_;
	
	/** An <code>ArrayList</code> which stores points (two successive points are one line). The points are used for rendering of bridges. */
	private ArrayList<Point2D.Double> bridgePaintLines_ = null;
	
	/** An <code>ArrayList</code> which stores points (four successive points are one polygon). The points are used for rendering bridges. */
	private ArrayList<Point2D.Double> bridgePaintPolygons_ = null;
	
	
	/** The state of the traffic light assigned to this street on the start node. */
	private int startNodeTrafficLightState_ = -1;
	
	/** The state of the traffic light assigned to this street on the end node. */
	private int endNodeTrafficLightState_ = -1;	
	
	/** The x drawing point for this traffic light */
	private int trafficLightEndX_ = -1;
	
	/** The y drawing point for this traffic light */
	private int trafficLightEndY_ = -1;	
	
	
	/** The x drawing point for this traffic light */
	private int trafficLightStartX_ = -1;
	
	/** The y drawing point for this traffic light */
	private int trafficLightStartY_ = -1;	
	
	/** set a flag if the street node is a priority street-node (only for performance issues) */
	private boolean priorityOnEndNode = false;	

	/** set a flag if the street node is a priority street-node (only for performance issues) */
	private boolean priorityOnStartNode = false;	
	
	/**
	 * Instantiates a new street.
	 * 
	 * @param name 			the name of this street
	 * @param startNode		the start node
	 * @param endNode 		the end node
	 * @param streetType	type of the street
	 * @param oneway 		<code>0</code>=twoway-street, <code>1</code>=oneway from startNode to endNode, else: oneway from endNode to startNode
	 * @param lanes 		the number of lanes on this street in one direction (!). Normal streets should have <code>1</code> here, motorways <code>2</code> or more!
	 * @param displayColor	the color in which to paint this street
	 * @param mainRegion 	the main region
	 * @param maxSpeed 		the maximum speed allowed on this street
	 */
	public Street(String name, Node startNode, Node endNode, String streetType, int oneway, int lanes, Color displayColor, Region mainRegion, int maxSpeed) {
		name_ = name;
		streetType_ = streetType;
		displayColor_ = displayColor;
		laneCount_ = lanes;
		mainRegion_ = mainRegion;
		maxSpeed_ = maxSpeed;
		if (oneway == 0){
			startNode_ = startNode;
			endNode_ = endNode;
			startNode_.addOutgoingStreet(this);
			endNode_.addOutgoingStreet(this);
			startNode_.addCrossingStreet(this);
			endNode_.addCrossingStreet(this);
			oneway_ = false;
		} else if (oneway == 1) {
			startNode_ = startNode;
			endNode_ = endNode;
			startNode_.addOutgoingStreet(this);
			startNode_.addCrossingStreet(this);
			endNode_.addCrossingStreet(this);
			oneway_ = true;
		} else {	// oneway street with wrong order => change it!
			endNode_ = startNode;
			startNode_ = endNode;
			startNode_.addOutgoingStreet(this);
			startNode_.addCrossingStreet(this);
			endNode_.addCrossingStreet(this);
			oneway_ = true;
		}
		
		long dx = endNode_.getX() - startNode_.getX();
		long dy = endNode_.getY() - startNode_.getY();
		length_ = Math.sqrt(dx * dx + dy * dy); 	// Pythagorean theorem: a^2 + b^2 = c^2

		//Calculate lane correction
		double[] result = new double[2];
		MapHelper.getXYParallelRight(startNode_.getX(), startNode_.getY(), endNode_.getX(), endNode_.getY(), Map.LANE_WIDTH, result);
		xFactor_ = result[0];
		yFactor_ = result[1];
	}

	/**
	 * Gets the length of the street.
	 * 
	 * @return the length of the street
	 */
	public double getLength() {
		return length_;
	}
	
	/**
	 * Gets the x correction factor of one lane for position calculation (in cm). Only valid if going
	 * from startNode to endNode (else you need to take the negative).
	 * 
	 * @return the x correction factor
	 */
	public double getXFactor(){
		return xFactor_;
	}
	
	/**
	 * Gets the x correction factor of one lane for position calculation (in cm). Only valid if going
	 * from startNode to endNode (else you need to take the negative).
	 * 
	 * @return the y correction factor
	 */
	public double getYFactor(){
		return yFactor_;
	}
	
	/**
	 * Gets the speed.
	 * 
	 * @return the speed
	 */
	public int getSpeed() {
		return maxSpeed_;
	}
	
	/**
	 * Sets the maximum speed.
	 * 
	 * @param maxSpeed the new speed
	 */
	public void setSpeed(int maxSpeed) {
		maxSpeed_ = maxSpeed;
	}
	
	/**
	 * Changes if this street is a oneway street or not.
	 * 
	 * @param oneway	<code>0</code>=twoway-street, <code>1</code>=oneway from startNode to endNode, else: oneway from endNode to startNode
	 */
	public void changeOneWay(int oneway){
		startNode_.delOutgoingStreet(this);
		endNode_.delOutgoingStreet(this);
		if(oneway == 0){
			oneway_ = false;
			startNode_.addOutgoingStreet(this);
			endNode_.addOutgoingStreet(this);
		} else if (oneway == 1){
			oneway_ = true;
			startNode_.addOutgoingStreet(this);
			endNode_.delOutgoingStreet(this);
		} else {
			oneway_ = true;
			Node tmpNode = endNode_;		//swap nodes
			endNode_ = startNode_;
			startNode_ = tmpNode;
			startNode_.addOutgoingStreet(this);
			endNode_.delOutgoingStreet(this);
		}
	}
	
	/**
	 * Gets the amount of lanes in one direction. If this is a twoway street, you need to multiply by 2 to get the
	 * total amount of lanes.
	 * 
	 * @return the number of lanes per direction
	 */
	public int getLanesCount() {
		return laneCount_;
	}
	
	/**
	 * Sets the amount of lanes.
	 * 
	 * @param laneCount the number of lanes per direction
	 */
	public void setLanesCount(int laneCount) {
		laneCount_ = laneCount;
	}
	
	/**
	 * Gets the first lane object. You may iterate through the objects using <code>getNext()</code> and
	 * <code>getPrevious()</code>. Note that the lane might change while you're iterating as it's not synchronized!
	 * 
	 * @param direction	<code>true</code> in the direction from startNode to endNode, <code>false</code> seen from
	 * 					endNode to startNode
	 * 
	 * @return the first lane object
	 */
	public LaneObject getFirstLaneObject(boolean direction){
		if(direction) return startToEndLane_.getHead();
		else return endToStartLane_.getHead();
	}
	
	/**
	 * Gets the last lane object. You may iterate through the objects using <code>getNext()</code> and
	 * <code>getPrevious()</code>. Note that the lane might change while you're iterating as it's not synchronized!
	 * 
	 * @param direction	<code>true</code> in the direction from startNode to endNode, <code>false</code> seen from
	 * 					endNode to startNode
	 * 
	 * @return the last lane object
	 */
	public LaneObject getLastLaneObject(boolean direction){
		if(direction) return startToEndLane_.getTail();
		else return endToStartLane_.getTail();
	}
		
	/**
	 * Adds a lane object. The underlying lane container is synchronized during this operation.
	 * 
	 * @param object	the object to add
	 * @param direction	<code>true</code> in the direction from startNode to endNode, <code>false</code> seen from
	 * 					endNode to startNode
	 */
	public void addLaneObject(LaneObject object, boolean direction){
		if(direction) startToEndLane_.addSorted(object);
		else endToStartLane_.addSorted(object);
	}
	
	/**
	 * Removes a lane object. The underlying lane container is synchronized during this operation.
	 * 
	 * @param object	the object to remove
	 * @param direction	<code>true</code> in the direction from startNode to endNode, <code>false</code> seen from
	 * 					endNode to startNode
	 */
	public void delLaneObject(LaneObject object, boolean direction){
		if(direction) startToEndLane_.remove(object);
		else endToStartLane_.remove(object);
	}
	
	/**
	 * Updates a lane object. The underlying lane container is synchronized during this operation.
	 * 
	 * @param object		the object to check for updates
	 * @param direction		<code>true</code> in the direction from startNode to endNode, <code>false</code> seen from
	 * 						endNode to startNode
	 * @param newPosition	the new position of the object
	 */
	public void updateLaneObject(LaneObject object, boolean direction, double newPosition){
		if(direction) startToEndLane_.updatePosition(object, newPosition);
		else endToStartLane_.updatePosition(object, newPosition);
	}
	
	/**
	 * Clears all objects from the lanes container.
	 */
	public void clearLanes(){
		startToEndLane_.clear();
		endToStartLane_.clear();
	}
	
	/**
	 * Adds a line for painting a bridge.
	 * 
	 * @param x1	the x coordinate of the start point
	 * @param y1	the y coordinate of the start point
	 * @param x2	the x coordinate of the end point
	 * @param y2	the y coordinate of the end point
	 */
	public void addBridgePaintLine(double x1, double y1, double x2, double y2){
		if(bridgePaintLines_ == null) bridgePaintLines_ = new ArrayList<Point2D.Double>(2);
		bridgePaintLines_.add(new Point2D.Double(x1,y1));
		bridgePaintLines_.add(new Point2D.Double(x2,y2));
	}
	
	/**
	 * Adds a polygon for painting a bridge.
	 * 
	 * @param x1	the x coordinate of the first point
	 * @param y1	the y coordinate of the first point
	 * @param x2	the x coordinate of the second point
	 * @param y2	the y coordinate of the second point
	 * @param x3	the x coordinate of the third point
	 * @param y3	the y coordinate of the third point
	 * @param x4	the x coordinate of the fourth point
	 * @param y4	the y coordinate of the fourth point
	 */
	public void addBridgePaintPolygon(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4){
		if(bridgePaintPolygons_ == null) bridgePaintPolygons_ = new ArrayList<Point2D.Double>(2);
		bridgePaintPolygons_.add(new Point2D.Double(x1,y1));
		bridgePaintPolygons_.add(new Point2D.Double(x2,y2));
		bridgePaintPolygons_.add(new Point2D.Double(x3,y3));
		bridgePaintPolygons_.add(new Point2D.Double(x4,y4));
	}
	

	/**
	 * Gets the lines to paint a bridge.
	 * 
	 * @return the <code>ArrayList</code> with all lines to paint (two successive points are a line)
	 */
	public ArrayList<Point2D.Double> getBridgePaintLines(){
		return bridgePaintLines_;
	}
	
	/**
	 * Gets the polygons to paint a bridge.
	 * 
	 * @return the <code>ArrayList</code> with all lines to paint (two successive points are a line)
	 */
	public ArrayList<Point2D.Double> getBridgePaintPolygons(){
		return bridgePaintPolygons_;
	}
	
	/**
	 * Gets the start node of this street.
	 * 
	 * @return the start node
	 */
	public Node getStartNode(){
		return startNode_;
	}
	
	public void setStartNode(Node startNode) {
		startNode_ = startNode;
	}

	/**
	 * Gets the end node of this street.
	 * 
	 * @return the end node
	 */
	public Node getEndNode(){
		return endNode_;
	}
	
	public void setEndNode(Node endNode) {
		endNode_ = endNode;
	}

	/**
	 * Gets the display color of this street.
	 * 
	 * @return the display color
	 */
	public Color getDisplayColor(){
		return displayColor_;
	}
	
	/**
	 * Sets the display color of this street.
	 * 
	 * @param displayColor	the new display color
	 */
	public void setDisplayColor(Color displayColor){
		displayColor_ = displayColor;
	}
	
	/**
	 * Gets the region to which this street is primarily assigned to.
	 * 
	 * @return the region
	 */
	public Region getMainRegion(){
		return mainRegion_;
	}
	
	/**
	 * Sets the name of this street.
	 * 
	 * @param name the new name
	 */
	public void setName(String name){
		name_ = name;
	}
	
	/**
	 * Gets the name of this street.
	 * 
	 * @return the name
	 */
	public String getName(){
		return name_;
	}
	
	/**
	 * Indicates if this is a oneway-street or not. Oneway-streets always go from the StartNode to the EndNode!
	 * 
	 * @return <code>true</code> if it's oneway, else <code>false</code>
	 */
	public boolean isOneway(){
		return oneway_;
	}
	
	/**
	 * Checks if this object is equal to another.
	 * 
	 * @param other	the object to compare to
	 * 
	 * @return <code>true</code>, if equals
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other){
		if(other == null) return false;
		else if (!this.getClass().equals(other.getClass())) return false;
		else {
			Street otherstreet = (Street)other;
			if(otherstreet.getStartNode() == startNode_ && otherstreet.getEndNode() == endNode_) return true;
			else return false;
		}
	}
	
	/**
	 * Creates a hash code (needed for HashMaps or similar structures).
	 * 
	 * @return an Integer
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode(){
        return new Long(((long)startNode_.getX() - startNode_.getY() + endNode_.getY() - endNode_.getX())%Integer.MAX_VALUE).intValue();
    }

	/**
	 * Gets the type of this street.
	 * 
	 * @return the street-type
	 */
	public String getStreetType_() {
		return streetType_;
	}

	/**
	 * Sets the type of this street.
	 * 
	 * @param streetType_ the type of the street
	 */
	public void setStreetType_(String streetType_) {
		this.streetType_ = streetType_;
	}
	
	/**
	 * Gets the state of the traffic light.
	 * 
	 * @return the state of the traffic light
	 */
	public int getStartNodeTrafficLightState() {
		return startNodeTrafficLightState_;
	}

	/**
	 * Sets the status of the traffic light
	 * 
	 * @param startNodeTrafficLightState_ the status of the traffic light
	 */
	public void setStartNodeTrafficLightState(int startNodeTrafficLightState_) {
		this.startNodeTrafficLightState_ = startNodeTrafficLightState_;
	}

	/**
	 * Gets the state of the traffic light.
	 * 
	 * @return the state of the traffic light
	 */
	public int getEndNodeTrafficLightState() {
		return endNodeTrafficLightState_;
	}

	/**
	 * Sets the status of the traffic light
	 * 
	 * @param endNodeTrafficLightState_ the status of the traffic light
	 */
	public void setEndNodeTrafficLightState(int endNodeTrafficLightState_) {
		this.endNodeTrafficLightState_ = endNodeTrafficLightState_;
	}
	
	/**
	 * Updates the state of the traffic light.
	 * 
	 */
	public void updateStartNodeTrafficLightState() {
		startNodeTrafficLightState_ = (startNodeTrafficLightState_ + 1) %8;
	}
	
	/**
	 * Updates the state of the traffic light.
	 * 
	 */
	public void updateEndNodeTrafficLightState() {
		endNodeTrafficLightState_ = (endNodeTrafficLightState_ + 1) %8;
	}


	/**
	 * @param trafficLightEndX_ the trafficLightEndX_ to set
	 */
	public void setTrafficLightEndX_(int trafficLightEndX_) {
		this.trafficLightEndX_ = trafficLightEndX_;
	}

	/**
	 * @return the trafficLightEndX_
	 */
	public int getTrafficLightEndX_() {
		return trafficLightEndX_;
	}

	/**
	 * @param trafficLightStartX_ the trafficLightStartX_ to set
	 */
	public void setTrafficLightStartX_(int trafficLightStartX_) {
		this.trafficLightStartX_ = trafficLightStartX_;
	}

	/**
	 * @return the trafficLightStartX_
	 */
	public int getTrafficLightStartX_() {
		return trafficLightStartX_;
	}

	/**
	 * @param trafficLightStartY_ the trafficLightStartY_ to set
	 */
	public void setTrafficLightStartY_(int trafficLightStartY_) {
		this.trafficLightStartY_ = trafficLightStartY_;
	}

	/**
	 * @return the trafficLightStartY_
	 */
	public int getTrafficLightStartY_() {
		return trafficLightStartY_;
	}

	/**
	 * @param trafficLightEndY_ the trafficLightEndY_ to set
	 */
	public void setTrafficLightEndY_(int trafficLightEndY_) {
		this.trafficLightEndY_ = trafficLightEndY_;
	}

	/**
	 * @return the trafficLightEndY_
	 */
	public int getTrafficLightEndY_() {
		return trafficLightEndY_;
	}

	/**
	 * @param priorityOnEndNode the priorityOnEndNode to set
	 */
	public void setPriorityOnEndNode(boolean priorityOnEndNode) {
		this.priorityOnEndNode = priorityOnEndNode;
	}

	/**
	 * @return the priorityOnEndNode
	 */
	public boolean isPriorityOnEndNode() {
		return priorityOnEndNode;
	}

	/**
	 * @param priorityOnStartNode the priorityOnStartNode to set
	 */
	public void setPriorityOnStartNode(boolean priorityOnStartNode) {
		this.priorityOnStartNode = priorityOnStartNode;
	}

	/**
	 * @return the priorityOnStartNode
	 */
	public boolean isPriorityOnStartNode() {
		return priorityOnStartNode;
	}
}