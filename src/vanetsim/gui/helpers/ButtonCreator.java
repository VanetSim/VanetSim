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
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import vanetsim.ErrorLog;
import vanetsim.localization.Messages;

public final class ButtonCreator{
	/**
	 * Convenience method to get a JButton with an image on it.
	 * 
	 * @param imageName		the filename of the image relative to the <code>vanetsim/images</code> directory
	 * @param command		a command string which can be used to track events in <code>actionPerformed()</code>
	 * @param altString		an alternative string to display on the button when the original image can't be loaded and as a tooltip
	 * @param listener		an <code>ActionListener</code> which performs actions on button clicks
	 * 
	 * @return the <code>JButton</code> created
	 */
	public static JButton getJButton(String imageName, String command, String altString, ActionListener listener){
		JButton button;
		
		if(imageName.equals("")){ //$NON-NLS-1$
			button = new JButton(altString);
			button.setPreferredSize(new Dimension(42, 42));
		} else {
			URL url = ClassLoader.getSystemResource("vanetsim/images/" + imageName); //$NON-NLS-1$
			if (url != null){
				button = new JButton(new ImageIcon(url));
			} else {
				button = new JButton(altString);
				button.setPreferredSize(new Dimension(42, 42));
				ErrorLog.log(Messages.getString("ButtonCreator.imageNotFound") + imageName, 5, ButtonCreator.class.getName(), "getJButton", null); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		button.setFocusPainted(false);
		button.setToolTipText(altString);
		button.setActionCommand(command);
		button.addActionListener(listener);
		return button;
	}
	
	public static JButton getJButton(String imageName, String command, String altString, boolean resize, ActionListener listener){
		JButton button;
		
		if(imageName.equals("")){ //$NON-NLS-1$
			button = new JButton(altString);
			button.setPreferredSize(new Dimension(42, 42));
		} else {
			URL url = ClassLoader.getSystemResource("vanetsim/images/" + imageName); //$NON-NLS-1$
			if (url != null){
				ImageIcon tmpImg = new ImageIcon(url);
				button = new JButton(tmpImg);
				button.setPreferredSize(new Dimension(tmpImg.getIconWidth(), tmpImg.getIconHeight()));
				button.setMaximumSize(new Dimension(tmpImg.getIconWidth(), tmpImg.getIconHeight()));
			} else {
				button = new JButton(altString);
				button.setPreferredSize(new Dimension(42, 42));
				ErrorLog.log(Messages.getString("ButtonCreator.imageNotFound") + imageName, 5, ButtonCreator.class.getName(), "getJButton", null); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		button.setFocusPainted(false);
		button.setToolTipText(altString);
		button.setActionCommand(command);
		button.addActionListener(listener);
		return button;
	}
}