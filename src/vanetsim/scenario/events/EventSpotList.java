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
package vanetsim.scenario.events;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;


import vanetsim.VanetSimStart;
import vanetsim.gui.Renderer;
import vanetsim.map.Map;




/**
 * This class stores all events.
 */
public final class EventSpotList{

	/** The only instance of this class (singleton). */
	private static final EventSpotList INSTANCE = new EventSpotList();

	
	/** the head of the event spot */
	private EventSpot head_ = null;

	/** the grids for the event probabiltites */
	int[][] gridEEBL_ = null;
	int[][] gridPCN_ = null;
	int[][] gridPCNFORWARD_ = null;
	int[][] gridRHCN_ = null;
	int[][] gridEVAFORWARD_ = null;
	int[][] gridEVA_ = null;


	/**
	 * Empty, private constructor in order to disable instancing.
	 */
	private EventSpotList(){
	}	

	/**
	 * Gets the single instance of this EventList.
	 * 
	 * @return single instance of this EventList
	 */
	public static EventSpotList getInstance(){
		return INSTANCE;
	}

	/**
	 * Adds an event.
	 * 
	 * @param event the event
	 */
	public void addEventSpot(EventSpot event){
		if(head_ == null) head_ = event;
		else{
			// test if event spot already exists
			EventSpot tmpEvent = head_;
			while(tmpEvent.getNext_() != null) {
				if(tmpEvent.getX_() == event.getX_() &&tmpEvent.getY_() == event.getY_())return;
				tmpEvent = tmpEvent.getNext_();
			}
			
			
			tmpEvent = head_;
			while(tmpEvent.getNext_() != null) tmpEvent = tmpEvent.getNext_();
			tmpEvent.setNext_(event);
		}
	}

	/**
	 * Removes an event.
	 * 
	 * @param event the event
	 */
	public void delEventSpot(EventSpot event){
		EventSpot tmpEvent = head_;
		if(tmpEvent.equals(event)) head_ = head_.getNext_();
		else{
			while(tmpEvent.getNext_() != null){
				if(tmpEvent.getNext_().equals(event)){
					tmpEvent.setNext_(tmpEvent.getNext_().getNext_());
				}
				tmpEvent = tmpEvent.getNext_();
			}
		}
	}

	/**
	 * find event with coordinates
	 */
	public EventSpot findEventSpot(int x, int y){
		EventSpot tmpSpot = head_;
		
		while(tmpSpot != null){
			if(tmpSpot.getX_() > (x - 100) && tmpSpot.getX_() < (x + 100) && tmpSpot.getY_() > (y - 100) && tmpSpot.getY_() < (y + 100)) return tmpSpot;
			tmpSpot = tmpSpot.getNext_();
		}
			
		return null;
	}
	
	/**
	 * find next event spot timing
	 */
	public int findEventSpotTiming(){
		int returnValue = 999999999;
		EventSpot tmpSpot = head_;
		while(tmpSpot != null){
			if(returnValue > tmpSpot.getEventSpotTiming_()) returnValue = tmpSpot.getEventSpotTiming_();
			tmpSpot = tmpSpot.getNext_();
		}
		return returnValue;
	}
	
	/**
	 * runs next simulation step
	 */
	public int doStep(int time){
		if(head_ != null){
			EventSpot tmpSpot = head_;
			while(tmpSpot != null){
				if(tmpSpot.getEventSpotTiming_() <= time) {
					
					tmpSpot.execute(Renderer.getInstance().getTimePassed());
				}
				tmpSpot = tmpSpot.getNext_();
			}
			return findEventSpotTiming();
		}
		return 999999999;
	}
	
	/**
	 * Removes all events.
	 */
	public void clearEvents(){
		head_ = null;
	}

	/**
	 * @return the head_
	 */
	public EventSpot getHead_() {
		return head_;
	}

