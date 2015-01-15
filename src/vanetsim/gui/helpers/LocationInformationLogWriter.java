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

import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.XMLFormatter;
import java.util.logging.Logger;

import vanetsim.ErrorLog;
import vanetsim.localization.Messages;
import vanetsim.scenario.Scenario;

/**
 * Helper Class for error logging.
 */
public final class LocationInformationLogWriter {

	/** The <code>java.util.logging.Logger</code> instance. */
	private static Logger logger = Logger.getLogger("LocationInformationLog"); //$NON-NLS-1$

	/** Path of log */
	private static String logPath = "";

	/** Old path of log */
	private static String logOldPath = "";

	/** file handler */
	private static FileHandler handler = null;

	/**
	 * Sets the parameters for the static class.
	 * 
	 * @param dir
	 *            the directory where the error log files are located
	 * @param format
	 *            the format of the log files (<code>txt</code> or
	 *            <code>xml</code>)
	 */
	public static void setParameters(String dir, String format) {
		logger.setLevel(Level.FINEST);
		if(dir.equals("/")){
			dir = System.getProperty("user.dir") + "/";
		}
		
		logPath = dir;
		
		java.util.Date dt = new java.util.Date();
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss"); //$NON-NLS-1$
		try {
			if (!dir.equals(logOldPath)) {
				if (handler != null)
					logger.removeHandler(handler);
				String scenName = Scenario.getInstance().getScenarioName();
				if(scenName != null && !scenName.equals("")) handler = new FileHandler(dir + "LocationInformationLog_" +  scenName.substring(0, scenName.length()-4) + "_" + df.format(dt) + "." + format, true);//$NON-NLS-1$ //$NON-NLS-2$
				else handler = new FileHandler(dir + "LocationInformationLog_" + df.format(dt) + "." + format, true);//$NON-NLS-1$ //$NON-NLS-2$
				logOldPath = dir;
				logger.setUseParentHandlers(false); // don't log to console
				logger.addHandler(handler);
				if (format.equals("log")) //$NON-NLS-1$
					handler.setFormatter(new LogFormatter());
				else
					handler.setFormatter(new XMLFormatter());
			}
		} catch (Exception e) {
			ErrorLog
					.log(
							Messages.getString("ErrorLog.whileSetting"), 7, ErrorLog.class.getName(), "setParameters", e); //$NON-NLS-1$ //$NON-NLS-2$
			System.exit(1);
		}
	}

	/**
	 * Logs the attacker data.
	 * 
	 * @param message data to log
	 * @param mode log mode
	 *            
	 */
	public static synchronized void log(String message, int mode) {
		try {
			logger.log(Level.FINEST, message);
		} catch (Exception new_e) {
			System.out
					.println(Messages.getString("ErrorLog.whileLogging") + message + ")! " + new_e.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			new_e.printStackTrace();
		}
	}

	/**
	 * Logs the attacker data.
	 * 
	 * @param message
	 *            data to log
	 */
	public static synchronized void log(String message) {
		try {
			logger.log(Level.FINEST, message);
		} catch (Exception new_e) {
			System.out
					.println(Messages.getString("ErrorLog.whileLogging") + message + ")! " + new_e.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			new_e.printStackTrace();
		}
	}

	public static void setLogPath(String logPath) {
		setParameters(logPath + "/", "log");
		LocationInformationLogWriter.logPath = logPath;
	}

	public static String getLogPath() {
		return logPath;
	}
}