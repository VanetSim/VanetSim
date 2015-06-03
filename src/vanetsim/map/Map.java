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
package vanetsim.map;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CyclicBarrier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.SMOutputFactory;
import org.codehaus.staxmate.in.SMInputCursor;
import org.codehaus.staxmate.out.SMOutputDocument;
import org.codehaus.staxmate.out.SMOutputElement;

import vanetsim.ErrorLog;
import vanetsim.VanetSimStart;
import vanetsim.gui.Renderer;
import vanetsim.gui.controlpanels.MapSizeDialog;
import vanetsim.gui.helpers.MouseClickManager;
import vanetsim.localization.Messages;
import vanetsim.routing.A_Star.A_Star_LookupTableFactory;
import vanetsim.scenario.Scenario;
import vanetsim.scenario.Vehicle;
import vanetsim.scenario.RSU;
import vanetsim.scenario.events.EventSpot;

/**
 * The map. The coordinate system is 2-dimensional with each axis allowing values from
 * 0 to Integer.MAXVALUE (2147483647). Negative values are not allowed. The scale is 1:1cm which means
 * that about 21474km x 21474km is the maximum size of a map (should be more than enough for all cases).
 * The map is divided into several rectangular {@link Region}s in order to improve performance. All
 * vehicles, nodes and streets are stored in these regions.
 * Because of the regions, for example rendering and distance calculations only need to be done
 * on a limited amount of vehicles/streets/nodes which helps handling large maps a lot.
 */
public final class Map{

	/** The only instance of this class (singleton). */
	private static final Map INSTANCE = new Map();
	
	/** The width of a single lane (3m). Used in various other places in this program! */
	public static final int LANE_WIDTH = 300;

	/** The width of the map in cm. */
	private int width_ = 0;

	/** The height of the map in cm. */
	private int height_ = 0;

	/** The width of a region in cm. */
	private int regionWidth_ = 0;

	/** The height of a region in cm. */
	private int regionHeight_ = 0;

	/** The amount of regions in x direction. */
	private int regionCountX_ = 0;

	/** The amount of regions in y direction. */
	private int regionCountY_ = 0;

	/** An array holding all {@link Region}s. */
	private Region[][] regions_ = null;

	/** A flag to signal if loading is ready. While loading is in progress, simulation and rendering is not possible. */
	private boolean ready_ = true;	

	/** A list for amenitys */
	private ArrayList<Node> amenityList_ = new ArrayList<Node>();
	
	/** the map name */
	private String mapName_ = "";
	/**
	 * Empty, private constructor in order to disable instancing.
	 */
	private Map() {
	}	

	/**
	 * Gets the single instance of this map.
	 * 
	 * @return single instance of this map
	 */
	public static Map getInstance(){
		return INSTANCE;
	}

