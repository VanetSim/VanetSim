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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JPanel;

import vanetsim.gui.helpers.TextAreaLabel;
import vanetsim.localization.Messages;
import vanetsim.simulation.WorkerThread;

/**
 * This class represents the control panel for selected traffic models or trace data
 */
public class EditTrafficModelControlPanel extends JPanel implements  ActionListener{

	/** The necessary constant for serializing. */
	private static final long serialVersionUID = -8394785435746199543L;

	/** RadioButton to add vehicle. */
	JRadioButton selectModel_;
	
	/** RadioButton to edit vehicle. */
	JRadioButton selectTraces_;
	
	/** A JComboBox to switch between traffic models. */
	private JComboBox<String> chooseTrafficModel_;
	
	/** A Label for the traffic model ComboBox. */
	private JLabel chooseTrafficModelLabel_;
	
	/** A JComboBox to switch between traces. */
	private JComboBox<String> chooseTraces_;
	
	/** A Label for the traces ComboBox. */
	private JLabel chooseTracesLabel_;
	
	/** Note to describe functionality */
	TextAreaLabel Note;
	
	
	/**
	 * Constructor, creating GUI items.
	 */
	public EditTrafficModelControlPanel() {
		setLayout(new GridBagLayout());
		
		// global layout settings
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		
		c.gridwidth = 1;

		c.insets = new Insets(5,5,5,5);
		
		c.gridx = 0;
		
		// Radio buttons to select add, edit or delete mode
		ButtonGroup group = new ButtonGroup();
		selectModel_ = new JRadioButton(Messages.getString("EditTrafficControlPanel.model")); //$NON-NLS-1$
		selectModel_.setActionCommand("model"); //$NON-NLS-1$
		selectModel_.addActionListener(this);
		selectModel_.setSelected(true);
		group.add(selectModel_);
		++c.gridy;
		add(selectModel_,c);
				
		selectTraces_ = new JRadioButton(Messages.getString("EditTrafficControlPanel.traces")); //$NON-NLS-1$
		selectTraces_.setActionCommand("traces"); //$NON-NLS-1$
		selectTraces_.addActionListener(this);
		group.add(selectTraces_);
		++c.gridy;
		add(selectTraces_,c);
			
		
		c.gridx = 0;
		chooseTrafficModelLabel_ = new JLabel(Messages.getString("EditTrafficControlPanel.comboBoxModel")); //$NON-NLS-1$
		++c.gridy;
		add(chooseTrafficModelLabel_,c);
		chooseTrafficModel_ = new JComboBox<String>();
		chooseTrafficModel_.setActionCommand("chooseTrafficModel");
		chooseTrafficModel_.addItem("VANETSim classic");
		chooseTrafficModel_.addItem("IDM/MOBIL");
		chooseTrafficModel_.addActionListener(this);
		c.gridx = 1;
		add(chooseTrafficModel_, c);
		
		
		c.gridx = 0;
		chooseTracesLabel_ = new JLabel(Messages.getString("EditTrafficControlPanel.comboBoxTraces")); //$NON-NLS-1$
		++c.gridy;
		add(chooseTracesLabel_,c);
		chooseTraces_ = new JComboBox<String>();
		chooseTraces_.setActionCommand("chooseTraces");
		chooseTraces_.addItem("sjtu taxi traces");
		chooseTraces_.addItem("San Francisco traces");
		chooseTraces_.addActionListener(this);
		c.gridx = 1;
		add(chooseTraces_, c);
		chooseTraces_.setVisible(false);
		chooseTracesLabel_.setVisible(false);
		
		
		
		//to consume the rest of the space
		c.weighty = 1.0;
		++c.gridy;
		JPanel space = new JPanel();
		space.setOpaque(false);
		add(space, c);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		String command = arg0.getActionCommand();

		
		if("model".equals(command)){	
			//display model selection related gui elements
			if(((String)chooseTrafficModel_.getSelectedItem()).equals("VANETSim classic")) WorkerThread.setSimulationMode_(1);
			else if(((String)chooseTrafficModel_.getSelectedItem()).equals("IDM/MOBIL")) WorkerThread.setSimulationMode_(2);
			
			chooseTrafficModelLabel_.setVisible(true);
			chooseTrafficModel_.setVisible(true);
			chooseTraces_.setVisible(false);
			chooseTracesLabel_.setVisible(false);
		}
		else if("traces".equals(command)){
			//display traces selection related gui elements
			
			if(((String)chooseTraces_.getSelectedItem()).equals("sjtu taxi traces")) WorkerThread.setSimulationMode_(3);
			else if(((String)chooseTraces_.getSelectedItem()).equals("San Francisco traces")) WorkerThread.setSimulationMode_(4);
			chooseTraces_.setVisible(true);
			chooseTracesLabel_.setVisible(true);
			chooseTrafficModel_.setVisible(false);
			chooseTrafficModelLabel_.setVisible(false);
		}
		else if("chooseTrafficModel".equals(command)){
			
			if(((String)chooseTrafficModel_.getSelectedItem()).equals("VANETSim classic")) WorkerThread.setSimulationMode_(1);
			else if(((String)chooseTrafficModel_.getSelectedItem()).equals("IDM/MOBIL")) WorkerThread.setSimulationMode_(2);
		}
		else if("chooseTraces".equals(command)){
			if(((String)chooseTraces_.getSelectedItem()).equals("sjtu taxi traces")) WorkerThread.setSimulationMode_(3);
			else if(((String)chooseTraces_.getSelectedItem()).equals("San Francisco traces")) WorkerThread.setSimulationMode_(4);
		}		
	}

}