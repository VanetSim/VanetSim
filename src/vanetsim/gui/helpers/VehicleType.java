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


public class VehicleType {
	
/**
 * A vehicle type defines a special type of vehicle (e.g. "PKW"). 
*/
	
	/** The name of the vehicle type (e.g. "PKW")*/
	private final String name_;
	
	/** The vehicle length.*/
	private  int vehicleLength_;
	
	/** The max speed of the vehicle type.*/
	private  int maxSpeed_;
	
	/** The min speed of the vehicle type.*/
	private  int minSpeed_;
	
	/** The max communication distance of the vehicle type.*/
	private  int maxCommDist_;
	
	/** The min communication distance of the vehicle type.*/
	private  int minCommDist_;
	
	/** The max braking rate of the vehicle type in cm/s^2.*/
	private  int maxBrakingRate_;
	
	/** The min braking rate of the vehicle type in cm/s^2.*/
	private  int minBrakingRate_;
	
	/** The max acceleration rate of the vehicle type in cm/s^2.*/
	private  int maxAccelerationRate_;
	
	/** The min acceleration rate of the vehicle type in cm/s^2.*/
	private  int minAccelerationRate_;
	
	/** The input field for the minimum time distance in ms. */
	private int minTimeDistance_;	
	
	/** The input field for the maximum time distance in ms. */
	private int maxTimeDistance_;	

	/** The input field for the minimum politeness factor in %. */
	private int minPoliteness_;	
	
	/** The input field for the maximum politeness factor in %. */
	private int maxPoliteness_;	
	
	/** The input field for the amount of the vehicles deviating from the speed limit in % */
	private int vehiclesDeviatingMaxSpeed_;	
	
	/** The input field for the amount of deviation of the speed limit */
	private int deviationFromSpeedLimit_;	
	
	/** The max wait time of the vehicle type.*/
	private  int maxWaittime_;
	
	/** The min wait time of the vehicle type.*/
	private  int minWaittime_;
	
	/** The wifi support of the vehicle type.*/
	private  boolean wifi_;
	
	/** Vehicle type is a emergency vehicle.*/
	private  boolean emergencyVehicle_;
	
	/** The color of the vehicle type.*/
	private  int color_;
	
	/**
	 * Instantiates a new vehicle type.
	 * 
	 * @param name						name of the vehicle type
	 * @param maxSpeed					the maximum speed of this vehicle type in cm/s
	 * @param minSpeed					the minimum speed of this vehicle in cm/s
	 * @param maxCommDist				the maximum communication distance in cm/s.
	 * @param minCommDist				the minimum communication distance in cm/s.
 	 * @param maxBrakingRate			the maximum braking rate in cm/s^2.
	 * @param minBrakingRate			the minimum braking rate in cm/s^2.
	 * @param maxAccelerationRate		the maximum acceleration rate in cm/s^2.
	 * @param minAccelerationRate		the minimum acceleration rate in cm/s^2.
	 * @param maxWaittime				the maximum wait time in ms.
	 * @param minWaittime				the minimum wait time in ms.
	 * @param minTimeDistance 			the min time distance
	 * @param maxTimeDistance			the max time distance
	 * @param minPoliteness			    the min politeness
  	 * @param maxPoliteness			    the max politeness
	 * @param vehicleLength				the vehicle length in cm.
	 * @param wifi						<code>true</code>: wifi is enabled in the vehicle type
	 * @param emergencyVehicle			<code>true</code>: emergencyVehicle functions are enabled in the vehicle type
	 * @param color						the color of the vehicle type
	 */
	public VehicleType(String name, int vehicleLength, int maxSpeed, int minSpeed, int maxCommDist, int minCommDist, int maxBrakingRate, int minBrakingRate, int maxAccelerationRate, int minAccelerationRate, int minTimeDistance, int maxTimeDistance, int minPoliteness, int maxPoliteness, int vehiclesDeviatingMaxSpeed, int deviationFromSpeedLimit,  int maxWaittime, int minWaittime, boolean wifi, boolean emergencyVehicle, int color){
		name_ = name;
		vehicleLength_ = vehicleLength;
		maxSpeed_ = maxSpeed;
		minSpeed_ = minSpeed;
		maxCommDist_ = maxCommDist;
		minCommDist_ = minCommDist;
		maxWaittime_ = maxWaittime;
		minWaittime_ = minWaittime;
		maxBrakingRate_ = maxBrakingRate;
		minBrakingRate_ = minBrakingRate;
		maxAccelerationRate_ = maxAccelerationRate;
		minAccelerationRate_ = minAccelerationRate;
		minTimeDistance_ = minTimeDistance;
		maxTimeDistance_ = maxTimeDistance;
		minPoliteness_ = minPoliteness;
		maxPoliteness_ = maxPoliteness;
		vehiclesDeviatingMaxSpeed_ = vehiclesDeviatingMaxSpeed;
		deviationFromSpeedLimit_ = deviationFromSpeedLimit;
		wifi_ = wifi;
		emergencyVehicle_ = emergencyVehicle;
		color_ = color;
	}

