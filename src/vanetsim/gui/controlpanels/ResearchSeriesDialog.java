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
package vanetsim.gui.controlpanels;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import java.util.ArrayDeque;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import vanetsim.ErrorLog;
import vanetsim.VanetSimStart;
import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.ResearchSeriesHelperDialog;
import vanetsim.gui.helpers.SimulationProperty;
import vanetsim.gui.helpers.SimulationSeries;
import vanetsim.gui.helpers.TextAreaLabel;
import vanetsim.gui.helpers.VehicleSet;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.routing.WayPoint;
import vanetsim.scenario.IDSProcessor;
import vanetsim.scenario.KnownEventSource;
import vanetsim.scenario.KnownVehicle;
import vanetsim.scenario.Scenario;
import vanetsim.scenario.Vehicle;

/**
 * A dialog to create,edit and delete vehicle type files.
 */


public final class ResearchSeriesDialog extends JDialog implements ActionListener, ChangeListener{
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = -2918735209479587896L;
	
	/** The only instance of this class (singleton). */
	private static final ResearchSeriesDialog INSTANCE = new ResearchSeriesDialog();

	/**
	 * Gets the single instance of this ResearchSeriesDialog.
	 * 
	 * @return single instance of this ResearchSeriesDialog
	 */
	public static ResearchSeriesDialog getInstance(){
		return INSTANCE;
	}
	

	/** A JComboBox to switch between simulation series. */
	private JComboBox<Object> chooseSeries_ = new JComboBox<Object>();	
	
	/** A JComboBox to switch between vehicle sets. */
	private JComboBox<Object> chooseVehicleSet_ = new JComboBox<Object>();	
	
	/** A JList displaying all selected general settings **/
	private JList<Object> generalSettingsList_;
	
	/** A DefaultListModel for the general settings **/
	private DefaultListModel<Object> generalSettingsModel_ = new DefaultListModel<Object>();

	/** A JList displaying all selected vehicle sets **/
	private JList<Object> vehicleSetsList_;
	
	/** A DefaultListModel for the vehicle sets **/
	private DefaultListModel<Object> vehicleSetsModel_ = new DefaultListModel<Object>();
	
	/** The input field for the amount of simulation runs per scenario */
	private final JSpinner amountOfSimulationRuns_;
	
	/** The selectProperties mode */
	private boolean selectPropertiesModeOn_ = true;
	
	/** A array containing all series */
	private ArrayList<SimulationSeries> simulationSeriesList_ = new ArrayList<SimulationSeries>();
	
	/** the active series */
	private SimulationSeries activeSeries_ = null;
	
	/** the active vehicle set */
	private VehicleSet activeVehicleSet_ = null;
	
	/** string array to save jobs */
	ArrayList<String> jobList_ = new ArrayList<String>();
	
	/** simulation duration */
	private int simulationDuration_ = 1000000;
	
	/** available memory for simulation */
	private int availableMemory_ = 6144;
	
