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
import java.text.ParseException;

import java.util.ArrayDeque;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;

import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.gui.helpers.TextAreaLabel;
import vanetsim.gui.helpers.VehicleType;
import vanetsim.gui.helpers.VehicleTypeXML;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.map.Region;
import vanetsim.routing.WayPoint;
import vanetsim.scenario.IDSProcessor;
import vanetsim.scenario.Vehicle;

/**
 * This class represents the control panel for adding vehicles by click.
 */

public class EditOneVehicleControlPanel extends JPanel implements ActionListener, MouseListener{
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = 8669978113870090221L;

	/** RadioButton to add vehicle. */
	JRadioButton addItem_;
	
	/** RadioButton to edit vehicle. */
	JRadioButton editItem_;
	
	/** RadioButton to delete vehicle. */
	JRadioButton deleteItem_;
	
	/** A Label for vehicle type ComboBox. */
	private JLabel chooseVehicleTypeLabel_;
		
	/** A JComboBox to switch between vehicles types. */
	private JComboBox<VehicleType> chooseVehicleType_;
	
	/** A Label for the JComboBox to switch between vehicles that are near each other. */
	private JLabel chooseVehicleLabel_;
		
	/** A JComboBox to switch between vehicles that are near each other. */
	private JComboBox<Vehicle> chooseVehicle_;
	
	/** The input field for the speed in km/h. */
	private final JFormattedTextField speed_;
	
	/** The input field for the communication distance in m. */
	private final JFormattedTextField commDist_;
	
	/** The input field for the wait in milliseconds. */
	private final JFormattedTextField wait_;
	
	/** The input field for the braking rate in cm/s^2. */
	private final JFormattedTextField brakingRate_;
	
	/** The input field for the acceleration rate in cm/s^2. */
	private final JFormattedTextField accelerationRate_;
	
	/** The input field for the time distance in ms. */
	private final JFormattedTextField timeDistance_;
	
	/** The input field for the politeness factor in %. */
	private final JFormattedTextField politeness_;
	
	/** The input field for the deviation from the speed limit */
	private final JFormattedTextField deviationFromSpeedLimit_;
	
	/** The input field for the vehicleLength in cm. */
	private final JFormattedTextField vehicleLength_;
	
	/** The checkbox to activate and deactivate wiFi */
	private final JCheckBox wifi_;	

	/** The checkbox to activate and deactivate emergency vehicle features */
	private final JCheckBox emergencyVehicle_;	
	
	/** The checkbox to activate and deactivate if a vehicle is faking messages */
	private final JCheckBox fakingVehicle_;	
	
	/** A JComboBox to switch between fake messages types. */
	private JComboBox<String> fakeMessagesTypes_;
	
	/** JPanel to preview the selected Vehicle color. */
	private final JPanel colorPreview_;	
	
	/** The spinner to define the amount of waypoints. */
	private final JSpinner waypointAmount_;
	
	/** The label describing the waypointAmount_ Spinner */
	private final JLabel waypointAmountLabel_;
	
	/** The spinner to define the amount of vehicles. */
	private final JSpinner vehicleAmount_;
	
	/** The label describing the vehiceAmount_ Spinner */
	private final JLabel vehicleAmountLabel_;

	/** Collects waypoints, when adding new vehicle. */
	private ArrayDeque<WayPoint> destinations = null;
	
	/** Create/Save button for vehicles. */
	private JButton createVehicle_;
	
	/** Delete button to delete a selected vehicle. */
	private JButton deleteVehicle_;
	
	/** Delete button to delete all vehicles. */
	private JButton deleteAllVehicles_;
	
	/** Note to describe vehicle action button. */
	TextAreaLabel addNote_;
	
	/** Note to describe vehicle save button. */
	TextAreaLabel saveNote_;
	
	/** Note to describe vehicle delete button. */
	TextAreaLabel deleteNote_;
	
