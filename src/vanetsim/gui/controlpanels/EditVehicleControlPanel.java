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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;

import java.util.ArrayDeque;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import vanetsim.ErrorLog;
import vanetsim.VanetSimStart;
import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.gui.helpers.TextAreaLabel;
import vanetsim.gui.helpers.VehicleType;
import vanetsim.gui.helpers.VehicleTypeXML;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.routing.WayPoint;
import vanetsim.scenario.IDSProcessor;
import vanetsim.scenario.Vehicle;

/**
 * This class represents the control panel for adding random vehicles.
 */
public class EditVehicleControlPanel extends JPanel implements ActionListener, MouseListener{
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = 1347869556374738481L;
	
	/** A JComboBox Label for vehicle type. */
	private JLabel chooseVehicleTypeLabel_;
		
	/** A JComboBox to switch between vehicles types. */
	private JComboBox<VehicleType> chooseVehicleType_;	
	
	/** The input field for the vehicle length (cm) */
	private final JFormattedTextField vehicleLength_;
	
	/** The input field for the minimum speed. */
	private final JFormattedTextField minSpeed_;
	
	/** The input field for the maximum speed. */
	private final JFormattedTextField maxSpeed_;
	
	/** The input field for the minimum communication distance. */
	private final JFormattedTextField minCommDist_;
	
	/** The input field for the maximum communication distance.. */
	private final JFormattedTextField maxCommDist_;
	
	/** The input field for the minimum wait in milliseconds. */
	private final JFormattedTextField minWait_;
	
	/** The input field for the maximum wait in milliseconds. */
	private final JFormattedTextField maxWait_;
	
	/** The input field for the minimum braking rate in cm/s^2. */
	private final JFormattedTextField minBraking_;
	
	/** The input field for the maximum braking rate in cm/s^2. */
	private final JFormattedTextField maxBraking_;	
	
	/** The input field for the minimum acceleration rate in cm/s^2. */
	private final JFormattedTextField minAcceleration_;
	
	/** The input field for the maximum acceleration rate in cm/s^2. */
	private final JFormattedTextField maxAcceleration_;	
	
	/** The input field for the minimum time distance in ms. */
	private final JFormattedTextField minTimeDistance_;	
	
	/** The input field for the maximum time distance in ms. */
	private final JFormattedTextField maxTimeDistance_;	

	/** The input field for the minimum politeness factor in %. */
	private final JFormattedTextField minPoliteness_;	
	
	/** The input field for the maximum politeness factor in %. */
	private final JFormattedTextField maxPoliteness_;	
	
	/** The input field for the percentage of vehicles with WiFi. */
	private final JFormattedTextField wiFi_;
	
	/** The input field for the percentage of emergency vehicles. */
	private final JFormattedTextField emergencyVehicle_;
	
	/** The input field for the percentage of vehicles faking messages */
	private final JFormattedTextField fakingVehicle_;
	
	/** A JComboBox to switch between fake messages types. */
	private JComboBox<String> fakeMessagesTypes_;
	
	/** The input field for the amount of vehicles to be created. */
	private final JFormattedTextField amount_;
	
	/** The input field for a restriction that source an destination may only be on specific streets. */
	private final JFormattedTextField speedStreetRestriction_;
	
	/** The input field for how much vehicles deviate from the max. speed limit. */
	private final JFormattedTextField vehiclesDeviatingMaxSpeed_;
	
	/** The input field for the number of km/h the vehicles will deviate from the max. speed limit. */
	private final JFormattedTextField deviationFromSpeedLimit_;
	
	/** The input field for the wait in milliseconds. */
	private final JPanel colorPreview_;	
	
	/** the create button */
	private static JButton createButton_;
	
	/** the delete button */
	private static JButton deleteButton_;
	
	/** the delete button */
	private static JButton scenarioApplyButton_;

