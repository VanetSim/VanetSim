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
import java.io.File;
import java.text.NumberFormat;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.gui.helpers.WekaHelper;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.map.Node;
import vanetsim.scenario.IDSProcessor;
import vanetsim.scenario.Vehicle;
import vanetsim.scenario.events.Cluster;
import vanetsim.scenario.events.EventSpot;
import vanetsim.scenario.events.EventSpotList;


/**
 * The control panel for editing events.
 */
public final class EditEventSpotsControlPanel extends JPanel implements ActionListener{
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = -8161612114065521616L;
	
	/** RadioButton to add event spot. */
	JRadioButton addItem_;
	
	/** RadioButton to delete event spot. */
	JRadioButton deleteItem_;
	
	/** A Label for event spot type ComboBox. */
	private JLabel eventSpotTypeLabel_;
		
	/** A JComboBox to switch between event spot types. */
	private JComboBox<String> eventSpotType_;
	
	/** The input field for the event spot radius. */
	private final JFormattedTextField eventSpotRadius_;
	
	/** The input field for the event spot frequency. */
	private final JFormattedTextField eventSpotFrequency_;
	
	/** Delete button to delete all vehicles. */
	private JButton deleteAllSpots_;
	
	/** Checkbox to import schools */
	private JCheckBox schoolBox_;
	
	/** Checkbox to import kindergartens */
	private JCheckBox kindergartenBox_;
	
	/** Checkbox to import police stations */
	private JCheckBox policeBox_;
	
	/** Checkbox to import firefighters  */
	private JCheckBox fireBox_;
	
	/** Checkbox to import hospitals */
	private JCheckBox hospitalBox_;
	
	/** The input field for the grid size of the event spot probabilities. */
	private final JFormattedTextField gridSize_;
	
	/** FileFilter to choose only ".log" files from FileChooser */
	private FileFilter logFileFilter_;
	
	/** a comboBox to select the event types to be displayed */
	private JComboBox<String> eventType_;
	
	/** a comboBox to select the cluster to be displayed */
	private JComboBox<Cluster> clusterSelection_;
	
	/** JPanel to consume whitespace */
	JPanel space_;
	