	/**
	 * Gets the current name of the vehicle type.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name_;
	}

	/**
	 * Gets the maximum speed of the vehicle type.
	 * 
	 * @return maximum Speed
	 */
	public int getMaxSpeed() {
		return maxSpeed_;
	}
	
	/**
	 * Set the maximum speed
	 * 
	 * @param maxSpeed the maximum speed in cm/s
	 */
	public void setMaxSpeed(int maxSpeed) {
		maxSpeed_ = maxSpeed;
	}
	
	/**
	 * Gets the minimum Speed of the vehicle type.
	 * 
	 * @return the minimum Speed
	 */
	public int getMinSpeed() {
		return minSpeed_;
	}
	
	/**
	 * Set the minimum speed
	 * 
	 * @param minSpeed the maximum speed in cm/s.
	 */
	public void setMinSpeed(int minSpeed) {
		minSpeed_ = minSpeed;
	}

	/**
	 * Gets the maximum communication distance of the vehicle type.
	 * 
	 * @return the maximum communication distance
	 */
	public int getMaxCommDist() {
		return maxCommDist_;
	}
	
	/**
	 * Set the maximum communication distance
	 * 
	 * @param maxCommDist the maximum communication distance in cm.
	 */
	public void setMaxCommDist(int maxCommDist) {
		maxCommDist_ = maxCommDist;
	}

	/**
	 * Gets the minimum communication distance of the vehicle type.
	 * 
	 * @return the minimum communication distance
	 */
	public int getMinCommDist() {
		return minCommDist_;
	}
	
	/**
	 * Set the minimum communication distance
	 * 
	 * @param minCommDist the minimum communication distance in cm.
	 */
	public void setMinCommDist(int minCommDist) {
		minCommDist_ = minCommDist;
	}

	/**
	 * Gets the maximum wait time of the vehicle type.
	 * 
	 * @return the maximum wait time
	 */
	public int getMaxWaittime() {
		return maxWaittime_;
	}
	
	/**
	 * Set the maximum wait time
	 * 
	 * @param maxWaittime the maximum wait time in ms.
	 */
	public void setMaxWaittime(int maxWaittime) {
		maxWaittime_ = maxWaittime;
	}

	/**
	 * Gets the minimum wait time of the vehicle type.
	 * 
	 * @return the minimum wait time
	 */
	public int getMinWaittime() {
		return minWaittime_;
	}
	
	/**
	 * Set the minimum wait time
	 * 
	 * @param minWaittime the minimum wait time in ms.
	 */
	public void setMinWaittime(int minWaittime) {
		minWaittime_ = minWaittime;
	}

	/**
	 * Gets the color of the vehicle type
	 * 
	 * @return the color
	 */
	public int getColor() {
		return color_;
	}
	
	/**
	 * Set the color.
	 * 
	 * @param color the rgb color
	 */
	public void setColor(int color) {
		color_ = color;
	}

	/**
	 * Gets the maximum braking rate of the vehicle type.
	 * 
	 * @return the maximum braking rate
	 */
	public int getMaxBrakingRate() {
		return maxBrakingRate_;
	}
	
	/**
	 * Set the maximum braking rate.
	 * 
	 * @param maxBrakingRate the maximum braking rate in cm/s^2
	 */
	public void setMaxBrakingRate(int maxBrakingRate) {
		maxBrakingRate_ = maxBrakingRate;
	}

	/**
	 * Gets the minimum braking rate of the vehicle type.
	 * 
	 * @return the minimum braking rate
	 */
	public int getMinBrakingRate() {
		return minBrakingRate_;
	}
	
	/**
	 * Set the minimum braking rate.
	 * 
	 * @param minBrakingRate the minimum braking rate in cm/s^2
	 */
	public void setMinBrakingRate(int minBrakingRate) {
		minBrakingRate_ = minBrakingRate;
	}

