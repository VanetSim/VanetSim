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

import java.awt.Color;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Random;



import java.util.ArrayDeque;




import vanetsim.VanetSimStart;
import vanetsim.gui.Renderer;
import vanetsim.gui.controlpanels.ReportingControlPanel;
import vanetsim.gui.helpers.GeneralLogWriter;
import vanetsim.gui.helpers.PrivacyLogWriter;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.map.Node;
import vanetsim.map.Region;
import vanetsim.map.Street;
import vanetsim.routing.RoutingAlgorithm;
import vanetsim.routing.WayPoint;
import vanetsim.routing.A_Star.A_Star_Algorithm;
import vanetsim.scenario.events.BlockingObject;
import vanetsim.scenario.messages.Message;
import vanetsim.scenario.messages.PenaltyMessage;

/**
 * A vehicle which can move and communicate (if wifi is enabled).
 */
public class Vehicle extends LaneObject{
	
	/** A reference to the map so that we don't need to call this over and over again. */
	private static final Map MAP = Map.getInstance();
	
	/** A reference to the reporting control panel so that we don't need to call this over and over again. */
	private static final ReportingControlPanel REPORT_PANEL = getReportingPanel();
	
	/** When known vehicles are rechecked for outdated entries. Measured in milliseconds. */
	private static final int KNOWN_VEHICLES_TIMEOUT_CHECKINTERVAL = 5000;
	
	/** When known Road-Side-Units are rechecked for outdated entries. Measured in milliseconds. */
	private static final int KNOWN_RSUS_TIMEOUT_CHECKINTERVAL = 5000;
	
	/** How long to wait between searching the known penalties for outdated entries. Measured in milliseconds. */
	private static final int KNOWN_PENALTIES_TIMEOUT_CHECKINTERVAL = 30000;

	/** The minimum time between two newly created messages in milliseconds (does not apply to forwarded messages!). */
	private static final int MESSAGE_INTERVAL = 5000;

	/** The minimum time between two lane changes in milliseconds. */
	private static final int LANE_CHANGE_INTERVAL = 5000;

	/** If a vehicle does not move (speed=0) for this time (in milliseconds), a jam is detected and a penalty message created. */
	private static final int TIME_FOR_JAM = 5000;

	/** The radius in cm around the message destination in which the message will be flooded. */
	private static final int PENALTY_MESSAGE_RADIUS = 50000;
	
	/** The radius in cm around the message destination in which the message will be flooded. */
	private static final int PENALTY_FAKE_MESSAGE_RADIUS = 50000;

	/** The radius in cm around the message destination in which the message will be flooded. */
	private static final int PENALTY_EVA_MESSAGE_RADIUS = 50000;
	
	/** How long the penalty message will be valid (in milliseconds). 20000*/
	private static final int PENALTY_MESSAGE_VALID = 10000;
	
	/** The penalty which will be added through the message in cm. This value will be added in routing calculations. */
	private static final int PENALTY_MESSAGE_VALUE = 2000000;	// 20km
	
	/** How long the penalty itself will be valid (in milliseconds). 5000*/
	private static final int PENALTY_VALID = 5000;
	
	/** How long a vehicle waits to check if it's inside a mix. */
	private static final int MIX_CHECK_INTERVAL = 1000;
	
	/** How long the attacker waits (steps) to check if has new information about the attacked vehicle and needs to reroute. */
	private static final int ATTACKER_INTERVAL = 50;
	
	/** When the speed fluctuation is changed. Measured in milliseconds. */
	private static final int SPEED_FLUCTUATION_CHECKINTERVAL = 5000;
	
	/** When the speed fluctuation is changed. Measured in milliseconds. */
	private static final int SPEED_NO_FLUCTUATION_CHECKINTERVAL = 10000;
	
	/** Deviation from max speed limit. Simulation fluctuations in the drivers speed when reaching the speed limit. Maximum in cm/s^2 */
	private static final int SPEED_FLUCTUATION_MAX = 6;
	
	/** A global random number generator used to initialize the generators of the vehicles. */
	private static final Random RANDOM = new Random(1L);

	/** The routing algorithm used. */
	private static final RoutingAlgorithm ROUTING_ALGO = new A_Star_Algorithm();	

	/** The routing mode used. See the A_Star_Algo for details. */
	private static int routingMode_ = 1;
	
	/** The minimum time a vehicle must have traveled to get recycled. This shall prevent very shortliving 
	 * vehicles from consuming lots of CPU time for recycling. */
	private static int minTravelTimeForRecycling_ = 60000;

	/** If communication is enabled */
	private static boolean communicationEnabled_ = true;	

	/** If beacons are enabled */
	private static boolean beaconsEnabled_ = true;

	/** If mix zones are enabled */
	private static boolean mixZonesEnabled_ = true;
	
	/** If a fallback to the beaconless method shall be done in mix zones */
	private static boolean mixZonesFallbackEnabled_ = true;
	
	/** If the fallback mode only sends messages which are in flooding/broadcast mode. */
	private static boolean mixZonesFallbackFloodingOnly_ = true;

	/** How large a mix is in cm. Set in the common settings */
	private static int mixZoneRadius_ = 10000;
	
	/** How large a mix is in cm max. */
	private static int maxMixZoneRadius_ = 0;
	
	/** How long a vehicle waits to communicate again (in milliseconds). Also used for cleaning up outdated known messages. */
	private static int communicationInterval_ = 160;

	/** How long a vehicle waits to send its beacons again. */
	private static int beaconInterval_ = 240;

	/** The maximum communication distance a vehicle has. */
	private static int maximumCommunicationDistance_ = 0;

	/** An array holding all regions of the map. */
	private static Region[][] regions_;

	/** If monitoring the beacon is enabled or not. */
	private static boolean beaconMonitorEnabled_ = false;
	
	/** The minimum x coordinate which is checked during beacon monitoring. */
	private static int beaconMonitorMinX_ = -1;
	
	/** The maximum x coordinate which is checked during beacon monitoring. */
	private static int beaconMonitorMaxX_ = -1;
	
	/** The minimum y coordinate which is checked during beacon monitoring. */
	private static int beaconMonitorMinY_ = -1;
	
	/** The maximum y coordinate which is checked during beacon monitoring. */
	private static int beaconMonitorMaxY_ = -1;
	
	/** If recycling of vehicles is allowed or not */
	private static boolean recyclingEnabled_ = true;
	
	/** List of all AttackRSUs */
	private static AttackRSU arsuList[] = new AttackRSU[0];
	
	/** If attacker logging is enabled */
	private static boolean attackerDataLogged_ = false;	
	
	/** If attacker encrypted logging is enabled */
	private static boolean attackerEncryptedDataLogged_ = false;
	
	/** If attacker logging is enabled */
	private static boolean privacyDataLogged_ = false;	
	
	/** ID of the attacked vehicle */
	private static long attackedVehicleID_ = 0;
	
	/** Time for reroute of attacker */
	private static int reRouteTime_ = -1;
	
	/** encrypted beacon communication in Mix-Zones */
	private static boolean encryptedBeaconsInMix_ = false;

	/** A counter for the steady id */
	private static int steadyIDCounter = 0;
	
	/** time between silent-periods (in ms)*/
	private static int TIME_BETWEEN_SILENT_PERIODS = 10000;

	/** time of silent-periods (in ms)*/
	private static int TIME_OF_SILENT_PERIODS = 2000;
	
	/** flag to show if there is a silent period at the moment */
	private static boolean silent_period = false;
	
	/** flag to turn silent periods on/off */
	private static boolean silentPeriodsOn = false;
	
	/** time until pseudonym change in slow model */
	private static int TIME_TO_PSEUDONYM_CHANGE = 3000;
	
	/** speed limit for slow model */
	private static int  SLOW_SPEED_LIMIT = (int)(30 * 100000.0/3600);
	
	/** enable/disable slow */
	private static boolean slowOn = false;
	
	/** ids is activated */
	private static boolean idsActivated = false;
	
	/** the interval to generate fake messages */
	private static int fakeMessagesInterval_ = 10000;
	
	/** mode to send message only to vehicles within reach and to disable forwarding! (used for IDS evaluation) */
	private static boolean directCommunicationMode_ = true;
	
	
	/** How many simulation steps we wait until we send the RHCN message */
	private static int WAIT_TO_SEND_RHCN_ = 4;
	
	/** the counter for the rhcn message */
	private int waitToSendRHCNCounter_ = -1;

	/** The destinations this vehicle wants to visit. */
	public ArrayDeque<WayPoint> originalDestinations_;
	
	/** The <code>WayPoint</code> where this vehicles started. */
	private final WayPoint startingWayPoint_;	
	
	/** The vehicle length in cm. */
	private int vehicleLength_;
	
	/** The maximum speed of this car in cm/s. */
	private int maxSpeed_;

	/** The color of the vehicle. */
	private Color color_;
	
	/** The braking rate in cm/s^2. */
	private int brakingRate_;
	
	/** The acceleration rate in cm/s^2. */
	private int accelerationRate_ ;
	
	/** if activated the vehicle is an emergency vehicle */
	private boolean emergencyVehicle_;

	/** The maximum braking distance in cm/s*/
	private final int maxBrakingDistance_;
	
	/** Deviation from max speed limit. Simulates driver types which drive slower / faster */
	private int speedDeviation_;
	
	/** A class storing messages of different states: execute, forward and old ones. Could also be stored inside the
	 * vehicle class but it's a lot more clearly arranged like that. */ 
	private final KnownMessages knownMessages_ = new KnownMessages(this);

	/** A list of all vehicles currently known because of received beacons. */
	private final KnownVehiclesList knownVehiclesList_ = new KnownVehiclesList();
	

	/** A list of all idsprocessors currently running. */
	private final IDSProcessorList idsProcessorList_ = new IDSProcessorList(this);
	
	/** A list of all known event sources. */
	private final KnownEventSourcesList knownEventSourcesList_ = new KnownEventSourcesList(this.getID());
	
	/** A list of all Road-Side-Units currently known because of received beacons. */
	private final KnownRSUsList knownRSUsList_ = new KnownRSUsList();

	/** All known penalties. */
	private final KnownPenalties knownPenalties_ = new KnownPenalties(this);
	
	/** <code>true</code> if this vehicle has a communication device (WiFi), else <code>false</code> . */
	private boolean wiFiEnabled_;
	
	/** A random number generator for each vehicle. Primarily used for ID generation but can be used for other tasks, too. */
	private final Random ownRandom_;

	/** An ID used in communication (beacons). This might change (=> mixing zone)!It cannot be guaranteed 
	 * that this is really an unique ID as it's generated randomly! */
	private long ID_;
	
	/** An ID used to track vehicles after changing pseudonyms (for logging purpose only) */
	private int steadyID_;
	
	/** The destinations this vehicle wants to visit. */
	private ArrayDeque<WayPoint> destinations_;

	/** The new speed after the step. <code>curSpeed_</code> will be set to this in the moving-process to circumvent synchronisation problems. */
	private double newSpeed_;

	/** The new lane after the step. <code>curLane_</code> will be set to this in the moving-process to circumvent synchronisation problems. */
	private int newLane_ = 1;

	/** If set to true, this car is active and thus is drawn and moves. */
	private boolean active_ = false;

	/** An array containing ALL streets on which this vehicle will move until the next destination is reached. This can change when a rerouting is done! */
	private Street[] routeStreets_;

	/** An array with the directions on the streets corresponding to <code>routeStreets_</code> */
	private boolean[] routeDirections_;

	/** The current position in the <code>routeStreets_</code> and <code>routeDirections_</code> array */
	private int routePosition_;

	/** The current braking distance. */
	private int curBrakingDistance_;

	/** The speed at last braking distance calculation. */
	private double speedAtLastBrakingDistanceCalculation_ = 0;

	/** If the vehicle is currently in a mixing zone */
	private boolean isInMixZone_ = false;

	/** A node that we are allowed to pass. */
	private Node junctionAllowed_ = null;

	/** The maximum distance in cm this car can communicate. */
	private int maxCommDistance_;

	/** The current region. */
	private Region curRegion_;

	/** The time in milliseconds before doing the next movement. During waiting the vehicle communicates but does not 
	 * block other cars from passing. */
	private int curWaitTime_;

	/** The total time in milliseconds this vehicle traveled (excludes predefined waittimes!) */
	private int totalTravelTime_;

	/** The total distance in cm this vehicle traveled */
	private long totalTravelDistance_;

	/** If braking for the next destination should be done currently. */
	private boolean brakeForDestination_ = false;

	/** A countdown for braking. */
	private int brakeForDestinationCountdown_ = Integer.MAX_VALUE;

	/** A countdown for doing the next destination check. */
	private int destinationCheckCountdown_ = 0;

	/** A countdown to check if the minimum time between two lane changes has been reached. */
	private int laneChangeCountdown = 0;

	/** A countdown for communication. Also used for cleaning up outdated known messages. */
	private int communicationCountdown_;

	/** A countdown for sending beacons. */
	private int beaconCountdown_;

	/** A countdown for checking if inside a mix or not. */
	private int mixCheckCountdown_;

	/** A countdown for rechecking if known vehicles are outdated. */
	private int knownVehiclesTimeoutCountdown_;
	
	/** A countdown for rechecking if known RSUs are outdated. */
	private int knownRSUsTimeoutCountdown_;
	
	/** A countdown for rechecking if known penalties are outdated. */
	private int knownPenaltiesTimeoutCountdown_;

	/** A countdown for recalculating the speed fluctuation. */
	private int speedFluctuationCountdown_;
	
	/** Flag to show if a breaking because of the speed fluctuation is currently in progress */
	private boolean isBraking_ = false;
	
	/** Breaking of vehicle in fluctuation mode */
	private double fluctuation_ = 0;
	

	/** How much time has passed since the last rhcn message was created*/
	private int lastRHCNMessageCreated = 0;

	/** How much time has passed since the last pcn message was created*/
	private int lastPCNMessageCreated = 0;
	
	/** How much time has passed since the last pcn forward message was created*/
	private int lastPCNFORWARDMessageCreated = 0;
	
	/** How much time has passed since the last eva message was created*/
	private int lastEVAMessageCreated = 0;
	
	/** How long this vehicle is waiting in a jam (in milliseconds). */
	private int stopTime_ = 0;
	
	/** How many messages this vehicle has created. Used to give the messages unique ids (vehicle id + messageCounter; note that a collusion is possible but unlikely) */
	private int messagesCounter_ = 0;
	
	
	/** How many messages this vehicle has created. */
	private int pcnMessagesCreated_ = 0;

	/** How many messages this vehicle has created. */
	private int pcnForwardMessagesCreated_ = 0;
	
	/** How many messages this vehicle has created. */
	private int evaMessagesCreated_ = 0;
	
	/** How many messages this vehicle has created. */
	private int evaForwardMessagesCreated_ = 0;
	
	/** How many messages this vehicle has created. */
	private int rhcnMessagesCreated_ = 0;
	
	/** How many messages this vehicle has created. */
	private int eeblMessagesCreated_ = 0;
	
	/** How many messages this vehicle has created. */
	private int fakeMessagesCreated_ = 0;
	
	/** How many times this vehicle changed it's ID (due to mixes) */
	private int IDsChanged_ = 0;
	
	/** If the vehicle may be reused. */
	private boolean mayBeRecycled_ = false;
	
	/** Forbid to recycle vehicle. */
	private boolean doNotRecycle_ = false;
	
	/** Used to reroute the attacker after leaving the mix-zone */
	private Boolean attackerWasInMix = false;
	
	/** Used to reroute the attacker after leaving the mix-zone */
	private Boolean attackedWasInMix = false;
	
	/** Flag is set true when the attacker finds the attacked vehicle the first time */
	private Boolean firstContact = false;

	/** Saves the node of the current mix-zone. Used for encrypted Beacons*/
	private Node curMixNode_ = null;

	/** Vehicle is waiting behind a traffic signal (do not send any message)*/
	private boolean waitingForSignal_ = false;
	
	/** A distance between vehicles based on time (ms) (between 0 - 1000)*/
	private int timeDistance_ = 1000;
	
	/** A politeness factor in % */
	private int politeness_ = 0;
	
	/** Flag to log begin and end of silent periods */
	private boolean silentPeriod = false;
	
	/** Saved Beacon 1 */
	private String savedBeacon1 = "";
	
	/** Saved Beacon 2 */
	private String savedBeacon2 = "";
	
	/** variable to log next x beacons */
	private int logNextBeacons = 0;
	
	/** flag to show if vehicle is in slow period */
	private boolean isInSlow = false;
	
	/** flag to show if vehicle already changed pseudonym in slow period */
	private boolean changedPseudonymInSlow = false;
	
	/** timestamp of entrance in slow period */
	private int slowTimestamp = 0;
	
	/** flag to show if beacons where already logged */
	private boolean slowBeaconsLogged = false;
	
	/** flag to show if vehicle just started (important for slow-model, because first slow-period won't be logged) */
	private boolean vehicleJustStartedInSlow = true;

	/** flag to move a vehicle if a emergency is behind it */
	private boolean moveOutOfTheWay_ = false;
	
	/** flag if EVA message has to be forwarded */
	private boolean forwardMessage_ = false;

	/** flag if vehicle is faking messages */
	private boolean fakingMessages_ = false;
	
	/** message type the vehicle is faking */
	private String fakeMessageType_ = "";
	
	/** A countdown for sending fake messages. */
	private int fakeMessageCountdown_ = 0;
	
	/** counter to switch between fake message types if "all" is activated */
	private int fakeMessageCounter_ = 0;
	
	/** number of fake message types */
	private int fakeMessageTypesCount = IDSProcessor.getIdsData_().length;
	
	/** emergency braking interval */
	private static int emergencyBrakingInterval_ = 600000;

	/** emergency braking */
	private boolean emergencyBraking_ = false;
	
	/** emergency braking duration */
	private int emergencyBrakingDuration_= 3000;
	
	/** emergency braking countdown */
	private int emergencyBrakingCountdown_= -1;
	
	/** flag to send EEBL messages only once */
	private boolean EEBLmessageIsCreated_ = false;
	
	/** counter how much emergency beacons should be faked */
	private int emergencyBeacons = -1;

	/** flag to save if vehicle is driving on the side of a road because of a emergency vehicle. */
	private boolean drivingOnTheSide_ = false;
	
	/** the emergency vehicle (faking vehicle) we are waiting for until driving back on street */
	private Vehicle waitingForVehicle_ = null;
	
	/** a flag to show if a vehicle is currently near a blocking and needs to update his position information */
	private boolean passingBlocking_ = false;
	
	/** vehicle is in a traffic jam (used to forward pcn message (ids check)) */
	private boolean inTrafficJam_ = false;
	
	/** flag if a ids processor of this vehicle needs to be fired */
	private boolean checkIDSProcessors_ = false;
	
	/** a counter to save the amount of found spamers */
	private int spamCounter_ = 0;
	
	/** the amount of normal Beacons, which are sent out before the EVA message */
	private int EVAMessageDelay_ = 3;

	/** min and max amount of beacons a recipient of an EVA message will wait until she moves out of the way */
	private static int minEVAMessageDelay_ = 1;
	private static int maxEVAMessageDelay_ = 10;
	
	/** enables logging of Beacons after an event */
	private boolean logBeaconsAfterEvent_ = false;
	
	/** buffers Beacons until they are written on hard drive */
	private String beaconString_ = "";
	
	/** amount beacons logged */
	private int amountOfLoggedBeacons_ = 0;
	
	/** enables logging of differents ways vehicles use in a junction (for entropy calculation) */
	private boolean logJunctionFrequency_ = false;
	
	/** enables logging of probe data */
	private boolean probeDataActive_ = false;
	
	/** frequency probe data is logged */
	private int PROBE_DATA_TIME_INTERVAL = 80;
	
	/** how many Beacons are logged in the probe data */
	private int amountOfProbeData_ = 3;
	
	/** time to wait before logging probe data */
	private int startAfterTime_ = 60000;
	
	/** helper for probe data logging */
	private int curProbeDataInterval_ = 0;
	
	/**
	 * Instantiates a new vehicle. You will get an exception if the destinations don't contain at least two <b>valid</b> elements.<br>
	 * Elements are considered as invalid if
	 * <ul>
	 * <li>no route can be found between them and the first destination</li>
	 * <li>the destination is on the same street as the first destination</li>
	 * </ul>
	 * 
	 * @param destinations		an <code>ArrayDeque</code> with at least 2 elements (start and target) indicating where to move.
	 * @param vehicleLength		the vehicle length
	 * @param maxSpeed			the maximum speed of this vehicle in cm/s
	 * @param maxCommDist		the maximum distance in cm this vehicle can communicate
	 * @param wiFiEnabled		<code>true</code> if this vehicle has a communication device (WiFi), else <code>false</code>
	 * @param emergencyVehicle	<code>true</code> vehicle is an emergency vehicle
	 * @param brakingRate		the braking rate in cm/s^2
	 * @param accelerationRate	the acceleration rate in cm/s^2
	 * @param color				the color of the vehicle, if empty the default (color.black) is used
	 * @param timeDistance		the time distance
	 * @param politeness		the politeness
	 * @throws ParseException an Exception indicating that you did not supply a valid destination list.
	 */
	public Vehicle(ArrayDeque<WayPoint> destinations, int vehicleLength, int maxSpeed, int maxCommDist, boolean wiFiEnabled, boolean emergencyVehicle, int brakingRate, int accelerationRate, int timeDistance, int politeness, int speedDeviation, Color color, boolean fakingMessages, String fakeMessageType) throws ParseException {
		if(destinations != null && destinations.size()>1){
			originalDestinations_ = destinations; 
			destinations_ = originalDestinations_.clone();			
			ID_ = RANDOM.nextLong();
			steadyID_ = steadyIDCounter++;
			vehicleLength_ = vehicleLength;
			maxSpeed_ = maxSpeed;
			emergencyVehicle_ = emergencyVehicle;
			color_ = color;
			brakingRate_ = brakingRate;
			accelerationRate_ = accelerationRate;
			timeDistance_ = timeDistance;
			politeness_ = politeness;
			maxBrakingDistance_ = maxSpeed_ + maxSpeed_ * maxSpeed_ / (2 * brakingRate_);	// see http://de.wikipedia.org/wiki/Bremsweg
			startingWayPoint_ = destinations_.pollFirst();		// take the first element and remove it from the destinations!
			wiFiEnabled_ = wiFiEnabled;
			speedDeviation_ = speedDeviation;
			ownRandom_ = new Random(RANDOM.nextLong());
			curX_ = startingWayPoint_.getX();
			curY_ = startingWayPoint_.getY();
			curPosition_ = startingWayPoint_.getPositionOnStreet();
			curStreet_ = startingWayPoint_.getStreet();
			curWaitTime_ = startingWayPoint_.getWaittime();
			
			fakingMessages_ = fakingMessages;
			fakeMessageType_ = fakeMessageType;
			curRegion_ = Map.getInstance().getRegionOfPoint(curX_,curY_);
			maxCommDistance_ = maxCommDist;
			curSpeed_ = brakingRate_/2;
			newSpeed_ = curSpeed_;
			if(curStreet_.isOneway()){
				while(!destinations_.isEmpty() && (destinations_.peekFirst().getStreet() == curStreet_ || !calculateRoute(true, false))){
					curWaitTime_ = destinations_.pollFirst().getWaittime();
				}
			} else {
				while(!destinations_.isEmpty() && (destinations_.peekFirst().getStreet() == curStreet_ || !calculateRoute(false, false))){
					curWaitTime_ = destinations_.pollFirst().getWaittime();
				}
			}
			if(destinations_.size() == 0) throw new ParseException(Messages.getString("Vehicle.errorNotEnoughDestinations"),0); //$NON-NLS-1$
			if(curWaitTime_ == 0){
				active_ = true;
				curStreet_.addLaneObject(this, curDirection_);
			}
			calculatePosition();
			
			//set the countdowns so that not all fire at the same time!
			beaconCountdown_ = (int)Math.round(curPosition_)%beaconInterval_;
			communicationCountdown_ = (int)Math.round(curPosition_)%communicationInterval_;
			mixCheckCountdown_ = (int)Math.round(curPosition_)%MIX_CHECK_INTERVAL;
			knownVehiclesTimeoutCountdown_ = (int)Math.round(curPosition_)%KNOWN_VEHICLES_TIMEOUT_CHECKINTERVAL;
			knownPenaltiesTimeoutCountdown_ = (int)Math.round(curPosition_)%KNOWN_PENALTIES_TIMEOUT_CHECKINTERVAL;

			knownRSUsTimeoutCountdown_ = (int)Math.round(curPosition_)%KNOWN_RSUS_TIMEOUT_CHECKINTERVAL;
			speedFluctuationCountdown_ = (int)Math.round(curPosition_)%SPEED_FLUCTUATION_CHECKINTERVAL;
			fakeMessageCountdown_ = (int)Math.round(curPosition_)%fakeMessagesInterval_;
			emergencyBrakingCountdown_ = ownRandom_.nextInt(emergencyBrakingInterval_)+1;
			
			EVAMessageDelay_ = minEVAMessageDelay_ + ownRandom_.nextInt(maxEVAMessageDelay_);
		} else throw new ParseException(Messages.getString("Vehicle.errorNotEnoughDestinations"),0); //$NON-NLS-1$
	}


