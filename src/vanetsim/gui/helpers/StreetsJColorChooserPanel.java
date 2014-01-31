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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;

import vanetsim.localization.Messages;

/**
 * This class extends the standard <code>JColorChooser</code> with predefined colors which are also used in imports from OpenStreetMap.
 */
public final class StreetsJColorChooserPanel extends AbstractColorChooserPanel implements ItemListener{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8693021938856630099L;

	/** The labels for the colors (basically names for street types). */
	private static final String LABELS[] = { Messages.getString("StreetsJColorChooserPanel.blue"), Messages.getString("StreetsJColorChooserPanel.green"), Messages.getString("StreetsJColorChooserPanel.red"), Messages.getString("StreetsJColorChooserPanel.orange"), Messages.getString("StreetsJColorChooserPanel.yellow"), Messages.getString("StreetsJColorChooserPanel.white")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	
	/** An array with color values. Each color refers to the entry (at the same array position) in the <code>labels</code> array. */
	private static final Color COLORS[] = { new Color(117,146,185), new Color(116,194,116),new Color(225,98,102), new Color(253,184,100), new Color(252,249,105), Color.WHITE};

	/** The combo box to choose the different streets. */
	private JComboBox<String> comboBox_;
	
	/**
	 * Sets the color when initializing the panel.
	 * 
	 * @param color	the new color
	 */
	private void setColor(Color color) {
		comboBox_.setSelectedIndex(findColorPosition(color));
	}

	/**
	 * Find position in <code>colors_[]</code> array based on the label.
	 * 
	 * @param label	the label
	 * 
	 * @return the position
	 */
	private int findColorLabel(Object label) {
		String stringLabel = label.toString();
		int position = -1;
		for (int i = 0; i < LABELS.length; ++i){
			if (stringLabel.equals(LABELS[i])) {
				position = i;
				break;
			}
		}
		return position;
	}

	/**
	 * Find position in <code>colors_[]</code> array based on color.
	 * 
	 * @param color	the color
	 * 
	 * @return the position
	 */
	private int findColorPosition(Color color) {
		int position = COLORS.length - 1;
		int colorRGB = color.getRGB();
		for (int i = 0; i < COLORS.length; ++i) {
			if ((COLORS[i] != null) && (colorRGB == COLORS[i].getRGB())) {
				position = i;
				break;
			}
		}
		return position;
	}

	/**
	 * An implemented <code>ItemListener</code> to change colors when user changes value in the <code>comboBox</code>.
	 * 
	 * @param itemEvent	the received event
	 * 
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent itemEvent) {
		int state = itemEvent.getStateChange();
		if (state == ItemEvent.SELECTED) {
			int position = findColorLabel(itemEvent.getItem());
			if (position != -1) {
				ColorSelectionModel selectionModel = getColorSelectionModel();
				selectionModel.setSelectedColor(COLORS[position]);
			}
		}
	}

	/** 
	 * The name displayed in the <code>JColorChooser</code>.
	 * 
	 * @see javax.swing.colorchooser.AbstractColorChooserPanel#getDisplayName()
	 */
	public String getDisplayName() {
		return Messages.getString("StreetsJColorChooserPanel.streetcolors"); //$NON-NLS-1$
	}

	/** 
	 * The small icon displayed in the <code>JColorChooser</code>.
	 * 
	 * @see javax.swing.colorchooser.AbstractColorChooserPanel#getSmallDisplayIcon()
	 */
	public Icon getSmallDisplayIcon() {
		URL url = ClassLoader.getSystemResource("vanetsim/images/streeticon.gif"); //$NON-NLS-1$
		if (url != null) return (Icon)new ImageIcon(url);
		else return (Icon)new ImageIcon();
	}

	/** 
	 * The large icon displayed in the <code>JColorChooser</code>.
	 * 
	 * @see javax.swing.colorchooser.AbstractColorChooserPanel#getLargeDisplayIcon()
	 */
	public Icon getLargeDisplayIcon() {
		URL url = ClassLoader.getSystemResource("vanetsim/images/streeticon.gif"); //$NON-NLS-1$
		if (url != null) return (Icon)new ImageIcon(url);
		else return (Icon)new ImageIcon();
	}

	/**
	 * Builds this color chooser.
	 * 
	 * @see javax.swing.colorchooser.AbstractColorChooserPanel#buildChooser()
	 */
	protected void buildChooser() {
		comboBox_ = new JComboBox<String>(LABELS);
		comboBox_.addItemListener(this);
		add(comboBox_);
	}

	/**
	 * Invoked automatically when the model's state changes.
	 * 
	 * @see javax.swing.colorchooser.AbstractColorChooserPanel#updateChooser()
	 */
	public void updateChooser() {
		Color color = getColorFromModel();
		setColor(color);
	}
}
