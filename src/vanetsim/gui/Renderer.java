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
package vanetsim.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.CyclicBarrier;

import java.awt.geom.Path2D;
import java.util.ArrayDeque;

import javax.imageio.ImageIO;

import vanetsim.ErrorLog;
import vanetsim.localization.Messages;
import vanetsim.map.Junction;
import vanetsim.map.Map;
import vanetsim.map.MapHelper;
import vanetsim.map.Node;
import vanetsim.map.Region;
import vanetsim.map.Street;
import vanetsim.routing.WayPoint;
import vanetsim.scenario.AttackRSU;
import vanetsim.scenario.KnownPenalties;
import vanetsim.scenario.KnownVehicle;
import vanetsim.scenario.Vehicle;
import vanetsim.scenario.RSU;
import vanetsim.scenario.events.Cluster;
import vanetsim.scenario.events.EventList;
import vanetsim.scenario.events.EventSpot;
import vanetsim.scenario.events.EventSpotList;
import vanetsim.scenario.events.StartBlocking;

/**
 * This class performs all rendering tasks.
 */
public final class Renderer{

	/** The only instance of this class (singleton). */
	private static final Renderer INSTANCE = new Renderer();

	/**
	 * Gets the single instance of this renderer.
	 * 
	 * @return single instance of this renderer
	 */
	public static Renderer getInstance(){
		return INSTANCE;
	}
	
	/** The size of one single vehicle (2,5m). */
	private static final int VEHICLE_SIZE = 250;
	
	/** A global formatter to get locale-specific grouping separators from numeric values */
	private static final DecimalFormat FORMATTER = new DecimalFormat(",###"); //$NON-NLS-1$
		
	/** A reference to the singleton instance of the {@link vanetsim.map.Map} because we need this quite often and don't want to rely on compiler inlining. */
	private final Map map_ = Map.getInstance();
	
	/** The font used for displaying the current time. */
	private final Font timeFont_ = new Font("Default", Font.PLAIN, 11); //$NON-NLS-1$
	
	/** The font used for displaying the silent period status. */
	private final Font silentPeriodFont_ = new Font("Default", Font.BOLD, 20); //$NON-NLS-1$
	
	/** The font size for displaying the vehicle ID. */
	private final int vehicleIDFontSize_ = 150;
	
	/** The font used for displaying the vehicle ID. */
	private final Font vehicleIDFont_ = new Font("SansSerif", Font.PLAIN, vehicleIDFontSize_); //$NON-NLS-1$
	
	/** A cached <code>BasicStroke</code> used as a (black) background for streets with 2 lanes total. */
	private final BasicStroke lineBackground_ = new BasicStroke((Map.LANE_WIDTH + 45)*2,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER);

	/** A cached <code>BasicStroke</code> used for painting streets with 2 lanes total. */
	private final BasicStroke lineMain_ = new BasicStroke(Map.LANE_WIDTH*2,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER);

	/** The <code>AffineTransform</code> which handles zooming and panning transparently for us. */
	private final AffineTransform transform_ = new AffineTransform();
	
	/** Used as source to transform the blockingImage into current coordinate space. */
	private final Point2D blockingImageTransformSource_ = new Point2D.Double(0,0);;

	/** To get the result of the transformation of the blockingImage. */
	private final Point2D blockingImageTransformDestination_ = new Point2D.Double(0,0);
	
	/** If a simulation thread is currently running. */
	private boolean simulationRunning_ = false;

	/** Used to display how long the relative time in milliseconds since the simulation started. */
	private int timePassed_ = 0;

	/** A boolean indicating if a new full render (=rendering of static objects) is needed. */
	private boolean scheduleFullRender_ = false;

	/** <code>true</code> if the current painting was forced by the simulation master, else <code>false</code> (by AWT/Swing or so). */
	private boolean doPaintInitializedBySimulation_ = false;

	/** The {@link DrawingArea} for the simulation. */
	private DrawingArea drawArea_;

	/** The width of the {@link DrawingArea}. */
	private int drawWidth_ = 0;

	/** The height of the {@link DrawingArea}. */
	private int drawHeight_ = 0;	

	/** The x coordinate of the center of the currently drawn map part. */
	private double middleX_ = 0;

	/** The y coordinate of the center of the currently drawn map part. */
	private double middleY_ = 0;	

	/** The minimum x coordinate to be painted. */
	private int mapMinX_ = 0;

	/** The minimum y coordinate to be painted. */
	private int mapMinY_ = 0;

	/** The maximum x coordinate to be painted. */
	private int mapMaxX_ = 0;

	/** The maximum y coordinate to be painted. */
	private int mapMaxY_ = 0;
	
	/** If the current rendered view displays over the borders of the map. Used to detect when efficient panning can NOT be used. */
	private boolean currentOverdrawn_ = true;
	
	/** If the last rendered view displays over the borders of the map. Used to detect when efficient panning can NOT be used. */
	private boolean lastOverdrawn_ = true;

	/** The current zooming factor. */
	private double zoom_ = Math.exp(1/100.0)/1000;

	/** The amount of pans in x direction (negative if moved down) since the last paint of the static objects. */
	private int panCountX_ = 0;

	/** The amount of pans in y direction (negative if moved down) since the last paint of the static objects. */
	private int panCountY_ = 0;

	/** The x-axis index of the leftmost region which is currently considered to be rendered. */
	private int regionMinX_ = 0;

	/** The y-axis index of the leftmost region which is currently considered to be rendered. */
	private int regionMinY_ = 0;

	/** The x-axis index of the rightmost region which is currently considered to be rendered. */
	private int regionMaxX_ = 0;

	/** The y-axis index of the rightmost region which is currently considered to be rendered. */
	private int regionMaxY_ = 0;

	/** A temporary <code>AffineTransform</code>. Prevents some unnecessary garbage collection. */
	private AffineTransform tmpAffine_ = new AffineTransform();

	/** The maximum translation in x direction we got from a <code>Graphics2D</code> object (used because this value sometimes is wrong when using substance themes). */
	private double maxTranslationX_ = 0;

	/** The maximum translation in y direction we got from a <code>Graphics2D</code> object (used because this value sometimes is wrong when using substance themes). */
	private double maxTranslationY_ = 0;

	/** A street which is to be drawn marked (selected by user). */
	private Street markedStreet_ = null;	

	/** A vehicle which is to be drawn marked (selected by user). */
	private Vehicle markedVehicle_ = null;
	
	/** A vehicle which is to be drawn as attacker (selected by user). */
	private Vehicle attackerVehicle_ = null;
	
	/** A vehicle which is to be drawn as attacked vehicle (selected by user). */
	private Vehicle attackedVehicle_ = null;
	
	/** If circles shall be displayed to indicate communication distances. */
	private boolean highlightCommunication_ = false;

	/** If all nodes shall be highlighted. */
	private boolean highlightAllNodes_ = false;
	
	/** If mix zones shall be hided. */
	private boolean hideMixZones_ = false;
	
	/** If vehicle IDs shall be displayed. */
	private boolean displayVehicleIDs_ = false;

	/** <code>true</code> if all blockings shall be showed, else <code>false</code>. */
	private boolean showAllBlockings_;
	
	/** If the monitored beacon zone shall be indicated or not */
	private boolean showBeaconMonitorZone_ = false;
	
	/** If vehicles should be displayed (used in vehicle edit modes) */
	private boolean showVehicles_ = false;
	
	/** If mix zones should be displayed (used in mix zone edit mode)*/
	private boolean showMixZones_ = false;
	
	/** If RSUs should be displayed (used in RSU edit mode)*/
	private boolean showRSUs_ = false;
	
	/** If attacker and attacked vehicle should be displayed*/
	private boolean showAttackers_ = false;
	
	/** If a mix zone should be added at each street corner */
	private boolean autoAddMixZones_ = false;
	
	/** The minimum x coordinate which is checked during beacon monitoring. */
	private int beaconMonitorMinX_ = -1;
	
	/** The maximum x coordinate which is checked during beacon monitoring. */
	private int beaconMonitorMaxX_ = -1;
	
	/** The minimum y coordinate which is checked during beacon monitoring. */
	private int beaconMonitorMinY_ = -1;
	
	/** The maximum y coordinate which is checked during beacon monitoring. */
	private int beaconMonitorMaxY_ = -1;
	
	/** The RSU color */
	private Color rsuColor = new Color(0,100,0);
	
	/** The ARSU color */
	private Color arsuColor = new Color(100,0,0);

	/** A <code>CyclicBarrier</code> to signal the SimulationMaster that we are ready with rendering the dynamic objects. */
	private CyclicBarrier barrierForSimulationMaster_;

	/** An image to indicate that there's a blocking. */
	private BufferedImage blockingImage_;
	
	/** An image to indicate that there's a slippery road. :) */
	private BufferedImage slipperyImage_;
	
	/** A scaled instance of the <code>blockingImage_</code>. Is updated on every zoom change. */
	private BufferedImage scaledBlockingImage_;
	
	/** A scaled instance of the <code>slipperyImage_</code>. Is updated on every zoom change. */
	private BufferedImage scaledSlipperyImage_;
	
	/** flag for console start*/
	private boolean consoleStart = false;

	/** the marked junction*/
	private Junction markedJunction_ = null;

	/** arraylist to show if a beacon was guessed correctly after mix. Is not used while simulation is running, so performance doesn't have to be that good*/
	private ArrayList<String> locationInformationMix_ = null;
	
	/** arraylist to show if a beacon was guessed correctly after silent period. Is not used while simulation is running, so performance doesn't have to be that good*/
	private ArrayList<String> locationInformationSilentPeriod_ = null;
	
	/** arraylist to show the results of the MDS*/
	private ArrayList<String> locationInformationMDS_ = null;
	
	/** flag to switch location information mode for MDS */
	private boolean MDSMode_ = true;
	
	/** counts passed mix-zones. Used to show location information */
	private int mixZoneAmount = 0;
	
	/** flag to activate the debug mode. In this mode more information is shown to the user */
	private boolean debugMode = false;
	
	/** flag to enable schools, police, ... on map (this are not event spots, but can be imported) */
	private boolean showAmenities = false;
	
	/** the selected event grid */
	private int[][] grid = null;
	
	/** the min value of the grid */
	private int minGridValue = 0;
	
	/** the max value of the grid */
	private int maxGridValue = 0;
	
	/** the grid size */
	private int gridSize_ = 0;
	
	/** flag to show the penalty connections */
	private boolean showPenaltyConnections_ = false;
	
	/** flag to show known vehicles connections */
	private boolean showKnownVehiclesConnections_ = false;
	
	/** the x values from the centroids */
	private ArrayList<Double> centroidsX = null;
	
	/** the y values from the centroids */
	private ArrayList<Double> centroidsY = null;
	
