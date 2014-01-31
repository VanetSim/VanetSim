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

/**
 * An abstract class for events.
 */
public abstract class Event implements Comparable<Event>{
	
	/** The time in milliseconds when this event gets active. */
	protected int time_;
	
	/** The color for display of this event. */
	protected Color color_;
	
	/**
	 * Returns a descriptive text for display in the GUI.
	 * 
	 * @return the text
	 */
	public abstract String getText();
	
	/**
	 * Destroys itself.
	 */
	public abstract void destroy();
	
	/**
	 * This function should be called when the event is due. The event here does all needed
	 * actions.
	 */
	public abstract void execute();
	
	/**
	 * Gets the time when this event will be fired.
	 * 
	 * @return the time
	 * 
	 * @see vanetsim.scenario.events.Event#getTime()
	 */
	public int getTime(){
		return time_;
	}
	
	/**
	 * Gets the color of the descriptive text for display in the GUI.
	 * 
	 * @return the text color
	 * 
	 * @see vanetsim.scenario.events.Event#getTextColor()
	 */
	public Color getTextColor() {
		return color_;
	}
}