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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import vanetsim.gui.helpers.ReRenderManager;
import vanetsim.gui.helpers.TextAreaLabel;
import vanetsim.localization.Messages;
import vanetsim.scenario.Vehicle;
import vanetsim.scenario.RSU;
import vanetsim.simulation.SimulationMaster;

/**
 * The control panel for changing some basic settings.
 */
public class EditSettingsControlPanel extends JPanel implements ItemListener, PropertyChangeListener {
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = -7820554929526157630L;

	/** A combo box for choosing the routing mode */
	private final JComboBox<String> routingModeChoice_;

	/** A CheckBox for enabling/disabling recycling of vehicles. */
	private final JCheckBox recyclingCheckBox_;
	
	/** A CheckBox for enabling/disabling communication. */
	private final JCheckBox communicationCheckBox_;
	
	/** The panel which is shown if communication is enabled. */
	private final JPanel communicationPanel_;
	
	/** An input field for setting the communication interval. */
	private JFormattedTextField communicationInterval_;
	
	/** The panel which is shown if beacons are enabled. */
	private JPanel beaconPanel_;
	
	/** A CheckBox for enabling/disabling beacons. */
	private JCheckBox beaconsCheckBox_;
	
	/** An input field for setting the beacons interval. */
	private JFormattedTextField beaconInterval_;
	
	/** A CheckBox for enabling/disabling the global infrastructure. */
	private JCheckBox globalInfrastructureCheckBox_;
	
	/** A CheckBox for enabling/disabling the mix zones. */
	private JCheckBox mixZonesCheckBox_;
	
	/** The panel which is shown if mix zones are enabled. */
	private JPanel mixZonePanel_;
	
	/** A CheckBox for enabling/disabling the fallback communication mode in mix zones. */
	private JCheckBox fallbackInMixZonesCheckBox_;
	
	/** The panel which is shown if fallback communication mode in mix zones is enabled */
	private JPanel fallbackInMixZonesPanel_;
	
	/** A CheckBox for enabling/disabling if only flooding messages are sent in mix zones fallback mode. */
	private JCheckBox fallbackInMixZonesFloodingOnlyCheckBox_;
	
	/** An input field for setting the radius of the mix zones. */
	private JFormattedTextField mixZoneRadius_;
	
	/**
	 * Constructor.
	 */
	public EditSettingsControlPanel(){
		setLayout(new GridBagLayout());

		// global layout settings
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.insets = new Insets(5,5,5,5);
		
		JLabel jLabel1 = new JLabel(Messages.getString("EditSettingsControlPanel.routingBasedOn")); //$NON-NLS-1$
		add(jLabel1,c);
		c.gridx = 1;
		c.weightx = 0;
		String[] choices = {Messages.getString("EditSettingsControlPanel.distance"), Messages.getString("EditSettingsControlPanel.time")}; //$NON-NLS-1$ //$NON-NLS-2$
		routingModeChoice_ = new JComboBox<String>(choices);
		routingModeChoice_.setSelectedIndex(1);
		routingModeChoice_.addItemListener(this);
		add(routingModeChoice_, c);
		++c.gridy;
		c.gridwidth = 2;
		c.gridx = 0;
		c.insets = new Insets(0,5,5,5);
		jLabel1 = new JLabel(Messages.getString("EditSettingsControlPanel.routingNote")); //$NON-NLS-1$
		add(jLabel1,c);
		c.insets = new Insets(5,5,5,5);
		
		++c.gridy;
		recyclingCheckBox_ = new JCheckBox(Messages.getString("EditSettingsControlPanel.enableRecycling"), true); //$NON-NLS-1$
		recyclingCheckBox_.setSelected(true);
		recyclingCheckBox_.addItemListener(this);		
		add(recyclingCheckBox_,c);		
		
		communicationCheckBox_ = new JCheckBox(Messages.getString("EditSettingsControlPanel.enableCommunication"), true); //$NON-NLS-1$
		communicationCheckBox_.addItemListener(this);
		++c.gridy;
		add(communicationCheckBox_,c);
		
		++c.gridy;
		c.insets = new Insets(0,10,0,5);
		communicationPanel_ = createCommunicationPanel();
		add(communicationPanel_,c);
		
		//to consume the rest of the space
		c.weighty = 1.0;
		++c.gridy;
		
		JPanel panel = new JPanel();
		
		panel.setOpaque(false);
		add(panel, c);
	}
	
