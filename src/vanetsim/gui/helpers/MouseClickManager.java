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
package vanetsim.gui.helpers;

import java.awt.Cursor;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;

import vanetsim.VanetSimStart;
import vanetsim.gui.DrawingArea;
import vanetsim.gui.Renderer;
import vanetsim.gui.controlpanels.EditControlPanel;
import vanetsim.gui.controlpanels.ReportingControlPanel;
import vanetsim.gui.controlpanels.SimulateControlPanel;
import vanetsim.localization.Messages;
import vanetsim.map.MapHelper;
import vanetsim.map.Node;
import vanetsim.map.Street;
import vanetsim.routing.WayPoint;
import vanetsim.scenario.Vehicle;

/**
 * A class to correctly handle mouseclicks and drags on the DrawingArea. Furthermore, this class also handles the display in the
 * information text area.
 */
public final class MouseClickManager extends Thread{

	/** The only instance of this class (singleton). */
	private static final MouseClickManager INSTANCE = new MouseClickManager();
	
	/** How often the information panel is refreshed in milliseconds (only achieved approximately!). */
	private static final int INFORMATION_REFRESH_INTERVAL = 800;
	
	/** After which time dragging shall be activated (in milliseconds) */
	private static final int DRAG_ACTIVATION_INTERVAL = 140;
	
	/** A formatter for integers with fractions */
	private static final DecimalFormat INTEGER_FORMAT_FRACTION = new DecimalFormat(",##0.00"); //$NON-NLS-1$
	
	/** A reference to the edit control panel. */
	private final EditControlPanel editPanel_ = VanetSimStart.getMainControlPanel().getEditPanel();
	
	/** A reference to the reporting control panel. */
	private final ReportingControlPanel reportPanel_ = VanetSimStart.getMainControlPanel().getReportingPanel();

	/** A StringBuilder for the information text. Reused to prevent creating lots of garbage */
	private final StringBuilder informationText_ = new StringBuilder();
	
	/** The default mouse cursor. */
	private Cursor defaultCursor_ = new Cursor(Cursor.DEFAULT_CURSOR);
	
	/** The move mouse cursor. */
	private Cursor moveCursor_ = new Cursor(Cursor.MOVE_CURSOR);

	/** The DrawingArea (needed to change mouse cursor). */
	private DrawingArea drawArea_ = null;

	/** <code>true</code> if this manager currently is active,<code>false</code> if it's inactive. */
	public boolean active_ = false;
	
	/** The time when mouse button was last pressed. */
	private long pressTime_ = 0;
	
	/** The x coordinate where mouse was pressed. */
	private int pressedX_ = -1;
	
	/** The y coordinate where mouse was pressed. */
	private int pressedY_ = -1;

	/** The time when mouse button was last released. */
	private long releaseTime_ = 0;

	/** The last x coordinate where mouse was released. */
	private int releasedX_ = -1;
	
	/** The last y coordinate where mouse was released. */
	private int releasedY_ = -1;	

	/** The time already waited to change from default cursor. If set to <code>-1</code> changing between the cursors is disabled. */
	private int waitingTime_ = -1;
	
	/** The marked node. */
	private Node markedNode_ = null;
	
	/** The marked street. */
	private Street markedStreet_ = null;
	
	/** The marked vehicle. */
	private Vehicle markedVehicle_ = null;
	
	/** The information about a street is cached here as it doesn't change that often. */
	private String cachedStreetInformation_ = ""; //$NON-NLS-1$
	
	/** Which street is currently cached */
	private Street cachedStreet_ = null;

	/**
	 * Gets the single instance of this manager.
	 * 
	 * @return single instance of this manager
	 */
	public static MouseClickManager getInstance(){
		return INSTANCE;
	}

	/**
	 * Empty, private constructor in order to disable instancing.
	 */
	private MouseClickManager(){
	}

	/**
	 * Sets the value for the <code>isActive</code> variable.
	 * 
	 * @param active	<code>true</code> to signal this thread that the DrawingArea has been entered,<code>false</code> to signal that the area was left
	 */
	public void setActive(boolean active){
		active_ = active;
		if(active_ == false && drawArea_ != null){
			waitingTime_ = -1;
			drawArea_.setCursor(defaultCursor_);	//to be sure that cursor is right if leaving the area
		}
	}

