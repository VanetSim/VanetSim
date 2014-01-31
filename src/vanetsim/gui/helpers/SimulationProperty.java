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

public class SimulationProperty {
	private final String propertyKey_;
	private final double startValue_;
	private final double stepValue_;
	private final int stepAmount_;
	
	public SimulationProperty(String propertyKey, double startValue, double stepValue, int stepAmount){
		propertyKey_ = propertyKey;
		startValue_ = startValue;
		stepValue_ = stepValue;
		stepAmount_ = stepAmount;
	}

	public String getPropertyKey_() {
		return propertyKey_;
	}

	public double getStartValue_() {
		return startValue_;
	}

	public double getStepValue_() {
		return stepValue_;
	}

	public int getStepAmount_() {
		return stepAmount_;
	}
	
	public String toString(){
		return "Key:" + propertyKey_ + " :Start:" + startValue_ + " :Step:" + stepValue_ + " :Amount:" + stepAmount_;
	}
}
