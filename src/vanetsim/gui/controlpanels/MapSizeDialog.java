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
import java.text.NumberFormat;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

import vanetsim.VanetSimStart;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.localization.Messages;
import vanetsim.map.Map;

/**
 * A dialog to set map parameters.
 */
public final class MapSizeDialog extends JDialog implements ActionListener{
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = 9008901792277578758L;

	/** An input field for the width of the map. */
	private final JFormattedTextField widthTextField_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
	
	/** An input field for the height of the map. */
	private final JFormattedTextField heightTextField_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
	
	/** An input field for the width of a region. */
	private final JFormattedTextField regionWidthTextField_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
	
	/** An input field for the height of a region. */
	private final JFormattedTextField regionHeightTextField_ = new JFormattedTextField(NumberFormat.getIntegerInstance());

	/** A barrier to allow the calling thread to wait for this thread to completely end it's work. */
	private final CyclicBarrier barrier_;
	
	/** The initial value in the dialog for the map width. */
	private int mapWidth_;
	
	/** The initial value in the dialog for the map height. */
	private int mapHeight_;
	
	/** The initial value in the dialog for the width of a region. */
	private int regionWidth_;
	
	/** The initial value in the dialog for the height of a region. */
	private int regionHeight_;
	
	/**
	 * Instantiates a new map size dialog.
	 * 
	 * @param mapWidth		the initial value in the dialog for the width of the map
	 * @param mapHeight		the initial value in the dialog for the height of the map
	 * @param regionWidth	the initial value in the dialog for the width of a region
	 * @param regionHeight 	the initial value in the dialog for the height of a region
	 * @param barrier		a barrier. You should wait on this barrier to get a clean thread synchronization!
	 */
	public MapSizeDialog(int mapWidth, int mapHeight, int regionWidth, int regionHeight, CyclicBarrier barrier){
		super(VanetSimStart.getMainFrame(),Messages.getString("MapSize.title"), true); //$NON-NLS-1$
		
		mapWidth_ = mapWidth;
		mapHeight_ = mapHeight;
		regionWidth_ = regionWidth;
		regionHeight_ = regionHeight;
		barrier_ = barrier;
		setUndecorated(true);
		setLayout(new GridBagLayout());
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
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
		c.insets = new Insets(5,5,5,5);
		c.gridwidth = 2;
		
		add(new JLabel(Messages.getString("MapSize.mapCreatedWith")), c); //$NON-NLS-1$
		c.gridwidth = 1;
		
		++c.gridy;		
		add(new JLabel(Messages.getString("MapSize.mapWidth")), c); //$NON-NLS-1$
		c.gridx = 1;
		widthTextField_.setValue(mapWidth);
		add(widthTextField_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("MapSize.mapHeight")), c); //$NON-NLS-1$
		c.gridx = 1;
		heightTextField_.setValue(mapHeight);
		add(heightTextField_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("MapSize.regionWidth")), c); //$NON-NLS-1$
		c.gridx = 1;
		regionWidthTextField_.setValue(regionWidth);
		add(regionWidthTextField_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("MapSize.regionHeight")), c); //$NON-NLS-1$
		c.gridx = 1;
		regionHeightTextField_.setValue(regionHeight);
		add(regionHeightTextField_, c);
		c.gridx = 0;
		
		++c.gridy;
		c.gridwidth = 2;
		add(new JLabel("<html><b>" + Messages.getString("MapSize.notes") + "<ul><li>" + Messages.getString("MapSize.noteSmallerValue")+"</li><li>"+Messages.getString("MapSize.noteRegionPerformance")+"</li><li>"+Messages.getString("MapSize.noteRegionMemory")+"</li></ul>"), c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
		
		++c.gridy;
		c.fill = GridBagConstraints.NONE;
		
		add(ButtonCreator.getJButton("ok.png", "OK", Messages.getString("MapSize.OK"), this), c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		pack();
		setLocationRelativeTo(VanetSimStart.getMainFrame());
		setVisible(true);
	}

	/**
	 * An implemented <code>ActionListener</code> which performs the needed actions when the OK-button
	 * is clicked.
	 * 
	 * @param e	an <code>ActionEvent</code>
	 */
	public void actionPerformed(ActionEvent e) {		
		setVisible(false);
		mapWidth_ = Math.max(mapWidth_, ((Number)widthTextField_.getValue()).intValue());
		mapHeight_ = Math.max(mapHeight_, ((Number)heightTextField_.getValue()).intValue());
		regionWidth_ = Math.max(1000, ((Number)regionWidthTextField_.getValue()).intValue());
		regionHeight_ = Math.max(1000, ((Number)regionHeightTextField_.getValue()).intValue());
		Map.getInstance().initNewMap(mapWidth_, mapHeight_, regionWidth_, regionHeight_);
		try {
			barrier_.await(2, TimeUnit.SECONDS);
		} catch (Exception e2) {}
		dispose();
	}
}