package vanetsim.gpstracing;

import java.awt.Color;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JFormattedTextField;

import vanetsim.VanetSimStart;
import vanetsim.gui.Renderer;
import vanetsim.map.*;
import vanetsim.routing.WayPoint;
import vanetsim.scenario.Vehicle;

public class GPSPrecalculation {
	//TODO: MAPS
	public static void openMap (int simulationMode){
		
		//Traces Shanghai
		if(simulationMode == 3){
			 File ShanghaiFile_ = new File("../Vanetsim/Shanghai_noTS");
			 Map.getInstance().load(ShanghaiFile_, false);
		}
		//Traces San Francisco
		else if(simulationMode == 4){
			 File SanFranciscoFile_ = new File("../Vanetsim/SanFrancisco_noTS.xml");
			 Map.getInstance().load(SanFranciscoFile_, false);
		}
		//Traces New York
		else if(simulationMode == 5){
			 File NYFile_ = new File("../VanetSim/NewYork_noTS.xml");
			 Map.getInstance().load(NYFile_, false);
		}
		//Traces Hamburg
		else if(simulationMode == 6){
			 File HHFile_ = new File("../Vanetsim/HH_noTS");
			 Map.getInstance().load(HHFile_, false);
		}
		
	}
	
	public static void runParser(int simulationMode, int minLine, int maxLine){
		//Traces Shanghai
				if(simulationMode == 3){
					GPSTracesShanghai.getInstance().getShanghaiTraces(minLine, maxLine);
				}
				//Traces San Francisco
				else if(simulationMode == 4){
					GPSTracesSanFrancisco.getInstance().getSanFranciscoTraces(minLine, maxLine);
				}
				//Traces New York
				else if(simulationMode == 5){
					GPSTracesNY.getInstance().getNYTraces(minLine, maxLine);
					
				}
				//Traces Hamburg
				else if(simulationMode == 6){
					GPSTracesXML.getInstance().getGpxTraces();
				}
	}
	
	
	//TODO: Calculate Route
	public static void precalculateRoute(int simulationMode, int minLine, int maxLine){


		//TODO: Precalculate relative Times
		
		
		ArrayDeque<WayPoint> destinations = null;
		
		if(simulationMode == 5){ //NY
		ArrayList<String> parsedTraces_ = GPSTracesNY.getInstance().getNYTraces(minLine, maxLine); 

		Vehicle tmpVehicle;
		
			for(int i = 0; i < parsedTraces_.size(); i=i+7){				
				
				destinations = new ArrayDeque<WayPoint>(2);			
					
					try{
						
						//Waypoints into int
						//i=0 - ID
						System.out.println(parsedTraces_.get(i));

						//i=1 - Lon
						
						double pickupX_ = (double)Double.parseDouble(parsedTraces_.get(i+1));
						System.out.println("X" + pickupX_);
						//i=2 - Lat
						double pickupY_ = (double)Double.parseDouble(parsedTraces_.get(i+2));
						
						System.out.println("Y" + pickupY_);
						//i=3 - Time
						int time_ = 0;
						System.out.println(i+3);
						
						//i=4 - Triptime
						System.out.println(i+4);
						
						//Add first way points here
					//	WayPoint tmpWayPoint = new GPXtoMetric(pickupX_,pickupY_,time_);//TODO:Die 0 ist hier ein Platzhalter für die relative Zeit !
					//		destinations.add(tmpWayPoint);

						//i=5 - Lon
						double dropoffX_ = (double)Double.parseDouble(parsedTraces_.get(i+5));
						System.out.println(i+5);
							
						//i=6 - Lat
						double dropoffy_ = (double)Double.parseDouble(parsedTraces_.get(i+6));							
						System.out.println(i+6);
						//Add second way points here
						//WayPoint tmpWayPoint_2 = new GPXtoMetric(dropoffX_,dropoffy_,time_);//TODO:Die 0 ist hier ein Platzhalter für die relative Zeit !
						//	destinations.add(tmpWayPoint_2);
						
					
					} catch (Exception e) {
						System.out.println("Routing failed");
					}
					//Create vehicle
					try {
						tmpVehicle = new Vehicle(destinations, 0, 0, 0, false, false, 0, 0, 0, 0, 0, new Color(0,255,0), false, "");
						Map.getInstance().addVehicle(tmpVehicle);
					} catch (ParseException e) {
						
						e.printStackTrace();
					}
					
				}
		
			}

		else if(simulationMode == 4){ //San Francisco
			ArrayList<String> parsedTraces_ = GPSTracesSanFrancisco.getInstance().getSanFranciscoTraces(minLine, maxLine);
		
			Vehicle tmpVehicle;
			
			destinations = new ArrayDeque<WayPoint>();	
			for (int i = 0; i<= parsedTraces_.size(); i=i+4){
				
				if (i==0){
					
					
					double x_ = (double)Double.parseDouble(parsedTraces_.get(i+1));
					double y_ = (double)Double.parseDouble(parsedTraces_.get(i+2));
					
					//Time - here the time is from the LAST point to the first one means
					//Vehicles have to drive BACKWARDS :)
					
					//long t_ = (long)(Long.parseLong(parsedTraces_.get(i+4)) - (long)(Long.parseLong(parsedTraces_.get(i+8))));
					//System.out.println(t_);
					
					
					//WayPoint tmpWayPoint = new GPXtoMetric(x_,y_,t_);
					//	destinations.add(tmpWayPoint);
					
					continue;
				}
				else if(parsedTraces_.get(i)==parsedTraces_.get(i-4)){
					//Put shit in Metricstuff
					
					
					double x_ = (double)Double.parseDouble(parsedTraces_.get(i+1));
					double y_ = (double)Double.parseDouble(parsedTraces_.get(i+2));
					
					//Time - here the time is from the LAST point to the first one means
					//Vehicles have to drive BACKWARDS :)
					
					//long t_ = (long)(Long.parseLong(parsedTraces_.get(i+4)) - (long)(Long.parseLong(parsedTraces_.get(i+8))));
					//System.out.println(t_);
					continue;
				}
				else if(parsedTraces_.get(i)!=parsedTraces_.get(i-4)){
					//Initialize new vehicle
					try {
						tmpVehicle = new Vehicle(destinations, 0, 0, 0, false, false, 0, 0, 0, 0, 0, new Color(0,255,0), false, "");
						Map.getInstance().addVehicle(tmpVehicle);
					} catch (ParseException e) {
						
						e.printStackTrace();
					}
					
					
					//empty destinations
					destinations.clear();
					
					double x_ = (double)Double.parseDouble(parsedTraces_.get(i+1));
					double y_ = (double)Double.parseDouble(parsedTraces_.get(i+2));
					
					//WayPoint tmpWayPoint = new GPXtoMetric(x_,y_,t_);
					//	destinations.add(tmpWayPoint);
					
				}
			}
		}
		else if(simulationMode == 6){ //HH - OSM
			ArrayList<String> parsedTraces_ = GPSTracesXML.getInstance().getGpxTraces();
			
			
			
			
			
		}
		else if(simulationMode == 3){ //Shanghai
			ArrayList<String> parsedTraces_ = GPSTracesShanghai.getInstance().getShanghaiTraces(minLine, maxLine);
			Vehicle tmpVehicle;
			destinations = new ArrayDeque<WayPoint>();	
			//Filter for ID
			HashSet<String> IDs = new HashSet<String>();
			for (int i = 0; i < parsedTraces_.size(); i=i+4){
					IDs.add(parsedTraces_.get(i));
			}
			for (String s : IDs){
				for (int j = 0; j < parsedTraces_.size(); j=j+4){
					if (s.equals(parsedTraces_.get(j))){
						
						double x_ = (double)Double.parseDouble(parsedTraces_.get(j+1));
						double y_ = (double)Double.parseDouble(parsedTraces_.get(j+2));
						
						//WayPoint tmpWayPoint = new GPXtoMetric(x_,y_,t_);
						//	destinations.add(tmpWayPoint);	
					}
					else {
						try {
							tmpVehicle = new Vehicle(destinations, 0, 0, 0, false, false, 0, 0, 0, 0, 0, new Color(0,255,0), false, "");
							Map.getInstance().addVehicle(tmpVehicle);
						} catch (ParseException e) {
							
							e.printStackTrace();
						}
						
						//empty destinations
						destinations.clear();
						
						double x_ = (double)Double.parseDouble(parsedTraces_.get(j+1));
						double y_ = (double)Double.parseDouble(parsedTraces_.get(j+2));
						
						//WayPoint tmpWayPoint = new GPXtoMetric(x_,y_,t_);
						//	destinations.add(tmpWayPoint);
						
					}
				}
			}
			
			
			
		}
		
				
		
		
		
		
		//TODO: Update GUI
		//Renderer.getInstance().setShowVehicles(true);
		//VanetSimStart.setProgressBar(true);
		
		
	}


}
