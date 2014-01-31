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
package vanetsim.scenario;

import vanetsim.gui.Renderer;


/**
 * A Road-Side-Unit to intercept WiFi signals.
 */

public final class AttackRSU {
	
	/** A common counter to generate unique IDs */
	private static int counter_ = 1;
	
	/** The x coordinate. */
	private final int x_;
	
	/** The y coordinate. */
	private final int y_;	
	
	/** The wifi radius */
	private final int wifiRadius_;
	
	/** A unique ID for this Attack-Road-Side-Unit */
	private final long arsuID_;
	
	/**
	 * Instantiates a new Attack-RSU.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param radius the signal radius
	 */
	public AttackRSU(int x, int y, int radius) {
		x_ = x;
		y_ = y;
		wifiRadius_ = radius;
		
		arsuID_ = counter_;
		++counter_;
		
		//add this Attack-RSU to the list with all ARSUS (an array is used for a better performance)
		int arrayLength = Vehicle.getArsuList().length;
		AttackRSU[] tempArray = new AttackRSU[arrayLength+1];
		for(int i=0;i<arrayLength;i++){
			tempArray[i]=Vehicle.getArsuList()[i];
		}
		
		tempArray[tempArray.length-1] = this;
		Vehicle.setArsuList(tempArray);
	}
	
	/**
	 * Searches for an arsu near this coordinates and deletes it. Returns true if an arsu was found and deleted.
	 * 
	 * @param x	coordinate of the ARSU to delete
	 * @param y	coordinate of the ARSU to delete
	 * 
 	 * @return if delete or no
	 */
	public static final boolean deleteARSU(int x, int y){
		Boolean returnValue = false;
		
		AttackRSU arsu = null;
		int oldIndex = -1;
		
		for(int i = 0; i < Vehicle.getArsuList().length;i++){
			if(Vehicle.getArsuList()[i].getX() > (x - 300) && Vehicle.getArsuList()[i].getX() < (x + 300) && Vehicle.getArsuList()[i].getY() > (y - 300) && Vehicle.getArsuList()[i].getY() < (y + 300)) {
				arsu = Vehicle.getArsuList()[i];
				oldIndex = i;
			}
		}
		
		if(arsu != null){
			Vehicle.getArsuList()[oldIndex] = Vehicle.getArsuList()[Vehicle.getArsuList().length-1];
		
			int arrayLength = Vehicle.getArsuList().length;
			AttackRSU[] tempArray = new AttackRSU[arrayLength-1];
			for(int i=0;i<tempArray.length;i++){
				tempArray[i]=Vehicle.getArsuList()[i];
			}
			Vehicle.setArsuList(tempArray);
			Renderer.getInstance().ReRender(true, false);
			returnValue = true;
			}
		
		return returnValue;
	}
	
	/**
	 * Get a Attack RSU with coordinates
	 * 
	 * @param x		X coordinate
	 * @param y		Y coordinate
	 * 
	 * @return the Attack RSU or null
	 */
	public static final AttackRSU getARSU(int x, int y){
		AttackRSU returnValue = null;
		
		for(int i = 0; i < Vehicle.getArsuList().length;i++){
			if(Vehicle.getArsuList()[i].getX() > (x - 100) && Vehicle.getArsuList()[i].getX() < (x + 100) && Vehicle.getArsuList()[i].getY() > (y - 100) && Vehicle.getArsuList()[i].getY() < (y + 100)) {
				returnValue = Vehicle.getArsuList()[i];
			}
		}
		
		return returnValue;
	}
	
	

	/**
	 * Returns the x coordinate of the RSU
	 * 
	 * @return the x coordinate
	 */
	public int getX() {
		return x_;
	}

	/**
	 * Returns the y coordinate of the RSU
	 * 
	 * @return the y coordinate
	 */
	public int getY() {
		return y_;
	}

	/**
	 * Returns the wifi radius
	 * 
	 * @return the wifi radius in cm
	 */
	public int getWifiRadius() {
		return wifiRadius_;
	}

	/**
	 * Returns the ARSU ID
	 * 
	 * @return the ID of this arsu
	 */
	public long getArsuID_() {
		return arsuID_;
	}
}