	/**
	 * (Re-)Calculates the route to the next destination.
	 * 
	 * @param careAboutDirection	<code>true</code> if the direction on the current street shall be used for calculation, <code>false</code> if the direction can be chosen freely
	 * @param isReroute				<code>true</code> to indicate that this is a Rerouting. If it's a rerouting, a non-existent route does not lead to the vehicle getting inactive!
	 * 
	 * @return <code>true</code> if new route has been found, else <code>false</code> (on error)
	 */
	public boolean calculateRoute(boolean careAboutDirection, boolean isReroute){
		try{
			WayPoint nextPoint = destinations_.peekFirst();			
			if(curStreet_ == nextPoint.getStreet()){
				boolean neededDirection;
				if(curPosition_ < nextPoint.getPositionOnStreet()) neededDirection = true;
				else neededDirection = false;
				if(!careAboutDirection || neededDirection == curDirection_){
					routeStreets_ = new Street[1];
					routeStreets_[0] = curStreet_;
					routeDirections_ = new boolean[1];
					routeDirections_[0] = true;
					routePosition_ = 0;
					return true;
				} //else calculate with routing algo below!
			}
			int direction;
			if(!careAboutDirection) direction = 0;
			else if(curDirection_) direction = -1;
			else direction = 1;
			ArrayDeque<Node> routing = ROUTING_ALGO.getRouting(routingMode_, direction, curX_, curY_, curStreet_, curPosition_, nextPoint.getX(), nextPoint.getY(), nextPoint.getStreet(), nextPoint.getPositionOnStreet(), knownPenalties_.getStreets(), knownPenalties_.getDirections(), knownPenalties_.getPenalties(), knownPenalties_.getSize(), maxSpeed_);

			if(routing.size() > 0){
				if(routing.size() == 1){
					routeStreets_ = new Street[2];
					routeStreets_[0] = curStreet_;
					routeStreets_[1] = nextPoint.getStreet();
					routeDirections_ = new boolean[2];
					if(routing.peekFirst() == curStreet_.getEndNode()) routeDirections_[0] = true;
					else routeDirections_[0] = false;
					if(routing.peekFirst() == nextPoint.getStreet().getStartNode()) routeDirections_[1] = true;
					else routeDirections_[1] = false;
					routePosition_ = 0;
					return true;
				} else {
					Node nextNode;
					int i;
					boolean usedDestination = false;
					Street[] outgoingStreets;
					Street tmpStreet = curStreet_, tmpStreet2;
					routeStreets_ = new Street[routing.size() + 1];
					routeDirections_ = new boolean[routing.size() + 1];
					Iterator<Node> routeIterator = routing.iterator();
					if(routing.peekFirst() == curStreet_.getEndNode()) curDirection_ = true;
					else curDirection_ = false;
					routeStreets_[0] = curStreet_;	//add current street as first element
					boolean tmpDirection;
					if(routeIterator.next() == curStreet_.getEndNode()) tmpDirection = true;
					else tmpDirection = false;
					routeDirections_[0] = tmpDirection;
					routePosition_ = 1;
	
					while(true){	//add all streets from routing
						if(routeIterator.hasNext()) nextNode = routeIterator.next();
						else if (!usedDestination){
							usedDestination = true;
							if(nextPoint.getStreet() != tmpStreet){
								nextNode = nextPoint.getStreet().getStartNode();
								if((!tmpDirection && nextNode == tmpStreet.getStartNode()) || (tmpDirection && nextNode == tmpStreet.getEndNode())) nextNode = nextPoint.getStreet().getEndNode();
							} else break;
						} else break;
						if(tmpDirection) outgoingStreets = tmpStreet.getEndNode().getOutgoingStreets();
						else outgoingStreets = tmpStreet.getStartNode().getOutgoingStreets();
						for(i = 0; i < outgoingStreets.length; ++i){
							tmpStreet2 = outgoingStreets[i];
							if (tmpStreet2.getStartNode() == nextNode){
								tmpStreet = tmpStreet2;
								tmpDirection = false;
								break;		// found street we want to => no need to look through others
							} else if (tmpStreet2.getEndNode() == nextNode){
								tmpStreet = tmpStreet2;
								tmpDirection = true;
								break;		// found street we want to => no need to look through others
							}
						}
						routeStreets_[routePosition_] = tmpStreet;
						routeDirections_[routePosition_] = tmpDirection;
						++routePosition_;
					}
					routePosition_ = 0;
					destinationCheckCountdown_ = 0;
					return true;
				}
			} else {
				if(!isReroute && destinations_.size() < 2) {
					active_ = false;
					curWaitTime_ = Integer.MIN_VALUE;
					if(totalTravelTime_ >= minTravelTimeForRecycling_) mayBeRecycled_ = true;
				}
				return false;
			}


		} catch (Exception e){ 
			return false;
		}
	}

	/**
	 * A method to be filled when implementing IDE
	 * @param timePerStep
	 */
	
	public void adjustSpeedWithIDM(int timePerStep){
		// start vehicle
		if(curWaitTime_ != 0 && curWaitTime_ != Integer.MIN_VALUE){
			if(curWaitTime_ <= timePerStep){
				//the time the vehicle will wait until it starts driving
				curWaitTime_ = 0;
				//needs to be set for vehicle to start driving
				active_ = true;
				brakeForDestination_ = false;
				//add the vehicle to the current lane object
				curStreet_.addLaneObject(this, curDirection_);
			} else curWaitTime_ -= timePerStep;
		}
		if(active_){
			if(curWaitTime_ == 0 && curStreet_ != null){
				//as a result of this method a newSpeed_ must be set
				newSpeed_ = 270;	
				if(this.getCurStreet().getName().contains("Mittelweg")) newSpeed_ = 27000;		
			}
		}
	}
	
	/**
	 * A method to be filled when implementing MOBIL
	 * @param timePerStep
	 */
	
	public boolean checkLaneFreeMOBIL(int lane){
		//Check implementation of checkLaneFree(int) to get ideas!
		return true;
	}
	
	
	/**
	 * A method to be filled when using vehicle sjtu trace files
	 * @param timePerStep
	 */
	
	public void adjustSpeedWithSJTUTraceFiles(int timePerStep){
		// start vehicle

		//needs to be set for vehicle to start driving
		active_ = true;
		
		//the time the vehicle will wait until it starts driving
		curWaitTime_ = 0;
		
		//add the vehicle to the current lane object
		curStreet_.addLaneObject(this, curDirection_);

		//as a result of this method a newSpeed_ must be set
		newSpeed_ = 1800;	
	}
	
	/**
	 * A method to be filled when using vehicle San Francisco trace files
	 * @param timePerStep
	 */
	
	public void adjustSpeedWithSanFranciscoTraceFiles(int timePerStep){
		// start vehicle

		//needs to be set for vehicle to start driving
		active_ = true;
		
		//the time the vehicle will wait until it starts driving
		curWaitTime_ = 0;
		
		//add the vehicle to the current lane object
		curStreet_.addLaneObject(this, curDirection_);

		//as a result of this method a newSpeed_ must be set
		newSpeed_ = 1800;	
	}

	
	/**
	 * Adjust the speed if reaching crossings or other cars. It also checks if the vehicle should get active.
	 * Furthermore some cleanup in the known messages and vehicles is done and new jam messages are created if necessary.
	 * 
	 * @param timePerStep the time per step in milliseconds
	 */

	public void adjustSpeed(int timePerStep){
		waitingForSignal_ = false;
		if(curWaitTime_ != 0 && curWaitTime_ != Integer.MIN_VALUE){
			if(curWaitTime_ <= timePerStep){
				curWaitTime_ = 0;
				active_ = true;
				brakeForDestination_ = false;
				curStreet_.addLaneObject(this, curDirection_);
			} else curWaitTime_ -= timePerStep;
		}

		if(active_){
			if(curWaitTime_ == 0 && curStreet_ != null){
				//curBrakingDistance always needs to be up-to-date but speed normally doesn't change too often...
				if(curSpeed_ != speedAtLastBrakingDistanceCalculation_){
					speedAtLastBrakingDistanceCalculation_ = curSpeed_;
					curBrakingDistance_ = (int)StrictMath.floor(0.5d + curSpeed_ + curSpeed_ * curSpeed_ / (2 * brakingRate_));
					if(curBrakingDistance_ < 500) curBrakingDistance_ = 500;
				}
				// ================================= 
				// Step 1: Check if vehicle is near destination so that it needs to brake (only checked when necessary => timer!)
				// ================================= 
				if(destinationCheckCountdown_ <= 0 && ! brakeForDestination_){
					WayPoint destinationWayPoint = destinations_.peekFirst();
					long dx = destinationWayPoint.getX() - curX_;
					long dy = destinationWayPoint.getY() - curY_;
					long distanceSquared = dx * dx + dy * dy;
					if(distanceSquared < (long)maxBrakingDistance_*maxBrakingDistance_*2){		//seems we're quite near a destination! This happens only in the last about 2-3 seconds!
						if(destinationWayPoint.getStreet() == curStreet_){ //if on the same street, the distance calculation is already correct!
							if(distanceSquared <= (long)curBrakingDistance_*curBrakingDistance_){
								if(brakeForDestinationCountdown_ > 1000) brakeForDestinationCountdown_ = 1000;
								brakeForDestination_ = true;
							} else destinationCheckCountdown_ = (int)StrictMath.floor(0.5d + ((StrictMath.sqrt(distanceSquared)-maxBrakingDistance_)/maxSpeed_)*1000);
						} else {	//not on the same street. Need to calculate the length of the rest of the way to the destination
							double distance = 0, tmpPosition = curPosition_;
							Street tmpStreet = curStreet_;
							boolean tmpDirection = curDirection_;
							int i;
							int j = routeStreets_.length-1;
							for(i = routePosition_; i < j;){
								if(tmpDirection) distance += tmpStreet.getLength() - tmpPosition;
								else distance += tmpPosition;
								++i;
								tmpDirection = routeDirections_[i];
								tmpStreet = routeStreets_[i];
								if(tmpDirection) tmpPosition = 0;
								else tmpPosition = tmpStreet.getLength();
							}
							if(tmpDirection) distance += destinations_.getFirst().getPositionOnStreet() - tmpPosition;	//left over...
							else distance += tmpPosition - destinations_.getFirst().getPositionOnStreet();			
							if(distance <= curBrakingDistance_){		//near enough to schedule braking!
								if(brakeForDestinationCountdown_ > 1000) brakeForDestinationCountdown_ = 1000;
								brakeForDestination_ = true;
							} else if(distance > maxBrakingDistance_) {	//far enough that we can sleep a little bit more
								destinationCheckCountdown_ = (int)StrictMath.floor(0.5d + (distance-maxBrakingDistance_)/maxSpeed_*1000);	//set time to recheck (using calculated distance and maximum speed!
							}	//don't need to change destinationCheckCountdown as we want to recheck next time
						}
					} else destinationCheckCountdown_ = (int)StrictMath.floor(0.5d + ((StrictMath.sqrt(distanceSquared)-maxBrakingDistance_)/maxSpeed_)*1000);		//set time to recheck (using minimum distance and maximum speed => can never be too high (vehicle might accelerate)!
					
					
					
				} else destinationCheckCountdown_ -= timePerStep;

				// ================================= 
				// Step 2: Check for vehicle/blocking in front of this one or a slower street and try to change lane
				// ================================= 
				int result = checkCurrentBraking(curLane_);
				boolean changedLane = false;
				laneChangeCountdown -= timePerStep;
				if(laneChangeCountdown < 0 && curLane_ == 0) newLane_ = 1;
				
				if(laneChangeCountdown < 0 && result == 1){
					if(curLane_ > 1){
						curBrakingDistance_ += 2000;	//make it little bit longer so that changes are not made too often if one lane has a little bit more space ;)
						int result2 = checkCurrentBraking(curLane_-1);
						curBrakingDistance_ -= 2000;
						
						if(result2 == 0 && checkLaneFree(curLane_+1)){	// only change lane if there are no obstacles on other lane or emergency vehicle is approaching
							newLane_ = curLane_ - 1;

							changedLane = true;
							
							laneChangeCountdown = LANE_CHANGE_INTERVAL;
							result = 0;
							moveOutOfTheWay_ = false;
							drivingOnTheSide_ = false;

						}
						
					}
					if(result == 1 && curStreet_.getLanesCount() > curLane_){
						curBrakingDistance_ += 2000;
						int result2 = checkCurrentBraking(curLane_+1);
						curBrakingDistance_ -= 2000;
						if(result2 == 0 && checkLaneFree(curLane_+1)){	// only change lane if there are no obstacles on other lane
							newLane_ = curLane_ + 1;

							changedLane = true;
							laneChangeCountdown = LANE_CHANGE_INTERVAL;
							result = 0;
						}
					}
				}
				// found a blocking. Check if we might change lane to prevent this
				

				boolean brakeOnce = false;
				if(result > 0){
					brakeOnce = true;
				}

				// ================================= 
				// Step 3: Check if we can change to the right lane
				// ================================= 
				if(laneChangeCountdown < 0 && curLane_ > 1 && !changedLane && result == 0){
					if(checkLaneFree(curLane_ - 1)){
						newLane_ = curLane_ - 1;
						changedLane = true;
						laneChangeCountdown = LANE_CHANGE_INTERVAL;
						moveOutOfTheWay_ = false;
						drivingOnTheSide_ = false;

					}
				}
				
				if(drivingOnTheSide_){		
					if(next_ != null && next_.getClass().equals(Vehicle.class) && waitingForVehicle_ != null && ((Vehicle) next_).getID() == waitingForVehicle_.getID()){
						drivingOnTheSide_ = false;
						waitingForVehicle_ = null;
						laneChangeCountdown = 1000;
					}
				}

				
				if(emergencyVehicle_) newLane_ = curStreet_.getLanesCount();
				if(moveOutOfTheWay_ && !emergencyVehicle_) {
					if(forwardMessage_ && waitingForVehicle_ != null){
						
						forwardMessage_ = false;
						// find the destination for the message. Will be sent to the next junction in FRONT of us!
						boolean tmpDirection2 = !curDirection_;
						Street tmpStreet2 = curStreet_;
						Street[] crossingStreets;
						Node tmpNode;
						int k, l = 0, destX = -1, destY = -1;
						do{
							++l;
							if(tmpDirection2){
								tmpNode = tmpStreet2.getStartNode();
							} else {
								tmpNode = tmpStreet2.getEndNode();
							}
							if(tmpNode.getJunction() != null){
								destX = tmpNode.getX();
								destY = tmpNode.getY();
								break;
							}
							crossingStreets = tmpNode.getCrossingStreets();
							// find next street behind of us
							if(crossingStreets.length != 2){	// end of a street or some special case. don't forward any further
								destX = tmpNode.getX();
								destY = tmpNode.getY();
								break;
							}
							for(k = 0; k < crossingStreets.length; ++k){
								if(crossingStreets[k] != tmpStreet2){
									tmpStreet2 = crossingStreets[k];
									if(tmpStreet2.getStartNode() == tmpNode) tmpDirection2 = false;
									else tmpDirection2 = true;
									break;
								}
							}
						} while(tmpStreet2 != curStreet_ && l < 10000);	//hard limit of 10000 nodes to maximally go back or if again arriving at source street (=>circle!)
						// found destination...now insert into messagequeue
						if(destX != -1 && destY != -1){
							int direction = -1;
							if(!curDirection_) direction = 1;
							int time = Renderer.getInstance().getTimePassed();
							PenaltyMessage message = new PenaltyMessage(curX_, curY_, destX, destY, PENALTY_EVA_MESSAGE_RADIUS, time + PENALTY_MESSAGE_VALID, curStreet_, curLane_, direction, PENALTY_MESSAGE_VALUE, time + PENALTY_VALID, false, ID_, waitingForVehicle_ , "HUANG_EVA_FORWARD", false, false);
							message.setFloodingMode(false);	// enable flooding mode if within distance!						
							knownMessages_.addMessage(message, false, true);
							++evaForwardMessagesCreated_;
													
						}	
					}
					
					if(newLane_ == curStreet_.getLanesCount() && ownRandom_.nextInt(100) == 0) {
						drivingOnTheSide_ = true;
						newLane_= curLane_-1;
						changedLane = true;
						laneChangeCountdown = LANE_CHANGE_INTERVAL + 20000;
						moveOutOfTheWay_ = false;
						
						
					}			
				}

				if((curLane_ == 0 && curSpeed_ > 277) || emergencyBraking_){
					brakeOnce = true;	
				}


				// ================================= 
				// Step 4: Break or accelerate
				// ================================= 
				if(brakeForDestinationCountdown_ > 0 && brakeForDestination_) {
					brakeForDestinationCountdown_ -= timePerStep;
				}
				if((brakeForDestinationCountdown_ <= 0 && brakeForDestination_) || brakeOnce || isBraking_){

					if(isBraking_ && !(brakeOnce || (brakeForDestinationCountdown_ <= 0 && brakeForDestination_))) newSpeed_ = curSpeed_ - (fluctuation_ * (double)timePerStep/1000);
					else newSpeed_ = curSpeed_ - (brakingRate_ * (double)timePerStep/1000);

					if(!brakeOnce && newSpeed_ < brakingRate_/2) newSpeed_ = brakingRate_/2;

				}
				if(!brakeForDestination_ && !brakeOnce && !isBraking_){		//if no breaking is scheduled we can accelerate (we don't need to look forward here because cars are not allowed by law to accelerate before they're on a "faster" street :D)

					if(curSpeed_ < (curStreet_.getSpeed() + speedDeviation_)) { 
						newSpeed_ = curSpeed_ + (accelerationRate_ * (double)timePerStep/1000);
					}
				}


				// ================================= 
				// Step 5: Correct to suit hard limits
				// ================================= 
				

				if(newSpeed_ > (maxSpeed_ + speedDeviation_)) newSpeed_ = (maxSpeed_ + speedDeviation_);
				else if (newSpeed_ < 0) newSpeed_ = 0;	//no negative speed
				if((curStreet_.getSpeed() + speedDeviation_) > 0 && newSpeed_ > (curStreet_.getSpeed() + speedDeviation_) && this != Renderer.getInstance().getAttackerVehicle() && !emergencyVehicle_) newSpeed_ = (curStreet_.getSpeed() + speedDeviation_);
			}

		
			// ================================= 
			// Step 6: Check message/beacons/penalties countdown and cleanup
			// ================================= 

			// in this first simulation step, no other communication is done. So we can we can some work concerning messages and 
			// known vehicles here without synchronization problems!	
			
			
			if(speedFluctuationCountdown_ < 1){

				isBraking_ = !isBraking_;
				if(isBraking_){
			
					fluctuation_ = ownRandom_.nextInt(SPEED_FLUCTUATION_MAX);
			
					speedFluctuationCountdown_ += SPEED_FLUCTUATION_CHECKINTERVAL;
				
				}
				else{

					speedFluctuationCountdown_ += SPEED_NO_FLUCTUATION_CHECKINTERVAL;
					fluctuation_ = 0;
				}
			}
			else speedFluctuationCountdown_ -= timePerStep;
			
			
				if(EEBLmessageIsCreated_){
					emergencyBrakingCountdown_ -= timePerStep;
					if(emergencyBrakingCountdown_ < 1){
						emergencyBraking_ = false;
						EEBLmessageIsCreated_ = false;
					}
				}
				
			
			if(isWiFiEnabled() && communicationEnabled_){
				
				if(knownMessages_.hasNewMessages()) {
					
					knownMessages_.processMessages();
				}
				communicationCountdown_ -= timePerStep;
				if(communicationCountdown_ < 1) knownMessages_.checkOutdatedMessages(true);

				knownPenaltiesTimeoutCountdown_ -= timePerStep;
				if(knownPenaltiesTimeoutCountdown_ < 1){
					if(knownPenalties_.getSize() > 0) knownPenalties_.checkValidUntil();
					knownPenaltiesTimeoutCountdown_ += KNOWN_PENALTIES_TIMEOUT_CHECKINTERVAL;
				}

				if(beaconsEnabled_){
					beaconCountdown_ -= timePerStep;

					// recheck known vehicles for outdated entries.
					if(knownVehiclesTimeoutCountdown_ < 1){
						knownVehiclesList_.checkOutdatedVehicles();
						idsProcessorList_.checkOutdatedProcessors();
						knownVehiclesTimeoutCountdown_ += KNOWN_VEHICLES_TIMEOUT_CHECKINTERVAL;
					} else knownVehiclesTimeoutCountdown_ -= timePerStep;
					
					// recheck known RSUs for outdated entries.
					if(knownRSUsTimeoutCountdown_ < 1){
						knownRSUsList_.checkOutdatedRSUs();
						knownRSUsTimeoutCountdown_ += KNOWN_RSUS_TIMEOUT_CHECKINTERVAL;
					} else knownRSUsTimeoutCountdown_ -= timePerStep;
					
				}


				
				// ================================= 
				// Step 7: Check if this vehicle is currently in a traffic jam and should create a message.
				// ================================= 
				lastRHCNMessageCreated += timePerStep;
				lastPCNMessageCreated += timePerStep;
				lastPCNFORWARDMessageCreated += timePerStep;
				lastEVAMessageCreated += timePerStep;

				if(newSpeed_ == 0){
					stopTime_ += timePerStep;
					
					if(stopTime_ > TIME_FOR_JAM && !waitingForSignal_){
						inTrafficJam_ = true;
						if(lastPCNMessageCreated >= MESSAGE_INTERVAL){
							lastPCNMessageCreated = 0;
							// find the destination for the message. Will be sent to the next junction behind us!
							boolean tmpDirection = curDirection_;
							Street tmpStreet = curStreet_;
							Street[] crossingStreets;
							Node tmpNode;
							int i, j = 0, destX = -1, destY = -1;
							do{
								++j;
								if(tmpDirection){
									tmpNode = tmpStreet.getStartNode();
								} else {
									tmpNode = tmpStreet.getEndNode();
								}
								if(tmpNode.getJunction() != null){
									destX = tmpNode.getX();
									destY = tmpNode.getY();
									break;
								}
								crossingStreets = tmpNode.getCrossingStreets();
								// find next street behind of us
								if(crossingStreets.length != 2){	// end of a street or some special case. don't forward any further
									destX = tmpNode.getX();
									destY = tmpNode.getY();
									break;
								}
								for(i = 0; i < crossingStreets.length; ++i){
									if(crossingStreets[i] != tmpStreet){
										tmpStreet = crossingStreets[i];
										if(tmpStreet.getStartNode() == tmpNode) tmpDirection = false;
										else tmpDirection = true;
										break;
									}
								}
							} while(tmpStreet != curStreet_ && j < 10000);	//hard limit of 10000 nodes to maximally go back or if again arriving at source street (=>circle!)
							// found destination...now insert into messagequeue
							if(destX != -1 && destY != -1){
								int direction = -1;
								if(!curDirection_) direction = 1;
								int time = Renderer.getInstance().getTimePassed();
								PenaltyMessage message = new PenaltyMessage(curX_, curY_, destX, destY, PENALTY_MESSAGE_RADIUS, time + PENALTY_MESSAGE_VALID, curStreet_, curLane_, direction, PENALTY_MESSAGE_VALUE, time + PENALTY_VALID, false, ID_, this,  "HUANG_PCN", false, false);
								long dx = message.getDestinationX_() - curX_;
								long dy = message.getDestinationY_() - curY_;
								if((long)PENALTY_MESSAGE_RADIUS * PENALTY_MESSAGE_RADIUS >= (dx*dx + dy*dy)){
									message.setFloodingMode(true);	// enable flooding mode if within distance!
								}								
								knownMessages_.addMessage(message, false, true);

								++pcnMessagesCreated_;
							}							
						}					
					}
				} else {
					inTrafficJam_ = false;
					stopTime_ = 0;
				}


				
				// ================================= 
				// Step 8: Check if vehicle is inside a mix zone and change vehicle ID if entering mix zone
				// ================================= 

				if(mixZonesEnabled_){
					mixCheckCountdown_ -= MIX_CHECK_INTERVAL;
					if(mixCheckCountdown_ <= 0){
						int MapMinX, MapMinY, MapMaxX, MapMaxY, RegionMinX, RegionMinY, RegionMaxX, RegionMaxY;
						int i, j, k, size;
						Node node;
						long dx, dy, mixDistanceSquared = (long)getMaxMixZoneRadius() * getMaxMixZoneRadius();
						boolean needsToMix = false;

						// Minimum x coordinate to be considered
						long tmp = curX_ - getMaxMixZoneRadius();
						if (tmp < 0) MapMinX = 0;	// Map stores only positive coordinates
						else if(tmp < Integer.MAX_VALUE) MapMinX = (int) tmp;
						else MapMinX = Integer.MAX_VALUE;

						// Maximum x coordinate to be considered
						tmp = curX_ + getMaxMixZoneRadius();
						if (tmp < 0) MapMaxX = 0;
						else if(tmp < Integer.MAX_VALUE) MapMaxX = (int) tmp;
						else MapMaxX = Integer.MAX_VALUE;

						// Minimum y coordinate to be considered
						tmp = curY_ - getMaxMixZoneRadius();
						if (tmp < 0) MapMinY = 0;
						else if(tmp < Integer.MAX_VALUE) MapMinY = (int) tmp;
						else MapMinY = Integer.MAX_VALUE;

						// Maximum y coordinate to be considered
						tmp = curY_ + getMaxMixZoneRadius();
						if (tmp < 0) MapMaxY = 0;
						else if(tmp < Integer.MAX_VALUE) MapMaxY = (int) tmp;
						else MapMaxY = Integer.MAX_VALUE;

						// Get the regions to be considered
						Region tmpregion = MAP.getRegionOfPoint(MapMinX, MapMinY);
						RegionMinX = tmpregion.getX();
						RegionMinY = tmpregion.getY();

						tmpregion = MAP.getRegionOfPoint(MapMaxX, MapMaxY);
						RegionMaxX = tmpregion.getX();
						RegionMaxY = tmpregion.getY();

						// only check those regions which are within the radius
						for(i = RegionMinX; i <= RegionMaxX; ++i){
	
							for(j = RegionMinY; j <= RegionMaxY; ++j){
								Node[] mixNodes = regions_[i][j].getMixZoneNodes();
								size = mixNodes.length;
								for(k = 0; k < size; ++k){
									node = mixNodes[k];
									// precheck if the mixing node is near enough and valid (check is not exact as its a rectangular box and not circle)
									if(node.getX() >= curX_ - node.getMixZoneRadius() && node.getX() <= curX_ + node.getMixZoneRadius() && node.getY() >= curY_ - node.getMixZoneRadius() && node.getY() <= curY_ + node.getMixZoneRadius()){
										dx = node.getX() - curX_;
										dy = node.getY() - curY_;

										mixDistanceSquared = node.getMixZoneRadius() * node.getMixZoneRadius();
										if((dx * dx + dy * dy) <= mixDistanceSquared){	// Pythagorean theorem: a^2 + b^2 = c^2 but without the needed Math.sqrt to save a little bit performance
											needsToMix = true;
											curMixNode_ = node;

											//change values to break out of all loops as we don't need to check further!
											i = RegionMaxX;
											j = RegionMaxY;
											k = mixNodes.length -1;									
										}
									} 
								}
							}
						}
						
						if(needsToMix != isInMixZone_){
							if(privacyDataLogged_){
								if(needsToMix) 	PrivacyLogWriter.log(Renderer.getInstance().getTimePassed() + ":Steady ID:" + this.steadyID_ + ":Pseudonym:" + Long.toHexString(this.ID_) + ":TraveledDistance:" + totalTravelDistance_ + ":TraveledTime:" + totalTravelTime_ + ":Node ID:" + curMixNode_.getNodeID() + ":Direction:IN" + ":Street:" + this.getCurStreet().getName() + ":StreetSpeed:" + this.getCurStreet().getSpeed() + ":VehicleSpeed:" + this.getCurSpeed() +  ":x:" + this.curX_ + ":y:" + this.curY_);
								else PrivacyLogWriter.log(Renderer.getInstance().getTimePassed() + ":Steady ID:" + this.steadyID_ + ":Pseudonym:" + Long.toHexString(this.ID_) + ":TraveledDistance:" + totalTravelDistance_ + ":TraveledTime:" + totalTravelTime_ + ":Node ID:" + curMixNode_.getNodeID() + ":Direction:OUT" + ":Street:" + this.getCurStreet().getName() + ":StreetSpeed:" + this.getCurStreet().getSpeed() + ":VehicleSpeed:" + this.getCurSpeed() + ":x:" + this.curX_ + ":y:" + this.curY_);
							}
							if(needsToMix){
								++IDsChanged_;
								ID_ = ownRandom_.nextLong();
							}
							isInMixZone_ = needsToMix;
						}
						if(!needsToMix) curMixNode_ = null;
					}
				}
			
				// ================================= 
				// Step 9: Send fake messages
				// ================================= 
				if(fakingMessages_){
					fakeMessageCountdown_ -= timePerStep;
					if(fakeMessageCountdown_ < 0){
						
						//fake messages
						fakeMessageCountdown_ = fakeMessagesInterval_;
						String messageType = fakeMessageType_;
						if(fakeMessageType_.equals("all") || fakeMessageType_.equals("Alle")) messageType = IDSProcessor.getIdsData_()[ownRandom_.nextInt(fakeMessageTypesCount)];
						
						// find the destination for the message. Will be sent to the next junction behind us! (if its pcn we send it in front)
						boolean tmpDirection2 = curDirection_;
						if(messageType.equals("HUANG_PCN")) tmpDirection2 = !curDirection_;
						
						Street tmpStreet2 = curStreet_;
						Street[] crossingStreets;
						Node tmpNode;
						int k, l = 0, destX = -1, destY = -1;
						do{
							++l;
							if(tmpDirection2){
								tmpNode = tmpStreet2.getStartNode();
							} else {
								tmpNode = tmpStreet2.getEndNode();
							}
							if(tmpNode.getJunction() != null){
								destX = tmpNode.getX();
								destY = tmpNode.getY();
								break;
							}
							crossingStreets = tmpNode.getCrossingStreets();
							// find next street behind of us
							if(crossingStreets.length != 2){	// end of a street or some special case. don't forward any further
								destX = tmpNode.getX();
								destY = tmpNode.getY();
								break;
							}
							for(k = 0; k < crossingStreets.length; ++k){
								if(crossingStreets[k] != tmpStreet2){
									tmpStreet2 = crossingStreets[k];
									if(tmpStreet2.getStartNode() == tmpNode) tmpDirection2 = false;
									else tmpDirection2 = true;
									break;
								}
							}
						} while(tmpStreet2 != curStreet_ && l < 10000);	//hard limit of 10000 nodes to maximally go back or if again arriving at source street (=>circle!)
						// found destination...now insert into messagequeue
						if(destX != -1 && destY != -1){
							int direction = -1;
							int time = Renderer.getInstance().getTimePassed();
							if(messageType.equals("HUANG_EVA_FORWARD")){
								PenaltyMessage message = new PenaltyMessage(curX_, curY_, destX, destY, PENALTY_FAKE_MESSAGE_RADIUS, time + PENALTY_MESSAGE_VALID, curStreet_, curLane_, direction, PENALTY_MESSAGE_VALUE, time + PENALTY_VALID, true, ID_, this,  messageType, false, true);
								message.setFloodingMode(true);	// enable flooding mode if within distance!				
								knownMessages_.addMessage(message, false, true);
							}
							else if(messageType.equals("EVA_EMERGENCY_ID")){
								if(emergencyBeacons == -1){
									emergencyBeacons = EVAMessageDelay_;
								}
				
							}
							else if(messageType.equals("HUANG_PCN")){
				
								PenaltyMessage message = new PenaltyMessage(curX_, curY_, destX, destY, PENALTY_FAKE_MESSAGE_RADIUS, time + PENALTY_MESSAGE_VALID, curStreet_, curLane_, direction, PENALTY_MESSAGE_VALUE, time + PENALTY_VALID, true, ID_, this, messageType, false, true);

								long dx = message.getDestinationX_() - curX_;
								long dy = message.getDestinationY_() - curY_;
								if((long)PENALTY_MESSAGE_RADIUS * PENALTY_MESSAGE_RADIUS >= (dx*dx + dy*dy)){
									message.setFloodingMode(true);	// enable flooding mode if within distance!
								}
								knownMessages_.addMessage(message, false, true);

							}
							else{
								PenaltyMessage message = new PenaltyMessage(curX_, curY_, destX, destY, PENALTY_FAKE_MESSAGE_RADIUS, time + PENALTY_MESSAGE_VALID, curStreet_, curLane_, direction, PENALTY_MESSAGE_VALUE, time + PENALTY_VALID, true, ID_, this, messageType, false, true);

								long dx = message.getDestinationX_() - curX_;
								long dy = message.getDestinationY_() - curY_;
								if((long)PENALTY_MESSAGE_RADIUS * PENALTY_MESSAGE_RADIUS >= (dx*dx + dy*dy)){
									message.setFloodingMode(true);	// enable flooding mode if within distance!
								}
							    knownMessages_.addMessage(message, false, true);

							}
						}		
						++fakeMessagesCreated_;
					
						fakeMessageCounter_ = fakeMessageCounter_%fakeMessageTypesCount;
					}
				}
			}
		}			
	}