	/**
	 * Creates the panel which is shown when communication is enabled.
	 * 
	 * @return the communication panel
	 */
	private final JPanel createCommunicationPanel(){
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.insets = new Insets(5,0,5,0);
		
		JLabel jLabel1 = new JLabel(Messages.getString("EditSettingsControlPanel.communicationInterval")); //$NON-NLS-1$
		panel.add(jLabel1,c);		
		communicationInterval_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		communicationInterval_.setPreferredSize(new Dimension(60,20));
		communicationInterval_.setValue(160);
		communicationInterval_.addPropertyChangeListener("value", this); //$NON-NLS-1$
		c.gridx = 1;
		c.weightx = 0;
		panel.add(communicationInterval_,c);

		c.gridx = 0;
		c.gridwidth = 2;
		++c.gridy;
		beaconsCheckBox_ = new JCheckBox(Messages.getString("EditSettingsControlPanel.enableBeacons"), true); //$NON-NLS-1$
		beaconsCheckBox_.addItemListener(this);
		panel.add(beaconsCheckBox_,c);
		
		++c.gridy;
		c.insets = new Insets(0,10,0,0);
		beaconPanel_ = createBeaconPanel();
		panel.add(beaconPanel_,c);
		
		++c.gridy;
		c.insets = new Insets(5,0,5,0);
		mixZonesCheckBox_ = new JCheckBox(Messages.getString("EditSettingsControlPanel.enableMixZones"), true); //$NON-NLS-1$
		mixZonesCheckBox_.addItemListener(this);
		panel.add(mixZonesCheckBox_,c);
		
		++c.gridy;
		c.insets = new Insets(0,10,0,0);
		mixZonePanel_ = createMixPanel();
		panel.add(mixZonePanel_,c);		
		
		TextAreaLabel jlabel1 = new TextAreaLabel(Messages.getString("EditSettingsControlPanel.intervalNote1") + SimulationMaster.TIME_PER_STEP +  Messages.getString("EditSettingsControlPanel.intervalNote2")); //$NON-NLS-1$ //$NON-NLS-2$
		++c.gridy;
		c.gridx = 0;
		c.gridwidth = 2;
		c.insets = new Insets(15,0,5,0);
		panel.add(jlabel1, c);
		return panel;
	}
	
	/**
	 * Creates the panel which is shown when beacon sending is enabled.
	 * 
	 * @return the beacon panel
	 */
	private final JPanel createBeaconPanel(){
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.insets = new Insets(5,0,5,0);
		
		JLabel jLabel1 = new JLabel(Messages.getString("EditSettingsControlPanel.beaconInterval")); //$NON-NLS-1$
		panel.add(jLabel1,c);		
		beaconInterval_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		beaconInterval_.setPreferredSize(new Dimension(60,20));
		beaconInterval_.setValue(240);
		beaconInterval_.addPropertyChangeListener("value", this); //$NON-NLS-1$
		c.gridx = 1;
		c.weightx = 0;
		panel.add(beaconInterval_,c);
		
		
		c.gridx = 0;
		c.gridwidth = 2;
		++c.gridy;
		globalInfrastructureCheckBox_ = new JCheckBox(Messages.getString("EditSettingsControlPanel.enableInfrastructure"), true); //$NON-NLS-1$
		globalInfrastructureCheckBox_.addItemListener(this);
		// Disabled as it's not yet implemented
		// beaconPanel_.add(globalInfrastructureCheckBox_,c);
		
		return panel;
	}
	
	/**
	 * Creates the panel which is shown when mix zones are enabled.
	 * 
	 * @return the mix zones panel
	 */
	private final JPanel createMixPanel(){
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.insets = new Insets(5,0,5,0);
		
		JLabel jLabel1 = new JLabel(Messages.getString("EditSettingsControlPanel.mixZoneSize")); //$NON-NLS-1$
		panel.add(jLabel1,c);		
		mixZoneRadius_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		mixZoneRadius_.setPreferredSize(new Dimension(60,20));
		mixZoneRadius_.setValue(100);
		mixZoneRadius_.addPropertyChangeListener("value", this); //$NON-NLS-1$
		c.gridx = 1;
		c.weightx = 0;
		panel.add(mixZoneRadius_,c);
		
		c.gridx = 0;
		c.gridwidth = 2;
		++c.gridy;
		fallbackInMixZonesCheckBox_ = new JCheckBox(Messages.getString("EditSettingsControlPanel.fallbackCommunicationInMixZones"), true); //$NON-NLS-1$
		fallbackInMixZonesCheckBox_.addItemListener(this);
		panel.add(fallbackInMixZonesCheckBox_,c);
		
		++c.gridy;
		fallbackInMixZonesPanel_ = createMixFallBackPanel();
		panel.add(fallbackInMixZonesPanel_,c);
		
		return panel;
	}
	
	/**
	 * Creates the panel which is shown when mix zones fallback communication mode is enabled.
	 * 
	 * @return the control panel
	 */
	private final JPanel createMixFallBackPanel(){
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.insets = new Insets(5,10,5,0);
		
		fallbackInMixZonesFloodingOnlyCheckBox_ = new JCheckBox(Messages.getString("EditSettingsControlPanel.falllbackCommunicationOnlyForFlooding"), true); //$NON-NLS-1$
		fallbackInMixZonesFloodingOnlyCheckBox_.addItemListener(this);
		panel.add(fallbackInMixZonesFloodingOnlyCheckBox_, c);
		
		return panel;
	}
	
	
	
	/**
	 * Sets a new state for for the communicationCheckBox.
	 * 
	 * @param state	the new state
	 */
	public void setCommunication(boolean state){
		communicationPanel_.setVisible(state);
		communicationCheckBox_.setSelected(state);
	}
	
