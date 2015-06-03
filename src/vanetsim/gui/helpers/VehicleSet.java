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
package vanetsim.gui.helpers;

import java.awt.Color;
import java.util.ArrayList;

public class VehicleSet {
	private final String name_;
	private ArrayList<SimulationProperty> propertyList_ = new ArrayList<SimulationProperty>();

	/** vehicle attributes --> see Vehicle.java */
	int vehicleLength_;
	int minSpeed_;
	int maxSpeed_;
	int minCommDist_;
	int maxCommDist_;
	int minWait_;
	int maxWait_;
	int minBraking_;
	int maxBraking_;	
	int minAcceleration_;
	int maxAcceleration_;	
	int minTimeDistance_;	
	int maxTimeDistance_;
	int minPoliteness_;	
	int maxPoliteness_;	
	int wiFi_;
	int emergencyVehicle_;
	int fakingVehicle_;
	String fakeMessagesTypes_;
	int amount_;
	int speedStreetRestriction_;
	int vehiclesDeviatingMaxSpeed_;
	int deviationFromSpeedLimit_;
	Color color_;	
	
	public VehicleSet(String name){
		name_ = name;
	}

	public ArrayList<SimulationProperty> getPropertyList_() {
		return propertyList_;
	}

	public void setPropertyList_(ArrayList<SimulationProperty> propertyList_) {
		this.propertyList_ = propertyList_;
	}

	public String getName_() {
		return name_;
	}
	
	public void setData(int vehicleLength, int minSpeed, int maxSpeed, int minCommDist, int maxCommDist, int minWait, int maxWait, int minBraking, int maxBraking,	
	int minAcceleration, int maxAcceleration, int minTimeDistance, int maxTimeDistance, int minPoliteness, int maxPoliteness, int wiFi,int emergencyVehicle,
	int fakingVehicle, String fakeMessagesTypes, int amount, int speedStreetRestriction, int vehiclesDeviatingMaxSpeed, int deviationFromSpeedLimit, Color color){
		vehicleLength_ = vehicleLength;
		minSpeed_ = minSpeed;
		maxSpeed_ = maxSpeed;
		minCommDist_ = minCommDist;
		maxCommDist_ = maxCommDist;
		minWait_ = minWait;
		maxWait_ = maxWait;
		minBraking_ = minBraking;
		maxBraking_ = maxBraking;	
		minAcceleration_ = minAcceleration;
		maxAcceleration_ = maxAcceleration;	
		minTimeDistance_ = minTimeDistance;	
		maxTimeDistance_ = maxTimeDistance;
		minPoliteness_ = minPoliteness;	
		maxPoliteness_ = maxPoliteness;	
		wiFi_ = wiFi;
		emergencyVehicle_ = emergencyVehicle;
		fakingVehicle_ = fakingVehicle;
		fakeMessagesTypes_ = fakeMessagesTypes;
		amount_ = amount;
		speedStreetRestriction_ = speedStreetRestriction;
		vehiclesDeviatingMaxSpeed_ = vehiclesDeviatingMaxSpeed;
		deviationFromSpeedLimit_ = deviationFromSpeedLimit;
		color_ = color;	
	}
	
	public String toString(){
		return name_;
	}
	
	public int getVehicleLength_() {
		return vehicleLength_;
	}

	public void setVehicleLength_(int vehicleLength_) {
		this.vehicleLength_ = vehicleLength_;
	}

	public int getMinSpeed_() {
		return minSpeed_;
	}

	public void setMinSpeed_(int minSpeed_) {
		this.minSpeed_ = minSpeed_;
	}

	public int getMaxSpeed_() {
		return maxSpeed_;
	}

	public void setMaxSpeed_(int maxSpeed_) {
		this.maxSpeed_ = maxSpeed_;
	}

	public int getMinCommDist_() {
		return minCommDist_;
	}

	public void setMinCommDist_(int minCommDist_) {
		this.minCommDist_ = minCommDist_;
	}

	public int getMaxCommDist_() {
		return maxCommDist_;
	}