	//Still in development, commented code is needed!
	/**
	 * helper class to develope politness factor
	 */
	@SuppressWarnings("unused")
	private final boolean checkPolitness(int lane){
		/*
		//Noch auszubessern: in checkLaneFree() die neededDistance wieder zur�cksetzen
		boolean vehicleInFront = false;
		boolean vehicleInFront2 = false;
		boolean vehicleBehind = false;
		boolean vehicleBehind2 = false;	
		Vehicle b = null, b2 = null, f = null, f2 = null;
		
		System.out.println("**************");
		//fahrzeug das jetzt vor uns ist:
		if(next_ != null){
			if(next_.getCurLane() == lane){	// next one is on the same lane
				//System.out.println("Fahrzeug vor uns: (1.Versuch) " + next_.curPosition_);
				if(this.equals(Renderer.getInstance().getMarkedVehicle())) ((Vehicle)next_).setColor(Color.cyan);
				f = (Vehicle)next_;
				vehicleInFront = true;
			} else {	// need to search for the next which is on our lane
				LaneObject tmpLaneObject = next_.getNext();
				while(tmpLaneObject != null){
					if(tmpLaneObject.getCurLane() == lane){
					//	System.out.println("Fahrzeug vor uns: (2. Versuch)" + tmpLaneObject.curPosition_);
						if(this.equals(Renderer.getInstance().getMarkedVehicle())) ((Vehicle)tmpLaneObject).setColor(Color.cyan);
						f = (Vehicle)tmpLaneObject;
						vehicleInFront = true;
						break;	// only check the first on our lane!
					}
					tmpLaneObject = tmpLaneObject.getNext();
				}
			}
		}
		
		if(!vehicleInFront){
			for(int i = routePosition_; i < routeStreets_.length; i++){
				boolean tmpDirection = routeDirections_[i];
				Street tmpStreet = routeStreets_[i];
				
				LaneObject tmpLaneObject = tmpStreet.getFirstLaneObject(tmpDirection);
				
				while(tmpLaneObject != null){
					if(tmpLaneObject.getCurLane() == lane){
						//System.out.println("Fahrzeug vor uns: (3. Versuch)" + tmpLaneObject.curPosition_);
						if(this.equals(Renderer.getInstance().getMarkedVehicle())) ((Vehicle)tmpLaneObject).setColor(Color.cyan);
						f = (Vehicle) tmpLaneObject;
						i=routeStreets_.length;
						break;
					}
					
					tmpLaneObject = tmpLaneObject.getNext();
				}			
			}
		}
		
		// fahrzeug das dann vor uns ist:
		if(next_ != null){
			if(next_.getCurLane() == lane+1){	// next one is on the same lane
				//System.out.println("!!!Fahrzeug dann vor uns: (1.Versuch) " + next_.curPosition_);
				if(this.equals(Renderer.getInstance().getMarkedVehicle())) ((Vehicle)next_).setColor(Color.cyan);
				f2 = (Vehicle)next_;
				vehicleInFront2 = true;
			} else {	// need to search for the next which is on our lane
				LaneObject tmpLaneObject = next_.getNext();
				while(tmpLaneObject != null){
					if(tmpLaneObject.getCurLane() == lane+1){
						//System.out.println("!!!Fahrzeug dann vor uns: (2. Versuch)" + tmpLaneObject.curPosition_);
						if(this.equals(Renderer.getInstance().getMarkedVehicle())) ((Vehicle)tmpLaneObject).setColor(Color.cyan);
						f2 = (Vehicle)tmpLaneObject;
						vehicleInFront2 = true;
						break;	// only check the first on our lane!
					}
					tmpLaneObject = tmpLaneObject.getNext();
				}
			}
		}
		
		if(!vehicleInFront2){
			for(int i = routePosition_; i < routeStreets_.length; i++){
				boolean tmpDirection = routeDirections_[i];
				Street tmpStreet = routeStreets_[i];
				
				LaneObject tmpLaneObject = tmpStreet.getFirstLaneObject(tmpDirection);
				
				while(tmpLaneObject != null){

					if(tmpLaneObject.getCurLane() == lane+1){
						//System.out.println("!!!Fahrzeug dann vor uns: (3. Versuch)" + tmpLaneObject.curPosition_);
						if(this.equals(Renderer.getInstance().getMarkedVehicle())) ((Vehicle)tmpLaneObject).setColor(Color.cyan);
						f2 = (Vehicle) tmpLaneObject;
						i = routeStreets_.length;
						break;
					}
					
					tmpLaneObject = tmpLaneObject.getNext();
				}			
			}
		}
		

		// ================================= 
		// Step 2: Check space behind
		// ================================= 

		if(previous_ == null)System.out.println("ist null");
		// check the lane object behind us (on our street)
		if(previous_ != null){
			
			if(previous_.getCurLane() == lane){	// is on the same lane
				vehicleBehind = true;
				//System.out.println("Fahrzeug hinter uns: (1. Versuch)" + previous_.curPosition_);
				if(this.equals(Renderer.getInstance().getMarkedVehicle())) ((Vehicle)previous_).setColor(Color.cyan);
				b = (Vehicle)previous_;
			} else {	// need to search for the previous one which is on our lane
				LaneObject tmpLaneObject = previous_.getPrevious();
				while(tmpLaneObject != null){
					if(tmpLaneObject.getCurLane() == lane){
						vehicleBehind = true;
					//	System.out.println("Fahrzeug hinter uns: (2. Versuch)" + previous_.curPosition_);
						if(this.equals(Renderer.getInstance().getMarkedVehicle())) ((Vehicle)tmpLaneObject).setColor(Color.cyan);
						b = (Vehicle)tmpLaneObject;
						break;	// only check the first on our lane!
					}
					tmpLaneObject = tmpLaneObject.getPrevious();
				}
			}
		}
		
		if(!vehicleBehind){
			Street[] outgoingStreets;
			Street tmpStreet = curStreet_, tmpStreet2;
			LaneObject tmpLaneObject;
			Node nextNode = null;
			boolean tmpDirection = curDirection_;
			int i;
			// check previous streets. We can't use the routing here so it's limited to streets with no junctions which makes it a bit simpler.
			int counter = 0;
			while(counter < 2){
				if(tmpDirection) nextNode = tmpStreet.getStartNode();
				else nextNode = tmpStreet.getEndNode();
				if(nextNode.getCrossingStreetsCount() != 2) break;		// don't handle junctions!
				else outgoingStreets = nextNode.getCrossingStreets();
				for(i = 0; i < outgoingStreets.length; ++i){
					tmpStreet2 = outgoingStreets[i];
					if(tmpStreet2 != tmpStreet){
						tmpStreet = tmpStreet2;
						if(lane > tmpStreet.getLanesCount()) break;
						if (tmpStreet2.getStartNode() == nextNode){
							tmpDirection = true;
							break;		// found street we want to => no need to look through others
						} else {
							tmpDirection = false;
							break;		// found street we want to => no need to look through others
						}
					}					
				}				

				if(!vehicleBehind){
					tmpLaneObject = tmpStreet.getFirstLaneObject(!tmpDirection);
					while(tmpLaneObject != null){
						if(tmpLaneObject.getCurLane() == lane && !tmpLaneObject.equals(this)){
						//	System.out.println("Fahrzeug hinter uns: (3. Versuch)" + tmpLaneObject.curPosition_);
							if(this.equals(Renderer.getInstance().getMarkedVehicle())) ((Vehicle)tmpLaneObject).setColor(Color.cyan);
							b = (Vehicle)tmpLaneObject;
							counter = 3;
							break;
						}
						tmpLaneObject = tmpLaneObject.getNext();
					}
				}
				counter++;
			}
		}
		
		
*/
		boolean vehicleBehind2 = false;	
		Vehicle b2 = null; 
		double distance = 0;
		
		// check the lane object then before us (on our street)
		if(previous_ != null){
			if(previous_.getCurLane() == lane+1){	// is on the same lane
				vehicleBehind2 = true;
			//	System.out.println("!!!Fahrzeug dann hinter uns: (1. Versuch)" + previous_.curPosition_);
				if(this.equals(Renderer.getInstance().getMarkedVehicle())) ((Vehicle)previous_).setColor(Color.cyan);
				b2 = (Vehicle)previous_;
			} else {	// need to search for the previous one which is on our lane
				LaneObject tmpLaneObject = previous_.getPrevious();
				while(tmpLaneObject != null){
					if(tmpLaneObject.getCurLane() == lane+1){
						vehicleBehind2 = true;
					//	System.out.println("!!!Fahrzeug dann hinter uns: (2. Versuch)" + previous_.curPosition_);
						if(this.equals(Renderer.getInstance().getMarkedVehicle())) ((Vehicle)tmpLaneObject).setColor(Color.cyan);
						b2 = (Vehicle)tmpLaneObject;
						break;	// only check the first on our lane!
					}
					tmpLaneObject = tmpLaneObject.getPrevious();
				}
			}
		}
		if(vehicleBehind2){
			distance = Math.abs(curPosition_ - b2.curPosition_);
		}
		else{
			if(curDirection_) distance = curPosition_;
			else distance = curStreet_.getLength() - curPosition_;
		}
	
	//	System.out.println("distance: " + distance);
		
		if(!vehicleBehind2){
			Street[] outgoingStreets;
			Street tmpStreet = curStreet_, tmpStreet2;
			LaneObject tmpLaneObject;
			Node nextNode = null;
			boolean tmpDirection = curDirection_;
			int i;
			// check previous streets. We can't use the routing here so it's limited to streets with no junctions which makes it a bit simpler.
			int counter = 0;
			
			while(counter < 3){
				//System.out.println("counter: " + counter);
				if(tmpDirection) nextNode = tmpStreet.getStartNode();
				else nextNode = tmpStreet.getEndNode();
				if(nextNode.getCrossingStreetsCount() != 2) break;		// don't handle junctions!
				else outgoingStreets = nextNode.getCrossingStreets();
				for(i = 0; i < outgoingStreets.length; ++i){
					tmpStreet2 = outgoingStreets[i];
					if(tmpStreet2 != tmpStreet){
						tmpStreet = tmpStreet2;
						if(lane+1 > tmpStreet.getLanesCount()) break;
						if (tmpStreet2.getStartNode() == nextNode){
							tmpDirection = true;
							break;		// found street we want to => no need to look through others
						} else {
							tmpDirection = false;
							break;		// found street we want to => no need to look through others
						}
					}					
				}				

				if(!vehicleBehind2){
					tmpLaneObject = tmpStreet.getFirstLaneObject(!tmpDirection);
					while(tmpLaneObject != null){
						if(tmpLaneObject.getCurLane() == lane + 1 && !tmpLaneObject.equals(this)){
						//	System.out.println("!!!Fahrzeug dann hinter uns: (3. Versuch)" + tmpLaneObject.curPosition_);
							counter = 3;
							if(tmpDirection) distance =  tmpStreet.getLength()-tmpLaneObject.getCurPosition()+distance;
							else distance = tmpLaneObject.getCurPosition()+distance;
							
							if(this.equals(Renderer.getInstance().getMarkedVehicle())) ((Vehicle)tmpLaneObject).setColor(Color.cyan);
							b2 = (Vehicle)tmpLaneObject;

							break;
						}
						tmpLaneObject = tmpLaneObject.getNext();
					}
				}
				counter ++;
				if(counter < 3)distance += tmpStreet.getLength();
			}
		}

		if(b2 != null){
			if(curSpeed_ >= b2.curSpeed_){
				//System.out.println("hinterer langsamer");
				return true;
			}
			float t = (float) ((b2.curSpeed_ - curSpeed_)/accelerationRate_);
			//System.out.println("distanz ben�tigt: " + b2.curSpeed_ * t);
		//	System.out.println("distanz vorhanden: " + (distance-b2.curBrakingDistance_));
			//System.out.println("braking distance" + b2.curBrakingDistance_);
			if((distance-b2.curBrakingDistance_) > ((politeness_/100) * (b2.curSpeed_*t))) return true;
			else return false;

		}
		else return true;
		//code need, still developing
		/*
		float t = (float) ((maxSpeed_ - curSpeed_)/accelerationRate_);
		if(b2 != null){
			//if(b2.curPosition_ - curPosition_)
			System.out.println(b2.curSpeed_ * t);
		}
		
		System.out.println("**************");
		*/
		/*
		boolean foundNextVehicle = false;
		// check the lane object in front of us (on our street). This is separated from the loop beneath as this is done most of the time!
		if(next_ != null){
			if(next_.getCurLane() == lane){	// next one is on the same lane
				foundNextVehicle = true;
				if((curDirection_ && next_.getCurPosition()-curPosition_ < curBrakingDistance_) || (!curDirection_ && curPosition_-next_.getCurPosition() < curBrakingDistance_)){
					if(curSpeed_ > next_.getCurSpeed()-brakingRate_) return 1;
				}
			} else {	// need to search for the next which is on our lane
				LaneObject tmpLaneObject = next_.getNext();
				while(tmpLaneObject != null){
					if(tmpLaneObject.getCurLane() == lane){
						foundNextVehicle = true;
						if((curDirection_ && tmpLaneObject.getCurPosition()-curPosition_ < curBrakingDistance_) || (!curDirection_ && curPosition_-tmpLaneObject.getCurPosition() < curBrakingDistance_)){
							if(curSpeed_ > next_.getCurSpeed()-brakingRate_) return 1;
						}
						break;	// only check the first on our lane!
					}
					tmpLaneObject = tmpLaneObject.getNext();
				}
			}
		}
		// didn't need to brake because of vehicle directly in front of us
		double distance;
		if(curDirection_) distance = curStreet_.getLength() - curPosition_;
		else distance = curPosition_;
		// only do the big calculation if the current street is empty AND the remainder of the current street is shorter than the braking distance
		if(distance < curBrakingDistance_){
			Street tmpStreet = curStreet_;
			LaneObject tmpLaneObject;
			Node junctionNode, nextNode;
			boolean tmpDirection = curDirection_;
			boolean gotJunctionPermission = false;
			int tmpLane = lane;
			int i;
			int j = routeStreets_.length-1;

			// check next streets that we will visit. If routing is empty this automatically skips (which is what we actually want)
			for(i = routePosition_; i < j;){
				//check for junctions
				if(tmpDirection) junctionNode = tmpStreet.getEndNode();
				else junctionNode = tmpStreet.getStartNode();
				if(junctionNode.getJunction() != null){
					if(junctionAllowed_ != junctionNode){
						if(routeDirections_[i+1]) nextNode = routeStreets_[i+1].getEndNode();
						else nextNode = routeStreets_[i+1].getStartNode();
						if(junctionNode.isHasTrafficSignal_()){
							if(junctionNode.getJunction().canPassTrafficLight(this, tmpStreet, nextNode)){	
								junctionAllowed_ = junctionNode;
								gotJunctionPermission = true;
							}
							else {
								waitingForSignal_ = true;
								return 2;
							}
						}
						else{
							int priority;
							if(tmpDirection) priority = junctionNode.getJunction().getJunctionPriority(tmpStreet.getStartNode(), nextNode);
							else priority = junctionNode.getJunction().getJunctionPriority(tmpStreet.getEndNode(), nextNode);
							if(priority != 1){	// don't do anything on priority streets
								// don't turn off faster than about 35km/h
								if(curSpeed_ > 1000){
									return 2;
								} else if(priority > 2){
									junctionNode.getJunction().addWaitingVehicle(this, priority);
									if(!junctionNode.getJunction().canPassJunction(this, priority, nextNode)) return 2;
									else {
										junctionAllowed_ = junctionNode;
										gotJunctionPermission = true;
									}
								}
							}
						}
					}
				}
				//get next street
				++i;
				tmpDirection = routeDirections_[i];
				tmpStreet = routeStreets_[i];
				if(tmpLane > tmpStreet.getLanesCount()) tmpLane = tmpStreet.getLanesCount();

				// Check if next street has smaller speed limit
				if(tmpStreet.getSpeed() < curSpeed_) {
					if(gotJunctionPermission) {
						junctionAllowed_.getJunction().allowOtherVehicle();
						junctionAllowed_ = null;
					}					
					return 2;
				}

				// Check if first lane object of next street on our lane forces us to stop
				if(!foundNextVehicle){
					tmpLaneObject = tmpStreet.getFirstLaneObject(tmpDirection);
					while(tmpLaneObject != null){
						if(tmpLaneObject.getCurLane() == tmpLane){
							foundNextVehicle = true;
							if((tmpDirection && tmpLaneObject.getCurPosition()+distance < curBrakingDistance_) || (!tmpDirection && tmpStreet.getLength()-tmpLaneObject.getCurPosition()+distance < curBrakingDistance_)){
								if(curSpeed_ > tmpLaneObject.getCurSpeed()-brakingRate_){
									if(gotJunctionPermission) {
										junctionAllowed_.getJunction().allowOtherVehicle();
										junctionAllowed_ = null;
									}	
									return 1;
								}
							}
							break;
						}
						tmpLaneObject = tmpLaneObject.getNext();
					}
				}

				// calculate distance of the whole street.
				distance += tmpStreet.getLength();
				// We can stop processing the next one if we get longer than braking distance
				if(distance > curBrakingDistance_) break;
				if(tmpStreet == destinations_.peekFirst().getStreet()) break;

			}
		}
		return 0;
		*/
	}

	
	/**
	 * Check if braking is necessary on the specified lane.
	 * 
	 * @param lane	the lane to check
	 * 
	 * @return <code>0</code> if braking is not necessary, <code>1</code> if braking is necessary because of an object on a lane,
	 * 	<code>2</code> if braking is necessary because of a lower speed street or junction
	 */
	private final int checkCurrentBraking(int lane){
		boolean foundNextVehicle = false;
	
				
		// check the lane object in front of us (on our street). This is separated from the loop beneath as this is done most of the time!
		if(next_ != null){
			if(next_.getCurLane() == lane){	// next one is on the same lane
				foundNextVehicle = true;
				if((curDirection_ && next_.getCurPosition()-curPosition_ < curBrakingDistance_) || (!curDirection_ && curPosition_-next_.getCurPosition() < curBrakingDistance_)){
					if(curSpeed_ > next_.getCurSpeed()-brakingRate_){
						if(!next_.getClass().equals(BlockingObject.class)){
							if(emergencyVehicle_){
								if(lastEVAMessageCreated >= MESSAGE_INTERVAL){
									lastEVAMessageCreated = 0;
									// find the destination for the message. Will be sent to the next junction in FRONT of us!
									boolean tmpDirection2 = !curDirection_;
									Street tmpStreet2 = curStreet_;
									Street[] crossingStreets;
									Node tmpNode;
									int k, l = 0, destX = -1, destY = -1;
									do{
										++l;
										if(tmpDirection2){
											tmpNode = tmpStreet2.getStartNode();
										} else {
											tmpNode = tmpStreet2.getEndNode();
										}
										if(tmpNode.getJunction() != null){
											destX = tmpNode.getX();
											destY = tmpNode.getY();
											break;
										}
										crossingStreets = tmpNode.getCrossingStreets();
										// find next street behind of us
										if(crossingStreets.length != 2){	// end of a street or some special case. don't forward any further
											destX = tmpNode.getX();
											destY = tmpNode.getY();
											break;
										}
										for(k = 0; k < crossingStreets.length; ++k){
											if(crossingStreets[k] != tmpStreet2){
												tmpStreet2 = crossingStreets[k];
												if(tmpStreet2.getStartNode() == tmpNode) tmpDirection2 = false;
												else tmpDirection2 = true;
												break;
											}
										}
									} while(tmpStreet2 != curStreet_ && l < 10000);	//hard limit of 10000 nodes to maximally go back or if again arriving at source street (=>circle!)
									// found destination...now insert into messagequeue
									if(destX != -1 && destY != -1){
										int direction = -1;
										if(!curDirection_) direction = 1;
										int time = Renderer.getInstance().getTimePassed();
										PenaltyMessage message = new PenaltyMessage(curX_, curY_, destX, destY, PENALTY_EVA_MESSAGE_RADIUS, time + PENALTY_MESSAGE_VALID, curStreet_, curLane_, direction, PENALTY_MESSAGE_VALUE, time + PENALTY_VALID, false, ID_, this, "EVA_EMERGENCY_ID", true, true);
										message.setFloodingMode(true);	// enable flooding mode if within distance!						
										knownMessages_.addMessage(message, false, true);
										
										++evaMessagesCreated_;
									}							
								}	

							}
							else if(((Vehicle)next_).isInTrafficJam_()  && !((Vehicle)next_).waitingForSignal_ && curSpeed_ > 0){
								
								if(lastPCNFORWARDMessageCreated >= MESSAGE_INTERVAL){
									lastPCNFORWARDMessageCreated = 0;
									// find the destination for the message. Will be sent to the next junction behind us!
									boolean tmpDirection = curDirection_;
									Street tmpStreet = curStreet_;
									Street[] crossingStreets;
									Node tmpNode;
									int i, j = 0, destX = -1, destY = -1;
									do{
										++j;
										if(tmpDirection){
											tmpNode = tmpStreet.getStartNode();
										} else {
											tmpNode = tmpStreet.getEndNode();
										}
										if(tmpNode.getJunction() != null){
											destX = tmpNode.getX();
											destY = tmpNode.getY();
											break;
										}
										crossingStreets = tmpNode.getCrossingStreets();
										// find next street behind of us
										if(crossingStreets.length != 2){	// end of a street or some special case. don't forward any further
											destX = tmpNode.getX();
											destY = tmpNode.getY();
											break;
										}
										for(i = 0; i < crossingStreets.length; ++i){
											if(crossingStreets[i] != tmpStreet){
												tmpStreet = crossingStreets[i];
												if(tmpStreet.getStartNode() == tmpNode) tmpDirection = false;
												else tmpDirection = true;
												break;
											}
										}
									} while(tmpStreet != curStreet_ && j < 10000);	//hard limit of 10000 nodes to maximally go back or if again arriving at source street (=>circle!)
									// found destination...now insert into messagequeue
									if(destX != -1 && destY != -1){
										int direction = -1;
										if(!curDirection_) direction = 1;
										int time = Renderer.getInstance().getTimePassed();
										PenaltyMessage message = new PenaltyMessage(curX_, curY_, destX, destY, PENALTY_MESSAGE_RADIUS, time + PENALTY_MESSAGE_VALID, curStreet_, curLane_, direction, PENALTY_MESSAGE_VALUE, time + PENALTY_VALID, false, ID_, null,  "PCN_FORWARD", false, false);
										long dx = message.getDestinationX_() - curX_;
										long dy = message.getDestinationY_() - curY_;
										if((long)PENALTY_MESSAGE_RADIUS * PENALTY_MESSAGE_RADIUS >= (dx*dx + dy*dy)){
											message.setFloodingMode(true);	// enable flooding mode if within distance!
										}									
										knownMessages_.addMessage(message, false, true);

										++pcnForwardMessagesCreated_;
									}	
														
								}
							}
								
							return 1;
						}
						else if(((BlockingObject) next_).getPenaltyType_().equals("HUANG_RHCN") &&  curSpeed_ < 360) return 0;
						else {
							passingBlocking_ = true;
							if(((BlockingObject) next_).getPenaltyType_().equals("HUANG_RHCN"))	{
								if(lastRHCNMessageCreated >= MESSAGE_INTERVAL){
									if(waitToSendRHCNCounter_ < 0)waitToSendRHCNCounter_ = WAIT_TO_SEND_RHCN_;
									else if(waitToSendRHCNCounter_ > 0) waitToSendRHCNCounter_--;
									else{	
										waitToSendRHCNCounter_ = -1;	
										
									
										lastRHCNMessageCreated = 0;
										// find the destination for the message. Will be sent to the next junction behind us!
										boolean tmpDirection2 = curDirection_;
										Street tmpStreet2 = curStreet_;
										Street[] crossingStreets;
										Node tmpNode;
										int k, l = 0, destX = -1, destY = -1;
										do{
											++l;
											if(tmpDirection2){
												tmpNode = tmpStreet2.getStartNode();
											} else {
												tmpNode = tmpStreet2.getEndNode();
											}
											if(tmpNode.getJunction() != null){
												destX = tmpNode.getX();
												destY = tmpNode.getY();
												break;
											}
											crossingStreets = tmpNode.getCrossingStreets();
											// find next street behind of us
											if(crossingStreets.length != 2){	// end of a street or some special case. don't forward any further
												destX = tmpNode.getX();
												destY = tmpNode.getY();
												break;
											}
											for(k = 0; k < crossingStreets.length; ++k){
												if(crossingStreets[k] != tmpStreet2){
													tmpStreet2 = crossingStreets[k];
													if(tmpStreet2.getStartNode() == tmpNode) tmpDirection2 = false;
													else tmpDirection2 = true;
													break;
												}
											}
										} while(tmpStreet2 != curStreet_ && l < 10000);	//hard limit of 10000 nodes to maximally go back or if again arriving at source street (=>circle!)
										// found destination...now insert into messagequeue
										if(destX != -1 && destY != -1){
											int direction = -1;
											if(!curDirection_) direction = 1;
											int time = Renderer.getInstance().getTimePassed();
											PenaltyMessage message = new PenaltyMessage(((BlockingObject) next_).getX(), ((BlockingObject) next_).getY(), destX, destY, PENALTY_MESSAGE_RADIUS, time + PENALTY_MESSAGE_VALID, curStreet_, curLane_, direction, PENALTY_MESSAGE_VALUE, time + PENALTY_VALID, false, ID_, this, "HUANG_RHCN", false, true);
											long dx = message.getDestinationX_() - curX_;
											long dy = message.getDestinationY_() - curY_;
											if((long)PENALTY_MESSAGE_RADIUS * PENALTY_MESSAGE_RADIUS >= (dx*dx + dy*dy)){
												message.setFloodingMode(true);	// enable flooding mode if within distance!
											}								
											knownMessages_.addMessage(message, false, true);

											++rhcnMessagesCreated_;
										}	
									}						
								}								
							}
							return 1;
						}
					}	
					

				}
			} else {	// need to search for the next which is on our lane
				LaneObject tmpLaneObject = next_.getNext();
				while(tmpLaneObject != null){
					if(tmpLaneObject.getCurLane() == lane){
						foundNextVehicle = true;
						if((curDirection_ && tmpLaneObject.getCurPosition()-curPosition_ < curBrakingDistance_) || (!curDirection_ && curPosition_-tmpLaneObject.getCurPosition() < curBrakingDistance_)){
							if(curSpeed_ > next_.getCurSpeed()-brakingRate_){
								if(!next_.getClass().equals(BlockingObject.class)){
									if(emergencyVehicle_){
										if(lastEVAMessageCreated >= MESSAGE_INTERVAL){
											lastEVAMessageCreated = 0;
											// find the destination for the message. Will be sent to the next junction in FRONT of us!
											boolean tmpDirection2 = !curDirection_;
											Street tmpStreet2 = curStreet_;
											Street[] crossingStreets;
											Node tmpNode;
											int k, l = 0, destX = -1, destY = -1;
											do{
												++l;
												if(tmpDirection2){
													tmpNode = tmpStreet2.getStartNode();
												} else {
													tmpNode = tmpStreet2.getEndNode();
												}
												if(tmpNode.getJunction() != null){
													destX = tmpNode.getX();
													destY = tmpNode.getY();
													break;
												}
												crossingStreets = tmpNode.getCrossingStreets();
												// find next street behind of us
												if(crossingStreets.length != 2){	// end of a street or some special case. don't forward any further
													destX = tmpNode.getX();
													destY = tmpNode.getY();
													break;
												}
												for(k = 0; k < crossingStreets.length; ++k){
													if(crossingStreets[k] != tmpStreet2){
														tmpStreet2 = crossingStreets[k];
														if(tmpStreet2.getStartNode() == tmpNode) tmpDirection2 = false;
														else tmpDirection2 = true;
														break;
													}
												}
											} while(tmpStreet2 != curStreet_ && l < 10000);	//hard limit of 10000 nodes to maximally go back or if again arriving at source street (=>circle!)
											// found destination...now insert into messagequeue
											if(destX != -1 && destY != -1){
												int direction = -1;
												if(!curDirection_) direction = 1;
												int time = Renderer.getInstance().getTimePassed();
												
												PenaltyMessage message = new PenaltyMessage(curX_, curY_, destX, destY, PENALTY_EVA_MESSAGE_RADIUS, time + PENALTY_MESSAGE_VALID, curStreet_, curLane_, direction, PENALTY_MESSAGE_VALUE, time + PENALTY_VALID, false, ID_, this, "EVA_EMERGENCY_ID", true, true);
												message.setFloodingMode(true);	// enable flooding mode if within distance!								
												knownMessages_.addMessage(message, false, true);
										

												++evaMessagesCreated_;
											}							
										}	
									}
									else if(((Vehicle)next_).isInTrafficJam_() && !((Vehicle)next_).waitingForSignal_ && curSpeed_ > 0){
										
										if(lastPCNFORWARDMessageCreated >= MESSAGE_INTERVAL){
											lastPCNFORWARDMessageCreated = 0;
											// find the destination for the message. Will be sent to the next junction behind us!
											boolean tmpDirection = curDirection_;
											Street tmpStreet = curStreet_;
											Street[] crossingStreets;
											Node tmpNode;
											int i, j = 0, destX = -1, destY = -1;
											do{
												++j;
												if(tmpDirection){
													tmpNode = tmpStreet.getStartNode();
												} else {
													tmpNode = tmpStreet.getEndNode();
												}
												if(tmpNode.getJunction() != null){
													destX = tmpNode.getX();
													destY = tmpNode.getY();
													break;
												}
												crossingStreets = tmpNode.getCrossingStreets();
												// find next street behind of us
												if(crossingStreets.length != 2){	// end of a street or some special case. don't forward any further
													destX = tmpNode.getX();
													destY = tmpNode.getY();
													break;
												}
												for(i = 0; i < crossingStreets.length; ++i){
													if(crossingStreets[i] != tmpStreet){
														tmpStreet = crossingStreets[i];
														if(tmpStreet.getStartNode() == tmpNode) tmpDirection = false;
														else tmpDirection = true;
														break;
													}
												}
											} while(tmpStreet != curStreet_ && j < 10000);	//hard limit of 10000 nodes to maximally go back or if again arriving at source street (=>circle!)
											// found destination...now insert into messagequeue
											if(destX != -1 && destY != -1){
												int direction = -1;
												if(!curDirection_) direction = 1;
												int time = Renderer.getInstance().getTimePassed();
												PenaltyMessage message = new PenaltyMessage(curX_, curY_, destX, destY, PENALTY_MESSAGE_RADIUS, time + PENALTY_MESSAGE_VALID, curStreet_, curLane_, direction, PENALTY_MESSAGE_VALUE, time + PENALTY_VALID, false, ID_, this,  "PCN_FORWARD", false, false);
												long dx = message.getDestinationX_() - curX_;
												long dy = message.getDestinationY_() - curY_;
												if((long)PENALTY_MESSAGE_RADIUS * PENALTY_MESSAGE_RADIUS >= (dx*dx + dy*dy)){
													message.setFloodingMode(true);	// enable flooding mode if within distance!
												}							
												knownMessages_.addMessage(message, false, true);

												++pcnForwardMessagesCreated_;
											}															
										}
									}
									return 1;
								}
								else if(((BlockingObject) next_).getPenaltyType_().equals("HUANG_RHCN") &&  curSpeed_ < 360 ) return 0;
								else{
					
									passingBlocking_ = true;
									if(((BlockingObject) next_).getPenaltyType_().equals("HUANG_RHCN"))	{	
										if(lastRHCNMessageCreated >= MESSAGE_INTERVAL){
											lastRHCNMessageCreated = 0;
											// find the destination for the message. Will be sent to the next junction behind us!
											boolean tmpDirection2 = curDirection_;
											Street tmpStreet2 = curStreet_;
											Street[] crossingStreets;
											Node tmpNode;
											int k, l = 0, destX = -1, destY = -1;
											do{
												++l;
												if(tmpDirection2){
													tmpNode = tmpStreet2.getStartNode();
												} else {
													tmpNode = tmpStreet2.getEndNode();
												}
												if(tmpNode.getJunction() != null){
													destX = tmpNode.getX();
													destY = tmpNode.getY();
													break;
												}
												crossingStreets = tmpNode.getCrossingStreets();
												// find next street behind of us
												if(crossingStreets.length != 2){	// end of a street or some special case. don't forward any further
													destX = tmpNode.getX();
													destY = tmpNode.getY();
													break;
												}
												for(k = 0; k < crossingStreets.length; ++k){
													if(crossingStreets[k] != tmpStreet2){
														tmpStreet2 = crossingStreets[k];
														if(tmpStreet2.getStartNode() == tmpNode) tmpDirection2 = false;
														else tmpDirection2 = true;
														break;
													}
												}
											} while(tmpStreet2 != curStreet_ && l < 10000);	//hard limit of 10000 nodes to maximally go back or if again arriving at source street (=>circle!)
											// found destination...now insert into messagequeue
											if(destX != -1 && destY != -1){
												int direction = -1;
												if(!curDirection_) direction = 1;
												int time = Renderer.getInstance().getTimePassed();
												PenaltyMessage message = new PenaltyMessage(((BlockingObject) next_).getX(), ((BlockingObject) next_).getY(), destX, destY, PENALTY_MESSAGE_RADIUS, time + PENALTY_MESSAGE_VALID, curStreet_, curLane_, direction, PENALTY_MESSAGE_VALUE, time + PENALTY_VALID, false, ID_, this, "HUANG_RHCN", false, true);
												long dx = message.getDestinationX_() - curX_;
												long dy = message.getDestinationY_() - curY_;
												if((long)PENALTY_MESSAGE_RADIUS * PENALTY_MESSAGE_RADIUS >= (dx*dx + dy*dy)){
													message.setFloodingMode(true);	// enable flooding mode if within distance!
												}								
												knownMessages_.addMessage(message, false, true);
												++rhcnMessagesCreated_;

											}							
										}	
									}
									return 1;
								}
							}							}
						break;	// only check the first on our lane!
					}
					tmpLaneObject = tmpLaneObject.getNext();
				}
			}
		}
		// didn't need to brake because of vehicle directly in front of us
		double distance;
		if(curDirection_) distance = curStreet_.getLength() - curPosition_;
		else distance = curPosition_;
		// only do the big calculation if the current street is empty AND the remainder of the current street is shorter than the braking distance
		if(distance < curBrakingDistance_){
			Street tmpStreet = curStreet_;
			LaneObject tmpLaneObject;
			Node junctionNode, nextNode;
			boolean tmpDirection = curDirection_;
			boolean gotJunctionPermission = false;
			int tmpLane = lane;
			int i;
			int j = routeStreets_.length-1;

			// check next streets that we will visit. If routing is empty this automatically skips (which is what we actually want)
			for(i = routePosition_; i < j;){
				//check for junctions
				if(tmpDirection) junctionNode = tmpStreet.getEndNode();
				else junctionNode = tmpStreet.getStartNode();
				if(junctionNode.getJunction() != null){
	
					if(junctionAllowed_ != junctionNode){
						if(routeDirections_[i+1]) nextNode = routeStreets_[i+1].getEndNode();
						else nextNode = routeStreets_[i+1].getStartNode();
						if(junctionNode.isHasTrafficSignal_()){
							if(junctionNode.getJunction().canPassTrafficLight(this, tmpStreet, nextNode)){	
								junctionAllowed_ = junctionNode;
								gotJunctionPermission = true;
							}
							else {
								waitingForSignal_ = true;
								return 2;
							}
						}
						else{
							int priority;
							if(tmpDirection) priority = junctionNode.getJunction().getJunctionPriority(tmpStreet.getStartNode(), nextNode);
							else priority = junctionNode.getJunction().getJunctionPriority(tmpStreet.getEndNode(), nextNode);
							if(priority != 1){	// don't do anything on priority streets
								// don't turn off faster than about 35km/h
								if(curSpeed_ > 1000){
									return 2;
								} else if(priority > 2){
									junctionNode.getJunction().addWaitingVehicle(this, priority);
									if(!emergencyVehicle_ && !fakingMessages_ && !junctionNode.getJunction().canPassJunction(this, priority, nextNode)) return 2;
									else {
										junctionAllowed_ = junctionNode;
										gotJunctionPermission = true;
									}
								}
							}
						}
					}
				}
				//get next street
				++i;
				tmpDirection = routeDirections_[i];
				tmpStreet = routeStreets_[i];
				if(tmpLane > tmpStreet.getLanesCount()) tmpLane = tmpStreet.getLanesCount();

				// Check if next street has smaller speed limit
				if(tmpStreet.getSpeed() < curSpeed_) {
					if(gotJunctionPermission) {
						junctionAllowed_.getJunction().allowOtherVehicle();
						junctionAllowed_ = null;
					}					
					return 2;
				}

				// Check if first lane object of next street on our lane forces us to stop
				if(!foundNextVehicle){
					tmpLaneObject = tmpStreet.getFirstLaneObject(tmpDirection);
					while(tmpLaneObject != null){
						if(tmpLaneObject.getCurLane() == tmpLane){
							foundNextVehicle = true;
							if((tmpDirection && tmpLaneObject.getCurPosition()+distance < curBrakingDistance_) || (!tmpDirection && tmpStreet.getLength()-tmpLaneObject.getCurPosition()+distance < curBrakingDistance_)){
								if(curSpeed_ > tmpLaneObject.getCurSpeed()-brakingRate_){
									if(gotJunctionPermission) {
										junctionAllowed_.getJunction().allowOtherVehicle();
										junctionAllowed_ = null;
									}	
									return 1;
								}
							}
							break;
						}
						tmpLaneObject = tmpLaneObject.getNext();
					}
				}

				// calculate distance of the whole street.
				distance += tmpStreet.getLength();
				// We can stop processing the next one if we get longer than braking distance
				if(distance > curBrakingDistance_) break;
				if(tmpStreet == destinations_.peekFirst().getStreet()) break;

			}		
		}

	
		return 0;
	}
	
