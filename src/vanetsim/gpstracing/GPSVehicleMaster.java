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

	private static GPSVehicleMaster instance_;

	private SortedVehicleQueue vehicles_;
	private boolean simulationIsRunning_ = false;
	private long minimumAtStart_;

	/**
	 * 
	 */
	private GPSVehicleMaster() {
		vehicles_ = new SortedVehicleQueue();
	}

	/**
	 * 
	 * @return
	 */
	public static GPSVehicleMaster getInstance() {

		if (instance_ == null) {
			instance_ = new GPSVehicleMaster();
		}

		return instance_;
	}

	public void addVehicle(Vehicle vehicle, long startTime) {
		if (!simulationIsRunning_) {
			vehicles_.add(vehicle, startTime);
		}
	}

	/**
	 * 
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
	 * 
	 * @param simTime
	 */
	public void receiveSimulationTime(long simTime) {
		while (vehicles_.peek().getStartTime()-minimumAtStart_ < simTime
				&& !vehicles_.isEmpty()) {
			Map.getInstance().addVehicle(vehicles_.remove().getVehicle());
		}
	}

}