	/**
	 * Initializes a new map.
	 * 
	 * @param width			the width
	 * @param height		the height
	 * @param regionWidth	the width of a region
	 * @param regionHeight	the height of a region
	 */
	public void initNewMap(int width, int height, int regionWidth, int regionHeight){
		int i, j;
		if(ready_ == true){
			ready_ = false;
			//cleanup!
			if(!Renderer.getInstance().isConsoleStart()){
				Scenario.getInstance().initNewScenario();	//stops the simulation thread so we don't need to do it here
				Scenario.getInstance().setReadyState(true);
			}
			
			A_Star_LookupTableFactory.clear();
			Node.resetNodeID();
			width_ = width;
			height_ = height;
			regionWidth_ = regionWidth;
			regionHeight_ = regionHeight;

			Renderer.getInstance().setMarkedStreet(null);
			Renderer.getInstance().setMarkedVehicle(null);
			Renderer.getInstance().setAttackerVehicle(null);
			Renderer.getInstance().setAttackedVehicle(null);

			
			if(!Renderer.getInstance().isConsoleStart())MouseClickManager.getInstance().cleanMarkings();

			// create the regions on the map
			regionCountX_ = width_/regionWidth_;
			if(width_%regionWidth_ > 0) ++regionCountX_;
			regionCountY_ = height_/regionHeight_;
			if(height_%regionHeight_ > 0) ++regionCountY_;
			regions_ = new Region[regionCountX_][regionCountY_];
			int upperboundary = 0, leftboundary = 0;
			for(i = 0; i < regionCountX_; ++i){
				for(j = 0; j < regionCountY_; ++j){
					regions_[i][j] = new Region(i,j, leftboundary, leftboundary + regionWidth_-1, upperboundary, upperboundary + regionHeight_-1);
					upperboundary += regionHeight_;
				}
				leftboundary += regionWidth_;
				upperboundary = 0;
			}
			Vehicle.setRegions(regions_);
			EventSpot.setRegions_(regions_);
			RSU.setRegions(regions_);
		} else {
			ErrorLog.log(Messages.getString("Map.mapLocked"), 7, getClass().getName(), "initNewMap", null); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * This function needs to be called to signal that the loading process of the map has finished.
	 */
	public void signalMapLoaded(){
		// optimize the ArrayLists in the regions in order to free wasted memory
		for(int i = 0; i < regionCountX_; ++i){
			for(int j = 0; j < regionCountY_; ++j){
				regions_[i][j].calculateJunctions();
			}
		}
		ready_ = true;
		if(!Renderer.getInstance().isConsoleStart()){
			Renderer.getInstance().setMiddle(width_/2, height_/2);
			Renderer.getInstance().setMapZoom(Math.exp(5/100.0)/1000);
			Renderer.getInstance().ReRender(true, false);

		}

		//start a thread which calculates bridges in background so that loading is faster (it's just eyecandy and not necessary otherwise ;))
		Runnable job = new Runnable() {
			public void run(){
				for(int i = 0; i < regionCountX_; ++i){
					for(int j = 0; j < regionCountY_; ++j){
						regions_[i][j].checkStreetsForBridges();
					}
				}
			}
		};
		Thread t = new Thread(job);
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * Load a map.
	 * 
	 * @param file	the file to load
	 * @param zip	<code>true</code> if the file given is zipped, else <code>false</code>
	 */
	public void load(File file, boolean zip){
		try{
			if(!Renderer.getInstance().isConsoleStart())VanetSimStart.setProgressBar(true);
			String childtype, setting, streetName, streetType, trafficSignalException, amenity ="";
			int x = 0, y = 0, maxSpeed, isOneway, lanes, newMapWidth, newMapHeight, newRegionWidth, newRegionHeight;
			Color displayColor;
			boolean xSet, ySet, trafficSignal;
			Node startNode, endNode;
			SMInputCursor childCrsr, nodeCrsr, settingsCrsr, streetCrsr, streetCrsr2, amenityCrsr, amenityCrsr2;
			XMLInputFactory factory = XMLInputFactory.newInstance();

			mapName_ = file.getName();
			
			ErrorLog.log(Messages.getString("Map.loadingMap") + file.getName(), 3, getClass().getName(), "loadMap", null); //$NON-NLS-1$ //$NON-NLS-2$
			// configure some factory options...
			factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
			factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
			factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
			factory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);

			InputStream filestream;
			if(zip){
				filestream = new ZipInputStream(new FileInputStream(file));
				((ZipInputStream) filestream).getNextEntry();
			} else filestream = new FileInputStream(file);
			XMLStreamReader sr = factory.createXMLStreamReader(filestream);	
			SMInputCursor rootCrsr = SMInputFactory.rootElementCursor(sr);
			rootCrsr.getNext();
			if(rootCrsr.getLocalName().toLowerCase().equals("map")){ //$NON-NLS-1$
				childCrsr = rootCrsr.childElementCursor();
				childCrsr.getNext();
				if(childCrsr.getLocalName().toLowerCase().equals("settings")){		// The settings section must be present! //$NON-NLS-1$
					newMapWidth = 0;
					newMapHeight = 0;
					newRegionWidth = 0;
					newRegionHeight = 0;
					settingsCrsr = childCrsr.childElementCursor();
					while (settingsCrsr.getNext() != null){		//parse general settings (size of regions and map)
						setting = settingsCrsr.getLocalName().toLowerCase();
						if(setting.equals("map_height")){ //$NON-NLS-1$
							try{
								newMapHeight = Integer.parseInt(settingsCrsr.collectDescendantText(false));
							} catch (Exception e) {}
						} else if(setting.equals("map_width")){ //$NON-NLS-1$
							try{
								newMapWidth = Integer.parseInt(settingsCrsr.collectDescendantText(false));
							} catch (Exception e) {}
						} else if(setting.equals("region_height")){ //$NON-NLS-1$
							try{
								newRegionWidth = Integer.parseInt(settingsCrsr.collectDescendantText(false));
							} catch (Exception e) {}
						} else if(setting.equals("region_width")){ //$NON-NLS-1$
							try{
								newRegionHeight = Integer.parseInt(settingsCrsr.collectDescendantText(false));
							} catch (Exception e) {}
						}
					}
					if(newMapWidth > 0 && newMapHeight > 0 && newRegionWidth > 0 && newRegionHeight > 0){		// only continue if settings were all found
						if(!Renderer.getInstance().isConsoleStart())VanetSimStart.setProgressBar(false);
						CyclicBarrier barrier = new CyclicBarrier(2);
						if(!Renderer.getInstance().isConsoleStart()){						
							new MapSizeDialog(newMapWidth, newMapHeight, newRegionWidth, newRegionHeight, barrier);	//initialize new map
						}
						else Map.getInstance().initNewMap(newMapWidth, newMapHeight, newRegionWidth, newRegionHeight);
						int addX = (width_ - newMapWidth)/2;
						int addY = (height_ - newMapHeight)/2;
						if(!Renderer.getInstance().isConsoleStart())VanetSimStart.setProgressBar(true);
						while (childCrsr.getNext() != null){
							if(childCrsr.getLocalName().toLowerCase().equals("streets")){ //$NON-NLS-1$
								streetCrsr = childCrsr.childElementCursor();
								while (streetCrsr.getNext() != null){
									if(streetCrsr.getLocalName().toLowerCase().equals("street")){ //$NON-NLS-1$
										streetName = ""; //$NON-NLS-1$
										startNode = null;
										endNode = null;
										trafficSignal = false;
										streetType = "unkown";
										lanes = 0;
										isOneway = 0;
										maxSpeed = 0;
										trafficSignalException = "";
										displayColor = null;
										streetCrsr2 = streetCrsr.childElementCursor();
										while (streetCrsr2.getNext() != null){
											childtype = streetCrsr2.getLocalName().toLowerCase();
											if(childtype.equals("name")){ //$NON-NLS-1$
												streetName = streetCrsr2.collectDescendantText(true);
											} else if(childtype.equals("startnode") || childtype.equals("endnode")){ //$NON-NLS-1$ //$NON-NLS-2$
												xSet = false;
												ySet = false;
												nodeCrsr = streetCrsr2.childElementCursor();
												while (nodeCrsr.getNext() != null){
													if(nodeCrsr.getLocalName().toLowerCase().equals("x")){ //$NON-NLS-1$
														try{
															x = Integer.parseInt(nodeCrsr.collectDescendantText(false)) + addX;
															xSet = true;
														} catch (Exception e) {}														
													} else if(nodeCrsr.getLocalName().toLowerCase().equals("y")){ //$NON-NLS-1$
														try{
															y = Integer.parseInt(nodeCrsr.collectDescendantText(false)) + addY;
															ySet = true;
														} catch (Exception e) {}
													} else if(nodeCrsr.getLocalName().toLowerCase().equals("trafficsignal")){ //$NON-NLS-1$
														if(nodeCrsr.collectDescendantText(false).toLowerCase().equals("true")) trafficSignal = true;
														else trafficSignal = false;
													} else if(nodeCrsr.getLocalName().toLowerCase().equals("trafficsignalexceptions")){ //$NON-NLS-1$
														trafficSignalException = nodeCrsr.collectDescendantText(true);
													}	
												}
												if(xSet && ySet){
													if(childtype.equals("startnode")){
														startNode = new Node(x, y, trafficSignal); //$NON-NLS-1$
														if(!trafficSignalException.equals(""))startNode.addSignalExceptionsOfString(trafficSignalException);
													}
													else {
														endNode = new Node(x, y, trafficSignal);
														if(!trafficSignalException.equals(""))endNode.addSignalExceptionsOfString(trafficSignalException);
													}
												}
											} else if(childtype.equals("oneway")){ //$NON-NLS-1$
												if(streetCrsr2.collectDescendantText(false).toLowerCase().equals("true")) isOneway = 1; //$NON-NLS-1$
												else isOneway = 0;
											} else if(childtype.equals("streettype")){ //$NON-NLS-1$
												streetType = streetCrsr2.collectDescendantText(true);
											} else if(childtype.equals("lanes")){ //$NON-NLS-1$
												try{
													lanes = Integer.parseInt(streetCrsr2.collectDescendantText(false));
												} catch (Exception e) {}
											} else if(childtype.equals("speed")){ //$NON-NLS-1$
												try{
													maxSpeed = Integer.parseInt(streetCrsr2.collectDescendantText(false));
												} catch (Exception e) {}
											} else if(childtype.equals("color")){ //$NON-NLS-1$
												try{
													displayColor = new Color(Integer.parseInt(streetCrsr2.collectDescendantText(false)));
												} catch (Exception e) {}
											} else ErrorLog.log(Messages.getString("Map.unknownElement"), 3, getClass().getName(), "load", null); //$NON-NLS-1$ //$NON-NLS-2$
										}
										if(maxSpeed > 0 && startNode != null && endNode != null && displayColor != null && !streetName.equals("")){ //$NON-NLS-1$
											startNode = addNode(startNode);
											endNode = addNode(endNode);
											addStreet(new Street(streetName, startNode, endNode, streetType, isOneway, lanes, displayColor, getRegionOfPoint(startNode.getX(), startNode.getY()), maxSpeed));
										}
									} else ErrorLog.log(Messages.getString("Map.unknownElementOnlyStreet"), 3, getClass().getName(), "load", null); //$NON-NLS-1$ //$NON-NLS-2$
								}
							} //else ErrorLog.log(Messages.getString("Map.unknownElementOnlyStreets"), 3, getClass().getName(), "load", null); //$NON-NLS-1$ //$NON-NLS-2$
							else if(childCrsr.getLocalName().toLowerCase().equals("amenities")){ //$NON-NLS-1$
								amenityCrsr = childCrsr.childElementCursor();
								while (amenityCrsr.getNext() != null){
									if(amenityCrsr.getLocalName().toLowerCase().equals("amenity")){ //$NON-NLS-1$
										x = 0; //$NON-NLS-1$
										y = 0;
										amenity = "";
										amenityCrsr2 = amenityCrsr.childElementCursor();
										while (amenityCrsr2.getNext() != null){
											childtype = amenityCrsr2.getLocalName().toLowerCase();
											if(childtype.equals("x")){ //$NON-NLS-1$
												x = Integer.parseInt(amenityCrsr2.collectDescendantText(false));
											}
											else if(childtype.equals("y")){ //$NON-NLS-1$
												y = Integer.parseInt(amenityCrsr2.collectDescendantText(false));
											} 
											else if(childtype.equals("amenity")){ //$NON-NLS-1$
												amenity = amenityCrsr2.collectDescendantText(true);
											} 
											else ErrorLog.log(Messages.getString("Map.unknownElement"), 3, getClass().getName(), "load", null); //$NON-NLS-1$ //$NON-NLS-2$
										}
									} else ErrorLog.log(Messages.getString("Map.unknownElementOnlyAmenity"), 3, getClass().getName(), "load", null); //$NON-NLS-1$ //$NON-NLS-2$
									addAmenityNode(new Node(x,y,false), amenity);
								}
							} else ErrorLog.log(Messages.getString("Map.unknownElementOnlyAmenitiesOrStreets"), 3, getClass().getName(), "load", null); //$NON-NLS-1$ //$NON-NLS-2$
						}
					} else ErrorLog.log(Messages.getString("Map.settingsIncomplete"), 7, getClass().getName(), "load", null); //$NON-NLS-1$ //$NON-NLS-2$
				} else ErrorLog.log(Messages.getString("Map.settingsMissing"), 7, getClass().getName(), "load", null); //$NON-NLS-1$ //$NON-NLS-2$
			} else ErrorLog.log(Messages.getString("Map.wrongRoot"), 7, getClass().getName(), "load", null); //$NON-NLS-1$ //$NON-NLS-2$

			sr.close();
			filestream.close();
		} catch (Exception e) {ErrorLog.log(Messages.getString("Map.errorLoading"), 7, getClass().getName(), "load", e);} //$NON-NLS-1$ //$NON-NLS-2$
		if(!Renderer.getInstance().isConsoleStart())VanetSimStart.setProgressBar(false);
		signalMapLoaded();
		ErrorLog.log(Messages.getString("Map.loadingFinished"), 3, getClass().getName(), "load", null); //$NON-NLS-1$ //$NON-NLS-2$	
	}


	/**
	 * Save the map.
	 * 
	 * @param file	the file in which to save
	 * @param zip	if <code>true</code>, file is saved in a compressed zip file (extension .zip is added to <code>file</code>!). If <code>false</code>, no compression is made.
	 */
	public void save(File file, boolean zip){
		try{
			if(!Renderer.getInstance().isConsoleStart())VanetSimStart.setProgressBar(true);
			ErrorLog.log(Messages.getString("Map.savingMap") + file.getName(), 3, getClass().getName(), "save", null); //$NON-NLS-1$ //$NON-NLS-2$
			int i, j, k;
			Street[] streetsArray;
			Street street;
			SMOutputElement level1, level2;

			OutputStream filestream;
			if(zip){
				filestream = new ZipOutputStream(new FileOutputStream(file + ".zip")); //$NON-NLS-1$
				((ZipOutputStream) filestream).putNextEntry(new ZipEntry(file.getName()));
			} else filestream = new FileOutputStream(file);
			XMLStreamWriter xw = XMLOutputFactory.newInstance().createXMLStreamWriter(filestream);
			SMOutputDocument doc = SMOutputFactory.createOutputDocument(xw);
			doc.setIndentation("\n\t\t\t\t\t\t\t\t", 2, 1); ;  //$NON-NLS-1$
			doc.addComment("Generated on " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date())); //$NON-NLS-1$ //$NON-NLS-2$
			doc.addComment("This file may contain data from the OpenStreetMap project which is licensed under the Creative Commons Attribution-ShareAlike 2.0 license."); //$NON-NLS-1$

			SMOutputElement root = doc.addElement("Map");			 //$NON-NLS-1$
			level1 = root.addElement("Settings"); //$NON-NLS-1$
			level2 = level1.addElement("Map_height"); //$NON-NLS-1$
			level2.addValue(height_);
			level2 = level1.addElement("Map_width"); //$NON-NLS-1$
			level2.addValue(width_);
			level2 = level1.addElement("Region_height"); //$NON-NLS-1$
			level2.addValue(regionHeight_);
			level2 = level1.addElement("Region_width"); //$NON-NLS-1$
			level2.addValue(regionWidth_);
			SMOutputElement streets = root.addElement("Streets");			 //$NON-NLS-1$

			for(i = 0; i < regionCountX_; ++i){
				for(j = 0; j < regionCountY_; ++j){
					streetsArray = regions_[i][j].getStreets();
					for(k = 0; k < streetsArray.length; ++k){
						street = streetsArray[k];
						if(street.getMainRegion() == regions_[i][j]){	//as a street can be in multiple regions only output it in the "main" region!
							level1 = streets.addElement("Street"); //$NON-NLS-1$
							level1.addElement("Name").addCharacters(street.getName()); //$NON-NLS-1$							
							level2 = level1.addElement("StartNode"); //$NON-NLS-1$
							level2.addElement("x").addValue(street.getStartNode().getX());; //$NON-NLS-1$
							level2.addElement("y").addValue(street.getStartNode().getY()); //$NON-NLS-1$
							if(street.getStartNode().isHasTrafficSignal_()) {
								level2.addElement("trafficSignal").addCharacters("true");
								if(street.getStartNode().hasNonDefaultSettings()) level2.addElement("TrafficSignalExceptions").addCharacters(street.getStartNode().getSignalExceptionsInString());
							}
							else level2.addElement("trafficSignal").addCharacters("false");
							level2 = level1.addElement("EndNode"); //$NON-NLS-1$
							level2.addElement("x").addValue(street.getEndNode().getX()); //$NON-NLS-1$
							level2.addElement("y").addValue(street.getEndNode().getY()); //$NON-NLS-1$
							if(street.getEndNode().isHasTrafficSignal_()) {
								level2.addElement("trafficSignal").addCharacters("true");
								if(street.getEndNode().hasNonDefaultSettings()) level2.addElement("TrafficSignalExceptions").addCharacters(street.getEndNode().getSignalExceptionsInString());
							}
							else level2.addElement("trafficSignal").addCharacters("false");
							if(street.isOneway()) level1.addElement("Oneway").addCharacters("true"); //$NON-NLS-1$ //$NON-NLS-2$
							else level1.addElement("Oneway").addCharacters("false"); //$NON-NLS-1$ //$NON-NLS-2$
							level1.addElement("StreetType").addCharacters(street.getStreetType_()); //$NON-NLS-1$
							level1.addElement("Lanes").addValue(street.getLanesCount()); //$NON-NLS-1$
							level1.addElement("Speed").addValue(street.getSpeed()); //$NON-NLS-1$
							level1.addElement("Color").addValue(street.getDisplayColor().getRGB()); //$NON-NLS-1$
						}
					}
				}
			}
			
			SMOutputElement amenities = root.addElement("Amenities"); //$NON-NLS-1$

			//save the list of amenities (e.g. schools, hospitals...)
			for(Node n: amenityList_){
				level1 = amenities.addElement("Amenity"); //$NON-NLS-1$
				level1.addElement("x").addValue(n.getX()); //$NON-NLS-1$
				level1.addElement("y").addValue(n.getY()); //$NON-NLS-1$
				level1.addElement("amenity").addCharacters(n.getAmenity_());
			}
			
			doc.closeRoot();
			xw.close();
			filestream.close();
		}catch (Exception e) {ErrorLog.log(Messages.getString("Map.errorSavingMap") , 6, getClass().getName(), "save", e);} //$NON-NLS-1$ //$NON-NLS-2$
		if(!Renderer.getInstance().isConsoleStart())VanetSimStart.setProgressBar(false);
	}

	/**
	 * Add a new node to the correct region. A node can only be in one region.
	 * 
	 * @param node	the node to add
	 * 
	 * @return the added node (might be different if already existing!)
	 */
	public Node addNode(Node node){
		int regionX = node.getX()/regionWidth_;	//implicit rounding (=floor)because of integer values!
		int regionY = node.getY()/regionHeight_;

		// to prevent "array out of bounds" when node is outside of map
		if (regionX >= regionCountX_) regionX = regionCountX_ - 1;
		else if(regionX < 0) regionX = 0;
		if (regionY >= regionCountY_) regionY = regionCountY_ - 1;
		else if (regionY < 0) regionY = 0;

		node.setRegion(regions_[regionX][regionY]);
		return regions_[regionX][regionY].addNode(node, true);
	}

	/**
	 * Delete a node from it's region.
	 * 
	 * @param node	the node to remove
	 */
	public void delNode(Node node){
		node.getRegion().delNode(node);
	}

	/**
	 * Add a new Road-Side-Unit to the correct region. A RSU can only be in one region.
	 * 
	 * @param rsu	the RSU to add
	 * 
	 */
	public void addRSU(RSU rsu){
		int regionX = rsu.getX()/regionWidth_;	//implicit rounding (=floor)because of integer values!
		int regionY = rsu.getY()/regionHeight_;

		// to prevent "array out of bounds" when RSU is outside of map
		if (regionX >= regionCountX_) regionX = regionCountX_ - 1;
		else if(regionX < 0) regionX = 0;
		if (regionY >= regionCountY_) regionY = regionCountY_ - 1;
		else if (regionY < 0) regionY = 0;

		rsu.setRegion(regions_[regionX][regionY]);
		regions_[regionX][regionY].addRSU(rsu);
	}

	/**
	 * Delete a Road-Side-Unit from it's region.
	 * 
	 * @param x  the x coordinate of the rsu
	 * @param y  the y coordinate of the rsu
	 */
	public void delRSU(int x, int y){
		// Get the nearest RSU
		RSU rsu = null;
		
		Region[][] tmpRegions = Map.getInstance().getRegions();
			
		int regionCountX = Map.getInstance().getRegionCountX();
		int regionCountY = Map.getInstance().getRegionCountY();
		int radius = 250;
			
		for(int i = 0; i <= regionCountX-1;i++){
			for(int j = 0; j <= regionCountY-1;j++){
				Region tmpRegion = tmpRegions[i][j];
					
				RSU[] rsus = tmpRegion.getRSUs();
					
				for(int k = 0;k < rsus.length; k++){
					if((x + radius) > rsus[k].getX() && (x - radius) < rsus[k].getX() && (y + radius) > rsus[k].getY() && (y - radius) < rsus[k].getY()){
						rsu = rsus[k];
					}
				}
			}
		}
		
		//if RSU was found, delete it
		if(rsu != null) rsu.getRegion().delRSU(rsu);
	}
	
	/**
	 * Add a new vehicle to the correct start region. A vehicle can only be in one region and may move to other regions during 
	 * the simulation. Although a vehicle is part of a scenario, it's added here as the map knows everything about how to
	 * correctly put it into a region.
	 * 
	 * @param vehicle	the vehicle to add
	 */
	public void addVehicle(Vehicle vehicle){
		int regionX = vehicle.getX()/regionWidth_;	//implicit rounding (=floor)because of integer values!
		int regionY = vehicle.getY()/regionHeight_;

		// to prevent "array out of bounds" when vehicle is outside of map
		if (regionX >= regionCountX_) regionX = regionCountX_ - 1;
		else if(regionX < 0) regionX = 0;
		if (regionY >= regionCountY_) regionY = regionCountY_ - 1;
		else if (regionY < 0) regionY = 0;

		vehicle.setRegion(regions_[regionX][regionY]);
		regions_[regionX][regionY].addVehicle(vehicle, false);
	}

	/**
	 * Deletes a vehicle from it's region.
	 * 
	 * @param vehicle	the vehicle to delete
	 */
	public void delVehicle(Vehicle vehicle){		
		int regionX = vehicle.getRegionX();
		int regionY = vehicle.getRegionY();
		regions_[regionX][regionY].delVehicle(vehicle);
	}

	/**
	 * Add a new street to the correct region(s). Note that a street can be in multiple regions and so we must determine
	 * all here! This makes rendering and calculations a lot easier later!
	 * 
	 * @param street	the street to add
	 */
	public void addStreet(Street street){
		int startRegionX = street.getStartNode().getRegion().getX();
		int startRegionY = street.getStartNode().getRegion().getY();
		int endRegionX = street.getEndNode().getRegion().getX();
		int endRegionY = street.getEndNode().getRegion().getY();
		int i;

		// find the regions in which this street belongs!
		if(startRegionX == endRegionX){
			if(startRegionY == endRegionY) regions_[startRegionX][startRegionY].addStreet(street, true);		// just in one region
			else{	// above or beneath
				if(startRegionY < endRegionY){
					for (i = startRegionY; i <= endRegionY; ++i) regions_[startRegionX][i].addStreet(street, true);
				} else {
					for (i = endRegionY; i <= startRegionY; ++i) regions_[startRegionX][i].addStreet(street, true);
				}
			}
		} else if(startRegionY == endRegionY){
			if(startRegionX < endRegionX){	// left or right
				for (i = startRegionX; i <= endRegionX; ++i) regions_[i][startRegionY].addStreet(street, true);
			} else {
				for (i = endRegionX; i <= startRegionX; ++i) regions_[i][startRegionY].addStreet(street, true);
			}
		} else{		// seems to be non-trivial crossing regions, try some kind of bruteforce now!
			// we now need the real coordinates and not just the regions!
			int start_x = street.getStartNode().getX();
			int start_y = street.getStartNode().getY();
			int end_x = street.getEndNode().getX();
			int end_y = street.getEndNode().getY();

			regions_[startRegionX][startRegionY].addStreet(street, true);
			regions_[endRegionX][endRegionY].addStreet(street, true);

			// calculate line parameters: y = ax + b
			double a = ((double)start_y - end_y) / ((double)start_x - end_x);	// (start_x - end_x) can't be zero because then (start_region_x == end_region_x) above would have been true!
			double b = start_y - a * start_x;

			double x, y;
			long tmp;

			int max_x = Math.max(endRegionX, startRegionX);		//cache so that the math-function isn't called too often
			int max_y = Math.max(startRegionY, endRegionY);
			for(i = Math.min(startRegionX, endRegionX); i < max_x; ++i){	// check all vertical grid lines of the regions to be considered
				y = a * (i * regionWidth_) + b;	// left side of this grid
				tmp = Math.round(y) / regionHeight_;
				if(tmp > -1 && tmp < regionCountY_) regions_[i][(int)tmp].addStreet(street, true);
				y = a * ((i * regionWidth_) + regionWidth_ - 1) + b;	// right side of this grid
				tmp = Math.round(y) / regionHeight_;
				if(tmp > -1 && tmp < regionCountY_) regions_[i][(int)tmp].addStreet(street, true);
			}
			for(i = Math.min(startRegionY, endRegionY); i < max_y; ++i){	// check all horizontal grid lines of the regions to be considered
				x = ((i * regionHeight_) - b)/ a;		// upper side of this grid
				tmp = Math.round(x) / regionWidth_;
				if(tmp > -1 && tmp < regionCountX_) regions_[(int)tmp][i].addStreet(street, true);
				x = (((i * regionHeight_) + regionHeight_ - 1) - b)/ a;	// lower side of this grid
				tmp = Math.round(x) / regionWidth_;
				if(tmp > -1 && tmp < regionCountX_) regions_[(int)tmp][i].addStreet(street, true);
			}
		}
	}


	/**
	 * Delete a street from all region(s) it's in.
	 * 
	 * @param street	the street to delete
	 */
	public void delStreet(Street street){
		int startRegionX = street.getStartNode().getRegion().getX();
		int startRegionY = street.getStartNode().getRegion().getY();
		int endRegionX = street.getEndNode().getRegion().getX();
		int endRegionY = street.getEndNode().getRegion().getY();
		int i;

		// find the regions in which this street belongs!
		if(startRegionX == endRegionX){
			if(startRegionY == endRegionY) regions_[startRegionX][startRegionY].delStreet(street);		// just in one region
			else{	// above or beneath
				if(startRegionY < endRegionY){
					for (i = startRegionY; i <= endRegionY; ++i) regions_[startRegionX][i].delStreet(street);
				} else {
					for (i = endRegionY; i <= startRegionY; ++i) regions_[startRegionX][i].delStreet(street);
				}
			}
		} else if(startRegionY == endRegionY){
			if(startRegionX < endRegionX){	// left or right
				for (i = startRegionX; i <= endRegionX; ++i) regions_[i][startRegionY].delStreet(street);
			} else {
				for (i = endRegionX; i <= startRegionX; ++i) regions_[i][startRegionY].delStreet(street);
			}
		} else{		// seems to be non-trivial crossing regions, try some kind of bruteforce now!
			// we now need the real coordinates and not just the regions!
			int start_x = street.getStartNode().getX();
			int start_y = street.getStartNode().getY();
			int end_x = street.getEndNode().getX();
			int end_y = street.getEndNode().getY();

			regions_[startRegionX][startRegionY].delStreet(street);
			regions_[endRegionX][endRegionY].delStreet(street);

			// calculate line parameters: y = ax + b
			double a = ((double)start_y - end_y) / ((double)start_x - end_x);	// (start_x - end_x) can't be zero because then (start_region_x == end_region_x) above would have been true!
			double b = start_y - a * start_x;

			double x, y;
			long tmp;

			int max_x = Math.max(endRegionX, startRegionX);		//cache so that the math-function isn't called too often
			int max_y = Math.max(startRegionY, endRegionY);
			for(i = Math.min(startRegionX, endRegionX); i < max_x; ++i){	// check all vertical grid lines of the regions to be considered
				y = a * (i * regionWidth_) + b;	// left side of this grid
				tmp = Math.round(y) / regionHeight_;
				if(tmp > -1 && tmp < regionCountY_) regions_[i][(int)tmp].delStreet(street);
				y = a * ((i * regionWidth_) + regionWidth_ - 1) + b;	// right side of this grid
				tmp = Math.round(y) / regionHeight_;
				if(tmp > -1 && tmp < regionCountY_) regions_[i][(int)tmp].delStreet(street);
			}
			for(i = Math.min(startRegionY, endRegionY); i < max_y; ++i){	// check all horizontal grid lines of the regions to be considered
				x = ((i * regionHeight_) - b)/ a;		// upper side of this grid
				tmp = Math.round(x) / regionWidth_;
				if(tmp > -1 && tmp < regionCountX_) regions_[(int)tmp][i].delStreet(street);
				x = (((i * regionHeight_) + regionHeight_ - 1) - b)/ a;	// lower side of this grid
				tmp = Math.round(x) / regionWidth_;
				if(tmp > -1 && tmp < regionCountX_) regions_[(int)tmp][i].delStreet(street);
			}
		}
	}


	/**
	 * Add a new mix zone to the correct region. A mix zone can only be in one region.
	 * 
	 * @param node	the mix zone node to add
	 * @param radius	the mix zone radius
	 */
	public void addMixZone(Node node, int radius){
		int regionX = node.getX()/regionWidth_;	//implicit rounding (=floor)because of integer values!
		int regionY = node.getY()/regionHeight_;
		regions_[regionX][regionY].addMixZone(node, radius);
	}
	
	/**
	 * Delete a mix zone in the correct region.
	 */
	public void deleteMixZone(Node node){
		int regionX = node.getX()/regionWidth_;	//implicit rounding (=floor)because of integer values!
		int regionY = node.getY()/regionHeight_;

		regions_[regionX][regionY].deleteMixZone(node);
	}
	
	/**
	 * Delete every Mix Zone on this map
	 */
	public void clearMixZones(){
		for(int i = 0; i < regionCountX_; ++i) for(int j = 0; j < regionCountY_; ++j) regions_[i][j].clearMixZones();
	}
	
	/**
	 * Delete every RSU on this map
	 */
	public void clearRSUs(){
		for(int i = 0; i < regionCountX_; ++i) for(int j = 0; j < regionCountY_; ++j) regions_[i][j].clearRSUs();
	}
	
	/**
	 * Delete every Vehicle on this map
	 */
	public void clearVehicles(){
		Renderer.getInstance().setMarkedVehicle(null);
		for(int i = 0; i < regionCountX_; ++i) for(int j = 0; j < regionCountY_; ++j) regions_[i][j].cleanVehicles();
	}
	
	/**
	 * Function to trim a map. If no coordinates are chosen the map will be trimmed to its borders (Only empty space will be removed). Otherwise the map will be trimmed according
	 * to the coordinates.
	 * 
	 * @param minX	the minimum x coordinate
	 * @param maxX	the maximum x coordinate
	 * @param minY	the minimum y coordinate
	 * @param maxY	the maximum y coordinate
	 */
	@SuppressWarnings("unchecked")
	public void autoTrimMap(int minX, int minY, int maxX, int maxY){
		Node[] tmpNodes = null;
		Street[] tmpStreets = null;
		
		boolean autoTrim = false;
		
		if(minX == -1 && minY == -1 && maxX == -1 && maxY == -1) autoTrim = true;
		
		//no coordinates where choosen, just trim the map
		if(autoTrim){
			minX = width_;
			minY = height_;
			
			maxX = 0;
			maxY = 0;
			
			int tmpX = 0;
			int tmpY = 0;
			

			
			//find min x and min y
			for(int i = 0; i < regions_.length; i++){
				for(int j = 0; j < regions_[i].length; j++){
					tmpNodes = regions_[i][j].getNodes();				
					for(int k = 0; k < tmpNodes.length; k++){
						tmpX = tmpNodes[k].getX();
						tmpY = tmpNodes[k].getY();
						
						if(tmpX < minX) minX = tmpX;
						if(tmpY < minY) minY = tmpY;
						
						if(tmpX > maxX) maxX = tmpX;
						if(tmpY > maxY) maxY = tmpY;
					}
				}
			}
		}
		//coordinates where chosen, trim according to coordinates
		else{
			Node tmpStartNode = null;
			Node tmpEndNode = null;
			//we probably have to trim some streets
			for(int i = 0; i < regions_.length; i++){
				for(int j = 0; j < regions_[i].length; j++){
					tmpStreets = regions_[i][j].getStreets();
					for(int k = 0; k < tmpStreets.length; k++){				
						tmpStartNode = tmpStreets[k].getStartNode();
						tmpEndNode = tmpStreets[k].getEndNode();
						
						//both nodes are outside of the array. Delete them
						if(((tmpStartNode.getX() < minX || tmpStartNode.getY() < minY) && 
								(tmpEndNode.getX() < minX || tmpEndNode.getY() < minY)) ||
								((tmpStartNode.getX() > maxX || tmpStartNode.getY() > maxY) && 
										(tmpEndNode.getX() > maxX || tmpEndNode.getY() > maxY))) {
							tmpStreets[k].getStartNode().delOutgoingStreet(tmpStreets[k]);
							tmpStreets[k].getStartNode().delCrossingStreet(tmpStreets[k]);
							if(tmpStreets[k].getStartNode().getCrossingStreetsCount()==0) Map.getInstance().delNode(tmpStreets[k].getStartNode());
							tmpStreets[k].getEndNode().delOutgoingStreet(tmpStreets[k]);
							tmpStreets[k].getEndNode().delCrossingStreet(tmpStreets[k]);
							if(tmpStreets[k].getEndNode().getCrossingStreetsCount()==0) Map.getInstance().delNode(tmpStreets[k].getEndNode());
							Map.getInstance().delStreet(tmpStreets[k]);
						}
						//the start node is outside the area
						else if	(tmpStartNode.getY() < minY ||  tmpStartNode.getY() > maxY || tmpEndNode.getY() < minY || tmpEndNode.getY() > maxY ||
								tmpStartNode.getX() < minX || tmpEndNode.getX() < minX || tmpStartNode.getX() > maxX || tmpEndNode.getX() > maxX){
							
							int tmpBorder;
							Node tmp = null;
							
							if (tmpStartNode.getY() < minY || tmpEndNode.getY() < minY) tmpBorder = minY;
							else if (tmpStartNode.getY() > maxY || tmpEndNode.getY() > maxY) tmpBorder = maxY;
							else if (tmpStartNode.getX() < minX || tmpEndNode.getX() < minX) tmpBorder = minX;
							else tmpBorder = maxX;
							
							if(tmpEndNode.getY() < minY || tmpEndNode.getY() > maxY || tmpEndNode.getX() < minX || tmpEndNode.getX() > maxX){
								tmp = tmpStartNode;
								tmpStartNode = tmpEndNode;
								tmpEndNode = tmp;
							}
							
							
							Node tmpNode = new Node(tmpStartNode.getX(), tmpStartNode.getY());
							tmpNode.addOutgoingStreet(tmpStreets[k]);
							tmpNode.addCrossingStreet(tmpStreets[k]);
							
							tmpStartNode.delCrossingStreet(tmpStreets[k]);
							tmpStartNode.delOutgoingStreet(tmpStreets[k]);
							
							
							regions_[i][j].delNode(tmpStartNode);
							
							regions_[i][j].addNode(tmpNode, false);
							tmpStartNode = tmpNode;
							
							if(tmp == null) tmpStreets[k].setStartNode(tmpStartNode);
							else tmpStreets[k].setEndNode(tmpStartNode);
							
							if(tmpStartNode.getX() < minX || tmpEndNode.getX() < minX || tmpStartNode.getX() > maxX || tmpEndNode.getX() > maxX){
								tmpStartNode.setY(trimStreet(tmpEndNode.getY(), tmpEndNode.getX(), tmpStartNode.getY(), tmpStartNode.getX(), tmpBorder));
								tmpStartNode.setX(tmpBorder);
							}
							else{
								tmpStartNode.setX(trimStreet(tmpEndNode.getX(), tmpEndNode.getY(), tmpStartNode.getX(), tmpStartNode.getY(), tmpBorder));
								tmpStartNode.setY(tmpBorder);
							}

						}
				 	}
				}
			}
		}

		
		//set new map width and height
		width_ = maxX - minX;
		height_ = maxY - minY;
		
		//change all nodes
		for(int i = 0; i < regions_.length; i++){
			for(int j = 0; j < regions_[i].length; j++){
				tmpNodes = regions_[i][j].getNodes();				
				for(int k = 0; k < tmpNodes.length; k++){				
					tmpNodes[k].setX(tmpNodes[k].getX() - minX);
					tmpNodes[k].setY(tmpNodes[k].getY() - minY);
				}
			}
		}
		
		ArrayList<Node> tmpList = (ArrayList<Node>) Map.getInstance().getAmenityList().clone();
		
		for(Node n:tmpList){
			if((n.getX() < minX || n.getY() < minY) || (n.getX() > maxX || n.getY() > maxY))  Map.getInstance().getAmenityList().remove(n);
			else{
				n.setX(n.getX() - minX);
				n.setY(n.getY() - minY);
			}
		}
		
		Renderer.getInstance().ReRender(true, true);
		
		saveReloadMap();
	}
		
	public int trimStreet(int streetXInside, int streetYInside, int streetXOutside, int streetYOutside, int border){
		int y, a, b, x;
		double f, fakt, c;
		if(streetXInside > streetXOutside) y = streetXInside - streetXOutside;
		else y = streetXOutside - streetXInside;
		
		if(streetYOutside > border){
			a = border - streetYInside;
			b = streetYOutside - border;
		}
		else{
			a = streetYInside - border;
			b = border - streetYOutside;
		}
		
		f = Math.sqrt(y*y + (a+b)*(a+b));
		fakt = f / (a+b);
		c = b * fakt;
		x = (int) Math.round(Math.sqrt(c*c - b*b));

		if(streetXOutside > streetXInside) return streetXOutside - x;
		else return streetXOutside + x;
	}
	
	/**
	 * Helper function to save and reload a map after editing
	 */
	public void saveReloadMap(){
		VanetSimStart.getMainControlPanel().changeFileChooser(false, true, false);
		final JFileChooser filechooser = VanetSimStart.getMainControlPanel().getFileChooser();
		int returnVal = filechooser.showSaveDialog(VanetSimStart.getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Runnable job = new Runnable() {
				public void run() {
					File file = filechooser.getSelectedFile();
					if(filechooser.getAcceptAllFileFilter() != filechooser.getFileFilter() && !file.getName().toLowerCase().endsWith(".xml")) file = new File(file.getAbsolutePath() + ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
					Map.getInstance().save(file, false);
					Map.getInstance().load(file, false);
				}
			};
			new Thread(job).start();
		}
	}	
	

	/**
	 * Delete every turn-off lane on this map
	 */
	public void clearTrafficLights(){
		for(int i = 0; i < regionCountX_; ++i) for(int j = 0; j < regionCountY_; ++j) regions_[i][j].clearTrafficLights();
	}
		
	/**
	 * add amenity node
	 */
	public void addAmenityNode(Node n, String value){
		if(value.equals("school")) n.setNodeColor(Color.yellow);
		else if(value.equals("kindergarten")) n.setNodeColor(Color.green);
		else if(value.equals("hospital")) n.setNodeColor(Color.magenta);
		else if(value.equals("police")) n.setNodeColor(Color.blue);
		else if(value.equals("fire_station")) n.setNodeColor(Color.red);

		n.setAmenity_(value);
		amenityList_.add(n);
	}
	
	/**
	 * Gets the map width.
	 * 
	 * @return the map width
	 */
	public int getMapWidth(){
		return width_;
	}

	/**
	 * Gets the map height.
	 * 
	 * @return the map height
	 */
	public int getMapHeight(){
		return height_;
	}

	/**
	 * Calculates the {@link Region} of a point.
	 * 
	 * @param x	the x coordinate of the point
	 * @param y	the y coordinate of the point
	 * 
	 * @return the region in which this point is located or <code>null</code> if there was a problem
	 */
	public Region getRegionOfPoint(int x, int y){
		if(regionWidth_ > 0 && regionHeight_ > 0){
			int region_x = x/regionWidth_;
			int region_y = y/regionHeight_;
			if (region_x < 0) region_x = 0;
			else if (region_x >= regionCountX_) region_x = regionCountX_ - 1;
			if (region_y < 0) region_y = 0;
			else if (region_y >= regionCountY_) region_y = regionCountY_ - 1;
			return regions_[region_x][region_y];
		} else return null;
	}	

	/**
	 * write silent period header to log file
	 */
	public void writeSilentPeriodHeader(){
		
	}
	
	
	/**
	 * Gets all regions.
	 * 
	 * @return all regions
	 */
	public Region[][] getRegions(){
		return regions_;
	}

	/**
	 * Gets the amount of regions in x direction.
	 * 
	 * @return the amount of regions in x direction
	 */
	public int getRegionCountX(){
		return regionCountX_;
	}

	/**
	 * Gets the amount of regions in y direction.
	 * 
	 * @return the amount of regions in y direction
	 */
	public int getRegionCountY(){
		return regionCountY_;
	}

	/**
	 * Returns if a map is currently in the process of being loaded. While loading, simulation
	 * and rendering should not be done because not all map elements are already existing!
	 * 
	 * @return <code>true</code> if loading has finished, else <code>false</code>
	 */
	public boolean getReadyState(){
		return ready_;
	}

	/**
	 * @return the amenityList
	 */
	public ArrayList<Node> getAmenityList() {
		return amenityList_;
	}

	/**
	 * @param amenityList the amenityList to set
	 */
	public  void setAmenityList(ArrayList<Node> amenityList) {
		amenityList_ = amenityList;
	}

	public String getMapName_() {
		return mapName_;
	}

	public void setMapName_(String mapName_) {
		this.mapName_ = mapName_;
	}
	
	
	/**
	 * displays amount of vehicles per region
	 */
	public void printVehiclesPerRegion(){
		int counter = 0;
		for(int i = 0; i < regionCountY_; ++i){
			for(int j = 0; j < regionCountX_; ++j){
				System.out.print(regions_[j][i].getVehicleArrayList().size() + ",");
				counter++;
			}
			System.out.println();
		}
		
		System.out.println("Counter: " + counter);
	}
	
	/**
	 * save vehicle coordinates to file
	 */
	public void saveVehicles(){
		 FileWriter fstream;
         try {
			fstream = new FileWriter("vehicleCoords.log", true);
			BufferedWriter out = new BufferedWriter(fstream);

			for(int i = 0; i < regionCountY_; ++i){
				for(int j = 0; j < regionCountX_; ++j){
					for(Vehicle vehicle:regions_[j][i].getVehicleArrayList()) out.write(vehicle.getX() + " " + vehicle.getY() + "\n");
				}
			}
			out.flush();
			out.close();
 		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	
	}
	
}