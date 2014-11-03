package vanetsim.gpstracing;

import java.awt.Color;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
			 File NYFile_ = new File("../VanetSim/NY_GPS_2.xml");
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
			System.out.println("In der 5");
		ArrayList<String> parsedTraces_ = GPSTracesNY.getInstance().getNYTraces(minLine, maxLine);
		System.out.println(parsedTraces_.size());

		Vehicle tmpVehicle;
		
			for(int i = 0; i < parsedTraces_.size(); i=i+7){				
				System.out.println("For Schleife");
				destinations = new ArrayDeque<WayPoint>(2);			
					
					try{
						System.out.println(parsedTraces_.get(i));

						double pickupX_ = (double)Double.parseDouble(parsedTraces_.get(i+1));
						double pickupY_ = (double)Double.parseDouble(parsedTraces_.get(i+2));
						int time_ = 0;				
						System.out.println("Parsing Double 1 done");
						
						System.out.println("*************");
						System.out.println(pickupX_);
						System.out.println(pickupY_);
						System.out.println("*************");
						//Add first way points here
						int[] CoordinatesPickup = new int[2];
						
						if (MapHelper.translateGPSToMapMetric(CoordinatesPickup, pickupX_, pickupY_) == false){
							continue;
						}
						System.out.println("GPXtoMetric Worked");
						
						
						System.out.println(CoordinatesPickup[0]);
						System.out.println(Map.getInstance().getMapWidth());
						System.out.println(CoordinatesPickup[1]);
						System.out.println(Map.getInstance().getMapHeight());
						
						
						WayPoint tmpWayPoint = new WayPoint(CoordinatesPickup[0],CoordinatesPickup[1], time_);
						System.out.println("Waypoint Worked");
							destinations.add(tmpWayPoint);
							System.out.println("Coordinates 1 created");

						double dropoffX_ = (double)Double.parseDouble(parsedTraces_.get(i+5));
						double dropoffY_ = (double)Double.parseDouble(parsedTraces_.get(i+6));							
						System.out.println("Parsing Double 2 done");
						//Add second way points here
						int[] CoordinatesDropoff = new int[2];
						
						if (MapHelper.translateGPSToMapMetric(CoordinatesDropoff, dropoffX_,dropoffY_) == false){
							continue;
						}
						WayPoint tmpWayPoint_2 = new WayPoint(CoordinatesDropoff[0],CoordinatesDropoff[1], time_);
							destinations.add(tmpWayPoint_2);
							System.out.println("Coordinates 2 created");
						
					} catch (Exception e) {
						System.out.println("Routing failed");
					}
					//Create vehicle
					try {
						if(destinations.size() >= 2){
						System.out.println("Before Vehicle created");
						System.out.println(destinations.size());
						System.out.println(destinations);
						tmpVehicle = new Vehicle(destinations, 1, 1, 1, false, false, 1, 1, 1, 1, 1, new Color(0,255,0), false, "");
						Map.getInstance().addVehicle(tmpVehicle);
						
						System.out.println("Vehicle created");
						}
					} catch (ParseException e) {
						System.out.println("Exception Vehicle creation");
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
					
					
					double x = (double)Double.parseDouble(parsedTraces_.get(i+1));
					double y = (double)Double.parseDouble(parsedTraces_.get(i+2));
					
					//Time - here the time is from the LAST point to the first one means
					//Vehicles have to drive BACKWARDS :)
					
					//long t_ = (long)(Long.parseLong(parsedTraces_.get(i+4)) - (long)(Long.parseLong(parsedTraces_.get(i+8))));
					//System.out.println(t_);
					
					int[] Coordinates = new int[2];
					MapHelper.translateGPSToMapMetric(Coordinates, x,y);
					WayPoint tmpWayPoint;
					try {
						tmpWayPoint = new WayPoint(Coordinates[0],Coordinates[1], 0);
						destinations.add(tmpWayPoint);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						
					
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
					
					double x = (double)Double.parseDouble(parsedTraces_.get(i+1));
					double y = (double)Double.parseDouble(parsedTraces_.get(i+2));
					
					int[] Coordinates = new int[2];
					MapHelper.translateGPSToMapMetric(Coordinates, x,y);
					WayPoint tmpWayPoint;
					try {
						tmpWayPoint = new WayPoint(Coordinates[0],Coordinates[1], 0);
						destinations.add(tmpWayPoint);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						
					
				}
			}
		}
		else if(simulationMode == 6){ //HH - OSM
			ArrayList<String> parsedTraces_ = GPSTracesXML.getInstance().getGpxTraces();
			
			Vehicle tmpVehicle;
			destinations = new ArrayDeque<WayPoint>();	
			//Filter for ID
			List<String> IDs= new ArrayList<String>();
			for (int i = 0; i < parsedTraces_.size(); i=i+4){
				if(!ArrayContainsElem(parsedTraces_.get(i), IDs)) IDs.add(parsedTraces_.get(i));
		}
		for (String s : IDs){
			for (int j = 0; j < parsedTraces_.size(); j=j+4){
				if (s.equals(parsedTraces_.get(j))){
					System.out.println("Hier");
					double x = (double)Double.parseDouble(parsedTraces_.get(j+1));
					double y = (double)Double.parseDouble(parsedTraces_.get(j+2));
					
					
					System.out.println(x);
					System.out.println(y);
					int[] Coordinates = new int[2];
					MapHelper.translateGPSToMapMetric(Coordinates, x,y);
					WayPoint tmpWayPoint;
					System.out.println(Coordinates[0]);
					System.out.println(Coordinates[1]);
					
					try {
						tmpWayPoint = new WayPoint(Coordinates[0],Coordinates[1], 0);
						destinations.add(tmpWayPoint);
						System.out.println("Waypoint added");
					} catch (ParseException e) {

						e.printStackTrace();
					}
						
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
					
					double x = (double)Double.parseDouble(parsedTraces_.get(j+1));
					double y = (double)Double.parseDouble(parsedTraces_.get(j+2));
					
					int[] Coordinates = new int[2];
					MapHelper.translateGPSToMapMetric(Coordinates, x,y);
					WayPoint tmpWayPoint;
					try {
						tmpWayPoint = new WayPoint(Coordinates[0],Coordinates[1], 0);
						destinations.add(tmpWayPoint);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						
					
				}
				}
			}
			
			
			
			
			
			
			
		}
		else if(simulationMode == 3){ //Shanghai
			ArrayList<String> parsedTraces_ = GPSTracesShanghai.getInstance().getShanghaiTraces(minLine, maxLine);
			Vehicle tmpVehicle;
			destinations = new ArrayDeque<WayPoint>();	
			//Filter for ID
			List<String> IDs= new ArrayList<String>();
			//HashSet<String> IDs = new HashSet<String>();
			for (int i = 0; i < parsedTraces_.size(); i=i+4){
					if(!ArrayContainsElem(parsedTraces_.get(i), IDs)) IDs.add(parsedTraces_.get(i));
			}
			for (String s : IDs){
				for (int j = 0; j < parsedTraces_.size(); j=j+4){
					if (s.equals(parsedTraces_.get(j))){
						
						double x = (double)Double.parseDouble(parsedTraces_.get(j+1));
						double y = (double)Double.parseDouble(parsedTraces_.get(j+2));
						
						int[] Coordinates = new int[2];
						MapHelper.translateGPSToMapMetric(Coordinates, x,y);
						WayPoint tmpWayPoint;
						try {
							tmpWayPoint = new WayPoint(Coordinates[0],Coordinates[1], 0);
							destinations.add(tmpWayPoint);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
							
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
						
						double x = (double)Double.parseDouble(parsedTraces_.get(j+1));
						double y = (double)Double.parseDouble(parsedTraces_.get(j+2));
						
						int[] Coordinates = new int[2];
						MapHelper.translateGPSToMapMetric(Coordinates, x,y);
						WayPoint tmpWayPoint;
						try {
							tmpWayPoint = new WayPoint(Coordinates[0],Coordinates[1], 0);
							destinations.add(tmpWayPoint);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
							
						
					}
				}
			}
			
			
			
		}

		//TODO: Update GUI
		
		Renderer.getInstance().setShowVehicles(true);
		Renderer.getInstance().ReRender(false, false);
		System.out.println("Done :)");
		//VanetSimStart.setProgressBar(true);

	}
	
	private static boolean ArrayContainsElem(String elem, List<String> array){
		for(String s : array){
			if(elem.equals(s)) return true;
		}
		return false;
	}


}
