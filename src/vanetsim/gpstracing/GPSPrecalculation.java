package vanetsim.gpstracing;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;

import javax.swing.JFormattedTextField;

import vanetsim.map.*;
import vanetsim.routing.WayPoint;

public class GPSPrecalculation {

	//TODO: Create Maps and save them :) No maps there yet, expect for NY (might need a new one though)
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
	

	//TODO: Run method according to selected Traces
	public static void runParser(int simulationMode_){
		//Traces Shanghai
				if(simulationMode_ == 3){
					GPSTracesShanghai.getInstance().getShanghaiTraces();
				}
				//Traces San Francisco
				else if(simulationMode_ == 4){
					GPSTracesSanFrancisco.getInstance().getSanFranciscoTraces();
				}
				//Traces New York
				else if(simulationMode_ == 5){
					GPSTracesNY.getInstance().getNYTraces();
					
				}
				//Traces Hamburg
				else if(simulationMode_ == 6){
					GPSTracesXML.getInstance().getGpxTraces();
				}
	}
	
	
	//TODO: Calculate Route
	public static void precalculateRoute(int simulationMode){
		
		
		
		//TODO: Precalculate relative Times
		
		//TODO: Create Waypoints
		ArrayDeque<WayPoint> destinations = null;
		
		//TODO: simulationMode wird zu Testzwecken immer gleich NY gesetzt
		simulationMode = 5;
		
		ArrayList<String> parsedTraces = null;
		
		//Traces Shanghai
		if(simulationMode == 3){
			parsedTraces = GPSTracesShanghai.getInstance().getShanghaiTraces(); 
		}
		//Traces San Francisco
		else if(simulationMode == 4){
			parsedTraces = GPSTracesSanFrancisco.getInstance().getSanFranciscoTraces(); 
		}
		//Traces New York
		else if(simulationMode == 5){
			parsedTraces = GPSTracesNY.getInstance().getNYTraces(); 
		}
		//Traces Hamburg
		else if(simulationMode == 6){
			parsedTraces = GPSTracesXML.getInstance().getGpxTraces(); 
		}
		
		
		System.out.println("I am here und ich habe :");
		System.out.println(parsedTraces.size());
		System.out.println(parsedTraces);
		for(int i = 0; i < 5;){
			
			
			/**
			int j = 0;			
			int x = 0;	
			int y = 0;	
			
			destinations = new ArrayDeque<WayPoint>(2);			
			while(j < 2){	
				try{
					
					WayPoint tmpWayPoint = new WayPoint(x,y,0);//TODO:Die 0 ist hier ein Platzhalter für die relative Zeit !
						destinations.add(tmpWayPoint);
						++j;
					
				} catch (Exception e) {
					System.out.println("Routing failed");
				}
				
			}*/
		
		}
		
		
		//TODO: Initialize Vehicles
		
		//TODO: Update GUI
		
		
		
	}


}
