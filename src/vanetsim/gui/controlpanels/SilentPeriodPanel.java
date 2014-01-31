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
import java.text.NumberFormat;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

import javax.swing.JPanel;

import vanetsim.localization.Messages;
import vanetsim.scenario.Vehicle;

/**
 * This class represents the control panel for adding mix zones.
 */
public class SilentPeriodPanel extends JPanel implements ActionListener, FocusListener{

	/** The necessary constant for serializing. */
	private static final long serialVersionUID = -8294786435746799533L;
	
	/** CheckBox to choose if Silent Periods are enabled */
	private final JCheckBox enableSilentPeriods_;	
	
	/** JLabel to describe enableSilentPeriods_ checkbox */
	private final JLabel enableSilentPeriodsLabel_;	
	
	/** The input field for the silent period duration */
	private final JFormattedTextField silentPeriodDuration_;

	/** JLabel to describe silentPeriodDuration_ textfield */
	private final JLabel silentPeriodDurationLabel_;	
	
	/** The input field for the silent period frequency */
	private final JFormattedTextField silentPeriodFrequency_;
	
	/** JLabel to describe silentPeriodFrequency_ textfield */
	private final JLabel silentPeriodFrequencyLabel_;	
	
	
	/**
	 * Constructor, creating GUI items.
	 */
	public SilentPeriodPanel(){
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
		
		
		c.gridwidth = 1;
		c.insets = new Insets(5,5,5,5);
		
		c.gridx = 0;
		silentPeriodDurationLabel_ = new JLabel(Messages.getString("SilentPeriodPanel.duration")); //$NON-NLS-1$
		++c.gridy;
		add(silentPeriodDurationLabel_,c);		
		silentPeriodDuration_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		silentPeriodDuration_.setValue(3000);

		silentPeriodDuration_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		silentPeriodDuration_.addFocusListener(this);
		add(silentPeriodDuration_,c);
		
		c.gridx = 0;
		silentPeriodFrequencyLabel_ = new JLabel(Messages.getString("SilentPeriodPanel.frequency")); //$NON-NLS-1$
		++c.gridy;
		add(silentPeriodFrequencyLabel_,c);		
		silentPeriodFrequency_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		silentPeriodFrequency_.setValue(10000);

		silentPeriodFrequency_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		silentPeriodFrequency_.addFocusListener(this);
		add(silentPeriodFrequency_,c);
		
		c.gridx = 0;
		enableSilentPeriodsLabel_ = new JLabel(Messages.getString("SilentPeriodPanel.enable")); //$NON-NLS-1$
		++c.gridy;
		add(enableSilentPeriodsLabel_,c);		
		enableSilentPeriods_ = new JCheckBox();
		enableSilentPeriods_.setSelected(false);
		enableSilentPeriods_.setActionCommand("enableSilentPeriods"); //$NON-NLS-1$
		c.gridx = 1;
		enableSilentPeriods_.addFocusListener(this);
		add(enableSilentPeriods_,c);
		enableSilentPeriods_.addActionListener(this);	
		
		
		//to consume the rest of the space
		c.weighty = 1.0;
		++c.gridy;
		JPanel space = new JPanel();
		space.setOpaque(false);
		add(space, c);
	}
	
	public void saveAttributes(){
		Vehicle.setTIME_BETWEEN_SILENT_PERIODS(((Number)silentPeriodFrequency_.getValue()).intValue());
		Vehicle.setTIME_OF_SILENT_PERIODS(((Number)silentPeriodDuration_.getValue()).intValue());
		Vehicle.setSilentPeriodsOn(enableSilentPeriods_.isSelected());
	}
	
	public void loadAttributes(){
		silentPeriodFrequency_.setValue(Vehicle.getTIME_BETWEEN_SILENT_PERIODS());
		silentPeriodDuration_.setValue(Vehicle.getTIME_OF_SILENT_PERIODS());
		enableSilentPeriods_.setSelected(Vehicle.isSilentPeriodsOn());
	}
	
	/**
	 * An implemented <code>ActionListener</code> which performs all needed actions when a <code>JCheckBox</code> or <code>JButton</code>
	 * is clicked.
	 * 
	 * @param e	an <code>ActionEvent</code>
	 */	
	public void actionPerformed(ActionEvent e) {
	//	String command = e.getActionCommand();

		
	}

	public JCheckBox getEnableSilentPeriods_() {
		return enableSilentPeriods_;
	}
	
	public JFormattedTextField getSilentPeriodDuration_() {
		return silentPeriodDuration_;
	}

	public JFormattedTextField getSilentPeriodFrequency_() {
		return silentPeriodFrequency_;
	}

	@Override
	public void focusGained(FocusEvent arg0) {
		// TODO Auto-generated method stub
		saveAttributes();
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		// TODO Auto-generated method stub
		saveAttributes();
	}

}