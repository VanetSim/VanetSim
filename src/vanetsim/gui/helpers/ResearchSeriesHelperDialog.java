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
package vanetsim.gui.helpers;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import vanetsim.gui.controlpanels.ResearchSeriesDialog;
import vanetsim.localization.Messages;

/**
 * A dialog to create,edit and delete vehicle type files.
 */


public final class ResearchSeriesHelperDialog extends JDialog implements ActionListener{
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = -2918735209479587896L;
	
	/** the mode of the new property */
	private final String mode_;
	
	/** the name of the new property */
	private final String command_;

	/** The field to display the property */
	private final JFormattedTextField property_ = new JFormattedTextField();
	
	/** The input field for the start value for the variation. */
	private final JFormattedTextField startValue_ = new JFormattedTextField(new DecimalFormat());
	
	/** The input field for the size of one step */
	private final JFormattedTextField stepSize_ = new JFormattedTextField(new DecimalFormat());
	
	/** The input field for the amount of steps */
	private JFormattedTextField stepAmount_;
	
	/** A Label for the JComboBox */
	private JLabel vehicleAmountVariationLabel_;
		
	/** CheckBox to add vehicle without variation */
	private final JCheckBox vehicleAmountVariation_;
	
	/** Note to display errors. */
	TextAreaLabel errorNote_;
	
	/**
	 * Constructor. Creating GUI items.
	 */
	public ResearchSeriesHelperDialog(String mode, String command){
	
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
		mode_ = mode;
		command_ = command;
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
		
		//start building gui

		
		c.gridx = 0;
		++c.gridy;		
		add(new JLabel(Messages.getString("ResearchSeriesHelperDialog.inputValues")), c); //$NON-NLS-1$
		
		++c.gridy;
		add(new JLabel(Messages.getString("ResearchSeriesHelperDialog.propertyTerm")), c); //$NON-NLS-1$
		c.gridx = 1;
		property_.setValue(command);
		property_.setEditable(false);
		property_.setPreferredSize(new Dimension(100,20));
		add(property_, c);
		
		c.gridx = 0;
		++c.gridy;
		add(new JLabel(Messages.getString("ResearchSeriesHelperDialog.startValue")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(startValue_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("ResearchSeriesHelperDialog.stepSize")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(stepSize_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("ResearchSeriesHelperDialog.stepAmount")), c); //$NON-NLS-1$
		c.gridx = 1;
		stepAmount_ =  new JFormattedTextField(NumberFormat.getIntegerInstance());
		add(stepAmount_, c);
		c.gridx = 0;
		
		c.gridwidth = 1;
		++c.gridy;
		c.gridx = 0;
		vehicleAmountVariationLabel_ = new JLabel(Messages.getString("ResearchSeriesHelperDialog.vehicleAmountVariation"));
		add(vehicleAmountVariationLabel_,c);	

		
		vehicleAmountVariation_ = new JCheckBox();
		vehicleAmountVariation_.setSelected(false);
		vehicleAmountVariation_.setActionCommand("activateAmountVariation"); //$NON-NLS-1$
		c.gridx = 1;
		add(vehicleAmountVariation_,c);
		vehicleAmountVariationLabel_.setVisible(true);
		vehicleAmountVariation_.setVisible(true);

		vehicleAmountVariationLabel_.setVisible(false);
		vehicleAmountVariation_.setVisible(false);
		vehicleAmountVariation_.addActionListener(this);	
		
		
		JButton button = new JButton(Messages.getString("ResearchSeriesHelperDialog.apply"));
		++c.gridy;
		add(button, c);
		button.setActionCommand("apply");
		button.addActionListener(this);
		
		++c.gridy;

		errorNote_ = new TextAreaLabel(Messages.getString("ResearchSeriesHelperDialog.MsgBoxMissingInformation")); //$NON-NLS-1$
		++c.gridy;
		c.gridwidth = 2;	
		c.gridx = 0;
		add(errorNote_, c);
		errorNote_.setVisible(false);
		
		//to consume the rest of the space
		c.weighty = 1.0;
		++c.gridy;
		add(new JPanel(), c);
		
		//resize window
		pack();
		//adjust window size
		setLocationRelativeTo(ResearchSeriesDialog.getInstance());
		//show window
		setVisible(true);
	}

	/**
	 * An implemented <code>ActionListener</code> which performs the needed actions when the combobox or buttons (load, save, delete ...)
	 * are clicked.
	 * 
	 * @param e	an <code>ActionEvent</code>
	 */
	public void actionPerformed(ActionEvent e) {	
		String command = e.getActionCommand();

		if ("apply".equals(command)){	
			if(startValue_.getValue() == null || startValue_.getValue().equals("") || (!vehicleAmountVariation_.isSelected() && (stepSize_.getValue() == null || stepSize_.getValue().equals("") || stepAmount_.getValue() == null || stepAmount_.getValue().equals("")))) {
				errorNote_.setVisible(true);
			}
			else{
				if(mode_.equals("vehicles")){		
					if(vehicleAmountVariation_.isSelected() && command_.equals("amount")) ResearchSeriesDialog.getInstance().getActiveVehicleSet_().getPropertyList_().add(new SimulationProperty("amount", Double.parseDouble(startValue_.getValue().toString()), -1, 1));
					else ResearchSeriesDialog.getInstance().getActiveVehicleSet_().getPropertyList_().add(new SimulationProperty(command_, Double.parseDouble(startValue_.getValue().toString()), Double.parseDouble(stepSize_.getValue().toString()), Integer.parseInt(stepAmount_.getValue().toString())));

				}
				else if(mode_.equals("generalSettings")){
					ResearchSeriesDialog.getInstance().getActiveSeries_().getPropertyList_().add(new SimulationProperty(command_, Double.parseDouble(startValue_.getValue().toString()), Double.parseDouble(stepSize_.getValue().toString()), Integer.parseInt(stepAmount_.getValue().toString())));
				}
				closeDialog();
			}
		}
		else if("activateAmountVariation".equals(command)){
			if(vehicleAmountVariation_.isSelected()){
				//startValue_.setEditable(false);
				stepSize_.setEditable(false);
				stepAmount_.setEditable(false);
			}
			else{
				//startValue_.setEditable(true);
				stepSize_.setEditable(true);
				stepAmount_.setEditable(true);
			}
		}
	}
	
	/**
	 * Methode is evoked when closing JDialog
	 */
	public void closeDialog(){

		//close JDialog
		this.dispose();
	}	
}