	public void setMaxCommDist_(int maxCommDist_) {
		this.maxCommDist_ = maxCommDist_;
	}

	public int getMinWait_() {
		return minWait_;
	}

	public void setMinWait_(int minWait_) {
		this.minWait_ = minWait_;
	}

	public int getMaxWait_() {
		return maxWait_;
	}

	public void setMaxWait_(int maxWait_) {
		this.maxWait_ = maxWait_;
	}

	public int getMinBraking_() {
		return minBraking_;
	}

	public void setMinBraking_(int minBraking_) {
		this.minBraking_ = minBraking_;
	}

	public int getMaxBraking_() {
		return maxBraking_;
	}

	public void setMaxBraking_(int maxBraking_) {
		this.maxBraking_ = maxBraking_;
	}

	public int getMinAcceleration_() {
		return minAcceleration_;
	}

	public void setMinAcceleration_(int minAcceleration_) {
		this.minAcceleration_ = minAcceleration_;
	}

	public int getMaxAcceleration_() {
		return maxAcceleration_;
	}

	public void setMaxAcceleration_(int maxAcceleration_) {
		this.maxAcceleration_ = maxAcceleration_;
	}

	public int getMinTimeDistance_() {
		return minTimeDistance_;
	}

	public void setMinTimeDistance_(int minTimeDistance_) {
		this.minTimeDistance_ = minTimeDistance_;
	}

	public int getMaxTimeDistance_() {
		return maxTimeDistance_;
	}

	public void setMaxTimeDistance_(int maxTimeDistance_) {
		this.maxTimeDistance_ = maxTimeDistance_;
	}

	public int getMinPoliteness_() {
		return minPoliteness_;
	}

	public void setMinPoliteness_(int minPoliteness_) {
		this.minPoliteness_ = minPoliteness_;
	}

	public int getMaxPoliteness_() {
		return maxPoliteness_;
	}

	public void setMaxPoliteness_(int maxPoliteness_) {
		this.maxPoliteness_ = maxPoliteness_;
	}

	public int getWiFi_() {
		return wiFi_;
	}

	public void setWiFi_(int wiFi_) {
		this.wiFi_ = wiFi_;
	}

	public int getEmergencyVehicle_() {
		return emergencyVehicle_;
	}

	public void setEmergencyVehicle_(int emergencyVehicle_) {
		this.emergencyVehicle_ = emergencyVehicle_;
	}

	public int getFakingVehicle_() {
		return fakingVehicle_;
	}

	public void setFakingVehicle_(int fakingVehicle_) {
		this.fakingVehicle_ = fakingVehicle_;
	}

	public String getFakeMessagesTypes_() {
		return fakeMessagesTypes_;
	}

	public void setFakeMessagesTypes_(String fakeMessagesTypes_) {
		this.fakeMessagesTypes_ = fakeMessagesTypes_;
	}

	public int getAmount_() {
		return amount_;
	}

	public void setAmount_(int amount_) {
		this.amount_ = amount_;
	}

	public int getSpeedStreetRestriction_() {
		return speedStreetRestriction_;
	}

	public void setSpeedStreetRestriction_(int speedStreetRestriction_) {
		this.speedStreetRestriction_ = speedStreetRestriction_;
	}

	public int getVehiclesDeviatingMaxSpeed_() {
		return vehiclesDeviatingMaxSpeed_;
	}

	public void setVehiclesDeviatingMaxSpeed_(int vehiclesDeviatingMaxSpeed_) {
		this.vehiclesDeviatingMaxSpeed_ = vehiclesDeviatingMaxSpeed_;
	}

	public int getDeviationFromSpeedLimit_() {
		return deviationFromSpeedLimit_;
	}

	public void setDeviationFromSpeedLimit_(int deviationFromSpeedLimit_) {
		this.deviationFromSpeedLimit_ = deviationFromSpeedLimit_;
	}

	public Color getColor_() {
		return color_;
	}

	public void setColor_(Color color_) {
		this.color_ = color_;
	}
}
