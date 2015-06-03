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
import java.text.ParseException;
import java.util.Random;
import java.util.ArrayDeque;

import vanetsim.ErrorLog;
import vanetsim.gui.Renderer;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.map.Region;
import vanetsim.routing.WayPoint;
import vanetsim.scenario.Vehicle;
import vanetsim.scenario.messages.PenaltyMessage;
import vanetsim.simulation.SimulationMaster;


/**
 * An abstract class for events.
 */
public class EventSpot{
	
	/** Frequency the spot gets active. */
	private int frequency_;
	
	/** radius of the spot */
	private  int radius_;
	
	/** the x coordinate of the event spot */
	private int x_;
	
	/** the y coordinate of the event spot */
	private int y_;
	
	/** the type of event spot */
	private String eventSpotType_;
	
	/** the color of event spot */
	private Color eventSpotColor_;
	
	/** the next eventSpot timing */
	private int eventSpotTiming_;
	
	/** a reference to the next eventspot in the list */
	private EventSpot next_ = null;
	
	/** The hospital color */
	private static Color hospitalColor = Color.magenta;
	
	/** The icy road color */
	private static Color icyRoadColor = Color.cyan;
	
	/** The damaged road color */
	private static Color damagedRoadColor = Color.gray;
	
	/** The school zone color */
	private static Color schoolColor = Color.yellow;
	
	/** The fire station zone color */
	private static Color fire_stationColor = Color.red;
	
	/** The police zone color */
	private static Color policeColor = Color.blue;
	
	/** The school zone color */
	private static Color kindergartenColor = Color.green;
	
	/** A reference to the map so that we don't need to call this over and over again. */
	private static final Map MAP = Map.getInstance();

	/** the map regions */
	private static Region[][] regions_;
	
	/** the random number generator seed */
	private long seed_;
	
	/** A random number generator used for the creation of emergency vehicles. */
	private Random random_ = null;
	
	/** EventSpot Multiplier */
	private boolean multiplier_ = true;
	
	public EventSpot(int x, int y, int frequency, int radius, String eventSpotType, long seed){
		seed_ = seed;
		x_ = x;
		y_ = y;
		frequency_ = frequency;
		if(seed != -1)	random_ = new Random(seed);
		eventSpotTiming_ = random_.nextInt(frequency) + 1;
		if(SimulationMaster.getEventSpotCountdown_() > eventSpotTiming_) SimulationMaster.setEventSpotCountdown_(eventSpotTiming_); 
		radius_ = radius;
		eventSpotType_ = eventSpotType;
		if(eventSpotType.equals("hospital")) eventSpotColor_ = hospitalColor;
		else if(eventSpotType.equals("ice")) eventSpotColor_ = icyRoadColor;
		else if(eventSpotType.equals("streetDamage")) eventSpotColor_ = damagedRoadColor;
		else if(eventSpotType.equals("school")) eventSpotColor_ = schoolColor;
		else if(eventSpotType.equals("fire_station")) eventSpotColor_ = fire_stationColor;
		else if(eventSpotType.equals("police")) eventSpotColor_ = policeColor;
		else if(eventSpotType.equals("kindergarten")) eventSpotColor_ = kindergartenColor;

	}
	
