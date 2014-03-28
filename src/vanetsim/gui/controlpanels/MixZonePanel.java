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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import javax.swing.JPanel;

import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.gui.helpers.TextAreaLabel;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.map.MapHelper;
import vanetsim.map.Node;
import vanetsim.scenario.RSU;
import vanetsim.scenario.Vehicle;

/**
 * This class represents the control panel for adding mix zones.
 */
public class MixZonePanel extends JPanel implements ActionListener{

	/** The necessary constant for serializing. */
	private static final long serialVersionUID = -8294786435746799533L;

	/** RadioButton to add mixZones. */
	JRadioButton addMixZone_;
	
	/** RadioButton to delete mixZones. */
	JRadioButton deleteMixZone_;
	
	/** CheckBox to choose if Mix Zones are created automatically. */
	private final JCheckBox autoAddMixZones_;	
	
	/** JLabel to describe autoAddMixZones_ checkbox */
	private final JLabel autoAddLabel_;	
	
	/** Activate encrypted Beacons in Mix */
	private final JCheckBox encryptedBeacons_;	
	
	/** JLabel to describe encryptedBeacons_ checkbox */
	private final JLabel encryptedBeaconsLabel_;	
	
	/** Activate encrypted Beacons in Mix display Mode */
	private final JCheckBox showEncryptedBeacons_;	
	
	/** JLabel to describe showEncryptedBeacons_ checkbox */
	private final JLabel showEncryptedBeaconsLabel_;	
	
	/** The input field for the mix zone radius */
	private final JFormattedTextField mixRadius_;
	
	/** JLabel to describe autoAddLabel_ textfield */
	private final JLabel radiusLabel_;	

	/** Note to describe add mix zone mode */
	TextAreaLabel addNote_;
	
	/** Note to describe delete mix zone mode. */
	TextAreaLabel deleteNote_;
	
	
	/**
	 * Constructor, creating GUI items.
	 */
	public MixZonePanel(){
		setLayout(new GridBagLayout());
		
		// global layout settings
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 2;
		
		// Radio buttons to select mode
		ButtonGroup group = new ButtonGroup();
		addMixZone_ = new JRadioButton(Messages.getString("MixZonePanel.addMixZone")); //$NON-NLS-1$
		addMixZone_.setActionCommand("addMixZone"); //$NON-NLS-1$
		addMixZone_.addActionListener(this);
		addMixZone_.setSelected(true);
		group.add(addMixZone_);
		++c.gridy;
		add(addMixZone_,c);
		
		deleteMixZone_ = new JRadioButton(Messages.getString("MixZonePanel.deleteMixZone")); //$NON-NLS-1$
		deleteMixZone_.setActionCommand("deleteMixZone"); //$NON-NLS-1$
		deleteMixZone_.addActionListener(this);
		group.add(deleteMixZone_);
		++c.gridy;
		add(deleteMixZone_,c);
		
		c.gridwidth = 1;
		c.insets = new Insets(5,5,5,5);
		
		c.gridx = 0;
		radiusLabel_ = new JLabel(Messages.getString("MixZonePanel.radius")); //$NON-NLS-1$
		++c.gridy;
		add(radiusLabel_,c);		
		mixRadius_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		mixRadius_.setValue(100);

		mixRadius_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(mixRadius_,c);
		
		c.gridx = 0;
		autoAddLabel_ = new JLabel(Messages.getString("MixZonePanel.autoAddMixZones")); //$NON-NLS-1$
		++c.gridy;
		add(autoAddLabel_,c);		
		autoAddMixZones_ = new JCheckBox();
		autoAddMixZones_.setSelected(false);
		autoAddMixZones_.setActionCommand("autoAddMixZones"); //$NON-NLS-1$
		c.gridx = 1;
		add(autoAddMixZones_,c);
		autoAddMixZones_.addActionListener(this);	
		
		c.gridx = 0;
		encryptedBeaconsLabel_ = new JLabel(Messages.getString("MixZonePanel.encryptedBeacons")); //$NON-NLS-1$
		++c.gridy;
		add(encryptedBeaconsLabel_,c);		
		encryptedBeacons_ = new JCheckBox();
		encryptedBeacons_.setSelected(false);
		encryptedBeacons_.setActionCommand("encryptedBeacons"); //$NON-NLS-1$
		c.gridx = 1;
		add(encryptedBeacons_,c);
		encryptedBeacons_.addActionListener(this);
		
		c.gridx = 0;
		showEncryptedBeaconsLabel_ = new JLabel(Messages.getString("MixZonePanel.showEncryptedBeacons")); //$NON-NLS-1$
		++c.gridy;
		add(showEncryptedBeaconsLabel_,c);		
		showEncryptedBeacons_ = new JCheckBox();
		showEncryptedBeacons_.setSelected(false);
		showEncryptedBeacons_.setActionCommand("showEncryptedBeacons"); //$NON-NLS-1$
		c.gridx = 1;
		add(showEncryptedBeacons_,c);
		showEncryptedBeacons_.addActionListener(this);
		
		c.gridx = 0;
		c.gridwidth = 2;
		++c.gridy;
		add(ButtonCreator.getJButton("deleteAll.png", "clearMixZones", Messages.getString("MixZonePanel.btnClearMixZones"), this),c);
		
		deleteNote_ = new TextAreaLabel(Messages.getString("MixZonePanel.noteDelete")); //$NON-NLS-1$
		++c.gridy;
		c.gridx = 0;
		add(deleteNote_, c);
		deleteNote_.setVisible(false);
		
		addNote_ = new TextAreaLabel(Messages.getString("MixZonePanel.noteAdd")); //$NON-NLS-1$
		++c.gridy;
		c.gridx = 0;
		add(addNote_, c);
		addNote_.setVisible(true);
		
		//to consume the rest of the space
		c.weighty = 1.0;
		++c.gridy;
		JPanel space = new JPanel();
		space.setOpaque(false);
		add(space, c);
	}
	
