package vanetsim.gpstracing;

import java.awt.Color;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
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
			 File ShanghaiFile_ = new File("../Vanetsim/Shanghai_kl.xml");
			 Map.getInstance().load(ShanghaiFile_, false);
		}
		//Traces San Francisco
		else if(simulationMode == 4){
			 File SanFranciscoFile_ = new File("../Vanetsim/SanFrancisco_kl.xml");
			 Map.getInstance().load(SanFranciscoFile_, false);
		}
		//Traces New York
		else if(simulationMode == 5){
			 File NYFile_ = new File("../VanetSim/NY_kl.xml");
			 Map.getInstance().load(NYFile_, false);
		}
		//Traces Hamburg
		else if(simulationMode == 6){
			 File HHFile_ = new File("../Vanetsim/Hamburg_kl.xml");
			 Map.getInstance().load(HHFile_, false);
		}
		
	}
	
	public static void precalculateRoute(int simulationMode, int minLine, int maxLine){
	//	VanetSimStart.setProgressBar(true);
		
		ArrayDeque<WayPoint> destinations = null;
		ArrayDeque<Double> tripTimes = null;
		
		//TODO: Simulation does not start.
		
		
		if(simulationMode == 5){ //NY

		ArrayList<String> parsedTraces_ = GPSTracesNY.getInstance().getNYTraces(minLine, maxLine);
		Vehicle tmpVehicle;
		
			for(int i = 0; i < parsedTraces_.size(); i=i+7){
				
				double tripTime = (double)Double.parseDouble(parsedTraces_.get(i+5));
			
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

						double dropoffX_ = (double)Double.parseDouble(parsedTraces_.get(i+5));
						double dropoffY_ = (double)Double.parseDouble(parsedTraces_.get(i+6));							
						//Add second way points here
						int[] CoordinatesDropoff = new int[2];
						
						if (MapHelper.translateGPSToMapMetric(CoordinatesDropoff, dropoffX_,dropoffY_) == false){
							destinations.clear();
							continue;
						}
						WayPoint tmpWayPoint_2 = new WayPoint(CoordinatesDropoff[0],CoordinatesDropoff[1], 0);
							destinations.add(tmpWayPoint_2);
						
					} catch (Exception e) {
						System.out.println("Routing failed");
					}
					//Create vehicle
					try {
						System.out.println(destinations.size());

						if(!destinations.isEmpty()){
							double time = (double)Double.parseDouble(parsedTraces_.get(i+5));
							tripTimes.add(time);
							//TODO:
						tmpVehicle = new Vehicle(destinations, tripTimes, 1, 1, 1, false, false, 1, 1, 1, 1, 1, new Color(0,255,0), false, "");
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
			
			for (int i = 0; i< parsedTraces_.size(); i=i+4){				
				if (i==0){	
					double x = (double)Double.parseDouble(parsedTraces_.get(i+1));
					double y = (double)Double.parseDouble(parsedTraces_.get(i+2));
					
					//Time - here the time is from the LAST point to the first one means
					//Vehicles have to drive BACKWARDS :)
					
					//Calculate times

					
					Date t1;
					Date t2;
					try {
						t1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(parsedTraces_.get(i+8));
						t2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(parsedTraces_.get(i+4));
						
						double timeDif = t1.getTime() - t2.getTime();
						tripTimes.add(timeDif);
						
						
					} catch (ParseException e1) {
						System.out.println("Parsing Times in SF Traces Failed");
						e1.printStackTrace();
					}
					
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
						
					
					continue;
				}
				else if(parsedTraces_.get(i).equals(parsedTraces_.get(i-4))){
					//System.out.println(parsedTraces_.get(i));
					
					//Put shit in Metricstuff
					//System.out.println("Else if i == i-4");
					
					double x = (double)Double.parseDouble(parsedTraces_.get(i+1));
					double y = (double)Double.parseDouble(parsedTraces_.get(i+2));
					
					Date t1;
					Date t2;
					try {
						t1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(parsedTraces_.get(i+8));
						t2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(parsedTraces_.get(i+4));
						
						double timeDif = t1.getTime() - t2.getTime();
						tripTimes.add(timeDif);
						
						
					} catch (ParseException e1) {
						System.out.println("Parsing Times in SF Traces Failed");
						e1.printStackTrace();
					}
					
					
					
					
					//Time - here the time is from the LAST point to the first one means
					//Vehicles have to drive BACKWARDS :)
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

					//long t_ = (long)(Long.parseLong(parsedTraces_.get(i+4)) - (long)(Long.parseLong(parsedTraces_.get(i+8))));
					//System.out.println(t_);
					continue;
				}
				else if(!parsedTraces_.get(i).equals(parsedTraces_.get(i-4))){
					//Initialize new vehicle
					try {
						if(destinations.size() >= 2){
							tmpVehicle =  new Vehicle(destinations, tripTimes, 1, 1, 1, false, false, 1, 1, 1, 1, 1, new Color(0,255,0), false, "");
						Map.getInstance().addVehicle(tmpVehicle);
						System.out.println("Vehicle created");
						}
					} catch (ParseException e) {

						e.printStackTrace();
					}
					
					
					//empty destinations
					destinations.clear();
					tripTimes.clear();
					
					double x = (double)Double.parseDouble(parsedTraces_.get(i+1));
					double y = (double)Double.parseDouble(parsedTraces_.get(i+2));
					
					Date t1;
					Date t2;
					try {
						t1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(parsedTraces_.get(i+8));
						t2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(parsedTraces_.get(i+4));
						
						double timeDif = t1.getTime() - t2.getTime();
						tripTimes.add(timeDif);
						
						
					} catch (ParseException e1) {
						System.out.println("Parsing Times in SF Traces Failed");
						e1.printStackTrace();
					}
					
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
					}
				}
				
			}
			try {
				if(destinations.size() >= 2){
					tmpVehicle = new Vehicle(destinations, tripTimes, 1, 1, 1, false, false, 1, 1, 1, 1, 1, new Color(0,255,0), false, "");
				Map.getInstance().addVehicle(tmpVehicle);
				}
			} catch (ParseException e) {
				e.printStackTrace();
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
					double x = (double)Double.parseDouble(parsedTraces_.get(j+1));
					double y = (double)Double.parseDouble(parsedTraces_.get(j+2));
					
					Date t1;
					Date t2;
					try {
						t1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(parsedTraces_.get(j+8));
						t2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(parsedTraces_.get(j+4));
						
						double timeDif = t1.getTime() - t2.getTime();
						tripTimes.add(timeDif);
						
						
					} catch (ParseException e1) {
						System.out.println("Parsing Times in HH Traces Failed");
						e1.printStackTrace();
					}
					
					
					
					
					
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
						
				}
				else {
					try {
						if(destinations.size() >= 2){
							tmpVehicle =  new Vehicle(destinations, tripTimes, 1, 1, 1, false, false, 1, 1, 1, 1, 1, new Color(0,255,0), false, "");
						Map.getInstance().addVehicle(tmpVehicle);
						}
					} catch (ParseException e) {
						
						e.printStackTrace();
					}
					
					//empty destinations
					destinations.clear();
					tripTimes.clear();
					
					double x = (double)Double.parseDouble(parsedTraces_.get(j+1));
					double y = (double)Double.parseDouble(parsedTraces_.get(j+2));
					
					Date t1;
					Date t2;
					try {
						t1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(parsedTraces_.get(j+8));
						t2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(parsedTraces_.get(j+4));
						
						double timeDif = t1.getTime() - t2.getTime();
						tripTimes.add(timeDif);
						
						
					} catch (ParseException e1) {
						System.out.println("Parsing Times in HH Traces Failed");
						e1.printStackTrace();
					}
					
					
					
					
					
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
						
						
						Date t1;
						Date t2;
						try {
							t1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(parsedTraces_.get(j+8));
							t2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(parsedTraces_.get(j+4));
							
							double timeDif = t1.getTime() - t2.getTime();
							tripTimes.add(timeDif);
							
							
						} catch (ParseException e1) {
							System.out.println("Parsing Times in SH Traces Failed");
							e1.printStackTrace();
						}
						
						
						
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
						}
							
					}
					else {
						try {
							if(destinations.size() >= 2){
							tmpVehicle =  new Vehicle(destinations, tripTimes, 1, 1, 1, false, false, 1, 1, 1, 1, 1, new Color(0,255,0), false, "");
							Map.getInstance().addVehicle(tmpVehicle);
							}
						} catch (ParseException e) {
							
							e.printStackTrace();
						}
						
						//empty destinations
						destinations.clear();
						tripTimes.clear();
						
						double x = (double)Double.parseDouble(parsedTraces_.get(j+1));
						double y = (double)Double.parseDouble(parsedTraces_.get(j+2));
						
						Date t1;
						Date t2;
						try {
							t1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(parsedTraces_.get(j+8));
							t2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(parsedTraces_.get(j+4));
							
							double timeDif = t1.getTime() - t2.getTime();
							tripTimes.add(timeDif);
							
							
						} catch (ParseException e1) {
							System.out.println("Parsing Times in SH Traces Failed");
							e1.printStackTrace();
						}
						
						
						
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
						}
							
						
					}
				}
			}
			
			
			
		}
		//Update GUI
		Renderer.getInstance().setShowVehicles(true);
		Renderer.getInstance().ReRender(true, true);
		System.out.println("Done :)");
		VanetSimStart.setProgressBar(false);

	}
	
	private static boolean ArrayContainsElem(String elem, List<String> array){
		for(String s : array){
			if(elem.equals(s)) return true;
		}
		return false;
	}


}
