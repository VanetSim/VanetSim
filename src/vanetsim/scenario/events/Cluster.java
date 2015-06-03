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

import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class Cluster {
	/** id of the cluster */
	private String clusterID_;
	
	/** event type the cluster represents */
	private String eventType_;
	
	/** min max values of the map */
	private int minX_ = Integer.MAX_VALUE;
	private int minY_ = Integer.MAX_VALUE;
	private int maxX_ = 0;
	private int maxY_ = 0;
	
	/** size of the cluster */
	private int size_ = 0;
	
	/** color of the cluster */
	private Color clusterColor = Color.black;

	/** coordinates of the cluster map */
	private ArrayList<Integer> xCoords_ = new ArrayList<Integer>();
	private ArrayList<Integer> yCoords_ = new ArrayList<Integer>();

	
	public boolean fillCluster(String filePath, String clusterID){		
		String searchKey = "@data";
		
		clusterID_ = clusterID;
		
		try{
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(filePath);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			String[] splitLine;
			//Read file line By line
			
			boolean found = false;
			
			while ((strLine = br.readLine()) != null) {
				if(strLine.equals(searchKey)) found = true;
				
				if(found){								
					splitLine = strLine.split(",");
					
					if(splitLine[splitLine.length-1].equals("cluster" + clusterID_)){
						size_++;
						eventType_ = splitLine[1];
						
						if(eventType_.equals("HUANG_PCN")) clusterColor = Color.green;
						if(eventType_.equals("HUANG_EEBL")) clusterColor = Color.blue;
						if(eventType_.equals("PCN_FORWARD")) clusterColor = Color.pink;
						if(eventType_.equals("HUANG_RHCN")) clusterColor = Color.gray;
						if(eventType_.equals("EVA_FORWARD")) clusterColor = Color.cyan;
						if(eventType_.equals("HUANG_EVA")) clusterColor = Color.magenta;
						
						xCoords_.add(Integer.parseInt(splitLine[2]));
						yCoords_.add(Integer.parseInt(splitLine[3]));
						if(Integer.parseInt(splitLine[2]) > maxX_) maxX_ = Integer.parseInt(splitLine[2]);
						if(Integer.parseInt(splitLine[2]) < minX_) minX_ = Integer.parseInt(splitLine[2]);
						
						if(Integer.parseInt(splitLine[3]) > maxY_) maxY_ = Integer.parseInt(splitLine[3]);
						if(Integer.parseInt(splitLine[3]) < minY_) minY_ = Integer.parseInt(splitLine[3]);
					}
					

				}	
			}
			  
			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		
		if(size_ > 0) return true;
		else return false;
	}
	
	public String toString(){
		return "cluster - " + clusterID_;
	}

	public String getEventType_() {
		return eventType_;
	}

	public int getMinX_() {
		return minX_;
	}

	public int getMinY_() {
		return minY_;
	}

	public int getMaxX_() {
		return maxX_;
	}

	public int getMaxY_() {
		return maxY_;
	}

	public int getSize_() {
		return size_;
	}

	public Color getClusterColor() {
		return clusterColor;
	}
	
	public ArrayList<Integer> getxCoords_() {
		return xCoords_;
	}

	public ArrayList<Integer> getyCoords_() {
		return yCoords_;
	}

	public String getClusterID_() {
		return clusterID_;
	}

	public void setClusterID_(String clusterID_) {
		this.clusterID_ = clusterID_;
	}
}
