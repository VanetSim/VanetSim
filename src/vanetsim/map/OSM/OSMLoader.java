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

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CyclicBarrier;
import java.util.ArrayDeque;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMInputCursor;

import vanetsim.ErrorLog;
import vanetsim.VanetSimStart;
import vanetsim.gui.controlpanels.MapSizeDialog;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.map.Node;
import vanetsim.map.Street;

/**
 * A class including functionality to import streets from the OpenStreetMap project. Implemented as Singleton.
 */
public final class OSMLoader{
	
	/** The only instance of this class (singleton). */
	private static final OSMLoader INSTANCE = new OSMLoader();
	
	/** Equatorial radius in meters (WGS84 specific, see <a href="http://en.wikipedia.org/wiki/Geodetic_system#World_Geodetic_System_1984_.28WGS84.29">Wikipedia</a> for details (link last visited: 23.08.08) */
	private static final double E_RADIUS = 6378137.0;

	/** First eccentricity squared (WGS84 specific, see <a href="http://en.wikipedia.org/wiki/Geodetic_system#World_Geodetic_System_1984_.28WGS84.29">Wikipedia</a> for details (link last visited: 23.08.08) */
	private static final double ECC_SQUARED = 0.00669437999014;

	/** Second eccentricity squared (WGS84 specific, see <a href="http://en.wikipedia.org/wiki/Geodetic_system#World_Geodetic_System_1984_.28WGS84.29">Wikipedia</a> for details (link last visited: 23.08.08) */
	private static final double ECC2_SQUARED = 0.00673949674228;

	/**
	 * Empty, private constructor in order to disable instancing.
	 */
	private OSMLoader() {
	}
	
	
 	/**
	  * Gets the single instance of OSM_Loader.
	  * 
	  * @return single instance of OSM_Loader
	  */
	 public static OSMLoader getInstance(){
 		return INSTANCE;
	}
	
