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





/**
 * This class represents a traffic light at a junction.
 */
public class TrafficLight {
	
	/** Default time intervals for state switching. */
	private static final double[] DEFAULT_SWITCH_INTERVALS = new double[] {5000, 1000, 5000};
	
	/** A static time to free a junction after a change of phases in ms. */
	private static final double JUNCTION_FREE_TIME = 2000;
	
	/** Duration of the red phase in ms for the priority street. */
	private double redPhaseLength_;
	
	/** Duration of the yellow phase in ms for the priority street. */
	private double yellowPhaseLength_;
	
	/** Duration of the green phase in ms for the priority street. */
	private double greenPhaseLength_;
	
	/** status of traffic light: 0 green : 1 green-orange : 2 red 3: freephase */
	private int state = 0;
	
	/** Traffic Light Collections */
	private Street[] streets_;

	
	/** Stores if a street is a priority street or not; used to distinguish between times. */
	private boolean[] streetIsPriority_;
		
	/** Timer for this traffic light; because all traffic lights on a junction run synchronously just one timer is needed. */
	private double timer_;
	
	/** The <code>Junction</code> this traffic light. */
	private Junction junction_;

	/** switcher between long and short signal length */
	private boolean switcher = true;

	/**
	 * Constructor.
	 */
	public TrafficLight(Junction junction) {
		junction_ = junction;
		//set the phases with standard times
		junction_.getNode().setTrafficLight_(this);
		
		redPhaseLength_ = DEFAULT_SWITCH_INTERVALS[0];
		yellowPhaseLength_ = DEFAULT_SWITCH_INTERVALS[1];
		greenPhaseLength_ = DEFAULT_SWITCH_INTERVALS[2];
		
		initialiseTrafficLight();
	}
	
	/**
	 * Constructor.
	 */
	public TrafficLight(double redPhaseLength, double yellowPhaseLength, double greenPhaseLength, Junction junction) {		
		junction_ = junction;
		
		junction_.getNode().setTrafficLight_(this);

		//set the phases from the given times
		redPhaseLength_ = redPhaseLength;
		yellowPhaseLength_ = yellowPhaseLength;
		greenPhaseLength_ = greenPhaseLength;
		
		initialiseTrafficLight();
	}
	
	/**
	 * This functions initializes the time arrays for each phase of the traffic light for each street;
	 * starts of with the priority streets at green and the crossing streets at red
	 * 
	 */
	private void initialiseTrafficLight(){			

		streetIsPriority_ = new boolean[junction_.getNode().getCrossingStreetsCount()];
		
		if(!junction_.getNode().hasNonDefaultSettings()){
			junction_.getNode().setStreetHasException_(new int[junction_.getNode().getCrossingStreetsCount()]);
			for(int m = 0; m < junction_.getNode().getStreetHasException_().length; m++) junction_.getNode().getStreetHasException_()[m] = 1;
		}

			
		streets_ = junction_.getNode().getCrossingStreets();
		Street[] tmpPriorityStreets = junction_.getPriorityStreets();
		
		boolean isOneway = false;
		
		for(int i = 0; i < streets_.length; i++){
			streetIsPriority_[i] = false;
			for(int j = 0; j < tmpPriorityStreets.length; j++){
				if(streets_[i] == tmpPriorityStreets[j]) streetIsPriority_[i] = true;
			}
			if(streets_[i].isOneway() && streets_[i].getStartNode() == junction_.getNode()) isOneway = true;

			if(!isOneway){
				if(streetIsPriority_[i]){
					if(junction_.getNode() == streets_[i].getStartNode()){
						streets_[i].setStartNodeTrafficLightState(0);
						if(junction_.getNode().hasNonDefaultSettings()){
							streets_[i].setStartNodeTrafficLightState(junction_.getNode().getStreetHasException_()[i]);
						}
						streets_[i].setPriorityOnStartNode(true);
					}
					else{
						streets_[i].setEndNodeTrafficLightState(0);	
						if(junction_.getNode().hasNonDefaultSettings()){
							streets_[i].setEndNodeTrafficLightState(junction_.getNode().getStreetHasException_()[i]);
						}
						streets_[i].setPriorityOnEndNode(true);
					}
				}
				else{
					if(junction_.getNode() == streets_[i].getStartNode()){
						streets_[i].setStartNodeTrafficLightState(4);
						if(junction_.getNode().hasNonDefaultSettings()){
							streets_[i].setStartNodeTrafficLightState(junction_.getNode().getStreetHasException_()[i]);
						}
					}
					else {
						streets_[i].setEndNodeTrafficLightState(4);
						if(junction_.getNode().hasNonDefaultSettings()){
							streets_[i].setEndNodeTrafficLightState(junction_.getNode().getStreetHasException_()[i]);
						}
					}
				}
			}

			isOneway = false;
			
			//lets calculate the drawing positions of the traffic light ... now its better for the performance
			calculateTrafficLightPosition(streets_[i]);
		}		
		timer_ = greenPhaseLength_;
		
		//tell the node, that he now has a traffic light
		junction_.getNode().setHasTrafficSignal_(true);
	
	}
	