	/**
	 * @param head_ the head_ to set
	 */
	public void setHead_(EventSpot head_) {
		this.head_ = head_;
	}
	
	
	/**
	 * calculates the probabilities of events
	 */
	public void calculateGrid(double gridSize, String filePath, String selectedGrid){
		int cellAmountX = (int) Math.ceil((double)Map.getInstance().getMapWidth()/gridSize);
		int cellAmountY = (int) Math.ceil((double)Map.getInstance().getMapHeight()/gridSize);
		gridEVA_ = new int[cellAmountX][cellAmountY];
		gridPCN_ = new int[cellAmountX][cellAmountY];
		gridEEBL_ = new int[cellAmountX][cellAmountY];
		gridPCNFORWARD_ = new int[cellAmountX][cellAmountY];
		gridRHCN_ = new int[cellAmountX][cellAmountY];
		gridEVAFORWARD_ = new int[cellAmountX][cellAmountY];
		
		try{
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(filePath);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			String[] strParse = null;
			ArrayList<String> sendersOfThisRound = new ArrayList<String>();
			String currentTime = "0";
			//Read file line By line
			while ((strLine = br.readLine()) != null) {
				strParse = strLine.split(":");
				if(Integer.parseInt(strParse[4]) > 0 && Integer.parseInt(strParse[6]) > 0){
					if(!currentTime.equals(strParse[0])){
						sendersOfThisRound.clear();
						currentTime = strParse[0];
					}
					
					if(!sendersOfThisRound.contains(strParse[8])){
						if(strParse[2].equals("HUANG_PCN")){
							gridPCN_[((int) Math.ceil(Integer.parseInt(strParse[4])/gridSize))-1][((int) Math.ceil(Integer.parseInt(strParse[6])/gridSize))-1]++;
						}
						else if(strParse[2].equals("HUANG_EEBL")){
							gridEEBL_[((int) Math.ceil(Integer.parseInt(strParse[4])/gridSize))-1][((int) Math.ceil(Integer.parseInt(strParse[6])/gridSize))-1]++;
						}
						else if(strParse[2].equals("PCN_FORWARD")){
							gridPCNFORWARD_[((int) Math.ceil(Integer.parseInt(strParse[4])/gridSize))-1][((int) Math.ceil(Integer.parseInt(strParse[6])/gridSize))-1]++;
						}
						else if(strParse[2].equals("HUANG_RHCN")){
							gridRHCN_[((int) Math.ceil(Integer.parseInt(strParse[4])/gridSize))-1][((int) Math.ceil(Integer.parseInt(strParse[6])/gridSize))-1]++;
						}
						else if(strParse[2].equals("HUANG_EVA_FORWARD")){
							gridEVAFORWARD_[((int) Math.ceil(Integer.parseInt(strParse[4])/gridSize))-1][((int) Math.ceil(Integer.parseInt(strParse[6])/gridSize))-1]++;
						}
						else if(strParse[2].equals("EVA_EMERGENCY_ID")){
							gridEVA_[((int) Math.ceil(Integer.parseInt(strParse[4])/gridSize))-1][((int) Math.ceil(Integer.parseInt(strParse[6])/gridSize))-1]++;
						}
						
						sendersOfThisRound.add(strParse[8]);
					}

				}

			}

			showGrid(selectedGrid, (int)gridSize);
			saveGrid(filePath.substring(0, filePath.length() - 4) + "grid_" + gridSize + ".log", (int)gridSize);
			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
			e.printStackTrace();
		}
	}

	public void createIntelligentStreetCluster(){
		//use to find nearest street
	}
	/**
	 * save the grid to file
	 */
	public void saveGrid(String filePath, int gridSize){
		try{
			// Create file 
			FileWriter fstream = new FileWriter(filePath);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("****:" + gridEEBL_.length + ":" + gridEEBL_[0].length + ":" + gridSize + "\n");
			out.write("HUANG_EEBL\n");
			for(int i = 0; i < gridEEBL_.length; i++){
				for(int j = 0; j < gridEEBL_[0].length - 1; j++){
					out.write(gridEEBL_[i][j] + ":");
				}
				out.write(gridEEBL_[i][gridEEBL_[0].length-1] + "\n");
			}
			out.write("HUANG_PCN\n");
			for(int i = 0; i < gridPCN_.length; i++){
				for(int j = 0; j < gridPCN_[0].length - 1; j++){
					out.write(gridPCN_[i][j] + ":");
				}
				out.write(gridPCN_[i][gridPCN_[0].length-1] + "\n");
			}
			out.write("PCN_FORWARD\n");
			for(int i = 0; i < gridPCNFORWARD_.length; i++){
				for(int j = 0; j < gridPCNFORWARD_[0].length - 1; j++){
					out.write(gridPCNFORWARD_[i][j] + ":");
				}
				out.write(gridPCNFORWARD_[i][gridPCNFORWARD_[0].length-1] + "\n");
			}
			out.write("HUANG_RHCN\n");
			for(int i = 0; i < gridRHCN_.length; i++){
				for(int j = 0; j < gridRHCN_[0].length - 1; j++){
					out.write(gridRHCN_[i][j] + ":");
				}
				out.write(gridRHCN_[i][gridRHCN_[0].length-1] + "\n");
			}
			out.write("HUANG_EVA_FORWARD\n");
			for(int i = 0; i < gridEVAFORWARD_.length; i++){
				for(int j = 0; j < gridEVAFORWARD_[0].length - 1; j++){
					out.write(gridEVAFORWARD_[i][j] + ":");
				}
				out.write(gridEVAFORWARD_[i][gridEVAFORWARD_[0].length-1] + "\n");
			}
			out.write("EVA_EMERGENCY_ID\n");
			for(int i = 0; i < gridEVA_.length; i++){
				for(int j = 0; j < gridEVA_[0].length - 1; j++){
					out.write(gridEVA_[i][j] + ":");
				}
				out.write(gridEVA_[i][gridEVA_[0].length-1] + "\n");
			}
			//Close the output stream
			out.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
	  }
	}
	
