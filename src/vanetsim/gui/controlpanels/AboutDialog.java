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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import vanetsim.VanetSimStart;
import vanetsim.localization.Messages;

/**
 * A credits dialog
 */


public final class AboutDialog extends JDialog{
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = -2918735209479587896L;

	
	/**
	 * Constructor. Creating GUI items.
	 */
	public AboutDialog(){
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

		//some basic options
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 0.5;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 2;	
		c.insets = new Insets(5,5,5,5);
		
		//start building gui
		++c.gridy;
		add(new JLabel(Messages.getString("AboutDialog.credits")), c); //$NON-NLS-1$
		
		//to consume the rest of the space
		c.weighty = 1.0;
		++c.gridy;
		JPanel space = new JPanel();
		space.setOpaque(false);
		add(space, c);
		
		//resize window
		pack();
		//adjust window size
		setLocationRelativeTo(VanetSimStart.getMainFrame());
		//show window
		setVisible(true);
	}

	/**
	 * Methode is evoked when closing JDialog
	 */
	public void closeDialog(){
		//close JDialog
		this.dispose();
	}	
}