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

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.gui.helpers.TextAreaLabel;
import vanetsim.localization.Messages;
import vanetsim.map.Junction;
import vanetsim.map.Map;
import vanetsim.map.MapHelper;
import vanetsim.map.Street;
import vanetsim.map.TrafficLight;

public class EditTrafficLightsControlPanel extends JPanel implements ActionListener, MouseListener{

	/** The necessary constant for serializing. */
	private static final long serialVersionUID = -6527882433020491577L;

	/** RadioButton to add traffic light. */
	private JRadioButton addItem_;
	
	/** RadioButton to edit traffic light. */
	private JRadioButton editItem_;
	
	/** RadioButton to edit a single signal. */
	private JRadioButton editOneSignal_;
	
	/** Label for the red-phase text. */
	private JLabel redPhaseLengthLabel_;
	
	/** Label for the yellow-phase text. */
	private JLabel yellowPhaseLengthLabel_;
	
	/** Label for the crossing priority difference text. */
	private JLabel crossingPriorityDifferenceLengthLabel_;
	
	/** The input field for the wait time in the red-phase in milliseconds. */
	private JFormattedTextField redGreenPhaseLength_;
	
	/** The input field for the wait time in the yellow-phase in milliseconds. */
	private JFormattedTextField yellowPhaseLength_;
	
	/** The input field for the wait time in the red-phase in milliseconds. */
	private JFormattedTextField crossingPriorityDifferenceLength_;
	
	/** Create/Save button for traffic lights. */
	private JButton createTrafficLight_;
	
	/** Delete button to delete a selected traffic light. */
	private JButton deleteTrafficLight_;
	
	/** Delete button to delete all traffic lights. */
	private JButton deleteAllTrafficLights_;
	
	/** Note to describe traffic light action button. */
	private TextAreaLabel addNote_;
	
	/** Note to describe traffic light save button. */
	private TextAreaLabel saveNote_;
	
	/** JPanel to consume whitespace */
	private JPanel space_;
	
	/** The actual marked Junction on the Map. */
	private Junction actualJunction_ = null;
	
	/** A JComboBox to switch between traffic signal. */
	private JComboBox<Street> chooseTrafficSignal_;
	
	/** JPanel to preview the selected signal color. */
	private final JPanel colorPreview_;	
	
	/** button the switch signal state. */
	private JButton switchSignalState_;
	
	/** the selected street (through edit one traffic signal) */
	private Street selectedStreet_ = null;
	