	/**
	 * Sets the {@link vanetsim.gui.DrawingArea} this MouseClickManager is associated with.
	 * 
	 * @param drawArea	the area on which this MouseClickManager operates
	 */
	public void setDrawArea(DrawingArea drawArea){
		drawArea_ = drawArea;
	}

	/**
	 * Signals this manager that the mouse was pressed. If the edit mode is currently active, the click is forwarded to the edit panel.
	 * 
	 * @param x	the x coordinate where mouse was pressed
	 * @param y	the y coordinate where mouse was pressed
	 */
	public synchronized void signalPressed(int x, int y){
		try{
			Point2D.Double mapposition_source = new Point2D.Double(0,0);
			Renderer.getInstance().getTransform().inverseTransform(new Point2D.Double(x,y), mapposition_source);
			boolean onEditingTab;
			if(VanetSimStart.getMainControlPanel().getSelectedTabComponent() instanceof EditControlPanel) onEditingTab = true;
			else onEditingTab = false;
			if(editPanel_.getEditMode() && onEditingTab){	//editing enabled? then forward the transformed coordinates to the editing panel
				editPanel_.receiveMouseEvent((int)Math.round(mapposition_source.getX()),(int)Math.round(mapposition_source.getY()));
			} else if(reportPanel_.isInMonitoredMixZoneEditMode()){
				reportPanel_.receiveMouseEvent((int)Math.round(mapposition_source.getX()),(int)Math.round(mapposition_source.getY()));
			} else {
				waitingTime_ = 0;	//to enable cursor change (cursor changes to indicate dragging)
				pressedX_ = (int)StrictMath.floor(0.5 + mapposition_source.getX());
				pressedY_ = (int)StrictMath.floor(0.5 + mapposition_source.getY());
				pressTime_ = System.currentTimeMillis();
				releaseTime_ = pressTime_;
			}
		} catch (Exception e){}		
	}

	/**
	 * Signals this manager that the mouse was released (used for dragging).
	 * 
	 * @param x	the x coordinate where mouse was released
	 * @param y	the y coordinate where mouse was released
	 */
	public synchronized void signalReleased(int x, int y){
		boolean onEditingTab;
		if(VanetSimStart.getMainControlPanel().getSelectedTabComponent() instanceof EditControlPanel) onEditingTab = true;
		else onEditingTab = false;
		if((!editPanel_.getEditMode() || !onEditingTab) && !reportPanel_.isInMonitoredMixZoneEditMode()){	//dragging only enabled when not editing!
			try{
				Point2D.Double mapposition_source = new Point2D.Double(0,0);
				Renderer.getInstance().getTransform().inverseTransform(new Point2D.Double(x,y), mapposition_source);
				waitingTime_ = -1;
				releasedX_ = (int)StrictMath.floor(0.5 + mapposition_source.getX());
				releasedY_ = (int)StrictMath.floor(0.5 + mapposition_source.getY());
				releaseTime_ = System.currentTimeMillis();
				if(drawArea_ != null) drawArea_.setCursor(defaultCursor_);
			} catch (Exception e){}	
		}
	}
	
	/**
	 * Cleans markings so that objects can be deleted through garbage collector.
	 */
	public void cleanMarkings(){
		markedVehicle_ = null;
		markedStreet_ = null;
		markedNode_ = null;
	}
	
