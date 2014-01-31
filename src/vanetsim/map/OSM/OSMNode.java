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
package vanetsim.map.OSM;

import vanetsim.map.Node;

/**
 * A helper class for processing OpenStreetMap-Nodes.
 */
public final class OSMNode{
	
	/** The longitude representing the origin of the map. */
	private static double longitudeMiddle_ = 1.0;
	
	/** The correction in x direction which is used for all OSM_Nodes. */
	private static double correctionX_ = 0;
	
	/** The correction in y direction which is used for all OSM_Nodes. */
	private static double correctionY_ = 0;
	
	/** The latitude of this node. */
	private final double latitude_;
	
	/** The longitude of this node. */
	private final double longitude_;
	
	/** flag for traffic signals */
	private final boolean hasTrafficSignal_;
	
	/** attribute for amenity */
	private String amenity_;

	
	/**
	 * Instantiates a new helper node.
	 * 
	 * @param latitude		the latitude
	 * @param longitude	the longitude
	 */
	public OSMNode(double latitude, double longitude){
		latitude_ = latitude;
		longitude_ = longitude;
		
		hasTrafficSignal_ = false;
	}
	
	/**
	 * Instantiates a new helper node.
	 * 
	 * @param latitude		the latitude
	 * @param longitude	the longitude
	 * @param hasTrafficSignal if node has a traffic signal
	 */
	public OSMNode(double latitude, double longitude, boolean hasTrafficSignal){
		latitude_ = latitude;
		longitude_ = longitude;
		
		hasTrafficSignal_ = hasTrafficSignal;
	}
	
	
	/**
	 * Instantiates a new helper node.
	 * 
	 * @param latitude		the latitude
	 * @param longitude	the longitude
	 * @param amenity the amenity type
	 */
	public OSMNode(double latitude, double longitude, String amenity){
		latitude_ = latitude;
		longitude_ = longitude;
		hasTrafficSignal_ = false;
		amenity_ = amenity;
	}
	
	/**
	 * Sets the corrections. Valid for all OSM_Nodes so it's static!
	 * 
	 * @param longitudeMiddle	the longitude to use the middle for all conversions from WGS84 to UTM
	 * @param correctionX		the correction value for the x coordinate (minimum x value you have)
	 * @param correctionY		the correction value for the y coordinate (maximum y value you have)
	 */
	public static void setCorrections(double longitudeMiddle, double correctionX, double correctionY){
		longitudeMiddle_ = longitudeMiddle;
		correctionX_ = correctionX;
		correctionY_ = correctionY;
	}
	
	
	/**
	 * Gets a real node (node with coordinates in cm) associated to this node.
	 * 
	 * @return the ready-to-use real node
	 */
	public Node getRealNode(){
		//convert coordinates
		double[] result = new double[2];
		OSMLoader.getInstance().WGS84toUTM(result,longitude_, latitude_, false, longitudeMiddle_, false);
		
		//make corrections
		int x = (int)Math.round((result[0] - correctionX_) * 100);
		int y = (int)Math.round((correctionY_ - result[1]) * 100);
		return new Node(x,y, hasTrafficSignal_);
	}

	/**
	 * @return the amenity_
	 */
	public String getAmenity_() {
		return amenity_;
	}

	/**
	 * @param amenity_ the amenity_ to set
	 */
	public void setAmenity_(String amenity_) {
		this.amenity_ = amenity_;
	}
}