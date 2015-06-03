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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.UIManager;



import vanetsim.gui.DrawingArea;
import vanetsim.gui.Renderer;
import vanetsim.gui.controlpanels.MainControlPanel;
import vanetsim.gui.helpers.MouseClickManager;
import vanetsim.gui.helpers.ProgressOverlay;
import vanetsim.gui.helpers.ReRenderManager;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.simulation.SimulationMaster;


/**
 * This is the main class for the VANet-Simulator which starts the GUI and all other components.
 */
public final class VanetSimStart implements Runnable {

	/** The master thread for simulation delegation. Stored here if any other class needs control over it. */
	private static SimulationMaster simulationMaster_;

	/** The controlpanel on the right side. */
	private static MainControlPanel controlPanel_;

	/** The <code>JFrame</code> which is the base of the application. */
	private static JFrame mainFrame_;
	
	/** <code>true</code> if double buffering shall be used on the drawing area. */
	private static boolean useDoubleBuffering_;
	
	/** <code>true</code> if a manual buffering shall be used on the drawing area. */
	private static boolean drawManualBuffered_;

	/** A reference to the progress bar. */
	private static ProgressOverlay progressBar_;
	
	
	public VanetSimStart(){
		readconfig("./config.txt"); //$NON-NLS-1$
	}

	/**
	 * Thread which creates the GUI.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		mainFrame_ = new JFrame();
		mainFrame_.setTitle(Messages.getString("StartGUI.applicationtitle")); //$NON-NLS-1$
		mainFrame_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		progressBar_ = new ProgressOverlay();
		if(Runtime.getRuntime().maxMemory() < 120000000) ErrorLog.log(Messages.getString("StartGUI.detectedLowMemory"), 6, VanetSimStart.class.getName(), "run", null); //$NON-NLS-1$ //$NON-NLS-2$
		URL appicon = ClassLoader.getSystemResource("vanetsim/images/logo.png"); //$NON-NLS-1$
		if (appicon != null){
			mainFrame_.setIconImage(Toolkit.getDefaultToolkit().getImage(appicon));
		} else ErrorLog.log(Messages.getString("StartGUI.noAppIcon"), 6, VanetSimStart.class.getName(), "run", null); //$NON-NLS-1$ //$NON-NLS-2$

		DrawingArea drawarea = addComponentsToPane(mainFrame_.getContentPane());

		Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds(); 
		
		String osName = System.getProperty("os.name").toLowerCase();
		@SuppressWarnings("unused")
		boolean isMacOs = osName.startsWith("mac os x");
		//leads to some issues on linux
		/*
		if (isMacOs) 
		{
			Application application = Application.getApplication();
			Image image = Toolkit.getDefaultToolkit().getImage(appicon);
			application.setDockIconImage(image);
		}
		*/

		mainFrame_.pack();
		//mainFrame_.setIconImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("vanetsim/images/logo.png")));
		mainFrame_.setSize((int) bounds.getWidth(), (int) bounds.getHeight());
		mainFrame_.setLocationRelativeTo(null); // center on screen
		mainFrame_.setResizable(true);
		mainFrame_.setVisible(true);
		controlPanel_.getEditPanel().setEditMode(false);


		simulationMaster_ = new SimulationMaster();
		simulationMaster_.start();
		Map.getInstance().initNewMap(100000, 100000, 10000, 10000);
		Map.getInstance().signalMapLoaded();
		ReRenderManager.getInstance().start();
		MouseClickManager.getInstance().setDrawArea(drawarea);
		MouseClickManager.getInstance().start();
	}

	/**
	 * Function to add the control elements to a container.
	 * 
	 * @param container	the container on which to add the elements
	 * 
	 * @return the constructed <code>DrawingArea</code>
	 */
	public static DrawingArea addComponentsToPane(Container container) {
		container.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		DrawingArea drawarea = new DrawingArea(useDoubleBuffering_, drawManualBuffered_);
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		container.add(drawarea, c);
		Renderer.getInstance().setDrawArea(drawarea);

		controlPanel_ = new MainControlPanel();
		
		controlPanel_.setPreferredSize(new Dimension(200, 100000));
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
		c.gridx = 1;
		c.gridy = 0;
	
		container.add(controlPanel_, c);

		return drawarea;
	}

	/**
	 * Sets the display state of the progress bar.
	 * 
	 * @param state	<code>true</code> to display the progress bar, <code>false</code> to disable it
	 */
	public static void setProgressBar(boolean state){
		progressBar_.setVisible(state);
	}
	
	/**
	 * Gets the control panel on the right side.
	 * 
	 * @return the control panel
	 */
	public static MainControlPanel getMainControlPanel(){
		return controlPanel_;
	}

	/**
	 * Gets the initial <code>JFrame</code> of the application.
	 * 
	 * @return the <code>JFrame</code>
	 */
	public static JFrame getMainFrame(){
		return mainFrame_;
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
					UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
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
		
			useDoubleBuffering_ = Boolean.parseBoolean(configFile.getProperty("double_buffer", "true")); //$NON-NLS-1$ //$NON-NLS-2$
			drawManualBuffered_ = Boolean.parseBoolean(configFile.getProperty("draw_manual_buffered", "false")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception e) {
			ErrorLog.log(Messages.getString("StartGUI.whileConfigreading"), 7, VanetSimStart.class.getName(), "readconfig",  e); //$NON-NLS-1$ //$NON-NLS-2$
			System.exit(1);
		}
	}

}