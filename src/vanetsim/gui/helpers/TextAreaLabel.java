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

import javax.swing.JTextArea;

/**
 * A class which uses a <code>JTextArea</code> to imitate a <code>JLabel</code> with automatic linewrap.
 */
public final class TextAreaLabel extends JTextArea{
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = 6703416429165263141L;

	/**
	 * Instantiates a new <code>JTextArea</code> with automatic linewrap.
	 * 
	 * @param text the text
	 */
	public TextAreaLabel(String text){
		super(text);
		setOpaque(false);
		setBorder(null);
		setFocusable(false);
		setWrapStyleWord(true);
		setLineWrap(true);
	}
	
	/**
	 * Return a little bit smaller width than original JTextArea would. This should help
	 * to compensate appearing scrollbars on the right side of this area. 
	 * 
	 * @return the modified preferred size
	 */
	public Dimension getPreferredSize(){
		Dimension dim = super.getPreferredSize();
		dim.width -= 6;
		return dim;
	}
}