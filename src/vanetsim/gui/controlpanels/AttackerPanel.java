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
import javax.swing.JRadioButton;

import javax.swing.JPanel; 

import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.gui.helpers.TextAreaLabel;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.map.Region;
import vanetsim.scenario.AttackRSU;
import vanetsim.scenario.Vehicle;

/**
 * This class represents the control panel for editing attacker settings.
 */
public class AttackerPanel extends JPanel implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** RadioButton to add, delete a RSU attacker unit */
	JRadioButton addDeleteAttackRSU_;
	
	/** RadioButton to select, unselect attacker vehicle. */
	JRadioButton selectUnselectAttackerVehicle_;

	/** RadioButton to select, unselect attacked vehicle. */
	JRadioButton selectUnselectAttackedVehicle_;
	
	/** The input field for the Attack-RSU radius */
	private final JFormattedTextField arsuRadius_;
	
	/** JLabel to describe arsuRadius_ textfield */
	private final JLabel arsuRadiusLabel_;	

	/** Note to describe this mode */
	TextAreaLabel attackerNote_;

	
	
	/**
	 * Constructor, creating GUI items.
	 */
	public AttackerPanel(){
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
		addDeleteAttackRSU_ = new JRadioButton(Messages.getString("AttackerPanel.addAttackRSU")); //$NON-NLS-1$
		addDeleteAttackRSU_.setActionCommand("addAttackRSU"); //$NON-NLS-1$
		addDeleteAttackRSU_.addActionListener(this);
		addDeleteAttackRSU_.setSelected(true);
		group.add(addDeleteAttackRSU_);
		++c.gridy;
		add(addDeleteAttackRSU_,c);
		
		selectUnselectAttackerVehicle_ = new JRadioButton(Messages.getString("AttackerPanel.selectAttackerVehicle")); //$NON-NLS-1$
		selectUnselectAttackerVehicle_.setActionCommand("selectAttackerVehicle"); //$NON-NLS-1$
		selectUnselectAttackerVehicle_.addActionListener(this);
		group.add(selectUnselectAttackerVehicle_);
		++c.gridy;
		add(selectUnselectAttackerVehicle_,c);
		
		selectUnselectAttackedVehicle_ = new JRadioButton(Messages.getString("AttackerPanel.selectAttackedVehicle")); //$NON-NLS-1$
		selectUnselectAttackedVehicle_.setActionCommand("selectAttackedVehicle"); //$NON-NLS-1$
		selectUnselectAttackedVehicle_.addActionListener(this);
		group.add(selectUnselectAttackedVehicle_);
		++c.gridy;
		add(selectUnselectAttackedVehicle_,c);
		
		c.gridwidth = 1;
		c.insets = new Insets(5,5,5,5);
		
		//Radius of the Attacker-RSU
		c.gridx = 0;
		arsuRadiusLabel_ = new JLabel(Messages.getString("AttackPanel.radius")); //$NON-NLS-1$
		++c.gridy;
		add(arsuRadiusLabel_,c);		
		arsuRadius_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		arsuRadius_.setValue(100);

		arsuRadius_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(arsuRadius_,c);

		//Button to delete all attackers
		c.gridx = 0;
		c.gridwidth = 2;
		++c.gridy;
		add(ButtonCreator.getJButton("deleteAll.png", "clearAttackers", Messages.getString("AttackerPanel.btnClearAttackers"), this),c);
		
		attackerNote_ = new TextAreaLabel(Messages.getString("AttackerPanel.noteAttackers")); //$NON-NLS-1$
		++c.gridy;
		c.gridx = 0;
		add(attackerNote_, c);
		attackerNote_.setVisible(true);
		
		//to consume the rest of the space
		c.weighty = 1.0;
		++c.gridy;
		JPanel space = new JPanel();
		space.setOpaque(false);
		add(space, c);
				
		//end of GUI
	}
	
	/**
	 * Receives a mouse event.
	 * 
	 * @param x	the x coordinate (in map scale)
	 * @param y	the y coordinate (in map scale)
	 */
	public void receiveMouseEvent(int x, int y){
		
		//checks if a vehicle is near the coordinations of the mouse click and sets the Attacker-Vehicle
		if(selectUnselectAttackerVehicle_.isSelected()){
			//find vehicles near x and y
			Renderer.getInstance().setMarkedVehicle(null);
			
			
			Region[][] Regions = Map.getInstance().getRegions();
			int Region_max_x = Map.getInstance().getRegionCountX();
			int Region_max_y = Map.getInstance().getRegionCountY();
			int i, j;
			for(i = 0; i < Region_max_x; ++i){
				for(j = 0; j < Region_max_y; ++j){
					Vehicle[] vehiclesArray = Regions[i][j].getVehicleArray();
					for(int k = 0; k < vehiclesArray.length; ++k){
						Vehicle vehicle = vehiclesArray[k];
						
						//selects attacker vehicle near the coordinates (200 cm radius)
						if(vehicle.getX() > (x - 200) && vehicle.getX() < (x + 200) && vehicle.getY() > (y - 200) && vehicle.getY() < (y + 200)) {
							if(Renderer.getInstance().getAttackedVehicle() != null && Renderer.getInstance().getAttackedVehicle().equals(vehicle)) Renderer.getInstance().setAttackedVehicle(null);
							Renderer.getInstance().setAttackerVehicle(vehicle);	
						}
					}		
				}
			}
		}
		
		//checks if a vehicle is near the coordinations of the mouse click and sets the Attacked-Vehicle
		else if(selectUnselectAttackedVehicle_.isSelected()){
			//find vehicles near x and y
			Renderer.getInstance().setMarkedVehicle(null);
			
			
			Region[][] Regions = Map.getInstance().getRegions();
			int Region_max_x = Map.getInstance().getRegionCountX();
			int Region_max_y = Map.getInstance().getRegionCountY();
			int i, j;
			for(i = 0; i < Region_max_x; ++i){
				for(j = 0; j < Region_max_y; ++j){
					Vehicle[] vehiclesArray = Regions[i][j].getVehicleArray();
					for(int k = 0; k < vehiclesArray.length; ++k){
						Vehicle vehicle = vehiclesArray[k];
						
						//selects attacked vehicle near the coordinates (200 cm radius)
						if(vehicle.getX() > (x - 200) && vehicle.getX() < (x + 200) && vehicle.getY() > (y - 200) && vehicle.getY() < (y + 200)) {
							if(Renderer.getInstance().getAttackerVehicle() != null && Renderer.getInstance().getAttackerVehicle().equals(vehicle)) Renderer.getInstance().setAttackerVehicle(null);
							Renderer.getInstance().setAttackedVehicle(vehicle);
							Vehicle.setAttackedVehicleID_(vehicle.getID());		
						}
					}		
				}
			}
		}
		else{
			//Checks if a ARSU is near the coordinates delete this ARSU. If not add a new ARSU
			if(!AttackRSU.deleteARSU(x, y)) new AttackRSU(x, y, ((Number)getArsuRadius_().getValue()).intValue() * 100);
			Renderer.getInstance().ReRender(true, false);
		}
		Renderer.getInstance().ReRender(true, true);
	}
	
	/**
	 * An implemented <code>ActionListener</code> which performs all needed actions when a <code>JRadioButton</code> or <code>JButton</code>
	 * is clicked.
	 * 
	 * @param e	an <code>ActionEvent</code>
	 */	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		//delete all attackers and attackerRSUs
		if("clearAttackers".equals(command)){	
			Renderer.getInstance().setAttackerVehicle(null);	
			Renderer.getInstance().setAttackedVehicle(null);
			Vehicle.setArsuList(null);
		}

		//show gui elements of "add arsu mode"
		else if(("addAttackRSU").equals(command)){
			arsuRadiusLabel_.setVisible(true);
			arsuRadius_.setVisible(true);
		}
		//show gui elements of "select Attacker / attacked vehicle mode" 
		else if(("selectAttackerVehicle").equals(command) || ("selectAttackedVehicle").equals(command)){
			arsuRadiusLabel_.setVisible(false);
			arsuRadius_.setVisible(false);
		}
		Renderer.getInstance().ReRender(true, true);
	}

	public JFormattedTextField getArsuRadius_() {
		return arsuRadius_;
	}

}