	/** a ArrayList of all buttons of the selectproperty mode */
	private static ArrayList<JButton> buttonList_ = new ArrayList<JButton>();
	/**
	 * Constructor.
	 */
	public EditVehicleControlPanel(){
		setLayout(new GridBagLayout());
		
		// global layout settings
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.insets = new Insets(5,5,5,5);
		
		//add vehicle types comboBox
		chooseVehicleTypeLabel_ = new JLabel(Messages.getString("EditOneVehicleControlPanel.selectVehicleType")); //$NON-NLS-1$
		++c.gridy;
		add(chooseVehicleTypeLabel_,c);
		chooseVehicleType_ = new JComboBox<VehicleType>();
		chooseVehicleType_.setName("chooseVehicleType");
		//load vehicle types from vehicleTypes.xml into JCombobox 
		refreshVehicleTypes();
		
		chooseVehicleType_.addActionListener(this);
		c.gridx = 1;
		add(chooseVehicleType_, c);

		//add vehicle properties
		c.gridx = 0;
		JLabel jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.minSpeed")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		minSpeed_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		minSpeed_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(minSpeed_,c);
		c.gridx = 2;
		
		c.gridheight = 2;
		JButton button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("speed");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		c.gridheight = 1;
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.maxSpeed")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		maxSpeed_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		maxSpeed_.setPreferredSize(new Dimension(60,20));;
		c.gridx = 1;
		add(maxSpeed_,c);
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.minCommDistance")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		minCommDist_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		minCommDist_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(minCommDist_,c);
		
		c.gridx = 2;
		c.gridheight = 2;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("communication distance");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		c.gridheight = 1;
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.maxCommDistance")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		maxCommDist_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		maxCommDist_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(maxCommDist_,c);
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.minWaittime")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		minWait_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		minWait_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(minWait_,c);
		
		c.gridx = 2;
		c.gridheight = 2;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("wait time");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		c.gridheight = 1;
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.maxWaittime")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		maxWait_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		maxWait_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(maxWait_,c);
		
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.minBraking_rate")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		minBraking_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		minBraking_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(minBraking_,c);
		
		c.gridx = 2;
		c.gridheight = 2;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("braking rate");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		c.gridheight = 1;
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.maxBraking_rate")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		maxBraking_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		maxBraking_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(maxBraking_,c);
		
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.minAcceleration_rate")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		minAcceleration_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		minAcceleration_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(minAcceleration_,c);
		
		c.gridx = 2;
		c.gridheight = 2;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("acceleration");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		c.gridheight = 1;
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.maxAcceleration_rate")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		maxAcceleration_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		maxAcceleration_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(maxAcceleration_,c);
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.minTimeDistance")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		minTimeDistance_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		minTimeDistance_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(minTimeDistance_,c);
		
		c.gridx = 2;
		c.gridheight = 2;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("time distance");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		c.gridheight = 1;
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.maxTimeDistance")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		maxTimeDistance_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		maxTimeDistance_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(maxTimeDistance_,c);
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.minPoliteness")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		minPoliteness_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		minPoliteness_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(minPoliteness_,c);
		
		c.gridx = 2;
		c.gridheight = 2;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("politeness");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		c.gridheight = 1;
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.maxPoliteness")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		maxPoliteness_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		maxPoliteness_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(maxPoliteness_,c);
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.vehiclesDeviatingMaxSpeed")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		vehiclesDeviatingMaxSpeed_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		vehiclesDeviatingMaxSpeed_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(vehiclesDeviatingMaxSpeed_,c);
		
		c.gridx = 2;
		c.gridheight = 1;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("vehicles deviating speed");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		c.gridheight = 1;
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.deviationFromSpeedLimit")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		deviationFromSpeedLimit_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		deviationFromSpeedLimit_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(deviationFromSpeedLimit_,c);
		
		c.gridx = 2;
		c.gridheight = 1;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("speed deviation");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		c.gridheight = 1;
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.vehicleLength")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		vehicleLength_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		vehicleLength_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(vehicleLength_,c);
		
		c.gridx = 2;
		c.gridheight = 1;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("length");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		c.gridheight = 1;
		
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.wiFiVehicles")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		wiFi_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		wiFi_.setPreferredSize(new Dimension(60,20));
		wiFi_.setValue(100);
		c.gridx = 1;
		add(wiFi_,c);
		
		c.gridx = 2;
		c.gridheight = 1;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("wifi amount");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		c.gridheight = 1;
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.emergencyVehicles")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		emergencyVehicle_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		emergencyVehicle_.setPreferredSize(new Dimension(60,20));
		emergencyVehicle_.setValue(0);
		c.gridx = 1;
		add(emergencyVehicle_,c);
		
		c.gridx = 2;
		c.gridheight = 1;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("emergency amount");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		c.gridheight = 1;
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.fakingVehicle")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		fakingVehicle_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		fakingVehicle_.setPreferredSize(new Dimension(60,20));
		fakingVehicle_.setValue(0);
		c.gridx = 1;
		add(fakingVehicle_,c);
		
		c.gridx = 2;
		c.gridheight = 1;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("faking amount");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		c.gridheight = 1;

		//add vehicle types comboBox
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.selectFakeMessageType")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);
		fakeMessagesTypes_ = new JComboBox<String>();
		fakeMessagesTypes_.setName("fakeMessagesTypes");

		fakeMessagesTypes_.addItem(Messages.getString("EditVehicleControlPanel.all"));
		for(int i = 0; i < IDSProcessor.getIdsData_().length; i++) if(!IDSProcessor.getIdsData_()[i].equals("PCN_FORWARD"))fakeMessagesTypes_.addItem(IDSProcessor.getIdsData_()[i]);
		
		c.gridx = 1;
		add(fakeMessagesTypes_, c);
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.amount")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);		
		amount_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		amount_.setPreferredSize(new Dimension(60,20));
		amount_.setValue(100);
		c.gridx = 1;
		add(amount_,c);	
		
		c.gridx = 2;
		c.gridheight = 1;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("amount");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		c.gridheight = 1;
		
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditOneVehicleControlPanel.color")); //$NON-NLS-1$
		++c.gridy;
		add(jLabel1,c);
		colorPreview_ = new JPanel();	
		colorPreview_.setBackground(Color.black);
		
		colorPreview_.setSize(10, 10);
		colorPreview_.addMouseListener(this);
		c.gridx = 1;
		add(colorPreview_,c);
		
		
		c.gridx = 0;
		jLabel1 = new JLabel("<html>" + Messages.getString("EditVehicleControlPanel.onlyOnLowerSpeedStreets")); //$NON-NLS-1$ //$NON-NLS-2$
		++c.gridy;
		add(jLabel1,c);		
		speedStreetRestriction_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		speedStreetRestriction_.setPreferredSize(new Dimension(60,20));
		speedStreetRestriction_.setValue(80);
		c.gridx = 1;
		add(speedStreetRestriction_,c);
		
		c.gridx = 0;
		c.gridwidth = 2;
		++c.gridy;
		createButton_ = ButtonCreator.getJButton("randomVehicles.png", "createRandom", Messages.getString("EditVehicleControlPanel.createRandom"), this);
		add(createButton_,c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		c.gridx = 0;
		c.gridwidth = 2;
		++c.gridy;
		deleteButton_ = ButtonCreator.getJButton("deleteAll.png", "clearVehicles", Messages.getString("EditVehicleControlPanel.btnClearVehicles"), this);
		add(deleteButton_,c);

		
		c.gridx = 0;
		scenarioApplyButton_ = new JButton(Messages.getString("EditVehicleControlPanel.apply"));
		scenarioApplyButton_.setActionCommand("applyToScenarioCreator");
		scenarioApplyButton_.addActionListener(this);
		add(scenarioApplyButton_,c);
		c.gridheight = 1;
		
		c.gridx = 0;
		++c.gridy;
		add(ButtonCreator.getJButton("openTypeDialog.png", "openTypeDialog", Messages.getString("EditControlPanel.openTypeDialog"), this), c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		c.gridx = 0;
		++c.gridy;
		
		TextAreaLabel jlabel1 = new TextAreaLabel(Messages.getString("EditVehicleControlPanel.note")); //$NON-NLS-1$
		++c.gridy;
		c.gridx = 0;
		c.gridwidth = 2;
		add(jlabel1, c);
		
		//to consume the rest of the space
		c.weighty = 1.0;
		++c.gridy;
		JPanel space = new JPanel();
		space.setOpaque(false);
		add(space, c);
		
		//updates the input fields to the first vehicle type
		actionPerformed(new ActionEvent(chooseVehicleType_,0,"comboBoxChanged"));
	}
	
	/**
	 * An implemented <code>ActionListener</code> which performs all needed actions when a <code>JButton</code>
	 * is clicked.
	 * 
	 * @param e	an <code>ActionEvent</code>
	 */	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if("createRandom".equals(command)){ //$NON-NLS-1$	
			Renderer.getInstance().setShowVehicles(true);
			Runnable job = new Runnable() {
				public void run() {
					int i, j, k, l = 0;
					VanetSimStart.setProgressBar(true);
					int maxX = Map.getInstance().getMapWidth();
					int maxY = Map.getInstance().getMapHeight();
					int minSpeedValue = (int)Math.round(((Number)minSpeed_.getValue()).intValue() * 100000.0/3600);
					int maxSpeedValue = (int)Math.round(((Number)maxSpeed_.getValue()).intValue() * 100000.0/3600);
					int minCommDistValue = ((Number)minCommDist_.getValue()).intValue()*100;
					int maxCommDistValue = ((Number)maxCommDist_.getValue()).intValue()*100;
					int minWaitValue = ((Number)minWait_.getValue()).intValue();
					int maxWaitValue = ((Number)maxWait_.getValue()).intValue();
					int minBrakingValue = ((Number)minBraking_.getValue()).intValue();
					int maxBrakingValue = ((Number)maxBraking_.getValue()).intValue();
					int minAccelerationValue = ((Number)minAcceleration_.getValue()).intValue();
					int maxAccelerationValue = ((Number)maxAcceleration_.getValue()).intValue();
					int minTimeDistance = ((Number)minTimeDistance_.getValue()).intValue();
					int maxTimeDistance = ((Number)maxTimeDistance_.getValue()).intValue();
					int minPoliteness = ((Number)minPoliteness_.getValue()).intValue();
					int maxPoliteness = ((Number)maxPoliteness_.getValue()).intValue();
					int vehiclesDeviatingMaxSpeed = ((Number)vehiclesDeviatingMaxSpeed_.getValue()).intValue();
					int deviationFromSpeedLimit = ((Number)deviationFromSpeedLimit_.getValue()).intValue();
					//int deviationFromSpeedLimit = (int)Math.round(((Number)deviationFromSpeedLimit_.getValue()).intValue() * 100000.0/3600);
					int speedDeviation = 0;
					int wiFiValue = ((Number)wiFi_.getValue()).intValue();
					int emergencyValue = ((Number)emergencyVehicle_.getValue()).intValue();
					int speedRestriction = (int)Math.round(((Number)speedStreetRestriction_.getValue()).intValue() * 100000.0/3600);
					int vehiclesFaking = ((Number)fakingVehicle_.getValue()).intValue();

					if(wiFiValue < 0){
						wiFiValue = 0;
						wiFi_.setValue(0);
					} else if(wiFiValue > 100){
						wiFiValue = 100;
						wiFi_.setValue(100);
					}
					if(emergencyValue < 0){
						emergencyValue = 0;
						emergencyVehicle_.setValue(0);
					} else if(emergencyValue > 100){
						emergencyValue = 100;
						emergencyVehicle_.setValue(100);
					}
					
					if(vehiclesFaking < 0){
						vehiclesFaking = 0;
						fakingVehicle_.setValue(0);
					} else if(vehiclesFaking > 100){
						vehiclesFaking = 100;
						fakingVehicle_.setValue(100);
					}
					
					int amountValue = ((Number)amount_.getValue()).intValue();
					
					boolean wiFiEnabled;
					boolean emergencyEnabled;
					boolean fakingEnabled;
					ArrayDeque<WayPoint> destinations = null;
					Vehicle tmpVehicle;
					Random random = new Random();
					int tmpRandom = -1;
					
					// create the random vehicles. It may fail lots of times if the map is almost empty. Then, possible less
					// vehicles are created than specified because it's only tried 4 x amountValue!
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
								tmpVehicle = new Vehicle(destinations, ((Number)vehicleLength_.getValue()).intValue(), getRandomRange(minSpeedValue, maxSpeedValue, random), getRandomRange(minCommDistValue, maxCommDistValue, random), wiFiEnabled, emergencyEnabled, getRandomRange(minBrakingValue, maxBrakingValue, random), getRandomRange(minAccelerationValue, maxAccelerationValue, random), getRandomRange(minTimeDistance, maxTimeDistance, random), getRandomRange(minPoliteness, maxPoliteness, random), (int)Math.round(speedDeviation * 100000.0/3600),  colorPreview_.getBackground(), fakingEnabled, fakeMessagesTypes_.getSelectedItem().toString());
								Map.getInstance().addVehicle(tmpVehicle);
								++i;
							} catch (Exception e) {}				
						}
						if(l > amountValue*4) break;
					}
					int errorLevel = 2;
					if(i < amountValue) errorLevel = 6;
					ErrorLog.log(Messages.getString("EditVehicleControlPanel.createdRandomVehicles") + i + " (" + amountValue +Messages.getString("EditVehicleControlPanel.requested"), errorLevel, getClass().getName(), "actionPerformed", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					VanetSimStart.setProgressBar(false);
					Renderer.getInstance().ReRender(false, false);
				}
			};
			new Thread(job).start();
			
		}
		//update GUI when vehicle type is selected
		else if ("comboBoxChanged".equals(command)){
			if(((Component) e.getSource()).getName().equals("chooseVehicleType")){
				VehicleType tmpVehicleType = (VehicleType) chooseVehicleType_.getSelectedItem();
				
				if(tmpVehicleType != null){
					maxSpeed_.setValue((int)Math.round(tmpVehicleType.getMaxSpeed() / (100000.0/3600)));
					vehicleLength_.setValue(tmpVehicleType.getVehicleLength());
					maxCommDist_.setValue((int)Math.round(tmpVehicleType.getMaxCommDist() / 100));
					maxWait_.setValue((int)tmpVehicleType.getMaxWaittime());
					maxBraking_.setValue((int)tmpVehicleType.getMaxBrakingRate());
					maxAcceleration_.setValue((int)tmpVehicleType.getMaxAccelerationRate());
					maxTimeDistance_.setValue((int)tmpVehicleType.getMaxTimeDistance());
					maxPoliteness_.setValue((int)tmpVehicleType.getMaxPoliteness());
					minSpeed_.setValue((int)Math.round(tmpVehicleType.getMinSpeed() / (100000.0/3600)));
					minCommDist_.setValue((int)Math.round(tmpVehicleType.getMinCommDist() / 100));
					minWait_.setValue((int)tmpVehicleType.getMinWaittime());
					minBraking_.setValue((int)tmpVehicleType.getMinBrakingRate());
					minAcceleration_.setValue((int)tmpVehicleType.getMinAccelerationRate());
					minTimeDistance_.setValue((int)tmpVehicleType.getMinTimeDistance());
					minPoliteness_.setValue((int)tmpVehicleType.getMinPoliteness());
					vehiclesDeviatingMaxSpeed_.setValue((int)tmpVehicleType.getVehiclesDeviatingMaxSpeed_());
					deviationFromSpeedLimit_.setValue((int)Math.round(tmpVehicleType.getDeviationFromSpeedLimit_() / (100000.0/3600)));
					colorPreview_.setBackground(new Color(tmpVehicleType.getColor()));
				}
			}
			else if(((Component) e.getSource()).getName().equals("fakeMessagesTypes")){
				
			}
		}
		//delete all Vehicles
		else if("clearVehicles".equals(command)){	
			if(JOptionPane.showConfirmDialog(null, Messages.getString("EditVehicleControlPanel.msgBoxClearAll"), "", JOptionPane.YES_NO_OPTION) == 0){
				Map.getInstance().clearVehicles();
				Renderer.getInstance().ReRender(true, false);
			}
		}
		else if("speed".equals(command) || "communication distance".equals(command) || "wait time".equals(command) || "braking rate".equals(command) ||
				"acceleration".equals(command) || "time distance".equals(command) || "politeness".equals(command) || "vehicles deviating speed".equals(command) || 
				"speed deviation".equals(command) || "length".equals(command) || "wifi amount".equals(command) || "emergency amount".equals(command) ||
				"faking amount".equals(command)  ||	"amount".equals(command)){
			ResearchSeriesDialog.getInstance().hideResearchWindow(false, "vehicles", command);
			ResearchSeriesDialog.getInstance().setVisible(true);
		}
		else if("applyToScenarioCreator".equals(command)){
			ResearchSeriesDialog.getInstance().hideResearchWindow(false, "allVehicleProperties", "all");
							
			
			int minSpeedValue = (int)Math.round(((Number)minSpeed_.getValue()).intValue() * 100000.0/3600);
			int maxSpeedValue = (int)Math.round(((Number)maxSpeed_.getValue()).intValue() * 100000.0/3600);
			int minCommDistValue = ((Number)minCommDist_.getValue()).intValue()*100;
			int maxCommDistValue = ((Number)maxCommDist_.getValue()).intValue()*100;
			int minWaitValue = ((Number)minWait_.getValue()).intValue();
			int maxWaitValue = ((Number)maxWait_.getValue()).intValue();
			int minBrakingValue = ((Number)minBraking_.getValue()).intValue();
			int maxBrakingValue = ((Number)maxBraking_.getValue()).intValue();
			int minAccelerationValue = ((Number)minAcceleration_.getValue()).intValue();
			int maxAccelerationValue = ((Number)maxAcceleration_.getValue()).intValue();
			int minTimeDistance = ((Number)minTimeDistance_.getValue()).intValue();
			int maxTimeDistance = ((Number)maxTimeDistance_.getValue()).intValue();
			int minPoliteness = ((Number)minPoliteness_.getValue()).intValue();
			int maxPoliteness = ((Number)maxPoliteness_.getValue()).intValue();
			int vehiclesDeviatingMaxSpeed = ((Number)vehiclesDeviatingMaxSpeed_.getValue()).intValue();
			int deviationFromSpeedLimit = ((Number)deviationFromSpeedLimit_.getValue()).intValue();
			int wiFiValue = ((Number)wiFi_.getValue()).intValue();
			int emergencyValue = ((Number)emergencyVehicle_.getValue()).intValue();
			int speedRestriction = (int)Math.round(((Number)speedStreetRestriction_.getValue()).intValue() * 100000.0/3600);
			int vehiclesFaking = ((Number)fakingVehicle_.getValue()).intValue();

			if(wiFiValue < 0){
				wiFiValue = 0;
				wiFi_.setValue(0);
			} else if(wiFiValue > 100){
				wiFiValue = 100;
				wiFi_.setValue(100);
			}
			if(emergencyValue < 0){
				emergencyValue = 0;
				emergencyVehicle_.setValue(0);
			} else if(emergencyValue > 100){
				emergencyValue = 100;
				emergencyVehicle_.setValue(100);
			}
			
			if(vehiclesFaking < 0){
				vehiclesFaking = 0;
				fakingVehicle_.setValue(0);
			} else if(vehiclesFaking > 100){
				vehiclesFaking = 100;
				fakingVehicle_.setValue(100);
			}
			
			int amountValue = ((Number)amount_.getValue()).intValue();
			
			ResearchSeriesDialog.getInstance().getActiveVehicleSet_().setData(((Number)vehicleLength_.getValue()).intValue(), minSpeedValue, maxSpeedValue, minCommDistValue, maxCommDistValue, minWaitValue, maxWaitValue, minBrakingValue, maxBrakingValue, minAccelerationValue, maxAccelerationValue, minTimeDistance, maxTimeDistance, minPoliteness, maxPoliteness, wiFiValue, emergencyValue, vehiclesFaking, fakeMessagesTypes_.getSelectedItem().toString(), amountValue, speedRestriction, vehiclesDeviatingMaxSpeed, deviationFromSpeedLimit, colorPreview_.getBackground());

			
			ResearchSeriesDialog.getInstance().setVisible(true);
		} else if ("openTypeDialog".equals(command)){ //$NON-NLS-1$
			new VehicleTypeDialog();
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
	
	/**
	 * 	updates the vehicle types combobox
	 */
	public void refreshVehicleTypes(){
		chooseVehicleType_.removeActionListener(this); //important: remove all ActionListeners before removing all items
		chooseVehicleType_.removeAllItems();
		VehicleTypeXML xml = new VehicleTypeXML(null);
		for(VehicleType type : xml.getVehicleTypes()){
			chooseVehicleType_.addItem(type);
		}
		chooseVehicleType_.addActionListener(this);
	}

	/**
	 * Mouse listener used to open JColorChooser dialog when colorPreview Panel is clicked
	 */
	public void mouseClicked(MouseEvent e) {
		Color color = JColorChooser.showDialog(this, Messages.getString("EditOneVehicleControlPanel.color"), colorPreview_.getBackground());
		
		if(color == null)colorPreview_.setBackground(Color.black);
		else colorPreview_.setBackground(color);
	}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}


	/** 
	 * a method to activate the scenario creation mode buttons on the left
	 */
	public static void activateSelectPropertiesMode(boolean mode){
		for(JButton b:buttonList_)b.setVisible(mode);
		
	}
	
	/** 
	 * a method to activate the scenario creation mode 
	 */
	public static void activateSelectAllPropertiesMode(boolean mode){
		createButton_.setVisible(!mode);
		deleteButton_.setVisible(!mode);
		scenarioApplyButton_.setVisible(mode);
	}
}