	/**
	 * Creates the information text from the marked street and vehicle. The function recycles a StringBuilder and appends
	 * strings so that practically no garbage collection is needed.
	 * 
	 * @return	the text
	 */
	private synchronized final String createInformationString(){
		double dx, dy;
		WayPoint tmpWayPoint;
		informationText_.setLength(0);	//clear without constructing a new one. Very efficient as max capacity of StringBuilder stays allocated, only the length variable gets changed (=> doesn't need to expand through array copys!) 
		if(markedStreet_ == cachedStreet_){
			informationText_.append(cachedStreetInformation_);
		} else {		
			if(markedStreet_ != null){
				informationText_.append(Messages.getString("MouseClickManager.streetInformation")); //$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.streetName"));	//using append on our own as otherwise (using "+") lots of new temporary StringBuilders are created. //$NON-NLS-1$
				informationText_.append(markedStreet_.getName());
				informationText_.append("\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.streetLength")); //$NON-NLS-1$
				informationText_.append(INTEGER_FORMAT_FRACTION.format(markedStreet_.getLength()/100));
				informationText_.append(" m\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.streetSpeed")); //$NON-NLS-1$
				informationText_.append(INTEGER_FORMAT_FRACTION.format(markedStreet_.getSpeed()/100000.0*3600));
				informationText_.append(" km/h\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.lanesPerDirection")); //$NON-NLS-1$
				informationText_.append(markedStreet_.getLanesCount());
				informationText_.append("\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.streetStart")); //$NON-NLS-1$
				informationText_.append(markedStreet_.getStartNode().getX());
				informationText_.append(" (x), ");	//$NON-NLS-1$
				informationText_.append(markedStreet_.getStartNode().getY());
				informationText_.append(" (y)");	//$NON-NLS-1$
				if(markedStreet_.getStartNode().getJunction() != null) informationText_.append(Messages.getString("MouseClickManager.junction")); //$NON-NLS-1$
				informationText_.append("\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.crossingsOutgoings")); //$NON-NLS-1$
				informationText_.append(markedStreet_.getStartNode().getCrossingStreetsCount());
				informationText_.append("/"); //$NON-NLS-1$
				informationText_.append(markedStreet_.getStartNode().getOutgoingStreetsCount());
				informationText_.append("\n"); //$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.streetEnd")); //$NON-NLS-1$
				informationText_.append(markedStreet_.getEndNode().getX());
				informationText_.append(" (x),");	//$NON-NLS-1$
				informationText_.append(markedStreet_.getEndNode().getY());
				informationText_.append(" (y)");	//$NON-NLS-1$
				if(markedStreet_.getEndNode().getJunction() != null) informationText_.append(Messages.getString("MouseClickManager.junction")); //$NON-NLS-1$
				informationText_.append("\n");	//$NON-NLS-1$
				informationText_.append(Messages.getString("MouseClickManager.crossingsOutgoings")); //$NON-NLS-1$
				informationText_.append(markedStreet_.getEndNode().getCrossingStreetsCount());
				informationText_.append("/"); //$NON-NLS-1$
				informationText_.append(markedStreet_.getEndNode().getOutgoingStreetsCount());
				informationText_.append("\n"); //$NON-NLS-1$
			}
			cachedStreet_ = markedStreet_;
			cachedStreetInformation_ = informationText_.toString();			
		}
		if(markedVehicle_ != null){
			if(informationText_.length() != 0) informationText_.append("\n"); //$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.vehicleInformation")); //$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.vehicleID")); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getHexID());
			informationText_.append("\n");	//$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.vehicleStart")); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getStartPoint().getX());
			informationText_.append(" (x), ");	//$NON-NLS-1$
			informationText_.append(markedVehicle_.getStartPoint().getY());
			informationText_.append(" (y)\n");	//$NON-NLS-1$				
			tmpWayPoint = markedVehicle_.getDestinations().peekFirst();	//needs to be cached to prevent threading issues (if WayPoint is removed by simulation after the check against null!)
			if(tmpWayPoint != null){
				informationText_.append(Messages.getString("MouseClickManager.vehicleNextDestination")); //$NON-NLS-1$
				informationText_.append(tmpWayPoint.getX());
				informationText_.append(" (x), ");	//$NON-NLS-1$
				informationText_.append(tmpWayPoint.getY());
				informationText_.append(" (y)\n");	//$NON-NLS-1$
				dx = markedVehicle_.getX() - tmpWayPoint.getX();
				dy = markedVehicle_.getY() - tmpWayPoint.getY();
				informationText_.append(Messages.getString("MouseClickManager.linearDistance")); //$NON-NLS-1$
				informationText_.append(INTEGER_FORMAT_FRACTION.format(Math.sqrt(dx * dx + dy * dy)/100));
				informationText_.append(" m\n");	//$NON-NLS-1$
			}
			informationText_.append(Messages.getString("MouseClickManager.vehiclesCurrentSpeed")); //$NON-NLS-1$
			informationText_.append(INTEGER_FORMAT_FRACTION.format(markedVehicle_.getCurSpeed()/100000.0*3600));
			informationText_.append(" km/h\n");	//$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.travelTime")); //$NON-NLS-1$
			informationText_.append(INTEGER_FORMAT_FRACTION.format(markedVehicle_.getTotalTravelTime()/1000.0));
			informationText_.append(" s\n");	//$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.travelDistance")); //$NON-NLS-1$
			informationText_.append(INTEGER_FORMAT_FRACTION.format(markedVehicle_.getTotalTravelDistance()/100));
			informationText_.append(" m\n");	//$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.knownVehicles")); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getKnownVehiclesList().getSize());
			informationText_.append("\n");	//$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.knownMessages")); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getKnownMessages().getSize());
			informationText_.append(" (+ "); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getKnownMessages().getOldMessagesSize());
			informationText_.append(Messages.getString("MouseClickManager.old")); //$NON-NLS-1$
			informationText_.append("\n");	//$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.failedForwardMessages")); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getKnownMessages().getFailedForwardCount());
			informationText_.append("\n"); //$NON-NLS-1$
			informationText_.append(Messages.getString("MouseClickManager.knownPenalties")); //$NON-NLS-1$
			informationText_.append(markedVehicle_.getKnownPenalties().getSize());
			informationText_.append("\n"); //$NON-NLS-1$
		}
		if(markedNode_ != null){
		}
		return informationText_.toString();
	}
	
	/**
	 * The thread which handles mouse drags and information display.
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run(){
		setName("MouseClickManager"); //$NON-NLS-1$
		Renderer renderer = Renderer.getInstance();
		SimulateControlPanel simulatePanel = VanetSimStart.getMainControlPanel().getSimulatePanel();
		int lastInformationRefresh = 0;
		int sleepTime;
		long time = 0;
		setPriority(Thread.MIN_PRIORITY);
		
		while(true){
			time = System.nanoTime();
			if(active_ && drawArea_ != null){	//check for mouse clicks if drawArea exists and we're on it
				synchronized(this){	//to prevent incoming mouse events changing variables during this code block!
					if(waitingTime_ > -1){
						if(waitingTime_ > DRAG_ACTIVATION_INTERVAL){	//change mousecursor after specified time
							drawArea_.setCursor(moveCursor_);
							waitingTime_ = -1;
						}else waitingTime_ += 2;	//it's not guaranteed that the sleep below really lasts 2ms but this should be fine as it doesn't need to be that precise...
					}
					if(releaseTime_ - pressTime_ > DRAG_ACTIVATION_INTERVAL){	//pan the map if mouse was pressed longer than specified time
						renderer.pan((pressedX_ - releasedX_)*2, (pressedY_ - releasedY_)*2);
						ReRenderManager.getInstance().doReRender();
						releaseTime_ = pressTime_;
					} else if (releaseTime_ != pressTime_){	//get information about a point if click was shorter
						int distance = (int)Math.round(Math.min(80/renderer.getMapZoom(), 1000000));
						if(distance < 3000) distance = 3000;
						markedStreet_ = MapHelper.findNearestStreet(pressedX_, pressedY_, distance, new double[2], new int[2]);
						renderer.setMarkedStreet(markedStreet_);
						markedVehicle_ = MapHelper.findNearestVehicle(pressedX_, pressedY_, distance, new long[1]);
						renderer.setMarkedVehicle(markedVehicle_);
						markedNode_ = MapHelper.findNearestNode(pressedX_, pressedY_, distance, new long[1]);
						renderer.ReRender(false, false);
						releaseTime_ = pressTime_;
					}
				}
			}
			//it's rather costly to create this text and set the text area so it's only done periodically
			if(lastInformationRefresh < 0){
				lastInformationRefresh = INFORMATION_REFRESH_INTERVAL;
				simulatePanel.setInformation(createInformationString());
			}
			if(active_)sleepTime = 2;
			else sleepTime = 50;
			lastInformationRefresh -= sleepTime;
			time = ((System.nanoTime() - time)/1000000);			
			if(time > 0) time = sleepTime - time;
			else time = sleepTime + time;	//nanoTime might overflow			
			//sleep
			if(time > 0 && time <= sleepTime){
				try{
					sleep(time);
				} catch (Exception e){};
			}			
		}
	}
}