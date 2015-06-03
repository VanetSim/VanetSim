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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import vanetsim.gui.Renderer;
import vanetsim.localization.Messages;
import vanetsim.scenario.IDSProcessor;
import vanetsim.scenario.KnownEventSource;
import vanetsim.scenario.KnownPenalties;
import vanetsim.scenario.KnownVehicle;
import vanetsim.scenario.Vehicle;


/**
 * The control panel for changing some basic settings.
 */
public class EditIDSControlPanel extends JPanel implements ListSelectionListener, ActionListener, ChangeListener, FocusListener {
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = -7820554929998157630L;

	/** A JList displaying all possible IDS rules **/
	private JList<String> availableIDSRules_;
	
	/** A JList displaying all selected IDS rules **/
	private JList<String> selectedIDSRules_;
	
	/** A DefaultListModel for the availableIDSRules **/
	private DefaultListModel<String> availableRulesModel = new DefaultListModel<String>();

	/** The input field for the amount of beacons being logged for the IDS (0 == off) */
	private final JSpinner beaconsLogged_;

	/** A DefaultListModel for the selectedIDSRules **/
	private DefaultListModel<String> selectedRulesModel = new DefaultListModel<String>();

	/** CheckBox to activate IDS */
	private final JCheckBox activateIDSCheckBox_;	
	
	/** CheckBox to the advanced IDS rules */
	private final JCheckBox activateAdvancedIDSCheckBox_;	
	
	/** The input field for the fake message interval */
	private final JFormattedTextField fakeMessageInterval_;
	
	/** Threshold for PCN */
	private final  JFormattedTextField PCNThreshold_;
	
	/** Threshold for PCN FORWARD */
	private final  JFormattedTextField PCNFORWARDThreshold_ = new JFormattedTextField(new DecimalFormat());
	
	/** Threshold for EVA FORWARD */
	private final  JFormattedTextField EVAFORWARDThreshold_ = new JFormattedTextField(new DecimalFormat());
	
	/** Threshold for RHCN */
	private final  JFormattedTextField RHCNThreshold_ = new JFormattedTextField(new DecimalFormat());
	
	/** Threshold for EEBL */
	private final  JFormattedTextField EEBLThreshold_ = new JFormattedTextField(new DecimalFormat());
	
	/** Threshold for EVA */
	private final  JFormattedTextField EVABeaconTimeThreshold_ = new JFormattedTextField(new DecimalFormat());
	
	/** Threshold for EVA */
	private final  JFormattedTextField EVABeaconThreshold_ = new JFormattedTextField(new DecimalFormat());
	
	/** EVA Message Delay in Beacons*/
	private final  JFormattedTextField EVAMessageDelay_ = new JFormattedTextField();
	
	/** a ArrayList of all buttons of the selectproperty mode */
	private static ArrayList<JButton> buttonList_ = new ArrayList<JButton>();
	
	/** CheckBox to activate the spam detection */
	private final JCheckBox spamDetectionCheckBox_;	
	
	/** The input field for the spam threshold based on message amount */
	private final JFormattedTextField spamMessageAmountThreshold_;
	
	/** The input field for the spam threshold based on message amount */
	private final JFormattedTextField spamTimeThreshold_;
	
	/** button to switch location information mode (TN, TP, FN, FP) */
	JButton locationInformationMode_;
	/** file filter */
	FileFilter logFileFilter_;
	
	/**
	 * Constructor.
	 */
	public EditIDSControlPanel(){
		setLayout(new GridBagLayout());

		// global layout settings
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5,5,5,5);
		
		add(new JLabel(Messages.getString("EditIDSControlPanel.availableIDSRules")),c);
		++c.gridy;


		availableIDSRules_ = new JList<String>(availableRulesModel);
		// Initialize the list with items
		for (int i=0; i<IDSProcessor.getIdsData_().length; i++) {
			availableRulesModel.add(i, IDSProcessor.getIdsData_()[i]);
		}
		
		availableIDSRules_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		availableIDSRules_.removeListSelectionListener(this);
		availableIDSRules_.addListSelectionListener(this);
		add(availableIDSRules_, c);
		++c.gridy;
		++c.gridy;
		
