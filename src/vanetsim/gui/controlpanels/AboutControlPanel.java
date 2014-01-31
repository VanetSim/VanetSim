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

import javax.swing.JLabel;
import javax.swing.JPanel;

import vanetsim.gui.helpers.TextAreaLabel;
import vanetsim.localization.Messages;


/**
 * This class contains the control elements for display of statistics and mix zone information
 */
public final class AboutControlPanel extends JPanel{
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = 5121979914528330821L;
	

	/**
	 * Constructor for this control panel.
	 */
	public AboutControlPanel(){
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 0.5;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.insets = new Insets(5,5,5,5);
		
		c.gridwidth = 1;
		
		//label for display of credits.
		++c.gridy;
		add(new JLabel("<html><b>" + Messages.getString("AboutDialog.creditsHeader") + "</b></html>"), c);
		++c.gridy;
		
		add(new TextAreaLabel(Messages.getString("AboutDialog.credits")), c);
		
		//to consume the rest of the space
		c.weighty = 1.0;
		++c.gridy;
		JPanel pane = new JPanel();
		pane.setOpaque(false);
		add(pane, c);
	}
}