	/**
	 * Gets the maximum acceleration rate of the vehicle type.
	 * 
	 * @return the maximum acceleration rate
	 */
	public int getMaxAccelerationRate() {
		return maxAccelerationRate_;
	}
	
	/**
	 * Set the maximum acceleration rate.
	 * 
	 * @param maxAccelerationRate the maximum acceleration rate in cm/s^2
	 */
	public void setMaxAccelerationRate(int maxAccelerationRate) {
		maxAccelerationRate_ = maxAccelerationRate;
	}
	
	/**
	 * Gets the minimum acceleration rate of the vehicle type.
	 * 
	 * @return the minimum acceleration rate
	 */
	public int getMinAccelerationRate() {
		return minAccelerationRate_;
	}
	
	/**
	 * Set the minimum acceleration rate.
	 * 
	 * @param minAccelerationRate the minimum acceleration rate in cm/s^2
	 */
	public void setMinAccelerationRate(int minAccelerationRate) {
		minAccelerationRate_ = minAccelerationRate;
	}
	
	/**
	 * Gets the wifi status of the vehicle type.
	 * 
	 * @return <code>true</code> if wifi is enabled
	 */
	public boolean isWifi() {
		return wifi_;
	}
	
	/**
	 * Set the wifi status.
	 * 
	 * @param wifi <code>true</code> wifi is enabled
	 */
	public void setWifi(boolean wifi) {
		wifi_ = wifi;
	}
	
	/**
	 * Gets the emergency vehicle status of the vehicle type.
	 * 
	 * @return <code>true</code> if vehicle type is an emergency vehicle.
	 */
	public boolean isEmergencyVehicle() {
		return emergencyVehicle_;
	}	
	
	/**
	 * Set the emergency vehicle status.
	 * 
	 * @param emergencyVehicle <code>true</code> emergencyVehicle status is enabled.
	 */
	public void setEmergencyVehicle(boolean emergencyVehicle) {
		emergencyVehicle_ = emergencyVehicle;
	}

	/**
	 * Gets the vehicle length.
	 * 
	 * @return the vehicle length.
	 */
	public int getVehicleLength() {
		return vehicleLength_;
	}
	
	/**
	 * Set the vehicle length.
	 * 
	 * @param vehicleLength Length of the vehicle.
	 */
	public void setVehicleLength(int vehicleLength) {
		vehicleLength_ = vehicleLength;
	}
	
	public void setMinTimeDistance(int minTimeDistance_) {
		this.minTimeDistance_ = minTimeDistance_;
	}

	public int getMinTimeDistance() {
		return minTimeDistance_;
	}

	public void setMaxTimeDistance(int maxTimeDistance_) {
		this.maxTimeDistance_ = maxTimeDistance_;
	}

	public int getMaxTimeDistance() {
		return maxTimeDistance_;
	}

	public void setMinPoliteness(int minPoliteness_) {
		this.minPoliteness_ = minPoliteness_;
	}

	public int getMinPoliteness() {
		return minPoliteness_;
	}

	public void setMaxPoliteness(int maxPoliteness_) {
		this.maxPoliteness_ = maxPoliteness_;
	}

	public int getMaxPoliteness() {
		return maxPoliteness_;
	}

	/**
	 * Gets the name of the vehicle type if the toString() function is called.
	 * 
	 * @return name of the vehicle
	 */
	public String toString(){
		return (String) name_;
	}

	/**
	 * @return the vehiclesDeviatingMaxSpeed_
	 */
	public int getVehiclesDeviatingMaxSpeed_() {
		return vehiclesDeviatingMaxSpeed_;
	}

	/**
	 * @param vehiclesDeviatingMaxSpeed_ the vehiclesDeviatingMaxSpeed_ to set
	 */
	public void setVehiclesDeviatingMaxSpeed_(int vehiclesDeviatingMaxSpeed_) {
		this.vehiclesDeviatingMaxSpeed_ = vehiclesDeviatingMaxSpeed_;
	}

	/**
	 * @return the deviationFromSpeedLimit_
	 */
	public int getDeviationFromSpeedLimit_() {
		return deviationFromSpeedLimit_;
	}

	/**
	 * @param deviationFromSpeedLimit_ the deviationFromSpeedLimit_ to set
	 */
	public void setDeviationFromSpeedLimit_(int deviationFromSpeedLimit_) {
		this.deviationFromSpeedLimit_ = deviationFromSpeedLimit_;
	}
}
