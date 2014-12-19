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
package vanetsim.gpstracing;

import vanetsim.map.Map;
import vanetsim.scenario.Vehicle;

public class GPSVehicleMaster {

	/** Instance for the Singleton usage */
	private static GPSVehicleMaster instance_;

	/** A SortedVehicleQueue for storing the GPS vehicles
	 * in order by their StartTime */
	private SortedVehicleQueue vehicles_;
	
	/** A boolean flag to prevent inserting Vehicles into
	 * the SortedVehcile after the simulation was started */
	private boolean simulationIsRunning_ = false;
	
	/**
	 * The minimum StartTime known at start the simulation */
	private long minimumAtStart_;

	
	/**
	 * Constructor of the GPSVehicleMaster
	 * Set to private for Singleton usage 
	 */
	private GPSVehicleMaster() {
		vehicles_ = new SortedVehicleQueue();
	}

	/**
	 * static method for getting an GPSVehicleMaster instance
	 * GPSVehicleMaster is designed as a Singleton class
	 * @return GPSVehicleMaster
	 */
	public static GPSVehicleMaster getInstance() {

		if (instance_ == null) {
			instance_ = new GPSVehicleMaster();
		}

		return instance_;
	}

	
	/**
	 * Adding a vehicle to the SortedVehcileQueue
	 * The vehicle will only be added if simulation hasn't
	 * started yet
	 * 
	 * @param vehicle
	 * @param startTime
	 */
	public void addVehicle(Vehicle vehicle, long startTime) {
		if (!simulationIsRunning_) {
			vehicles_.add(vehicle, startTime);
		}
	}

	
	/**
	 * Method for preparing the VehicleMaster for simulation
	 * There for the SortedVehicleQueue must be filled
	 * The StartTime of the first element of the SortedVehicleQueue
	 * will become the minimumStartTime
	 */
	public void startSim() throws NoVehicleFoundException{
		simulationIsRunning_ = true;
		if(vehicles_.peek() == null){
			throw new NoVehicleFoundException();
		}
		minimumAtStart_ = vehicles_.peek().getStartTime();
		if (!vehicles_.isEmpty()) {
			Map.getInstance().addVehicle(vehicles_.remove().getVehicle());
		}
	}

	/**
	 * This function is called from an outside class and tranfers
	 * the actual simulationTime to the GPSVehicleMaster
	 * If minimumStartTime - StartTime of an vehicle is smaller than
	 * the received simulationTime, the vehicle will be added to the simulation
	 * @param simTime
	 */
	public void receiveSimulationTime(long simTime) {
		while (vehicles_.peek().getStartTime()-minimumAtStart_ < simTime
				&& !vehicles_.isEmpty()) {
			Map.getInstance().addVehicle(vehicles_.remove().getVehicle());
		}
	}

}