	/**
	 * load the grid
	 */
	public void loadGrid(String filepath, String selectedGrid){	
		  int sizeX = 0;
		  int sizeY = 0;
		  int gridSize = 0;
		  int[][] grid = null;
		  int counter = 0;
		  
		  try{
			  // Open the file that is the first 
			  	// command line parameter
			  FileInputStream fstream = new FileInputStream(filepath);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;

			  //Read File Line By Line
			  while ((strLine = br.readLine()) != null)   {
				  if(strLine.length() > 4){
					  if(strLine.substring(0, 4).equals("****")){
						  sizeX = Integer.parseInt(strLine.split(":")[1]);
						  sizeY = Integer.parseInt(strLine.split(":")[2]);
						  gridSize = Integer.parseInt(strLine.split(":")[3]);
					  }
					  else if(strLine.equals("HUANG_PCN")){
						  gridPCN_ = new int[sizeX][sizeY];
						  grid = gridPCN_;
						  counter = 0;
					  }
					  else if(strLine.equals("HUANG_EEBL")){
						  gridEEBL_ = new int[sizeX][sizeY];
						  grid = gridEEBL_;
						  counter = 0;
					  }
					  else if(strLine.equals("PCN_FORWARD")){
						  gridPCNFORWARD_ = new int[sizeX][sizeY];
						  grid = gridPCNFORWARD_;
						  counter = 0;
					  }
					  else if(strLine.equals("HUANG_RHCN")){
						  gridRHCN_ = new int[sizeX][sizeY];
						  grid = gridRHCN_;
						  counter = 0;
					  }
					  else if(strLine.equals("HUANG_EVA_FORWARD")){
						  gridEVAFORWARD_ = new int[sizeX][sizeY];
						  grid = gridEVAFORWARD_;
						  counter = 0;
					  }
					  else if(strLine.equals("EVA_EMERGENCY_ID")){
						  gridEVA_ = new int[sizeX][sizeY];
						  grid = gridEVA_;
						  counter = 0;
					  }
					  else{
						  for(int i = 0; i < strLine.split(":").length; i++){
							  grid[counter][i] = Integer.parseInt(strLine.split(":")[i]);
						  }
						  counter++;
					  }
				  }
			  }
			  //Close the input stream
			  in.close();
		  }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
		  }
		  VanetSimStart.getMainControlPanel().getEditPanel().getEditEventSpotsPanel_().getGridSize_().setValue(gridSize);
		  showGrid(selectedGrid, gridSize);
	}
	
	/**
	 * shows the selected grid on the map
	 */
	public void showGrid(String selectedGrid, int gridSize){
		if(selectedGrid.equals("HUANG_PCN")) Renderer.getInstance().setGrid(gridPCN_);
		else if(selectedGrid.equals("HUANG_EEBL")) Renderer.getInstance().setGrid(gridEEBL_);
		if(selectedGrid.equals("PCN_FORWARD")) Renderer.getInstance().setGrid(gridPCNFORWARD_);
		if(selectedGrid.equals("HUANG_RHCN")) Renderer.getInstance().setGrid(gridRHCN_);
		if(selectedGrid.equals("HUANG_EVA_FORWARD")) Renderer.getInstance().setGrid(gridEVAFORWARD_);
		if(selectedGrid.equals("EVA_EMERGENCY_ID")) Renderer.getInstance().setGrid(gridEVA_);

		int[][] grid = Renderer.getInstance().getGrid();
		int min = 999999999;
		int max = 0;
		
		for(int i = 0; i < grid.length;i++){
			for(int j = 0; j < grid[0].length; j++){
				if(grid[i][j] > max) max = grid[i][j];
				if(grid[i][j] < min) min = grid[i][j];
			}
		}
		
		Renderer.getInstance().setMinGridValue(min);
		Renderer.getInstance().setMaxGridValue(max);
		Renderer.getInstance().setGridSize_(gridSize);
		
		Renderer.getInstance().ReRender(true, true);
	}
	
	
	
	/**
	 * @return the gridEVA_
	 */
	public int[][] getGridEVA_() {
		return gridEVA_;
	}

	/**
	 * @param gridEVA_ the gridEVA_ to set
	 */
	public void setGridEVA_(int[][] gridEVA_) {
		this.gridEVA_ = gridEVA_;
	}



}