		add(new JLabel(Messages.getString("EditIDSControlPanel.selectedIDSRules")),c);
		++c.gridy;
		

		selectedIDSRules_ = new JList<String>(selectedRulesModel);
		selectedRulesModel.add(0, "-");
		selectedIDSRules_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectedIDSRules_.removeListSelectionListener(this);
		selectedIDSRules_.addListSelectionListener(this);
		add(selectedIDSRules_, c);
		
		c.gridwidth = 1;
		++c.gridy;
		c.gridx = 0;
		add(new JLabel(Messages.getString("EditIDSControlPanel.activateIDS")),c);	
		
		activateIDSCheckBox_ = new JCheckBox();
		activateIDSCheckBox_.setSelected(false);
		activateIDSCheckBox_.setActionCommand("activateIDS"); //$NON-NLS-1$
		c.gridx = 1;
		add(activateIDSCheckBox_,c);
		activateIDSCheckBox_.addActionListener(this);	
		
		c.gridwidth = 1;
		++c.gridy;
		c.gridx = 0;
		add(new JLabel(Messages.getString("EditIDSControlPanel.activateAdvancedIDS")),c);	
		
		activateAdvancedIDSCheckBox_ = new JCheckBox();
		activateAdvancedIDSCheckBox_.setSelected(false);
		activateAdvancedIDSCheckBox_.setActionCommand("activateAdvancedIDS"); //$NON-NLS-1$
		c.gridx = 1;
		add(activateAdvancedIDSCheckBox_,c);
		activateAdvancedIDSCheckBox_.addActionListener(this);	
		
		++c.gridy;
		c.gridwidth = 2;
		c.gridx = 0;	
		add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		++c.gridy;
		c.gridwidth = 1;
		c.gridx = 0;
		JLabel label = new JLabel(Messages.getString("EditIDSControlPanel.fakeMessagesInterval")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		fakeMessageInterval_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		fakeMessageInterval_.setPreferredSize(new Dimension(60,20));
		fakeMessageInterval_.setValue(10000);
		fakeMessageInterval_.addFocusListener(this);
		c.gridx = 1;
		add(fakeMessageInterval_,c);
		
		c.gridx = 2;
		JButton button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("fake message interval");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);

		
		++c.gridy;
		c.gridwidth = 2;
		c.gridx = 0;	
		add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		++c.gridy;
		c.gridwidth = 1;
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditIDSControlPanel.PCNThreshold")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		PCNThreshold_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		PCNThreshold_.setPreferredSize(new Dimension(60,20));
		PCNThreshold_.setValue(625);
		PCNThreshold_.addFocusListener(this);
		c.gridx = 1;
		add(PCNThreshold_,c);
		
		c.gridx = 2;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("PCN threshold");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		
		++c.gridy;
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditIDSControlPanel.PCNFORWARDThreshold")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		PCNFORWARDThreshold_.setPreferredSize(new Dimension(60,20));
		PCNFORWARDThreshold_.setValue(0.5);
		PCNFORWARDThreshold_.addFocusListener(this);
		c.gridx = 1;
		add(PCNFORWARDThreshold_,c);
		
		c.gridx = 2;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("PCN forward threshold");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		
		++c.gridy;
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditIDSControlPanel.EVAFORWARDThreshold")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		EVAFORWARDThreshold_.setPreferredSize(new Dimension(60,20));
		EVAFORWARDThreshold_.setValue(500);
		EVAFORWARDThreshold_.addFocusListener(this);
		c.gridx = 1;
		add(EVAFORWARDThreshold_,c);
		
		c.gridx = 2;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("EVA forward threshold");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		
		++c.gridy;
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditIDSControlPanel.RHCNThreshold")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		RHCNThreshold_.setPreferredSize(new Dimension(60,20));
		RHCNThreshold_.setValue(0.5);
		RHCNThreshold_.addFocusListener(this);
		c.gridx = 1;
		add(RHCNThreshold_,c);
		
		c.gridx = 2;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("RHCN threshold");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		
		++c.gridy;
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditIDSControlPanel.EEBLThreshold")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		EEBLThreshold_.setPreferredSize(new Dimension(60,20));
		EEBLThreshold_.setValue(0.5);
		EEBLThreshold_.addFocusListener(this);
		c.gridx = 1;
		add(EEBLThreshold_,c);