	/**
	 * Constructor.
	 */
	public EditTrafficLightsControlPanel() {
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
		addItem_ = new JRadioButton(Messages.getString("EditTrafficLightsControlPanel.add")); //$NON-NLS-1$
		addItem_.setActionCommand("add"); //$NON-NLS-1$
		addItem_.addActionListener(this);
		addItem_.setSelected(true);
		group.add(addItem_);
		++c.gridy;
		add(addItem_,c);
		
		editItem_ = new JRadioButton(Messages.getString("EditTrafficLightsControlPanel.edit")); //$NON-NLS-1$
		editItem_.setActionCommand("edit"); //$NON-NLS-1$
		editItem_.addActionListener(this);
		group.add(editItem_);
		++c.gridy;
		add(editItem_,c);
	
		editOneSignal_ = new JRadioButton(Messages.getString("EditTrafficLightsControlPanel.editOneSignal")); //$NON-NLS-1$
		editOneSignal_.setActionCommand("editOneSignal"); //$NON-NLS-1$
		editOneSignal_.addActionListener(this);
		group.add(editOneSignal_);
		++c.gridy;
		add(editOneSignal_,c);
		
		c.gridwidth = 1;
		c.insets = new Insets(5,5,5,5);
		
		//red-phase length
		redPhaseLengthLabel_ = new JLabel(Messages.getString("EditTrafficLightsControlPanel.redGreenPhaseLength")); //$NON-NLS-1$
		c.gridx = 0;
		++c.gridy;
		add(redPhaseLengthLabel_, c);		
		
		redGreenPhaseLength_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		redGreenPhaseLength_.setValue(10000);
		redGreenPhaseLength_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(redGreenPhaseLength_,c);
		
		//yellow-phase length
		yellowPhaseLengthLabel_ = new JLabel(Messages.getString("EditTrafficLightsControlPanel.yellowPhaseLength")); //$NON-NLS-1$
		c.gridx = 0;
		++c.gridy;
		add(yellowPhaseLengthLabel_, c);		

		yellowPhaseLength_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		yellowPhaseLength_.setValue(2000);
		yellowPhaseLength_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(yellowPhaseLength_,c);
		
		//priority-crossing difference
		crossingPriorityDifferenceLengthLabel_ = new JLabel(Messages.getString("EditTrafficLightsControlPanel.crossingPriorityDifferenceLength")); //$NON-NLS-1$
		c.gridx = 0;
		++c.gridy;
		add(crossingPriorityDifferenceLengthLabel_, c);		
		
		crossingPriorityDifferenceLength_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		crossingPriorityDifferenceLength_.setValue(2000);
		crossingPriorityDifferenceLength_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(crossingPriorityDifferenceLength_,c);		
		
		//edit one signal panel:
		c.gridwidth = 2;
		c.gridx = 0;
		chooseTrafficSignal_ = new JComboBox<Street>();
		chooseTrafficSignal_.setName("chooseTrafficSignal");		
		chooseTrafficSignal_.addActionListener(this);
		add(chooseTrafficSignal_, c);
		chooseTrafficSignal_.setVisible(false);
		
		
		c.gridwidth = 1;
		c.gridx = 0;
		++c.gridy;
		colorPreview_ = new JPanel();	
		colorPreview_.setBackground(Color.black);
		colorPreview_.setSize(10, 10);
		add(colorPreview_,c);
		colorPreview_.setVisible(false);
		
		c.gridx = 1;
		switchSignalState_ = ButtonCreator.getJButton("addTrafficLight.png", "switchTrafficLight", Messages.getString("EditTrafficLightsControlPanel.addTrafficLight"), this);
		add(switchSignalState_,c);
		switchSignalState_.setVisible(false);
		
		//add buttons
		c.gridx = 0;
		c.gridwidth = 2;
		++c.gridy;
		createTrafficLight_ = ButtonCreator.getJButton("addTrafficLight.png", "addTrafficLight", Messages.getString("EditTrafficLightsControlPanel.addTrafficLight"), this);
		add(createTrafficLight_,c);
		
		c.gridx = 0;
		++c.gridy;
		deleteTrafficLight_ = ButtonCreator.getJButton("delTrafficLight.png", "deleteTrafficLight", Messages.getString("EditTrafficLightsControlPanel.deleteTrafficLight"), this);
		deleteTrafficLight_.setVisible(false);
		add(deleteTrafficLight_,c);
		
		c.gridx = 0;
		c.gridwidth = 2;
		++c.gridy;
		deleteAllTrafficLights_ = ButtonCreator.getJButton("deleteAll.png", "clearTrafficLights", Messages.getString("EditTrafficLightsControlPanel.clearTrafficLights"), this);
		add(deleteAllTrafficLights_,c);
				
		addNote_ = new TextAreaLabel(Messages.getString("EditTrafficLightsControlPanel.noteAdd")); //$NON-NLS-1$
		++c.gridy;
		c.gridx = 0;
		add(addNote_, c);
		
		saveNote_ = new TextAreaLabel(Messages.getString("EditTrafficLightsControlPanel.noteSave")); //$NON-NLS-1$
		++c.gridy;
		c.gridx = 0;
		add(saveNote_, c);
		saveNote_.setVisible(false);
		
		//to consume the rest of the space
		space_ = new JPanel();
		c.gridwidth = 2;
		c.weighty = 1.0;
		++c.gridy;
		space_.setOpaque(false);
		add(space_, c);
	}
	