	/**
	 * Check if a lane is free. It is considered as free if
	 * <ul><li>in front of this vehicle there's minimum double the current braking distance space and</li>
	 * <li>behind there's at least the current braking distance + 10m space</li>
	 * </ul>
	 * 
	 * @param lane 	the lane to check
	 * 
	 * @return <code>true</code> if lane is free, else <code>false</code>
	 */
	
	private final boolean checkLaneFree(int lane){
		// ================================= 
		// Step 1: Check space in front
		// ================================= 
		boolean foundNextVehicle = false;
		int neededFreeDistance = curBrakingDistance_ / 2;
		// check the lane object in front of us (on our street)
		if(next_ != null){
			if(next_.getCurLane() == lane){	// next one is on the same lane
				foundNextVehicle = true;
				if((curDirection_ && next_.getCurPosition()-curPosition_ < neededFreeDistance) || (!curDirection_ && curPosition_-next_.getCurPosition() < neededFreeDistance)){
					if(curSpeed_ > next_.getCurSpeed()-brakingRate_) return false;
				}
			} else {	// need to search for the next which is on our lane
				LaneObject tmpLaneObject = next_.getNext();
				while(tmpLaneObject != null){
					if(tmpLaneObject.getCurLane() == lane){
						foundNextVehicle = true;
						if((curDirection_ && tmpLaneObject.getCurPosition()-curPosition_ < neededFreeDistance) || (!curDirection_ && curPosition_-tmpLaneObject.getCurPosition() < neededFreeDistance)){
							if(curSpeed_ > next_.getCurSpeed()-brakingRate_) return false;
						}
						break;	// only check the first on our lane!
					}
					tmpLaneObject = tmpLaneObject.getNext();
				}
			}
		}
		double distance;
		if(curDirection_) distance = curStreet_.getLength() - curPosition_;
		else distance = curPosition_;
		// only do the big calculation if the current street is empty AND the remainder of the current street is shorter than the braking distance
		if(!foundNextVehicle && distance < neededFreeDistance){
			Street tmpStreet = curStreet_;
			LaneObject tmpLaneObject;
			boolean tmpDirection = curDirection_;
			// check next streets that we will visit. If routing is empty this automatically skips (which is what we actually want)
			for(int i = routePosition_ + 1; i < routeStreets_.length; ++i){
				tmpDirection = routeDirections_[i];
				tmpStreet = routeStreets_[i];		

				if(!foundNextVehicle){
					tmpLaneObject = tmpStreet.getFirstLaneObject(tmpDirection);
					while(tmpLaneObject != null){
						if(tmpLaneObject.getCurLane() == lane){
							foundNextVehicle = true;
							if((tmpDirection && tmpLaneObject.getCurPosition()+distance < neededFreeDistance) || (!tmpDirection && tmpStreet.getLength()-tmpLaneObject.getCurPosition()+distance < neededFreeDistance)){
								if(curSpeed_ > tmpLaneObject.getCurSpeed()-brakingRate_) return false;
							}
							break;
						}
						tmpLaneObject = tmpLaneObject.getNext();
					}
				}

				// calculate distance of the whole street. We can stop processing the next one if we get longer than braking distance
				distance += tmpStreet.getLength();
				if(distance > neededFreeDistance) break;
			}
		}

		// ================================= 
		// Step 2: Check space behind
		// ================================= 
		neededFreeDistance = curBrakingDistance_;
		boolean foundPreviousVehicle = false;
		// check the lane object before us (on our street)
		if(previous_ != null){
			if(previous_.getCurLane() == lane){	// is on the same lane
				foundPreviousVehicle = true;
				if((curDirection_ && curPosition_-previous_.getCurPosition() < neededFreeDistance) || (!curDirection_ && previous_.getCurPosition()-curPosition_ < neededFreeDistance)){
					if(curSpeed_ > previous_.getCurSpeed()-brakingRate_) return false;
				}
			} else {	// need to search for the previous one which is on our lane
				LaneObject tmpLaneObject = previous_.getPrevious();
				while(tmpLaneObject != null){
					if(tmpLaneObject.getCurLane() == lane){
						foundPreviousVehicle = true;
						if((curDirection_ && curPosition_-tmpLaneObject.getCurPosition() < neededFreeDistance) || (!curDirection_ && tmpLaneObject.getCurPosition()-curPosition_ < neededFreeDistance)){
							if(curSpeed_ > previous_.getCurSpeed()-brakingRate_) return false;
						}
						break;	// only check the first on our lane!
					}
					tmpLaneObject = tmpLaneObject.getPrevious();
				}
			}
		}
		if(curDirection_) distance = curPosition_;
		else distance = curStreet_.getLength() - curPosition_;
		// current street is not long enough. need to iterate backwards
		if(!foundPreviousVehicle && distance < neededFreeDistance){
			Street[] outgoingStreets;
			Street tmpStreet = curStreet_, tmpStreet2;
			LaneObject tmpLaneObject;
			Node nextNode = null;
			boolean tmpDirection = curDirection_;
			int i;
			// check previous streets. We can't use the routing here so it's limited to streets with no junctions which makes it a bit simpler.
			while(true){
				if(tmpDirection) nextNode = tmpStreet.getStartNode();
				else nextNode = tmpStreet.getEndNode();
				if(nextNode.getCrossingStreetsCount() != 2) return false;		// don't handle junctions!
				else outgoingStreets = nextNode.getCrossingStreets();
				for(i = 0; i < outgoingStreets.length; ++i){
					tmpStreet2 = outgoingStreets[i];
					if(tmpStreet2 != tmpStreet){
						tmpStreet = tmpStreet2;
						if(lane > tmpStreet.getLanesCount()) return false;
						if (tmpStreet2.getStartNode() == nextNode){
							tmpDirection = true;
							break;		// found street we want to => no need to look through others
						} else {
							tmpDirection = false;
							break;		// found street we want to => no need to look through others
						}
					}					
				}				

				if(!foundPreviousVehicle){
					tmpLaneObject = tmpStreet.getFirstLaneObject(tmpDirection);
					while(tmpLaneObject != null){
						if(tmpLaneObject.getCurLane() == lane){
							foundNextVehicle = true;
							if((tmpDirection && tmpStreet.getLength()-tmpLaneObject.getCurPosition()+distance < neededFreeDistance) || (!tmpDirection && tmpLaneObject.getCurPosition()+distance < neededFreeDistance)){
								if(curSpeed_ > tmpLaneObject.getCurSpeed()-brakingRate_) return false;
							}
							break;
						}
						tmpLaneObject = tmpLaneObject.getNext();
					}
				}
			
				// calculate distance of the whole street. We can stop processing the next one if we get longer than braking distance
				distance += tmpStreet.getLength();
				if(distance > neededFreeDistance) break;
			}
		}
		return true;
	}
	
	
	/**
	 * Find vehicles in neighborhood and give information to them. Please check the following conditions before calling this function:
	 * <ul>
	 * <li>communication is generally enabled</li>
	 * <li>if this vehicle is active</li>
	 * <li>if it has wifi</li> 
	 * <li>if the communication countdown is 0 or less</li>
	 * </ul>
	 */
	public void sendMessages(){
			
		communicationCountdown_ += communicationInterval_;
		if(beaconsEnabled_ && !isInMixZone_){
			Message[] messages = knownMessages_.getForwardMessages();
			int size = knownMessages_.getSize();
			Vehicle nearestVehicle;
			
			//send messages to all knownRSUs
			RSU nearestRSU;
			for(int i = size - 1; i > -1; --i){			
				KnownRSU[] rsuHeads = knownRSUsList_.getFirstKnownRSU();
				int sendCount = 0;
				KnownRSU rsuNext;
				long dx, dy, maxCommSquared = (long)maxCommDistance_ * maxCommDistance_;
				for(int j = 0; j < rsuHeads.length; ++j){
					rsuNext = rsuHeads[j];								
					while(rsuNext != null){
						++sendCount;
						nearestRSU = rsuNext.getRSU();
						dx = nearestRSU.getX() - curX_;
						dy = nearestRSU.getY() - curY_;
						if((dx * dx + dy * dy) < maxCommSquared && !nearestRSU.isEncrypted_()){	//check if vehicle really is in communication distance and it's no mix-zone rsu
							nearestRSU.receiveMessage(curX_, curY_, messages[i]);
						}
						rsuNext = rsuNext.getNext();
					}
				}
				
				// flooding mode => send to all known vehicles
				if(messages[i].getFloodingMode()){
					KnownVehicle[] heads = knownVehiclesList_.getFirstKnownVehicle();
					KnownVehicle next;
					for(int j = 0; j < heads.length; ++j){
						next = heads[j];								
						while(next != null){
							++sendCount;
							nearestVehicle = next.getVehicle();
							dx = nearestVehicle.getX() - curX_;
							dy = nearestVehicle.getY() - curY_;
							if((dx * dx + dy * dy) < maxCommSquared){	//check if vehicle really is in communication distance
								//nearestVehicle.setColor(Color.red);
								nearestVehicle.receiveMessage(curX_, curY_, messages[i]);
							}
							next = next.getNext();
						}
					}

					if(sendCount > 0) knownMessages_.deleteForwardMessage(i, true);
				// line based mode => only communicate with the nearest known vehicle to message destination
				} else {
					
					nearestVehicle = knownVehiclesList_.findNearestVehicle(curX_, curY_, messages[i].getDestinationX_(), messages[i].getDestinationY_(), maxCommDistance_);
					if(nearestVehicle != null){	// only communicate if a nearer vehicle was found!
						nearestVehicle.receiveMessage(curX_, curY_, messages[i]);
						knownMessages_.deleteForwardMessage(i, true);
					}
				}
			}
		} else if (!isInMixZone_ || mixZonesFallbackEnabled_){	
			Message[] messages = knownMessages_.getForwardMessages();
			int messageSize = knownMessages_.getSize();
			if(messageSize > 0){
				// only look through all vehicles if beacons are generally disabled and messages need to be sent in a bruteforce-mode or if the fallback mode in mix zones is enabled
				int MapMinX, MapMinY, MapMaxX, MapMaxY, RegionMinX, RegionMinY, RegionMaxX, RegionMaxY;
	
				// Minimum x coordinate to be considered for communication
				long tmp = curX_ - maxCommDistance_;
				if (tmp < 0) MapMinX = 0;	// Map stores only positive coordinates
				else if(tmp < Integer.MAX_VALUE) MapMinX = (int) tmp;
				else MapMinX = Integer.MAX_VALUE;
	
				// Maximum x coordinate to be considered for communication
				tmp = curX_ + (long)maxCommDistance_;
				if (tmp < 0) MapMaxX = 0;
				else if(tmp < Integer.MAX_VALUE) MapMaxX = (int) tmp;
				else MapMaxX = Integer.MAX_VALUE;
	
				// Minimum y coordinate to be considered for communication
				tmp = curY_ - maxCommDistance_;
				if (tmp < 0) MapMinY = 0;
				else if(tmp < Integer.MAX_VALUE) MapMinY = (int) tmp;
				else MapMinY = Integer.MAX_VALUE;
	
				// Maximum y coordinate to be considered for communication
				tmp = curY_ + (long)maxCommDistance_;
				if (tmp < 0) MapMaxY = 0;
				else if(tmp < Integer.MAX_VALUE) MapMaxY = (int) tmp;
				else MapMaxY = Integer.MAX_VALUE;
	
				// Get the regions to be considered for communication
				Region tmpregion = MAP.getRegionOfPoint(MapMinX, MapMinY);
				RegionMinX = tmpregion.getX();
				RegionMinY = tmpregion.getY();
	
				tmpregion = MAP.getRegionOfPoint(MapMaxX, MapMaxY);
				RegionMaxX = tmpregion.getX();
				RegionMaxY = tmpregion.getY();
				long maxCommDistance_square = (long)maxCommDistance_ * maxCommDistance_;
				long dx, dy, distance = 0;
				int i, j, k, l, size;
				Vehicle[] vehicles = null;
				Vehicle vehicle = null;
				
				RSU[] rsus = null;
				RSU rsu = null;
				
	
				// only iterate through those regions which are within the distance
				for(i = RegionMinX; i <= RegionMaxX; ++i){
					for(j = RegionMinY; j <= RegionMaxY; ++j){
						//send to vehicles
						vehicles = regions_[i][j].getVehicleArray();	//use the array as it's MUCH faster!
						size = vehicles.length;
						for(k = 0; k < size; ++k){
							vehicle = vehicles[k];
							// precheck if the vehicle is near enough and valid (check is not exact as its a rectangular box and not circle)
							if(vehicle.isWiFiEnabled() && vehicle.isActive() && vehicle != this && vehicle.getX() >= MapMinX && vehicle.getX() <= MapMaxX && vehicle.getY() >= MapMinY && vehicle.getY() <= MapMaxY){
								dx = vehicle.getX() - curX_;
								dy = vehicle.getY() - curY_;
								distance = dx * dx + dy * dy; 	// Pythagorean theorem: a^2 + b^2 = c^2 but without the needed Math.sqrt to save a little bit performance
								if(distance <= maxCommDistance_square){
									if(!isInMixZone_ || !mixZonesFallbackFloodingOnly_){
										for(l = 0; l < messageSize; ++l){
											vehicle.receiveMessage(curX_, curY_, messages[l]);
											//vehicle.setColor(Color.blue);
										}
									} else {
										for(l = 0; l < messageSize; ++l){
											if(messages[l].getFloodingMode()) {
												vehicle.receiveMessage(curX_, curY_, messages[l]);
												//vehicle.setColor(Color.cyan);
											}
										}
									}
								}
							}
						}
						
						//send to Road-Side-Units
						rsus = regions_[i][j].getRSUs();	//use the array as it's MUCH faster!
						size = rsus.length;
						for(k = 0; k < size; ++k){
							rsu = rsus[k];
							// precheck if the rsu is near enough and valid (check is not exact as its a rectangular box and not circle)
							if(rsu.getX() >= MapMinX && rsu.getX() <= MapMaxX && rsu.getY() >= MapMinY && rsu.getY() <= MapMaxY){
								dx = rsu.getX() - curX_;
								dy = rsu.getY() - curY_;
								distance = dx * dx + dy * dy; 	// Pythagorean theorem: a^2 + b^2 = c^2 but without the needed Math.sqrt to save a little bit performance
								if(distance <= maxCommDistance_square){
									if(!isInMixZone_ || !mixZonesFallbackFloodingOnly_){
										for(l = 0; l < messageSize; ++l){
											rsu.receiveMessage(curX_, curY_, messages[l]);
										}
									} else {
										for(l = 0; l < messageSize; ++l){
											if(messages[l].getFloodingMode()) rsu.receiveMessage(curX_, curY_, messages[l]);
										}
									}
								}
							}
						}
					}
				}
				if(!isInMixZone_ || !mixZonesFallbackFloodingOnly_) knownMessages_.deleteAllForwardMessages(true);
				else knownMessages_.deleteAllFloodingForwardMessages(true);
			}
		} 
	}
	