		c.gridx = 2;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("EEBL threshold");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		
		++c.gridy;
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditIDSControlPanel.EVABeaconTimeThreshold")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		EVABeaconTimeThreshold_.setPreferredSize(new Dimension(60,20));
		EVABeaconTimeThreshold_.setValue(3);
		EVABeaconTimeThreshold_.addFocusListener(this);
		c.gridx = 1;
		add(EVABeaconTimeThreshold_,c);
		
		c.gridx = 2;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("EVA Beacon time");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		
		++c.gridy;
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditIDSControlPanel.EVABeaconThreshold")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		EVABeaconThreshold_.setPreferredSize(new Dimension(60,20));
		EVABeaconThreshold_.setValue(0.5);
		EVABeaconThreshold_.addFocusListener(this);
		c.gridx = 1;
		add(EVABeaconThreshold_,c);
		
		c.gridx = 2;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("EVA Beacon threshold");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		
		++c.gridy;
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditIDSControlPanel.EVAMessageDelay")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		EVAMessageDelay_.setPreferredSize(new Dimension(60,20));
		EVAMessageDelay_.setValue(3);
		EVAMessageDelay_.addFocusListener(this);
		c.gridx = 1;
		add(EVAMessageDelay_,c);
		
		c.gridx = 2;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("EVA Message Delay");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		
		
		
		++c.gridy;
		c.gridwidth = 2;
		c.gridx = 0;	
		add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		//add spinner to choose amount of vehicles
		++c.gridy;
		c.gridx = 0;
		c.gridwidth = 1;
		label = new JLabel(Messages.getString("EditIDSControlPanel.beaconsLogged")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		beaconsLogged_ = new JSpinner();
		beaconsLogged_.setValue(10);
		beaconsLogged_.addChangeListener(this);
		c.gridx = 1;
		add(beaconsLogged_,c);
		
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("Beacon amount");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		
		++c.gridy;
		c.gridx = 0;
		add(new JLabel(Messages.getString("EditIDSControlPanel.activateSpamDetection")),c);	
		
		spamDetectionCheckBox_ = new JCheckBox();
		spamDetectionCheckBox_.setSelected(false);
		spamDetectionCheckBox_.setActionCommand("activateSpamDetection"); //$NON-NLS-1$
		c.gridx = 1;
		add(spamDetectionCheckBox_,c);
		spamDetectionCheckBox_.addActionListener(this);	
		
		++c.gridy;
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditIDSControlPanel.spamTimeThreshold"));
		add(label,c);		
		spamTimeThreshold_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		spamTimeThreshold_.setPreferredSize(new Dimension(60,20));
		spamTimeThreshold_.setValue(12000);
		spamTimeThreshold_.addFocusListener(this);
		c.gridx = 1;
		add(spamTimeThreshold_,c);
		
		c.gridx = 2;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("spam time threshold");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		
		
		++c.gridy;
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditIDSControlPanel.spamMessageAmountThreshold"));
		add(label,c);		
		spamMessageAmountThreshold_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		spamMessageAmountThreshold_.setPreferredSize(new Dimension(60,20));
		spamMessageAmountThreshold_.setValue(3);
		spamMessageAmountThreshold_.addFocusListener(this);
		c.gridx = 1;
		add(spamMessageAmountThreshold_,c);

		c.gridx = 2;
		button = new JButton(Messages.getString("EditVehicleControlPanel.selectPropertyButton"));
		button.setActionCommand("spam message threshold");
		button.addActionListener(this);
		button.setVisible(false);
		buttonList_.add(button);
		add(button,c);
		
		++c.gridy;
		c.gridwidth = 2;
		c.gridx = 0;	
		add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		//locationInformation
		++c.gridy;
		c.gridx = 0;
		JButton locationInformation = new JButton("Load location data");
		locationInformation.setActionCommand("locationdata");
		locationInformation.setPreferredSize(new Dimension(200,20));
		locationInformation.addActionListener(this);
		add(locationInformation,c);		
		
		//switch locationInformation mode
		++c.gridy;
		c.gridx = 0;
		locationInformationMode_ = new JButton("Switch to TN/FP");
		locationInformationMode_.setActionCommand("locationdatamode");
		locationInformationMode_.setPreferredSize(new Dimension(200,20));
		locationInformationMode_.addActionListener(this);
		add(locationInformationMode_,c);		
		
		//switch locationInformation mode
		++c.gridy;
		c.gridx = 0;
		JButton reset = new JButton("Reset");
		reset.setActionCommand("reset");
		reset.setPreferredSize(new Dimension(200,20));
		reset.addActionListener(this);
		add(reset,c);	
		
		//save to file
		++c.gridy;
		c.gridx = 0;
		JButton save = new JButton("Save to file");
		save.setActionCommand("save");
		save.setPreferredSize(new Dimension(200,20));
		save.addActionListener(this);
		add(save,c);
		
		//define FileFilter for fileChooser
		logFileFilter_ = new FileFilter(){
			public boolean accept(File f) {
				if (f.isDirectory()) return true;
				return f.getName().toLowerCase().endsWith(".log"); //$NON-NLS-1$
			}
			public String getDescription () { 
				return Messages.getString("EditLogControlPanel.logFiles") + " (*.log)"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		};
		
			
		//to consume the rest of the space
		c.weighty = 1.0;
		c.gridy = c.gridy + 100;
		JPanel space = new JPanel();
		space.setOpaque(false);
		add(space, c);
	}
	

	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		@SuppressWarnings("unchecked")
		JList<Object> list = (JList<Object>) arg0.getSource();
		
		if(!list.getValueIsAdjusting() && list.getSelectedIndex() != -1){

			if(list.getModel().equals(availableRulesModel)){
				updateList("available", availableRulesModel.get(list.getSelectedIndex()).toString());
			}
			else if (list.getModel().equals(selectedRulesModel)){
				updateList("selected", selectedRulesModel.get(list.getSelectedIndex()).toString());
			}
		}
	}
	
