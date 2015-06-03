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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import vanetsim.VanetSimStart;
import vanetsim.gui.helpers.VehicleType;
import vanetsim.gui.helpers.VehicleTypeXML;
import vanetsim.localization.Messages;

/**
 * A dialog to create,edit and delete vehicle type files.
 */


public final class VehicleTypeDialog extends JDialog implements ActionListener, MouseListener{
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = -2918735209479587896L;

	/** A JComboBox to switch between vehicles types. */
	private JComboBox<VehicleType> chooseVehicleType_ = new JComboBox<VehicleType>();	
	
	/** The field to show and save the current vehicle type file path. */
	private final JFormattedTextField filePath_ = new JFormattedTextField();
	
	/** The input field for the vehicle length. */
	private final JFormattedTextField vehicleLength_ = new JFormattedTextField();
	
	/** The input field for the minimum speed. */
	private final JFormattedTextField minSpeed_ = new JFormattedTextField();
	
	/** The input field for the maximum speed. */
	private final JFormattedTextField maxSpeed_ = new JFormattedTextField();
	
	/** The input field for the minimum communication distance. */
	private final JFormattedTextField minCommDist_ = new JFormattedTextField();
	
	/** The input field for the maximum communication distance.. */
	private final JFormattedTextField maxCommDist_ = new JFormattedTextField();
	
	/** The input field for the minimum wait time in milliseconds. */
	private final JFormattedTextField minWait_ = new JFormattedTextField();
	
	/** The input field for the maximum wait time in milliseconds. */
	private final JFormattedTextField maxWait_ = new JFormattedTextField();
	
	/** The input field for the minimum braking rate in cm/s^2. */
	private final JFormattedTextField minBraking_ = new JFormattedTextField();
	
	/** The input field for the maximum braking rate in cm/s^2. */
	private final JFormattedTextField maxBraking_ = new JFormattedTextField();	
	
	/** The input field for the minimum acceleration rate in cm/s^2. */
	private final JFormattedTextField minAcceleration_ = new JFormattedTextField();
	
	/** The input field for the maximum acceleration rate in cm/s^2. */
	private final JFormattedTextField maxAcceleration_ = new JFormattedTextField();	
	
	/** The input field for the minimum time distance in ms. */
	private final JFormattedTextField minTimeDistance_ = new JFormattedTextField();		
	
	/** The input field for the maximum time distance in ms. */
	private final JFormattedTextField maxTimeDistance_ = new JFormattedTextField();	

	/** The input field for the minimum politeness factor in %. */
	private final JFormattedTextField minPoliteness_ = new JFormattedTextField();		
	
	/** The input field for the maximum politeness factor in %. */
	private final JFormattedTextField maxPoliteness_ = new JFormattedTextField();		
	
	/** The input field for how much vehicles deviate from the max. speed limit. */
	private final JFormattedTextField vehiclesDeviatingMaxSpeed_  = new JFormattedTextField();
	
	/** The input field for the number of km/h the vehicles will deviate from the max. speed limit. */
	private final JFormattedTextField deviationFromSpeedLimit_  = new JFormattedTextField();
	
	/** The checkbox to activate and deactivate wiFi. */
	private final JCheckBox wifi_;	
	
	/** The checkbox to activate and deactivate emergency vehicle functions */
	private final JCheckBox emergencyVehicle_;	
	
	/** JPanel to preview the selected Vehicle-Color. */
	private final JPanel colorPreview_;	
	
	/** Saves previous item of Combobox */
	private VehicleType prevItem_ = null;
	
	/** FileFilter to choose only ".xml" files from FileChooser */
	private FileFilter xmlFileFilter_;
	
