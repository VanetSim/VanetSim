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

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;

import vanetsim.gui.Renderer;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.scenario.Scenario;
import vanetsim.simulation.SimulationMaster;

/**
 * This is the main class for the VANet-Simulator which starts the GUI and all other components.
 */
public final class ConsoleStart implements Runnable {

	/** The master thread for simulation delegation. Stored here if any other class needs control over it. */
	private static SimulationMaster simulationMaster_;
	
	/** Map file to load */
	private static File mapFile_ = null;
	
	/** Scenario file to load */
	private static File scenarioFile_ = null;
	
	/** Simulation time */
	private static int simulationTime_ = 0;
	

	/**
	 * Starts the simulator in console mode
	 * 
	 * @param mapFile	path of the used map
	 * @param scenarioFile	path of the used scenario
	 * @param simulationTime	the simulation time in milliseconds
	 */	
	public ConsoleStart(String mapFile, String scenarioFile, String simulationTime){
		readconfig("./config.txt");
		
		mapFile_ = new File(mapFile);
		scenarioFile_ = new File(scenarioFile);
		simulationTime_ = Integer.parseInt(simulationTime);
	}

	/**
	 * Thread which creates the GUI.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Renderer.getInstance().setConsoleStart(true);

		System.out.println("Time:" + new Date());

		System.out.println(Messages.getString("ConsoleStart.SimMasterInit"));
		simulationMaster_ = new SimulationMaster();
		simulationMaster_.start();
		System.out.println(Messages.getString("ConsoleStart.SimMasterInited"));
		
		Map.getInstance().initNewMap(100000, 100000, 10000, 10000);
		Map.getInstance().signalMapLoaded();
		
		System.out.println(Messages.getString("ConsoleStart.MapLoad"));
		Map.getInstance().load(mapFile_, false);
		System.out.println(Messages.getString("ConsoleStart.MapLoaded"));
		
		System.out.println(Messages.getString("ConsoleStart.ScenarioLoad"));
		Scenario.getInstance().load(scenarioFile_, false);
		System.out.println(Messages.getString("ConsoleStart.ScenarioLoaded"));
		
		System.out.println(Messages.getString("ConsoleStart.SetSimTime"));
		ConsoleStart.getSimulationMaster().jumpToTime(simulationTime_);
		System.out.println(Messages.getString("ConsoleStart.SimTimeSet"));
		
		System.out.println(Messages.getString("ConsoleStart.SimulationStart"));
		ConsoleStart.getSimulationMaster().startThread();
		System.out.println(Messages.getString("ConsoleStart.SimulationStarted"));
	}

	/**
	 * Returns the simulation master (for example in order to stop or start simulation).
	 * 
	 * @return the simulation master
	 */
	public static SimulationMaster getSimulationMaster(){
		return simulationMaster_;
	}

	/**
	 * Reads the parameters from the configuration file.
	 * 
	 * @param configFilePath	path to the configuration file
	 */
	private static void readconfig(String configFilePath) {
		String loggerFormat, loggerDir;
		Integer loggerLevel;
		Long loggerTrashtime;
		boolean loggerFormatError = false;
		Properties configFile = new Properties();
		try {
			configFile.load(new FileInputStream(configFilePath));

			String guiTheme = configFile.getProperty("gui_theme", ""); //$NON-NLS-1$ //$NON-NLS-2$
			// set substance theme
			if (!guiTheme.equals("")) { //$NON-NLS-1$
				try {
					
				} catch (Exception e) {
					ErrorLog.log(Messages.getString("StartGUI.substanceThemeError"), 3, VanetSimStart.class.getName(), "readconfig", e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			// read parameters for logfile
			loggerTrashtime = Long.parseLong(configFile.getProperty("logger_trashtime", "365000")); //$NON-NLS-1$ //$NON-NLS-2$
			loggerDir = configFile.getProperty("logger_dir", "./"); //$NON-NLS-1$ //$NON-NLS-2$
			loggerFormat = configFile.getProperty("logger_format", "txt"); //$NON-NLS-1$ //$NON-NLS-2$
			loggerLevel = Integer.parseInt(configFile.getProperty("logger_level", "1")); //$NON-NLS-1$ //$NON-NLS-2$

			if (!loggerFormat.equals("txt") && !loggerFormat.equals("xml")) { //$NON-NLS-1$ //$NON-NLS-2$
				loggerFormatError = true;
				loggerFormat = "txt"; //$NON-NLS-1$
			}

			ErrorLog.setParameters(loggerLevel, loggerDir, loggerFormat);

			if (loggerTrashtime < 0 || loggerTrashtime > 365000) {
				loggerTrashtime = (long) 365000;
				ErrorLog.log("", 4, VanetSimStart.class.getName(), "readconfig", null); //$NON-NLS-1$ //$NON-NLS-2$
			}
			ErrorLog.deleteOld(loggerTrashtime, loggerDir);

			if (loggerFormatError) ErrorLog.log(Messages.getString("StartGUI.wrongLogformat"), 4, VanetSimStart.class.getName(), "readconfig", null); //$NON-NLS-1$ //$NON-NLS-2$
			if (loggerLevel < 1 || loggerLevel > 7) ErrorLog.log(Messages.getString("StartGUI.wrongLoglevel"), 4, VanetSimStart.class.getName(), "readconfig", null); //$NON-NLS-1$ //$NON-NLS-2$
		
		} catch (Exception e) {
			ErrorLog.log(Messages.getString("StartGUI.whileConfigreading"), 7, VanetSimStart.class.getName(), "readconfig",  e); //$NON-NLS-1$ //$NON-NLS-2$
			System.exit(1);
		}
	}

}