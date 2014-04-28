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

import javax.swing.JButton;
import javax.swing.JPanel;

import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.localization.Messages;


/**
 * This class represents the control panel for selected traffic models or trace data
 */
public class EditDataAnalysisControlPanel extends JPanel implements  ActionListener{

	private static final long serialVersionUID = 5915143087427387639L;
	
	/** Button for opening the "anonymize logfile" window. */
	private JButton anonymizeLogFile_;
	
	/** Button for opening the data linking window. */
	private JButton linkData_;

	
	
	/**
	 * Constructor, creating GUI items.
	 */
	public EditDataAnalysisControlPanel() {
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
		
		//add buttons
		c.gridx = 0;
		c.gridwidth = 2;
		++c.gridy;
		// TODO [MH] create image
		anonymizeLogFile_ = ButtonCreator.getJButton("", "anonymizeLogFile", Messages.getString("EditDataAnalysisControlPanel.anonymizeLogFile"), this);
//		anonymizeLogFile_.setVisible(false);		
		add(anonymizeLogFile_,c);
		
		c.gridx = 0;
		++c.gridy;
		// TODO [MH] create image
		linkData_ = ButtonCreator.getJButton("", "linkData", Messages.getString("EditDataAnalysisControlPanel.linkData"), this);
//		linkData_.setVisible(false);
		add(linkData_,c);
		
		//to consume the rest of the space
		c.weighty = 1.0;
		++c.gridy;
		JPanel space = new JPanel();
		space.setOpaque(false);
		add(space, c);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		if ("anonymizeLogFile".equals(command)){
			new AnonymizeDataDialog();
		}
	}

}