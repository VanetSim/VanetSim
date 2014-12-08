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
			// File HHFile_ = new File("../Vanetsim/Hamburg_kl.xml"); 
			// Map.getInstance().load(HHFile_, false);
			
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
			
			ArrayList<String> parsedTraces_ = GPSTracesShanghai.getInstance().getShanghaiTraces(minLine, maxLine);
			Vehicle tmpVehicle;
			//Filter for ID						
			List<String> IDs= new ArrayList<String>();
			for (int i = 0; i < parsedTraces_.size(); i=i+4){
				if(!ArrayContainsElem(parsedTraces_.get(i), IDs)) IDs.add(parsedTraces_.get(i));
			}
			long startTime = 0;
			for (String s : IDs){
				HashMap<Long, WayPoint> wayPointMap = new HashMap<Long, WayPoint>();
				destinations = new ArrayDeque<WayPoint>();
				ArrayDeque<Long> tripTimes; //(parsedTraces_.size()/4)
				for (int j = 0; j < parsedTraces_.size(); j=j+4){
					if (s.equals(parsedTraces_.get(j))){
						
						double x = (double)Double.parseDouble(parsedTraces_.get(j+1));
						double y = (double)Double.parseDouble(parsedTraces_.get(j+2));
						
						int[] Coordinates = new int[2];
						MapHelper.translateGPSToMapMetric(Coordinates, x,y);
						if (!(MapHelper.translateGPSToMapMetric(Coordinates, x,y))){
							continue;
						}
						WayPoint tmpWayPoint;
						try {
							tmpWayPoint = new WayPoint(Coordinates[0],Coordinates[1], 0);
							
							//destinations.add(tmpWayPoint);
							Date t1;
								if(j+7 < parsedTraces_.size()){//&& j!=0
									t1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(parsedTraces_.get(j+3));	
									long time = t1.getTime();
									//tripTimes.add(time);
									wayPointMap.put(time, tmpWayPoint);
								}
							} catch (ParseException e) {
								System.out.println("Mapping Waypoint failed");
								//e.printStackTrace();
							} catch (IllegalArgumentException e1) {
								System.out.println("Parsing Times in Shanghai Traces failed");
								//e1.printStackTrace();
							}		
					}
					
				}
				try {
					if(wayPointMap.size() >= 2){
						ArrayList<Long> tripTimesAsArray = new ArrayList<Long>(wayPointMap.keySet());
						Collections.sort(tripTimesAsArray);
						tripTimes = new ArrayDeque<Long>(tripTimesAsArray);
						startTime = tripTimes.peekFirst();
						for(int i = 0; i < tripTimesAsArray.size(); i++){
							destinations.add(wayPointMap.get(tripTimesAsArray.get(i)));
						}
						tmpVehicle =  new Vehicle(destinations, tripTimes, 1, 1, 1, false, false, 1, 1, 1, 1, 1, new Color(0,255,0), false, "");		
						if (RealTimeCalc_ == true){
						GPSVehicleMaster.getInstance().addVehicle(tmpVehicle, startTime);
						}
						else{
							Map.getInstance().addVehicle(tmpVehicle);
						}
					}
				} catch (ParseException e) {
					//e.printStackTrace();
					System.out.println("Vehicle creation failed");
				}	
			}
		}
		
		// Precalculation for the San Francisco Szenario
		else if(simulationMode == 4){ 
			
			ArrayList<String> parsedTraces_ = GPSTracesSanFrancisco.getInstance().getSanFranciscoTraces(minLine, maxLine);
		
			Vehicle tmpVehicle;
			destinations = new ArrayDeque<WayPoint>();	
			ArrayDeque<Long> tripTimes = new ArrayDeque<Long>(); //(parsedTraces_.size()/4)
			
			for (int i = 0; i< parsedTraces_.size(); i=i+4){				
				if (i==0){	
					double x = (double)Double.parseDouble(parsedTraces_.get(i+1));
					double y = (double)Double.parseDouble(parsedTraces_.get(i+2));
					
					int[] Coordinates = new int[2];
					MapHelper.translateGPSToMapMetric(Coordinates, x,y);
					if (MapHelper.translateGPSToMapMetric(Coordinates, x,y) == false){
						continue;
					}
					WayPoint tmpWayPoint;
					try {
						tmpWayPoint = new WayPoint(Coordinates[0],Coordinates[1], 0);
						destinations.add(tmpWayPoint);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}

					Date t1;
					try {		
						t1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(parsedTraces_.get(i+3)); 
						long time = t1.getTime();
						//Time in ms
						//long timeDif = t2.getTime() - t1.getTime();
						tripTimes.add(time);
					} catch (ParseException e1) {
						System.out.println("Parsing Times in SF Traces Failed");
						//e1.printStackTrace();
					}
					continue;
				}
				else if(parsedTraces_.get(i).equals(parsedTraces_.get(i-4))){
					
					double x = (double)Double.parseDouble(parsedTraces_.get(i+1));
					double y = (double)Double.parseDouble(parsedTraces_.get(i+2));

					int[] Coordinates = new int[2];
					MapHelper.translateGPSToMapMetric(Coordinates, x,y);
				
					if (MapHelper.translateGPSToMapMetric(Coordinates, x,y) == false){
						continue;
					}
					WayPoint tmpWayPoint;
					try {
						tmpWayPoint = new WayPoint(Coordinates[0],Coordinates[1], 0);
						destinations.add(tmpWayPoint);
					} catch (ParseException e) {
						//e.printStackTrace();
						continue;
					}		
					Date t1;
					try {
						if(i+7 < parsedTraces_.size()){
						t1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(parsedTraces_.get(i+3)); 
						long time = t1.getTime();
						tripTimes.add(time);
						}
						
					} catch (ParseException e1) {
						System.out.println("Parsing Times in SF Traces Failed");
						//e1.printStackTrace();
					}
					continue;
				}
				else if(!parsedTraces_.get(i).equals(parsedTraces_.get(i-4))){
					try {
						if(destinations.size() >= 2){
							tmpVehicle =  new Vehicle(destinations, tripTimes, 1, 1, 1, false, false, 1, 1, 1, 1, 1, new Color(0,255,0), false, "");
							
							long startTime = tripTimes.getFirst(); //TODO: Get Last ?
							if (RealTimeCalc_ == true){
								GPSVehicleMaster.getInstance().addVehicle(tmpVehicle, startTime);
								}
								else{
									Map.getInstance().addVehicle(tmpVehicle);
								}
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
					
					//empty destinations
					destinations.clear();
					tripTimes.clear();
					
					double x = (double)Double.parseDouble(parsedTraces_.get(i+1));
					double y = (double)Double.parseDouble(parsedTraces_.get(i+2));

					int[] Coordinates = new int[2];
					MapHelper.translateGPSToMapMetric(Coordinates, x,y);
					if (MapHelper.translateGPSToMapMetric(Coordinates, x,y) == false){
						continue;
					}
					WayPoint tmpWayPoint;
					try {
						tmpWayPoint = new WayPoint(Coordinates[0],Coordinates[1], 0);
						destinations.add(tmpWayPoint);

					} catch (ParseException e) {			
						e.printStackTrace();
					}
					
					Date t1;
					try {
						if(i+7 < parsedTraces_.size()){
						t1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(parsedTraces_.get(i+3)); 
						long time = t1.getTime();
						tripTimes.add(time);
						}
						
					} catch (ParseException e1) {
						System.out.println("Parsing Times in SF Traces Failed");
						e1.printStackTrace();
					}
				}
			}
			try {
				if(destinations.size() >= 2){
					tmpVehicle = new Vehicle(destinations, tripTimes, 1, 1, 1, false, false, 1, 1, 1, 1, 1, new Color(0,255,0), false, "");
					long startTime = tripTimes.getFirst(); //TODO: Get Last ?
					if (RealTimeCalc_ == true){
						GPSVehicleMaster.getInstance().addVehicle(tmpVehicle, startTime);
						}
						else{
							Map.getInstance().addVehicle(tmpVehicle);
						}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		//Precalculation for the New York Szenario
		else if(simulationMode == 5){

				ArrayList<String> parsedTraces_ = GPSTracesNY.getInstance().getNYTraces(minLine, maxLine);
				Vehicle tmpVehicle;
				
					for(int i = 0; i < parsedTraces_.size(); i=i+7){
						
						ArrayDeque<Long> tripTimes = new ArrayDeque<Long>();		//(parsedTraces_.size()/7)
						destinations = new ArrayDeque<WayPoint>(2);		
						
							try{
				
								double pickupX_ = (double)Double.parseDouble(parsedTraces_.get(i+1));
								double pickupY_ = (double)Double.parseDouble(parsedTraces_.get(i+2));		

								//Add first way points here
								int[] CoordinatesPickup = new int[2];
								
								if (MapHelper.translateGPSToMapMetric(CoordinatesPickup, pickupX_, pickupY_) == false){
									destinations.clear();
									continue;
								}
													
								WayPoint tmpWayPoint = new WayPoint(CoordinatesPickup[0],CoordinatesPickup[1], 0);
									destinations.add(tmpWayPoint);
									Date t3;
									//System.out.println("Time 1: " + parsedTraces_.get(i+3));
									t3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(parsedTraces_.get(i+3));
									long time = t3.getTime();
									//long time = (long)Long.parseLong(parsedTraces_.get(i+3));
									//TripTime is in Seconds
									tripTimes.add(time);
									

								double dropoffX_ = (double)Double.parseDouble(parsedTraces_.get(i+6));
								double dropoffY_ = (double)Double.parseDouble(parsedTraces_.get(i+7));							
								//Add second way points here
								int[] CoordinatesDropoff = new int[2];
								
								if (MapHelper.translateGPSToMapMetric(CoordinatesDropoff, dropoffX_,dropoffY_) == false){
									destinations.clear();
									continue;
								}
								WayPoint tmpWayPoint_2 = new WayPoint(CoordinatesDropoff[0],CoordinatesDropoff[1], 0);
									destinations.add(tmpWayPoint_2);
									
									//long time2 = (long)Long.parseLong(parsedTraces_.get(i+4));
									//TripTime is in Seconds
									//System.out.println("Time 2: " + parsedTraces_.get(i+4));
									Date t2;
									t2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(parsedTraces_.get(i+4));
									long time2 = t2.getTime();
									tripTimes.add(time2);
								
							} catch (Exception e) {
								System.out.println("Routing failed");
							}
							//Create vehicle
							try {
								if(!(destinations.size() <2)){							
									//Time in ms
									//Times can be added here, as there is only one trip time per vehicle, as New York Taxis always only have 2 pOints

									//Date t1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(parsedTraces_.get(i+3)); 
									long startTime = tripTimes.peekFirst();
									
									tmpVehicle = new Vehicle(destinations, tripTimes, 1, 1, 1, false, false, 1, 1, 1, 1, 1, new Color(0,255,0), false, "");
									if (RealTimeCalc_ == true){
										GPSVehicleMaster.getInstance().addVehicle(tmpVehicle, startTime);
										}
										else{
											Map.getInstance().addVehicle(tmpVehicle);
										}

								}
							} catch (ParseException e) {
								System.out.println("Vehicle creation failed");
								e.printStackTrace();
							}
							
							
						}
				
					}
		
		// Precalculation for the general GPX-Files Szenario
		else if(simulationMode == 6){ 
			
			ArrayList<String> parsedTraces_ = GPSTracesXML.getInstance().getGpxTraces();
			
			Vehicle tmpVehicle;

			//Filter for ID
			List<String> IDs= new ArrayList<String>();
			for (int i = 0; i < parsedTraces_.size(); i=i+4){
				if(!ArrayContainsElem(parsedTraces_.get(i), IDs)) IDs.add(parsedTraces_.get(i));
			}
			long startTime = 0;
			for (String s : IDs){
				destinations = new ArrayDeque<WayPoint>();
				ArrayDeque<Long> tripTimes = new ArrayDeque<Long>(); //(parsedTraces_.size()/4)
				for (int j = 0; j < parsedTraces_.size(); j=j+4){
					if (s.equals(parsedTraces_.get(j))){
						
						double x = (double)Double.parseDouble(parsedTraces_.get(j+1));
						double y = (double)Double.parseDouble(parsedTraces_.get(j+2));
											
						int[] Coordinates = new int[2];
						MapHelper.translateGPSToMapMetric(Coordinates, x,y);
						if (!(MapHelper.translateGPSToMapMetric(Coordinates, x,y))){
							continue;
						}
						WayPoint tmpWayPoint;
						
						try {
							tmpWayPoint = new WayPoint(Coordinates[0],Coordinates[1], 0);
							Map.getInstance().addRSU(new RSU(Coordinates[0],Coordinates[1], 500, false));
							destinations.add(tmpWayPoint);
	
							Date t1;
		
							if(j+7 < parsedTraces_.size() && j != 0){
								t1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(parsedTraces_.get(j+3)); 
								long time = t1.getTime();
								tripTimes.add(time);
							}
						} catch (ParseException e) {
							System.out.println("Mapping Waypoint failed");
							//e.printStackTrace();
						} catch (IllegalArgumentException e1) {
							System.out.println("Parsing Times in GPX Traces failed");
							//e1.printStackTrace();
						}		
					}
				}//TODO:
				//
				try {
					if(destinations.size() >= 2){
						startTime = tripTimes.peekFirst();
						tmpVehicle =  new Vehicle(destinations, tripTimes, 1, 1, 1, false, false, 1, 1, 1, 1, 1, new Color(0,255,0), false, "");		
						if (RealTimeCalc_ == true){
						GPSVehicleMaster.getInstance().addVehicle(tmpVehicle, startTime);
						}
						else{
							Map.getInstance().addVehicle(tmpVehicle);
						}
					}
				} catch (ParseException e) {
					e.printStackTrace();
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
		System.out.println("Done :)");
		VanetSimStart.setProgressBar(false);
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