	/**
	 * 	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		JList list = (JList) arg0.getSource();
		
		if(!list.getValueIsAdjusting() && list.getSelectedIndex() != -1){

			if(list.getModel().equals(availableRulesModel)){
				selectedRulesModel.addElement(availableRulesModel.get(list.getSelectedIndex()));
			    availableRulesModel.removeElement(availableRulesModel.get(list.getSelectedIndex()));
			}
			else if (list.getModel().equals(selectedRulesModel)){
				availableRulesModel.addElement(selectedRulesModel.get(list.getSelectedIndex()));
				selectedRulesModel.removeElement(selectedRulesModel.get(list.getSelectedIndex()));
			}
			

			
			if(selectedRulesModel.getSize() == 0) selectedRulesModel.addElement("-");
			else selectedRulesModel.removeElement("-");
			
			if(availableRulesModel.getSize() == 0) availableRulesModel.addElement("-");
			else availableRulesModel.removeElement("-");
			
			
			
			Object[] tmpArray2 = selectedRulesModel.toArray();
			String[] tmpArray = new String[tmpArray2.length];
			
			for (int i=0; i< tmpArray2.length; i++) {
				tmpArray[i] = tmpArray2[i].toString();
			}
			
			IDSProcessor.setActiveRules(tmpArray);
		}
	}
	 */