	/**
	 * Receive a message from another vehicle.
	 * 
	 * @param sourceX	the x coordinate of the other vehicle
	 * @param sourceY	the y coordinate of the other vehicle
	 * @param message	the message
	 */
	public final void receiveMessage(int sourceX, int sourceY, Message message){
		long dx = message.getDestinationX_() - curX_;
		long dy = message.getDestinationY_() - curY_;
		long distanceToDestinationSquared = dx*dx + dy*dy;
		
		if(message.getFloodingMode()){	// in flooding mode, vehicles only forward messages they got if they are within the target area
			if((message.getDestinationRadiusSquared() >= distanceToDestinationSquared) && !directCommunicationMode_){
				knownMessages_.addMessage(message, true, true);
			} else knownMessages_.addMessage(message, true, false);
		} else {	// line-based mode
			if(message.getDestinationRadiusSquared() >= distanceToDestinationSquared){
				message.setFloodingMode(true);	// enable flooding mode if within distance!
			}
			if(beaconsEnabled_){	// if beacons are enabled, we can be sure that we are nearer than the last vehicle
				if(directCommunicationMode_)knownMessages_.addMessage(message, true, false);
				else knownMessages_.addMessage(message, true, true);
			} else {
				// no beacons. check manually if we are nearer to the destination than the sending vehicle
				dx = message.getDestinationX_() - sourceX;
				dy = message.getDestinationY_() - sourceY;
				if(((dx * dx + dy * dy) > distanceToDestinationSquared)  && !directCommunicationMode_){
					knownMessages_.addMessage(message, true, true);
				} else knownMessages_.addMessage(message, true, false);
			}
		}
	}