	/**
	 * Sets a new state for the beaconsCheckBox.
	 * 
	 * @param state	the new state
	 */
	public void setBeacons(boolean state){
		beaconPanel_.setVisible(state);
		beaconsCheckBox_.setSelected(state);
	}
	
	/**
	 * Sets a new state for the mixZonesCheckBox.
	 * 
	 * @param state	the new state
	 */
	public void setMixZonesEnabled(boolean state){
		mixZonePanel_.setVisible(state);
		mixZonesCheckBox_.setSelected(state);
	}
	
	/**
	 * Sets a new state for the fallbackInMixZonesCheckbox.
	 * 
	 * @param state	the new state
	 */
	public void setMixZonesFallbackEnabled(boolean state){
		fallbackInMixZonesPanel_.setVisible(state);
		fallbackInMixZonesCheckBox_.setSelected(state);
	}
	
	/**
	 * Sets a new state for the fallbackInMixZonesFloodingOnlyCheckBox.
	 * 
	 * @param state	the new state
	 */
	public void setMixZonesFallbackFloodingOnly(boolean state){
		fallbackInMixZonesFloodingOnlyCheckBox_.setSelected(state);
	}
	
	/**
	 * Sets a new state for the recycling checkbox.
	 * 
	 * @param state	the new state
	 */
	public void setRecyclingEnabled(boolean state){
		recyclingCheckBox_.setSelected(state);
	}
	
	/**
	 * Sets a new state for the communication for the globalInfrastructureCheckBox.
	 * 
	 * @param state	the new state
	 */
	public void setGlobalInfrastructure(boolean state){
		globalInfrastructureCheckBox_.setSelected(state);
	}
	
	/**
	 * Sets a new value in the communication interval input field.
	 * 
	 * @param communicationInterval the new value (in ms)
	 */
	public void setCommunicationInterval(int communicationInterval){
		communicationInterval_.setValue(communicationInterval);
	}
	
	/**
	 * Sets a new value in the beacon interval input field.
	 * 
	 * @param beaconInterval the new value (in ms)
	 */
	public void setBeaconInterval(int beaconInterval){
		beaconInterval_.setValue(beaconInterval);
	}
	
	/**
	 * Sets a new value in the mix distance input field.
	 * 
	 * @param mixZoneRadius the new value (in cm)
	 */
	public void setMixZoneRadius(int mixZoneRadius){
		mixZoneRadius_.setValue(Math.round(mixZoneRadius/100.0));
	}
	
	/**
	 * Sets a new value for the routing mode choice field.
	 * 
	 * @param mode the new value
	 */
	public void setRoutingMode(int mode){
		routingModeChoice_.setSelectedIndex(mode);
	}
	
	/**
	 * Invoked when an item changes. Used for the JCheckBoxes and JComboBoxes.
	 * 
	 * @param e the change event
	 * 
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e){
		boolean state;
		if(e.getStateChange() == ItemEvent.SELECTED) state = true;
		else state = false;
		Object source = e.getSource();
		if(source == communicationCheckBox_){
			setCommunication(state);
			Vehicle.setCommunicationEnabled(state);
			RSU.setCommunicationEnabled(state);
		} else if(source == beaconsCheckBox_){
			setBeacons(state);
			Vehicle.setBeaconsEnabled(state);
			RSU.setBeaconsEnabled(state);
		} else if (source == mixZonesCheckBox_){
			setMixZonesEnabled(state);
			Vehicle.setMixZonesEnabled(state);
			ReRenderManager.getInstance().doReRender();
        } else if (source == globalInfrastructureCheckBox_){
			setGlobalInfrastructure(state);
		} else if (source == routingModeChoice_){
        	Vehicle.setRoutingMode(routingModeChoice_.getSelectedIndex());
        } else if (source == recyclingCheckBox_){
        	Vehicle.setRecyclingEnabled(state);
        } else if (source == fallbackInMixZonesCheckBox_){
        	setMixZonesFallbackEnabled(state);
        	Vehicle.setMixZonesFallbackEnabled(state);
        } else if (source == fallbackInMixZonesFloodingOnlyCheckBox_){
        	Vehicle.setMixZonesFallbackFloodingOnly(state);
        }
	}

	/**
	 * Invoked when a field which is associated with this PropertyListener is changed.
	 * 
	 * @param e	the change event
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent e) {
		Object source = e.getSource();
        if (source == communicationInterval_){
        	Vehicle.setCommunicationInterval(((Number)communicationInterval_.getValue()).intValue());
        	RSU.setCommunicationInterval(((Number)communicationInterval_.getValue()).intValue());
        } else if (source == beaconInterval_){
        	Vehicle.setBeaconInterval(((Number)beaconInterval_.getValue()).intValue());
        	RSU.setBeaconInterval(((Number)beaconInterval_.getValue()).intValue());
        } else if (source == mixZoneRadius_){
        	Vehicle.setMixZoneRadius(((Number)mixZoneRadius_.getValue()).intValue()*100);
        	ReRenderManager.getInstance().doReRender();
        }
	}	
}