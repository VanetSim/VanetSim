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

import java.awt.Color;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;

import vanetsim.VanetSimStart;
import vanetsim.gui.Renderer;
import vanetsim.map.*;
import vanetsim.routing.WayPoint;
import vanetsim.scenario.RSU;
import vanetsim.scenario.Vehicle;
import vanetsim.simulation.SimulationMaster;

/**
 * 
 * Class does the complete precalculation for the GPS Simulation
 * - Loading Maps - predefined for three Szenarios
 * - Calculate Trip Times
 * - Add Vehicles to either 1) Map or 2) Vehilce Master
 *
 */
public class GPSPrecalculation {
	
	/** Real Time Calculation enabled/disabled */
	private static boolean RealTimeCalc_ = false;
	
	//Open Map for defined Szenarios
	public static void openMap (int simulationMode){
		
		//Traces Shanghai - Map predefined
		if(simulationMode == 3){
			 File ShanghaiFile_ = new File("../Vanetsim/Shanghai_kl.xml");
			 Map.getInstance().load(ShanghaiFile_, false);
		}
		//Traces San Francisco - Map predefined
		else if(simulationMode == 4){
			 File SanFranciscoFile_ = new File("../Vanetsim/SanFrancisco_kl.xml");
			 Map.getInstance().load(SanFranciscoFile_, false);
		}
		//Traces New York - Map predefined
		else if(simulationMode == 5){
			 File NYFile_ = new File("../VanetSim/NY_kl.xml");
			 Map.getInstance().load(NYFile_, false);
		}
		//GPX Traces - this is the only generic Function for opening Maps
		else if(simulationMode == 6){
			//open Map Dialog
			VanetSimStart.getMainControlPanel().changeFileChooser(true, true, false);
			int returnVal = VanetSimStart.getMainControlPanel().getFileChooser().showOpenDialog(VanetSimStart.getMainFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {           
						Map.getInstance().load(VanetSimStart.getMainControlPanel().getFileChooser().getSelectedFile(), false);
			}
		}
	}
		
	// Precalculation for the GPS Szenarios
	public static void precalculateRoute(int simulationMode, int minLine, int maxLine){		
		ArrayDeque<WayPoint> destinations = null;
		
		// Precalculation for the Shanghai Szenario
		if(simulationMode == 3){ 
			//Parse Input File
			ArrayList<String> parsedTraces_ = GPSTracesShanghai.getInstance().getShanghaiTraces(minLine, maxLine);
			Vehicle tmpVehicle;
			//Filter for ID						
			List<String> IDs= new ArrayList<String>();
			for (int i = 0; i < parsedTraces_.size(); i=i+4){
				if(!ArrayContainsElem(parsedTraces_.get(i), IDs)) IDs.add(parsedTraces_.get(i));
			}
			long startTime = 0;
			//Go over all IDs
			for (String s : IDs){
				HashMap<Long, WayPoint> wayPointMap = new HashMap<Long, WayPoint>();
				destinations = new ArrayDeque<WayPoint>();
				ArrayDeque<Long> tripTimes; 
				//Go over Array List
				for (int j = 0; j < parsedTraces_.size(); j=j+4){
					if (s.equals(parsedTraces_.get(j))){
						
						//Get Coordinates
						double x = (double)Double.parseDouble(parsedTraces_.get(j+1));
						double y = (double)Double.parseDouble(parsedTraces_.get(j+2));
						
						//Parse Coordinates to MapMetric
						int[] Coordinates = new int[2];
						MapHelper.translateGPSToMapMetric(Coordinates, x,y);
						if (!(MapHelper.translateGPSToMapMetric(Coordinates, x,y))){
							continue;
						}
						WayPoint tmpWayPoint;
						try {
							//Add Waypoint
							tmpWayPoint = new WayPoint(Coordinates[0],Coordinates[1], 0);
							
							Date t1;
								//Get time corresponding to waypoint
								if(j+7 < parsedTraces_.size()){
									t1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(parsedTraces_.get(j+3));	
									long time = t1.getTime();
									wayPointMap.put(time, tmpWayPoint);
								}
							} catch (ParseException e) {
								//e.printStackTrace();
							} catch (IllegalArgumentException e1) {
								//e1.printStackTrace();
							}		
					}
					
				}
				try {
					//Check that Waypoints were selected
					if(wayPointMap.size() >= 2){
						//Sort List 
						ArrayList<Long> tripTimesAsArray = new ArrayList<Long>(wayPointMap.keySet());
						Collections.sort(tripTimesAsArray);
						tripTimes = new ArrayDeque<Long>(tripTimesAsArray);
						//Get start time for vehicle
						startTime = tripTimes.peekFirst();
						//Add Waypoints to destinations
						for(int i = 0; i < tripTimesAsArray.size(); i++){
							destinations.add(wayPointMap.get(tripTimesAsArray.get(i)));
						}
						tmpVehicle =  new Vehicle(destinations, tripTimes, 1, 1, 1, false, false, 1, 1, 1, 1, 1, new Color(0,255,0), false, "");		
						//Check if real time scenario or all vehicles start at once
						if (RealTimeCalc_ == true){
						GPSVehicleMaster.getInstance().addVehicle(tmpVehicle, startTime);
						}
						else{
							Map.getInstance().addVehicle(tmpVehicle);
						}
					}
				} catch (ParseException e) {
					//e.printStackTrace();
				}	
			}
		}
		
		// Precalculation for the San Francisco Szenario
		else if(simulationMode == 4){ 
			
			//Parse Input File
			ArrayList<String> parsedTraces_ = GPSTracesSanFrancisco.getInstance().getSanFranciscoTraces(minLine, maxLine);
			
			Vehicle tmpVehicle;

			//Filter for ID
			List<String> IDs= new ArrayList<String>();
			//Go over Array List
			for (int i = 0; i < parsedTraces_.size(); i=i+4){
				if(!ArrayContainsElem(parsedTraces_.get(i), IDs)) IDs.add(parsedTraces_.get(i));
			}
			long startTime = 0;
			//Go over all IDs
			for (String s : IDs){
				HashMap<Long, WayPoint> wayPointMap = new HashMap<Long, WayPoint>();
				destinations = new ArrayDeque<WayPoint>();
				ArrayDeque<Long> tripTimes;
				for (int j = 0; j < parsedTraces_.size(); j=j+4){
					if (s.equals(parsedTraces_.get(j))){
						//Get Coordinates
						double x = (double)Double.parseDouble(parsedTraces_.get(j+1));
						double y = (double)Double.parseDouble(parsedTraces_.get(j+2));
						
						//Parse Coordinates to MapMetric					
						int[] Coordinates = new int[2];
						MapHelper.translateGPSToMapMetric(Coordinates, x,y);
						if (!(MapHelper.translateGPSToMapMetric(Coordinates, x,y))){
							continue;
						}
						WayPoint tmpWayPoint;
						//Add Waypoint
						try {
							tmpWayPoint = new WayPoint(Coordinates[0],Coordinates[1], 0);
							//Map.getInstance().addRSU(new RSU(Coordinates[0],Coordinates[1], 500, false));
							//Get time corresponding to waypoint
							Date t1;
							t1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssz").parse(parsedTraces_.get(j+3)); 
							
							long time = t1.getTime();
							wayPointMap.put(time, tmpWayPoint);
							//tripTimes.add(time);
							
						} catch (ParseException e) {
							//e.printStackTrace();
							continue;
						} catch (IllegalArgumentException e1) {
							//e1.printStackTrace();
							continue;
						}		
					}
				}
				//
				try {
					//Check that Waypoints were selected
					if(wayPointMap.size() >= 2){

						ArrayList<Long> tripTimesAsArray = new ArrayList<Long>(wayPointMap.keySet());
						Collections.sort(tripTimesAsArray);
						tripTimes = new ArrayDeque<Long>(tripTimesAsArray);
						//Get start time for vehicle
						startTime = tripTimes.peekFirst();
						
						for(int i = 0; i < tripTimesAsArray.size(); i++){
							destinations.add(wayPointMap.get(tripTimesAsArray.get(i)));
						}
						
						tmpVehicle =  new Vehicle(destinations, tripTimes, 1, 1, 1, false, false, 1, 1, 1, 1, 1, new Color(0,255,0), false, "");	
						//Check if real time scenario or all vehicles start at once
						if (RealTimeCalc_ == true){
						GPSVehicleMaster.getInstance().addVehicle(tmpVehicle, startTime);
						}
						else{
							Map.getInstance().addVehicle(tmpVehicle);
						}
					}
				} catch (ParseException e) {
					//e.printStackTrace();
				}	
			}
		}


		
		//Precalculation for the New York Szenario
		else if(simulationMode == 5){
			
			//Parse Input File
			ArrayList<String> parsedTraces_ = GPSTracesNY.getInstance().getNYTraces(minLine, maxLine);
			Vehicle tmpVehicle;
				//Go over Array List
				for(int i = 0; i < parsedTraces_.size(); i=i+7){
					
					ArrayDeque<Long> tripTimes = new ArrayDeque<Long>();	
					destinations = new ArrayDeque<WayPoint>(2);		
					
					try{
						//Get Coordinates
						double pickupX_ = (double)Double.parseDouble(parsedTraces_.get(i+1));
						double pickupY_ = (double)Double.parseDouble(parsedTraces_.get(i+2));		

						//Parse Coordinates to MapMetric
						int[] CoordinatesPickup = new int[2];
						if (MapHelper.translateGPSToMapMetric(CoordinatesPickup, pickupX_, pickupY_) == false){
							destinations.clear();
							continue;
						}
						//Add Waypoint					
						WayPoint tmpWayPoint = new WayPoint(CoordinatesPickup[0],CoordinatesPickup[1], 0);
						destinations.add(tmpWayPoint);
						//Get time corresponding to waypoint
						Date t3;
						t3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(parsedTraces_.get(i+3));
						long time = t3.getTime();
						tripTimes.add(time);
							
						//Get Coordinates
						double dropoffX_ = (double)Double.parseDouble(parsedTraces_.get(i+6));
						double dropoffY_ = (double)Double.parseDouble(parsedTraces_.get(i+7));	
						
						//Parse Coordinates to MapMetric
						int[] CoordinatesDropoff = new int[2];
						if (MapHelper.translateGPSToMapMetric(CoordinatesDropoff, dropoffX_,dropoffY_) == false){
							destinations.clear();
							continue;
						}
						//Add Waypoint
						WayPoint tmpWayPoint_2 = new WayPoint(CoordinatesDropoff[0],CoordinatesDropoff[1], 0);
						destinations.add(tmpWayPoint_2);
						//Get time corresponding to waypoint
						Date t2;
						t2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(parsedTraces_.get(i+4));
						long time2 = t2.getTime();
						tripTimes.add(time2);
						
					} catch (Exception e) {
						//System.out.println("Routing failed");
					}
					//Create vehicle
					try {
						//Check that Waypoints were selected
						if(!(destinations.size() <2)){				
							//Get start time for vehicle
							long startTime = tripTimes.peekFirst();	
							tmpVehicle = new Vehicle(destinations, tripTimes, 1, 1, 1, false, false, 1, 1, 1, 1, 1, new Color(0,255,0), false, "");
							//Check if real time scenario or all vehicles start at once
							if (RealTimeCalc_ == true){
								GPSVehicleMaster.getInstance().addVehicle(tmpVehicle, startTime);
								}
								else{
									Map.getInstance().addVehicle(tmpVehicle);
								}

						}
					} catch (ParseException e) {
						//e.printStackTrace();
					}
					
					
				}
		
			}
		
		// Precalculation for the general GPX-Files Szenario
		else if(simulationMode == 6){ 
			
			//Parse Input File
			ArrayList<String> parsedTraces_ = GPSTracesXML.getInstance().getGpxTraces();
			
			Vehicle tmpVehicle;
			//Filter for ID
			List<String> IDs= new ArrayList<String>();
			//Go over Array List
			for (int i = 0; i < parsedTraces_.size(); i=i+4){
				if(!ArrayContainsElem(parsedTraces_.get(i), IDs)) IDs.add(parsedTraces_.get(i));
			}
			long startTime = 0;
			//Go over all IDs
			for (String s : IDs){
				destinations = new ArrayDeque<WayPoint>();
				ArrayDeque<Long> tripTimes = new ArrayDeque<Long>();
				for (int j = 0; j < parsedTraces_.size(); j=j+4){
					if (s.equals(parsedTraces_.get(j))){
						//Get Coordinates
						double x = (double)Double.parseDouble(parsedTraces_.get(j+1));
						double y = (double)Double.parseDouble(parsedTraces_.get(j+2));
											
						//Parse Coordinates to MapMetric
						int[] Coordinates = new int[2];
						MapHelper.translateGPSToMapMetric(Coordinates, x,y);
						if (!(MapHelper.translateGPSToMapMetric(Coordinates, x,y))){
							continue;
						}
						WayPoint tmpWayPoint;
						//Add Waypoint
						try {
							tmpWayPoint = new WayPoint(Coordinates[0],Coordinates[1], 0);
							//Map.getInstance().addRSU(new RSU(Coordinates[0],Coordinates[1], 500, false));
							destinations.add(tmpWayPoint);
							
							//Get time corresponding to waypoint
							Date t1;
							if(j+7 < parsedTraces_.size() && j != 0){
								t1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(parsedTraces_.get(j+3)); 
								long time = t1.getTime();
								tripTimes.add(time);
							}
						} catch (ParseException e) {
							//e.printStackTrace();
							continue;
						} catch (IllegalArgumentException e1) {
							//e1.printStackTrace();
							continue;
						}		
					}
				}
				try {
					//Check that Waypoints were selected
					if(destinations.size() >= 2){
						//Get start time for vehicle
						startTime = tripTimes.peekFirst();
						tmpVehicle =  new Vehicle(destinations, tripTimes, 1, 1, 1, false, false, 1, 1, 1, 1, 1, new Color(0,255,0), false, "");		
						//Check if real time scenario or all vehicles start at once
						if (RealTimeCalc_ == true){
						GPSVehicleMaster.getInstance().addVehicle(tmpVehicle, startTime);
						}
						else{
							Map.getInstance().addVehicle(tmpVehicle);
						}
					}
				} catch (ParseException e) {
					//e.printStackTrace();
				}	
			}
		}

		//Update GUI
		Renderer.getInstance().setShowVehicles(true);
		Renderer.getInstance().ReRender(true, true);

		if (RealTimeCalc_ == true){
			SimulationMaster.setGPSSimulationFlag(true);
			try {
				GPSVehicleMaster.getInstance().startSim();
			} catch (NoVehicleFoundException e) {
				System.out.println("No vehicles created");
				e.printStackTrace();
			}
		}
	//	System.out.println("Done :)");
	//	VanetSimStart.setProgressBar(false);
	}
	
	private static boolean ArrayContainsElem(String elem, List<String> array){
		for(String s : array){
			if(elem.equals(s)) return true;
		}
		return false;
	}
	
	public static boolean getRealTimeCalc(){
		return RealTimeCalc_;
	}
	
	public static void setRealTimeCalc(boolean state){
		RealTimeCalc_ = state;
	}
}