	/**
	 * Find vehicles in neighborhood and send beacons to them. Please check the following conditions before calling this function:
	 * <ul>
	 * <li>communication is generally enabled</li>
	 * <li>beacons are generally enabled</li>
	 * <li>if this vehicle is active</li>
	 * <li>if it has wifi</li> 
	 * <li>if the beacon countdown is 0 or less</li>
	 * <li>if vehicle is not in a mix zone</li>
	 * </ul>
	 */
	public void sendBeacons(){
		beaconCountdown_ += beaconInterval_;
		
		
		if(probeDataActive_ && Renderer.getInstance().getTimePassed() > startAfterTime_ && amountOfProbeData_ > 0){
			if(curProbeDataInterval_ <= 1){
				
				GeneralLogWriter.log(Renderer.getInstance().getTimePassed() + ":" +  curX_ + ":" +  curY_ + ":" + curSpeed_ + ":" + ID_);

				curProbeDataInterval_  = PROBE_DATA_TIME_INTERVAL;
				amountOfProbeData_--;
			}
			else curProbeDataInterval_--;
		}

		
		if(isInSlow && !changedPseudonymInSlow && Renderer.getInstance().getTimePassed() >= (slowTimestamp + TIME_TO_PSEUDONYM_CHANGE - (2*beaconInterval_))){
			changedPseudonymInSlow = true;
			
			++IDsChanged_;
			ID_ = ownRandom_.nextLong();
		}
		
		if(slowOn){
			if(privacyDataLogged_ && isInSlow && !slowBeaconsLogged){
				slowBeaconsLogged = true;
				if(!vehicleJustStartedInSlow)PrivacyLogWriter.log(savedBeacon2.replace("%0%aa%0%", "IN")  + "\n" + savedBeacon1.replace("%0%aa%0%", "IN") );
			}
			
			else if(privacyDataLogged_ && !isInSlow && slowBeaconsLogged){
				slowBeaconsLogged = false;
				
				logNextBeacons = 2;
			}
		}

		if(slowOn){
			if(!isInSlow && this.curSpeed_ <= SLOW_SPEED_LIMIT && logNextBeacons == 0){
				isInSlow = true;
				slowTimestamp = Renderer.getInstance().getTimePassed();
				changedPseudonymInSlow = false;

			}
			else if(isInSlow && this.curSpeed_ > SLOW_SPEED_LIMIT && (Renderer.getInstance().getTimePassed() - slowTimestamp) > (2*beaconInterval_)){
				isInSlow = false;
			}
		}


		
		if(silent_period != silentPeriod){ 
			silentPeriod = silent_period;
			
			if(!silent_period) logNextBeacons = 2;
			//log beacon
			if(silentPeriod && privacyDataLogged_ && isSilentPeriodsOn()) PrivacyLogWriter.log(savedBeacon2 + "\n" + savedBeacon1);
		}
		

		
		if(!silent_period && !isInSlow){
			//beaconCountdown_ += beaconInterval_;
			int i, j, k, size = 0, MapMinX, MapMinY, MapMaxX, MapMaxY, RegionMinX, RegionMinY, RegionMaxX, RegionMaxY;
			Vehicle[] vehicles = null;
			Vehicle vehicle = null;
		

			// Minimum x coordinate to be considered for sending beacons
			long tmp = curX_ - maxCommDistance_;
			if (tmp < 0) MapMinX = 0;	// Map stores only positive coordinates
			else if(tmp < Integer.MAX_VALUE) MapMinX = (int) tmp;
			else MapMinX = Integer.MAX_VALUE;

			// Maximum x coordinate to be considered for sending beacons
			tmp = curX_ + (long)maxCommDistance_;
			if (tmp < 0) MapMaxX = 0;
			else if(tmp < Integer.MAX_VALUE) MapMaxX = (int) tmp;
			else MapMaxX = Integer.MAX_VALUE;

			// Minimum y coordinate to be considered for sending beacons
			tmp = curY_ - maxCommDistance_;
			if (tmp < 0) MapMinY = 0;
			else if(tmp < Integer.MAX_VALUE) MapMinY = (int) tmp;
			else MapMinY = Integer.MAX_VALUE;

			// Maximum y coordinate to be considered for sending beacons
			tmp = curY_ + (long)maxCommDistance_;
			if (tmp < 0) MapMaxY = 0;
			else if(tmp < Integer.MAX_VALUE) MapMaxY = (int) tmp;
			else MapMaxY = Integer.MAX_VALUE;

			// Get the regions to be considered for sending beacons
			Region tmpregion = MAP.getRegionOfPoint(MapMinX, MapMinY);
			RegionMinX = tmpregion.getX();
			RegionMinY = tmpregion.getY();

			tmpregion = MAP.getRegionOfPoint(MapMaxX, MapMaxY);
			RegionMaxX = tmpregion.getX();
			RegionMaxY = tmpregion.getY();
			long maxCommDistanceSquared = (long)maxCommDistance_ * maxCommDistance_;
			long dx, dy;


			// only iterate through those regions which are within the distance
			for(i = RegionMinX; i <= RegionMaxX; ++i){
				for(j = RegionMinY; j <= RegionMaxY; ++j){
					vehicles = regions_[i][j].getVehicleArray();	//use the array as it's MUCH faster!
					size = vehicles.length;

					for(k = 0; k < size; ++k){
						vehicle = vehicles[k];
						// precheck if the vehicle is near enough and valid (check is not exact as its a rectangular box and not circle)
						if(vehicle.isWiFiEnabled() && vehicle.isActive() && vehicle != this && vehicle.getX() >= MapMinX && vehicle.getX() <= MapMaxX && vehicle.getY() >= MapMinY && vehicle.getY() <= MapMaxY){
							dx = vehicle.getX() - curX_;
							dy = vehicle.getY() - curY_;
							if((dx * dx + dy * dy) <= maxCommDistanceSquared){	// Pythagorean theorem: a^2 + b^2 = c^2 but without the needed Math.sqrt to save a little bit performance
								if(emergencyBeacons > 0){
									vehicle.getIdsProcessorList_().updateProcessor((ID_-1), curX_, curY_, curSpeed_, curLane_);
									vehicle.getKnownVehiclesList().updateVehicle(this, (ID_-1), curX_, curY_, curSpeed_, vehicle.getID(), false,false);
								}
								else if (emergencyBeacons == 0){
									//fake messages
									
									// find the destination for the message. Will be sent to the next junction behind us! (if its pcn we send it in front)
									boolean tmpDirection2 = curDirection_;
										
									Street tmpStreet2 = curStreet_;
									Street[] crossingStreets;
									Node tmpNode;
									int k1, l = 0, destX = -1, destY = -1;
									do{
										++l;
										if(tmpDirection2){
											tmpNode = tmpStreet2.getStartNode();
										} else {
											tmpNode = tmpStreet2.getEndNode();
										}
										if(tmpNode.getJunction() != null){
											destX = tmpNode.getX();
											destY = tmpNode.getY();
											break;
										}
										crossingStreets = tmpNode.getCrossingStreets();
										// find next street behind of us
										if(crossingStreets.length != 2){	// end of a street or some special case. don't forward any further
											destX = tmpNode.getX();
											destY = tmpNode.getY();
											break;
										}
										for(k1 = 0; k1 < crossingStreets.length; ++k1){
											if(crossingStreets[k1] != tmpStreet2){
												tmpStreet2 = crossingStreets[k1];
												if(tmpStreet2.getStartNode() == tmpNode) tmpDirection2 = false;
												else tmpDirection2 = true;
												break;
											}
										}
									} while(tmpStreet2 != curStreet_ && l < 10000);	//hard limit of 10000 nodes to maximally go back or if again arriving at source street (=>circle!)
									// found destination...now insert into messagequeue
									if(destX != -1 && destY != -1){
										int direction = -1;
										int time = Renderer.getInstance().getTimePassed();
										
										PenaltyMessage message = new PenaltyMessage(curX_, curY_, destX, destY, PENALTY_FAKE_MESSAGE_RADIUS, time + PENALTY_MESSAGE_VALID, curStreet_, curLane_, direction, PENALTY_MESSAGE_VALUE, time + PENALTY_VALID, true, (ID_-1), this,  "EVA_EMERGENCY_ID", true, true);
										message.setFloodingMode(true);	// enable flooding mode if within distance!				
										knownMessages_.addMessage(message, false, true);	
										
										emergencyBeacons = -1;								
									}		
									++fakeMessagesCreated_;
									
									fakeMessageCounter_ = fakeMessageCounter_%fakeMessageTypesCount;
								}
								
								
									
							
								
								vehicle.getKnownVehiclesList().updateVehicle(this, ID_, curX_, curY_, curSpeed_, vehicle.getID(), false,false);
								vehicle.getIdsProcessorList_().updateProcessor(ID_, curX_, curY_, curSpeed_, curLane_);
							}
						}
					}
				}
			}
 
			if(emergencyBeacons >= 0) emergencyBeacons--;
				/*
				if(emergencyBeacons == 0){
					PenaltyMessage message = new PenaltyMessage(curX_, curY_, destX, destY, PENALTY_FAKE_MESSAGE_RADIUS, time + PENALTY_MESSAGE_VALID, curStreet_, curLane_, direction, PENALTY_MESSAGE_VALUE, time + PENALTY_VALID, true, ID_, this,  messageType, true, true);
					message.setFloodingMode(false);	// enable flooding mode if within distance!				
					knownMessages_.addMessage(message, true, true);
				}
				
			}
			*/
			// allow beacon monitoring
			if(beaconMonitorEnabled_){
				if(curX_ >= beaconMonitorMinX_ && curX_ <= beaconMonitorMaxX_ && curY_ >= beaconMonitorMinY_ && curY_ <= beaconMonitorMaxY_){
					REPORT_PANEL.addBeacon(this, ID_, curX_, curY_, curSpeed_, false);
				}
			}
							
			
			if(logBeaconsAfterEvent_){
				amountOfLoggedBeacons_++;
				if(amountOfLoggedBeacons_ == 10){
					beaconString_ += "," + curX_ + "," + curY_ + "," + curSpeed_;
					if(!beaconString_.equals(","))GeneralLogWriter.log(beaconString_); 
					logBeaconsAfterEvent_ = false;
				}
				else{
					beaconString_ += "," + curX_ + "," + curY_ + "," + curSpeed_;
				}
			}
			
			// interception of Beacons by ARSUs
			AttackRSU[] tempARSUList = getArsuList();
			if(tempARSUList.length>0){
			    for(int l = 0; l < tempARSUList.length;l++) {
			    	dx = tempARSUList[l].getX() - curX_;
			    	dy = tempARSUList[l].getY() - curY_;
			    	
			   
					if((dx * dx + dy * dy) <= maxCommDistanceSquared){	// Pythagorean theorem: a^2 + b^2 = c^2 but without the needed Math.sqrt to save a little bit performance
						if(Renderer.getInstance().getAttackerVehicle() != null && !Renderer.getInstance().getAttackerVehicle().equals(this))Renderer.getInstance().getAttackerVehicle().getKnownVehiclesList().updateVehicle(this, ID_, curX_, curY_, curSpeed_, tempARSUList[l].getArsuID_(), false, true);
					}	    	
			      }

			}

			if(privacyDataLogged_ && (silentPeriodsOn || slowOn)){				
				savedBeacon2 = savedBeacon1;
				savedBeacon1 = Renderer.getInstance().getTimePassed() + ":Steady ID:" + this.steadyID_ + ":Pseudonym:" + Long.toHexString(this.ID_) + ":TraveledDistance:" + totalTravelDistance_ + ":TraveledTime:" + totalTravelTime_ + ":Node ID:None" + ":Direction:%0%aa%0%" + ":Street:" + this.getCurStreet().getName() + ":StreetSpeed:" + this.getCurStreet().getSpeed() + ":VehicleSpeed:" + this.getCurSpeed() +  ":x:" + this.curX_ + ":y:" + this.curY_;
			
				if(logNextBeacons == 1){
					logNextBeacons = 0;
					if(!slowOn || !vehicleJustStartedInSlow)PrivacyLogWriter.log(savedBeacon2.replace("%0%aa%0%", "OUT") + ":TimeInSlow:" +  (Renderer.getInstance().getTimePassed() - slowTimestamp) + "\n" + savedBeacon1.replace("%0%aa%0%", "OUT"));
					if(vehicleJustStartedInSlow) vehicleJustStartedInSlow = false;
				}
				else if(logNextBeacons == 2){
					logNextBeacons--;
				}
			}
		}
	}
	
	/**
	 * Find vehicles nearest in neighborhood and send encrypted beacons to them. Please check the following conditions before calling this function:
	 * <ul>
	 * <li>communication is generally enabled</li>
	 * <li>beacons are generally enabled</li>
	 * <li>if this vehicle is active</li>
	 * <li>if it has wifi</li> 
	 * <li>if the beacon countdown is 0 or less</li>
	 * <li>if vehicle is in a mix zone</li>
	 * <li>if encryptedBeaconsInMix is enabled</li>
	 * </ul>
	 */
	public void sendEncryptedBeacons(){
		if(!silentPeriod){
			beaconCountdown_ += beaconInterval_;
			
			RSU tmpRSU = null;

			if(curMixNode_.getEncryptedRSU_() != null){
				tmpRSU = curMixNode_.getEncryptedRSU_();
				tmpRSU.getKnownVehiclesList_().updateVehicle(this, ID_, curX_, curY_, curSpeed_, tmpRSU.getRSUID(), true, false);

				// allow beacon monitoring
				if(beaconMonitorEnabled_){
					if(curX_ >= beaconMonitorMinX_ && curX_ <= beaconMonitorMaxX_ && curY_ >= beaconMonitorMinY_ && curY_ <= beaconMonitorMaxY_){
						REPORT_PANEL.addBeacon(this, ID_, curX_, curY_, curSpeed_, true);
					}
				}

			}	
		}
		
		//check if the static flag is the same as the object flag. If not a silent period is beginning or ending -> log
		if(silent_period != silentPeriod){ 
			silentPeriod = silent_period;
			
			//silent period did begin -> log
			if(silentPeriod){
				if(privacyDataLogged_) PrivacyLogWriter.log(Renderer.getInstance().getTimePassed() + ":Steady ID:" + this.steadyID_ + ":Pseudonym:" + Long.toHexString(this.ID_) + ":TraveledDistance:" + totalTravelDistance_ + ":TraveledTime:" + totalTravelTime_ + ":Node ID:none" + ":Direction:IN" +  ":x:" + this.curX_ + ":y:" + this.curY_);
			}
			//silent perdiod did end -> log and change pseudonym
			else{
				ID_ = ownRandom_.nextLong();
				if(privacyDataLogged_) PrivacyLogWriter.log(Renderer.getInstance().getTimePassed() + ":Steady ID:" + this.steadyID_ + ":Pseudonym:" + Long.toHexString(this.ID_) + ":TraveledDistance:" + totalTravelDistance_ + ":TraveledTime:" + totalTravelTime_ + ":Node ID:none" + ":Direction:OUT" +  ":x:" + this.curX_ + ":y:" + this.curY_);
			}
		}
	}