	/**
	 * Function to load in an OSM map in OSM Protocol version 0.5. See <a href="http://wiki.openstreetmap.org/index.php/OSM_Protocol_Version_0.5">
	 * the protocol specifications</a> the other wiki entries and the source annotations for details.
	 * This functions expects an already downloaded file from a bounding box link like <a href="http://api.openstreetmap.org/api/0.5/map?bbox=11.54,48.14,11.543,48.145">
	 * http://api.openstreetmap.org/api/0.5/map?bbox=11.54,48.14,11.543,48.145</a> or created by Osmosis as an input.<br>
	 * The parsing code assumes that all nodes are declared before the first way comes (which is so in all currently known OSM files).
	 * <br><br>
	 * Notes:
	 * <ul>
	 * <li>If the map is somewhere in the pacific ocean and there are negative AND positive values for the latitude, this function does not work 
	 *     as minLatitude and maxLatitude aren't set correctly. However, there should not be any really interesting maps there...</li>
	 * <li>If there are nodes which don't belong to a street (but rather to some sightseeing place or so) the map might get too large as the 
	 *     map size must be calculated before the type of the node can be checked.</li>
	 * </ul>
	 * 
	 * @param file	the file to import
	 */
	public void loadOSM(File file){
		Map map = Map.getInstance();
		try{
			VanetSimStart.setProgressBar(true);
			String childtype, waytype, nodetype, key, value, streetName, streetType;
			int i=0, id, maxspeed, isOneway, lanes;
			double latitude, longitude, minLatitude=Double.MAX_VALUE, maxLatitude=-Double.MAX_VALUE, minLongitude=Double.MAX_VALUE, maxLongitude=-Double.MAX_VALUE;
			
			
			boolean maxspeedSet, onewaySet, isRoundabout, laneSet, correctionSet = false, hasTrafficSignal;
			Node lastNode, node;
			OSMNode tmpNode;
			HashMap<Integer,OSMNode> OSMNodes = new HashMap<Integer,OSMNode>();
			ArrayList<OSMNode>amenityNodes = new ArrayList<OSMNode>();
			ArrayDeque<Integer> wayPoints = new ArrayDeque<Integer>();
			Iterator<Integer> wayPointsIterator;
			Color displayColor;
			SMInputCursor wayCursor, nodeCursor;
			XMLInputFactory factory = XMLInputFactory.newInstance();

			ErrorLog.log(Messages.getString("OSM_Loader.loading") + file.getName(), 3, OSMLoader.class.getName(), "loadOSM", null); //$NON-NLS-1$ //$NON-NLS-2$
			// configure some factory options...
			factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
			factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
			factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
			factory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);

			FileInputStream filestream = new FileInputStream(file);
			XMLStreamReader sr = factory.createXMLStreamReader(filestream);	
			SMInputCursor rootCrsr = SMInputFactory.rootElementCursor(sr);
			rootCrsr.getNext();
			
			if(rootCrsr.getLocalName().toLowerCase().equals("osm") && (rootCrsr.getAttrValue("version").equals("0.5") || rootCrsr.getAttrValue("version").equals("0.6"))){	// only accept version 0.5 and 0.6 because other versions might have changed XML syntax! //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				SMInputCursor childCrsr = rootCrsr.childElementCursor();
				while (childCrsr.getNext() != null){
					childtype = childCrsr.getLocalName().toLowerCase();
					if(childtype.equals("node")){		// A node which is referenced later by a way //$NON-NLS-1$
						if(correctionSet) ErrorLog.log(Messages.getString("OSM_Loader.nodeAfterWay"), 7, Map.class.getName(), "loadOSM", null);  //$NON-NLS-1$ //$NON-NLS-2$
						if(childCrsr.getAttrValue("visible")==null || !childCrsr.getAttrValue("visible").equals("false")){		// if attribute missing or true => use it!								 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							try{
								id = Integer.parseInt(childCrsr.getAttrValue("id")); //$NON-NLS-1$
								latitude = Double.parseDouble(childCrsr.getAttrValue("lat")); //$NON-NLS-1$
								longitude = Double.parseDouble(childCrsr.getAttrValue("lon")); //$NON-NLS-1$
								if(latitude < minLatitude){
									minLatitude = latitude;
								}
								
								if(latitude > maxLatitude){
									maxLatitude = latitude;
								}
								if(longitude < minLongitude) minLongitude = longitude;
								if(longitude > maxLongitude) maxLongitude = longitude;
								
								hasTrafficSignal = false;
								nodeCursor = childCrsr.childElementCursor();
								while (nodeCursor.getNext() != null){
									nodetype = nodeCursor.getLocalName().toLowerCase();
									if(nodetype.equals("tag")){ //$NON-NLS-1$
										key = nodeCursor.getAttrValue("k").toLowerCase(); //$NON-NLS-1$
										value = nodeCursor.getAttrValue("v"); //$NON-NLS-1$
										if (key.equals("highway")){
											if(value.equals("traffic_signals")) hasTrafficSignal = true;
										}
										
										if (key.equals("amenity")){
											if(value.equals("school")  || value.equals("hospital")|| value.equals("police") || value.equals("fire_station") || value.equals("kindergarten")) amenityNodes.add(new OSMNode(latitude, longitude, value));
										}
									}
								}
								
								OSMNodes.put(id, new OSMNode(latitude,longitude, hasTrafficSignal));		// put them in a hashmap!
							} catch (Exception e) {ErrorLog.log(Messages.getString("OSM_Loader.errorParsingNode"), 5, Map.class.getName(), "loadOSM", e);} //$NON-NLS-1$ //$NON-NLS-2$
						}
					} else if (childtype.equals("way")){		// A way with nodes as waypoints //$NON-NLS-1$
						if(childCrsr.getAttrValue("visible")==null || !childCrsr.getAttrValue("visible").equals("false")){		// if attribute missing or true => use it! //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							try{
								// set the correction parameters and map size only once
								if(correctionSet == false){
									correctionSet = true;
									double[] result1 = new double[2], result2 = new double[2], result3 = new double[2], result4 = new double[2], result5 = new double[2], result6 = new double[2];
									double longitudeMiddle = minLongitude + (maxLongitude - minLongitude)/2;
									// convert the bounds into meters
									// all combinations to surely get min and max
									WGS84toUTM(result1, maxLongitude, minLatitude, false, longitudeMiddle, false);	
									
									WGS84toUTM(result2, minLongitude, maxLatitude, false, longitudeMiddle, false); //error
									WGS84toUTM(result3, minLongitude, minLatitude, false, longitudeMiddle, false);
									WGS84toUTM(result4, maxLongitude, maxLatitude, false, longitudeMiddle, false); //error
									// the min and max values of the height of the map can be in the middle of the sector because of the projection!
									WGS84toUTM(result5, longitudeMiddle, minLatitude, false, longitudeMiddle, false);
									WGS84toUTM(result6, longitudeMiddle, maxLatitude, false, longitudeMiddle, false); //error
									double leftBound = Math.min(result2[0], result3[0]);
									double rightBound = Math.max(result1[0], result4[0]);
									double upperBound = Math.max(result2[1], result6[1]);
									double lowerBound = Math.min(result1[1], result5[1]);
									int width = (int)Math.round((rightBound - leftBound + 1000)* 100);		//set the size a little bit bigger so that we have 500m on each side for spare
									int height = (int)Math.round((upperBound - lowerBound + 1000)* 100);
									VanetSimStart.setProgressBar(false);
									CyclicBarrier barrier = new CyclicBarrier(2);
									new MapSizeDialog(width , height, 100000, 100000, barrier);
									try {
										barrier.await();
									} catch (Exception e2) {}									
									VanetSimStart.setProgressBar(true);
									int correctionX = (int)Math.round(leftBound - (Map.getInstance().getMapWidth() - width)/200) - 500;
									int correctionY = (int)Math.round(upperBound + (Map.getInstance().getMapHeight() - height)/200) + 500;
									OSMNode.setCorrections(longitudeMiddle, correctionX, correctionY);
								}
								streetName = ""; //$NON-NLS-1$
								streetType = "unknown";
								maxspeedSet = false;	// if value has been set by an explicit maxspeed-key then don't overwrite it based on streettype!
								onewaySet = false;		// if value has been set by an explicit oneway-key then don't overwrite it based on streettype!
								laneSet = false;
								isOneway = 0;
								isRoundabout = false;
								lanes = 1;
								displayColor = Color.WHITE;
								maxspeed = -1;
								wayPoints.clear();
								id = Integer.parseInt(childCrsr.getAttrValue("id")); //$NON-NLS-1$
								wayCursor = childCrsr.childElementCursor();
								while (wayCursor.getNext() != null){
									waytype = wayCursor.getLocalName().toLowerCase();
									if(waytype.equals("nd")){ //$NON-NLS-1$
										try{
											id = Integer.parseInt(wayCursor.getAttrValue("ref")); //$NON-NLS-1$
											wayPoints.add(id);
										} catch (Exception e) {}
									} else if(waytype.equals("tag")){ //$NON-NLS-1$
										key = wayCursor.getAttrValue("k").toLowerCase(); //$NON-NLS-1$
										value = wayCursor.getAttrValue("v"); //$NON-NLS-1$
										if((streetName.equals("") && key.equals("ref")) || key.equals("name")) streetName = value;		// streetname is taken from name field or if not present in ref //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
										else if (key.equals("highway")){		// see http://wiki.openstreetmap.org/index.php/Map_Features (link last visited: 22.08.08) for details on highway types! //$NON-NLS-1$
											streetType = value;
											
											if(value.equals("motorway")){ //$NON-NLS-1$
												if(onewaySet == false) isOneway = 1;
												if(laneSet == false) lanes = 2;		//motorways should always have at least two lanes!
												displayColor = new Color(117,146,185);	//blue						
												if(maxspeedSet == false){
													maxspeed = 130*100000/3600;
												}
											} else if(value.equals("motorway_link")){ //$NON-NLS-1$
												displayColor = new Color(117,146,185);	//blue
												if(maxspeedSet == false){
													maxspeed = 70*100000/3600;
												}													
											} else if(value.equals("trunk")){ //$NON-NLS-1$
												displayColor = new Color(116,194,116);	//green
												if(maxspeedSet == false){
													maxspeed = 110*100000/3600;
												}
											} else if(value.equals("trunk_link")){ //$NON-NLS-1$
												displayColor = new Color(116,194,116);	//green
												if(maxspeedSet == false){
													maxspeed = 70*100000/3600;
												}
											} else if(value.equals("primary")){ //$NON-NLS-1$
												displayColor = new Color(225,98,102);	//red
												if(maxspeedSet == false){
													maxspeed = 100*100000/3600;
												}
											} else if(value.equals("primary_link")){ //$NON-NLS-1$
												displayColor = new Color(225,98,102);	//red
												if(maxspeedSet == false){
													maxspeed = 70*100000/3600;
												}
											} else if(value.equals("secondary")){ //$NON-NLS-1$
												displayColor = new Color(253,184,100);	//orange
												if(maxspeedSet == false){
													maxspeed = 100*100000/3600;
												}
											} else if(value.equals("tertiary")){ //$NON-NLS-1$
												displayColor = new Color(252,249,105);	//yellow
												if(maxspeedSet == false){
													maxspeed = 90*100000/3600;
												}
											} else if(value.equals("road")){ //$NON-NLS-1$
												if(maxspeedSet == false){
													maxspeed = 70*100000/3600;
												}
											} else if(value.equals("unclassified")){ //$NON-NLS-1$
												if(maxspeedSet == false){
													maxspeed = 70*100000/3600;
												}
											} else if(value.equals("residential")){ //$NON-NLS-1$
												if(maxspeedSet == false){
													maxspeed = 30*100000/3600;
												}												
											} else if(value.equals("living_street") || value.equals("service")){ //$NON-NLS-1$ //$NON-NLS-2$
												if(maxspeedSet == false){
													maxspeed = 10*100000/3600;
												}
											} else if(value.equals("unsurfaced") || value.equals("track")){ //$NON-NLS-1$ //$NON-NLS-2$
												if(maxspeedSet == false){
													maxspeed = 2*100000/3600;
												}											
											} else {		// anything else...pedestrian or so
												if(maxspeedSet == false){
													maxspeed = -1;
												}
											}
										} else if (key.equals("network") && value.equals("BAB")){		// some motorways in Germany are markes as road with network=BAB set... //$NON-NLS-1$ //$NON-NLS-2$
											if(onewaySet == false) isOneway = 1;
											if(laneSet == false) lanes = 2;		//motorways should always have at least two lanes!
											displayColor = Color.BLUE;
											if(maxspeedSet == false){
												maxspeed = 120*100000/3600;
												maxspeedSet = true;
											}
										} else if (key.equals("tracktype") && maxspeedSet == false){	//see http://wiki.openstreetmap.org/index.php/Proposed_features/grade1-5 (link last visited: 22.09.2008) (all other tracks stay at 2km/h //$NON-NLS-1$
											if(value.equals("grade1") && maxspeed < 10*100000/3600){ //$NON-NLS-1$
												maxspeed = 10*100000/3600;		//grade1 is a paved track so 10km/h should be possible there
												maxspeedSet = true;
											}
											else if(value.equals("grade2") && maxspeed < 5*100000/3600){ //$NON-NLS-1$
												maxspeed = 5*100000/3600;	//grade2 is a track with gravel so 5km/h should be possible there
												maxspeedSet = true;
											}
										} else if (key.equals("maxspeed")){ //$NON-NLS-1$
											try{
												maxspeed = Integer.parseInt(value)*100000/3600;
												maxspeedSet = true;
											} catch (Exception e) {}
										} else if (key.equals("oneway")){	// A street is oneway when it mets several conditions like seen on http://wiki.openstreetmap.org/index.php/OSM_tags_for_routing (link last checked: 22.08.08) //$NON-NLS-1$
											onewaySet = true;
											if(value.equals("yes") || value.equals("true") || value.equals("1")) isOneway = 1; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											else if(value.equals("-1")) isOneway = -1;	//nodes are in opposite direction! //$NON-NLS-1$
											else isOneway = 0;
										} else if (key.equals("lanes")){ //$NON-NLS-1$
											try{
												lanes = Integer.parseInt(value);
												if(lanes == 0) lanes = 1;	//a street with no lane would be useless...
												laneSet = true;
											} catch (Exception e) {}
										} else if (key.equals("junction")){ //$NON-NLS-1$
											if(value.equals("roundabout")){ //$NON-NLS-1$
												isRoundabout = true;
											}
										}
									}
								}
								if(maxspeed > 0){		// if we don't have a maxspeed set previously it's probably not a street for cars
									wayPointsIterator = wayPoints.iterator();
									if(streetName.length() == 0){		// give it a number if it doesn't have a name yet! //$NON-NLS-1$
										streetName = "S " + i; //$NON-NLS-1$
										++i;
									}
									node = null;
									lastNode = null;
									while(wayPointsIterator.hasNext()){		// iterate through all waypoints and build streets out of them!
										id = wayPointsIterator.next();
										tmpNode = OSMNodes.get(id);
										if(tmpNode != null){
											node = tmpNode.getRealNode();
											node = map.addNode(node);		// lastNode was already added in previous iteration!
											if(lastNode != null){													
												map.addStreet(new Street(streetName, lastNode, node, streetType, isOneway, lanes, displayColor, map.getRegionOfPoint(node.getX(), node.getY()), maxspeed));
											}					
											lastNode = node;											
										}
									}
									if(isRoundabout){		//need to close roundabouts (connect last node to first node)!
										wayPointsIterator = wayPoints.iterator();
										if(wayPointsIterator.hasNext()){
											id = wayPointsIterator.next();
											tmpNode = OSMNodes.get(id);
											if(tmpNode != null){
												node = tmpNode.getRealNode();
												node = map.addNode(node);		// lastNode was already added above!
												if(lastNode != null){													
													map.addStreet(new Street(streetName, node, lastNode, streetType, isOneway, lanes, displayColor, map.getRegionOfPoint(lastNode.getX(), lastNode.getY()), maxspeed));
												}											
											}
										}
									}
								}
							} catch (Exception e) {ErrorLog.log(Messages.getString("OSM_Loader.errorParsingWay"), 5, Map.class.getName(), "loadOSM", e);} //$NON-NLS-1$ //$NON-NLS-2$
						}
					} else if (childtype.equals("area") || childtype.equals("relation") || childtype.equals("bounds")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						//not implemented as not really needed for our purposes
					} else ErrorLog.log(Messages.getString("OSM_Loader.unknownElement"), 3, Map.class.getName(), "loadOSM", null); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else ErrorLog.log(Messages.getString("OSM_Loader.wrongFileFormat"), 6, Map.class.getName(), "loadOSM", null);			 //$NON-NLS-1$ //$NON-NLS-2$

			for(OSMNode n: amenityNodes){
				Map.getInstance().addAmenityNode(n.getRealNode(), n.getAmenity_());
				
			}
			sr.close();
			filestream.close();
		} catch (Exception e) {ErrorLog.log(Messages.getString("OSM_Loader.errorLoading"), 7, Map.class.getName(), "loadOSM", e);}			 //$NON-NLS-1$ //$NON-NLS-2$
		map.signalMapLoaded();
		VanetSimStart.setProgressBar(false);
		ErrorLog.log(Messages.getString("OSM_Loader.loadingFinished"), 3, OSMLoader.class.getName(), "loadOSM", null);  //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Convert a WGS84 coordinate to the Universal Transverse Mercator coordinate system.
	 * 
	 * @param result			an array which can be used to store the results. The array must consist of at least two elements. The x coordinate will be stored in 
	 * 							<code>result[0]</code>, the y coordinate in <code>result[1]</code> (both have meters as scale)
	 * @param latitude			the latitude in the WGS84 system
	 * @param longitude			the longitude in the WGS84 system
	 * @param calculateZone		<code>true</code> if the zone should be calculated, <code>false</code> if you want to set your own <code>longitudeOrigin</code>
	 * @param longitudeMiddle	used to set your own longitude as the middle of the UTM zone (only used if <code>calculateZone=false</code>!). Note that using this gives results 
	 * 							which are not conforming to UTM anymore.<br>If you have multiple locations in normally different UTM zones you can use this to get correct distances between 
	 * 							all locations. The value for this variable should be the same for all your locations and it should also be near your other locations (ideally 
	 * 							the one in the middle of all).
	 * @param highPrecision		If some higher order terms for calculation should be used. Setting to <code>true</code> needs more performance but gives more precision in the 
	 * 							sub-millimeter-range.<br>
	 * 							Normally setting to <code>false</code> should be fine!
	 * 
	 * @return <code>true</code> if calculation succeeded, else <code>false</code> (error in your input!)
	 */
	public boolean WGS84toUTM(double[] result, double longitude, double latitude, boolean calculateZone, double longitudeMiddle, boolean highPrecision){
		if(result.length > 1){
			if (latitude < -80 || latitude > 84) return false;
			if (longitude == 180.0) longitude = -180.0;
			else if (longitude > 180.0 || longitude < -180) return false;

			double longitudeMiddleRad;
			//get zone if needed
			if(calculateZone){
				int longitudeZone = (int) Math.floor((longitude + 180.0) / 6.0) + 1;

				// For some parts of Norway and Svalbard the computed zone is wrong => correct it
				if (latitude >= 56.0 && latitude < 64.0 && longitude >= 3.0 && longitude < 12.0) longitudeZone = 32;
				if (latitude >= 72.0 && latitude < 84.0) {
					if (longitude >= 0.0 && longitude < 9.0) longitudeZone = 31;
					else if (longitude >= 9.0 && longitude < 21.0) longitudeZone = 33;
					else if (longitude >= 21.0 && longitude < 33.0) longitudeZone = 35;
					else if (longitude >= 33.0 && longitude < 42.0) longitudeZone = 37;
				}			
				longitudeMiddleRad = ((longitudeZone-1) * 6 - 177) * (Math.PI / 180.0);
			} else {
				longitudeMiddleRad = longitudeMiddle * (Math.PI / 180.0);
			}

			double latitudeRad = latitude * (Math.PI / 180.0);
			double longitudeRad = longitude * (Math.PI / 180.0);
			
			// Meridian distance calculation according to http://www.icsm.gov.au/gda/gdatm/gdav2.3.pdf (chapter 5, link last visited: 23.08.08)
			/*double m = E_RADIUS * ((1 - ECC_SQUARED / 4 - 3 * ECC_SQUARED * ECC_SQUARED / 64 - 5 * ECC_SQUARED * ECC_SQUARED * ECC_SQUARED / 256) * latitudeRad
		            - (3 * ECC_SQUARED / 8 + 3 * ECC_SQUARED * ECC_SQUARED / 32 + 45 * ECC_SQUARED * ECC_SQUARED * ECC_SQUARED / 1024) * Math.sin(2 * latitudeRad)
		            + (15 * ECC_SQUARED * ECC_SQUARED / 256 + 45 * ECC_SQUARED * ECC_SQUARED * ECC_SQUARED / 1024) * Math.sin(4 * latitudeRad) 
		            - (35 * ECC_SQUARED * ECC_SQUARED * ECC_SQUARED / 3072) * Math.sin(6 * latitudeRad));*/
			// quite long formula above => we use precomputed values to save some CPU power (absolutely same result, just inserted the constants into the formula)...
			double m = 6367449.145960818 * latitudeRad - 16038.508333152433 * Math.sin(2 * latitudeRad) + 16.832200728062137 * Math.sin(4 * latitudeRad) - 0.021800766212608933 * Math.sin(6 * latitudeRad);

			// simplified calculation according to http://www.mucl.de/~hharm/files/utm.pdf (page 10, link last visited: 23.08.08)  
			double latitudeSinus = Math.sin(latitudeRad);
			double latitudeTangens = Math.tan(latitudeRad);
			double latitudeCosinus = Math.cos(latitudeRad);
			double n = E_RADIUS / Math.sqrt(1 - ECC_SQUARED * latitudeSinus * latitudeSinus);
			double t = latitudeTangens * latitudeTangens;			
			double c = ECC2_SQUARED * latitudeCosinus * latitudeCosinus;			
			double a = latitudeCosinus * (longitudeRad - longitudeMiddleRad);	

			double g1=0;
			double g2=0;
			double g3=0;
			double g4=0;
			if(highPrecision){		// calculate higher order terms for sub-millimeter precision
				g1 = 13*c*c + 4*Math.pow(c,3.0) - 64*c*c - 24*Math.pow(c,3.0)*t;
				g2 = (61 - 479*t + 179*t*t - Math.pow(t,3.0)) * Math.pow(a, 7.0)/5040;
				g3 = 445*c*c + 324*Math.pow(c,3.0) - 680*c*c*t + 88*Math.pow(c,4.0) - 600*Math.pow(c,3.0)*t - 192*Math.pow(c,4.0)*t;
				g4 = (1385 - 3111*t + 543*t*t - Math.pow(t,3.0)) * Math.pow(a,8.0)/40320;
			}

			// precompute some terms (only for better readability)
			double term1 = 1 - t + c;
			double term2 = 5 - 18*t + t*t + 72*c - 58*ECC2_SQUARED + g1;
			result[0] = 0.9996 * n * (a + term1 * Math.pow(a, 3.0)/6 + term2 * Math.pow(a, 5.0)/120 + g2) + 500000.0;	//false easting of 500.000 meters

			term1 = 5 - t + 9*c + 4*c*c;
			term2 = 61 - (58 * t) + (t * t) + (600 * c) - (330 * ECC2_SQUARED) + g3;
			result[1] = 0.9996 * (m + n * latitudeTangens * (a*a/2 + term1 * Math.pow(a, 4.0)/24 + term2 * Math.pow(a, 6.0)/720) + g4);
			if (latitude < 0) result[1] = result[1] + 10000000.0;	// Adjustment for the southern hemisphere (false northing of 10.000.000 meters)
			return true;
		} else return false;
	}
}