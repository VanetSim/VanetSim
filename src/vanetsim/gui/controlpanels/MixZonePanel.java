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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.gui.helpers.TextAreaLabel;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.map.MapHelper;
import vanetsim.map.Node;
import vanetsim.map.Region;
import vanetsim.map.Street;
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

	/** JLabel to describe addModeChoice_ JComboBox */
	private final JLabel addMixZonesByModeLabel_;

	/** JCombobox to select choice of auto create mix-zone mode */
	private final JComboBox<String> addModeChoice_;
	
	/** An array with all street types */ 
	private static final String[] PRESET_TYPES = {Messages.getString("MixZonePanel.best"), Messages.getString("MixZonePanel.random")};  
	
	/** JLabel to describe mixAmount_ textfield */
	private final JLabel addMixZonesAmountLabel_;
	
	/** JFormattedTextField containing the amount of mix zones to be created */
	private final JFormattedTextField mixAmount_;
	
	/** Button to create mix-zones */
	private final JButton readLogAndStartAdding_;
	
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
		
		++c.gridy;
		c.gridx = 0;
		c.gridwidth = 2;
		add(new JSeparator(SwingConstants.HORIZONTAL),c);
		++c.gridy;
		c.gridwidth = 1;
		c.gridx = 0;
		addMixZonesByModeLabel_ = new JLabel(Messages.getString("MixZonePanel.addMixZonesToBestJunctions")); //$NON-NLS-1$
		add(addMixZonesByModeLabel_, c);
		c.gridx = 1;
		addModeChoice_ = new JComboBox<String>(PRESET_TYPES);
		addModeChoice_.setSelectedIndex(0);
		add(addModeChoice_, c);
		
		++c.gridy;
		c.gridx = 0;
		addMixZonesAmountLabel_ = new JLabel(Messages.getString("MixZonePanel.addMixZonesAmount")); //$NON-NLS-1$
		add(addMixZonesAmountLabel_, c);
		c.gridx = 1;
		mixAmount_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		mixAmount_.setValue(5);
		mixAmount_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(mixAmount_,c);
		
		++c.gridy;
		c.gridx = 0;
		c.gridwidth = 2;
		readLogAndStartAdding_ = new JButton(Messages.getString("MixZonePanel.readLogAndStartAdding"));
		readLogAndStartAdding_.setActionCommand("add mix zones");
		readLogAndStartAdding_.addActionListener(this);
		add(readLogAndStartAdding_, c);
		
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
			addMixZonesByModeLabel_.setVisible(true);
			addModeChoice_.setVisible(true);
			addMixZonesAmountLabel_.setVisible(true);
			mixAmount_.setVisible(true);
			readLogAndStartAdding_.setVisible(true);
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
			addMixZonesByModeLabel_.setVisible(false);
			addModeChoice_.setVisible(false);
			addMixZonesAmountLabel_.setVisible(false);
			mixAmount_.setVisible(false);
			readLogAndStartAdding_.setVisible(false);
		}
		else if("add mix zones".equals(command)){
			if(mixAmount_.getValue() != null){
				if(addModeChoice_.getSelectedItem().toString().equals(Messages.getString("MixZonePanel.best"))) {
					Node[] nodes = calculateBestJunctionsForMixZones(((Number)mixAmount_.getValue()).intValue());
					
					for(int i = 0; i < nodes.length; i++) {
						if(nodes[i] != null){
							Map.getInstance().addMixZone(nodes[i], ((Number)mixRadius_.getValue()).intValue() * 100);
							Renderer.getInstance().ReRender(true, false);
						}
						
					}
				}
				else if(addModeChoice_.getSelectedItem().toString().equals(Messages.getString("MixZonePanel.random"))){
					Node[] nodes = calculateRandomJunctionsForMixZones(((Number)mixAmount_.getValue()).intValue());
					
					for(int i = 0; i < nodes.length; i++) {
						if(nodes[i] != null){
							Map.getInstance().addMixZone(nodes[i], ((Number)mixRadius_.getValue()).intValue() * 100);
							Renderer.getInstance().ReRender(true, false);
						}
						
					}
				}
			}		
		}
	
	}

	/**
	 * Opens a log file containing vehicle routes; calculates the junctions with the best vehicle distribution
	 */
	public Node[] calculateBestJunctionsForMixZones(int n){
		Node[] nodeIDs = new Node[n];
		
		//open log file:
		JFileChooser fc = new JFileChooser();
				
		//set directory and ".log" filter
		fc.setMultiSelectionEnabled(false);
		fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				
		int status = fc.showDialog(this, Messages.getString("EditLogControlPanel.approveButton"));
			
		int numberOfLines = 1;
		int maxNumberOfVehicleInNode = 0;
		
		HashMap<String,Integer> numberOfVehiclesPerNode = new HashMap<String,Integer>();
		HashMap<String,Integer> numberOfVehiclesPerPort = new HashMap<String,Integer>();
		HashMap<Node,Double> entropyOfNode = new HashMap<Node,Double>();
		
		String[] lineSplit;
		
		if(status == JFileChooser.APPROVE_OPTION){
			File tmpFile = fc.getSelectedFile();
			//read file
			BufferedReader reader;
						        
			try{
				reader = new BufferedReader(new FileReader(tmpFile));

				System.out.println("started reading file");
	            String line = reader.readLine();    
		           while(line != null){
		        	   lineSplit = line.split(":");
		        	   if(lineSplit.length == 3){
		        		   if((numberOfLines%1000) == 0)System.out.print(".");
			        	   //count vehicles per node
			        	   if(numberOfVehiclesPerNode.containsKey(lineSplit[0])){
			        		   numberOfVehiclesPerNode.put(lineSplit[0], (numberOfVehiclesPerNode.get(lineSplit[0]) + 1));
			        		   if(maxNumberOfVehicleInNode < numberOfVehiclesPerNode.get(lineSplit[0])) maxNumberOfVehicleInNode = numberOfVehiclesPerNode.get(lineSplit[0]);
			        	   }
			        	   else numberOfVehiclesPerNode.put(lineSplit[0], 1);
			        	   
			        	   //count vehicles per port
			        	   if(numberOfVehiclesPerPort.containsKey(line))numberOfVehiclesPerPort.put(line, (numberOfVehiclesPerPort.get(line) + 1));
			        	   else numberOfVehiclesPerPort.put(line, 1);
			        	   
			        	   numberOfLines++;
			        	     
		        	   }
		        	   line = reader.readLine();
		        	   
		           }
		           System.out.println("ended reading file"); 
			} catch (FileNotFoundException e) {
			    System.out.println("FileNotFoundException: " + e.getMessage());
			} catch (IOException e) {
			    System.out.println("Caught IOException: " + e.getMessage());
					
			}
		}
		
		
		//get all nodes
		Region[][] tmpRegions = Map.getInstance().getRegions();
		
		int regionCountX = Map.getInstance().getRegionCountX();
		int regionCountY = Map.getInstance().getRegionCountY();
		Node[] nodes;
		Street[] crossingStreets;
		 
		int totalNumberOfVehiclesForNode = 0;
		double entropyInProgress = 0;
		int maxVehiclesPerNode = 0;
		
		System.out.println("started calculating entropy");
		for(int i = 0; i <= regionCountX-1;i++){
			for(int j = 0; j <= regionCountY-1;j++){
				Region tmpRegion = tmpRegions[i][j];
					
				nodes = tmpRegion.getNodes();
				
				for(int k = 0; k < nodes.length; k++){
					crossingStreets = nodes[k].getCrossingStreets();
					
					//only use crossing with 4 ports
					if(crossingStreets.length > 3 && numberOfVehiclesPerNode.containsKey(nodes[k].getNodeID() + "")){
						if((numberOfLines % 1000) == 0) System.out.print(".");
						totalNumberOfVehiclesForNode = numberOfVehiclesPerNode.get(nodes[k].getNodeID() + "");
						if(maxVehiclesPerNode < totalNumberOfVehiclesForNode)maxVehiclesPerNode = totalNumberOfVehiclesForNode;
						entropyInProgress = 0;
						//create all possible entry / exit combinations of this junction
						for(int l = 0; l < crossingStreets.length; l++){
							for(int m = 0; m < crossingStreets.length; m++){
									if(numberOfVehiclesPerPort.get(nodes[k].getNodeID() + ":" + l + ":" + m) != null) {
										entropyInProgress += ((double)numberOfVehiclesPerPort.get(nodes[k].getNodeID() + ":" + l + ":" + m)/totalNumberOfVehiclesForNode) * (Math.log((double)numberOfVehiclesPerPort.get(nodes[k].getNodeID() + ":" + l + ":" + m)/totalNumberOfVehiclesForNode)/Math.log(crossingStreets.length*crossingStreets.length));
									}
								
							}
						}
						System.out.println("entropy: " + -entropyInProgress);
						entropyOfNode.put(nodes[k], (-entropyInProgress));
					}
				}
			}
		}
		
		System.out.println("ended calculating entropy");
		double[] topValues = new double[n];
		
		int mixZoneRadius = ((Number)mixRadius_.getValue()).intValue() * 100;
		double mixDistanceSquared = (4*((double)mixZoneRadius+100)*((double)mixZoneRadius)+100);

		System.out.println("started calculating best mix zones places");

		for (Entry<Node, Double> entry : entropyOfNode.entrySet()) {
		    Node node = entry.getKey();
		    Double entropy = entry.getValue();
		    Double density = ((double)numberOfVehiclesPerNode.get(node.getNodeID() + ""))/maxVehiclesPerNode;
		    double rememberSmallestValue = 999999999;
		    int rememberSmallestIndex = -1;
		    
		    for(int i = 0; i < topValues.length; i++){   	
		    	if((entropy+density) > topValues[i]){
		    		if(rememberSmallestValue > topValues[i]){
		    				rememberSmallestValue = topValues[i];
		    				rememberSmallestIndex = i;
		    		}
		    	}
		    }
		    
		    
		    if(rememberSmallestIndex != -1){
		    	boolean switched = false;
			    for(int i = 0; i < nodeIDs.length; i++){   	
			    	if(nodeIDs[i] != null){
			    		double dx = node.getX() - nodeIDs[i].getX();
			    		double dy = node.getY() - nodeIDs[i].getY();

						
						if((dx * dx + dy * dy) <= mixDistanceSquared){	// Pythagorean theorem: a^2 + b^2 = c^2 but without the needed Math.sqrt to save a little bit performance
							//the mix zones are to near ... only one can survive!
							switched = true;
							
							if(topValues[i] < (entropy+density)){
								nodeIDs[i] = node;
					    		topValues[i] = (entropy+density);
							}
						}
			    	}
			    }
			    
			    if(!switched){
			    	nodeIDs[rememberSmallestIndex] = node;
		    		topValues[rememberSmallestIndex] = (entropy+density);
			    }
	    		
		    }
		}
		System.out.println("ended calculating best mix zones places");

		return nodeIDs;
	}
	
	/**
	 * Gets the amount of junctions in on a map and chooses n randomly
	 */
	public Node[] calculateRandomJunctionsForMixZones(int n){
		Node[] nodeIDs = new Node[n];
		
		//get all nodes save nodes with more then 3 ports into ArrayList
		ArrayList<Node> nodeList = new ArrayList<Node>();
		Region[][] tmpRegions = Map.getInstance().getRegions();
		
		int regionCountX = Map.getInstance().getRegionCountX();
		int regionCountY = Map.getInstance().getRegionCountY();
		Node[] nodes;
		Street[] crossingStreets;
		
		System.out.println("Getting node list");
		for(int i = 0; i <= regionCountX-1;i++){
			for(int j = 0; j <= regionCountY-1;j++){
				Region tmpRegion = tmpRegions[i][j];
					
				nodes = tmpRegion.getNodes();
				
				for(int k = 0; k < nodes.length; k++){
					crossingStreets = nodes[k].getCrossingStreets();
					
					//only use crossing with 4 ports
					if(crossingStreets.length > 3){
						nodeList.add(nodes[k]);
					}
				}
			}
		}
		

		System.out.println("Choosing nodes from list");
		
		for(int i = 0; i < nodeIDs.length;){
			int random = (int) (Math.random()*nodeList.size());
			
			boolean found = false;
			for(int j = 0; j< nodeIDs.length; j++){
				if(nodeIDs[j] != null && nodeIDs[j].equals(nodeList.get(random))){
					found = true;
				}
			}
			if(!found){
				nodeIDs[i] = nodeList.get(random);
				i++;
			}
		}

		return nodeIDs;
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