	/**
	 * Constructor.
	 */
	public EditEventSpotsControlPanel(){
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
	
		// Radio buttons to select add, edit or delete mode
		ButtonGroup group = new ButtonGroup();
		addItem_ = new JRadioButton(Messages.getString("EditEventSpotsControlPanel.add")); //$NON-NLS-1$
		addItem_.setActionCommand("add"); //$NON-NLS-1$
		addItem_.addActionListener(this);
		addItem_.setSelected(true);
		group.add(addItem_);
		++c.gridy;
		add(addItem_,c);
		
		deleteItem_ = new JRadioButton(Messages.getString("EditEventSpotsControlPanel.delete")); //$NON-NLS-1$
		deleteItem_.setActionCommand("delete"); //$NON-NLS-1$
		deleteItem_.setSelected(true);
		deleteItem_.addActionListener(this);
		group.add(deleteItem_);
		++c.gridy;
		add(deleteItem_,c);
		
		//add comboBox to choose vehicle types and vehicles
		c.gridwidth = 1;
		c.insets = new Insets(5,5,5,5);

		
		eventSpotTypeLabel_ = new JLabel(Messages.getString("EditEventSpotsControlPanel.eventSpotType")); //$NON-NLS-1$
		++c.gridy;
		add(eventSpotTypeLabel_,c);
		eventSpotType_ = new JComboBox<String>();
		eventSpotType_.addItem(Messages.getString("EditEventSpotsControlPanel.Hospital"));
		eventSpotType_.addItem(Messages.getString("EditEventSpotsControlPanel.Ice"));
		eventSpotType_.addItem(Messages.getString("EditEventSpotsControlPanel.StreetDamage"));
		eventSpotType_.addItem(Messages.getString("EditEventSpotsControlPanel.School"));
		eventSpotType_.setName("eventSpotType");
			
		eventSpotType_.addActionListener(this);
		c.gridx = 1;
		add(eventSpotType_, c);
		
	
		//add textfields and checkboxes to change eventspot properties
		c.gridx = 0;
		JLabel label = new JLabel(Messages.getString("EditEventSpotsControlPanel.eventSpotRadius")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		eventSpotRadius_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		eventSpotRadius_.setValue(100);
		eventSpotRadius_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(eventSpotRadius_,c);
	
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditEventSpotsControlPanel.eventSportFrequency")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		eventSpotFrequency_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		eventSpotFrequency_.setValue(10);
		eventSpotFrequency_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(eventSpotFrequency_,c);
		
		++c.gridy;
		c.gridwidth = 2;
		c.gridx = 0;	
		add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditEventSpotsControlPanel.schoolBox")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		schoolBox_ = new JCheckBox();
		c.gridx = 1;
		add(schoolBox_,c);
		
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditEventSpotsControlPanel.kindergartenBox")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		kindergartenBox_ = new JCheckBox();
		c.gridx = 1;
		add(kindergartenBox_,c);
		
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditEventSpotsControlPanel.policeBox")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		policeBox_ = new JCheckBox();
		c.gridx = 1;
		add(policeBox_,c);
		
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditEventSpotsControlPanel.fireBox")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		fireBox_ = new JCheckBox();
		c.gridx = 1;
		add(fireBox_,c);
		
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditEventSpotsControlPanel.hospitalBox")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		hospitalBox_ = new JCheckBox();
		c.gridx = 1;
		add(hospitalBox_,c);
		
		//import amenities
		++c.gridy;
		c.gridx = 0;
		JButton makejobs = new JButton(Messages.getString("EditEventSpotsControlPanel.importAmenities"));
		makejobs.setActionCommand("importAmenities");
		makejobs.setPreferredSize(new Dimension(200,20));
		makejobs.addActionListener(this);
		add(makejobs,c);
		
		
		++c.gridy;
		c.gridwidth = 2;
		c.gridx = 0;	
		add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		//create Event probabilities
		++c.gridy;
		c.gridx = 0;
		JButton createEventProbabilities = new JButton(Messages.getString("EditEventSpotsControlPanel.createProbabilities"));
		createEventProbabilities.setActionCommand("createProbabilities");
		createEventProbabilities.setPreferredSize(new Dimension(200,20));
		createEventProbabilities.addActionListener(this);
		add(createEventProbabilities,c);
		
		//open Event probabilities
		++c.gridy;
		c.gridx = 0;
		JButton openEventProbabilities = new JButton(Messages.getString("EditEventSpotsControlPanel.openProbabilities"));
		openEventProbabilities.setActionCommand("openProbabilities");
		openEventProbabilities.setPreferredSize(new Dimension(200,20));
		openEventProbabilities.addActionListener(this);
		add(openEventProbabilities,c);
		
		//import centroids from weka
		++c.gridy;
		c.gridx = 0;
		JButton importCentroids = new JButton(Messages.getString("EditEventSpotsControlPanel.importCentroids"));
		importCentroids.setActionCommand("importCentroids");
		importCentroids.setPreferredSize(new Dimension(200,20));
		importCentroids.addActionListener(this);
		add(importCentroids,c);
		
		//import clusters from weka
		++c.gridy;
		c.gridx = 0;
		JButton importClusters = new JButton(Messages.getString("EditEventSpotsControlPanel.importClusters"));
		importClusters.setActionCommand("importClusters");
		importClusters.setPreferredSize(new Dimension(200,20));
		importClusters.addActionListener(this);
		add(importClusters,c);
		
		//select cluster
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditEventSpotsControlPanel.selectCluster")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);
		clusterSelection_ = new JComboBox<Cluster>();
		clusterSelection_.setActionCommand("selectCluster");
		clusterSelection_.addActionListener(this);

		c.gridx = 1;
		add(clusterSelection_, c);
		
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditEventSpotsControlPanel.eventSpotGrid")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);		
		gridSize_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		gridSize_.setValue(10000);
		gridSize_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		add(gridSize_,c);
		
		
		//select event type
		c.gridx = 0;
		label = new JLabel(Messages.getString("EditEventSpotsControlPanel.eventTypes")); //$NON-NLS-1$
		++c.gridy;
		add(label,c);
		eventType_ = new JComboBox<String>();
		for(int i = 0; i < IDSProcessor.getIdsData_().length; i++) eventType_.addItem(IDSProcessor.getIdsData_()[i]);

		c.gridx = 1;
		add(eventType_, c);
		
		//load Event probabilities
		++c.gridy;
		c.gridx = 0;
		JButton loadEventProbabilities = new JButton(Messages.getString("EditEventSpotsControlPanel.refreshProbabilities"));
		loadEventProbabilities.setActionCommand("refreshProbabilities");
		loadEventProbabilities.setPreferredSize(new Dimension(200,20));
		loadEventProbabilities.addActionListener(this);
		add(loadEventProbabilities,c);
		

		

		
		++c.gridy;
		c.gridwidth = 2;
		c.gridx = 0;	
		add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		c.gridx = 0;
		c.gridwidth = 2;
		++c.gridy;
		deleteAllSpots_ = ButtonCreator.getJButton("deleteAll.png", "clearSpots", Messages.getString("EditEventSpotsControlPanel.deleteAllSpots"), this);
		add(deleteAllSpots_,c);	
		
		
		//define FileFilter for fileChooser
		logFileFilter_ = new FileFilter(){
			public boolean accept(File f) {
				if (f.isDirectory()) return true;
				return f.getName().toLowerCase().endsWith(".log"); //$NON-NLS-1$
			}
			public String getDescription () { 
				return Messages.getString("LogAnalyserDialog.logFiles") + " (*.log)"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		};
		
		//to consume the rest of the space
		c.weighty = 1.0;
		++c.gridy;
		space_ = new JPanel();
		space_.setOpaque(false);
		add(space_, c);
	}
	
	/**
	 * Receives a mouse event.
	 * 
	 * @param x	the x coordinate (in map scale)
	 * @param y	the y coordinate (in map scale)
	 */
	public void receiveMouseEvent(int x, int y){
		if(addItem_.isSelected()){
				EventSpotList.getInstance().addEventSpot(new EventSpot(x,y, ((Number)eventSpotFrequency_.getValue()).intValue()*1000, ((Number)eventSpotRadius_.getValue()).intValue()*100, returnAmenityCode(eventSpotType_.getSelectedItem().toString()), Vehicle.getRandom().nextLong()));
			
		}
		else if(deleteItem_.isSelected()){
			EventSpotList.getInstance().delEventSpot(EventSpotList.getInstance().findEventSpot(x, y));
		}
		
		Renderer.getInstance().ReRender(true, true);
	}
	

	/**
	 * An implemented <code>ActionListener</code> which performs all needed actions when a <code>JButton</code>
	 * is clicked.
	 * 
	 * @param e	an <code>ActionEvent</code>
	 */	
	public void actionPerformed(ActionEvent e) {		
		String command = e.getActionCommand();
		if("clearSpots".equals(command)) EventSpotList.getInstance().clearEvents();
		else if("importAmenities".equals(command)){
			for(Node n:Map.getInstance().getAmenityList()){
				if(schoolBox_.isSelected() && n.getAmenity_().equals("school"))	EventSpotList.getInstance().addEventSpot(new EventSpot(n.getX(),n.getY(), ((Number)eventSpotFrequency_.getValue()).intValue()*1000, ((Number)eventSpotRadius_.getValue()).intValue()*100, n.getAmenity_(), Vehicle.getRandom().nextLong()));
				else if(kindergartenBox_.isSelected() && n.getAmenity_().equals("kindergarten"))	EventSpotList.getInstance().addEventSpot(new EventSpot(n.getX(),n.getY(), ((Number)eventSpotFrequency_.getValue()).intValue()*1000, ((Number)eventSpotRadius_.getValue()).intValue()*100, n.getAmenity_(), Vehicle.getRandom().nextLong()));
				else if(policeBox_.isSelected() && n.getAmenity_().equals("police"))	EventSpotList.getInstance().addEventSpot(new EventSpot(n.getX(),n.getY(), ((Number)eventSpotFrequency_.getValue()).intValue()*1000, ((Number)eventSpotRadius_.getValue()).intValue()*100, n.getAmenity_(), Vehicle.getRandom().nextLong()));
				else if(hospitalBox_.isSelected() && n.getAmenity_().equals("hospital"))	EventSpotList.getInstance().addEventSpot(new EventSpot(n.getX(),n.getY(), ((Number)eventSpotFrequency_.getValue()).intValue()*1000, ((Number)eventSpotRadius_.getValue()).intValue()*100, n.getAmenity_(), Vehicle.getRandom().nextLong()));
				else if(fireBox_.isSelected() && n.getAmenity_().equals("fire_station"))	EventSpotList.getInstance().addEventSpot(new EventSpot(n.getX(),n.getY(), ((Number)eventSpotFrequency_.getValue()).intValue()*1000, ((Number)eventSpotRadius_.getValue()).intValue()*100, n.getAmenity_(), Vehicle.getRandom().nextLong()));

			}
		}
		else if("createProbabilities".equals(command)){
			//start with open file dialog
			JFileChooser fc = new JFileChooser();

			//set directory and ".log" filter
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			fc.addChoosableFileFilter(logFileFilter_);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(logFileFilter_);
			
			int status = fc.showOpenDialog(this);
			
			if(status == JFileChooser.APPROVE_OPTION){
				EventSpotList.getInstance().calculateGrid(((Number)gridSize_.getValue()).doubleValue(), fc.getSelectedFile().getAbsoluteFile().getAbsolutePath(), eventType_.getSelectedItem().toString());
			}		
		}
		else if("refreshProbabilities".equals(command)){
			EventSpotList.getInstance().showGrid(eventType_.getSelectedItem().toString(), ((Number)gridSize_.getValue()).intValue());
		}
		else if("openProbabilities".equals(command)){
			//start with open file dialog
			JFileChooser fc = new JFileChooser();

			//set directory and ".log" filter
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			fc.addChoosableFileFilter(logFileFilter_);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(logFileFilter_);
			
			int status = fc.showOpenDialog(this);
			
			if(status == JFileChooser.APPROVE_OPTION){
				EventSpotList.getInstance().loadGrid(fc.getSelectedFile().getAbsoluteFile().getAbsolutePath(), eventType_.getSelectedItem().toString());
			}	
		}
		else if("importCentroids".equals(command)){
			//start with open file dialog
			JFileChooser fc = new JFileChooser();

			//set directory and ".log" filter
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			fc.addChoosableFileFilter(logFileFilter_);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(logFileFilter_);
			
			int status = fc.showOpenDialog(this);
			
			if(status == JFileChooser.APPROVE_OPTION){
				Renderer.getInstance().setCentroidsX(WekaHelper.readWekaCentroids(fc.getSelectedFile().getAbsoluteFile().getAbsolutePath(), "X"));
				Renderer.getInstance().setCentroidsY(WekaHelper.readWekaCentroids(fc.getSelectedFile().getAbsoluteFile().getAbsolutePath(), "Y"));
				
				
			}	
		}
		else if("importClusters".equals(command)){
			//start with open file dialog
			JFileChooser fc = new JFileChooser();

			//set directory and ".log" filter
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			fc.setAcceptAllFileFilterUsed(true);
			fc.setFileFilter(logFileFilter_);
			
			int status = fc.showOpenDialog(this);
			
			clusterSelection_.removeActionListener(this);
			clusterSelection_.removeAllItems();
			
			Cluster allClusters = new Cluster();
			allClusters.setClusterID_("all");
			Cluster noneClusters = new Cluster();
			noneClusters.setClusterID_("none");
			clusterSelection_.addItem(noneClusters);
			clusterSelection_.addItem(allClusters);
			
			
			if(status == JFileChooser.APPROVE_OPTION){
				for(int i = 0; ;i++){
					Cluster tmpCluster = new Cluster();
					
					if(!tmpCluster.fillCluster(fc.getSelectedFile().getAbsoluteFile().getAbsolutePath(), i + "")) break;
					clusterSelection_.addItem(tmpCluster);
					Renderer.getInstance().getClusters().add(tmpCluster);
				}
				
			}	
			
			
			clusterSelection_.addActionListener(this);
			if(Renderer.getInstance().getClusters().get(0) != null) clusterSelection_.setSelectedIndex(0);
		}
		else if("selectCluster".equals(command)){
			Renderer.getInstance().setDisplayCluster_(null);
			Renderer.getInstance().setShowAllClusters(false);

			if(((Cluster)clusterSelection_.getSelectedItem()).getClusterID_().equals("all")) Renderer.getInstance().setShowAllClusters(true);
			else if(!((Cluster)clusterSelection_.getSelectedItem()).getClusterID_().equals("none")) Renderer.getInstance().setDisplayCluster_((Cluster)clusterSelection_.getSelectedItem());
				
		}

		
		Renderer.getInstance().ReRender(true, true);
	}
	
	/**
	 * returns the amenity code
	 */
	public String returnAmenityCode(String a){
		if(a.equals(Messages.getString("EditEventSpotsControlPanel.Hospital"))) return "hospital";
		else if(a.equals(Messages.getString("EditEventSpotsControlPanel.School"))) return "school";
		else if(a.equals(Messages.getString("EditEventSpotsControlPanel.Kindergarten"))) return "kindergarten";
		else if(a.equals(Messages.getString("EditEventSpotsControlPanel.Fire"))) return "fire_station";
		else if(a.equals(Messages.getString("EditEventSpotsControlPanel.Police"))) return "police";
		else if(a.equals(Messages.getString("EditEventSpotsControlPanel.Ice"))) return "ice";
		else if(a.equals(Messages.getString("EditEventSpotsControlPanel.StreetDamage"))) return "streetDamage";

		return "";
	}

	/**
	 * @return the gridSize_
	 */
	public JFormattedTextField getGridSize_() {
		return gridSize_;
	}

}