	/** JPanel to consume whitespace */
	JPanel space_;

	
	/**
	 * Constructor.
	 */
	public EditOneVehicleControlPanel(){
		setLayout(new GridBagLayout());
		
		// global layout settings
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 2;
	
		// Radio buttons to select add, edit or delete mode
		ButtonGroup group = new ButtonGroup();
		addItem_ = new JRadioButton(Messages.getString("EditOneVehicleControlPanel.add")); //$NON-NLS-1$
		addItem_.setActionCommand("add"); //$NON-NLS-1$
		addItem_.addActionListener(this);
		addItem_.setSelected(true);
		group.add(addItem_);
		++c.gridy;
		add(addItem_,c);
		
		editItem_ = new JRadioButton(Messages.getString("EditOneVehicleControlPanel.edit")); //$NON-NLS-1$
		editItem_.setActionCommand("edit"); //$NON-NLS-1$
		editItem_.addActionListener(this);
		group.add(editItem_);
		++c.gridy;
		add(editItem_,c);
		
		deleteItem_ = new JRadioButton(Messages.getString("EditOneVehicleControlPanel.delete")); //$NON-NLS-1$
		deleteItem_.setActionCommand("delete"); //$NON-NLS-1$
		deleteItem_.setSelected(true);
		deleteItem_.addActionListener(this);
		group.add(deleteItem_);
		++c.gridy;
		add(deleteItem_,c);
		
		//add comboBox to choose vehicle types and vehicles
		c.gridwidth = 1;
		c.insets = new Insets(5,5,5,5);

		
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
		
		
		c.gridx = 0;
		chooseVehicleLabel_ = new JLabel(Messages.getString("EditOneVehicleControlPanel.selectVehicle")); //$NON-NLS-1$
		++c.gridy;
		add(chooseVehicleLabel_,c);
		chooseVehicle_ = new JComboBox<Vehicle>();
		chooseVehicle_.setName("chooseVehicle");
		chooseVehicle_.addActionListener(this);
		c.gridx = 1;
		add(chooseVehicle_, c);
		chooseVehicle_.setVisible(false);
		chooseVehicleLabel_.setVisible(false);
	
		//add textfields and checkboxes to change vehicle properties
		c.gridx = 0;
		JLabel label = new JLabel(Messages.getString("EditOneVehicleControlPanel.speed")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		speed_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		speed_.setValue(100);
		getSpeed().setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(getSpeed(),c);
	
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditOneVehicleControlPanel.commDistance")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		commDist_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		commDist_.setValue(100);
		getCommDist().setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(getCommDist(),c);
		
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditOneVehicleControlPanel.waittime")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		wait_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		wait_.setValue(10);
		getWait().setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(getWait(),c);
		
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditOneVehicleControlPanel.brakingRate")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		brakingRate_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		brakingRate_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(brakingRate_,c);
		
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditOneVehicleControlPanel.accelerationRate")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		accelerationRate_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		accelerationRate_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(accelerationRate_,c);
		
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditOneVehicleControlPanel.timeDistance")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		timeDistance_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		timeDistance_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(timeDistance_,c);
		
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditOneVehicleControlPanel.politeness")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		politeness_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		politeness_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(politeness_,c);
		
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditVehicleControlPanel.deviationFromSpeedLimit")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		deviationFromSpeedLimit_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		deviationFromSpeedLimit_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(deviationFromSpeedLimit_,c);
		
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditOneVehicleControlPanel.vehicleLength")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		vehicleLength_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		vehicleLength_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(vehicleLength_,c);
		
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditOneVehicleControlPanel.wifi")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		wifi_ = new JCheckBox();
		c.gridx = 1;
		add(wifi_,c);
		
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditOneVehicleControlPanel.emergencyVehicle")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		emergencyVehicle_ = new JCheckBox();
		c.gridx = 1;
		add(emergencyVehicle_,c);
		
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditOneVehicleControlPanel.fakingVehicle")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		fakingVehicle_ = new JCheckBox();
		c.gridx = 1;
		add(fakingVehicle_,c);
		
		//add vehicle types comboBox
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditOneVehicleControlPanel.selectFakeMessageType")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);
		fakeMessagesTypes_ = new JComboBox<String>();

		fakeMessagesTypes_.addItem(Messages.getString("EditVehicleControlPanel.all"));
		for(int i = 0; i < IDSProcessor.getIdsData_().length; i++) fakeMessagesTypes_.addItem(IDSProcessor.getIdsData_()[i]);

		c.gridx = 1;
		add(fakeMessagesTypes_, c);
		
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditOneVehicleControlPanel.color")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);
		colorPreview_ = new JPanel();	
		getColorPreview().setBackground(Color.black);
		getColorPreview().setSize(10, 10);
		getColorPreview().addMouseListener(this);
		c.gridx = 1;
		add(getColorPreview(),c);
		
		//add spinner to choose the amount of waypoints
		c.gridx = 0;
		waypointAmountLabel_ = new JLabel(Messages.getString("EditOneVehicleControlPanel.waypointAmount")); //$NON-NLS-1$
		++c.gridy;
		add(waypointAmountLabel_,c);		
		waypointAmount_ = new JSpinner();
		waypointAmount_.setValue(2);
		c.gridx = 1;
		add(waypointAmount_,c);
		
		//add spinner to choose amount of vehicles
		c.gridx = 0;
		vehicleAmountLabel_ = new JLabel(Messages.getString("EditOneVehicleControlPanel.vehicleAmount")); //$NON-NLS-1$
		++c.gridy;
		add(vehicleAmountLabel_,c);		
		vehicleAmount_ = new JSpinner();
		vehicleAmount_.setValue(1);
		c.gridx = 1;
		add(vehicleAmount_,c);
		
		//add buttons
		c.gridx = 0;
		c.gridwidth = 2;
		++c.gridy;
		createVehicle_ = ButtonCreator.getJButton("oneVehicle.png", "vehicleAction", Messages.getString("EditOneVehicleControlPanel.vehicleAction"), this);
		add(createVehicle_,c);
		
		c.gridx = 0;
		++c.gridy;
		deleteVehicle_ = ButtonCreator.getJButton("deleteVehicles.png", "deleteVehicle", Messages.getString("EditOneVehicleControlPanel.deleteVehicle"), this);
		add(deleteVehicle_,c);
		deleteVehicle_.setVisible(false);
		
		c.gridx = 0;
		c.gridwidth = 2;
		++c.gridy;
		deleteAllVehicles_ = ButtonCreator.getJButton("deleteAll.png", "clearVehicles", Messages.getString("EditOneVehicleControlPanel.btnClearVehicles"), this);
		add(deleteAllVehicles_,c);
			
		c.gridx = 0;
		++c.gridy;
		add(ButtonCreator.getJButton("openTypeDialog.png", "openTypeDialog", Messages.getString("EditControlPanel.openTypeDialog"), this), c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		c.gridx = 0;
		++c.gridy;
		
		addNote_ = new TextAreaLabel(Messages.getString("EditOneVehicleControlPanel.noteAdd")); //$NON-NLS-1$
		++c.gridy;
		c.gridx = 0;
		add(addNote_, c);
		
		saveNote_ = new TextAreaLabel(Messages.getString("EditOneVehicleControlPanel.noteSave")); //$NON-NLS-1$
		++c.gridy;
		c.gridx = 0;
		add(saveNote_, c);
		saveNote_.setVisible(false);
		
		deleteNote_ = new TextAreaLabel(Messages.getString("EditOneVehicleControlPanel.noteDelete")); //$NON-NLS-1$
		++c.gridy;
		c.gridx = 0;
		add(deleteNote_, c);
		deleteNote_.setVisible(false);
		
		//to consume the rest of the space
		c.weighty = 1.0;
		++c.gridy;
		space_ = new JPanel();
		space_.setOpaque(false);
		add(space_, c);

	    
		//updates the input fields to the first vehicle type
		actionPerformed(new ActionEvent(chooseVehicleType_,0,"comboBoxChanged"));
	}
	
	/**
	 * Receives a mouse event.
	 * 
	 * @param x	the x coordinate (in map scale)
	 * @param y	the y coordinate (in map scale)
	 */
	public void receiveMouseEvent(int x, int y){

		if(editItem_.isSelected() || deleteItem_.isSelected()){
			//find vehicles near x and y and add those to the vehicle combobox
			chooseVehicle_.removeActionListener(this); //important: Remove Action listener before removing all comboBox items otherwise the combobox will be buggy
			chooseVehicle_.removeAllItems();			
			chooseVehicle_.setVisible(false);
			chooseVehicleLabel_.setVisible(false);
			Renderer.getInstance().setMarkedVehicle(null);
			
			
			Region[][] Regions = Map.getInstance().getRegions();
			int Region_max_x = Map.getInstance().getRegionCountX();
			int Region_max_y = Map.getInstance().getRegionCountY();
			int i, j;
			for(i = 0; i < Region_max_x; ++i){
				for(j = 0; j < Region_max_y; ++j){
					Vehicle[] vehiclesArray = Regions[i][j].getVehicleArray();
					for(int k = 0; k < vehiclesArray.length; ++k){
						Vehicle vehicle = vehiclesArray[k];
						
						//add all vehicles near the coordinates (300 cm radius)
						if(editItem_.isSelected()){
							if(vehicle.getX() > (x - 300) && vehicle.getX() < (x + 300) && vehicle.getY() > (y - 300) && vehicle.getY() < (y + 300)) {
								chooseVehicle_.addItem(vehicle);	
								chooseVehicle_.setVisible(true);
								chooseVehicleLabel_.setVisible(true);
							}
						}		
						
						//sets one of the nearest vehicles to the selected item in the combobox
						if(vehicle.getX() > (x - 100) && vehicle.getX() < (x + 100) && vehicle.getY() > (y - 100) && vehicle.getY() < (y + 100)) {
							Renderer.getInstance().setMarkedVehicle(vehicle);
							
							if(editItem_.isSelected()){
								speed_.setValue((int)Math.round(vehicle.getMaxSpeed() / (100000.0/3600)));
								vehicleLength_.setValue((int)vehicle.getVehicleLength());
								commDist_.setValue((int)Math.round(vehicle.getMaxCommDistance() / 100));
								wait_.setValue((int)vehicle.getWaittime());	
								wifi_.setSelected(vehicle.isWiFiEnabled());
								emergencyVehicle_.setSelected(vehicle.isEmergencyVehicle());
								fakingVehicle_.setSelected(vehicle.isFakingMessages());
								fakeMessagesTypes_.setSelectedItem(vehicle.getFakeMessageType());
								colorPreview_.setBackground(vehicle.getColor());
								brakingRate_.setValue(vehicle.getBrakingRate());
								accelerationRate_.setValue(vehicle.getAccelerationRate());
								timeDistance_.setValue(vehicle.getTimeDistance());
								politeness_.setValue(vehicle.getPoliteness());
								deviationFromSpeedLimit_.setValue((int)Math.round(vehicle.getSpeedDeviation_() / (100000.0/3600)));
								chooseVehicle_.setSelectedItem(vehicle);
							}
							else{
								Map.getInstance().delVehicle(Renderer.getInstance().getMarkedVehicle());
								if(Renderer.getInstance().getMarkedVehicle().equals(Renderer.getInstance().getAttackedVehicle())) Renderer.getInstance().setAttackedVehicle(null);
								if(Renderer.getInstance().getMarkedVehicle().equals(Renderer.getInstance().getAttackerVehicle())) Renderer.getInstance().setAttackerVehicle(null);
								Renderer.getInstance().setMarkedVehicle(null);
								
							}

						}
						
						

					}
				}
				//if no vehicle in a 100 cm radius was found just select the first vehicle in the combobox
				if(Renderer.getInstance().getMarkedVehicle() == null && chooseVehicle_.getItemCount() != 0){
					Renderer.getInstance().setMarkedVehicle((Vehicle) chooseVehicle_.getItemAt(0));
				    
					//updates the input fields to the first vehicle
					actionPerformed(new ActionEvent(chooseVehicle_,0,"comboBoxChanged"));
				}
		}
		chooseVehicle_.addActionListener(this); //add the ActionsListener when all vehicles are added to the combobox
		Renderer.getInstance().ReRender(false, false);
		}
		
		//destinations is not null after pressing the add vehicles button, now all clicks are saved to destinations as waypoints
		else if(destinations != null){
			WayPoint tmpWayPoint;
			try {		
				tmpWayPoint = new WayPoint(x,y,((Number)wait_.getValue()).intValue());
				destinations.add(tmpWayPoint);			
				//engough waypoints are selected call addVehicle()
				if(destinations.size() == ((Number)waypointAmount_.getValue()).intValue()) addVehicle();
			} catch (ParseException e) {
				JOptionPane.showMessageDialog(null, Messages.getString("EditOneVehicleControlPanel.MsgBoxCreateWaypointError"), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}

		}
	}
	
	/**
	 * Function to add vehicles. Called after enough waypoints where selected on map.
	 */	
	private void addVehicle(){
			Vehicle tmpVehicle;
			int timeBetween = 0;
			
			//if there is more than one vehicle to be created, ask for the time between the vehicles start
			if(((Number)vehicleAmount_.getValue()).intValue() > 1)  timeBetween = Integer.parseInt(JOptionPane.showInputDialog(Messages.getString("EditOneVehicleControlPanel.MsgBoxVehicleAmount")));
			try {
				for(int i = 0; i < ((Number)vehicleAmount_.getValue()).intValue() ;i++){
					destinations.peekFirst().setWaittime(i*timeBetween * 1000 + ((Number)wait_.getValue()).intValue());
					tmpVehicle = new Vehicle(destinations, ((Number)vehicleLength_.getValue()).intValue(), (int)Math.round(((Number)speed_.getValue()).intValue() * 100000.0/3600), ((Number)commDist_.getValue()).intValue()*100, wifi_.isSelected(), emergencyVehicle_.isSelected(), ((Number) brakingRate_.getValue()).intValue(), ((Number)accelerationRate_.getValue()).intValue(), ((Number)timeDistance_.getValue()).intValue(), ((Number)politeness_.getValue()).intValue(), (int)Math.round(((Number)deviationFromSpeedLimit_.getValue()).intValue() * 100000.0/3600), getColorPreview().getBackground(), fakingVehicle_.isSelected(), fakeMessagesTypes_.getSelectedItem().toString());
					Map.getInstance().addVehicle(tmpVehicle);
					Renderer.getInstance().setMarkedVehicle(tmpVehicle);
				}

				destinations = null;			
			} catch (ParseException e) {
				JOptionPane.showMessageDialog(null, Messages.getString("EditOneVehicleControlPanel.MsgBoxCreateVehicleError"), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			Renderer.getInstance().ReRender(true, false);
			
			//reset the text of the add vehicle note
			addNote_.setForeground(Color.black);
			addNote_.setText(Messages.getString("EditOneVehicleControlPanel.noteAdd"));
	}
	
	
	/**
	 * An implemented <code>ActionListener</code> which performs all needed actions when a <code>JButton</code>,
	 * or a <code>JComboBox</code>, or a <code>JRadioButton</code> is clicked.
	 * 
	 * @param e	an <code>ActionEvent</code>
	 */	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		//action when the add RadioButton is selected
		if("add".equals(command)){	
			destinations = null;
			Renderer.getInstance().setMarkedVehicle(null);
			setGuiElements("add");
			actionPerformed(new ActionEvent(chooseVehicleType_,0,"comboBoxChanged"));
			Renderer.getInstance().ReRender(false, false);
		}
		//action when the edit RadioButton is selected
		if("edit".equals(command)){	
			Renderer.getInstance().setMarkedVehicle(null);
			setGuiElements("save");
			Renderer.getInstance().ReRender(false, false);
			
			//reset the text of the add vehicle note
			addNote_.setForeground(Color.black);
			addNote_.setText(Messages.getString("EditOneVehicleControlPanel.noteAdd"));
		}
		//action when the delete RadioButton is selected
		if("delete".equals(command)){	
			Renderer.getInstance().setMarkedVehicle(null);
			setGuiElements("delete");
			Renderer.getInstance().ReRender(false, false);
			
			//reset the text of the add vehicle note
			addNote_.setForeground(Color.black);
			addNote_.setText(Messages.getString("EditOneVehicleControlPanel.noteAdd"));
		}	
		//action when vehicleAction button is pressed and add item radiobutton is selected
		if("vehicleAction".equals(command) && addItem_.isSelected()){ //$NON-NLS-1$	;
			//show add vehicle information and create destinations ArrayDeque (now all mouse events placed on the map will create Waypoints)
			if(((Number)waypointAmount_.getValue()).intValue() > 1){
				addNote_.setForeground(Color.red);
				addNote_.setText(Messages.getString("EditOneVehicleControlPanel.MsgCreateVehicle"));
				destinations = new ArrayDeque<WayPoint>(((Number)waypointAmount_.getValue()).intValue());				
			}
			else{
				JOptionPane.showMessageDialog(null, Messages.getString("EditOneVehicleControlPanel.MsgBoxCreateVehicleWaypointAmountError"), "Error", JOptionPane.ERROR_MESSAGE);	
			}
		}
		//action when vehicleAction button is pressed and edit item radiobutton is selected
		else if("vehicleAction".equals(command) && editItem_.isSelected()){ //$NON-NLS-1$	
			Renderer.getInstance().setShowVehicles(true);
			//save the vehicle
			Vehicle tmpVehicle = Renderer.getInstance().getMarkedVehicle();	

			if(tmpVehicle != null && !getSpeed().getValue().equals("") && !getCommDist().getValue().equals("") && !getWait().getValue().equals("")){
				tmpVehicle.setMaxSpeed((int)Math.round(((Number)getSpeed().getValue()).intValue() * 100000.0/3600));
				tmpVehicle.setVehicleLength(((Number)vehicleLength_.getValue()).intValue());
				tmpVehicle.setMaxCommDistance(((Number)getCommDist().getValue()).intValue()*100);
				tmpVehicle.setCurWaitTime(((Number)getWait().getValue()).intValue());	
				tmpVehicle.setAccelerationRate(((Number)getAccelerationRate().getValue()).intValue());
				tmpVehicle.setTimeDistance(((Number)timeDistance_.getValue()).intValue());
				tmpVehicle.setPoliteness(((Number)politeness_.getValue()).intValue());
				tmpVehicle.setSpeedDeviation_((int)Math.round(((Number)deviationFromSpeedLimit_.getValue()).intValue() * 100000.0/3600));
				tmpVehicle.setBrakingRate(((Number)getBrakingRate().getValue()).intValue());
				tmpVehicle.setWiFiEnabled(wifi_.isSelected());
				tmpVehicle.setColor(colorPreview_.getBackground());
				tmpVehicle.setEmergencyVehicle(emergencyVehicle_.isSelected());
				tmpVehicle.setFakingMessages(fakingVehicle_.isSelected());
				tmpVehicle.setFakeMessageType(fakeMessagesTypes_.getSelectedItem().toString());

				JOptionPane.showMessageDialog(null, Messages.getString("EditOneVehicleControlPanel.MsgBoxSavedText"), "Information", JOptionPane.INFORMATION_MESSAGE);
			}
			else{
				JOptionPane.showMessageDialog(null, Messages.getString("EditOneVehicleControlPanel.MsgBoxNOTSavedText"), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		//action when the deleteVehicle Button is pressed
		if("deleteVehicle".equals(command)){
			//delete vehicle, if markedVehicle is not null
			if(Renderer.getInstance().getMarkedVehicle() != null){
				if(chooseVehicle_.getItemCount() > 1){
				Map.getInstance().delVehicle(Renderer.getInstance().getMarkedVehicle());
				chooseVehicle_.removeItem(Renderer.getInstance().getMarkedVehicle());
				Renderer.getInstance().setMarkedVehicle((Vehicle) chooseVehicle_.getSelectedItem());					
				}
				else{
					chooseVehicle_.removeActionListener(this); //important: Remove ActionListener before removing all items from Combobox
					chooseVehicle_.removeAllItems();			
					chooseVehicle_.setVisible(false);
					chooseVehicleLabel_.setVisible(false);
					Map.getInstance().delVehicle(Renderer.getInstance().getMarkedVehicle());
					Renderer.getInstance().setMarkedVehicle(null);
					chooseVehicle_.addActionListener(this);
				}


				Renderer.getInstance().ReRender(false, false);
			}
			else{
				JOptionPane.showMessageDialog(null, Messages.getString("EditOneVehicleControlPanel.MsgBoxDeleteVehicle"), "Error", JOptionPane.ERROR_MESSAGE);	
			}
		}
		else if ("comboBoxChanged".equals(command)){
			//action when a vehicle in the chooseVehicle Combobox is selected update GUI
			if(((Component) e.getSource()).getName().equals("chooseVehicle")){
				Vehicle tmpVehicle = (Vehicle) chooseVehicle_.getSelectedItem();
				Renderer.getInstance().setMarkedVehicle(tmpVehicle);
				
				if(tmpVehicle != null){
					getSpeed().setValue((int)Math.round(tmpVehicle.getMaxSpeed() / (100000.0/3600)));
					vehicleLength_.setValue(tmpVehicle.getVehicleLength());
					commDist_.setValue((int)Math.round(tmpVehicle.getMaxCommDistance() / 100));
					wait_.setValue((int)tmpVehicle.getWaittime());
					brakingRate_.setValue(tmpVehicle.getBrakingRate());
					accelerationRate_.setValue(tmpVehicle.getAccelerationRate());
					timeDistance_.setValue(tmpVehicle.getTimeDistance());
					politeness_.setValue(tmpVehicle.getPoliteness());
					deviationFromSpeedLimit_.setValue((int)Math.round(tmpVehicle.getSpeedDeviation_() / (100000.0/3600)));
					wifi_.setSelected(tmpVehicle.isWiFiEnabled());
					emergencyVehicle_.setSelected(tmpVehicle.isEmergencyVehicle());
					colorPreview_.setBackground(tmpVehicle.getColor());
					fakingVehicle_.setSelected(tmpVehicle.isFakingMessages());
					fakeMessagesTypes_.setSelectedItem(tmpVehicle.getFakeMessageType());
					Renderer.getInstance().ReRender(false, false);					
				}
			}
			//action when a vehicle  type in the chooseVehicleType Combobox is selected update GUI
			else if(((Component) e.getSource()).getName().equals("chooseVehicleType")){
				VehicleType tmpVehicleType = (VehicleType) chooseVehicleType_.getSelectedItem();
				
				if(tmpVehicleType != null){
					speed_.setValue((int)Math.round((tmpVehicleType.getMaxSpeed() / (100000.0/3600) + tmpVehicleType.getMinSpeed() / (100000.0/3600)) / 2));
					vehicleLength_.setValue(tmpVehicleType.getVehicleLength());
					commDist_.setValue((int)Math.round((tmpVehicleType.getMaxCommDist() / 100 + tmpVehicleType.getMinCommDist() / 100) / 2));
					wait_.setValue((int)Math.round((tmpVehicleType.getMaxWaittime() + tmpVehicleType.getMinWaittime()) /2));
					brakingRate_.setValue(((int)Math.round(tmpVehicleType.getMaxBrakingRate() + tmpVehicleType.getMinBrakingRate()) / 2));
					accelerationRate_.setValue(Math.round((tmpVehicleType.getMaxAccelerationRate() + tmpVehicleType.getMinAccelerationRate()) / 2));
					timeDistance_.setValue(Math.round((tmpVehicleType.getMaxTimeDistance() + tmpVehicleType.getMinTimeDistance()) / 2));
					politeness_.setValue(Math.round((tmpVehicleType.getMaxPoliteness() + tmpVehicleType.getMinPoliteness()) / 2));
					deviationFromSpeedLimit_.setValue((int)Math.round(tmpVehicleType.getDeviationFromSpeedLimit_() / (100000.0/3600)));
					wifi_.setSelected(tmpVehicleType.isWifi());
					emergencyVehicle_.setSelected(tmpVehicleType.isEmergencyVehicle());
					colorPreview_.setBackground(new Color(tmpVehicleType.getColor()));					
				}

			}
		}
		//delete all vehicles
		else if("clearVehicles".equals(command)){	
			if(JOptionPane.showConfirmDialog(null, Messages.getString("EditOneVehicleControlPanel.msgBoxClearAll"), "", JOptionPane.YES_NO_OPTION) == 0){
				Map.getInstance().clearVehicles();
				Renderer.getInstance().ReRender(true, false);
			}
		} else if ("openTypeDialog".equals(command)){ //$NON-NLS-1$
			new VehicleTypeDialog();
		}
	}
	
	/**
	 * updates the vehicle type combobox
	 */
	public void refreshVehicleTypes(){
		chooseVehicleType_.removeActionListener(this);  //important: Remove ActionListener before removing all items from Combobox
		chooseVehicleType_.removeAllItems();
		VehicleTypeXML xml = new VehicleTypeXML(null);
		for(VehicleType type : xml.getVehicleTypes()){
			chooseVehicleType_.addItem(type);
		}
		if(chooseVehicleType_.getItemCount() > 0){
			chooseVehicleType_.setVisible(true);
			chooseVehicleTypeLabel_.setVisible(true);
		}
		chooseVehicleType_.addActionListener(this);
	}

	/**
	 * Sets the visibility of GUI elements for adding vehicles
	 */
	public void setGuiElements(String command){
		for(Object o:this.getComponents()){
			((Component) o).setVisible(true);
		}	
		if(command.equals("add")){
			if(chooseVehicleType_.getItemCount() < 1){
				chooseVehicleType_.setVisible(false);
				chooseVehicleTypeLabel_.setVisible(false);		
			}
			chooseVehicle_.setVisible(false);
			chooseVehicleLabel_.setVisible(false);		
			saveNote_.setVisible(false);
			deleteNote_.setVisible(false);
			deleteVehicle_.setVisible(false);
		}
		else if(command.equals("save")){
			chooseVehicle_.setVisible(false);
			chooseVehicleLabel_.setVisible(false);				
			chooseVehicleType_.setVisible(false);
			chooseVehicleTypeLabel_.setVisible(false);	
			waypointAmount_.setVisible(false);
			waypointAmountLabel_.setVisible(false);
			vehicleAmount_.setVisible(false);
			vehicleAmountLabel_.setVisible(false);
			addNote_.setVisible(false);
			deleteNote_.setVisible(false);
		}
		else if(command.equals("delete")){
			for(Object o:this.getComponents()){
				((Component) o).setVisible(false);
			}	
			addItem_.setVisible(true);
			editItem_.setVisible(true);
			deleteItem_.setVisible(true);
			deleteNote_.setVisible(true);
			deleteAllVehicles_.setVisible(true);
			space_.setVisible(true);
		}

	}
	
	/**
	 * Gets the vehicle wait time TextField 
	 * 
	 * @return the wait_ TextField
	 */
	public JFormattedTextField getWait() {
		return wait_;
	}

	/**
	 * Gets the vehicle speed TextField
	 * 
	 * @return the speed_ TextField
	 */
	public JFormattedTextField getSpeed() {
		return speed_;
	}

	/**
	 * Gets the vehicle communications distance TextField
	 * 
	 * @return the commDist_ TextField
	 */
	public JFormattedTextField getCommDist() {
		return commDist_;
	}

	/**
	 * Gets the vehicle color Panel
	 * 
	 * @return the colorPreview_ Panel
	 */
	public JPanel getColorPreview() {
		return colorPreview_;
	}

	/**
	 * Gets the vehicle braking rate TextField
	 * 
	 * @return the brakingRate_ TextField
	 */
	public JFormattedTextField getBrakingRate() {
		return brakingRate_;
	}

	/**
	 * Gets the vehicle acceleration rate TextField
	 * 
	 * @return the accelerationRate_ TextField
	 */
	public JFormattedTextField getAccelerationRate() {
		return accelerationRate_;
	}
	
	/**
	 * Gets the TextAreaLabel in the add vehicle menu
	 * 
	 * @return addNote_		the add note
	 */
	public TextAreaLabel getAddNote() {
		return addNote_;
	}

	//Mouse Listener to open the JColorChooser to choose the vehicle color, when the colorPreview Panel is clicked
	public void mouseClicked(MouseEvent e) {
		Color color = JColorChooser.showDialog(this, Messages.getString("EditOneVehicleControlPanel.color"), colorPreview_.getBackground());
		
		if(color == null)colorPreview_.setBackground(Color.black);
		else colorPreview_.setBackground(color);
	}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

}