	/**
	 * This function should change the states of the traffic lights if necessary. Should be called after the first greenphase
	 */
	public void changePhases(int timePerStep){
		//if remaining time is smaller than the timerPerStep we have to change the states
		if(timer_ < timePerStep){
			state = (state +1) % 4;		

			//could be less code, but this way I get a better performanz
			//(non)priorties where green: Change to orange
			if(state == 1) timer_ = yellowPhaseLength_;
			//(non)priorties where green-orange: Change to red for a freephase
			else if(state == 2)timer_ = JUNCTION_FREE_TIME;
			//priorties where free: Change to red
			else if(state == 0 && switcher)timer_ = greenPhaseLength_;
			//non-priorties where free: Change to red
			else if(state == 0 && !switcher)timer_ = redPhaseLength_;
			//yellow
			else if(state == 3)timer_ = yellowPhaseLength_;	
			
			
			switcher = !switcher;
			//update all street + 1

			for(int i = 0; i < streets_.length; i++){
				if(streets_[i].getStartNode() == junction_.getNode() && streets_[i].getStartNodeTrafficLightState() != -1){
					streets_[i].updateStartNodeTrafficLightState();
				}
				else if(streets_[i].getEndNode() == junction_.getNode() && streets_[i].getEndNodeTrafficLightState() != -1){
					streets_[i].updateEndNodeTrafficLightState();
				}
			}
		}
		//else change timer
		else timer_ = timer_ - timePerStep;
	}
	
	/* Calculates Traffic light position */
	/**
	 * Calculates Traffic light position for drawing
	 */
	public void calculateTrafficLightPosition(Street tmpStreet){	
		double junctionX = junction_.getNode().getX();
		double junctionY = junction_.getNode().getY();
		
		double nodeX;
		double nodeY;
		
		if(junction_.getNode().equals(tmpStreet.getStartNode())){
			nodeX = tmpStreet.getEndNode().getX();
			nodeY = tmpStreet.getEndNode().getY();
		}
		else{
			nodeX = tmpStreet.getStartNode().getX();
			nodeY = tmpStreet.getStartNode().getY();		
		}

		//calculate the linear function (y = mx + n) between junction node an the other node
		double m = (nodeY-junctionY)/(nodeX-junctionX);
		double n = nodeY - m*nodeX;
		
		double a = 1 + (m * m);
		double b = (2 * m * n) - (2 * m * junctionY) - (2 * junctionX);
		double c = (n * n) - (2 * n * junctionY) + (junctionY * junctionY) - (700 * 700) + (junctionX * junctionX);

		if(junction_.getNode().equals(tmpStreet.getStartNode())){
			if(nodeX < junctionX){
				tmpStreet.setTrafficLightStartX_((int)Math.round((-b - Math.sqrt((b*b) - (4*a*c))) / (2*a))); 
				tmpStreet.setTrafficLightStartY_((int)Math.round((m * tmpStreet.getTrafficLightStartX_()) + n));
			 }
			 else{
				tmpStreet.setTrafficLightStartX_((int)Math.round((-b + Math.sqrt((b*b) - (4*a*c))) / (2*a)));
				tmpStreet.setTrafficLightStartY_((int)Math.round((m * tmpStreet.getTrafficLightStartX_()) + n));
			 }
		}
		else{
			if(nodeX < junctionX){
				tmpStreet.setTrafficLightEndX_((int)Math.round((-b - Math.sqrt((b*b) - (4*a*c))) / (2*a))); 
				tmpStreet.setTrafficLightEndY_((int)Math.round((m * tmpStreet.getTrafficLightEndX_()) + n));
			 }
			 else{
				tmpStreet.setTrafficLightEndX_((int)Math.round((-b + Math.sqrt((b*b) - (4*a*c))) / (2*a)));
				tmpStreet.setTrafficLightEndY_((int)Math.round((m * tmpStreet.getTrafficLightEndX_()) + n));
			 }	
		}

	}


	/**
	 * Gets the length of the green phase.
	 * 
	 * @return the length of the green phase
	 */
	public double getGreenPhaseLength() {
		return greenPhaseLength_;
	}

	
	/**
	 * Sets the length for the green Phase.
	 * 
	 * @param greenPhaseLength the length for the green phase
	 */
	public void setGreenPhaseLength(double greenPhaseLength) {
		this.greenPhaseLength_ = greenPhaseLength;
	}
	
	/**
	 * Sets the length of the yellow phase.
	 * 
	 * @param yellowPhaseLength the length of the yellow phase
	 */
	public void setYellowPhaseLength(double yellowPhaseLength) {
		this.yellowPhaseLength_ = yellowPhaseLength;
	}

	/**
	 * Gets the length of the yellow phase.
	 * 
	 * @return the length of the yellow phase
	 */
	public double getYellowPhaseLength() {
		return yellowPhaseLength_;
	}

	/**
	 * Sets the length of the red phase.
	 * 
	 * @param redPhaseLength the length of the red phase
	 */
	public void setRedPhaseLength(double redPhaseLength) {
		this.redPhaseLength_ = redPhaseLength;
	}

	/**
	 * Gets the length of the red phase.
	 * 
	 * @return the length of the red phase
	 */
	public double getRedPhaseLength() {
		return redPhaseLength_;
	}


	/**
	 * @param state the state to set
	 */
	public void setState(int state) {
		this.state = state;
	}

	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	}

	/**
	 * @return the streets_
	 */
	public Street[] getStreets_() {
		return streets_;
	}

	/**
	 * @param streets_ the streets_ to set
	 */
	public void setStreets_(Street[] streets_) {
		this.streets_ = streets_;
	}




}