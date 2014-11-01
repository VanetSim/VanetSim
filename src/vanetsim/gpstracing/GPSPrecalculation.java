package vanetsim.gpstracing;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;

import javax.swing.JFormattedTextField;

import vanetsim.VanetSimStart;
import vanetsim.gui.Renderer;
import vanetsim.map.*;
import vanetsim.routing.WayPoint;

public class GPSPrecalculation {

	//TODO: Create Maps and save them :) No maps there yet, expect for NY (might need a new one though)
	public static void openMap (int simulationMode_){
		
		//Traces Shanghai
		if(simulationMode_ == 3){
			 File ShanghaiFile_ = new File("../Vanetsim/Shanghai_noTS");
			 Map.getInstance().load(ShanghaiFile_, false);
		}
		//Traces San Francisco
		else if(simulationMode_ == 4){
			 File SanFranciscoFile_ = new File("../Vanetsim/SanFrancisco_noTS.xml");
			 Map.getInstance().load(SanFranciscoFile_, false);
		}
		//Traces New York
		else if(simulationMode_ == 5){
			 File NYFile_ = new File("../VanetSim/NewYork_noTS.xml");
			 Map.getInstance().load(NYFile_, false);
		}
		//Traces Hamburg
		else if(simulationMode_ == 6){
			 File HHFile_ = new File("../Vanetsim/HH_noTS");
			 Map.getInstance().load(HHFile_, false);
		}
		
	}
	

	//TODO: Run method according to selected Traces
	public static void runParser(int simulationMode_, int minLine_, int maxLine_){
		//Traces Shanghai
				if(simulationMode_ == 3){
					GPSTracesShanghai.getInstance().getShanghaiTraces(minLine_, maxLine_);
				}
				//Traces San Francisco
				else if(simulationMode_ == 4){
					GPSTracesSanFrancisco.getInstance().getSanFranciscoTraces(minLine_, maxLine_);
				}
				//Traces New York
				else if(simulationMode_ == 5){
					GPSTracesNY.getInstance().getNYTraces(minLine_, maxLine_);
					
				}
				//Traces Hamburg
				else if(simulationMode_ == 6){
					GPSTracesXML.getInstance().getGpxTraces();
				}
	}
	
	
	//TODO: Calculate Route
	public static void precalculateRoute(int simulationMode_, int minLine_, int maxLine_){
		

		//TODO: Precalculate relative Times
		
		//TODO: Create Waypoints
		ArrayDeque<WayPoint> destinations = null;
		
		
		//TODO: Hier muss noch die Fallunterscheidung hin...
		if(simulationMode_ == 5){ //NY
		ArrayList<String> parsedTraces_ = GPSTracesNY.getInstance().getNYTraces(minLine_, maxLine_); 
		System.out.println(parsedTraces_.get(0));
		System.out.println(parsedTraces_.size());
		
		
			for(int i = 0; i < parsedTraces_.size(); i=i+7){ //TODO: Create vehicle				
				
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
				
				}
		
			}

		else if(simulationMode_ == 4){ //San Francisco
			ArrayList<String> parsedTraces_ = GPSTracesSanFrancisco.getInstance().getSanFranciscoTraces(minLine_, maxLine_);
		
		
			for (int i = 0; i<= parsedTraces_.size(); i=i+4){
				System.out.println(i);
				if (i==0){
					//Create first vehicle
					destinations = new ArrayDeque<WayPoint>();	
					double x_ = (double)Double.parseDouble(parsedTraces_.get(i+1));
					double y_ = (double)Double.parseDouble(parsedTraces_.get(i+2));
					
					//Time - here the time is from the LAST point to the first one means
					//Vehicles have to drive BACKWARDS :)
					
					//long t_ = (long)(Long.parseLong(parsedTraces_.get(i+4)) - (long)(Long.parseLong(parsedTraces_.get(i+8))));
					//System.out.println(t_);
					System.out.println(x_);
					System.out.println(y_);
					
					//WayPoint tmpWayPoint = new GPXtoMetric(x_,y_,t_);
					//	destinations.add(tmpWayPoint);
					System.out.println(i);
					continue;
				}
				System.out.println(i);
				System.out.println(parsedTraces_.get(i));
				System.out.println(parsedTraces_.get(i+4));
				if(parsedTraces_.get(i)==parsedTraces_.get(i-4)){
					//Put shit in Metricstuff
					System.out.println("Schleife " + i);
					
					double x_ = (double)Double.parseDouble(parsedTraces_.get(i+1));
					double y_ = (double)Double.parseDouble(parsedTraces_.get(i+2));
					
					//Time - here the time is from the LAST point to the first one means
					//Vehicles have to drive BACKWARDS :)
					
					//long t_ = (long)(Long.parseLong(parsedTraces_.get(i+4)) - (long)(Long.parseLong(parsedTraces_.get(i+8))));
					//System.out.println(t_);
					System.out.println(x_);
					System.out.println(y_);
					
					//WayPoint tmpWayPoint = new GPXtoMetric(x_,y_,t_);
					//	destinations.add(tmpWayPoint);
					
				}
				else{
					System.out.println("Hier bin ich im else " + i);
					//get new vehicle
				}
				
				
			}
		
		
		
		
		
		
		
		
		
		
		
		
		
		}
		else if(simulationMode_ == 6){ //HH - OSM
			ArrayList<String> parsedTraces_ = GPSTracesXML.getInstance().getGpxTraces();
		}
		else if(simulationMode_ == 3){ //Shanghai
			ArrayList<String> parsedTraces_ = GPSTracesShanghai.getInstance().getShanghaiTraces(minLine_, maxLine_);
			
			//Filter for ID
			ArrayList<String> IDs = new ArrayList<String>();
			int j = 0;
			IDs.add(0, parsedTraces_.get(0));

			for (int i = 0; i<= parsedTraces_.size(); i=i+4){
				
				for (j=1; j <= IDs.size(); j++){
					if (IDs.get(j) == parsedTraces_.get(i)){					
					}
					else {
						IDs.add(j, parsedTraces_.get(i));
					}
				}

			
			}
			
		}
		
				
		
		
		//TODO: Initialize Vehicles
		
		//TODO: Update GUI
		//Renderer.getInstance().setShowVehicles(true);
		//VanetSimStart.setProgressBar(true);
		
		
	}


}