	@Override
	public void actionPerformed(ActionEvent arg0) {
		String command = arg0.getActionCommand();
		//action when the add RadioButton is selected
		if("activateIDS".equals(command)){	
			Vehicle.setIdsActivated(activateIDSCheckBox_.isSelected());
		}
		else if("logIDS".equals(command)){
			
		}
		else if("activateAdvancedIDS".equals(command)){
			IDSProcessor.setAdvancedIDSRules_(activateAdvancedIDSCheckBox_.isSelected());
		}
		else if("fake message interval".equals(command) || "PCN threshold".equals(command) || "PCN forward threshold".equals(command) || "EVA forward threshold".equals(command) ||
				"RHCN threshold".equals(command) || "EEBL threshold".equals(command) || "EVA Beacon time".equals(command) || "EVA Beacon threshold".equals(command) || 
				"Beacon amount".equals(command) || "spam time threshold".equals(command) || "spam message threshold".equals(command) || "EVA Message Delay".equals(command)){
			ResearchSeriesDialog.getInstance().hideResearchWindow(false, "generalSettings", command);
			ResearchSeriesDialog.getInstance().setVisible(true);
		}
		else if("activateSpamDetection".equals(command)){
			if(spamDetectionCheckBox_.isSelected()){
				KnownPenalties.setSpamCheck_(true);
				KnownEventSource.setSpamCheck_(true);
			}
			else{
				KnownPenalties.setSpamCheck_(false);
				KnownEventSource.setSpamCheck_(false);
			}
		}
		else if("locationdata".equals(command)){
			showAdvancedLocationInformation();
		}
		else if("locationdatamode".equals(command)){
			if(Renderer.getInstance().getMDSMode_())locationInformationMode_.setText("Switch to TP/FN");
			else locationInformationMode_.setText("Switch to TN/FP");
			Renderer.getInstance().setMDSMode_(!Renderer.getInstance().getMDSMode_());
			Renderer.getInstance().ReRender(true, true);
		}
		else if("reset".equals(command)){
			Renderer.getInstance().setLocationInformationMDS_(null);
			Renderer.getInstance().ReRender(true, true);
		}
	}
	
