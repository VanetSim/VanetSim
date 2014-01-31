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

import java.util.ArrayList;

public class SimulationSeries {
	private final String name_;
	private ArrayList<VehicleSet> vehicleSetList_ = new ArrayList<VehicleSet>();
	private ArrayList<SimulationProperty> propertyList_ = new ArrayList<SimulationProperty>();
	
	public SimulationSeries(String name){
		name_ = name;
	}

	/**
	 * removes property with specific key
	 */
	public void removeProperty(String key){
		int index = -1;
		for(int i = 0; i < propertyList_.size(); i++){
			if(propertyList_.get(i).getPropertyKey_().equals(key)){
				index = i;
				break;
			}		
		}
		if(index != -1)propertyList_.remove(index);
	}
	
	/**
	 * removes vehicleset with specific key
	 */
	public void removeVehicleSet(String name){
		int index = -1;
		for(int i = 0; i < vehicleSetList_.size(); i++){
			if(vehicleSetList_.get(i).getName_().equals(name)){
				index = i;
				break;
			}		
		}
		if(index != -1)vehicleSetList_.remove(index);
	}
	
	public ArrayList<VehicleSet> getVehicleSetList_() {
		return vehicleSetList_;
	}

	public void setVehicleSetList_(ArrayList<VehicleSet> vehicleSetList_) {
		this.vehicleSetList_ = vehicleSetList_;
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
	
	public String toString(){
		return name_;
	}
}
