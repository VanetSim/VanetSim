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

import vanetsim.gui.helpers.PrivacyLogWriter;
import vanetsim.localization.Messages;
import vanetsim.scenario.Vehicle;

/**
 * This class represents the control panel for adding mix zones.
 */
public class SlowPanel extends JPanel implements ActionListener, FocusListener{

	/** The necessary constant for serializing. */
	private static final long serialVersionUID = -8294786435746799533L;
	
	/** CheckBox to choose if Slow-Modell are enabled */
	private final JCheckBox enableSlow_;	

	/** JLabel to describe enableSlow_ checkbox */
	private final JLabel enableSlowLabel_;	
	
	/** The input field for min time until a pseudonym is changed*/
	private final JFormattedTextField timeToPseudonymChange_;

	/** JLabel to describe timeToPseudonymChange_ textfield */
	private final JLabel timeToPseudonymChangeLabel_;	
	
	/** The input field for the speed limit for the slow modell */
	private final JFormattedTextField slowSpeedLimit_;
	
	/** JLabel to describe slowSpeedLimit_ textfield */
	private final JLabel slowSpeedLimitLabel_;	
	
	
	/**
	 * Constructor, creating GUI items.
	 */
	public SlowPanel(){
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
		timeToPseudonymChangeLabel_ = new JLabel(Messages.getString("SlowPanel.timeToPseudonymChange")); //$NON-NLS-1$
		++c.gridy;
		add(timeToPseudonymChangeLabel_,c);		
		timeToPseudonymChange_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		timeToPseudonymChange_.setValue(3000);

		timeToPseudonymChange_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		timeToPseudonymChange_.addFocusListener(this);
		add(timeToPseudonymChange_,c);
		
		c.gridx = 0;
		slowSpeedLimitLabel_ = new JLabel(Messages.getString("SlowPanel.speedLimit")); //$NON-NLS-1$
		++c.gridy;
		add(slowSpeedLimitLabel_,c);		
		slowSpeedLimit_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		slowSpeedLimit_.setValue(30);

		slowSpeedLimit_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		slowSpeedLimit_.addFocusListener(this);
		add(slowSpeedLimit_,c);
		
		c.gridx = 0;
		enableSlowLabel_ = new JLabel(Messages.getString("SlowPanel.enable")); //$NON-NLS-1$
		++c.gridy;
		add(enableSlowLabel_,c);		
		enableSlow_ = new JCheckBox();
		enableSlow_.setSelected(false);
		enableSlow_.setActionCommand("enableSlow"); //$NON-NLS-1$
		c.gridx = 1;
		enableSlow_.addFocusListener(this);
		add(enableSlow_,c);
		enableSlow_.addActionListener(this);	
		
		
		//to consume the rest of the space
		c.weighty = 1.0;
		++c.gridy;
		JPanel space = new JPanel();
		space.setOpaque(false);
		add(space, c);
	}
	
	public void saveAttributes(){
		Vehicle.setTIME_TO_PSEUDONYM_CHANGE(((Number)timeToPseudonymChange_.getValue()).intValue());
		Vehicle.setSLOW_SPEED_LIMIT((int)Math.round(((Number)slowSpeedLimit_.getValue()).intValue()  * (100000.0/3600)));
		Vehicle.setSlowOn(enableSlow_.isSelected());
	}
	
	public void loadAttributes(){
		timeToPseudonymChange_.setValue(Vehicle.getTIME_TO_PSEUDONYM_CHANGE());
		slowSpeedLimit_.setValue((int)Math.round(Vehicle.getSLOW_SPEED_LIMIT() / (100000.0/3600)));
		enableSlow_.setSelected(Vehicle.isSlowOn());
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
	
	public static void writeSlowHeader(){
		PrivacyLogWriter.log("Slow speed limit:" + Vehicle.getSLOW_SPEED_LIMIT() + ":Time to pseudonym change:" + Vehicle.getTIME_TO_PSEUDONYM_CHANGE());
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
	
	public JCheckBox getEnableSlow_() {
		return enableSlow_;
	}

	public JFormattedTextField getTimeToPseudonymChange_() {
		return timeToPseudonymChange_;
	}

	public JFormattedTextField getSlowSpeedLimit_() {
		return slowSpeedLimit_;
	}

}