	/**
	 * Constructor. Creating GUI items.
	 */
	public ResearchSeriesDialog(){
		this.setSize(500, 700);
		this.setPreferredSize(new Dimension(500,700));
		
		this.setModal(true);
		//some JDialog options
		setUndecorated(true);
		setLayout(new GridBagLayout());
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		//WindowAdapter to catch closing event
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                    closeDialog();
            }
        }
        );  
        
		setModal(true);

		//some basic options
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 0.5;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 1;	
		c.insets = new Insets(5,5,5,5);
		
		JLabel jLabel1 = new JLabel("<html><b>" + Messages.getString("ResearchSeriesDialog.header") +"</b></html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		c.gridwidth = 3;
		
		add(jLabel1,c);
		++c.gridy;
		++c.gridy;
		//start building gui
		c.gridwidth = 3;
		add(new JLabel(Messages.getString("ResearchSeriesDialog.selectSeries")),c);
		++c.gridy;
		
		c.gridwidth = 1;
		c.gridx = 0;	
		add(chooseSeries_, c);
		chooseSeries_.setActionCommand("SeriesChanged");
		chooseSeries_.addActionListener(this);
		
		c.gridx = 1;
		c.gridwidth = 2;
		JPanel wraperPanel = new JPanel();
		add(wraperPanel, c);
		wraperPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		JButton addSeries = new JButton("+");
		addSeries.setPreferredSize(new Dimension(40,20));
		addSeries.setActionCommand("addSeries");
		wraperPanel.add(addSeries);
		addSeries.addActionListener(this);
		
		JButton deleteSeries = new JButton("-");
		deleteSeries.setPreferredSize(new Dimension(40,20));
		deleteSeries.setActionCommand("deleteSeries");
		wraperPanel.add(deleteSeries);
		deleteSeries.addActionListener(this);
		
		JButton copySeries = new JButton("C");
		copySeries.setPreferredSize(new Dimension(40,20));
		copySeries.setActionCommand("copySeries");
		wraperPanel.add(copySeries);
		copySeries.addActionListener(this);
		
		
		++c.gridy;
		c.gridwidth = 3;
		c.gridx = 0;	
		add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		++c.gridy;
		
		
		c.gridx = 0;
		add(new JLabel(Messages.getString("ResearchSeriesDialog.generalSettings")),c);
		++c.gridy;
		
		c.gridwidth = 3;
		generalSettingsList_ = new JList<Object>(generalSettingsModel_);
		generalSettingsList_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(generalSettingsList_, c);
		
		++c.gridy;
		c.gridx = 1;
		c.gridwidth = 2;
		wraperPanel = new JPanel();
		add(wraperPanel, c);
		wraperPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		JButton addGeneralSetting = new JButton("+");
		addGeneralSetting.setPreferredSize(new Dimension(40,20));
		addGeneralSetting.setActionCommand("addGeneralSetting");
		wraperPanel.add(addGeneralSetting);
		addGeneralSetting.addActionListener(this);
		
		c.gridx = 2;
		JButton deleteGeneralSetting = new JButton("-");
		deleteGeneralSetting.setPreferredSize(new Dimension(40,20));
		deleteGeneralSetting.setActionCommand("deleteGeneralSetting");
		wraperPanel.add(deleteGeneralSetting);
		deleteGeneralSetting.addActionListener(this);
		
		++c.gridy;
		c.gridwidth = 3;
		c.gridx = 0;	
		add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		
		++c.gridy;
		
		c.gridx = 0;
		c.gridwidth = 3;
		add(new JLabel(Messages.getString("ResearchSeriesDialog.selectVehicleSet")),c);
		++c.gridy;
		
		c.gridwidth = 1;
		c.gridx = 0;	
		add(chooseVehicleSet_, c);
		chooseVehicleSet_.setActionCommand("VehicleSetChanged");
		chooseVehicleSet_.addActionListener(this);
		
		c.gridx = 1;
		c.gridwidth = 2;
		wraperPanel = new JPanel();
		add(wraperPanel, c);
		wraperPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		JButton addVehicleSet = new JButton("+");
		addVehicleSet.setPreferredSize(new Dimension(40,20));
		addVehicleSet.setActionCommand("addVehicleSet");
		wraperPanel.add(addVehicleSet);
		addVehicleSet.addActionListener(this);
		
		c.gridx = 2;
		JButton deleteVehicleSet = new JButton("-");
		deleteVehicleSet.setPreferredSize(new Dimension(40,20));
		deleteVehicleSet.setActionCommand("deleteVehicleSet");
		wraperPanel.add(deleteVehicleSet);
		deleteVehicleSet.addActionListener(this);
		
		++c.gridy;
		c.gridwidth = 3;
		c.gridx = 0;	
		add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		
		++c.gridy;
		
		c.gridwidth = 3;
		c.gridx = 0;
		add(new JLabel(Messages.getString("ResearchSeriesDialog.vehicleSets")),c);
		++c.gridy;

		vehicleSetsList_ = new JList<Object>(vehicleSetsModel_);

		vehicleSetsList_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(vehicleSetsList_, c);
		
		c.gridwidth = 1;
		++c.gridy;
		c.gridx = 1;
		c.gridwidth = 2;
		wraperPanel = new JPanel();
		add(wraperPanel, c);
		wraperPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		JButton addVehicleSetting = new JButton("+");
		addVehicleSetting.setPreferredSize(new Dimension(40,20));
		addVehicleSetting.setActionCommand("addVehicleSetting");
		wraperPanel.add(addVehicleSetting);
		addVehicleSetting.addActionListener(this);
		
		c.gridx = 2;
		JButton deleteVehicleSetting = new JButton("-");
		deleteVehicleSetting.setPreferredSize(new Dimension(40,20));
		deleteVehicleSetting.setActionCommand("deleteVehicleSetting");
		wraperPanel.add(deleteVehicleSetting);
		deleteVehicleSetting.addActionListener(this);
		
		++c.gridy;
		c.gridwidth = 3;
		c.gridx = 0;	
		add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		
		++c.gridy;
		
		
		c.gridx = 0;
		c.gridwidth = 2;
		add(new JLabel(Messages.getString("ResearchSeriesDialog.selectSimulationRunAmount")),c);
		c.gridx = 2;
		c.gridwidth = 1;
		amountOfSimulationRuns_ = new JSpinner();
		amountOfSimulationRuns_.setValue(5);
		amountOfSimulationRuns_.addChangeListener(this);
		add(amountOfSimulationRuns_,c);
		++c.gridy;
		
		c.gridx = 0;
		c.gridwidth = 1;
		JButton createScenarios = new JButton(Messages.getString("ResearchSeriesDialog.createButton"));
		createScenarios.setActionCommand("createScenarios");
		add(createScenarios, c);
		createScenarios.addActionListener(this);
		++c.gridy;
		
		TextAreaLabel jlabel1 = new TextAreaLabel(Messages.getString("ResearchSeriesDialog.note")); //$NON-NLS-1$
		++c.gridy;

		c.gridx = 0;
		c.gridwidth = 3;
		add(jlabel1, c);
		
		//to consume the rest of the space
		c.weighty = 1.0;
		++c.gridy;
		++c.gridy;
		++c.gridy;
		add(new JPanel(), c);
		
		//resize window
		pack();
		//adjust window size
		setLocationRelativeTo(VanetSimStart.getMainFrame());
		//show window
		setVisible(false);
	}

	/**
	 * An implemented <code>ActionListener</code> which performs the needed actions when the combobox or buttons (load, save, delete ...)
	 * are clicked.
	 * 
	 * @param e	an <code>ActionEvent</code>
	 */
	public void actionPerformed(ActionEvent e) {	
		String command = e.getActionCommand();
		
		if ("SeriesChanged".equals(command)){
			loadNewSeries((SimulationSeries)chooseSeries_.getSelectedItem());
		}
		else if(("addSeries").equals(command)){		
			String str = JOptionPane.showInputDialog(null, Messages.getString("ResearchSeriesDialog.inputMessage"), Messages.getString("ResearchSeriesDialog.inputMessageTitle"), 1);
			if(str != null)simulationSeriesList_.add(new SimulationSeries(str));	
			clearGui();
			updateSeries();
		}
		else if(("deleteSeries").equals(command)){
			if(simulationSeriesList_.size() > 0) simulationSeriesList_.remove(chooseSeries_.getSelectedItem());
			updateSeries();
			
			loadNewSeries((SimulationSeries)chooseSeries_.getSelectedItem());

		}
		else if(("copySeries").equals(command)){
			if(activeSeries_ != null) copySeries(activeSeries_);
		}
		
		
		else if(("addGeneralSetting").equals(command)){
			if(generalSettingsModel_.getSize() == 1){
			}
			else{
				if(activeSeries_ == null){
					JOptionPane.showMessageDialog(null, Messages.getString("ResearchSeriesDialog.MsgBoxCreateSeries"), "Error", JOptionPane.ERROR_MESSAGE);
				}
				else hideResearchWindow(true, "generalSettings", "");
			}

		}
		else if(("deleteGeneralSetting").equals(command)){
			if(generalSettingsList_.getSelectedIndex() > -1){
				activeSeries_.removeProperty(((SimulationProperty)generalSettingsModel_.get(generalSettingsList_.getSelectedIndex())).getPropertyKey_());
				generalSettingsModel_.remove(generalSettingsList_.getSelectedIndex());	
			}
		}
		else if(("VehicleSetChanged").equals(command)){
			loadNewSeries((SimulationSeries)chooseSeries_.getSelectedItem());
		}
		else if(("addVehicleSet").equals(command)){
			
			if(activeSeries_ == null){
				JOptionPane.showMessageDialog(null, Messages.getString("ResearchSeriesDialog.MsgBoxCreateSeries"), "Error", JOptionPane.ERROR_MESSAGE);
			}
			else{
				String str = JOptionPane.showInputDialog(null, Messages.getString("ResearchSeriesDialog.inputMessage"), Messages.getString("ResearchSeriesDialog.inputMessageTitle"), 1);
				if(str != null){
					activeVehicleSet_ = new VehicleSet(str);
					activeSeries_.getVehicleSetList_().add(activeVehicleSet_);
					chooseVehicleSet_.addItem(activeVehicleSet_);	
					chooseVehicleSet_.setSelectedItem(activeVehicleSet_);
				}
				clearVehicles();
				loadNewSeries((SimulationSeries)chooseSeries_.getSelectedItem());
				
				hideResearchWindow(true, "allVehicleProperties", "all");

				
			}
		}
		else if(("deleteVehicleSet").equals(command)){
			chooseVehicleSet_.removeActionListener(this);
			activeSeries_.removeVehicleSet(((VehicleSet)chooseVehicleSet_.getSelectedItem()).getName_());
			if(chooseVehicleSet_.getItemCount() > 0) chooseVehicleSet_.remove(chooseVehicleSet_.getSelectedIndex());
			chooseVehicleSet_.addActionListener(this);
			clearVehicles();
		}
		else if(("addVehicleSetting").equals(command)){
			if(vehicleSetsModel_.getSize() == 1){
			}
			else{
				if(activeSeries_ == null){
					JOptionPane.showMessageDialog(null, Messages.getString("ResearchSeriesDialog.MsgBoxCreateSeries"), "Error", JOptionPane.ERROR_MESSAGE);
				}
				else if(chooseVehicleSet_.getSelectedIndex() == -1){
					JOptionPane.showMessageDialog(null, Messages.getString("ResearchSeriesDialog.MsgBoxCreateVehicleSet"), "Error", JOptionPane.ERROR_MESSAGE);
				}
				else hideResearchWindow(true, "vehicles", "");
			}
			
			
		}
		else if(("deleteVehicleSetting").equals(command)){
			if(vehicleSetsList_.getSelectedIndex() > -1){
				activeSeries_.removeProperty(((VehicleSet)vehicleSetsModel_.get(vehicleSetsList_.getSelectedIndex())).getName_());
				generalSettingsModel_.remove(generalSettingsList_.getSelectedIndex());	
			}
		}
		else if(("createScenarios").equals(command)){
			if(activeSeries_ == null){
				JOptionPane.showMessageDialog(null, Messages.getString("ResearchSeriesDialog.MsgBoxCreateSeries"), "Error", JOptionPane.ERROR_MESSAGE);
			}
			else if(chooseVehicleSet_.getSelectedIndex() == -1){
				JOptionPane.showMessageDialog(null, Messages.getString("ResearchSeriesDialog.MsgBoxCreateVehicleSet"), "Error", JOptionPane.ERROR_MESSAGE);
			}
			else if(Map.getInstance().getMapName_().equals("")){
				JOptionPane.showMessageDialog(null, Messages.getString("ResearchSeriesDialog.loadMapBeforeStarting"), "Error", JOptionPane.ERROR_MESSAGE);
			}
			else{
				for(SimulationSeries series:simulationSeriesList_){
					if(series.getPropertyList_().size() == 1){
						
						//get one item
						for(SimulationProperty simulationProperty:series.getPropertyList_()){
							for(int i = 0;i < simulationProperty.getStepAmount_();i++){
								solveRec(series.getVehicleSetList_(), simulationSeriesList_.indexOf(series) + ":" + series.getPropertyList_().indexOf(simulationProperty) + ":" + i + ":", 0);
							}
						}
					}
					else{
						solveRec(series.getVehicleSetList_(),  simulationSeriesList_.indexOf(series) + ":-1:-1:", 0);
					}
				}
				startJobs();
			}
		}
	}
	
	/**
	 * add a job to the queue (Format: SeriesIndex:GeneralPropertyIndex:GeneralPropertyAmountIndex:*VehiceSetIndex:VehicleSetPropertyIndex:VehicleSetPropertyAmountIndex*)
	 */
	public void addJob(String s){
		jobList_.add(s);
	}

	public void startJobs(){
		VanetSimStart.setProgressBar(true);
		ResearchSeriesDialog.getInstance().setVisible(false);
		//do the job
		Runnable job = new Runnable() {
			public void run() {
				ArrayList<String> jobList = new ArrayList<String>();
				long time = System.currentTimeMillis();
				double oldValue;
				String[] data;
				SimulationSeries series;
				SimulationProperty simulationProperty = null;
				for(String theJob:jobList_){
					for(int n = 0; n < ((Number)amountOfSimulationRuns_.getValue()).intValue(); n++){
						//clear old vehicles!
						Map.getInstance().clearVehicles();
						
						data = theJob.split(":");
						series = simulationSeriesList_.get(Integer.parseInt(data[0]));
						simulationProperty = null;
						
						oldValue = -1;
						if(!data[1].equals("-1")){
							simulationProperty = series.getPropertyList_().get(Integer.parseInt(data[1]));
							oldValue = 	getValueForKey(simulationProperty.getPropertyKey_());
							setValueForKey(simulationProperty.getPropertyKey_(), simulationProperty.getStartValue_() + simulationProperty.getStepValue_() * Integer.parseInt(data[2]));
						}
						for(int m = 0; m < series.getVehicleSetList_().size(); m++){
									int i, j, k, l = 0;
									int speedDeviation = 0;
									int maxX = Map.getInstance().getMapWidth();
									int maxY = Map.getInstance().getMapHeight();
									boolean wiFiEnabled;
									boolean emergencyEnabled;
									boolean fakingEnabled;
									ArrayDeque<WayPoint> destinations = null;
									Vehicle tmpVehicle;
									Random random = new Random();
									int tmpRandom = -1;
									VehicleSet vehicleSet = series.getVehicleSetList_().get(m);
									// create the random vehicles. It may fail lots of times if the map is almost empty. Then, possible less
									// vehicles are created than specified because it's only tried 4 x amountValue!
						
									int minSpeedValue = vehicleSet.getMinSpeed_();
									int maxSpeedValue = vehicleSet.getMaxSpeed_();
									int minCommDistValue = vehicleSet.getMinCommDist_();
									int maxCommDistValue = vehicleSet.getMaxCommDist_();
									int minWaitValue = vehicleSet.getMinWait_();
									int maxWaitValue = vehicleSet.getMaxWait_();
									int minBrakingValue = vehicleSet.getMinBraking_();
									int maxBrakingValue = vehicleSet.getMaxBraking_();
									int minAccelerationValue = vehicleSet.getMinAcceleration_();
									int maxAccelerationValue = vehicleSet.getMaxAcceleration_();
									int minTimeDistance = vehicleSet.getMinTimeDistance_();
									int maxTimeDistance = vehicleSet.getMaxTimeDistance_();
									int minPoliteness = vehicleSet.getMinPoliteness_();
									int maxPoliteness = vehicleSet.getMaxPoliteness_();
									int vehiclesDeviatingMaxSpeed = vehicleSet.getVehiclesDeviatingMaxSpeed_();
									int deviationFromSpeedLimit = vehicleSet.getDeviationFromSpeedLimit_();
									int wiFiValue = vehicleSet.getWiFi_();
									int emergencyValue = vehicleSet.getEmergencyVehicle_();
									int speedRestriction = vehicleSet.getSpeedStreetRestriction_();
									int vehiclesFaking = vehicleSet.getFakingVehicle_();
									int amountValue = vehicleSet.getAmount_();
									int vehicleLength = vehicleSet.getVehicleLength_();
									
									if(wiFiValue < 0){
										wiFiValue = 0;
									} else if(wiFiValue > 100){
										wiFiValue = 100;
									}
									if(emergencyValue < 0){
										emergencyValue = 0;
									} else if(emergencyValue > 100){
										emergencyValue = 100;
									}
									
									if(vehiclesFaking < 0){
										vehiclesFaking = 0;
									} else if(vehiclesFaking > 100){
										vehiclesFaking = 100;
									}
									
									if(vehicleSet.getPropertyList_() != null && vehicleSet.getPropertyList_().size() > 0 && vehicleSet.getPropertyList_().get(0) != null){		
										String propertyKey = vehicleSet.getPropertyList_().get(0).getPropertyKey_();
										double value = (vehicleSet.getPropertyList_().get(0).getStartValue_() + vehicleSet.getPropertyList_().get(0).getStepValue_() *(Integer.parseInt(data[5])*(m+1)));
										
										if(propertyKey.equals("speed")){
											minSpeedValue = (int)value;
											maxSpeedValue = (int)value;
										}
										else if(propertyKey.equals("communication distance")){
											minCommDistValue = (int)value;
											maxCommDistValue = (int)value;
										}else if(propertyKey.equals("wait time")){
											minWaitValue = (int)value;
											maxWaitValue = (int)value;
										}else if(propertyKey.equals("braking rate")){
											minBrakingValue = (int)value;
											maxBrakingValue = (int)value;
										}else if(propertyKey.equals("acceleration")){
											minAccelerationValue = (int)value;
											maxAccelerationValue = (int)value;
										}else if(propertyKey.equals("time distance")){
											minTimeDistance = (int)value;
											maxTimeDistance = (int)value;
										}else if(propertyKey.equals("politeness")){
											minPoliteness = (int)value;
											maxPoliteness = (int)value;
										}else if(propertyKey.equals("vehicles deviating speed")){
											vehiclesDeviatingMaxSpeed = (int)value;
										}else if(propertyKey.equals("speed deviation")){
											deviationFromSpeedLimit = (int)value;
										}else if(propertyKey.equals("length")){
											vehicleLength = (int)value;
										}else if(propertyKey.equals("wifi amount")){
											wiFiValue = (int)value;
										}else if(propertyKey.equals("emergency amount")){
											emergencyValue = (int)value;
										}else if(propertyKey.equals("faking amount")){
											vehiclesFaking = (int)value;
										}else if(propertyKey.equals("amount")){
											amountValue = (int)value;
										}		
									}
									for(i = 0; i < amountValue;){
										j = 0;
										k = 0;						
										++l;
										destinations = new ArrayDeque<WayPoint>(2);		
										while(j < 2 && k < 20){	// if snapping fails more than 20 times break
											try{
												++k;
												WayPoint tmpWayPoint = new WayPoint(random.nextInt(maxX),random.nextInt(maxY),getRandomRange(minWaitValue, maxWaitValue, random));
												if(tmpWayPoint.getStreet().getSpeed() <= speedRestriction){
													destinations.add(tmpWayPoint);
													++j;
												}
											} catch (Exception e) {}
										}
										if(k < 20) {
											try {
												tmpRandom = getRandomRange(1, 100, random);
												if(tmpRandom <= vehiclesDeviatingMaxSpeed) speedDeviation = getRandomRange(-deviationFromSpeedLimit, deviationFromSpeedLimit, random);
												else speedDeviation = 0;
												if(getRandomRange(0, 99, random) < wiFiValue) wiFiEnabled = true;
												else wiFiEnabled = false;
												if(getRandomRange(0, 99, random) < emergencyValue) emergencyEnabled = true;
												else emergencyEnabled = false;
												if(getRandomRange(0, 99, random) < vehiclesFaking) fakingEnabled = true;
												else fakingEnabled = false;
												tmpVehicle = new Vehicle(destinations, vehicleLength, getRandomRange(minSpeedValue, maxSpeedValue, random), getRandomRange(minCommDistValue, maxCommDistValue, random), wiFiEnabled, emergencyEnabled, getRandomRange(minBrakingValue, maxBrakingValue, random), getRandomRange(minAccelerationValue, maxAccelerationValue, random), getRandomRange(minTimeDistance, maxTimeDistance, random), getRandomRange(minPoliteness, maxPoliteness, random), (int)Math.round(speedDeviation * 100000.0/3600),  vehicleSet.getColor_(), fakingEnabled, vehicleSet.getFakeMessagesTypes_());
												Map.getInstance().addVehicle(tmpVehicle);
												++i;
											} catch (Exception e) {}				
										}
										if(l > amountValue*4) break;
									}
									int errorLevel = 2;
									if(i < vehicleSet.getAmount_()) errorLevel = 6;
									ErrorLog.log(Messages.getString("EditVehicleControlPanel.createdRandomVehicles") + i + " (" + vehicleSet.getAmount_() +Messages.getString("EditVehicleControlPanel.requested"), errorLevel, getClass().getName(), "actionPerformed", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
									
								}
						
								String fileName = "";
								if(Map.getInstance().getMapName_().length() > 3) fileName += Map.getInstance().getMapName_().substring(0, Map.getInstance().getMapName_().length()-4);
								fileName += "_" + series.getName_();
								if(simulationProperty != null) fileName += "_" + simulationProperty.getPropertyKey_() + "_" + (simulationProperty.getStartValue_() + simulationProperty.getStepValue_() * Integer.parseInt(data[2]));
								
								for(int o = 0; o < series.getVehicleSetList_().size(); o++) {
									if(series.getVehicleSetList_().get(o).getPropertyList_().size() > 0 && series.getVehicleSetList_().get(o).getPropertyList_().get(0) != null) fileName += "_" + series.getVehicleSetList_().get(o).getName_() + "_" + series.getVehicleSetList_().get(o).getPropertyList_().get(0).getPropertyKey_() + "_" + (series.getVehicleSetList_().get(o).getPropertyList_().get(0).getStartValue_() + series.getVehicleSetList_().get(o).getPropertyList_().get(0).getStepValue_() * (Integer.parseInt(data[5])*(o+1)));
									else fileName += "_" + series.getVehicleSetList_().get(o).getName_() + "_standard";
								}
								//save the to file
								File file = new File(System.getProperty("user.dir") + "/" + time + "_scenarios/");
								if(!file.exists())file.mkdir();
								jobList.add(time + "_scenarios/" + fileName + "_v" + n +  ".xml");
								Scenario.getInstance().save(new File((System.getProperty("user.dir") + "/" + time + "_scenarios/" + fileName + "_v" + n +  ".xml").replace(" ", "")), false);
								
								if(oldValue != -1 && simulationProperty != null) setValueForKey(simulationProperty.getPropertyKey_(), oldValue);

							}
						}
						String writeToFileText = "#!/bin/sh\n";
						for(String line:jobList) writeToFileText += "java -Xmx" + availableMemory_ + "m -jar VanetSimStarter.jar " + Map.getInstance().getMapName_() + " " + line.replace(" ", "") + " " + simulationDuration_ + "\n";
						writeToFile(writeToFileText, System.getProperty("user.dir") + "/" + time + "_scenarios/jobs.sh");
						ResearchSeriesDialog.getInstance().setVisible(true);
						VanetSimStart.setProgressBar(false);
						Renderer.getInstance().ReRender(false, false);
						jobList_.clear();
					}
			
		};
		new Thread(job).start();
	}
	
	public void solveRec(ArrayList<VehicleSet> vehicleSets, String s, int index){
		if(vehicleSets.size() == index){
			addJob(s);
			//and end this madness
			
		}
		else{
			if(vehicleSets.get(index).getPropertyList_().size() == 0) solveRec(vehicleSets, s + vehicleSets.get(index).getName_() + ":" + -1 + ":" +  1 + ":", (index+1));
			else{
				for(int i = 0; i < vehicleSets.get(index).getPropertyList_().size(); i++){
					for(int j = 0; j < vehicleSets.get(index).getPropertyList_().get(i).getStepAmount_(); j++){
						solveRec(vehicleSets, s + vehicleSets.get(index).getName_() + ":" + i + ":" +  j + ":", (index+1));
					}
				}
			}
		}
	}
	
	public void copySeries(SimulationSeries source){
		String str = JOptionPane.showInputDialog(null, Messages.getString("ResearchSeriesDialog.inputMessage"), Messages.getString("ResearchSeriesDialog.inputMessageTitle"), 1);
		if(str != null){
			SimulationSeries newSeries = new SimulationSeries(str);
			simulationSeriesList_.add(newSeries);	
			
			ArrayList<SimulationProperty> newGeneralPropertyList = new ArrayList<SimulationProperty>();
			for(SimulationProperty simPro:source.getPropertyList_()){
				newGeneralPropertyList.add(new SimulationProperty(simPro.getPropertyKey_(), simPro.getStartValue_(), simPro.getStepValue_(), simPro.getStepAmount_()));
			}
			
			ArrayList<VehicleSet> newVehicleSetList = new ArrayList<VehicleSet>();
			for(VehicleSet vehicleSet:source.getVehicleSetList_()){
				VehicleSet newVehicleSet = new VehicleSet(vehicleSet.getName_());
				newVehicleSetList.add(newVehicleSet);
				
				newVehicleSet.setData(vehicleSet.getVehicleLength_(), vehicleSet.getMinSpeed_(), vehicleSet.getMaxSpeed_(), vehicleSet.getMinCommDist_(), vehicleSet.getMaxCommDist_(), vehicleSet.getMinWait_(), vehicleSet.getMaxWait_(), vehicleSet.getMinBraking_(), vehicleSet.getMaxBraking_(), vehicleSet.getMinAcceleration_(), vehicleSet.getMaxAcceleration_(), vehicleSet.getMinTimeDistance_(), vehicleSet.getMaxTimeDistance_(), vehicleSet.getMinPoliteness_(), vehicleSet.getMaxPoliteness_(), vehicleSet.getWiFi_(), vehicleSet.getEmergencyVehicle_(), vehicleSet.getFakingVehicle_(), vehicleSet.getFakeMessagesTypes_(), vehicleSet.getAmount_(), vehicleSet.getSpeedStreetRestriction_(), vehicleSet.getVehiclesDeviatingMaxSpeed_(), vehicleSet.getDeviationFromSpeedLimit_(), vehicleSet.getColor_());
			
				ArrayList<SimulationProperty> newVehiclePropertyList = new ArrayList<SimulationProperty>();
				for(SimulationProperty simPro:vehicleSet.getPropertyList_()){
					newVehiclePropertyList.add(new SimulationProperty(simPro.getPropertyKey_(), simPro.getStartValue_(), simPro.getStepValue_(), simPro.getStepAmount_()));
				}
				newVehicleSet.setPropertyList_(newVehiclePropertyList);
			}
			
			newSeries.setPropertyList_(newGeneralPropertyList);
			newSeries.setVehicleSetList_(newVehicleSetList);
			
			chooseSeries_.addItem(newSeries);
			activeSeries_ = newSeries;
			
			clearGui();
			updateSeries();
		}

	}

	public void setValueForKey(String key, double value){
		if("fake message interval".equals(key)){
			Vehicle.setFakeMessagesInterval_((int)value);
		}else if("PCN threshold".equals(key)){
			IDSProcessor.setPCNDistance_((int)value);
		}else if("PCN forward threshold".equals(key)){
			IDSProcessor.setPCNFORWARDThreshold_(value);
		}else if("EVA forward threshold".equals(key)){
			IDSProcessor.setEVAFORWARDThreshold_(value);
		}else if("RHCN threshold".equals(key)){
			IDSProcessor.setRHCNThreshold_(value);
		}else if("EEBL threshold".equals(key)){
			IDSProcessor.setEEBLThreshold_(value);
		}else if("EVA Beacon time".equals(key)){
			IDSProcessor.setEVABeaconTimeFactor_(value);
		}else if("EVA Beacon threshold".equals(key)){
			IDSProcessor.setEVABeaconFactor_(value);
		}else if("Beacon amount".equals(key)){
			KnownVehicle.setAmountOfSavedBeacons((int)value);
		}else if("spam time threshold".equals(key)){
			KnownEventSource.setSpammingTimeThreshold_((int)value);
		}else if("spam message threshold".equals(key)){
			KnownEventSource.setSpammingThreshold_((int)value);
		}else if("EVA Message Delay".equals(key)){
			Vehicle.setMaxEVAMessageDelay_((int)value);
		}
		
	}
	
	public double getValueForKey(String key){
		if("fake message interval".equals(key)){
			return Vehicle.getFakeMessagesInterval_();
		}else if("PCN threshold".equals(key)){
			return IDSProcessor.getPCNDistance_();
		}else if("PCN forward threshold".equals(key)){
			return IDSProcessor.getPCNFORWARDThreshold_();
		}else if("EVA forward threshold".equals(key)){
			return IDSProcessor.getEVAFORWARDThreshold_();
		}else if("RHCN threshold".equals(key)){
			return IDSProcessor.getRHCNThreshold_();
		}else if("EEBL threshold".equals(key)){
			return IDSProcessor.getEEBLThreshold_();
		}else if("EVA Beacon time".equals(key)){
			return IDSProcessor.getEVABeaconTimeFactor_();
		}else if("EVA Beacon threshold".equals(key)){
			return IDSProcessor.getEVABeaconFactor_();
		}else if("Beacon amount".equals(key)){
			return KnownVehicle.getAmountOfSavedBeacons_();
		}else if("spam time threshold".equals(key)){
			return KnownEventSource.getSpammingtimethreshold();
		}else if("spam message threshold".equals(key)){
			return KnownEventSource.getSpammingthreshold();
		}else if("EVA Message Delay".equals(key)){
			return Vehicle.getMaxEVAMessageDelay_();
		}
		
		 return -1;
	}
	
	/**
	 * Hide this windows and load simulator in select scenario options mode
	 */
	public void hideResearchWindow(boolean hide, String mode, String command){
		if(hide){
			this.setVisible(false);
		    VanetSimStart.getMainControlPanel().activateEditPane();
		    VanetSimStart.getMainControlPanel().getEditPanel().getEnableEdit_().setSelected(true);
		    VanetSimStart.getMainControlPanel().getEditPanel().actionPerformed(new ActionEvent(VanetSimStart.getMainControlPanel().getEditPanel().getEnableEdit_(), 0, VanetSimStart.getMainControlPanel().getEditPanel().getEnableEdit_().getActionCommand()));

		    if(mode.equals("vehicles")){
			    VanetSimStart.getMainControlPanel().getEditPanel().getEditChoice_().setSelectedIndex(3);
			    EditVehicleControlPanel.activateSelectPropertiesMode(true);
			    VanetSimStart.getMainControlPanel().resizeSideBar(true);
		    }
		    else if(mode.equals("allVehicleProperties")){
			    VanetSimStart.getMainControlPanel().getEditPanel().getEditChoice_().setSelectedIndex(3);
			    EditVehicleControlPanel.activateSelectAllPropertiesMode(true);
		    }
		    else if(mode.equals("generalSettings")){
			    VanetSimStart.getMainControlPanel().getEditPanel().getEditChoice_().setSelectedIndex(8);
			    EditIDSControlPanel.activateSelectPropertiesMode(true);
			    VanetSimStart.getMainControlPanel().resizeSideBar(true);
		    }
		    
		}
		else{
		    if(mode.equals("vehicles")){
			    EditVehicleControlPanel.activateSelectPropertiesMode(false);
			    new ResearchSeriesHelperDialog("vehicles", command);
		    }
		    else if(mode.equals("allVehicleProperties")){
			    EditVehicleControlPanel.activateSelectAllPropertiesMode(false);
		    }
		    else if(mode.equals("generalSettings")){
		    	EditIDSControlPanel.activateSelectPropertiesMode(false);
		    	 new ResearchSeriesHelperDialog("generalSettings", command);
		    }
		    updateSeries();
		    
		    VanetSimStart.getMainControlPanel().resizeSideBar(false);
		}
	}
	
	/**
	 * Methode is evoked when closing JDialog
	 */
	public void closeDialog(){
		//close JDialog
		this.dispose();
	}	
	

	public void windowActivated(WindowEvent e) {}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public boolean isSelectPropertiesModeOn_() {
		return selectPropertiesModeOn_;
	}

	public void setSelectPropertiesModeOn_(boolean selectPropertiesModeOn_) {
		this.selectPropertiesModeOn_ = selectPropertiesModeOn_;
	}


	public JComboBox<Object> getChooseSeries_() {
		return chooseSeries_;
	}

	public void setChooseSeries_(JComboBox<Object> chooseSeries_) {
		this.chooseSeries_ = chooseSeries_;
	}

	public JComboBox<Object> getChooseVehicleSet_() {
		return chooseVehicleSet_;
	}

	public void setChooseVehicleSet_(JComboBox<Object> chooseVehicleSet_) {
		this.chooseVehicleSet_ = chooseVehicleSet_;
	}

	public void loadNewSeries(SimulationSeries series){
		
		if(series == null) clearGui();
		else{
			chooseSeries_.removeActionListener(this);
			chooseSeries_.setSelectedItem(series);
			chooseSeries_.addActionListener(this);
			
			activeVehicleSet_ = (VehicleSet)chooseVehicleSet_.getSelectedItem();
			chooseVehicleSet_.removeActionListener(this);
			chooseVehicleSet_.removeAllItems();
			for(VehicleSet vSet: series.getVehicleSetList_()){
				chooseVehicleSet_.addItem(vSet);
			}
			if(activeVehicleSet_ != null)chooseVehicleSet_.setSelectedItem(activeVehicleSet_);
			chooseVehicleSet_.addActionListener(this);
			
			generalSettingsModel_.removeAllElements();
			if(series.getPropertyList_() != null && series.getPropertyList_().size() > 0)for(SimulationProperty sprop: series.getPropertyList_())generalSettingsModel_.addElement(sprop);
			
			vehicleSetsModel_.removeAllElements();
			if(chooseVehicleSet_.getSelectedItem() != null && ((VehicleSet)chooseVehicleSet_.getSelectedItem()).getPropertyList_() != null && ((VehicleSet)chooseVehicleSet_.getSelectedItem()).getPropertyList_().size() > 0)for(SimulationProperty sprop: ((VehicleSet)chooseVehicleSet_.getSelectedItem()).getPropertyList_())vehicleSetsModel_.addElement(sprop);
		}

	}
	
	/**
	 * Gets an integer in the range between <code>min</code> and <code>max</code> (including both!). If you don't put the bigger variable in 
	 * the <code>min</code>, the variables will be automatically swapped.
	 * 
	 * @param min		the first integer (lower limit)
	 * @param max		the second integer (upper limit)
	 * @param random	the random number generator
	 * 
	 * @return the random range
	 */
	private int getRandomRange(int min, int max, Random random){
		if(min == max) return min;
		else {
			if(max < min){	//swap to make sure that smallest value is in min if wrong values were passed
				int tmp = max;
				max = min;
				min = tmp;
			}
			return (random.nextInt(max - min + 1) + min);
		}
	}
	
	public void clearGui(){
	
		chooseVehicleSet_.removeActionListener(this);
		chooseVehicleSet_.removeAllItems();
		chooseVehicleSet_.addActionListener(this);
		
		generalSettingsModel_.removeAllElements();
		
		vehicleSetsModel_.removeAllElements();
	}

	public void updateSeries(){
		activeVehicleSet_ = null;
		activeSeries_ = null;
		chooseSeries_.removeActionListener(this);
		chooseSeries_.removeAllItems();
		for(SimulationSeries s:simulationSeriesList_){
			chooseSeries_.addItem(s);
			activeSeries_ = s;
		}
		chooseSeries_.addActionListener(this);
		
		if(activeSeries_ != null) loadNewSeries(activeSeries_);
	}
	
	/**
	 * Writes any data to any filepath (used to save data after simulation end)
	 */
	public void writeToFile(String text, String filePath){
		
		
		System.out.println("writing file...");
		System.out.println(filePath);
		try {
			
			BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
			if(text != null){
				out.write(text);
				out.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();		
		}
	}
	public void clearVehicles(){
		vehicleSetsModel_.removeAllElements();
	}

	/**
	 * @return the simulationSeriesList_
	 */
	public ArrayList<SimulationSeries> getSimulationSeriesList_() {
		return simulationSeriesList_;
	}

	/**
	 * @param simulationSeriesList_ the simulationSeriesList_ to set
	 */
	public void setSimulationSeriesList_(
			ArrayList<SimulationSeries> simulationSeriesList_) {
		this.simulationSeriesList_ = simulationSeriesList_;
	}

	/**
	 * @return the activeSeries_
	 */
	public SimulationSeries getActiveSeries_() {
		return activeSeries_;
	}

	/**
	 * @param activeSeries_ the activeSeries_ to set
	 */
	public void setActiveSeries_(SimulationSeries activeSeries_) {
		this.activeSeries_ = activeSeries_;
	}

	/**
	 * @return the activeVehicleSet_
	 */
	public VehicleSet getActiveVehicleSet_() {
		return activeVehicleSet_;
	}

	/**
	 * @param activeVehicleSet_ the activeVehicleSet_ to set
	 */
	public void setActiveVehicleSet_(VehicleSet activeVehicleSet_) {
		this.activeVehicleSet_ = activeVehicleSet_;
	}
}