	public void updateList(String list, String value){
		
		if(list.equals("available")){
			selectedRulesModel.addElement(value);
		    availableRulesModel.removeElement(value);
		}
		else if (list.equals("selected")){
			availableRulesModel.addElement(value);
			selectedRulesModel.removeElement(value);
		}
		
		if(selectedRulesModel.getSize() == 0) selectedRulesModel.addElement("-");
		else selectedRulesModel.removeElement("-");
			
		if(availableRulesModel.getSize() == 0) availableRulesModel.addElement("-");
		else availableRulesModel.removeElement("-");
			
			
			
		Object[] tmpArray2 = selectedRulesModel.toArray();
		String[] tmpArray = new String[tmpArray2.length];
			
		for (int i=0; i< tmpArray2.length; i++) {
			tmpArray[i] = tmpArray2[i].toString();
		}
			
		IDSProcessor.setActiveRules_(tmpArray);
	}
	
	
	public void updateGUI(){
		String[] activeIDSRules = IDSProcessor.getActiveRules_();
		
		// Initialize the list with items
		for (int i=0; i< activeIDSRules.length; i++) {
			if(!selectedRulesModel.contains(activeIDSRules[i])){
				updateList("available", activeIDSRules[i]);
			}
		}
	}


	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource().getClass().equals(beaconsLogged_.getClass())){
			if(((Number)beaconsLogged_.getValue()).intValue() < 1) KnownVehicle.setAmountOfSavedBeacons(-1);
			else KnownVehicle.setAmountOfSavedBeacons(((Number)beaconsLogged_.getValue()).intValue());
		}
		
	}
	
	public void saveAttributes(){
		if(fakeMessageInterval_.getValue() != null)Vehicle.setFakeMessagesInterval_(((Number)fakeMessageInterval_.getValue()).intValue());
		if(PCNThreshold_.getValue() != null)IDSProcessor.setPCNDistance_(((Number)PCNThreshold_.getValue()).intValue());
		if(PCNFORWARDThreshold_.getValue() != null) IDSProcessor.setPCNFORWARDThreshold_(((Number)PCNFORWARDThreshold_.getValue()).doubleValue());
		if(RHCNThreshold_.getValue() != null) IDSProcessor.setRHCNThreshold_(((Number)RHCNThreshold_.getValue()).doubleValue());
		if(EEBLThreshold_.getValue() != null) IDSProcessor.setEEBLThreshold_(((Number)EEBLThreshold_.getValue()).doubleValue());
		if(EVAFORWARDThreshold_.getValue() != null)IDSProcessor.setEVAFORWARDThreshold_(((Number)EVAFORWARDThreshold_.getValue()).intValue());
		if(EVABeaconTimeThreshold_.getValue() != null)IDSProcessor.setEVABeaconTimeFactor_(((Number)EVABeaconTimeThreshold_.getValue()).doubleValue());
		if(EVABeaconThreshold_.getValue() != null)IDSProcessor.setEVABeaconFactor_(((Number)EVABeaconThreshold_.getValue()).doubleValue());
		if(spamMessageAmountThreshold_.getValue() != null)KnownEventSource.setSpammingThreshold_(((Number)spamMessageAmountThreshold_.getValue()).intValue());
		if(spamTimeThreshold_.getValue() != null)KnownEventSource.setSpammingTimeThreshold_(((Number)spamTimeThreshold_.getValue()).intValue());
	
	}


	/**
	 * Show the attack results on the map
	 */
	public void showAdvancedLocationInformation(){
		//begin with selection of file
			JFileChooser fc = new JFileChooser();
			//set directory and ".log" filter
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setFileFilter(logFileFilter_);
			
			int status = fc.showDialog(this, Messages.getString("EditLogControlPanel.approveButton"));
			
        	ArrayList<String> locationInformation = new ArrayList<String>();
        	
			
			if(status == JFileChooser.APPROVE_OPTION){
					File file = fc.getSelectedFile().getAbsoluteFile();
			        BufferedReader reader;
			        
			        try{
			            reader = new BufferedReader(new FileReader(file));
			            String line = reader.readLine();
			            			            		            
			            //check if the log is a silent-period or a mix-zone log
			            while(line != null){
				        	locationInformation.add(line);
		            		
			            	line = reader.readLine();
			            }
					} catch (FileNotFoundException e) {
					    System.err.println("FileNotFoundException: " + e.getMessage());
					} catch (IOException e) {
					    System.err.println("Caught IOException: " + e.getMessage());
					}
			        
					Renderer.getInstance().setLocationInformationMDS_(locationInformation);
					Renderer.getInstance().ReRender(true, true);
					
			}
	}
	
	
	
	/**
	 * @return the beaconsLogged_
	 */
	public JSpinner getBeaconsLogged_() {
		return beaconsLogged_;
	}


	@Override
	public void focusGained(FocusEvent arg0) {
		saveAttributes();
	}


	@Override
	public void focusLost(FocusEvent arg0) {
		saveAttributes();
	}


	/**
	 * @return the fakeMessageInterval_
	 */
	public JFormattedTextField getFakeMessageInterval_() {
		return fakeMessageInterval_;
	}


	/**
	 * @return the activateIDSCheckBox_
	 */
	public JCheckBox getActivateIDSCheckBox_() {
		return activateIDSCheckBox_;
	}


	/**
	 * @return the pCNThreshold_
	 */
	public JFormattedTextField getPCNThreshold_() {
		return PCNThreshold_;
	}


	/**
	 * @return the rHCNThreshold_
	 */
	public JFormattedTextField getRHCNThreshold_() {
		return RHCNThreshold_;
	}


	/**
	 * @return the eEBLThreshold_
	 */
	public JFormattedTextField getEEBLThreshold_() {
		return EEBLThreshold_;
	}


	/**
	 * @return the pCNFORWARDThreshold_
	 */
	public JFormattedTextField getPCNFORWARDThreshold_() {
		return PCNFORWARDThreshold_;
	}


	public JFormattedTextField getEVAFORWARDThreshold_() {
		return EVAFORWARDThreshold_;
	}


	public JFormattedTextField getEVABeaconTimeThreshold_() {
		return EVABeaconTimeThreshold_;
	}


	public JFormattedTextField getEVABeaconThreshold_() {
		return EVABeaconThreshold_;
	}


	public JCheckBox getActivateAdvancedIDSCheckBox_() {
		return activateAdvancedIDSCheckBox_;
	}
	
	/** 
	 * a method to activate the scenario creation mode buttons on the left
	 */
	public static void activateSelectPropertiesMode(boolean mode){
		for(JButton b:buttonList_)b.setVisible(mode);
		
	}


	public JCheckBox getSpamDetectionCheckBox_() {
		return spamDetectionCheckBox_;
	}


	public JFormattedTextField getSpamMessageAmountThreshold_() {
		return spamMessageAmountThreshold_;
	}


	public JFormattedTextField getSpamTimeThreshold_() {
		return spamTimeThreshold_;
	}


	public JFormattedTextField getEVAMessageDelay_() {
		return EVAMessageDelay_;
	}
}