	/** a ArrayList of the clusters */
	private ArrayList<Cluster> clusters = new ArrayList<Cluster>();
	
	/** a cluster that should be shown */
	private Cluster displayCluster_ = null;
	
	/** a flag to toggle between cluster display modes */
	private boolean showAllClusters = false;
	
	/** The font used for displaying the cluster ID. */
	private final Font clusterIDFont_ = new Font("SansSerif", Font.PLAIN, 1000); //$NON-NLS-1$
	
	/**
	 * Private constructor in order to disable instancing. Creates the blocking images.
	 */
	private Renderer(){
		try{
			//We're not directly using the BufferedImage returned by ImageIO as this might not be suitable for the graphics environment and thus slow. The small overhead here does not hurt
			//as it's just done on program startup
			BufferedImage tmpImage;
			URL url = ClassLoader.getSystemResource("vanetsim/images/blocking.png"); //$NON-NLS-1$
			tmpImage = ImageIO.read(url);
			blockingImage_ = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(tmpImage.getWidth(), tmpImage.getHeight(), 3);
			blockingImage_.getGraphics().drawImage(tmpImage, 0, 0, null);
			
			url = ClassLoader.getSystemResource("vanetsim/images/slippery_road.png"); 
			tmpImage = ImageIO.read(url);
			slipperyImage_ = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(tmpImage.getWidth(), tmpImage.getHeight(), 3);
			slipperyImage_.getGraphics().drawImage(tmpImage, 0, 0, null);
		} catch(Exception e){
			ErrorLog.log(Messages.getString("Renderer.noBlockingImage"), 7, Renderer.class.getName(), "Constructor", e);  //$NON-NLS-1$//$NON-NLS-2$
			//create just empty transparent images.
			blockingImage_ = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(1, 1, 3);
			slipperyImage_ = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(1, 1, 3);
		}
	}