	/**
	 * Sets the visibility of GUI elements for adding traffic lights
	 */
	public void setGuiElements(String command){	
		if(command.equals("add")){
			for(Object o:this.getComponents()){
				((Component) o).setVisible(false);
			}
			addItem_.setVisible(true);
			editItem_.setVisible(true);
			editOneSignal_.setVisible(true);
			redPhaseLengthLabel_.setVisible(true);
			redGreenPhaseLength_.setVisible(true);
			yellowPhaseLengthLabel_.setVisible(true);
			yellowPhaseLength_.setVisible(true);
			crossingPriorityDifferenceLengthLabel_.setVisible(true);
			crossingPriorityDifferenceLength_.setVisible(true);
			createTrafficLight_.setVisible(true);
			deleteAllTrafficLights_.setVisible(true);
			addNote_.setVisible(true);
			space_.setVisible(true);			
		}
		else if(command.equals("edit")){
			for(Object o:this.getComponents()){
				((Component) o).setVisible(false);
			}
			addItem_.setVisible(true);
			editItem_.setVisible(true);
			editOneSignal_.setVisible(true);
			redPhaseLengthLabel_.setVisible(true);
			redGreenPhaseLength_.setVisible(true);
			yellowPhaseLengthLabel_.setVisible(true);
			yellowPhaseLength_.setVisible(true);
			crossingPriorityDifferenceLengthLabel_.setVisible(true);
			crossingPriorityDifferenceLength_.setVisible(true);
			createTrafficLight_.setVisible(true);
			deleteTrafficLight_.setVisible(true);
			deleteAllTrafficLights_.setVisible(true);
			saveNote_.setVisible(true);
			space_.setVisible(true);
		}
		else if(command.equals("editOneSignal")){
			for(Object o:this.getComponents()){
				((Component) o).setVisible(false);
			}
			addItem_.setVisible(true);
			editItem_.setVisible(true);
			editOneSignal_.setVisible(true);
			space_.setVisible(true);
			chooseTrafficSignal_.setVisible(true);
			colorPreview_.setVisible(true);	
			switchSignalState_.setVisible(true);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		//action when the add RadioButton is selected
		if("add".equals(command)){	
			setGuiElements("add");
			redGreenPhaseLength_.setValue(10000);
			yellowPhaseLength_.setValue(3000);
			crossingPriorityDifferenceLength_.setValue(5000);
			Renderer.getInstance().ReRender(true, false);
		}
		//action when the edit RadioButton is selected
		else if("edit".equals(command)){	
			setGuiElements("edit");
			if(actualJunction_ != null && actualJunction_.getNode().getTrafficLight_() != null){
				redGreenPhaseLength_.setValue(actualJunction_.getNode().getTrafficLight_().getRedPhaseLength());
				yellowPhaseLength_.setValue(actualJunction_.getNode().getTrafficLight_().getYellowPhaseLength());
				crossingPriorityDifferenceLength_.setValue(actualJunction_.getNode().getTrafficLight_().getGreenPhaseLength() - actualJunction_.getNode().getTrafficLight_().getRedPhaseLength());
			}
			else {
				redGreenPhaseLength_.setValue(0);
				yellowPhaseLength_.setValue(0);
				crossingPriorityDifferenceLength_.setValue(0);
			}
			Renderer.getInstance().ReRender(true, false);
		}
		//action when the edit one signal RadioButton is selected
		else if("editOneSignal".equals(command)){	
			setGuiElements("editOneSignal");
			refreshTrafficSignals();
			Renderer.getInstance().ReRender(true, false);
		}
		else if("addTrafficLight".equals(command) && addItem_.isSelected()){ //$NON-NLS-1$
			//check if junction is marked
	
			if(actualJunction_ != null && Renderer.getInstance().getMarkedJunction_().equals(actualJunction_)){ 
				int tmpRedPhaseLength = ((Number)redGreenPhaseLength_.getValue()).intValue();
				int tmpYellowPhaseLength = ((Number)yellowPhaseLength_.getValue()).intValue();
				int tmpGreenPhaseLength = ((Number)redGreenPhaseLength_.getValue()).intValue() + ((Number)crossingPriorityDifferenceLength_.getValue()).intValue();
				if(tmpRedPhaseLength != 0 && tmpYellowPhaseLength != 0 &&  tmpGreenPhaseLength != 0){
					//create new TrafficLight object
					actualJunction_.getNode().setTrafficLight_(new TrafficLight(tmpRedPhaseLength, tmpYellowPhaseLength, tmpGreenPhaseLength, actualJunction_));
					actualJunction_.getNode().setHasTrafficSignal_(true);
					Renderer.getInstance().ReRender(true, false);
				}
				else {
					JOptionPane.showMessageDialog(null, Messages.getString("EditTrafficLightsControlPanel.msgBoxNOTSavedText"), "Information", JOptionPane.INFORMATION_MESSAGE);
				}
			}	
		}
		else if("addTrafficLight".equals(command) && editItem_.isSelected()){ //$NON-NLS-1$
			if(actualJunction_ != null && Renderer.getInstance().getMarkedJunction_().equals(actualJunction_) && actualJunction_.getNode().getTrafficLight_() != null){ 
				//get reference to the traffic light object
				TrafficLight tmpTrafficLight = actualJunction_.getNode().getTrafficLight_();
				int tmpRedPhaseLength = ((Number)redGreenPhaseLength_.getValue()).intValue();
				int tmpYellowPhaseLength = ((Number)yellowPhaseLength_.getValue()).intValue();
				int tmpGreenPhaseLength = ((Number)redGreenPhaseLength_.getValue()).intValue() + ((Number)crossingPriorityDifferenceLength_.getValue()).intValue();
				
				if(tmpRedPhaseLength != 0 && tmpYellowPhaseLength != 0 && tmpGreenPhaseLength != 0){
					tmpTrafficLight.setRedPhaseLength(tmpRedPhaseLength);		
					tmpTrafficLight.setYellowPhaseLength(tmpYellowPhaseLength);	
					tmpTrafficLight.setGreenPhaseLength(tmpGreenPhaseLength);	
				}
				else {
					JOptionPane.showMessageDialog(null, Messages.getString("EditTrafficLightsControlPanel.msgBoxNOTSavedText"), "Information", JOptionPane.INFORMATION_MESSAGE);
				}
				Renderer.getInstance().ReRender(true, false);
			}
		}
		//action when the deleteTrafficLight Button is pressed
		else if("deleteTrafficLight".equals(command)){ //$NON-NLS-1$
			if(actualJunction_ != null && Renderer.getInstance().getMarkedJunction_().equals(actualJunction_) && actualJunction_.getNode().getTrafficLight_() != null){ 
				actualJunction_.delTrafficLight();
				Renderer.getInstance().ReRender(true, false);
			}
		}
		//delete all traffic lights
		else if("clearTrafficLights".equals(command)){ //$NON-NLS-1$	
			if(JOptionPane.showConfirmDialog(null, Messages.getString("EditTrafficLightsControlPanel.msgBoxClearAll"), "", JOptionPane.YES_NO_OPTION) == 0){
				Map.getInstance().clearTrafficLights();
				Renderer.getInstance().ReRender(true, false);
			}
		}	
		else if ("comboBoxChanged".equals(command)){
			selectedStreet_ = (Street) chooseTrafficSignal_.getSelectedItem();
			Renderer.getInstance().setMarkedStreet(selectedStreet_);
			
			if(actualJunction_.getNode().equals(selectedStreet_.getStartNode())){
				if(selectedStreet_.getStartNodeTrafficLightState() == -1) colorPreview_.setBackground(Color.gray);
				else if (selectedStreet_.getStartNodeTrafficLightState() == 0) colorPreview_.setBackground(Color.green);
				else colorPreview_.setBackground(Color.red);			
			}
			else{
				if(selectedStreet_.getEndNodeTrafficLightState() == -1) colorPreview_.setBackground(Color.gray);
				else if (selectedStreet_.getEndNodeTrafficLightState() == 0) colorPreview_.setBackground(Color.green);
				else colorPreview_.setBackground(Color.red);				
			}
			
			Renderer.getInstance().ReRender(true, false);
		}
		else if ("switchTrafficLight".equals(command)){
			selectedStreet_ = (Street) chooseTrafficSignal_.getSelectedItem();
			
			if(actualJunction_.getNode().equals(selectedStreet_.getStartNode())){
				if(selectedStreet_.getStartNodeTrafficLightState() == -1) selectedStreet_.setStartNodeTrafficLightState(0);
				else if (selectedStreet_.getStartNodeTrafficLightState() == 0) selectedStreet_.setStartNodeTrafficLightState(4);
				else  selectedStreet_.setStartNodeTrafficLightState(-1);			
			}
			else{
				if(selectedStreet_.getEndNodeTrafficLightState() == -1)  selectedStreet_.setEndNodeTrafficLightState(0);
				else if (selectedStreet_.getEndNodeTrafficLightState() == 0) selectedStreet_.setEndNodeTrafficLightState(4);
				else selectedStreet_.setEndNodeTrafficLightState(-1);				
			}
	
			if(actualJunction_.getNode().equals(selectedStreet_.getStartNode())){
				if(selectedStreet_.getStartNodeTrafficLightState() == -1) colorPreview_.setBackground(Color.gray);
				else if (selectedStreet_.getStartNodeTrafficLightState() == 0) colorPreview_.setBackground(Color.green);
				else colorPreview_.setBackground(Color.red);			
			}
			else{
				if(selectedStreet_.getEndNodeTrafficLightState() == -1) colorPreview_.setBackground(Color.gray);
				else if (selectedStreet_.getEndNodeTrafficLightState() == 0) colorPreview_.setBackground(Color.green);
				else colorPreview_.setBackground(Color.red);				
			}
			
			//save exceptions to object
			
			Street[] tmpStreet = actualJunction_.getNode().getTrafficLight_().getStreets_();
			
			for(int i = 0; i < tmpStreet.length; i++){
					if(tmpStreet[i].getEndNode().equals(actualJunction_.getNode())){
						actualJunction_.getNode().getStreetHasException_()[i] = tmpStreet[i].getEndNodeTrafficLightState();
					}
					else if(tmpStreet[i].getStartNode().equals(actualJunction_.getNode())){
						actualJunction_.getNode().getStreetHasException_()[i] = tmpStreet[i].getStartNodeTrafficLightState();					
					}
			}
			
			Renderer.getInstance().ReRender(true, false);
		}			
	}

	/**
	 * Receives a mouse event.
	 * 
	 * @param x	the x coordinate (in map scale)
	 * @param y	the y coordinate (in map scale)
	 */
	public void receiveMouseEvent(int x, int y){
		//get selected (nearest) junction and set as marked on renderer
		actualJunction_ = MapHelper.findNearestNode(x, y, 20000, new long[1]).getJunction();
		selectedStreet_ = null;
		
		if(editItem_.isSelected()){
			if(actualJunction_ != null && actualJunction_.getNode().getTrafficLight_() != null){
				redGreenPhaseLength_.setValue(actualJunction_.getNode().getTrafficLight_().getRedPhaseLength());
				yellowPhaseLength_.setValue(actualJunction_.getNode().getTrafficLight_().getYellowPhaseLength());
				crossingPriorityDifferenceLength_.setValue(actualJunction_.getNode().getTrafficLight_().getGreenPhaseLength() - actualJunction_.getNode().getTrafficLight_().getRedPhaseLength());
			}
			else {
				redGreenPhaseLength_.setValue(0);
				yellowPhaseLength_.setValue(0);
				crossingPriorityDifferenceLength_.setValue(0);
			}
		}
		if(editOneSignal_.isSelected()){
			refreshTrafficSignals();
		}
		
		Renderer.getInstance().setMarkedJunction_(actualJunction_);
		Renderer.getInstance().ReRender(false, false);
	}
	
	/**
	 * updates the traffic signal combobox
	 */
	public void refreshTrafficSignals(){
		chooseTrafficSignal_.removeActionListener(this);  //important: Remove ActionListener before removing all items from Combobox
		chooseTrafficSignal_.removeAllItems();
		
		if(actualJunction_ != null && actualJunction_.getNode().getTrafficLight_() != null){
			for(Street s : actualJunction_.getNode().getTrafficLight_().getStreets_())chooseTrafficSignal_.addItem(s);
			chooseTrafficSignal_.setVisible(true);
			colorPreview_.setVisible(true);	
			switchSignalState_.setVisible(true);
			
			selectedStreet_ = (Street) chooseTrafficSignal_.getItemAt(0);
			Renderer.getInstance().setMarkedStreet(selectedStreet_);
			if(actualJunction_.getNode().equals(selectedStreet_.getStartNode())){
				if(selectedStreet_.getStartNodeTrafficLightState() == -1) colorPreview_.setBackground(Color.gray);
				else if (selectedStreet_.getStartNodeTrafficLightState() == 0) colorPreview_.setBackground(Color.green);
				else colorPreview_.setBackground(Color.red);			
			}
			else{
				if(selectedStreet_.getEndNodeTrafficLightState() == -1) colorPreview_.setBackground(Color.gray);
				else if (selectedStreet_.getEndNodeTrafficLightState() == 0) colorPreview_.setBackground(Color.green);
				else colorPreview_.setBackground(Color.red);				
			}	
		}
		else{
			selectedStreet_ = null;
			chooseTrafficSignal_.setVisible(false);
			colorPreview_.setVisible(false);	
			switchSignalState_.setVisible(false);
		}
		chooseTrafficSignal_.addActionListener(this);
		
		Renderer.getInstance().ReRender(true, true);
	}
	
	public void mouseClicked(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}
	
	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	/**
	 * @param selectedStreet_ the selectedStreet_ to set
	 */
	public void setSelectedStreet_(Street selectedStreet_) {
		this.selectedStreet_ = selectedStreet_;
	}

	/**
	 * @return the selectedStreet_
	 */
	public Street getSelectedStreet_() {
		return selectedStreet_;
	}

}