	public void execute(int timePassed){
		eventSpotTiming_ += frequency_;
		
		if(eventSpotType_.equals("hospital") || eventSpotType_.equals("police") || eventSpotType_.equals("fire_station")){
			int maxX = x_ + radius_;
			int maxY = y_ + radius_;
			int minX = x_ - radius_;
			int minY = y_ - radius_;
	    	long dx = 0;
	    	long dy = 0;
	    	long maxCommDistanceSquared = (long)radius_*(long)radius_;
	    	int randomX = 0;
	    	int randomY = 0;
	   	    	
	    	
	    	WayPoint tmpWayPoint = null;

			ArrayDeque<WayPoint> destinations = null;
			Vehicle tmpVehicle;
			
			int k = 0;	
			int j = 0;
			destinations = new ArrayDeque<WayPoint>(2);	
			
			try {
				destinations.add(new WayPoint(x_,y_,0));
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			while(j < 1 && k < 99999){	// if snapping fails more than 99999 times break
				try{
					++k;
					
					while(true){
						randomX = minX + random_.nextInt((maxX - minX));
						randomY = minY + random_.nextInt((maxY - minY));
				    	dx = x_ - randomX;
				    	dy = y_ - randomY;
				    	if((dx * dx + dy * dy) <= maxCommDistanceSquared)	break;

					}
					
					tmpWayPoint = new WayPoint(randomX, randomY, 0);
					j++;
					destinations.add(tmpWayPoint);
				} catch (Exception e) {}
			}
			
			try {
				destinations.add(new WayPoint(x_,y_,0));
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if(k < 99999) {
				try {
					tmpVehicle = new Vehicle(destinations, 600, 4722, 10000, true, true, 800, 300, 100, 0, 833,  new Color(124,148,235), false, "");
					tmpVehicle.setDoNotRecycle_(true);
					Map.getInstance().addVehicle(tmpVehicle);
				} catch (Exception e) {e.printStackTrace();}				
			}
		}
		else if(eventSpotType_.equals("ice") || eventSpotType_.equals("streetDamage")){
			int maxX = x_ + radius_;
			int maxY = y_ + radius_;
			int minX = x_ - radius_;
			int minY = y_ - radius_;
	    	long dx = 0;
	    	long dy = 0;
	    	long maxCommDistanceSquared = (long)radius_*(long)radius_;
	    	int randomX = 0;
	    	int randomY = 0;

			int k = 0;	
			int j = 0;
	
			while(j < 1 && k < 99999){	// if snapping fails more than 99999 times break
				try{
					++k;
					
					while(true){
						randomX = minX + random_.nextInt((maxX - minX));
						randomY = minY + random_.nextInt((maxY - minY));
				    	dx = x_ - randomX;
				    	dy = y_ - randomY;
				    	if((dx * dx + dy * dy) <= maxCommDistanceSquared){
				    		break;
				    	}
					}
					try{
						EventList.getInstance().addEvent(new StartBlocking((timePassed), randomX, randomY, 0, 99, false, "HUANG_RHCN")); //$NON-NLS-1$
						EventList.getInstance().addEvent(new StopBlocking((timePassed + frequency_),randomX,randomY)); //$NON-NLS-1$

						j++;
					}catch (Exception e2) { ErrorLog.log(Messages.getString("EditEventControlPanel.errorCreatingEvent"), 6, getClass().getName(), "actionPerformed", e2);} //$NON-NLS-1$ //$NON-NLS-2$
				} catch (Exception e) {}
			}
			Renderer.getInstance().ReRender(false, false);
		}
		else if(eventSpotType_.equals("school") || eventSpotType_.equals("kindergarten")){
			int RegionMinX, RegionMinY, RegionMaxX, RegionMaxY;
			long dx, dy, maxDistanceSquared = (long)radius_ * radius_;

			// Get the regions to be considered
			Region tmpregion = MAP.getRegionOfPoint((x_ - radius_), (y_ - radius_));
			RegionMinX = tmpregion.getX();
			RegionMinY = tmpregion.getY();

			tmpregion = MAP.getRegionOfPoint((x_ + radius_), (y_ + radius_));
			RegionMaxX = tmpregion.getX();
			RegionMaxY = tmpregion.getY();
			
			int i = 0;
			Vehicle tmpVehicle = null;
			while(i < 100){		//there could be some vehicles in the region but no vehicle in the zone!
				i++;
				Vehicle[] tmpVehicleArray = regions_[(RegionMinX + random_.nextInt(RegionMaxX - RegionMinX + 1))][(RegionMinY + random_.nextInt(RegionMaxY - RegionMinY + 1))].getVehicleArray();
				tmpVehicle = tmpVehicleArray[random_.nextInt(tmpVehicleArray.length)];
				
				//check if vehicle is driving. Otherwise a emergency braking does not make any sense!
				if(tmpVehicle.getCurSpeed() != 0){
					dx = tmpVehicle.getX() - x_;
					dy = tmpVehicle.getY() - y_;
					
					if((dx*dx + dy*dy) <= maxDistanceSquared) {

						PenaltyMessage message = new PenaltyMessage(tmpVehicle.getX(), tmpVehicle.getY(), tmpVehicle.getX(), tmpVehicle.getY(), 50000, (timePassed + 2000), tmpVehicle.getCurStreet(), tmpVehicle.getCurLane(), (int) tmpVehicle.getCurPosition(), 500, (timePassed + 2000), false, tmpVehicle.getID(), null,  "HUANG_EEBL", false, true);							
						tmpVehicle.getKnownMessages().addMessage(message, false, true);
						message.setFloodingMode(true);	// enable flooding mode if within distance!				
					
						tmpVehicle.setEmergencyBraking_(true);
						tmpVehicle.setEEBLmessageIsCreated_(true);
						tmpVehicle.setEmergencyBrakingCountdown_(tmpVehicle.getEmergencyBrakingDuration_());
						if(!multiplier_) i = 100;
					}
				}
			}
		}
	}
	
	/**
	 * Gets the frequency of the event
	 * 
	 * @return the frequency
	 */
	public int getFrequency_(){
		return frequency_;
	}
	
	/**
	 * Gets the radius of the event spot
	 * 
	 * @return the radius of the spot
	 * 
	 * @see vanetsim.scenario.events.EventSpot#getRadius_()
	 */
	public int getRadius_() {
		return radius_;
	}

	/**
	 * @return the next_
	 */
	public EventSpot getNext_() {
		return next_;
	}

	/**
	 * @param next_ the next_ to set
	 */
	public void setNext_(EventSpot next_) {
		this.next_ = next_;
	}

	/**
	 * @return the x_
	 */
	public int getX_() {
		return x_;
	}

	/**
	 * @param x_ the x_ to set
	 */
	public void setX_(int x_) {
		this.x_ = x_;
	}

	/**
	 * @return the y_
	 */
	public int getY_() {
		return y_;
	}

	/**
	 * @param y_ the y_ to set
	 */
	public void setY_(int y_) {
		this.y_ = y_;
	}

	/**
	 * @return the eventSpotType_
	 */
	public String getEventSpotType_() {
		return eventSpotType_;
	}

	/**
	 * @param eventSpotType_ the eventSpotType_ to set
	 */
	public void setEventSpotType_(String eventSpotType_) {
		this.eventSpotType_ = eventSpotType_;
	}

	/**
	 * @return the eventSpotColor_
	 */
	public Color getEventSpotColor_() {
		return eventSpotColor_;
	}

	/**
	 * @param eventSpotColor_ the eventSpotColor_ to set
	 */
	public void setEventSpotColor_(Color eventSpotColor_) {
		this.eventSpotColor_ = eventSpotColor_;
	}

	/**
	 * @return the eventSpotTiming_
	 */
	public int getEventSpotTiming_() {
		return eventSpotTiming_;
	}

	/**
	 * @param eventSpotTiming_ the eventSpotTiming_ to set
	 */
	public void setEventSpotTiming_(int eventSpotTiming_) {
		this.eventSpotTiming_ = eventSpotTiming_;
	}

	/**
	 * @param regions_ the regions_ to set
	 */
	public static void setRegions_(Region[][] regions) {
		regions_ = regions;
	}

	public long getSeed_() {
		return seed_;
	}

	public  void setSeed_(long seed) {
		seed_ = seed;
	}
}