	/**
	 * This function renders all non-static objects on the supplied <code>Graphics2D</code> object.
	 * 
	 * @param g2d	the <code>Graphics2D</code> object on which rendering takes place
	 */
	public void drawMovingObjects(Graphics2D g2d){
		Region[][] regions = map_.getRegions();
		if(regions != null && map_.getReadyState() && (!simulationRunning_ || doPaintInitializedBySimulation_)){
			int i, j, k, size;
			Vehicle vehicle;
			Vehicle[] vehicles;

			// A small fix because the substance theme engine sometimes causes unwanted shifts
			AffineTransform g2dAffine = g2d.getTransform();	//cache to save some calls
			if(g2dAffine.getTranslateX() > maxTranslationX_) maxTranslationX_ = g2dAffine.getTranslateX();
			if(g2dAffine.getTranslateY() > maxTranslationY_) maxTranslationY_ = g2dAffine.getTranslateY();		
			if(g2dAffine.getTranslateX() < 0 || g2dAffine.getTranslateY() < 0) tmpAffine_ = (AffineTransform)g2dAffine.clone();
			else if(g2dAffine.getTranslateX() < maxTranslationX_ || g2dAffine.getTranslateY() < maxTranslationY_) tmpAffine_.setToIdentity();
			else tmpAffine_.setToTranslation(maxTranslationX_, maxTranslationY_);
			tmpAffine_.concatenate(transform_);
			AffineTransform tmpAffine2 = g2d.getTransform();
			g2d.setTransform(tmpAffine_);
			
			if(showBeaconMonitorZone_){
				g2d.setColor(Color.orange);
				g2d.drawRect(beaconMonitorMinX_, beaconMonitorMinY_, beaconMonitorMaxX_ - beaconMonitorMinX_, beaconMonitorMaxY_ - beaconMonitorMinY_);
			}
			
			// draw street marked by user
			if(markedStreet_!=null){
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
				int totalLanes;
				if(markedStreet_.isOneway()) totalLanes = markedStreet_.getLanesCount();
				else totalLanes = 2 * markedStreet_.getLanesCount();

				int x0 = markedStreet_.getStartNode().getX();
				int y0 = markedStreet_.getStartNode().getY();
				int x1 = markedStreet_.getEndNode().getX();
				int y1 = markedStreet_.getEndNode().getY();

				// paint the line with a black background
				g2d.setStroke(new BasicStroke(Map.LANE_WIDTH*totalLanes + 45,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER));
				g2d.setPaint(Color.BLACK);
				g2d.drawLine(x0, y0, x1, y1);

				// paint arrows to indicate direction. See http://lifshitz.ucdavis.edu/~dmartin/teach_java/slope/arrows.html (link last visited: 04.09.2008)
				int deltaX = x1 - x0;
				int deltaY = y1 - y0;
				double frac = 0.2;
				if(markedStreet_.getLength() < 1000 && markedStreet_.isOneway())frac = 0.9;	//paint short streets which are oneway with longer arrow. There's no solution implemented for short twoway-streets...

				g2d.drawLine(x0 + (int)((1-frac)*deltaX + frac*deltaY),y0 + (int)((1-frac)*deltaY - frac*deltaX),x1, y1);
				g2d.drawLine(x0 + (int)((1-frac)*deltaX - frac*deltaY),y0 + (int)((1-frac)*deltaY + frac*deltaX),x1, y1);
				if(!markedStreet_.isOneway()){
					deltaX = -deltaX;
					deltaY = -deltaY;
					g2d.drawLine(x1 + (int)((1-frac)*deltaX + frac*deltaY),y1 + (int)((1-frac)*deltaY - frac*deltaX),x0, y0);
					g2d.drawLine(x1 + (int)((1-frac)*deltaX - frac*deltaY),y1 + (int)((1-frac)*deltaY + frac*deltaX),x0, y0);
					deltaX = -deltaX;
					deltaY = -deltaY;
				}

				// paint the whole arrow again but this time with the fill color
				g2d.setStroke(new BasicStroke(Map.LANE_WIDTH*totalLanes,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER));
				g2d.setPaint(Color.CYAN);
				g2d.drawLine(x0, y0, x1, y1);

				g2d.drawLine(x0 + (int)((1-frac)*deltaX + frac*deltaY),y0 + (int)((1-frac)*deltaY - frac*deltaX),x1, y1);
				g2d.drawLine(x0 + (int)((1-frac)*deltaX - frac*deltaY),y0 + (int)((1-frac)*deltaY + frac*deltaX),x1, y1);
				if(!markedStreet_.isOneway()){
					deltaX = -deltaX;
					deltaY = -deltaY;
					g2d.drawLine(x1 + (int)((1-frac)*deltaX + frac*deltaY),y1 + (int)((1-frac)*deltaY - frac*deltaX),x0, y0);
					g2d.drawLine(x1 + (int)((1-frac)*deltaX - frac*deltaY),y1 + (int)((1-frac)*deltaY + frac*deltaX),x0, y0);
				}
			}
			
			g2d.setTransform(tmpAffine2);

			//draw blocking events
			ArrayList<StartBlocking> blockings;
			if(showAllBlockings_) blockings = EventList.getInstance().getAllBlockingsArrayList();
			else blockings = EventList.getInstance().getCurrentBlockingsArrayList();
			size = blockings.size();
			if(size > 0){
				k=scaledBlockingImage_.getHeight()/2;
				int l = scaledBlockingImage_.getHeight()/2;
				for(i = 0; i < size; ++i){
					blockingImageTransformSource_.setLocation(blockings.get(i).getX()-k/zoom_, blockings.get(i).getY()-l/zoom_);
					transform_.transform(blockingImageTransformSource_, blockingImageTransformDestination_);
					if(blockings.get(i).getPenaltyType_().equals("HUANG_RHCN")){
						g2d.drawImage(scaledSlipperyImage_, (int)Math.round(blockingImageTransformDestination_.getX()), (int)Math.round(blockingImageTransformDestination_.getY()), null, drawArea_);
					}
					else{
						g2d.drawImage(scaledBlockingImage_, (int)Math.round(blockingImageTransformDestination_.getX()), (int)Math.round(blockingImageTransformDestination_.getY()), null, drawArea_);
					}
				}
			}

			g2d.setTransform(tmpAffine_);

			//Traffic lights
			Street[] streets;
			Street street;
			for(i = regionMinX_; i <= regionMaxX_; ++i){
				for(j = regionMinY_; j <= regionMaxY_; ++j){
					streets = regions[i][j].getStreets();
					for(k = 0; k < streets.length; ++k){
						
						street = streets[k];
						
						
						if(street.getEndNodeTrafficLightState() != -1) {
							if(markedJunction_ != null && markedJunction_.getNode().equals(street.getEndNode())){
								g2d.setPaint(Color.orange);
								g2d.fillOval(street.getTrafficLightEndX_()-(Map.LANE_WIDTH+45), street.getTrafficLightEndY_()-(Map.LANE_WIDTH+45),(Map.LANE_WIDTH+45)*2,(Map.LANE_WIDTH+45)*2);		
							}
							if(street.getEndNodeTrafficLightState() == 0) g2d.setPaint(Color.green);
							else if (street.getEndNodeTrafficLightState() == 1 || street.getEndNodeTrafficLightState() == 7) g2d.setPaint(Color.yellow);
							else g2d.setPaint(Color.red);
							
							g2d.fillOval(street.getTrafficLightEndX_() - (Map.LANE_WIDTH), street.getTrafficLightEndY_() - (Map.LANE_WIDTH),(Map.LANE_WIDTH)*2,(Map.LANE_WIDTH)*2);
						}
						if(street.getStartNodeTrafficLightState() != -1){
							if(markedJunction_ != null && markedJunction_.getNode().equals(street.getStartNode())){
								g2d.setPaint(Color.orange);
								g2d.fillOval(street.getTrafficLightStartX_()-(Map.LANE_WIDTH+45), street.getTrafficLightStartY_()-(Map.LANE_WIDTH+45),(Map.LANE_WIDTH+45)*2,(Map.LANE_WIDTH+45)*2);		
							}
							if(street.getStartNodeTrafficLightState() == 0) g2d.setPaint(Color.green);
							else if (street.getStartNodeTrafficLightState() == 1 || street.getStartNodeTrafficLightState() == 7) g2d.setPaint(Color.yellow);
							else g2d.setPaint(Color.red);
							
							g2d.fillOval(street.getTrafficLightStartX_() - (Map.LANE_WIDTH), street.getTrafficLightStartY_() - (Map.LANE_WIDTH),(Map.LANE_WIDTH)*2,(Map.LANE_WIDTH)*2);
						}
					}
				}
			}
		
			
			
			//only if zoom is near enough
			if(zoom_ > 0.0018){
				if(displayVehicleIDs_) g2d.setFont(vehicleIDFont_);
				// draw all visible vehicles
				g2d.setStroke(new BasicStroke(20,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER));
				g2d.setPaint(Color.black);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
				try{
					for(i = regionMinX_; i <= regionMaxX_; ++i){
						for(j = regionMinY_; j <= regionMaxY_; ++j){
							vehicles = regions[i][j].getVehicleArray();
							size = vehicles.length;
							for(k = 0; k < size; ++k){
								vehicle = vehicles[k];
								g2d.setPaint(vehicle.getColor());
								if(vehicle.isInSlow())g2d.setPaint(Color.pink);
								if((isShowVehicles() || vehicle.isActive()) && vehicle.getX() >= mapMinX_ && vehicle.getX() <= mapMaxX_ && vehicle.getY() >= mapMinY_ && vehicle.getY() <= mapMaxY_){		//only paint when necessary and within paint area
									if(highlightCommunication_){
										if(vehicle.isWiFiEnabled() && (!vehicle.isInMixZone() || Vehicle.getMixZonesFallbackEnabled())){
											g2d.setPaint(Color.blue);
											if(vehicle != markedVehicle_) g2d.drawOval(vehicle.getX()-vehicle.getMaxCommDistance(), vehicle.getY()-vehicle.getMaxCommDistance(),vehicle.getMaxCommDistance()*2,vehicle.getMaxCommDistance()*2);
										} else g2d.setPaint(Color.black);
									}
									g2d.fillOval(vehicle.getX()-VEHICLE_SIZE/2, vehicle.getY()-VEHICLE_SIZE/2,VEHICLE_SIZE,VEHICLE_SIZE);
								}
							}
						}
					}
				} catch (Exception e){}
				if(displayVehicleIDs_){
					g2d.setPaint(new Color(153, 102, 100));
					try{
						for(i = regionMinX_; i <= regionMaxX_; ++i){
							for(j = regionMinY_; j <= regionMaxY_; ++j){
								vehicles = regions[i][j].getVehicleArray();
								size = vehicles.length;
								for(k = 0; k < size; ++k){
									vehicle = vehicles[k];
									if(vehicle.isActive() && vehicle.getX() >= mapMinX_ && vehicle.getX() <= mapMaxX_ && vehicle.getY() >= mapMinY_ && vehicle.getY() <= mapMaxY_){		//only paint when necessary and within paint area
										g2d.drawString(vehicle.getHexID(), vehicle.getX() - vehicleIDFontSize_ *4, vehicle.getY() - VEHICLE_SIZE/2);
									}
								}
							}
						}
					} catch (Exception e){}
				}


				//visualize known vehicles connections
				if(showKnownVehiclesConnections_){
					g2d.setPaint(new Color(153, 102, 100));
					try{
						for(i = regionMinX_; i <= regionMaxX_; ++i){
							for(j = regionMinY_; j <= regionMaxY_; ++j){
								vehicles = regions[i][j].getVehicleArray();
								size = vehicles.length;
								for(k = 0; k < size; ++k){
									vehicle = vehicles[k];
									if(vehicle.isActive() && vehicle.getX() >= mapMinX_ && vehicle.getX() <= mapMaxX_ && vehicle.getY() >= mapMinY_ && vehicle.getY() <= mapMaxY_){		//only paint when necessary and within paint area
										
										
										KnownVehicle[] heads = vehicle.getKnownVehiclesList().getFirstKnownVehicle();
										KnownVehicle next;
										
										//traverse all vehicle which sent beacons
										for(int l = 0; l < heads.length; ++l){
											next = heads[l];								
											while(next != null){
												//Find the attacker data
												g2d.drawLine(vehicle.getX(), vehicle.getY(),next.getX(), next.getY());
												next = next.getNext();
											}
										}
										

									}
								}
							}
						}
					} catch (Exception e){e.printStackTrace();}
				}
				
				// visualize penalty connections
				if(showPenaltyConnections_){
					
					try{
						for(i = regionMinX_; i <= regionMaxX_; ++i){
							for(j = regionMinY_; j <= regionMaxY_; ++j){
								vehicles = regions[i][j].getVehicleArray();
								size = vehicles.length;
								for(k = 0; k < size; ++k){
									vehicle = vehicles[k];
									if(vehicle.isActive() && vehicle.getX() >= mapMinX_ && vehicle.getX() <= mapMaxX_ && vehicle.getY() >= mapMinY_ && vehicle.getY() <= mapMaxY_){		//only paint when necessary and within paint area
										KnownPenalties tmp = vehicle.getKnownPenalties();
										
										if(tmp != null){
											for(int p = 0; p < tmp.getSize(); p++){
												if(tmp.getPenaltyType_()[p].equals("EVA_EMERGENCY_ID")) g2d.setPaint(Color.red);
												else if(tmp.getPenaltyType_()[p].equals("HUANG_PCN")) g2d.setPaint(Color.black);
												else if(tmp.getPenaltyType_()[p].equals("PCN_FORWARD")) g2d.setPaint(Color.gray);
												else if(tmp.getPenaltyType_()[p].equals("HUANG_RHCN")) g2d.setPaint(Color.green);
												else if(tmp.getPenaltyType_()[p].equals("HUANG_EVA_FORWARD")) g2d.setPaint(Color.pink);
												else if(tmp.getPenaltyType_()[p].equals("HUANG_EEBL")) g2d.setPaint(Color.orange);
												
												
												
												if(tmp.getPenaltySourceVehicle_() != null && tmp.getPenaltySourceVehicle_()[p] != null)g2d.drawLine(vehicle.getX(), vehicle.getY(), tmp.getPenaltySourceVehicle_()[p].getX(), tmp.getPenaltySourceVehicle_()[p].getY());
											}
										}
										
									}
								}
							}
						}
					} catch (Exception e){e.printStackTrace();}
				}
				// draw vehicle marked by user
				if(markedVehicle_ != null){
					g2d.setPaint(Color.RED);
					g2d.fillOval(markedVehicle_.getX()-VEHICLE_SIZE/2+35, markedVehicle_.getY()-VEHICLE_SIZE/2+35,VEHICLE_SIZE-70,VEHICLE_SIZE-70);
					if(markedVehicle_.isWiFiEnabled() && (!markedVehicle_.isInMixZone() || Vehicle.getMixZonesFallbackEnabled())) g2d.drawOval(markedVehicle_.getX()-markedVehicle_.getMaxCommDistance(), markedVehicle_.getY()-markedVehicle_.getMaxCommDistance(),markedVehicle_.getMaxCommDistance()*2,markedVehicle_.getMaxCommDistance()*2);
					WayPoint nextDestination = markedVehicle_.getDestinations().peekFirst();
					if(nextDestination != null){
						g2d.drawLine(markedVehicle_.getX(), markedVehicle_.getY(), nextDestination.getX(), nextDestination.getY());
						g2d.fillOval(nextDestination.getX()-VEHICLE_SIZE, nextDestination.getY()-VEHICLE_SIZE,VEHICLE_SIZE*2,VEHICLE_SIZE*2);
						Street[] routestreets = markedVehicle_.getRouteStreets();
						if(routestreets.length > 1){
							g2d.setPaint(Color.blue);
							if(markedVehicle_.getCurDirection()) g2d.drawLine(markedVehicle_.getX(), markedVehicle_.getY(), markedVehicle_.getCurStreet().getEndNode().getX(), markedVehicle_.getCurStreet().getEndNode().getY());
							else g2d.drawLine(markedVehicle_.getX(), markedVehicle_.getY(), markedVehicle_.getCurStreet().getStartNode().getX(), markedVehicle_.getCurStreet().getStartNode().getY());
							for(i = markedVehicle_.getRoutePosition()+1; i < routestreets.length-1; ++i){
								g2d.drawLine(routestreets[i].getStartNode().getX(), routestreets[i].getStartNode().getY(), routestreets[i].getEndNode().getX(), routestreets[i].getEndNode().getY());
							}
							if(!markedVehicle_.getRouteDirections()[routestreets.length-1]){
								g2d.drawLine(nextDestination.getX(), nextDestination.getY(), routestreets[routestreets.length-1].getEndNode().getX(), routestreets[routestreets.length-1].getEndNode().getY());
							} else {
								g2d.drawLine(nextDestination.getX(), nextDestination.getY(), routestreets[routestreets.length-1].getStartNode().getX(), routestreets[routestreets.length-1].getStartNode().getY());
							}
						}
					}
				
					//added to display more than 2 Waypoints for one vehicle (only used in vehicle edit mode, so no need to improve the performance)
					if(isShowVehicles()){
						
						ArrayDeque<WayPoint> tmpDestinations = markedVehicle_.getDestinations();
						
							WayPoint oldDestination = null;
							for(WayPoint destination : tmpDestinations){	
								if(oldDestination != null){
									g2d.setPaint(Color.blue);
									g2d.fillOval(oldDestination.getX()-VEHICLE_SIZE, oldDestination.getY()-VEHICLE_SIZE,VEHICLE_SIZE*2,VEHICLE_SIZE*2);
									g2d.setPaint(Color.red);
									g2d.drawLine(oldDestination.getX(), oldDestination.getY(), destination.getX(), destination.getY());
								}
							
								g2d.setPaint(Color.red);
								g2d.fillOval(destination.getX()-VEHICLE_SIZE, destination.getY()-VEHICLE_SIZE,VEHICLE_SIZE*2,VEHICLE_SIZE*2);
								
								oldDestination = destination;
							}
						}
				}
				if(showAttackers_ || simulationRunning_){
					// draw attacker vehicle
					if(attackerVehicle_ != null){
						g2d.setPaint(Color.LIGHT_GRAY);
						g2d.fillOval(attackerVehicle_.getX()-VEHICLE_SIZE/2+35, attackerVehicle_.getY()-VEHICLE_SIZE/2+35,VEHICLE_SIZE-70,VEHICLE_SIZE-70);
					}
					// draw attacked vehicle
					if(attackedVehicle_ != null){
						g2d.setPaint(Color.GREEN);
						g2d.fillOval(attackedVehicle_.getX()-VEHICLE_SIZE/2+35, attackedVehicle_.getY()-VEHICLE_SIZE/2+35,VEHICLE_SIZE-70,VEHICLE_SIZE-70);
					}
				}
			}

			// draw all nodes coloured to aid editing (only when editing streets and near enough)
			if(highlightAllNodes_ && zoom_ > 0.0012){
				Node[] nodes;
				Node node;
				for(i = regionMinX_; i <= regionMaxX_; ++i){
					for(j = regionMinY_; j <= regionMaxY_; ++j){
						nodes = regions[i][j].getNodes();
						for(k = 0; k < nodes.length; ++k){
							node = nodes[k];
							g2d.setPaint(Color.black);
							g2d.fillOval(node.getX()-(Map.LANE_WIDTH+45), node.getY()-(Map.LANE_WIDTH+45),(Map.LANE_WIDTH+45)*2,(Map.LANE_WIDTH+45)*2);						
							g2d.setPaint(Color.pink);
							//marked junction
							if(markedJunction_ != null && markedJunction_.getNode().equals(node))g2d.setPaint(Color.red);
							g2d.fillOval(node.getX()-Map.LANE_WIDTH, node.getY()-Map.LANE_WIDTH,Map.LANE_WIDTH*2,Map.LANE_WIDTH*2);
						}
					}
				}
			}

			
			// apply the fix for substance again for others which draw after us
			if(g2dAffine.getTranslateX() < 0 || g2dAffine.getTranslateY() < 0) tmpAffine_ = g2dAffine;
			else if(g2dAffine.getTranslateX() < maxTranslationX_ || g2dAffine.getTranslateY() < maxTranslationY_) tmpAffine_.setToIdentity();
			else tmpAffine_.setToTranslation(maxTranslationX_, maxTranslationY_);
			g2d.setTransform(tmpAffine_);

			// display time
			g2d.setPaint(Color.black);
			g2d.setFont(timeFont_);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
			g2d.drawString(FORMATTER.format(timePassed_) + " ms", 5 ,10 ); //$NON-NLS-1$

			//draw silent period sign
			if(Vehicle.isSilent_period()){
				// display time
				g2d.setPaint(Color.red);
				g2d.setFont(silentPeriodFont_);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
				g2d.drawString("SILENT PERIOD", 5 ,this.drawHeight_ - 10); //$NON-NLS-1$
			}
			
			if(simulationRunning_ && doPaintInitializedBySimulation_){
				doPaintInitializedBySimulation_ = false;
				try{
					barrierForSimulationMaster_.await();	//signal SimulationMaster that we are ready!
					barrierForSimulationMaster_.reset();
				} catch (Exception e){}
			}
		}
	}