	/**
	 * Receives a mouse event.
	 * 
	 * @param x	the x coordinate (in map scale)
	 * @param y	the y coordinate (in map scale)
	 */
	public void receiveMouseEvent(int x, int y){	
		Node tmpNode = MapHelper.findNearestNode(x, y, 2000, new long[1]);
		if(tmpNode != null){
			if(addMixZone_.isSelected()){	
				Map.getInstance().addMixZone(tmpNode, ((Number)mixRadius_.getValue()).intValue() * 100);
				Renderer.getInstance().ReRender(true, false);
			}	
			else if(deleteMixZone_.isSelected()){
				Map.getInstance().deleteMixZone(tmpNode);
				Renderer.getInstance().ReRender(true, false);
			}
		}		
	}
	
	/**
	 * An implemented <code>ActionListener</code> which performs all needed actions when a <code>JCheckBox</code> or <code>JButton</code>
	 * is clicked.
	 * 
	 * @param e	an <code>ActionEvent</code>
	 */	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		//delete all mix zones
		if("clearMixZones".equals(command)){	
			if(JOptionPane.showConfirmDialog(null, Messages.getString("MixZonePanel.msgBoxClearAll"), "", JOptionPane.YES_NO_OPTION) == 0){
				Map.getInstance().clearMixZones();
				Renderer.getInstance().ReRender(true, false);
			}
		}
		//set flag to add mix zones automatically to every street corner
		else if("autoAddMixZones".equals(command)){
			Renderer.getInstance().setAutoAddMixZones(autoAddMixZones_.isSelected());
		}
		//set flag to enable encrypted communication in mix-zone
		else if("encryptedBeacons".equals(command)){
			Vehicle.setEncryptedBeaconsInMix_(encryptedBeacons_.isSelected());
			if(!encryptedBeacons_.isSelected()){
				showEncryptedBeacons_.setSelected(false);
				RSU.setShowEncryptedBeaconsInMix_(false);
			}
		}		
		//set flag to enable the demonstation mode of encrypted communication in mix-zone
		else if("showEncryptedBeacons".equals(command)){
			RSU.setShowEncryptedBeaconsInMix_(showEncryptedBeacons_.isSelected());
		}	
		//JRadioButton event; add mix zone mode
		else if("addMixZone".equals(command)){
			mixRadius_.setVisible(true);
			radiusLabel_.setVisible(true);
			autoAddMixZones_.setVisible(true);
			autoAddLabel_.setVisible(true);
			deleteNote_.setVisible(false);
			addNote_.setVisible(true);
			encryptedBeacons_.setVisible(true);	
			encryptedBeaconsLabel_.setVisible(true);
			showEncryptedBeacons_.setVisible(true);
			showEncryptedBeaconsLabel_.setVisible(true);
		}
		//JRadioButton event; delete mix zone mode
		else if("deleteMixZone".equals(command)){
			mixRadius_.setVisible(false);
			radiusLabel_.setVisible(false);
			autoAddMixZones_.setVisible(false);
			autoAddLabel_.setVisible(false);
			deleteNote_.setVisible(true);
			addNote_.setVisible(false);
			encryptedBeacons_.setVisible(false);	
			encryptedBeaconsLabel_.setVisible(false);
			showEncryptedBeacons_.setVisible(false);
			showEncryptedBeaconsLabel_.setVisible(false);
		}
	}

	/**
	 * Returns if mix zones are added automatically
	 * 
	 * @return true: Mix Zones are added automatically
	 */
	public JCheckBox getAutoAddMixZones() {
		return autoAddMixZones_;
	}

	public JCheckBox getEncryptedBeacons_() {
		return encryptedBeacons_;
	}

	public JCheckBox getShowEncryptedBeacons_() {
		return showEncryptedBeacons_;
	}
	
	public void updateMixRadius(){
		Vehicle.setMixZoneRadius(((Number)mixRadius_.getValue()).intValue());
	}
}