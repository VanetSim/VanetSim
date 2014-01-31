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
package vanetsim;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import vanetsim.localization.Messages;


public class VanetSimStarter {
	/**
	 * The main method.
	 * 
	 * @param args	command line arguments. No argument is given the simulator will start in GUI-Mode.
	 * if 3 arguments are given the simulator will start without GUI in console mode. args[0] = map path; args[1] = scenario path args[2] = time until the simulation stops 
	 * example for console mode: java -jar VanetSimStarter.jar /Users/Max_Mustermann/rgb-1.xml /Users/Max_Mustermann/rgb-1_scen.xml 50000 
	 */
	public static void main(String[] args) {
		if(args.length < 3) SwingUtilities.invokeLater(new VanetSimStart());
		else SwingUtilities.invokeLater(new ConsoleStart(args[0], args[1], args[2]));
	}

	public static void restartWithLanguage(String language){
		String[] buttons = {Messages.getString("VanetSimStarter.Yes", language), Messages.getString("VanetSimStarter.No", language)};
		if(JOptionPane.showOptionDialog(null, Messages.getString("VanetSimStarter.WarningMessage", language), "", JOptionPane.WARNING_MESSAGE, 0, null, buttons, buttons[1]) == 0){
			Messages.setLanguage(language);
			VanetSimStart.getMainFrame().dispose();
			SwingUtilities.invokeLater(new VanetSimStart());
		}	
	}
}
