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

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import vanetsim.scenario.events.Event;

/**
 * This class implements an own <code>ListCellRenderer</code> in order to display the list of events appropriately.
 */
@SuppressWarnings("rawtypes")
public final class EventJListRenderer extends JPanel implements ListCellRenderer {
	
	/** The constant for serializing. */
	private static final long serialVersionUID = -4716099862947417497L;

	/**
	 * Overwriting the standard cell renderer to get the information in it the way we want.
	 * 
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
		JPanel pane = new JPanel();
		JLabel line1 = new JLabel();
		JLabel line2 = new JLabel();
		Event event = (Event)value;
		line1.setText(event.getTime() + " ms"); //$NON-NLS-1$
		line2.setText(event.getText());		

		line1.setForeground(new Color(0,0,0));
		line2.setForeground(event.getTextColor());

		pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));
		pane.add(line1);
		pane.add(line2);

		if(isSelected) {
			pane.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
			pane.setBackground(new Color(230,230,230));
		} else pane.setBackground(super.getBackground());
		return pane;
	}
}