	/**
	 * Move the vehicle one step forward. Please check if the vehicle is active before calling this!
	 * 
	 * @param timePerStep	the time per step in milliseconds
	 */
	public void move(int timePerStep){
		if(curWaitTime_ == 0 && curStreet_ != null){

			curLane_ = newLane_;
			curSpeed_ = newSpeed_;

			// ================================= 
			// Step 1: Move the vehicle according to its speed
			// ================================= 
			double tmpPosition, newPosition = curPosition_, movement;
			WayPoint nextTarget;
			movement = curSpeed_ * (timePerStep/1000.0);
			totalTravelTime_ += timePerStep;
			totalTravelDistance_ += movement;	// not totally precise when reaching destination but should be enough as long as timePerStep is less than a second...
			Street oldStreet = curStreet_;
			boolean oldDirection = curDirection_;
			while(movement > 0){
				if(curDirection_) tmpPosition = newPosition + movement;
				else tmpPosition = newPosition - movement;
				// no more routing points and on the street specified by the waypoint
				if(routePosition_ == routeStreets_.length-1 && destinations_.peekFirst().getStreet() == curStreet_){
					nextTarget = destinations_.peekFirst();	//doing this intentionally after the if! The peekFirst() is performed twice but in almost always the first one isn't even reached!
					if((curDirection_ && nextTarget.getPositionOnStreet() < tmpPosition) || (!curDirection_ && nextTarget.getPositionOnStreet() > tmpPosition)){		// gone over the position specified by the waypoint
						//we're on the last street of a routing but we still got more waypoints
						movement = tmpPosition - nextTarget.getPositionOnStreet();
						newPosition = nextTarget.getPositionOnStreet();
						do{
							destinations_.poll();
							if(destinations_.isEmpty()) break;
							curWaitTime_ = destinations_.peekFirst().getWaittime();
						} while(!calculateRoute(true, false));
						if(destinations_.isEmpty()){
							//if logging and slow is active write in privacy log to flag that the last slow won't be counted
							if(slowOn)PrivacyLogWriter.log("VehicleReachedDestination:" + this.steadyID_ + ":" + Long.toHexString(this.ID_));
							
							active_ = false;	//found no new destination where we can route to
							curWaitTime_ = Integer.MIN_VALUE;
							if(totalTravelTime_ >= minTravelTimeForRecycling_) mayBeRecycled_ = true;
							break;	
						} else brakeForDestinationCountdown_ = Integer.MAX_VALUE;
						if(curWaitTime_ > 0){
							curSpeed_ = 0;
							break;		//movement to next destination shall begin after some waiting on the current location
						} else brakeForDestination_ = false;		//stop braking for destination
					} else {
						newPosition = tmpPosition;
						movement = 0;
					}
				// leaving current street as movement is larger than the street length!
				} else if((curDirection_ && tmpPosition > curStreet_.getLength()) || (!curDirection_ && tmpPosition < 0)){
					if(curDirection_) movement = tmpPosition - curStreet_.getLength();
					else movement = -tmpPosition;
					++routePosition_;
					if(routePosition_ >= routeStreets_.length){	//no more routing entries
						//create a correct last position if routing to a next waypoint fails later
						if(curDirection_) newPosition = curStreet_.getLength();
						else newPosition = 0;
						do{
							destinations_.poll();
							if(destinations_.isEmpty()) break;
							curWaitTime_ = destinations_.peekFirst().getWaittime();
						} while(!calculateRoute(true, false));
						if(destinations_.isEmpty()){
							active_ = false;	//found no new destination where we can route to
							curWaitTime_ = Integer.MIN_VALUE;
							if(totalTravelTime_ >= minTravelTimeForRecycling_) mayBeRecycled_ = true;
							break;	
						} else brakeForDestinationCountdown_ = Integer.MAX_VALUE;
						if(curWaitTime_ > 0){
							curSpeed_ = 0;
							break;		//movement to next destination shall begin after some waiting on the current location
						} else brakeForDestination_ = false;	//stop braking for destination
					} 
					curDirection_ = routeDirections_[routePosition_];
					
					if(logJunctionFrequency_){
						//use this to log vehicles passing junctions. To count the different routes and their frequency.
						Node tmpNode = null;
						@SuppressWarnings("unused")
						int street1 = -1;
						@SuppressWarnings("unused")
						int street2 = -1;
	 					if(curDirection_ && routeStreets_[routePosition_].getStartNode().getCrossingStreetsCount() > 2){
	 						tmpNode = routeStreets_[routePosition_].getStartNode();
	 						for(int b = 0; b < tmpNode.getCrossingStreetsCount(); b++){
	 							if(tmpNode.getCrossingStreets()[b].equals(curStreet_)) street1 = b;
	 							if(tmpNode.getCrossingStreets()[b].equals(routeStreets_[routePosition_])) street2 = b;
	 						}
						}
						else if(!curDirection_ && routeStreets_[routePosition_].getEndNode().getCrossingStreetsCount() > 2) {
							tmpNode = routeStreets_[routePosition_].getEndNode();
	 						for(int b = 0; b < tmpNode.getCrossingStreetsCount(); b++){
	 							if(tmpNode.getCrossingStreets()[b].equals(curStreet_)) street1 = b;
	 							if(tmpNode.getCrossingStreets()[b].equals(routeStreets_[routePosition_])) street2 = b;
	 						}
						}
					}

					curStreet_ = routeStreets_[routePosition_];	
					
					if(curDirection_){						
						if(curStreet_.getStartNode() == junctionAllowed_){
							junctionAllowed_.getJunction().allowOtherVehicle();
							junctionAllowed_ = null;
						}
						newPosition = 0;
					} else {				
						if(curStreet_.getEndNode() == junctionAllowed_){
							junctionAllowed_.getJunction().allowOtherVehicle();
							junctionAllowed_ = null;
						}
						newPosition = curStreet_.getLength();
					}
				} else {
					newPosition = tmpPosition;
					movement = 0;
				}
			}
			if(!active_ || curWaitTime_ != 0) {
				oldStreet.delLaneObject(this, oldDirection);
				curPosition_ = newPosition;
			}
			else if(curStreet_ != oldStreet || curDirection_ != oldDirection){
				if(curStreet_.getLanesCount() < curLane_){
					curLane_ = curStreet_.getLanesCount();
					newLane_ = curLane_;
				}
				oldStreet.delLaneObject(this, oldDirection);
				curPosition_ = newPosition;
				curStreet_.addLaneObject(this, curDirection_);
			} else if (curLane_ > 1 || drivingOnTheSide_ || passingBlocking_){	// all vehicles which are on multilanes and which did not change street need to call the update method in the LaneContainer to preserve order!
				passingBlocking_ = false;
				curStreet_.updateLaneObject(this, curDirection_, newPosition);	// updates curPosition_ in the synchronized method!
			} else {
				curPosition_ = newPosition;
			}

			// ================================= 
			// Step 2: Recalculate values
			// ================================= 

			// recalculate position on map
			if(curStreet_ != null){
				calculatePosition();
			}

			// recalculate region
			if(curX_ < curRegion_.getLeftBoundary() || curX_ > curRegion_.getRightBoundary() || curY_ < curRegion_.getUpperBoundary() || curY_ > curRegion_.getLowerBoundary()){
				curRegion_.delVehicle(this);
				curRegion_ = MAP.getRegionOfPoint(curX_, curY_);
				curRegion_.addVehicle(this, false);
			}			
		}

	}

	
	/**
	 * Move Attacker. 
	 */
	public final void moveAttacker(){
		Vehicle tmpAttacked = Renderer.getInstance().getAttackedVehicle();
		//Save if attacker is in mix-zone
		if(isInMixZone_ && firstContact) attackerWasInMix = true;
		
		//If attacked vehicle drives in mix-zone set it null and save information that the attacked vehicle was in mix-zone
		if(tmpAttacked != null && tmpAttacked.isInMixZone_ && firstContact) {
			Renderer.getInstance().setAttackedVehicle(null);
			Vehicle.setAttackedVehicleID_(0);
			attackedWasInMix = true;
			newSpeed_ = curStreet_.getSpeed();
			//Make route to leave the mix-zone
			searchAttackedVehicle_();
		}
 
		//If attacker has left the mix-zone look for the attacked vehicle. Note that it is just plain guessing here.
		if(attackedWasInMix && attackerWasInMix && !isInMixZone_ && firstContact){
			if(getKnownVehiclesList().findNearestVehicle(0, 0, curX_, curY_, 10000000) != null){
				Vehicle.setAttackedVehicleID_(getKnownVehiclesList().findNearestVehicle(0, 0, curX_, curY_, 10000000).getID());
				Renderer.getInstance().setAttackedVehicle(getKnownVehiclesList().findNearestVehicle(0, 0, curX_, curY_, 10000000));
				attackedWasInMix = false;
			}
		}
		
		//Attacker knows attacked Vehicle: follow it
		if(attackedVehicleID_ != 0) {
			reRouteTime_--;
			if(reRouteTime_ < 0){
				reRouteTime_=ATTACKER_INTERVAL;
				long dx, dy, dg;
				KnownVehicle[] heads = knownVehiclesList_.getFirstKnownVehicle();
				KnownVehicle next;
				
				//traverse all vehicle which sent beacons
				for(int l = 0; l < heads.length; ++l){
					next = heads[l];								
					while(next != null){
						//Find the attacker data
						if(next.getVehicle().getID() == attackedVehicleID_){
							firstContact = true;
							
							dx = next.getVehicle().getX() - curX_;
							dy = next.getVehicle().getY() - curY_;
							dg = (dx * dx + dy * dy);
	
							//update speed if attacker is to near / to far
							if(dg > 60000000) newSpeed_ = maxSpeed_;
							else if(dg > 20000000 && dg < 60000000) newSpeed_ = Renderer.getInstance().getAttackedVehicle().getCurSpeed();
							else if(dg < 20000000) newSpeed_ = 0;
							
							//clear destinations and add new ones (only if the attacker is far enough away
							//from the attacked vehicle. Otherwise the attacker would reach the final destination
							//until it gets a new one)
							if(dg > 10000000){
							getDestinations().clear();
								try {
									getDestinations().add(new WayPoint(next.getX(),next.getY(),0));
									getDestinations().add(new WayPoint(next.getX(),next.getY(),0));
									calculateRoute(false, true);
									brakeForDestination_ = false;
									brakeForDestinationCountdown_ = 1000;
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						next = null;
						}
						else next = next.getNext();
					}
				}
			}
		}
	}


	/**
	 * The attacked vehicle can�t be found. But it can�t be far because we received a beacon the last time.
	 * This method is only evoked one time per mix-zone so it doesn't need to be that efficient
	 */
	public void searchAttackedVehicle_(){
		boolean NodeFound = false;
		Node tempNode = curStreet_.getStartNode();
		if(!curDirection_) tempNode = curStreet_.getEndNode();
		
		int guessX = curX_ - destinations_.peekFirst().getX();
		int guessY = curY_ - destinations_.peekFirst().getY();
		
		//Traverse the street nodes 
		//The vehicle try's to conserve the current vector
		for(int i = 0;i < tempNode.getCrossingStreets().length; i++){
			Node tempNode2;
			if(tempNode.equals(tempNode.getCrossingStreets()[i].getEndNode())) tempNode2 = tempNode.getCrossingStreets()[i].getStartNode();
			else tempNode2 = tempNode.getCrossingStreets()[i].getEndNode();
			
			if(guessX < 0 && guessY < 0) if(curX_ < tempNode2.getX() && curY_ < tempNode2.getY()) {
				NodeFound = true;
				tempNode = tempNode2;
				i=10;
			}
			else if(guessX < 0 && guessY > 0) if(curX_ < tempNode2.getX() && curY_ > tempNode2.getY() ) {
				NodeFound = true;
				tempNode = tempNode2;
				i=10;
			}
			else if(guessX > 0 && guessY > 0) if(curX_ > tempNode2.getX() && curY_ > tempNode2.getY() ) {
				NodeFound = true;
				tempNode = tempNode2;
				i=10;
			}
			else if(guessX > 0 && guessY < 0) if(curX_ > tempNode2.getX() && curY_ < tempNode2.getY() )  {
				NodeFound = true;
				tempNode = tempNode2;
				i=10;
			}
		}
		
		//if it can't be found (e.g. because the street changes direction) we look for a node that isn't as perfect but still works
		if(!NodeFound){
			for(int i = 0;i < tempNode.getCrossingStreets().length; i++){
				Node tempNode2;
				if(tempNode.equals(tempNode.getCrossingStreets()[i].getEndNode())) tempNode2 = tempNode.getCrossingStreets()[i].getStartNode();
				else tempNode2 = tempNode.getCrossingStreets()[i].getEndNode();
				
				if(Math.abs(guessX) > Math.abs(guessY)){
					if(guessX < 0){
						if(curX_  < tempNode2.getX()) {
							tempNode = tempNode2;
							i=10;
						}
					}
					else if(guessX > 0){
						if(curX_  > tempNode2.getX()) {
							tempNode = tempNode2;
							i=10;
						}
					}
				}
				else{
					if(guessY < 0){
						if(curY_ < tempNode2.getY()){
							tempNode = tempNode2;
							i=10;				
						}
					}
					else if(guessY > 0){
						if(curY_ > tempNode2.getY()) {
							tempNode = tempNode2;
							i=10;				
						}
					}
				}
			}
		}
		
		//we search for 30 more nodes to make sure to drive longer than the mix zone. This is not really performant but this function is only evoked on time per mix-zone
		for(int j = 0;j < 30;j++){
			NodeFound = false;

			for(int i = 0;i < tempNode.getCrossingStreets().length; i++){
				Node tempNode2;
				if(tempNode.equals(tempNode.getCrossingStreets()[i].getEndNode())) tempNode2 = tempNode.getCrossingStreets()[i].getStartNode();
				else tempNode2 = tempNode.getCrossingStreets()[i].getEndNode();
				
				if(guessX < 0 && guessY < 0) if(tempNode.getX() < tempNode2.getX() && tempNode.getY() < tempNode2.getY()) {
					NodeFound = true;
					tempNode = tempNode2;
					i=10;
				}
				else if(guessX < 0 && guessY > 0) if(tempNode.getX() < tempNode2.getX() && tempNode.getY() > tempNode2.getY() ) {
					NodeFound = true;
					tempNode = tempNode2;
					i=10;
				}
				else if(guessX > 0 && guessY > 0) if(tempNode.getX() > tempNode2.getX() && tempNode.getY() > tempNode2.getY() ) {
					NodeFound = true;
					tempNode = tempNode2;
					i=10;
				}
				else if(guessX > 0 && guessY < 0) if(tempNode.getX() > tempNode2.getX() && tempNode.getY() < tempNode2.getY() )  {
					NodeFound = true;
					tempNode = tempNode2;
					i=10;
				}
			}
			
			if(!NodeFound){
				for(int i = 0;i < tempNode.getCrossingStreets().length; i++){
					Node tempNode2;
					if(tempNode.equals(tempNode.getCrossingStreets()[i].getEndNode())) tempNode2 = tempNode.getCrossingStreets()[i].getStartNode();
					else tempNode2 = tempNode.getCrossingStreets()[i].getEndNode();
					
					if(Math.abs(guessX) > Math.abs(guessY)){
						if(guessX < 0){
							if(tempNode.getX()  < tempNode2.getX()) {
								tempNode = tempNode2;
								i=10;
							}
						}
						else if(guessX > 0){
							if(tempNode.getX()  > tempNode2.getX()) {
								tempNode = tempNode2;
								i=10;
							}
						}
					}
					else{
						if(guessY < 0){
							if(tempNode.getY() < tempNode2.getY()){
								tempNode = tempNode2;
								i=10;				
							}
						}
						else if(guessY > 0){
							if(tempNode.getY() > tempNode2.getY()) {
								tempNode = tempNode2;
								i=10;				
							}
						}
					}
				}
				}
		}

				
		//clear the old destinations and save the new ones.
		getDestinations().clear();
		try {
			getDestinations().add(new WayPoint(tempNode.getX(),tempNode.getY(),0));
			getDestinations().add(new WayPoint(tempNode.getX(),tempNode.getY(),0));
			calculateRoute(false, true);
			brakeForDestination_ = false;
			brakeForDestinationCountdown_ = 1000;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Resets this vehicle so that it can be reused. It will travel on the same route as last time!
	 */
	public void reset(){
		
		//reset countdowns and other variables
		ID_ = ownRandom_.nextLong();
		steadyID_ = steadyIDCounter++;
		curSpeed_ = brakingRate_/2;
		newSpeed_ = curSpeed_;
		totalTravelTime_ = 0;
		totalTravelDistance_ = 0;
		newLane_ = 1;
		active_ = false;
		speedAtLastBrakingDistanceCalculation_ = 0;
		isInMixZone_ = false;
		junctionAllowed_ = null;			
		brakeForDestination_ = false;
		brakeForDestinationCountdown_ = Integer.MAX_VALUE;
		destinationCheckCountdown_ = 0;
		laneChangeCountdown = 0;
		communicationCountdown_ = 0;
		knownVehiclesTimeoutCountdown_ = 0;
		knownPenaltiesTimeoutCountdown_ = 0;
		beaconCountdown_ = (int)Math.round(curPosition_)%beaconInterval_;
		communicationCountdown_ = (int)Math.round(curPosition_)%communicationInterval_;
		mixCheckCountdown_ = (int)Math.round(curPosition_)%MIX_CHECK_INTERVAL;
		emergencyBrakingCountdown_ = ownRandom_.nextInt(emergencyBrakingInterval_)+1;
		lastRHCNMessageCreated = 0;		
		lastPCNMessageCreated = 0;
		lastPCNFORWARDMessageCreated = 0;
		lastEVAMessageCreated = 0;

		stopTime_ = 0;
		passingBlocking_ = false;
		//don't set them 0, because we wan't the count of all messages created (included the deleted ones)

		IDsChanged_ = 0;
		
		//slow model
		isInSlow = false;
		changedPseudonymInSlow = false;
		slowBeaconsLogged = false;
		vehicleJustStartedInSlow = true;
		
		//reset communication info
		knownVehiclesList_.clear();
		knownPenalties_.clear();
		knownMessages_.clear();
		idsProcessorList_.clear();
		
		//reset RSU infos
		knownRSUsList_.clear();
		knownRSUsTimeoutCountdown_ = 0;
		
		speedFluctuationCountdown_ = (int)Math.round(curPosition_)%SPEED_FLUCTUATION_CHECKINTERVAL;
		
		//reset known event sources
		knownEventSourcesList_.clear();
		
		// reset position
		curX_ = startingWayPoint_.getX();
		curY_ = startingWayPoint_.getY();
		curPosition_ = startingWayPoint_.getPositionOnStreet();
		curStreet_ = startingWayPoint_.getStreet();
		curWaitTime_ = startingWayPoint_.getWaittime();

		// recalculate routing information
		destinations_ = originalDestinations_.clone();
		if(curStreet_.isOneway()){
			while(!destinations_.isEmpty() && (!calculateRoute(false, false) || destinations_.peekFirst().getStreet() == curStreet_)){
				curWaitTime_ = destinations_.pollFirst().getWaittime();
			}
		} else {
			while(!destinations_.isEmpty() && (!calculateRoute(false, false) || destinations_.peekFirst().getStreet() == curStreet_)){
				curWaitTime_ = destinations_.pollFirst().getWaittime();
			}
		}
		if(curWaitTime_ == 0){
			active_ = true;
			curStreet_.addLaneObject(this, curDirection_);
		}
		calculatePosition();
		
		//reset region
		curRegion_.delVehicle(this);
		curRegion_ = MAP.getRegionOfPoint(curX_, curY_);
		curRegion_.addVehicle(this, false);
		
		mayBeRecycled_ = false;
	}
	
	/**
	 * Resets the global random number generator
	 */
	public static void resetGlobalRandomGenerator(){
		RANDOM.setSeed(1L);
	}
	

	/**
	 * Gets a substring of the Vehicle ID. Used in the EditOneVehicleControl.java chooseVehicle_ JCombobox.
	 */
	public String toString(){
		return Long.toHexString(ID_).substring(0,5);		
	}

	
	/**
	 * Indicates if this vehicle may be reset and reused.
	 * 
	 * @return	<code>true</code> if it may be reused, else <code>false</code>
	 */
	public boolean getMayBeRecycled(){
		return mayBeRecycled_;
	}

	/**
	 * Gets an array with all streets which will be visited until arriving at the next destination.
	 * 
	 * @return the array with all streets
	 */
	public Street[] getRouteStreets(){
		return routeStreets_;
	}

	/**
	 * Get the directions corresponding to the array returned <code>getRouteStreets()</code>.
	 * 
	 * @return the array with all directions
	 */
	public boolean[] getRouteDirections(){
		return routeDirections_;
	}

	/**
	 * Gets the current position in the array returned by <code>getRouteStreets()</code> and <code>getRouteDirections()</code>.
	 * 
	 * @return the position
	 */
	public int getRoutePosition(){
		return routePosition_;
	}


	/**
	 * Gets the current ID of this vehicle. This ID might change if mixing is enabled!
	 * 
	 * @return the ID
	 */
	public int getVehicleID(){
		return 0;
	}

	/**
	 * Gets the current communication countdown
	 * 
	 * @return the communication countdown
	 */
	public int getCommunicationCountdown(){
		return communicationCountdown_;
	}

	/**
	 * Gets the current beacon countdown
	 * 
	 * @return the beacon countdown
	 */
	public int getBeaconCountdown(){
		return beaconCountdown_;
	}

	/**
	 * Gets the starting point of the vehicle.
	 * 
	 * @return the <code>WayPoint</code>
	 */
	public WayPoint getStartPoint() {
		return startingWayPoint_;
	}

	/**
	 * Gets the regions x-coordinate in which this vehicle is found.
	 * 
	 * @return An Integer representing the x-coordinate of the Region
	 */
	public int getRegionX() {
		return curRegion_.getX();
	}

	/**
	 * Gets the regions y-coordinate in which this vehicle is found.
	 * 
	 * @return An Integer representing the y-coordinate of the Region
	 */
	public int getRegionY() {
		return curRegion_.getY();
	}

	/**
	 * Gets the maximum speed of this vehicle.
	 * 
	 * @return the maximum speed in cm/s
	 */
	public int getMaxSpeed(){
		return maxSpeed_;
	}

	/**
	 * Gets the current waittime.
	 * 
	 * @return the current waittime in milliseconds
	 */
	public int getWaittime(){
		if(curWaitTime_ < 0) return 0; 
		else return curWaitTime_;
	}

	/**
	 * Gets the destinations of this vehicle.
	 * 
	 * @return the <code>ArrayDeque</code> with all destinations
	 */
	public ArrayDeque<WayPoint> getDestinations(){
		return destinations_;
	}

	/**
	 * Gets the maximum communication distance of this vehicle.
	 * 
	 * @return the distance in cm
	 */
	public int getMaxCommDistance(){
		return maxCommDistance_;
	}

	/**
	 * Returns if this vehicle is currently active.
	 * 
	 * @return <code>true</code> if it's active
	 */
	public boolean isActive(){
		return active_;
	}

	/**
	 * Returns if this vehicle is currently in a mix zone.
	 * 
	 * @return <code>true</code> if it's in a mix zone
	 */
	public boolean isInMixZone(){
		return isInMixZone_;
	}

	/**
	 * Returns if this vehicle has WiFi functionality.
	 * 
	 * @return <code>true</code> if it has WiFi
	 */
	public boolean isWiFiEnabled(){
		return wiFiEnabled_;
	}

	/**
	 * Gets the special data structure with all known messages.
	 * 
	 * @return the data structure
	 */
	public KnownMessages getKnownMessages(){
		return knownMessages_;
	}
	
	/**
	 * Gets the special data structure with all known penalties.
	 * 
	 * @return the data structure
	 */
	public KnownPenalties getKnownPenalties(){
		return knownPenalties_;
	}

	/**
	 * Gets the special data structure with all known vehicles.
	 * 
	 * @return the data structure
	 */
	public KnownVehiclesList getKnownVehiclesList(){
		return knownVehiclesList_;
	}
	
	/**
	 * Gets the special data structure with all known RSUs.
	 * 
	 * @return the data structure
	 */
	public KnownRSUsList getKnownRSUsList(){
		return knownRSUsList_;
	}
	

	/**
	 * Returns how often this vehicle has changed it's ID (excluding the initial ID).
	 * 
	 * @return the amount
	 */
	public int getIDsChanged(){
		return IDsChanged_;
	}

	/**
	 * Gets how long this vehicle traveled. This excludes predefined waiting times but includes
	 * all other stops.
	 * 
	 * @return the total time in milliseconds
	 */
	public int getTotalTravelTime(){
		return totalTravelTime_;
	}

	/**
	 * The total distance traveled. This is not completely exact but should suffice in most cases.
	 * Small aberration from the real value occur if this vehicle reaches a destination (which should 
	 * not happen too often). 
	 * 
	 * @return the total distance in cm
	 */
	public long getTotalTravelDistance(){
		return totalTravelDistance_;
	}

	/**
	 * Sets the region in which this vehicle is found.
	 * 
	 * @param region	the region
	 */
	public void setRegion(Region region) {
		curRegion_ = region;
	}

	/**
	 * Gets the ID used in beacons encoded in HEX so that it's shorter. If the vehicle is not wifi
	 * enabled, brackets are used to indicate this.
	 * 
	 * @return the ID as an hex string
	 */
	public String getHexID(){
		if(wiFiEnabled_) return Long.toHexString(ID_);
		else return "(" + Long.toHexString(ID_) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Returns the interval between messages.
	 * 
	 * @return the interval in milliseconds
	 */
	public static int getCommunicationInterval(){
		return communicationInterval_;
	}

	/**
	 * Returns the interval between beacons.
	 * 
	 * @return the interval in milliseconds
	 */
	public static int getBeaconInterval(){
		return beaconInterval_;
	}


	/**
	 * Signals if communication is enabled.
	 * 
	 * @return	<code>true</code> if communication is enabled, else <code>false</code>
	 */
	public static boolean getCommunicationEnabled(){
		return communicationEnabled_;
	}
	
	/**
	 * Signals if recycling of vehicles is enabled or not
	 * 
	 * @return	<code>true</code> if recycling is enabled, else <code>false</code>
	 */
	public static boolean getRecyclingEnabled(){
		return recyclingEnabled_;
	}

	/**
	 * Signals if beacons are enabled.
	 * 
	 * @return <code>true</code> if beacons are enabled, else <code>false</code>
	 */
	public static boolean getBeaconsEnabled(){
		return beaconsEnabled_;
	}
	
	/**
	 * Signals if mix zones are enabled.
	 * 
	 * @return <code>true</code> if mix zones are enabled, else <code>false</code>
	 */
	public static boolean getMixZonesEnabled(){
		return mixZonesEnabled_;
	}
	
	/**
	 * If the fallback mode shall be enabled in mix zones. This fallback mode enables the beaconless
	 * communication inside mix zones.
	 * 
	 * @return <code>true</code> if the fallback mode is enabled, else <code>false</code>
	 */
	public static boolean getMixZonesFallbackEnabled(){
		return mixZonesFallbackEnabled_;
	}
	
	/**
	 * If the fallback mode only sends messages which are in flooding/broadcast mode.
	 * 
	 * @return <code>true</code> if only flooding messages are sent, else <code>false</code>
	 */
	public static boolean getMixZonesFallbackFloodingOnly(){
		return mixZonesFallbackFloodingOnly_;
	}

	/**
	 * Returns the current routing mode.
	 * 
	 * @return the routing mode
	 */
	public static int getRoutingMode(){
		return routingMode_;
	}

	/**
	 * Returns the maximum communication distance.
	 * 
	 * @return the maximum communication distance in cm
	 */
	public static int getMaximumCommunicationDistance(){
		return maximumCommunicationDistance_;
	}
	
	/**
	 * Gets the minimum time a vehicle needs to have traveled in order to be able to be recycled. Vehicles
	 * which travel shorter than this time will NOT get recycled. 
	 * 
	 * @return the time in milliseconds
	 */
	public static int getMinTravelTimeForRecycling(){
		return minTravelTimeForRecycling_;
	}

	/**
	 * Returns the radius of the mix zones.
	 * 
	 * @return the mix zone radius in cm
	 */
	public static int getMixZoneRadius(){
		return mixZoneRadius_;
	}

	/**
	 * Set the maximum radius of the mix zones.
	 * 
	 * @param maxMixZoneRadius	the maximum radius of the mix zones in cm
	 */
	public static void setMaxMixZoneRadius(int maxMixZoneRadius) {
		maxMixZoneRadius_ = maxMixZoneRadius;
	}

	/**
	 * Gets the maximum mix zone radius used in the scenario.
	 * 
	 * @return maxMixZoneRadius_ the maximum mix zone radius in cm
	 */
	public static int getMaxMixZoneRadius() {
		return maxMixZoneRadius_;
	}


	/**
	 * Set the default radius of the mix zones (in the common settings panel).
	 * 
	 * @param mixZoneRadius	the radius of the mix zones in cm
	 */
	public static void setMixZoneRadius(int mixZoneRadius){
		mixZoneRadius_ = mixZoneRadius;
	}
	
	/**
	 * Sets the minimum time a vehicle needs to have traveled in order to be able to be recycled. Vehicles
	 * which travel shorter than this time will NOT get recycled. 
	 * 
	 * @param minTravelTimeForRecycling	the time in milliseconds
	 */
	public static void setMinTravelTimeForRecycling(int minTravelTimeForRecycling){
		minTravelTimeForRecycling_ = minTravelTimeForRecycling;
	}

	/**
	 * Set the maximum communication distance.
	 * 
	 * @param maximumCommunicationDistance	the maximum communication distance in cm
	 */
	public static void setMaximumCommunicationDistance(int maximumCommunicationDistance){
		maximumCommunicationDistance_ = maximumCommunicationDistance;
	}

	/**
	 * Sets the reference to all regions. Call this on map reload!
	 * 
	 * @param regions	the array with all regions
	 */
	public static void setRegions(Region[][] regions){
		regions_ = regions;
	}

	/**
	 * Sets a new value for the communication interval. Common to all vehicles.
	 * 
	 * @param communicationInterval	the new value 
	 */
	public static void setCommunicationInterval(int communicationInterval){
		communicationInterval_ = communicationInterval;
	}

	/**
	 * Sets a new value for the beacon interval. Common to all vehicles.
	 * 
	 * @param beaconInterval	the new value 
	 */
	public static void setBeaconInterval(int beaconInterval){
		beaconInterval_ = beaconInterval;
	}

	/**
	 * Sets if communication is enabled or not. Common to all vehicles.
	 * 
	 * @param state	<code>true</code> to enable communication, else <code>false</code> 
	 */
	public static void setCommunicationEnabled(boolean state){
		RSU.setCommunicationEnabled(state);
		communicationEnabled_ = state;
	}
	
	/**
	 * Sets if recycling of vehicles is enabled or not. Common to all vehicles.
	 * 
	 * @param state	<code>true</code> to enable recycling, else <code>false</code> 
	 */
	public static void setRecyclingEnabled(boolean state){
		recyclingEnabled_ = state;
	}

	/**
	 * Sets if beacons are enabled or not. Common to all vehicles.
	 * 
	 * @param state	<code>true</code> to enable beacons, else <code>false</code> 
	 */
	public static void setBeaconsEnabled(boolean state){
		RSU.setBeaconsEnabled(state);
		beaconsEnabled_ = state;
	}

	/**
	 * Sets if mix zones are enabled or not. Common to all vehicles.
	 * 
	 * @param state	<code>true</code> to enable mix zones, else <code>false</code> 
	 */
	public static void setMixZonesEnabled(boolean state){
		mixZonesEnabled_ = state;
	}
	
	/**
	 * Sets if the fallback mode shall be enabled in mix zones. This fallback mode enables the beaconless
	 * communication inside mix zones.
	 * 
	 * @param state	<code>true</code> if the fallback mode is enabled, else <code>false</code>
	 */
	public static void setMixZonesFallbackEnabled(boolean state){
		mixZonesFallbackEnabled_ = state;
	}
	
	/**
	 * Sets if the fallback mode only sends messages which are in flooding/broadcast mode.
	 * 
	 * @param state	<code>true</code> if only flooding messages are sent, else <code>false</code>
	 */
	public static void setMixZonesFallbackFloodingOnly(boolean state){
		mixZonesFallbackFloodingOnly_ = state;
	}
	
	/**
	 * Sets if beacon zones should be monitored or not. Common to all vehicles.
	 * 
	 * @param beaconMonitorEnabled	<code>true</code> to enable monitoring mix zones, else <code>false</code> 
	 */
	public static void setBeaconMonitorZoneEnabled(boolean beaconMonitorEnabled){
		beaconMonitorEnabled_ = beaconMonitorEnabled;
		RSU.setBeaconMonitorZoneEnabled(beaconMonitorEnabled);
	}
	
	/**
	 * Gets beacon monitor status
	 * 
	 * @return beaconMonitorEnabled_ <code>true</code> if beacon monitor is enabled
	 */
	public static boolean getbeaconMonitorEnabled() {
		return beaconMonitorEnabled_;
	}
	
	/**
	 * Sets the values for the monitored beacon zone. A rectangular bounding box within the specified coordinates
	 * is monitored if {@link #setBeaconMonitorZoneEnabled(boolean)} is set to <code>true</code>.
	 * 
	 * @param beaconMonitorMinX	the minimum x coordinate
	 * @param beaconMonitorMaxX	the maximum x coordinate
	 * @param beaconMonitorMinY	the minimum y coordinate
	 * @param beaconMonitorMaxY	the maximum y coordinate
	 */
	public static void setMonitoredMixZoneVariables(int beaconMonitorMinX, int beaconMonitorMaxX, int beaconMonitorMinY, int beaconMonitorMaxY){
		beaconMonitorMinX_ = beaconMonitorMinX;
		beaconMonitorMaxX_ = beaconMonitorMaxX;
		beaconMonitorMinY_ = beaconMonitorMinY;
		beaconMonitorMaxY_ = beaconMonitorMaxY;		
		RSU.setMonitoredMixZoneVariables(beaconMonitorMinX,beaconMonitorMaxX,beaconMonitorMinY,beaconMonitorMaxY);
	}
	
	/**
	 * Gets beacon monitor minX coordinate
	 * 
	 * @return beaconMonitorMinX_ the minX coordinate of the beacon monitor window
	 */
	public static int getbeaconMonitorMinX() {
		return beaconMonitorMinX_;
	}
	
	/**
	 * Gets beacon monitor maxX coordinate
	 * 
	 * @return beaconMonitorMaxX_ the maxX coordinate of the beacon monitor window
	 */
	public static int getbeaconMonitorMaxX() {
		return beaconMonitorMaxX_;
	}
	
	/**
	 * Gets beacon monitor minY coordinate
	 * 
	 * @return beaconMonitorMinY_ the minY coordinate of the beacon monitor window
	 */
	public static int getbeaconMonitorMinY() {
		return beaconMonitorMinX_;
	}
	
	/**
	 * Gets beacon monitor maxY coordinate
	 * 
	 * @return beaconMonitorMaxY_ the maxY coordinate of the beacon monitor window
	 */
	public static int getbeaconMonitorMaxY() {
		return beaconMonitorMaxY_;
	}

	/**
	 * Gets the report panel for beacon monitoring
	 * 
	 * @return REPORT_PANEL the beacon report panel
	 */
	public static ReportingControlPanel getREPORT_PANEL() {
		return REPORT_PANEL;
	}
	/**
	 * Sets a new routing mode. See the A_Star_Algor for details. Common to all vehicles.
	 * 
	 * @param mode	the new routing mode
	 */
	public static void setRoutingMode(int mode){
		routingMode_ = mode;
	}

	/**
	 * Gets the vehicle ID
	 * 
	 * @return ID_ the vehicle ID
	 */
	public long getID() {
		return ID_;
	}

	/**
	 * Set vehicle WiFi
	 * 
	 * @param wiFiEnabled <code>true</code> to enable WiFi for the vehicle
	 */
	public void setWiFiEnabled(boolean wiFiEnabled) {
		wiFiEnabled_ = wiFiEnabled;
	}


	/**
	 * Sets the maximum speed
	 * 
	 * @param maxSpeed the speed in cm/s
	 */
	public void setMaxSpeed(int maxSpeed) {
		maxSpeed_ = maxSpeed;
	}

	/**
	 * Sets the maximum communication distance
	 * 
	 * @param maxCommDistance the maximum communication distance in cm
	 */
	public void setMaxCommDistance(int maxCommDistance) {
		maxCommDistance_ = maxCommDistance;
	}

	/**
	 * Sets the current wait time.
	 * 
	 * @param curWaitTime the current wait time in ms.
	 */
	public void setCurWaitTime(int curWaitTime) {
		curWaitTime_ = curWaitTime;
	}

	/**
	 * Gets the current wait time
	 * 
	 * @return curWaitTime_ the current wait time
	 */
	public int getCurWaitTime() {
		return curWaitTime_;
	}

	/**
	 * Sets the color
	 * 
	 * @param color the new color
	 */
	public void setColor(Color color) {
		color_ = color;
	}

	/**
	 * Gets the vehicle color.
	 * 
	 * @return color_ the vehicle color.
	 */
	public Color getColor() {
		return color_;
	}

	/**
	 * Sets the braking rate
	 * 
	 * @param brakingRate the braking rate in cm/s^2
	 */
	public void setBrakingRate(int brakingRate) {
		if(brakingRate <= 0) brakingRate_ = 300;
		else brakingRate_ = brakingRate;	
	}

	/**
	 * Gets the braking rate
	 * 
	 * @return brakingRate_ the braking rate in cm/s^2
	 */
	public int getBrakingRate() {
		return brakingRate_;
	}

	/**
	 * Sets the acceleration rate
	 * 
	 * @param accelerationRate the acceleration rate in cm/s^2
	 */
	public void setAccelerationRate(int accelerationRate) {
		if(accelerationRate <= 0) accelerationRate_ = 800;
		else accelerationRate_ = accelerationRate;	
	}

	/**
	 * Gets the acceleration rate
	 * 
	 * @return accelerationRate_ the acceleration rate in cm/s^2
	 */
	public int getAccelerationRate() {
		return accelerationRate_;
	}

	/**
	 * Sets the emergency vehicle mode
	 * 
	 * @param emergencyVehicle <code>true</code> to enable emergency mode
	 */
	public void setEmergencyVehicle(boolean emergencyVehicle) {
		emergencyVehicle_ = emergencyVehicle;
	}

	/**
	 * Signals if vehicle is an emergency vehicle
	 * 
	 * @return <code>true</code> if vehicle is an emergency vehicle
	 */
	public boolean isEmergencyVehicle() {
		return emergencyVehicle_;
	}

	/**
	 * Sets the vehicle length
	 * 
	 * @param vehicleLength the vehicle length in cm.
	 */ 
	public void setVehicleLength(int vehicleLength) {
		vehicleLength_ = vehicleLength;
	}

	/**
	 * Gets the vehicle length.
	 * 
	 * @return vehicleLength_ the vehicle length in cm.
	 */
	public int getVehicleLength() {
		return vehicleLength_;
	}


	public static AttackRSU[] getArsuList() {
		return arsuList;
	}


	public static void setArsuList(AttackRSU[] arsuList) {
		Vehicle.arsuList = arsuList;
	}


	public static boolean isAttackerDataLogged_() {
		return attackerDataLogged_;
	}


	public static void setAttackerDataLogged_(boolean attackerDataLogged_) {
		Vehicle.attackerDataLogged_ = attackerDataLogged_;
	}


	public static long getAttackedVehicleID_() {
		return attackedVehicleID_;
	}


	public static void setAttackedVehicleID_(long attackedVehicleID_) {
		Vehicle.attackedVehicleID_ = attackedVehicleID_;
	}


	public static boolean isEncryptedBeaconsInMix_() {
		return encryptedBeaconsInMix_;
	}


	public static void setEncryptedBeaconsInMix_(boolean encryptedBeaconsInMix_) {
		Vehicle.encryptedBeaconsInMix_ = encryptedBeaconsInMix_;
	}


	public static boolean isAttackerEncryptedDataLogged_() {
		return attackerEncryptedDataLogged_;
	}


	public static void setAttackerEncryptedDataLogged_(
			boolean attackerEncryptedDataLogged_) {
		Vehicle.attackerEncryptedDataLogged_ = attackerEncryptedDataLogged_;
	}


	public Node getCurMixNode_() {
		return curMixNode_;
	}


	public void setCurMixNode_(Node curMixNode_) {
		this.curMixNode_ = curMixNode_;
	}

	public static ReportingControlPanel getReportingPanel(){
		if(Renderer.getInstance().isConsoleStart()) return null;
		else return VanetSimStart.getMainControlPanel().getReportingPanel();
	}


	/**
	 * @return the waitingForSignal_
	 */
	public boolean isWaitingForSignal_() {
		return waitingForSignal_;
	}


	/**
	 * @param waitingForSignal_ the waitingForSignal_ to set
	 */
	public void setWaitingForSignal_(boolean waitingForSignal_) {
		this.waitingForSignal_ = waitingForSignal_;
	}


	public static boolean isPrivacyDataLogged_() {
		return privacyDataLogged_;
	}


	public static void setPrivacyDataLogged_(boolean privacyDataLogged_) {
		Vehicle.privacyDataLogged_ = privacyDataLogged_;
	}

	public void setTimeDistance(int timeDistance) {
		timeDistance_ = timeDistance;
	}


	public int getTimeDistance() {
		return timeDistance_;
	}

	public void setPoliteness(int politeness_) {
		this.politeness_ = politeness_;
	}


	public int getPoliteness() {
		return politeness_;
	}

	public static int getTIME_BETWEEN_SILENT_PERIODS() {
		return TIME_BETWEEN_SILENT_PERIODS;
	}

	public static void setTIME_BETWEEN_SILENT_PERIODS(int i){
		TIME_BETWEEN_SILENT_PERIODS = i;
	}

	public static int getTIME_OF_SILENT_PERIODS() {
		return TIME_OF_SILENT_PERIODS;
	}

	public static void setTIME_OF_SILENT_PERIODS(int i){
		TIME_OF_SILENT_PERIODS = i;
	}

	public static boolean isSilent_period() {
		return silent_period;
	}


	public static void setSilent_period(boolean silent_period) {
		Vehicle.silent_period = silent_period;
	}


	public static boolean isSilentPeriodsOn() {
		return silentPeriodsOn;
	}


	public static void setSilentPeriodsOn(boolean silentPeriodsOn) {
		Vehicle.silentPeriodsOn = silentPeriodsOn;
	}


	/**
	 * @return the tIME_TO_PSEUDONYM_CHANGE
	 */
	public static int getTIME_TO_PSEUDONYM_CHANGE() {
		return TIME_TO_PSEUDONYM_CHANGE;
	}


	/**
	 * @param tIME_TO_PSEUDONYM_CHANGE the tIME_TO_PSEUDONYM_CHANGE to set
	 */
	public static void setTIME_TO_PSEUDONYM_CHANGE(int tIME_TO_PSEUDONYM_CHANGE) {
		TIME_TO_PSEUDONYM_CHANGE = tIME_TO_PSEUDONYM_CHANGE;
	}


	/**
	 * @return the sLOW_SPEED_LIMIT
	 */
	public static int getSLOW_SPEED_LIMIT() {
		return SLOW_SPEED_LIMIT;
	}


	/**
	 * @param sLOW_SPEED_LIMIT the sLOW_SPEED_LIMIT to set
	 */
	public static void setSLOW_SPEED_LIMIT(int sLOW_SPEED_LIMIT) {
		SLOW_SPEED_LIMIT = sLOW_SPEED_LIMIT;
	}


	/**
	 * @return the slowOn
	 */
	public static boolean isSlowOn() {
		return slowOn;
	}


	/**
	 * @param slowOn the slowOn to set
	 */
	public static void setSlowOn(boolean slowOn) {
		Vehicle.slowOn = slowOn;
	}


	/**
	 * @return the isInSlow
	 */
	public boolean isInSlow() {
		return isInSlow;
	}


	/**
	 * @param isInSlow the isInSlow to set
	 */
	public void setInSlow(boolean isInSlow) {
		this.isInSlow = isInSlow;
	}


	/**
	 * @return the changedPseudonymInSlow
	 */
	public boolean isChangedPseudonymInSlow() {
		return changedPseudonymInSlow;
	}


	/**
	 * @param changedPseudonymInSlow the changedPseudonymInSlow to set
	 */
	public void setChangedPseudonymInSlow(boolean changedPseudonymInSlow) {
		this.changedPseudonymInSlow = changedPseudonymInSlow;
	}


	/**
	 * @return the slowTimestamp
	 */
	public int getSlowTimestamp() {
		return slowTimestamp;
	}


	/**
	 * @param slowTimestamp the slowTimestamp to set
	 */
	public void setSlowTimestamp(int slowTimestamp) {
		this.slowTimestamp = slowTimestamp;
	}


	/**
	 * @return the speedDeviation_
	 */
	public int getSpeedDeviation_() {
		return speedDeviation_;
	}


	/**
	 * @param speedDeviation_ the speedDeviation_ to set
	 */
	public void setSpeedDeviation_(int speedDeviation_) {
		this.speedDeviation_ = speedDeviation_;
	}


	/**
	 * @return the idsActivated
	 */
	public static boolean isIdsActivated() {
		return idsActivated;
	}


	/**
	 * @param idsActivated the idsActivated to set
	 */
	public static void setIdsActivated(boolean idsActivated) {
		Vehicle.idsActivated = idsActivated;
	}


	/**
	 * @return the moveOutOfTheWay
	 */
	public boolean isMoveOutOfTheWay_() {
		return moveOutOfTheWay_;
	}


	/**
	 * @param moveOutOfTheWay the moveOutOfTheWay to set
	 */
	public void setMoveOutOfTheWay_(boolean moveOutOfTheWay) {
		moveOutOfTheWay_ = moveOutOfTheWay;
	}


	/**
	 * @return the fakingMessages_
	 */
	public boolean isFakingMessages() {
		return fakingMessages_;
	}


	/**
	 * @param fakingMessages_ the fakingMessages_ to set
	 */
	public void setFakingMessages(boolean fakingMessages) {
		fakingMessages_ = fakingMessages;
	}


	/**
	 * @return the fakeMessageType
	 */
	public String getFakeMessageType() {
		return fakeMessageType_;
	}


	/**
	 * @param fakeMessageType the fakeMessageType to set
	 */
	public void setFakeMessageType(String fakeMessageType) {
		fakeMessageType_ = fakeMessageType;
	}


	/**
	 * @return the fakeMessagesInterval_
	 */
	public static int getFakeMessagesInterval_() {
		return fakeMessagesInterval_;
	}


	/**
	 * @param fakeMessagesInterval_ the fakeMessagesInterval_ to set
	 */
	public static void setFakeMessagesInterval_(int fakeMessagesInterval_) {
		Vehicle.fakeMessagesInterval_ = fakeMessagesInterval_;
	}



	/**
	 * @return the emergencyBrakingCountdown_
	 */
	public long getEmergencyBrakingCountdown_() {
		return emergencyBrakingCountdown_;
	}


	/**
	 * @param emergencyBrakingCountdown_ the emergencyBrakingCountdown_ to set
	 */
	public void setEmergencyBrakingCountdown_(int emergencyBrakingCountdown_) {
		this.emergencyBrakingCountdown_ = emergencyBrakingCountdown_;
	}


	/**
	 * @return the emergencyBrakingDuration_
	 */
	public int getEmergencyBrakingDuration_() {
		return emergencyBrakingDuration_;
	}


	/**
	 * @param emergencyBrakingDuration_ the emergencyBrakingDuration_ to set
	 */
	public void setEmergencyBrakingDuration_(int emergencyBrakingDuration_) {
		this.emergencyBrakingDuration_ = emergencyBrakingDuration_;
	}


	/**
	 * @return the stopTime_
	 */
	public int getStopTime_() {
		return stopTime_;
	}


	/**
	 * @param forwardMessage_ the forwardMessage_ to set
	 */
	public void setForwardMessage_(boolean forwardMessage_) {
		this.forwardMessage_ = forwardMessage_;
	}


	/**
	 * @return the random
	 */
	public static Random getRandom() {
		return RANDOM;
	}


	/**
	 * @return the doNotRecycle_
	 */
	public boolean isDoNotRecycle_() {
		return doNotRecycle_;
	}


	/**
	 * @param doNotRecycle_ the doNotRecycle_ to set
	 */
	public void setDoNotRecycle_(boolean doNotRecycle_) {
		this.doNotRecycle_ = doNotRecycle_;
	}


	/**
	 * @return the emergencyBeacons
	 */
	public int getEmergencyBeacons() {
		return emergencyBeacons;
	}


	/**
	 * @param emergencyBeacons the emergencyBeacons to set
	 */
	public void setEmergencyBeacons(int emergencyBeacons) {
		this.emergencyBeacons = emergencyBeacons;
	}


	/**
	 * @return the waitingForVehicle_
	 */
	public Vehicle getWaitingForVehicle_() {
		return waitingForVehicle_;
	}


	/**
	 * @param waitingForVehicle_ the waitingForVehicle_ to set
	 */
	public void setWaitingForVehicle_(Vehicle waitingForVehicle_) {
		this.waitingForVehicle_ = waitingForVehicle_;
	}


	/**
	 * @return the drivingOnTheSide_
	 */
	public boolean isDrivingOnTheSide_() {
		return drivingOnTheSide_;
	}


	/**
	 * @return the idsProcessorList_
	 */
	public IDSProcessorList getIdsProcessorList_() {
		return idsProcessorList_;
	}


	/**
	 * @return the passingBlocking_
	 */
	public boolean isPassingBlocking_() {
		return passingBlocking_;
	}


	/**
	 * @param passingBlocking_ the passingBlocking_ to set
	 */
	public void setPassingBlocking_(boolean passingBlocking_) {
		this.passingBlocking_ = passingBlocking_;
	}


	/**
	 * @return the pcnMessagesCreated_
	 */
	public int getPcnMessagesCreated_() {
		return pcnMessagesCreated_;
	}


	/**
	 * @return the pcnForwardMessagesCreated_
	 */
	public int getPcnForwardMessagesCreated_() {
		return pcnForwardMessagesCreated_;
	}


	/**
	 * @return the evaMessagesCreated_
	 */
	public int getEvaMessagesCreated_() {
		return evaMessagesCreated_;
	}


	/**
	 * @return the evaForwardMessagesCreated_
	 */
	public int getEvaForwardMessagesCreated_() {
		return evaForwardMessagesCreated_;
	}


	/**
	 * @return the rhcnMessagesCreated_
	 */
	public int getRhcnMessagesCreated_() {
		return rhcnMessagesCreated_;
	}


	/**
	 * @return the eeblMessagesCreated_
	 */
	public int getEeblMessagesCreated_() {
		return eeblMessagesCreated_;
	}


	/**
	 * @return the fakeMessagesCreated_
	 */
	public int getFakeMessagesCreated_() {
		return fakeMessagesCreated_;
	}


	public boolean isInTrafficJam_() {
		return inTrafficJam_;
	}


	public void setInTrafficJam_(boolean inTrafficJam_) {
		this.inTrafficJam_ = inTrafficJam_;
	}


	/**
	 * @return the checkIDSProcessors_
	 */
	public boolean isCheckIDSProcessors_() {
		return checkIDSProcessors_;
	}


	/**
	 * @param checkIDSProcessors_ the checkIDSProcessors_ to set
	 */
	public void setCheckIDSProcessors_(boolean checkIDSProcessors_) {
		this.checkIDSProcessors_ = checkIDSProcessors_;
	}


	public void setEmergencyBraking_(boolean emergencyBraking_) {
		this.emergencyBraking_ = emergencyBraking_;
	}


	public boolean isEmergencyBraking_() {
		return emergencyBraking_;
	}


	public boolean isEEBLmessageIsCreated_() {
		return EEBLmessageIsCreated_;
	}


	public void setEEBLmessageIsCreated_(boolean eEBLmessageIsCreated_) {
		EEBLmessageIsCreated_ = eEBLmessageIsCreated_;
	}


	public KnownEventSourcesList getKnownEventSourcesList_() {
		return knownEventSourcesList_;
	}


	public int getSpamCounter_() {
		return spamCounter_;
	}


	public void setSpamCounter_(int spamCounter_) {
		this.spamCounter_ = spamCounter_;
	}


	public int getMessagesCounter_() {
		return messagesCounter_;
	}


	public void setMessagesCounter_(int messagesCounter_) {
		this.messagesCounter_ = messagesCounter_;
	}


	public Random getOwnRandom_() {
		return ownRandom_;
	}


	public int getEVAMessageDelay_() {
		return EVAMessageDelay_;
	}

	public void setEVAMessageDelay_(int eVAMessageDelay_) {
		EVAMessageDelay_ = eVAMessageDelay_;
	}
	
	public static int getMaxEVAMessageDelay_() {
		return maxEVAMessageDelay_;
	}

	public static void setMaxEVAMessageDelay_(int theMaxEVAMessageDelay_) {
		maxEVAMessageDelay_ = theMaxEVAMessageDelay_;
	}


	public boolean isLogBeaconsAfterEvent_() {
		return logBeaconsAfterEvent_;
	}


	public void setLogBeaconsAfterEvent_(boolean logBeaconsAfterEvent_) {
		this.logBeaconsAfterEvent_ = logBeaconsAfterEvent_;
	}


	public int getAmountOfLoggedBeacons_() {
		return amountOfLoggedBeacons_;
	}


	public void setAmountOfLoggedBeacons_(int amountOfLoggedBeacons_) {
		this.amountOfLoggedBeacons_ = amountOfLoggedBeacons_;
	}


	public String getBeaconString_() {
		return beaconString_;
	}


	public void setBeaconString_(String beaconString_) {
		this.beaconString_ = beaconString_;
	}

}