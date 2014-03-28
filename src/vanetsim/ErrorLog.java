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
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import vanetsim.localization.Messages;

/**
 * Helper Class for error logging.
 */
public final class ErrorLog {

	/** The <code>java.util.logging.Logger</code> instance. */
	private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); //$NON-NLS-1$

	/**
	 * Sets the parameters for the static class.
	 *
	 * @param level	the minimum level for error messages. If the severity of an error is lower than this value, nothing is logged.
	 * @param dir		the directory where the error log files are located
	 * @param format	the format of the log files (<code>txt</code> or <code>xml</code>)
	 */
	public static void setParameters(int level, String dir, String format) {
		switch (level) {
		case 2:
			logger.setLevel(Level.FINER);
			break;
		case 3:
			logger.setLevel(Level.FINE);
			break;
		case 4:
			logger.setLevel(Level.CONFIG);
			break;
		case 5:
			logger.setLevel(Level.INFO);
			break;
		case 6:
			logger.setLevel(Level.WARNING);
			break;
		case 7:
			logger.setLevel(Level.SEVERE);
			break;
		default:
			logger.setLevel(Level.FINEST);
		}
		java.util.Date dt = new java.util.Date();
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss"); //$NON-NLS-1$
		try {
			FileHandler handler = new FileHandler(dir + "log_" + df.format(dt) + "." + format, true);//$NON-NLS-1$ //$NON-NLS-2$
			logger.addHandler(handler);
			logger.setUseParentHandlers(false); // don't log to console
			if (format.equals("txt")) //$NON-NLS-1$
				handler.setFormatter(new SimpleFormatter());
			else
				handler.setFormatter(new XMLFormatter());
		} catch (Exception e) {
			ErrorLog.log(Messages.getString("ErrorLog.whileSetting"), 7, ErrorLog.class.getName(), "setParameters",  e); //$NON-NLS-1$ //$NON-NLS-2$
			System.exit(1);
		}
	}

	/**
	 * Logs an error.
	 *
	 * @param message	error message
	 * @param severity	error severity in scale of 1-7. A higher value stands for a more severe error (Finest, Finer, Fine, Config, Info, Warning, Severe)
	 * @param errClass	class where the error occured
	 * @param errMethod	method in which the error occured
	 * @param e			the exception (use <code>null</code> if you don't have one)
	 */
	public static synchronized void log(String message, int severity, String errClass, String errMethod, Exception e) {
		try {
			if (e != null) {
				message = message + "\n" + e.getLocalizedMessage(); //$NON-NLS-1$
				switch (severity) {
				case 1:
					logger.logp(Level.FINEST, errClass, errMethod, message, e);
					break;
				case 2:
					logger.logp(Level.FINER, errClass, errMethod, message, e);
					break;
				case 3:
					logger.logp(Level.FINE, errClass, errMethod, message, e);
					break;
				case 4:
					logger.logp(Level.CONFIG, errClass, errMethod, message, e);
					break;
				case 5:
					logger.logp(Level.INFO, errClass, errMethod, message, e);
					break;
				case 6:
					logger.logp(Level.WARNING, errClass, errMethod, message, e);
					break;
				default:
					logger.logp(Level.SEVERE, errClass, errMethod, message, e);
				}
			} else {
				switch (severity) {
				case 1:
					logger.logp(Level.FINEST, errClass, errMethod, message);
					break;
				case 2:
					logger.logp(Level.FINER, errClass, errMethod, message);
					break;
				case 3:
					logger.logp(Level.FINE, errClass, errMethod, message);
					break;
				case 4:
					logger.logp(Level.CONFIG, errClass, errMethod, message);
					break;
				case 5:
					logger.logp(Level.INFO, errClass, errMethod, message);
					break;
				case 6:
					logger.logp(Level.WARNING, errClass, errMethod, message);
					break;
				default:
					logger.logp(Level.SEVERE, errClass, errMethod, message);
				}
			}
			if(severity == 7) JOptionPane.showMessageDialog(VanetSimStart.getMainFrame(), Messages.getString("ErrorLog.error") + message + (e!=null?"\n" + Messages.getString("ErrorLog.seeErrorlog"):""), Messages.getString("ErrorLog.errorWindowTitle"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			else if(severity == 6) JOptionPane.showMessageDialog(VanetSimStart.getMainFrame(), Messages.getString("ErrorLog.warning") + message + (e!=null?"\n" + Messages.getString("ErrorLog.seeErrorlog"):""), Messages.getString("ErrorLog.warningWindowTitle"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		} catch (Exception new_e) {
			System.out.println(Messages.getString("ErrorLog.whileLogging") + message + ":" + e.getLocalizedMessage() + ")! " + new_e.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			new_e.printStackTrace();
		}
	}

	/**
	 * Deletes old logfiles (uses file modification time to determine which files are old)
	 *
	 * @param loggerTrashtime	maximum age (in days) for a log file. Older files are deleted.
	 * @param loggerDir			directory where to delete files
	 */
	public static void deleteOld(long loggerTrashtime, String loggerDir) {
		try {
			File path = new File(loggerDir);
			File files[] = path.listFiles();
			long deletedate = System.currentTimeMillis() - 60000 - (loggerTrashtime * 86400000); // subtraction of 60000ms to avoid deletion of current logfile if logger_trashtime=0
			for (int i = 0, n = files.length; i < n; i++) {
				if (files[i].lastModified() < deletedate) {
					String filename = files[i].toString();
					if (files[i].delete())
						log(Messages.getString("ErrorLog.oldLogfile") + filename + Messages.getString("ErrorLog.deletedSuccess"), 2, ErrorLog.class.getName(), "deleteold", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					else
						log(Messages.getString("ErrorLog.deleteFailed") + filename + "!", 5, ErrorLog.class.getName(), "deleteold", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		} catch (Exception e) {
			log(Messages.getString("ErrorLog.deleteFailedGlobal"), 6, ErrorLog.class.getName(), "deleteold", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}