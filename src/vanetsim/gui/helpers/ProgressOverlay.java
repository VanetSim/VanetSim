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

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;

import vanetsim.VanetSimStart;
import vanetsim.localization.Messages;

/**
 * A ProgressBar overlay in an undecorated <code>JDialog</code>. This should be activated when longer calculations are
 * made. The user can't click anywhere else while it's visible.
 */
public final class ProgressOverlay extends JDialog implements ActionListener{
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = 6272889496006127410L;

	/**
	 * Instantiates a new progress bar.
	 */
	public ProgressOverlay(){		
		super(VanetSimStart.getMainFrame());
		setUndecorated(true);
		getRootPane().setWindowDecorationStyle(JRootPane.NONE); 
		JProgressBar progressBar = new JProgressBar(0, 100);      
        progressBar.setIndeterminate(true);
        setLayout(new BorderLayout());
        add(progressBar, BorderLayout.PAGE_START);
        add(ButtonCreator.getJButton("shutdown.png", "shutdown", Messages.getString("ProgressOverlay.quitProgram"), this), BorderLayout.PAGE_END); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        pack();       
        setVisible(false);
	}
	
	/** 
	 * Overwriting the original setVisible function to always set position in center of screen if it's set to visible.
	 * 
	 * @see java.awt.Window#setVisible(boolean)
	 */
	public void setVisible(boolean state){
		if(state == true){
			VanetSimStart.getMainFrame().setEnabled(false);
			Point p = VanetSimStart.getMainFrame().getLocationOnScreen();
			setLocation((VanetSimStart.getMainFrame().getBounds().width - getBounds().width) / 2 + p.x,(VanetSimStart.getMainFrame().getBounds().height - getBounds().height) / 2 + p.y);
		} else VanetSimStart.getMainFrame().setEnabled(true);
        super.setVisible(state);		
	}

	/**
	 * An implemented <code>ActionListener</code> which allows to exit the program when the Quit-button
	 * is clicked.
	 * 
	 * @param e	an <code>ActionEvent</code>
	 */
	public void actionPerformed(ActionEvent e) {
		System.exit(ABORT);		
	}
}