	/**
	 * Constructor. Creating GUI items.
	 */
	public VehicleTypeDialog(){
		//some JDialog options
		setLayout(new GridBagLayout());
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		//WindowAdapter to catch closing event
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                    closeDialog();
            }
        }
        );  
        
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
		c.gridwidth = 1;	
		c.insets = new Insets(5,5,5,5);
		
		//start building gui
		++c.gridy;
		filePath_.setEditable(false);
		filePath_.setPreferredSize(new Dimension(100,20));
		add(filePath_, c);
		
		c.gridx = 1;
		JPanel OpenFilePanel = new JPanel();
		add(OpenFilePanel, c);
		
		//add navigation
		OpenFilePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JButton btnOpen_ = new JButton(Messages.getString("VehicleTypeDialog.btnOpen"));
		btnOpen_.setActionCommand("openFile");
		btnOpen_.setPreferredSize(new Dimension(30,20));
		btnOpen_.addActionListener(this);
		OpenFilePanel.add(btnOpen_);
		
		JButton btnStandard_ = new JButton(Messages.getString("VehicleTypeDialog.btnMakeStandard"));
		btnStandard_.setActionCommand("makeStandard");
		btnStandard_.setPreferredSize(new Dimension(200,20));
		btnStandard_.addActionListener(this);
		OpenFilePanel.add(btnStandard_);
		
		c.gridx = 0;
		++c.gridy;		
		add(new JLabel(Messages.getString("VehicleTypeDialog.selectType")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(chooseVehicleType_, c);
		c.gridx = 0;
		
		//gets the vehicle type standard file ("vehicleType.xml")
		VehicleTypeXML xml = new VehicleTypeXML(null);
		for(VehicleType type : xml.getVehicleTypes()){
			chooseVehicleType_.addItem(type);
		}
		chooseVehicleType_.addActionListener(this);
		filePath_.setValue(xml.getDefaultPath());
		
		
		//add textfields, checkboxes, preview panel
		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.minSpeed")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(minSpeed_, c);
		c.gridx = 0;
		
		
		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.maxSpeed")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(maxSpeed_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.minCommDist")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(minCommDist_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.maxCommDist")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(maxCommDist_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.minWait")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(minWait_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.maxWait")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(maxWait_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.minBraking")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(minBraking_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.maxBraking")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(maxBraking_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.minAcceleration")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(minAcceleration_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.maxAcceleration")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(maxAcceleration_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.minTimeDistance")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(minTimeDistance_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.maxTimeDistance")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(maxTimeDistance_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.minPoliteness")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(minPoliteness_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.maxPoliteness")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(maxPoliteness_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("EditVehicleControlPanel.vehiclesDeviatingMaxSpeed")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(vehiclesDeviatingMaxSpeed_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("EditVehicleControlPanel.deviationFromSpeedLimit")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(deviationFromSpeedLimit_, c);
		c.gridx = 0;

		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.vehicleLength")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(vehicleLength_, c);
		c.gridx = 0;
		
		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.wifi")),c); //$NON-NLS-1$	
		wifi_ = new JCheckBox();
		c.gridx = 1;
		add(wifi_,c);
		
		c.gridx = 0;
		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.emergencyVehicle")),c); //$NON-NLS-1$	
		emergencyVehicle_ = new JCheckBox();
		c.gridx = 1;
		add(emergencyVehicle_,c);
		
		c.gridx = 0;
		++c.gridy;
		add(new JLabel(Messages.getString("VehicleTypeDialog.color")),c);
		colorPreview_ = new JPanel();	
		colorPreview_.setBackground(Color.black);
		colorPreview_.setSize(10, 10);
		colorPreview_.addMouseListener(this);
		c.gridx = 1;
		add(colorPreview_,c);

		//add vehicle type buttons
		++c.gridy;
		c.gridx = 0;
		add(new JLabel(Messages.getString("VehicleTypeDialog.TypeOptions")),c); //$NON-NLS-9$
		
		c.gridx = 1;
		JPanel TypePanel = new JPanel();
		add(TypePanel, c);
		
		TypePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JButton btnNewType_ = new JButton(Messages.getString("VehicleTypeDialog.btnNewType"));
		btnNewType_.setActionCommand("newType");
		btnNewType_.setPreferredSize(new Dimension(80,20));
		btnNewType_.addActionListener(this);
		TypePanel.add(btnNewType_);
		
		JButton btnDeleteType_ = new JButton(Messages.getString("VehicleTypeDialog.btnDeleteType"));
		btnDeleteType_.setActionCommand("deleteType");
		btnDeleteType_.setPreferredSize(new Dimension(80,20));
		btnDeleteType_.addActionListener(this);
		TypePanel.add(btnDeleteType_);	
		
		//add file buttons
		++c.gridy;
		c.gridx = 0;
		add(new JLabel(Messages.getString("VehicleTypeDialog.FileOptions")),c); //$NON-NLS-9$
		
		c.gridx = 1;
		JPanel FilePanel = new JPanel();
		add(FilePanel, c);
		
		FilePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JButton btnNewFile_ = new JButton(Messages.getString("VehicleTypeDialog.btnNewFile"));
		btnNewFile_.setActionCommand("newFile");
		btnNewFile_.setPreferredSize(new Dimension(80,20));
		btnNewFile_.addActionListener(this);
		FilePanel.add(btnNewFile_);

		JButton btnSaveFile_ = new JButton(Messages.getString("VehicleTypeDialog.btnSaveFile"));
		btnSaveFile_.setActionCommand("saveFile");
		btnSaveFile_.setPreferredSize(new Dimension(80,20));
		btnSaveFile_.addActionListener(this);
		FilePanel.add(btnSaveFile_);	
		
		JButton btnDeleteFile_ = new JButton(Messages.getString("VehicleTypeDialog.btnDeleteFile"));
		btnDeleteFile_.setActionCommand("deleteFile");
		btnDeleteFile_.setPreferredSize(new Dimension(80,20));
		btnDeleteFile_.addActionListener(this);
		FilePanel.add(btnDeleteFile_);	
		
		//end of gui
		
		//updates the input fields to the first vehicle type
		actionPerformed(new ActionEvent(chooseVehicleType_,0,"comboBoxChanged"));
		
		//define FileFilter for fileChooser
		xmlFileFilter_ = new FileFilter(){
			public boolean accept(File f) {
				if (f.isDirectory()) return true;
				return f.getName().toLowerCase().endsWith(".xml"); //$NON-NLS-1$
			}
			public String getDescription () { 
				return Messages.getString("MainControlPanel.xmlFiles") + " (*.xml)"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		};
		
		//to consume the rest of the space
		c.weighty = 1.0;
		++c.gridy;
		add(new JPanel(), c);
		
		//resize window
		pack();
		//adjust window size
		setLocationRelativeTo(VanetSimStart.getMainFrame());
		//show window
		setVisible(true);
	}

	/**
	 * An implemented <code>ActionListener</code> which performs the needed actions when the combobox or buttons (load, save, delete ...)
	 * are clicked.
	 * 
	 * @param e	an <code>ActionEvent</code>
	 */
	public void actionPerformed(ActionEvent e) {	
		String command = e.getActionCommand();

		if ("comboBoxChanged".equals(command)){
			//save previous vehicle type
			if(prevItem_ != null) saveType(prevItem_);

			//update gui fields
			VehicleType tmpVehicleType = (VehicleType) chooseVehicleType_.getSelectedItem();
			if(tmpVehicleType != null){
				maxSpeed_.setValue((int)Math.round(tmpVehicleType.getMaxSpeed() / (100000.0/3600)));
				vehicleLength_.setValue(tmpVehicleType.getVehicleLength());
				maxCommDist_.setValue((int)Math.round(tmpVehicleType.getMaxCommDist() / 100));
				maxWait_.setValue((int)tmpVehicleType.getMaxWaittime());
				maxBraking_.setValue((int)tmpVehicleType.getMaxBrakingRate());
				maxAcceleration_.setValue((int)tmpVehicleType.getMaxAccelerationRate());
				maxTimeDistance_.setValue((int)tmpVehicleType.getMaxTimeDistance());
				maxPoliteness_.setValue((int)tmpVehicleType.getMaxPoliteness());
				minSpeed_.setValue((int)Math.round(tmpVehicleType.getMinSpeed() / (100000.0/3600)));
				minCommDist_.setValue((int)Math.round(tmpVehicleType.getMinCommDist() / 100));
				minWait_.setValue((int)tmpVehicleType.getMinWaittime());
				minBraking_.setValue((int)tmpVehicleType.getMinBrakingRate());
				minAcceleration_.setValue((int)tmpVehicleType.getMinAccelerationRate());
				minTimeDistance_.setValue((int)tmpVehicleType.getMinTimeDistance());
				minPoliteness_.setValue((int)tmpVehicleType.getMinPoliteness());
				wifi_.setSelected(tmpVehicleType.isWifi());
				emergencyVehicle_.setSelected(tmpVehicleType.isEmergencyVehicle());
				colorPreview_.setBackground(new Color(tmpVehicleType.getColor()));			
				deviationFromSpeedLimit_.setValue((int)Math.round(tmpVehicleType.getDeviationFromSpeedLimit_() / (100000.0/3600)));
				vehiclesDeviatingMaxSpeed_.setValue((int)tmpVehicleType.getVehiclesDeviatingMaxSpeed_());
				
				
				pack();
				
				//save vehicle type to have control over the last shown type when switched
				prevItem_ = tmpVehicleType;
			}
		}
		else if(("openFile").equals(command)){
			//check if type or file should be saved before continuing
			saveQuestions();

			//start with open file dialog
			JFileChooser fc = new JFileChooser();

			//set directory and ".xml" filter
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			fc.addChoosableFileFilter(xmlFileFilter_);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(xmlFileFilter_);
			
			int status = fc.showOpenDialog(this);
			
			if(status == JFileChooser.APPROVE_OPTION){
				String tmpPath = filePath_.getValue().toString();
				filePath_.setValue(fc.getSelectedFile().getAbsoluteFile());
				
				VehicleTypeXML xml = new VehicleTypeXML(filePath_.getValue().toString());
				
				if(xml.getVehicleTypes().size() == 0){
					filePath_.setValue(tmpPath);
					JOptionPane.showMessageDialog(null, Messages.getString("VehicleTypeDialog.loadFileError"), "Error", JOptionPane.ERROR_MESSAGE);
				}
				else{		
					openFile(filePath_.getValue().toString());
				}
			}		
		}
		else if("newFile".equals(command)){
			//check if type or file should be saved before continuing
			saveQuestions();
			
			//begin with creation of new file
			JFileChooser fc = new JFileChooser();
			//set directory and ".xml" filter
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			fc.addChoosableFileFilter(xmlFileFilter_);
			fc.setFileFilter(xmlFileFilter_);
			
			int status = fc.showDialog(this, "Create");
			
			if(status == JFileChooser.APPROVE_OPTION){
				if(!fc.getSelectedFile().getAbsoluteFile().toString().equals("vehicleTypes.xml") && !fc.getSelectedFile().getAbsoluteFile().toString().equals(System.getProperty("user.dir") + "/vehicleTypes.xml")){
					if(fc.getSelectedFile().getAbsoluteFile().toString().toLowerCase().endsWith(".xml")) filePath_.setValue(fc.getSelectedFile().getAbsoluteFile());
					else filePath_.setValue(fc.getSelectedFile().getAbsoluteFile() + ".xml");
					
					VehicleTypeXML xml = new VehicleTypeXML(filePath_.getValue().toString());
					ArrayList<VehicleType> tmpList = new ArrayList<VehicleType>();
					String typeName = "";
					while(typeName.equals("")){
						typeName = JOptionPane.showInputDialog(Messages.getString("VehicleTypeDialog.inputTypeName"));
					}
					tmpList.add(new VehicleType(typeName,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0, 0, 0,false,false,-16777216));
					xml.saveVehicleTypes(tmpList);
					
					chooseVehicleType_.removeActionListener(this);
					chooseVehicleType_.removeAllItems();
					chooseVehicleType_.addActionListener(this);
					
					for(VehicleType type : xml.getVehicleTypes()){
						chooseVehicleType_.addItem(type);
					}
					
					
					//updates the input fields to the first vehicle type
					actionPerformed(new ActionEvent(chooseVehicleType_,0,"comboBoxChanged"));
				}
				else JOptionPane.showMessageDialog(null, Messages.getString("VehicleTypeDialog.deleteVehicleTypesFile"), "Error", JOptionPane.ERROR_MESSAGE);	
			}
			
		}
		else if("saveFile".equals(command)){
			// save actual vehicle type
			if(chooseVehicleType_.getSelectedItem() != null) saveType((VehicleType) chooseVehicleType_.getSelectedItem());
			
			if(chooseVehicleType_.getItemCount() > 0){
				VehicleTypeXML xml = new VehicleTypeXML(filePath_.getValue().toString());
				
				ArrayList<VehicleType> tmpList = new ArrayList<VehicleType>();
				for(int i = 0; i < chooseVehicleType_.getItemCount(); i ++) tmpList.add((VehicleType) chooseVehicleType_.getItemAt(i));
					
				xml.saveVehicleTypes(tmpList);
				
				JOptionPane.showMessageDialog(null, Messages.getString("VehicleTypeDialog.saveFileSuccess"), "Information", JOptionPane.INFORMATION_MESSAGE);
				
				//if the vehicleTypes.xml has been edited refresh the two vehicle Panels
				if((filePath_.getValue().toString().equals("vehicleTypes.xml") || filePath_.getValue().toString().equals(System.getProperty("user.dir") + "/vehicleTypes.xml"))){
					VanetSimStart.getMainControlPanel().getEditPanel().getEditOneVehiclePanel().refreshVehicleTypes();
					VanetSimStart.getMainControlPanel().getEditPanel().getEditVehiclePanel().refreshVehicleTypes();
				}
			}
		}
		else if("deleteFile".equals(command)){
			if(!filePath_.getValue().toString().equals("vehicleTypes.xml") && !filePath_.getValue().toString().equals(System.getProperty("user.dir") + "/vehicleTypes.xml")){
			//delete file
				if(JOptionPane.showConfirmDialog(null, Messages.getString("VehicleTypeDialog.deleteFile"), "", JOptionPane.YES_NO_OPTION) == 0){
			
					boolean success = (new File(filePath_.getValue().toString())).delete();
					
					if(success){
						JOptionPane.showMessageDialog(null, Messages.getString("VehicleTypeDialog.deleteSuccess"), "Information", JOptionPane.INFORMATION_MESSAGE);
						filePath_.setValue("");
						clearGui();
					}
					else JOptionPane.showMessageDialog(null, Messages.getString("VehicleTypeDialog.deleteError"), "Error", JOptionPane.ERROR_MESSAGE);					

					prevItem_ = null;
					openFile("vehicleTypes.xml");

				}
			}
			else JOptionPane.showMessageDialog(null, Messages.getString("VehicleTypeDialog.deleteVehicleTypesFile"), "Error", JOptionPane.ERROR_MESSAGE);				
		}
		else if("newType".equals(command)){
			//add new type
			removeEmptyTypes(); //because when creating a file a empty type (with no name) is added
			String typeName = "";
			while(typeName != null && typeName.equals("")){
				typeName = JOptionPane.showInputDialog(Messages.getString("VehicleTypeDialog.inputTypeName"));
			}

			chooseVehicleType_.addItem(new VehicleType(typeName,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,false,false,-16777216));
			
			chooseVehicleType_.setSelectedIndex(chooseVehicleType_.getItemCount()-1);
		}
		else if("deleteType".equals(command)){
			//delete a type
			if(JOptionPane.showConfirmDialog(null, Messages.getString("VehicleTypeDialog.deleteType"), "", JOptionPane.YES_NO_OPTION) == 0){
				chooseVehicleType_.removeActionListener(this);
				chooseVehicleType_.removeItemAt(chooseVehicleType_.getSelectedIndex());
				chooseVehicleType_.addActionListener(this);
			
				if(chooseVehicleType_.getItemCount() > 0){
					chooseVehicleType_.setSelectedIndex(0);
				
					//updates the input fields to the first vehicle type
					actionPerformed(new ActionEvent(chooseVehicleType_,0,"comboBoxChanged"));
				}
				else clearGui();
			}
		}
		else if("makeStandard".equals(command)){
			//saved this vehicle types to the standard file (vehicleTypes.xml)
			if(JOptionPane.showConfirmDialog(null, Messages.getString("VehicleTypeDialog.makeStandardBox"), "", JOptionPane.YES_NO_OPTION) == 0){
			VehicleTypeXML xml = new VehicleTypeXML("vehicleTypes.xml");
			
			ArrayList<VehicleType> tmpList = new ArrayList<VehicleType>();
			for(int i = 0; i < chooseVehicleType_.getItemCount(); i ++) tmpList.add((VehicleType) chooseVehicleType_.getItemAt(i));
				
			xml.saveVehicleTypes(tmpList);
			
			
			VanetSimStart.getMainControlPanel().getEditPanel().getEditOneVehiclePanel().refreshVehicleTypes();
			VanetSimStart.getMainControlPanel().getEditPanel().getEditVehiclePanel().refreshVehicleTypes();
			}
		}
	}
	
	/**
	 * Removes all items of JCombobox
	 * Sets all gui fields to 0 or false
	 */
	public void clearGui(){
		chooseVehicleType_.removeActionListener(this); //important: remove ActionListener before removing all items
		chooseVehicleType_.removeAllItems();
		chooseVehicleType_.addActionListener(this);
		
		maxSpeed_.setValue(0);
		vehicleLength_.setValue(0);
		maxCommDist_.setValue(0);
		maxWait_.setValue(0);
		maxBraking_.setValue(0);
		maxAcceleration_.setValue(0);
		maxTimeDistance_.setValue(0);
		maxPoliteness_.setValue(0);
		minSpeed_.setValue(0);
		minCommDist_.setValue(0);
		minWait_.setValue(0);
		minBraking_.setValue(0);
		minAcceleration_.setValue(0);
		minTimeDistance_.setValue(0);
		minPoliteness_.setValue(0);
		vehiclesDeviatingMaxSpeed_.setValue(0);
		deviationFromSpeedLimit_.setValue(0);
		wifi_.setSelected(false);
		emergencyVehicle_.setSelected(false);
		colorPreview_.setBackground(new Color(0));
	}
	
	/**
	 * Clears the JCombobox and adds new items from xml File
	 */
	public void openFile(String file){
		filePath_.setValue(file);
		chooseVehicleType_.removeActionListener(this);  //important: remove ActionListener before removing all items
		chooseVehicleType_.removeAllItems();
		chooseVehicleType_.addActionListener(this);
		
		VehicleTypeXML xml = new VehicleTypeXML(filePath_.getValue().toString());
		
		for(VehicleType type : xml.getVehicleTypes()){
			chooseVehicleType_.addItem(type);
		}
				
		//updates the input fields to the first vehicle type
		actionPerformed(new ActionEvent(chooseVehicleType_,0,"comboBoxChanged"));
	}
	
	/**
	 * Compares two vehicle type arraylists if they are different
	 * 
	 * @return true, false
	 */
	public boolean compareTypeList(ArrayList<VehicleType> list1, ArrayList<VehicleType> list2){
		if(list1.size() != list2.size()) return false;
		
		for(int i = 0; i < list1.size()-1; i++){
			VehicleType type1 = list1.get(i);
			VehicleType type2 = list2.get(i);
			
			if(type1.getVehicleLength() != type2.getVehicleLength()) return false;
			if(type1.getMinSpeed() != type2.getMinSpeed()) return false;
			if(type1.getMaxSpeed() != type2.getMaxSpeed()) return false;
			if(type1.getMinCommDist() != type2.getMinCommDist()) return false;
			if(type1.getMaxCommDist() != type2.getMaxCommDist()) return false;
			if(type1.getMinWaittime() != type2.getMinWaittime()) return false;
			if(type1.getMaxWaittime() != type2.getMaxWaittime()) return false;
			if(type1.getMinBrakingRate() != type2.getMinBrakingRate()) return false;
			if(type1.getMaxBrakingRate() != type2.getMaxBrakingRate()) return false;
			if(type1.getMinAccelerationRate() != type2.getMinAccelerationRate()) return false;
			if(type1.getMaxAccelerationRate() != type2.getMaxAccelerationRate()) return false;
			if(type1.getMinTimeDistance() != type2.getMinTimeDistance()) return false;
			if(type1.getMaxTimeDistance() != type2.getMaxTimeDistance()) return false;
			if(type1.getMinPoliteness() != type2.getMinPoliteness()) return false;
			if(type1.getMaxPoliteness() != type2.getMaxPoliteness()) return false;
			if(type1.getDeviationFromSpeedLimit_() != type2.getDeviationFromSpeedLimit_()) return false;
			if(type1.getVehiclesDeviatingMaxSpeed_() != type2.getVehiclesDeviatingMaxSpeed_()) return false;
			if(type1.isWifi() != type2.isWifi()) return false;
			if(type1.isEmergencyVehicle() != type2.isEmergencyVehicle()) return false;
			if(type1.getColor() != type2.getColor()) return false;
		}
		return true;
	}
	
	/**
	 * Save gui fields values to VehicleType object
	 * @throws ParseException 
	 * 
	 */
	public void saveType(VehicleType tmpType) {	
		//at first all JFormattedTextFields have to be validated manually. Otherwise getValue() returns the old values
		//JFormattedTextField should do this on its own but it doesn't work in this case because it only
		//validates if another component is clicked. This works with buttons, textfields ... but it seems
		//not with the X and the WindowsListener used in this case. I couldn't find out if this is a bug 
		// or a feature :-), but i will fix this as soon as i get mail from the sun support forum.
		try {
			vehicleLength_.commitEdit();
			minSpeed_.commitEdit();
			maxSpeed_.commitEdit();
			minCommDist_.commitEdit();
			maxCommDist_.commitEdit();
			minWait_.commitEdit();
			maxWait_.commitEdit();
			minBraking_.commitEdit();
			maxBraking_.commitEdit();
			minAcceleration_.commitEdit();
			maxAcceleration_.commitEdit();
			minTimeDistance_.commitEdit();
			maxTimeDistance_.commitEdit();
			minPoliteness_.commitEdit();
			maxPoliteness_.commitEdit();
			deviationFromSpeedLimit_.commitEdit();
			vehiclesDeviatingMaxSpeed_.commitEdit();
				
		} catch (ParseException e) {}
		tmpType.setVehicleLength(((Number)vehicleLength_.getValue()).intValue());
		tmpType.setMinSpeed((int)Math.round(((Number)minSpeed_.getValue()).intValue() * (100000.0/3600)));
		tmpType.setMaxSpeed((int)Math.round(((Number)maxSpeed_.getValue()).intValue() * (100000.0/3600)));
		tmpType.setMinCommDist((int)Math.round(((Number)minCommDist_.getValue()).intValue() * 100));
		tmpType.setMaxCommDist((int)Math.round(((Number)maxCommDist_.getValue()).intValue() * 100));
		tmpType.setMinWaittime((int)Math.round(((Number)minWait_.getValue()).intValue()));
		tmpType.setMaxWaittime((int)Math.round(((Number)maxWait_.getValue()).intValue()));
		tmpType.setMinBrakingRate((int)Math.round(((Number)minBraking_.getValue()).intValue()));
		tmpType.setMaxBrakingRate((int)Math.round(((Number)maxBraking_.getValue()).intValue()));
		tmpType.setMinAccelerationRate((int)Math.round(((Number)minAcceleration_.getValue()).intValue()));
		tmpType.setMaxAccelerationRate((int)Math.round(((Number)maxAcceleration_.getValue()).intValue()));
		tmpType.setMinTimeDistance((int)Math.round(((Number)minTimeDistance_.getValue()).intValue()));
		tmpType.setMaxTimeDistance((int)Math.round(((Number)maxTimeDistance_.getValue()).intValue()));
		tmpType.setMinPoliteness((int)Math.round(((Number)minPoliteness_.getValue()).intValue()));
		tmpType.setMaxPoliteness((int)Math.round(((Number)maxPoliteness_.getValue()).intValue()));
		tmpType.setWifi(wifi_.isSelected());
		tmpType.setEmergencyVehicle(emergencyVehicle_.isSelected());
		tmpType.setColor(colorPreview_.getBackground().getRGB());
		tmpType.setDeviationFromSpeedLimit_(((int)Math.round(((Number)deviationFromSpeedLimit_.getValue()).intValue() * (100000.0/3600))));
		tmpType.setVehiclesDeviatingMaxSpeed_(((int)Math.round(((Number)vehiclesDeviatingMaxSpeed_.getValue()).intValue())));
	}
	
	/**
	 * Save gui fields values to VehicleType object
	 */
	public void saveQuestions(){
		// save current type
		if(chooseVehicleType_.getSelectedItem() != null) saveType((VehicleType) chooseVehicleType_.getSelectedItem());
		
		//check if file has been saved
		VehicleTypeXML xml = new VehicleTypeXML(filePath_.getValue().toString());
		
		ArrayList<VehicleType> tmpList = new ArrayList<VehicleType>();
		for(int i = 0; i < chooseVehicleType_.getItemCount(); i ++) tmpList.add((VehicleType) chooseVehicleType_.getItemAt(i));		
		
		if(!compareTypeList(xml.getVehicleTypes(), tmpList)) if(JOptionPane.showConfirmDialog(null, Messages.getString("VehicleTypeDialog.saveFile"), "", JOptionPane.YES_NO_OPTION) == 0){
			actionPerformed(new ActionEvent(chooseVehicleType_,0,"saveFile"));
			
			//if the vehicleTypes.xml has been edited refresh the two vehicle Panels
			if((filePath_.getValue().toString().equals("vehicleTypes.xml") || filePath_.getValue().toString().equals(System.getProperty("user.dir") + "/vehicleTypes.xml"))){
				VanetSimStart.getMainControlPanel().getEditPanel().getEditOneVehiclePanel().refreshVehicleTypes();
				VanetSimStart.getMainControlPanel().getEditPanel().getEditVehiclePanel().refreshVehicleTypes();
			}
		}
	}
	
	/**
	 * Remove JCombobox item if name is empty
	 */
	public void removeEmptyTypes(){
		for(int i = 0;i < chooseVehicleType_.getItemCount(); i++){
			if(((VehicleType) chooseVehicleType_.getItemAt(i)).getName().equals("")) chooseVehicleType_.removeItemAt(i);
		}
	}
	
	/**
	 * Methode is evoked when closing JDialog
	 */
	public void closeDialog(){
		//check if the file has to be saved
		saveQuestions();
		
		//close JDialog
		this.dispose();
	}	
	
	/** Mouse listener to open a JColorChooser when colorPreview_ Panel is clicked*/
	public void mouseClicked(MouseEvent e) {
		Color color = JColorChooser.showDialog(this, Messages.getString("VehicleTypeDialog.color"), colorPreview_.getBackground());
		
		if(color == null)colorPreview_.setBackground(Color.black);
		else colorPreview_.setBackground(color);
	}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	public void windowActivated(WindowEvent e) {}

}