	/**
	 * Creates an image to see the current scale.
	 * 
	 * @param image	the <code>BufferedImage</code> on which rendering should be done
	 */
	public void drawScale(BufferedImage image){
		Graphics2D g2d = image.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.white);
		g2d.fillRect(1, 1, 98, 28);
		g2d.setColor(Color.black);
		try{
			Point2D.Double point1 = new Point2D.Double();
			Point2D.Double point2 = new Point2D.Double();
			transform_.inverseTransform(new Point2D.Double(0,0), point1);
			transform_.inverseTransform(new Point2D.Double(0,70), point2);
			int len = (int)Math.round(point2.getY() - point1.getY());	//calculate length in cm of a 70px sample
			//now find an "appropriate" length which is smaller than 70px and an even multiple of a base distance (for example 7km, 300m, 20m instead of 7.2km, 312m and 24.2m) 
			if(len < 1){
				double len3 = (point2.getY() - point1.getY())*10;
				DecimalFormat df = new DecimalFormat(",###.###"); //$NON-NLS-1$
				g2d.drawString(df.format(len3) + " mm", 30, 15); //$NON-NLS-1$
				g2d.drawLine(15, 20, 85, 20);
				g2d.drawLine(15, 18, 15, 22);
				g2d.drawLine(85, 18, 85, 22);
			} else {
				transform_.transform(new Point2D.Double(0,0), point1);
				if(len > 10000000){	//100km and more
					len = len/10000000;
					transform_.transform(new Point2D.Double(0,len*10000000), point2);					
					g2d.drawString(len + "00 km", 25, 15);					 //$NON-NLS-1$
				} else if(len > 1000000){	//10km
					len = len/1000000;
					transform_.transform(new Point2D.Double(0,len*1000000), point2);
					g2d.drawString(len + "0 km", 30, 15); //$NON-NLS-1$
				} else if(len > 100000){	//1km
					len = len/100000;
					transform_.transform(new Point2D.Double(0,len*100000), point2);
					g2d.drawString(len + " km", 35, 15); //$NON-NLS-1$
				} else if(len > 10000){	//100m
					len = len/10000;
					transform_.transform(new Point2D.Double(0,len*10000), point2);
					g2d.drawString(len + "00 m", 35, 15); //$NON-NLS-1$
				} else if(len > 1000){		//10m
					len = len/1000;
					transform_.transform(new Point2D.Double(0,len*1000), point2);
					g2d.drawString(len + "0 m", 35, 15); //$NON-NLS-1$
				} else if(len > 100){		//1m
					len = len/100;
					transform_.transform(new Point2D.Double(0,len*100), point2);
					g2d.drawString(len + " m", 35, 15); //$NON-NLS-1$
				} else if(len > 10){		//10cm
					len = len/10;
					transform_.transform(new Point2D.Double(0,len*10), point2);
					g2d.drawString(len + "0 cm", 35, 15); //$NON-NLS-1$
				} else {		//1cm
					transform_.transform(new Point2D.Double(0,len), point2);
					g2d.drawString(len + " cm", 35, 15); //$NON-NLS-1$
				}
				len = (int)Math.round(point2.getY() - point1.getY());
				g2d.drawLine(15, 20, 15+len, 20);
				g2d.drawLine(15, 18, 15, 22);
				g2d.drawLine(15+len, 18, 15+len, 22);
			}
		} catch (Exception e){

		}
	}

	/**
	 * This function renders all non-moving objects on a supplied image.
	 * All streets and other static objects are rendered into the cached Image in this function.
	 * This function should only be called on
	 * <ul>
	 * <li>size changes of the drawing area</li>
	 * <li>zooming or panning</li>
	 * <li>map loading</li>
	 * <li>new map objects appearing</li>
	 * </ul>
	 * 
	 * <br><br>
	 * 
	 * Note: The main performance factor here are the calls to <code>draw()</code> or <code>drawLine()</code>. This is a factor determined by the underlying graphics
	 * subsytem and there's not much space for lot of improvement.
	 * Multithreading approaches are not possible here: The graphics subsystem is just singlethreaded (blocks concurrent draw-Calls
	 * even if they are done on different BufferedImages) and so there's no performance benefit.<br>
	 * Optimizing anything concerned with iterating through the regions is not worth it because the cpu time for this is barely measurable after
	 * using <code>ArrayLists</code> instead of <code>Iterators</code> (iterators create lots of object creations and calls)<br>
	 * 
	 * @param image the <code>BufferedImage</code> on which rendering should be done
	 */
	public synchronized void drawStaticObjects(BufferedImage image){
		Graphics2D g2d = image.createGraphics();
		Region[][] regions = map_.getRegions();
		
		if(regions != null && map_.getReadyState()){
			int savedRegionMinX = regionMinX_;		//copy as we might change it beneath
			int savedRegionMaxX = regionMaxX_;
			int savedRegionMinY = regionMinY_;
			int savedRegionMaxY = regionMaxY_;

			//try to reuse image elements if possible (only possible when panning); this reduces rendering time by up to 50%!
			if(!lastOverdrawn_ && (((panCountX_ == 1 || panCountX_ == -1) && panCountY_ == 0) || ((panCountY_ == 1 || panCountY_ == -1) && panCountX_ == 0))){	// only ONE change in ONE direction has happened since last render
				//draw old image onto new one with an offset in x or y direction
				g2d.drawImage(image, (int)Math.round(panCountX_*drawWidth_/2), (int)Math.round(panCountY_*drawHeight_/2), null, drawArea_);

				//calculate and set clipping for new drawing operation
				int clipX=0, clipY=0, clipWidth, clipHeight;
				if(panCountX_ != 0) clipWidth = drawWidth_/2;
				else clipWidth = drawWidth_;
				if(panCountY_ != 0) clipHeight = drawHeight_/2;
				else clipHeight = drawHeight_;
				if(panCountX_ < 0)	clipX = drawWidth_/2;
				else if(panCountY_ < 0) clipY = drawHeight_/2;
				g2d.setClip(clipX,clipY,clipWidth,clipHeight);

				//Update coordinates which need to be drawn
				int newMinX=mapMinX_, newMaxX=mapMaxX_,newMinY=mapMinY_, newMaxY=mapMaxY_;
				if(panCountX_ > 0) newMaxX = mapMaxX_ - ((mapMaxX_-mapMinX_)/2);
				else if(panCountX_ < 0) newMinX = mapMinX_ + ((mapMaxX_-mapMinX_)/2);
				if(panCountY_ > 0) newMaxY = mapMaxY_ - ((mapMaxY_-mapMinY_)/2);
				else if(panCountY_ < 0) newMinY = mapMinY_ + ((mapMaxY_-mapMinY_)/2);

				//Update regions which need to be drawn
				Region tmpRegion = map_.getRegionOfPoint(newMinX, newMinY);
				savedRegionMinX = tmpRegion.getX();
				savedRegionMinY = tmpRegion.getY();			
				tmpRegion = map_.getRegionOfPoint(newMaxX, newMaxY);
				savedRegionMaxX = tmpRegion.getX();
				savedRegionMaxY = tmpRegion.getY();
			}
			panCountX_ = 0;
			panCountY_ = 0;

			// fill background
			//FIXME change for black white mode
			g2d.setColor(new Color(230,230,230));
			//g2d.setColor(Color.white);
			
			g2d.fillRect(0,0,drawWidth_,drawHeight_);	

			//Check if we're near enough to use antialiasing (quite costly if there are too many streets)
			boolean antialias = true;
			if(zoom_ > 0.0018) g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			else {
				antialias = false;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
			}

			// set transformation (for automatic scaling and panning)
			g2d.setTransform(transform_);

			// paint outline of map
			if(!antialias) g2d.setPaint(Color.gray);
			else g2d.setPaint(Color.black);
			//FIXME change for black white mode
			g2d.drawRect(0, 0, map_.getMapWidth(), map_.getMapHeight());

			
			// prepare variables
			int i, j, k, totalLanes, lastEntry = -1;
			Street street;
			Region streetRegion;
			Node startNode, endNode;
			Street[] streets;
			TreeMap<Integer,Path2D.Float> layers = new TreeMap<Integer,Path2D.Float>();		//a sorted map so that we always get the same result (basically sorted by color)
			Path2D.Float currentPath = null;
			
			// display mix zones, moved to drawStaticObject() to improve the performance
			if(showMixZones_ || (hideMixZones_ && Vehicle.getCommunicationEnabled() && Vehicle.getMixZonesEnabled())){
				g2d.setPaint(Color.LIGHT_GRAY);
				for(i = regionMinX_; i <= regionMaxX_; ++i){
					for(j = regionMinY_; j <= regionMaxY_; ++j){
						Node[] nodes = regions[i][j].getMixZoneNodes();
						for(int l = 0; l < nodes.length; ++l){
							g2d.setPaint(Color.LIGHT_GRAY);
							g2d.fillOval(nodes[l].getX()-nodes[l].getMixZoneRadius(), nodes[l].getY()-nodes[l].getMixZoneRadius(),nodes[l].getMixZoneRadius()*2,nodes[l].getMixZoneRadius()*2);
							g2d.setPaint(Color.RED);
							if(nodes[l].getEncryptedRSU_() != null){
								g2d.fillOval(nodes[l].getEncryptedRSU_().getX() - 1500, nodes[l].getEncryptedRSU_().getY() - 1500,3000,3000);
							}
						}
					}
				}
			}
			

			//draw location Information silent period
			if(locationInformationSilentPeriod_ != null){
				showMixZones_ = true;
				String[] data;
				for(String location:locationInformationSilentPeriod_){
					data = location.split(":");
					if(data[0].equals("true"))g2d.setPaint(Color.RED);
					else if(data[0].equals("slow"))g2d.setPaint(Color.BLUE);
					else g2d.setPaint(Color.GREEN);
					g2d.drawOval(Integer.parseInt(data[1])-2500, Integer.parseInt(data[2])-2500,5000,5000);
				}
			}
			
			//draw location Information MDS
			if(locationInformationMDS_ != null){
				showMixZones_ = true;
				String[] data;
				for(String location:locationInformationMDS_){
					data = location.split(":");
					if(MDSMode_){
						// TP
						if(data[8].equals("true") && data[10].equals("true"))g2d.setPaint(Color.LIGHT_GRAY);
						//FN
						else if(data[8].equals("false") && data[10].equals("false"))g2d.setPaint(Color.BLACK);
					}
					else{
						// TN
						if(data[8].equals("false") && data[10].equals("true"))g2d.setPaint(Color.LIGHT_GRAY);
						//FP
						else if(data[8].equals("true") && data[10].equals("false"))g2d.setPaint(Color.BLACK);
					}
					g2d.drawOval(Integer.parseInt(data[5])-2500, Integer.parseInt(data[6])-2500,5000,5000);
				}
			}
			
	
			//draw location Information mix
			if(locationInformationMix_ != null){
				String[] data;
				int size = 0;
				for(String location:locationInformationMix_){
					data = location.split(":");
					if(Float.parseFloat(data[4]) != 0) size = (int)(1000 + (Float.parseFloat(data[4])/mixZoneAmount)*100000);
					else size = 0;
					g2d.setPaint(new Color((int)(Float.parseFloat(data[2])*200) + 55,(int)(Float.parseFloat(data[3])*200)+55,0));
					g2d.fillOval(Integer.parseInt(data[0])-size, Integer.parseInt(data[1])-size,size*2,size*2);
				}
			}
			
			// Create and draw a black background so that streets have black boundaries. Only makes sense when antialias is on because otherwise it looks ugly and is really slow(!)
			if(antialias){
				for(i = savedRegionMinX; i <= savedRegionMaxX; ++i){
					for(j = savedRegionMinY; j <= savedRegionMaxY; ++j){
						streets = regions[i][j].getStreets();
						for(k = 0; k < streets.length; ++k){
							street = streets[k];
							streetRegion = street.getMainRegion();
							if(streetRegion==regions[i][j] || streetRegion.getX()<savedRegionMinX || streetRegion.getX()>savedRegionMaxX || streetRegion.getY()<savedRegionMinY || streetRegion.getY()>savedRegionMaxY){	// only paint it if necessary (to prevent painting it multiple times)
								if(street.isOneway()) totalLanes = street.getLanesCount();
								else totalLanes = 2 * street.getLanesCount();
								if(lastEntry != totalLanes){
									lastEntry = totalLanes;
									currentPath = layers.get(totalLanes);
									if(currentPath == null){
										currentPath = new Path2D.Float (Path2D.WIND_NON_ZERO, 5000);
										layers.put(lastEntry, currentPath);
									}
								}
								startNode = street.getStartNode();		//saves some function calls
								endNode = street.getEndNode();								
								currentPath.moveTo(startNode.getX(), startNode.getY());
								currentPath.lineTo(endNode.getX(), endNode.getY());
							}
						}
					}
				}
				Iterator<Integer> iterator = layers.keySet().iterator();
				int key;
				//FIXME change for black white mode
				g2d.setPaint(Color.black);
				//g2d.setPaint(Color.white);
				g2d.setStroke(lineBackground_);
				//iterate through all layers and draw them
				while(iterator.hasNext()){
					key = iterator.next();
					if(key == 2) g2d.setStroke(lineBackground_);
					else g2d.setStroke(new BasicStroke(Map.LANE_WIDTH*key+90,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER));
					//FIXME change for black white mode
					g2d.draw(layers.get(key));
				}
			}
			//now create the layers. This second iteration step (only second one if antialiasing is on) is actually faster than mixing it into the first one!)
			layers.clear();		// to store the layers of the map separated by color
			lastEntry = 1;
			boolean detailedView;
			if(zoom_ > 0.0001) detailedView = true;	// only show fast speed streets if zoom is very far away
			else detailedView = false;
			for(i = savedRegionMinX; i <= savedRegionMaxX; ++i){
				for(j = savedRegionMinY; j <= savedRegionMaxY; ++j){
					streets = regions[i][j].getStreets();
					for(k = 0; k < streets.length; ++k){
						street = streets[k];
						streetRegion = street.getMainRegion();
						if(streetRegion==regions[i][j] || streetRegion.getX()<savedRegionMinX || streetRegion.getX()>savedRegionMaxX || streetRegion.getY()<savedRegionMinY || streetRegion.getY()>savedRegionMaxY){	// only paint it if necessary (to prevent painting it multiple times)
							if(detailedView || street.getSpeed() > 2000){
								startNode = street.getStartNode();		//saves some function calls
								endNode = street.getEndNode();
								if(antialias){
									//FIXME change for black white mode
									if(street.isOneway()) totalLanes = street.getDisplayColor().getRGB()*100 - street.getLanesCount();
									else totalLanes = street.getDisplayColor().getRGB()*100 - (2*street.getLanesCount());
								} else totalLanes = street.getDisplayColor().getRGB();							
								if(lastEntry != totalLanes){
									lastEntry = totalLanes;
									currentPath = layers.get(totalLanes);
									if(currentPath == null){	//create the layer if doesn't exist yet
										currentPath = new Path2D.Float (Path2D.WIND_NON_ZERO, 5000);
										layers.put(lastEntry, currentPath);
									}
								}
								currentPath.moveTo(startNode.getX(), startNode.getY());
								currentPath.lineTo(endNode.getX(), endNode.getY());
							}
						}
					}
				}
			}
			
			Color drawColor;	
			Iterator<Integer> coloriterator = layers.keySet().iterator();
			int key, lanes;
			//iterate through all layers and draw them
			while(coloriterator.hasNext()){
				key = coloriterator.next();
				if(antialias){
					//FIXME change for black white mode
					drawColor = new Color(key/100);
					//drawColor = Color.white;
					lanes = -key+(key/100*100);
					if(lanes == 2) g2d.setStroke(lineMain_);
					else g2d.setStroke(new BasicStroke(Map.LANE_WIDTH*lanes,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER));
				} else {
					g2d.setStroke(lineMain_);
					drawColor = new Color(Math.max((int)(((key >> 16) & 0xFF) *0.8), 0), Math.max((int)(((key >> 8) & 0xFF)*0.8), 0), Math.max((int)(((key >> 0) & 0xFF)*0.8), 0));	//simulate colors like if they were with antialias (darker)!
				}
				//FIXME change for black white mode
				//drawColor = Color.black;
				g2d.setPaint(drawColor);	
				//FIXME change for black white mode
				g2d.draw(layers.get(key));
			}
			// If the zoom is near enough, do a correction to display bridges more accurately (though not perfect as intersection calculation
			// seems to be a little bit problematic in some rare cases)
			if(antialias){
				int l, correction;
				int[] start = new int[2];
				int[] end = new int[2];
				boolean correctStart, correctEnd;
				ArrayList<Point2D.Double> paintArrayList;
				//1. step: paint streets again which are bridges
				for(i = savedRegionMinX; i <= savedRegionMaxX; ++i){
					for(j = savedRegionMinY; j <= savedRegionMaxY; ++j){
						streets = regions[i][j].getStreets();
						for(k = 0; k < streets.length; ++k){
							street = streets[k];
							streetRegion = street.getMainRegion();
							if(streetRegion==regions[i][j] || streetRegion.getX()<savedRegionMinX || streetRegion.getX()>savedRegionMaxX || streetRegion.getY()<savedRegionMinY || streetRegion.getY()>savedRegionMaxY){	// only paint it if necessary (to prevent painting it multiple times)
								if(street.getBridgePaintLines() != null || street.getBridgePaintPolygons() != null){
									if(street.isOneway()) totalLanes = street.getLanesCount();
									else totalLanes = 2 * street.getLanesCount();
									correction = totalLanes * Map.LANE_WIDTH*2;
									correctStart = false;
									correctEnd = false;
									start[0] = street.getStartNode().getX();
									start[1] = street.getStartNode().getY();
									end[0] = street.getEndNode().getX();
									end[1] = street.getEndNode().getY();
									if(street.getStartNode().getCrossingStreetsCount() > 2) correctStart = true;
									if(street.getEndNode().getCrossingStreetsCount() > 2) correctEnd = true;
									if(street.getLength() > correction*2) {
										MapHelper.calculateResizedLine(start, end, correction, correctStart, correctEnd);
										//paint the now shorter line completely
										g2d.setStroke(new BasicStroke(Map.LANE_WIDTH*totalLanes+90,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER));
										//FIXME change for black white mode
										//g2d.setColor(Color.black);
										g2d.setColor(Color.white);
										g2d.drawLine(start[0], start[1], end[0], end[1]);
										g2d.setStroke(new BasicStroke(Map.LANE_WIDTH*totalLanes,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER));
										//FIXME change for black white mode
										g2d.setColor(street.getDisplayColor());
										//g2d.setColor(Color.white);
										g2d.drawLine(start[0], start[1], end[0], end[1]);
									}									
								}
							}
						}
					}
				}
				//Step 2: Paint the overlapping parts again
				for(i = savedRegionMinX; i <= savedRegionMaxX; ++i){
					for(j = savedRegionMinY; j <= savedRegionMaxY; ++j){
						streets = regions[i][j].getStreets();
						for(k = 0; k < streets.length; ++k){
							street = streets[k];
							streetRegion = street.getMainRegion();
							if(streetRegion==regions[i][j] || streetRegion.getX()<savedRegionMinX || streetRegion.getX()>savedRegionMaxX || streetRegion.getY()<savedRegionMinY || streetRegion.getY()>savedRegionMaxY){	// only paint it if necessary (to prevent painting it multiple times)
								//Step 2.1: Paint intersections which consist of 4 intersection points
								paintArrayList = street.getBridgePaintPolygons();
								if(paintArrayList != null){		
									//FIXME change for black white mode
									//g2d.setColor(Color.white);
									g2d.setColor(street.getDisplayColor());
									for(l = 3; l < paintArrayList.size(); l= l+4){
										int[] xPoints = new int[4];
										int[] yPoints = new int[4];
										xPoints[0]=(int)Math.round(paintArrayList.get(l-3).x);
										yPoints[0]=(int)Math.round(paintArrayList.get(l-3).y);
										xPoints[1]=(int)Math.round(paintArrayList.get(l-2).x);
										yPoints[1]=(int)Math.round(paintArrayList.get(l-2).y);
										xPoints[2]=(int)Math.round(paintArrayList.get(l).x);
										yPoints[2]=(int)Math.round(paintArrayList.get(l).y);
										xPoints[3]=(int)Math.round(paintArrayList.get(l-1).x);
										yPoints[3]=(int)Math.round(paintArrayList.get(l-1).y);
										start[0] = xPoints[0];
										start[1] = yPoints[0];
										end[0] = xPoints[3];
										end[1] = yPoints[3];
										MapHelper.calculateResizedLine(start, end, 50, true, true);
										xPoints[0] = start[0];
										yPoints[0] = start[1];
										xPoints[3] = end[0];
										yPoints[3] = end[1];
										start[0] = xPoints[1];
										start[1] = yPoints[1];
										end[0] = xPoints[2];
										end[1] = yPoints[2];
										MapHelper.calculateResizedLine(start, end, 50, true, true);
										xPoints[1] = start[0];
										yPoints[1] = start[1];
										xPoints[2] = end[0];
										yPoints[2] = end[1];
										g2d.fillPolygon(xPoints, yPoints, 4);			
									}
									g2d.setStroke(new BasicStroke(45,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_MITER));
									//FIXME change for black white mode
									//g2d.setColor(Color.black);
									g2d.setColor(Color.white);
									for(l = 1; l < paintArrayList.size(); l= l+2){
										int x1=(int)Math.round(paintArrayList.get(l-1).x);
										int y1=(int)Math.round(paintArrayList.get(l-1).y);
										int x2=(int)Math.round(paintArrayList.get(l).x);
										int y2=(int)Math.round(paintArrayList.get(l).y);
										g2d.drawLine(x1, y1, x2, y2); 
									}								
								}
								//Step 2.2: Paint intersections which consist of 2 intersection points
								paintArrayList = street.getBridgePaintLines();
								if(paintArrayList != null){									
									g2d.setStroke(new BasicStroke(45,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_MITER));
									//FIXME change for black white mode
									//g2d.setColor(Color.black);
									g2d.setColor(Color.white);
									for(l = 1; l < paintArrayList.size(); l= l+2){
										int x1=(int)Math.round(paintArrayList.get(l-1).x);
										int y1=(int)Math.round(paintArrayList.get(l-1).y);
										int x2=(int)Math.round(paintArrayList.get(l).x);
										int y2=(int)Math.round(paintArrayList.get(l).y);
										g2d.drawLine(x1, y1, x2, y2); 
									}								
								}
							}
						}
					}
				}
			}
			// display RSUs
			if(isShowRSUs() || simulationRunning_){
				g2d.setStroke(new BasicStroke(20,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER));
				g2d.setPaint(rsuColor);
				for(i = regionMinX_; i <= regionMaxX_; ++i){
					for(j = regionMinY_; j <= regionMaxY_; ++j){
						RSU[] rsus = regions[i][j].getRSUs();
						for(int l = 0; l < rsus.length; ++l){	
							if(highlightCommunication_)g2d.drawOval(rsus[l].getX()-rsus[l].getWifiRadius(), rsus[l].getY()-rsus[l].getWifiRadius(),rsus[l].getWifiRadius()*2,rsus[l].getWifiRadius()*2);
							g2d.fillOval(rsus[l].getX() - 250, rsus[l].getY() - 250,500,500);
						}
					}
				}
			}
			
			// display mix entrances. Only active if debugMode is on
			
			if(debugMode){
				g2d.setPaint(Color.PINK);
				g2d.setFont(new Font("SansSerif", Font.PLAIN, 400));
				g2d.setStroke(new BasicStroke(20,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER));
				
				for(i = regionMinX_; i <= regionMaxX_; ++i){
					for(j = regionMinY_; j <= regionMaxY_; ++j){
						ArrayList<String> xxx = regions[i][j].xxx;
						ArrayList<String> yyy = regions[i][j].yyy;
						ArrayList<String> nnn = regions[i][j].nnn;
						if(xxx != null){
			
						for(k = 0; k < xxx.size(); k++){
							g2d.drawOval(Integer.parseInt(xxx.get(k))-300, Integer.parseInt(yyy.get(k))-300,300*2,300*2);
							g2d.drawString(nnn.get(k), Integer.parseInt(xxx.get(k)), Integer.parseInt(yyy.get(k)));
						}
						}
					}
				}
			}
	
				
			// display ARSUs
			if(isShowAttackers() || simulationRunning_){
				g2d.setStroke(new BasicStroke(20,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER));
				g2d.setPaint(arsuColor);
				AttackRSU[] tempARSUList= Vehicle.getArsuList();

				if(tempARSUList.length > 0){
				    for(int l = 0; l < tempARSUList.length;l++) {
						if(highlightCommunication_)g2d.drawOval(tempARSUList[l].getX()-tempARSUList[l].getWifiRadius(), tempARSUList[l].getY()-tempARSUList[l].getWifiRadius(),tempARSUList[l].getWifiRadius()*2,tempARSUList[l].getWifiRadius()*2);
						g2d.fillOval(tempARSUList[l].getX() - 250, tempARSUList[l].getY() - 250,500,500);    	
				      }
				}
			}
			
			
			// display EventSpots
			g2d.setStroke(new BasicStroke(300,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER));
			EventSpot tmpEventSpot = EventSpotList.getInstance().getHead_();
			while(tmpEventSpot != null){
				g2d.setPaint(tmpEventSpot.getEventSpotColor_());
				g2d.drawOval(tmpEventSpot.getX_()-tmpEventSpot.getRadius_(), tmpEventSpot.getY_()-tmpEventSpot.getRadius_(),tmpEventSpot.getRadius_()*2,tmpEventSpot.getRadius_()*2);
				g2d.fillOval(tmpEventSpot.getX_() - 500, tmpEventSpot.getY_() - 500,1000,1000);  
				
				tmpEventSpot = tmpEventSpot.getNext_();
			}
			
			// display Centroids
			g2d.setStroke(new BasicStroke(300,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER));
			g2d.setPaint(Color.red);

			if(centroidsX != null && centroidsY != null){
				for(int r = 0; r < centroidsX.size(); r++){
					g2d.drawOval(centroidsX.get(r).intValue()-500, centroidsY.get(r).intValue()-500,1000,1000);
				}

			}
			
			// display clusters
			g2d.setStroke(new BasicStroke(300,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER));
			
			for(Cluster cluster:clusters){
				g2d.setColor(cluster.getClusterColor());
				g2d.setFont(clusterIDFont_);
				g2d.drawString(cluster.getClusterID_(), cluster.getMinX_() - 500, cluster.getMinY_() - 500);
				g2d.drawRect(cluster.getMinX_(), cluster.getMinY_(), (cluster.getMaxX_() - cluster.getMinX_()), (cluster.getMaxY_()-cluster.getMinY_()));
			}
			
			//display specific cluster
			
			g2d.setStroke(new BasicStroke(25,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER));
			if(showAllClusters){
				for(Cluster cluster:clusters){
					g2d.setColor(cluster.getClusterColor());
					for(int e = 0; e < cluster.getxCoords_().size(); e++){
						
						g2d.drawOval(cluster.getxCoords_().get(e)-500, cluster.getyCoords_().get(e)-500,1000,1000);
					}
				}
			}
			else if(displayCluster_ != null){
				g2d.setColor(displayCluster_.getClusterColor());
				for(int e = 0; e < displayCluster_.getxCoords_().size(); e++){
					
					g2d.drawOval(displayCluster_.getxCoords_().get(e)-500, displayCluster_.getyCoords_().get(e)-500,1000,1000);
				}
			}
			
			if(showAmenities){
				// display amenities
				g2d.setStroke(new BasicStroke(20,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER));
				
				for(Node amenity:Map.getInstance().getAmenityList()){
					g2d.setPaint(amenity.getNodeColor());
					g2d.drawOval(amenity.getX()-200000, amenity.getY()-200000,400000,400000);
					g2d.fillOval(amenity.getX() - 500, amenity.getY() - 500,1000,1000);  
				}
			}
			

			
			if(grid != null){
				int maxMinusmin = maxGridValue - minGridValue;
				int basicStroke = 100;
				double probability = 0;
				int strokeSize = 0;
				
				for(int a = 0; a < grid.length; a++){
					for(int b = 0; b < grid[0].length; b++){
						if(grid[a][b] > 0) 	g2d.setPaint(new Color(255,0,0));
						else g2d.setPaint(new Color(0,255,0));
						
					//	g2d.setPaint(new Color((int)(255 * (((double)grid[a][b] - minEVA)) / maxMinusmin), 255 - (int)(255 * (((double)grid[a][b] - minEVA)) / maxMinusmin), 0));
						//g2d.drawRect(a*10000, b*10000, 10000 - 400, 10000 - 400);
						probability = ((double)grid[a][b] - minGridValue) / maxMinusmin;
						strokeSize = (int) (basicStroke + probability*(gridSize_ - basicStroke));
						g2d.setStroke(new BasicStroke(strokeSize,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER));
						g2d.drawRect(a*gridSize_ - (gridSize_ - strokeSize)/2, b*gridSize_ - (gridSize_ - strokeSize)/2, gridSize_ - strokeSize, gridSize_ - strokeSize);
				
					}
				}
			}

		} else {
			// just fill background
			g2d.setColor(new Color(230,230,230));
			g2d.fillRect(0,0,drawWidth_,drawHeight_);
		}
		g2d.dispose(); // should be disposed to aid garbage collector
	}

	/**
	 * Pans the viewable area.
	 * 
	 * @param direction <code>u</code> to pan up, <code>d</code> to pan down, <code>l</code> to pan left, <code>r</code> to pan right
	 */
	public synchronized void pan(char direction){
		if(direction == 'u'){
			panCountY_ += 1;
			middleY_ -= (drawHeight_/2 / zoom_); 
		}
		else if(direction == 'd'){
			panCountY_ -= 1;
			middleY_ += (drawHeight_/2 / zoom_);
		}
		else if(direction == 'l') {
			panCountX_ += 1;
			middleX_ -= (drawWidth_/2 / zoom_); 
		} else {
			panCountX_ -= 1;
			middleX_ += (drawWidth_/2 / zoom_);
		}
		updateParams();
	}

	/**
	 * Pans the viewable area.
	 * 
	 * @param x the value for how far to pan in x direction (in map scale!)
	 * @param y the value for how far to pan in y direction (in map scale!)
	 */
	public synchronized void pan(double x, double y){
		middleX_ += x;
		middleY_ += y;
		updateParams();
	}

	/**
	 * Sets a new zooming factor.
	 * 
	 * @param zoom the new zooming factor
	 */
	public synchronized void setMapZoom(double zoom){
		if(zoom > 0.0000045 && zoom < 0.5){		//limit zooming by mousewheel into range from 100km to 1m
			zoom_ = zoom;
			updateParams();
			
			// update the image for blockings. The code is taken from 
			// http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html (link last visited: 21.20.2008)
			// and the comments there. getScaledImage() is a little bit better quality but is deprecated according to this as it's slow.
			int size = (int)Math.round(1000 * zoom_);
			if(size < 4) size = 4;
			size = size - (size %2);

			Graphics2D g2;
			BufferedImage tmp;
			BufferedImage tmp2;
			GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            int oldSize, curSize = blockingImage_.getWidth();
            if(size < blockingImage_.getWidth()/2){ // new image is smaller than the original
            	// add some blur
            	float weight = 1.0f/9.0f;
            	float[] elements = new float[9]; 
        	    for (int i = 0; i < 9; i++) {
        	        elements[i] = weight;
        	    }
        	    Kernel blurKernel = new Kernel(3, 3, elements);
        	    scaledBlockingImage_ = gc.createCompatibleImage(blockingImage_.getWidth(), blockingImage_.getHeight(), Transparency.TRANSLUCENT);
        	    new ConvolveOp(blurKernel).filter(blockingImage_, scaledBlockingImage_);
        	    
        	    scaledSlipperyImage_ = gc.createCompatibleImage(slipperyImage_.getWidth(), slipperyImage_.getHeight(), Transparency.TRANSLUCENT);
        	    new ConvolveOp(blurKernel).filter(slipperyImage_, scaledSlipperyImage_);
        	    
        	    // do a multi-step resizing to get higher quality
    	        do {
    	        	oldSize = curSize;
    	            if (curSize > size) {
    	            	curSize /= 2;
    	                if (curSize < size) curSize = size;
    	            }	            

    	            tmp = gc.createCompatibleImage(curSize, curSize, Transparency.TRANSLUCENT);
    	            g2 = tmp.createGraphics();
    	            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    	            g2.drawImage(scaledBlockingImage_, 0, 0, curSize, curSize ,0, 0, oldSize, oldSize, null);
    	            g2.dispose();

    	            scaledBlockingImage_ = tmp;
    	            
    	            tmp2 = gc.createCompatibleImage(curSize, curSize, Transparency.TRANSLUCENT);
    	            g2 = tmp2.createGraphics();
    	            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    	            g2.drawImage(scaledSlipperyImage_, 0, 0, curSize, curSize ,0, 0, oldSize, oldSize, null);
    	            g2.dispose();

    	            scaledSlipperyImage_ = tmp2;
    	            
    	        } while (curSize > size);	
            } else {		// new image is larger than the original
            	scaledBlockingImage_ = gc.createCompatibleImage(size, size, Transparency.TRANSLUCENT);
            	g2 = scaledBlockingImage_.createGraphics();
            	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            	g2.drawImage(blockingImage_, 0, 0, size, size, null);
            	g2.dispose();
            	
            	scaledSlipperyImage_ = gc.createCompatibleImage(size, size, Transparency.TRANSLUCENT);
            	g2 = scaledSlipperyImage_.createGraphics();
            	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            	g2.drawImage(slipperyImage_, 0, 0, size, size, null);
            	g2.dispose();
            }
		}		
	}

	/**
	 * Updates various internal parameters after changes through panning or zooming.<br>
	 * The regions which shall be drawn are calculated here. They are a little bit larger than
	 * normally necessary in order to correctly display communication distance and mix zones. Otherwise,
	 * the distance of a vehicle, which is a little bit outside the currently viewable area wouldn't
	 * be drawn. This all induces a little bit of an unnecessary overdraw to the static objects but this 
	 * should not matter a lot.
	 */
	public synchronized void updateParams(){
		if(map_.getReadyState() == true){
			transform_.setToScale(zoom_, zoom_);		// set the zoom
			transform_.translate(-middleX_ + (drawWidth_ / (zoom_*2)), -middleY_ + (drawHeight_/(zoom_*2)));		// pan and correct center of screen

			lastOverdrawn_ = currentOverdrawn_;
			currentOverdrawn_ = false;
			
			//add a value on top to be able to correctly display communication distance and mix radius
			int addValue = Vehicle.getMaximumCommunicationDistance();
			if(Vehicle.getMixZoneRadius() > addValue) addValue = Vehicle.getMixZoneRadius();
			
			// Minimum x coordinate to be considered for rendering
			long tmp = (long) StrictMath.floor(middleX_ - (drawWidth_ / (zoom_*2))) - addValue;
			if (tmp < 0){
				mapMinX_ = 0;
				currentOverdrawn_ = true;
			} else if(tmp < Integer.MAX_VALUE) mapMinX_ = (int) tmp;
			else mapMinX_ = Integer.MAX_VALUE;

			// Maximum x coordinate to be considered for rendering
			tmp = (long) StrictMath.ceil(middleX_ + (drawWidth_ / (zoom_*2))) + addValue;
			if (tmp < 0) mapMaxX_ = 0;
			else {
				if(tmp > map_.getMapWidth()) currentOverdrawn_ = true;
				if(tmp < Integer.MAX_VALUE) mapMaxX_ = (int) tmp;
				else mapMaxX_ = Integer.MAX_VALUE;
			}
			

			// Minimum y coordinate to be considered for rendering
			tmp = (long) StrictMath.floor(middleY_ - (drawHeight_ / (zoom_*2))) - addValue;
			if (tmp < 0){
				mapMinY_ = 0;		// Map stores only positive coordinates
				currentOverdrawn_ = true;
			} else if(tmp < Integer.MAX_VALUE) mapMinY_ = (int) tmp;
			else mapMinY_ = Integer.MAX_VALUE;

			// Maximum y coordinate to be considered for rendering
			tmp = (long) StrictMath.ceil(middleY_ + (drawHeight_ / (zoom_*2))) + addValue;
			if (tmp < 0) mapMaxY_ = 0;
			else {
				if(tmp > map_.getMapHeight()) currentOverdrawn_ = true;
				if(tmp < Integer.MAX_VALUE) mapMaxY_ = (int) tmp;
				else mapMaxY_ = Integer.MAX_VALUE;
			}
			

			// Get the regions to be considered for rendering
			Region tmpregion = map_.getRegionOfPoint(mapMinX_, mapMinY_);
			regionMinX_ = tmpregion.getX();
			regionMinY_ = tmpregion.getY();

			tmpregion = map_.getRegionOfPoint(mapMaxX_, mapMaxY_);
			regionMaxX_ = tmpregion.getX();
			regionMaxY_ = tmpregion.getY();
		}
	}
	
	/**
	 * Schedules an update of the {@link DrawingArea}. Note that depending on if a simulation is running or not, the update
	 * might be performed at a later time!
	 * 
	 * @param fullRender 	<code>true</code> if a full update including all static objects should be done, else <code>false</code>
	 * @param forceRenderNow <code>true</code> to force an immediate update regardless of consistency considerations (should only be used by the {@link vanetsim.simulation.SimulationMaster})
	 */
	public void ReRender(boolean fullRender, boolean forceRenderNow){
		if(fullRender) scheduleFullRender_ = true;
		if (drawArea_ != null){
			if(!simulationRunning_ || forceRenderNow){
				if(scheduleFullRender_){
					scheduleFullRender_ = false;
					drawArea_.prepareBufferedImages();					
				}
				doPaintInitializedBySimulation_ = true;
				drawArea_.repaint();
			}
		}
	}

	/**
	 * A method to move the camera to a specific region
	 */
	
	public void moveCamera(double middleX, double middleY, double zoom){
		middleX_ = middleX;
		middleY_ = middleY;
		
		setMapZoom(zoom);
	}

	/**
	 * Gets the x coordinate of the middle of the current view.
	 * 
	 * @return the x coordinate in map scale
	 */
	public double getMiddleX(){
		return middleX_;
	}

	/**
	 * Gets the y coordinate of the middle of the current view.
	 * 
	 * @return the y coordinate in map scale
	 */
	public double getMiddleY(){
		return middleY_;
	}
	
	/**
	 * Gets the current zooming factor.
	 * 
	 * @return the current zooming factor
	 */
	public double getMapZoom(){
		return zoom_;
	}

	/**
	 * Gets the time passed since simulation start.
	 * 
	 * @return the time passed in milliseconds
	 */
	public int getTimePassed(){
		return timePassed_;
	}


	/**
	 * Gets the currently active coordinate transformation.
	 * 
	 * @return the transform
	 */
	public AffineTransform getTransform(){
		return transform_;
	}

	/**
	 * Notify if the simulation is running or not.
	 * 
	 * @param running	<code>true</code> if a simulation is currently running, <code>false</code> if it's suspended
	 */
	public void notifySimulationRunning(boolean running){
		simulationRunning_ = running;
	}
	
	/**
	 * Sets a new marked street.
	 * 
	 * @param markedStreet the street to mark
	 */
	public synchronized void setMarkedStreet(Street markedStreet){
		markedStreet_ = markedStreet;	
	}

	/**
	 * Sets a new marked vehicle.
	 * 
	 * @param markedVehicle the vehicle to mark
	 */
	public synchronized void setMarkedVehicle(Vehicle markedVehicle){
		markedVehicle_ = markedVehicle;
	}

	/**
	 * Gets a marked vehicle.
	 *
	 */
	public synchronized Vehicle getMarkedVehicle(){
		return markedVehicle_;
	}
	
	/**
	 * Sets a new attacker vehicle.
	 * 
	 * @param attackerVehicle the attacker vehicle
	 */
	public synchronized void setAttackerVehicle(Vehicle attackerVehicle){
		attackerVehicle_ = attackerVehicle;
	}

	/**
	 * Gets the attacker vehicle.
	 *
	 */
	public synchronized Vehicle getAttackerVehicle(){
		return attackerVehicle_;
	}
	
	/**
	 * Sets the coordinates of the center of the viewable area.
	 * 
	 * @param x	the new x coordinate for the center of the viewable area
	 * @param y	the new y coordinate for the center of the viewable area
	 */
	public synchronized void setMiddle(int x, int y){
		middleX_ = x;
		middleY_ = y;		
		updateParams();
	}
	
	/**
	 * Sets the {@link DrawingArea} this Renderer is associated with.
	 * 
	 * @param drawArea 	the area on which this Renderer draws
	 */
	public void setDrawArea(DrawingArea drawArea){
		drawArea_ = drawArea;
	}

	/**
	 * Set the height of the {@link DrawingArea}.
	 * 
	 * @param drawHeight the new height
	 */
	public void setDrawHeight(int drawHeight) {
		drawHeight_ = drawHeight;
	}

	/**
	 * Set the width of the {@link DrawingArea}.
	 * 
	 * @param drawWidth the new width
	 */
	public void setDrawWidth(int drawWidth) {
		drawWidth_ = drawWidth;
	}
	
	/**
	 * Sets the time passed since simulation start.
	 * 
	 * @param timePassed the new time in milliseconds
	 */
	public void setTimePassed(int timePassed){
		timePassed_ = timePassed;
	}

	/**
	 * Sets the barrier for synchronization with the SimulationMaster.
	 * 
	 * @param barrier the barrier to use
	 */
	public void setBarrierForSimulationMaster(CyclicBarrier barrier){
		barrierForSimulationMaster_ = barrier;
	}

	/**
	 * If all nodes shall be highlighted.
	 * 
	 * @param highlightNodes <code>true</code> if you want to highlight nodes, else <code>false</code>
	 */
	public void setHighlightNodes(boolean highlightNodes){
		highlightAllNodes_ = highlightNodes;
	}
	
	/**
	 * If circles shall be displayed to show communication distance.
	 * 
	 * @param highlightCommunication <code>true</code> if you want to show the circles, else <code>false</code>
	 */
	public void setHighlightCommunication(boolean highlightCommunication){
		highlightCommunication_ = highlightCommunication;
	}
	
	/**
	 * If filled circles shall be displayed to hide the mix zones.
	 * 
	 * @param hideMixZones <code>true</code> if you want to show the circles, else <code>false</code>
	 */
	public void setHideMixZones(boolean hideMixZones){
		hideMixZones_ = hideMixZones;
	}
	
	/**
	 * If the IDs of the vehicle shall be drawn on the map.
	 * 
	 * @param displayVehicleIDs <code>true</code> if you want to show the IDs, else <code>false</code>
	 */
	public void setDisplayVehicleIDs(boolean displayVehicleIDs){
		displayVehicleIDs_ = displayVehicleIDs;
	}

	/**
	 * If you want to show all blockings.
	 * 
	 * @param showAllBlockings <code>true</code> if you want to show all blockings, else <code>false</code>
	 */
	public void setShowAllBlockings(boolean showAllBlockings){
		showAllBlockings_ = showAllBlockings;
	}
	
	/**
	 * If you want to display the monitored beacon zone.
	 * 
	 * @param showBeaconMonitorZone	<code>true</code> if you want to display the monitored beacon zones, else <code>false</code>
	 */
	public void setShowBeaconMonitorZone(boolean showBeaconMonitorZone){
		showBeaconMonitorZone_ = showBeaconMonitorZone;
	}
	
	/**
	 * Sets the values for the monitored beacon zone. A rectangular bounding box within the specified coordinates
	 * is monitored if {@link #setShowBeaconMonitorZone(boolean)} is set to <code>true</code>.
	 * 
	 * @param beaconMonitorMinX	the minimum x coordinate
	 * @param beaconMonitorMaxX	the maximum x coordinate
	 * @param beaconMonitorMinY	the minimum y coordinate
	 * @param beaconMonitorMaxY	the maximum y coordinate
	 */
	public void setMonitoredBeaconZoneVariables(int beaconMonitorMinX, int beaconMonitorMaxX, int beaconMonitorMinY, int beaconMonitorMaxY){
		beaconMonitorMinX_ = beaconMonitorMinX;
		beaconMonitorMaxX_ = beaconMonitorMaxX;
		beaconMonitorMinY_ = beaconMonitorMinY;
		beaconMonitorMaxY_ = beaconMonitorMaxY;
	}

	/**
	 * If you want to show all vehicles.
	 * 
	 * @param showVehicles <code>true</code> if you want to show all vehicles, else <code>false</code>
	 */
	public void setShowVehicles(boolean showVehicles) {
		showVehicles_ = showVehicles;
	}

	/**
	 * Gets if vehicles are displayed.
	 * 
	 * @return <code>true</code> if vehicles are displayed
	 */
	public boolean isShowVehicles() {
		return showVehicles_;
	}
	
	/**
	 * If you want to show all mix zones.
	 * 
	 * @param showMixZones <code>true</code> if you want to show all mix zones, else <code>false</code>
	 */
	public void setShowMixZones(boolean showMixZones) {
		showMixZones_ = showMixZones;
	}

	/**
	 * Gets if mix zones are displayed
	 * 
	 * @return true if mix zones are displayed
	 */
	public boolean isShowMixZones() {
		return showMixZones_;
	}

	/**
	 * If you want to add mix zones to all street corners automatically.
	 * 
	 * @param autoAddMixZones <code>true</code> if you want to add mix zones to all street corners automatically else <code>false</code>
	 */
	public void setAutoAddMixZones(boolean autoAddMixZones) {
		autoAddMixZones_ = autoAddMixZones;
	}

	/**
	 * Gets if mix zones are added automatically on each street corner
	 * 
	 * @return true if mix zones are added automatically
	 */
	public boolean isAutoAddMixZones() {
		return autoAddMixZones_;
	}

	/**
	 * If you want to show all RSUs.
	 * 
	 * @param showRSUs <code>true</code> if you want to show all RSUs, else <code>false</code>
	 */
	public void setShowRSUs(boolean showRSUs) {
		showRSUs_ = showRSUs;
	}

	/**
	 * Gets if RSUs are displayed
	 * 
	 * @return true if RSUs are displayed
	 */
	public boolean isShowRSUs() {
		return showRSUs_;
	}
	
	public Vehicle getAttackedVehicle() {
		return attackedVehicle_;
	}

	public void setAttackedVehicle(Vehicle attackedVehicle_) {
		this.attackedVehicle_ = attackedVehicle_;
	}

	public boolean isShowAttackers() {
		return showAttackers_;
	}

	public void setShowAttackers(boolean showAttackers_) {
		this.showAttackers_ = showAttackers_;
	}

	public boolean isConsoleStart() {
		return consoleStart;
	}

	public void setConsoleStart(boolean consoleStart) {
		this.consoleStart = consoleStart;
	}

	/**
	 * @param markedJunction_ the markedJunction_ to set
	 */
	public void setMarkedJunction_(Junction markedJunction_) {
		this.markedJunction_ = markedJunction_;
	}

	/**
	 * @return the markedJunction_
	 */
	public Junction getMarkedJunction_() {
		return markedJunction_;
	}

	public ArrayList<String> getLocationInformationMix() {
		return locationInformationMix_;
	}

	public void setLocationInformationMix(ArrayList<String> locationInformation) {
		this.locationInformationMix_ = locationInformation;
	}

	public ArrayList<String> getLocationInformationSilentPeriod_() {
		return locationInformationSilentPeriod_;
	}

	public void setLocationInformationSilentPeriod_(
			ArrayList<String> locationInformationSilentPeriod_) {
		this.locationInformationSilentPeriod_ = locationInformationSilentPeriod_;
	}

	public int getMixZoneAmount() {
		return mixZoneAmount;
	}

	public void setMixZoneAmount(int mixZoneAmount) {
		this.mixZoneAmount = mixZoneAmount;
	}

	/**
	 * @return the grid
	 */
	public int[][] getGrid() {
		return grid;
	}

	/**
	 * @param grid the grid to set
	 */
	public void setGrid(int[][] grid) {
		this.grid = grid;
	}

	/**
	 * @return the minGridValue
	 */
	public int getMinGridValue() {
		return minGridValue;
	}

	/**
	 * @param minGridValue the minGridValue to set
	 */
	public void setMinGridValue(int minGridValue) {
		this.minGridValue = minGridValue;
	}

	/**
	 * @return the maxGridValue
	 */
	public int getMaxGridValue() {
		return maxGridValue;
	}

	/**
	 * @param maxGridValue the maxGridValue to set
	 */
	public void setMaxGridValue(int maxGridValue) {
		this.maxGridValue = maxGridValue;
	}

	/**
	 * @return the gridSize_
	 */
	public int getGridSize_() {
		return gridSize_;
	}

	/**
	 * @param gridSize_ the gridSize_ to set
	 */
	public void setGridSize_(int gridSize_) {
		this.gridSize_ = gridSize_;
	}

	public boolean isShowPenaltyConnections_() {
		return showPenaltyConnections_;
	}

	public void setShowPenaltyConnections_(boolean showPenaltyConnections_) {
		this.showPenaltyConnections_ = showPenaltyConnections_;
	}

	public boolean isShowKnownVehiclesConnections_() {
		return showKnownVehiclesConnections_;
	}

	public void setShowKnownVehiclesConnections_(
			boolean showKnownVehiclesConnections_) {
		this.showKnownVehiclesConnections_ = showKnownVehiclesConnections_;
	}

	public ArrayList<Double> getCentroidsX() {
		return centroidsX;
	}

	public void setCentroidsX(ArrayList<Double> centroidsX) {
		this.centroidsX = centroidsX;
	}

	public ArrayList<Double> getCentroidsY() {
		return centroidsY;
	}

	public void setCentroidsY(ArrayList<Double> centroidsY) {
		this.centroidsY = centroidsY;
	}

	public ArrayList<Cluster> getClusters() {
		return clusters;
	}

	public Cluster getDisplayCluster_() {
		return displayCluster_;
	}

	public void setDisplayCluster_(Cluster displayCluster_) {
		this.displayCluster_ = displayCluster_;
	}

	public void setShowAllClusters(boolean showAllClusters) {
		this.showAllClusters = showAllClusters;
	}

	public boolean getMDSMode_() {
		return MDSMode_;
	}

	public void setMDSMode_(boolean mDSMode_) {
		MDSMode_ = mDSMode_;
	}

	public ArrayList<String> getLocationInformationMDS_() {
		return locationInformationMDS_;
	}

	public void setLocationInformationMDS_(ArrayList<String> locationInformationMDS_) {
		this.locationInformationMDS_ = locationInformationMDS_;
	}

	public int getDrawWidth_() {
		return drawWidth_;
	}

	public void setDrawWidth_(int drawWidth_) {
		this.drawWidth_ = drawWidth_;
	}

	public int getDrawHeight_() {
		return drawHeight_;
	}

	public void setDrawHeight_(int drawHeight_) {
		this.drawHeight_ = drawHeight_;
	}


}