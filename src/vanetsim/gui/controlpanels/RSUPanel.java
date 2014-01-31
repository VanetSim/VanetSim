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
import vanetsim.scenario.RSU;

/**
 * This class represents the control panel for adding Road-Side-Units (RSUs).
 */
public class RSUPanel extends JPanel implements ActionListener{

	/** The necessary constant for serializing. */
	private static final long serialVersionUID = 6951925324502007245L;

	/** RadioButton to add RSUs. */
	JRadioButton addRSU_;
	
	/** RadioButton to delete RSUs. */
	JRadioButton deleteRSU_;
	
	/** The input field for the RSU radius */
	private final JFormattedTextField rsuRadius_;
	
	/** The label of the RSU radius textfield */
	private final JLabel rsuLabel_;
	
	/** Note to describe add rsu mode */
	TextAreaLabel addNote_;
	
	/** Note to describe delete rsu mode. */
	TextAreaLabel deleteNote_;

	
	/**
	 * Constructor. Creating GUI items.
	 */
	public RSUPanel(){
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
		addRSU_ = new JRadioButton(Messages.getString("RSUPanel.addRSU")); //$NON-NLS-1$
		addRSU_.setActionCommand("addRSU"); //$NON-NLS-1$;
		addRSU_.setSelected(true);
		group.add(addRSU_);
		++c.gridy;
		add(addRSU_,c);
		addRSU_.addActionListener(this);
		
		deleteRSU_ = new JRadioButton(Messages.getString("RSUPanel.deleteRSU")); //$NON-NLS-1$
		deleteRSU_.setActionCommand("deleteRSU"); //$NON-NLS-1$
		group.add(deleteRSU_);
		++c.gridy;
		add(deleteRSU_,c);
		deleteRSU_.addActionListener(this);
		
		c.gridwidth = 1;
		c.insets = new Insets(5,5,5,5);
		
		//textfields
		c.gridx = 0;
		rsuLabel_ = new JLabel(Messages.getString("RSUPanel.radius")); //$NON-NLS-1$
		++c.gridy;
		add(rsuLabel_,c);		
		rsuRadius_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		rsuRadius_.setValue(500);

		rsuRadius_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(rsuRadius_,c);
		
		c.gridx = 0;
		c.gridwidth = 2;
		++c.gridy;
		add(ButtonCreator.getJButton("deleteAll.png", "clearRSUs", Messages.getString("RSUPanel.btnClearRSUs"), this),c);
		
		deleteNote_ = new TextAreaLabel(Messages.getString("RSUPanel.noteDelete")); //$NON-NLS-1$
		++c.gridy;
		c.gridx = 0;
		add(deleteNote_, c);
		deleteNote_.setVisible(false);
		
		addNote_ = new TextAreaLabel(Messages.getString("RSUPanel.noteAdd")); //$NON-NLS-1$
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
		if(addRSU_.isSelected()){	
			Map.getInstance().addRSU(new RSU(x,y,(((Number)rsuRadius_.getValue()).intValue())*100,false));
			Renderer.getInstance().ReRender(true, false);
		}	
		else if(deleteRSU_.isSelected()){
			Map.getInstance().delRSU(x,y);
			Renderer.getInstance().ReRender(true, false);
		}	
	}

	/**
	 * An implemented <code>ActionListener</code> which performs all needed actions when a <code>JRadioButton</code> or <code>JButton</code>
	 * is clicked.
	 * 
	 * @param e	an <code>ActionEvent</code>
	 */	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		//delete all RSUs
		if("clearRSUs".equals(command)){	
			if(JOptionPane.showConfirmDialog(null, Messages.getString("RSUPanel.msgBoxClearAll"), "", JOptionPane.YES_NO_OPTION) == 0){
				Map.getInstance().clearRSUs();
				Renderer.getInstance().ReRender(true, false);
			}
		}
		//enable RSU add mode
		else if("addRSU".equals(command)){
			rsuRadius_.setVisible(true);
			rsuLabel_.setVisible(true);
			addNote_.setVisible(true);
			deleteNote_.setVisible(false);
		}
		//enable RSU delete mode
		else if("deleteRSU".equals(command)){
			rsuRadius_.setVisible(false);
			rsuLabel_.setVisible(false);
			addNote_.setVisible(false);
			deleteNote_.setVisible(true);
		}
	}
}