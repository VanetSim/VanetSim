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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import vanetsim.ErrorLog;
import vanetsim.VanetSimStart;
import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.gui.helpers.StreetsJColorChooserPanel;
import vanetsim.gui.helpers.TextAreaLabel;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.map.MapHelper;
import vanetsim.map.Node;
import vanetsim.map.Street;

/**
 * This class represents the control panel for adding, deleting and editing streets. All elements are created here and the actions
 * are performed from here.<br><br>
 * All control elements exist twice - once for editing ("edit" prefix) and once for creating a new street ("new" prefix).
 */
public final class EditStreetControlPanel extends JPanel implements ActionListener{
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = -258179274886213461L;
	
	/** The <code>JPanel</code> with <code>CardLayout</code> which stores the the different views (add, edit and delete). */
	private final JPanel cardPanel_;
	
	/** An array with all string for the presets. Note that all preset arrays must have the same size! */
	private static final String[] PRESET_STRINGS = {Messages.getString("EditStreetControlPanel.motorway"), Messages.getString("EditStreetControlPanel.trunk"), Messages.getString("EditStreetControlPanel.primary"), Messages.getString("EditStreetControlPanel.secondary"), Messages.getString("EditStreetControlPanel.tertiary"), Messages.getString("EditStreetControlPanel.residential")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	
	/** An array with all speeds for the presets in km/h (!). Note that all preset arrays must have the same size! */
	private static final int[] PRESET_SPEEDS = {130, 110, 100, 100, 90, 30};
	
	/** An array with all street colors for the presets. Note that all preset arrays must have the same size! */
	private static final Color[] PRESET_COLORS = {new Color(117,146,185), new Color(116,194,116),new Color(225,98,102), new Color(253,184,100), new Color(252,249,105), Color.WHITE};
	
	/** An array with all lane amounts (per direction!) for the presets. Note that all preset arrays must have the same size! */
	private static final int[] PRESET_LANES = {2, 2, 1, 1, 1, 1};
	
	/** An array with booleans indicating if it is a onewaystreet (<code>true</code>) or not (<code>false</code>) for the presets. 
	 * Note that all preset arrays must have the same size! */
	private static final boolean[] PRESET_ONEWAY = {true, true, false, false, false, false};
	
	/** An array with all street types */ 
	private static final String[] PRESET_TYPES = {"unkown", "motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link", "secondary", "secondary_link", "tertiary", "unclassified", "road", "residential", "living_street", "service", "track", "pedestrian", "raceway", "services", "bus_guideway"}; // all roads attributes from the openStreetMap specification + "unknown": http://wiki.openstreetmap.org/wiki/Map_Features, last checked 30.06.2010 

	/** The field which stores the street name for a new street. */
	private JTextField newName_;
	
	/** The field which stores the street name for an edited street. */
	private JTextField editName_;
	
	/** A button to apply the preset */
	private JButton newApplyPreset_;
	
	/** A button to apply the preset */
	private JButton editApplyPreset_;
	
	/** A combobox with presets for easier street creation. */
	private JComboBox<String> newPresetChoice_;
	
	/** A combobox for setting street types. */
	private JComboBox<String> newStreetTypeChoice_;
	
	/** A combobox with presets for easier street creation. */
	private JComboBox<String> editPresetChoice_;
	
	/** A checkbox for snapping of the startNode. */
	private JCheckBox newSnap1Checkbox_;
	
	/** A checkbox for snapping of the endNode. */
	private JCheckBox newSnap2Checkbox_;
	
	/** A combobox for setting oneway/twoway of new streets. */
	private JComboBox<String> newOnewayChoice_;
	
	/** A combobox for setting oneway/twoway of edited streets. */
	private JComboBox<String> editOnewayChoice_;
	
	/** A combobox for setting street types. */
	private JComboBox<String> editStreetTypeChoice_;	
	
	/** The color for a new street. */
	private Color newColor_ = Color.white;
	
	/** The color for an edited street. */
	private Color editColor_ = Color.white;
	
	/** The button to open the colorchooser while editing an existing street (used to change text color on it). */
	private JButton editColorButton_;
	
	/** The button to open the colorchooser while creating a new street  (used to change text color on it). */
	private JButton newColorButton_;
	
	/** The input field for the number of lanes of  a new street. */
	private JFormattedTextField newLanes_;
	
	/** The input field for the number of lanes of an edited street. */
	private JFormattedTextField editLanes_;
	
	/** The input field for the speed of a new street. */
	private JFormattedTextField newSpeed_;
	
	/** The input field for the speed of an edited street. */
	private JFormattedTextField editSpeed_;
	
	/** The last x coordinate where mouse was pressed. */
	private int lastPressedX_ = -1;
	
	/** The last y coordinate where mouse was pressed. */
	private int lastPressedY_ = -1;
	
	/** To indicate which mode is active. <code>0</code> means add, <code>1</code> means edit and <code>2</code> means delete. */
	private int currentMode_ = 0;
	
	/** The street we're currently working on (for editing streets). */
	private Street editStreet_ = null;	
	
	/** min x to trim */
	private int minX = -1;

	/** min y to trim */
	private int minY = -1;
	
	/** max x to trim */
	private int maxX = -1;
	
	/** max y to trim */
	private int maxY = -1;
	
	/** flag to trim map */
	private boolean selectArea = false;

	/**
	 * Constructor.
	 */
	public EditStreetControlPanel(){
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
		
		// Radio buttons to select what to do
		ButtonGroup group = new ButtonGroup();
		JRadioButton addItem = new JRadioButton(Messages.getString("EditStreetControlPanel.add")); //$NON-NLS-1$
		addItem.setActionCommand("add"); //$NON-NLS-1$
		addItem.addActionListener(this);
		addItem.setSelected(true);
		group.add(addItem);
		++c.gridy;
		add(addItem,c);
		
		JRadioButton editItem = new JRadioButton(Messages.getString("EditStreetControlPanel.edit")); //$NON-NLS-1$
		editItem.setActionCommand("edit"); //$NON-NLS-1$
		editItem.addActionListener(this);
		group.add(editItem);
		++c.gridy;
		add(editItem,c);
		
		JRadioButton deleteItem = new JRadioButton(Messages.getString("EditStreetControlPanel.delete")); //$NON-NLS-1$
		deleteItem.setActionCommand("delete"); //$NON-NLS-1$
		deleteItem.setSelected(true);
		deleteItem.addActionListener(this);
		group.add(deleteItem);
		++c.gridy;
		add(deleteItem,c);
		
		c.gridwidth = 1;
		c.insets = new Insets(5,5,5,5);

		// all controls for creating a new street
		JPanel newPanel = createNewPanel();
		JPanel editPanel = createEditPanel();
		JPanel deletePanel = createDeletePanel();
		
		cardPanel_ = new JPanel(new CardLayout());
		cardPanel_.add(newPanel, "add"); //$NON-NLS-1$
		cardPanel_.add(editPanel, "edit"); //$NON-NLS-1$
		cardPanel_.add(deletePanel, "delete"); //$NON-NLS-1$
		++c.gridy;
		add(cardPanel_,c);
		
		TextAreaLabel jlabel1 = new TextAreaLabel(Messages.getString("EditStreetControlPanel.note")); //$NON-NLS-1$
		++c.gridy;
		add(jlabel1, c);
		
		//to consume the rest of the space
		c.gridwidth = 2;
		c.weighty = 1.0;
		++c.gridy;
		JPanel space = new JPanel();
		space.setOpaque(false);
		add(space, c);
	}
	
	
	/**
	 * Creates the panel with all controls for adding a new street.
	 * 
	 * @return the panel
	 */
	private final JPanel createNewPanel(){
		JPanel newPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5,5,5,5);	

		JPanel tmpPanel = new JPanel();
		newPresetChoice_ = new JComboBox<String>(PRESET_STRINGS);
		newPresetChoice_.setSelectedIndex(0);
		tmpPanel.add(newPresetChoice_);
		newApplyPreset_ = ButtonCreator.getJButton("ok_small.png", "newApplyPreset", Messages.getString("EditStreetControlPanel.applyPreset"), this); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		tmpPanel.add(newApplyPreset_);
		newPanel.add(tmpPanel, c);
		
		c.gridwidth = 1;
		JLabel jLabel1 = new JLabel(Messages.getString("EditStreetControlPanel.streetname")); //$NON-NLS-1$
		++c.gridy;
		newPanel.add(jLabel1,c);
		
		c.gridx = 1;
		newName_ = new JTextField(0);
		newName_.setPreferredSize(new Dimension(60,20));
		newPanel.add(newName_,c);
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditStreetControlPanel.streettype")); //$NON-NLS-1$
		++c.gridy;
		newPanel.add(jLabel1,c);
		
		c.gridx = 1;
		newStreetTypeChoice_ = new JComboBox<String>(PRESET_TYPES);
		newStreetTypeChoice_.setSelectedIndex(0);
		newPanel.add(newStreetTypeChoice_, c);
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditStreetControlPanel.color")); //$NON-NLS-1$
		++c.gridy;
		newPanel.add(jLabel1,c);
		
		newColorButton_ = new JButton(Messages.getString("EditStreetControlPanel.changeColor")); //$NON-NLS-1$
		newColorButton_.setForeground(newColor_);
		newColorButton_.setActionCommand("newColor"); //$NON-NLS-1$
		newColorButton_.addActionListener(this);
        c.gridx = 1;
		newPanel.add(newColorButton_, c);
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditStreetControlPanel.speed")); //$NON-NLS-1$
		++c.gridy;
		newPanel.add(jLabel1,c);
		
		newSpeed_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		newSpeed_.setPreferredSize(new Dimension(60,20));
		newSpeed_.setValue(100);
		c.gridx = 1;
		newPanel.add(newSpeed_,c);
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditStreetControlPanel.lanesPerDirection")); //$NON-NLS-1$
		++c.gridy;
		newPanel.add(jLabel1,c);
		
		newLanes_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		newLanes_.setPreferredSize(new Dimension(60,20));
		newLanes_.setValue(1);
		c.gridx = 1;
		newPanel.add(newLanes_,c);
		
		jLabel1 = new JLabel(Messages.getString("EditStreetControlPanel.directions")); //$NON-NLS-1$
		c.gridx = 0;
		++c.gridy;
		newPanel.add(jLabel1,c);
		
		c.gridx = 1;
		String[] choices = {Messages.getString("EditStreetControlPanel.twoWay"), Messages.getString("EditStreetControlPanel.oneWay"), Messages.getString("EditStreetControlPanel.reverse")};  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		newOnewayChoice_ = new JComboBox<String>(choices);
		newOnewayChoice_.setSelectedIndex(0);
		newPanel.add(newOnewayChoice_, c);
		
		c.gridx = 0;
		newSnap1Checkbox_ = new JCheckBox(Messages.getString("EditStreetControlPanel.snapFirst")); //$NON-NLS-1$
		++c.gridy;
		c.gridwidth = 2;
		newPanel.add(newSnap1Checkbox_,c);
		
		newSnap2Checkbox_ = new JCheckBox(Messages.getString("EditStreetControlPanel.snapSecond")); //$NON-NLS-1$
		++c.gridy;
		newPanel.add(newSnap2Checkbox_,c);
		
		//to consume the rest of the space
		c.gridwidth = 2;
		c.weighty = 1.0;
		++c.gridy;
		newPanel.add(new JPanel(), c);
		return newPanel;
	}
	
	/**
	 * Creates the panel with all controls for editing a street.
	 * 
	 * @return the panel
	 */
	private final JPanel createEditPanel(){
		JPanel editPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5,5,5,5);	
		
		JPanel tmpPanel = new JPanel();
		editPresetChoice_ = new JComboBox<String>(PRESET_STRINGS);
		editPresetChoice_.setSelectedIndex(0);
		tmpPanel.add(editPresetChoice_);
		editApplyPreset_ = ButtonCreator.getJButton("ok_small.png", "editApplyPreset", Messages.getString("EditStreetControlPanel.applyPreset"), this); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		tmpPanel.add(editApplyPreset_);
		editPanel.add(tmpPanel, c);
		
		c.gridwidth = 1;		
		JLabel jLabel1 = new JLabel(Messages.getString("EditStreetControlPanel.streetname")); //$NON-NLS-1$
		++c.gridy;
		editPanel.add(jLabel1,c);
		
		c.gridx = 1;
		editName_ = new JTextField(0);
		editName_.setPreferredSize(new Dimension(60,20));
		editPanel.add(editName_,c);		
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditStreetControlPanel.streettype")); //$NON-NLS-1$
		++c.gridy;
		editPanel.add(jLabel1,c);
		
		c.gridx = 1;
		editStreetTypeChoice_ = new JComboBox<String>(PRESET_TYPES);
		editStreetTypeChoice_.setSelectedIndex(0);
		editPanel.add(editStreetTypeChoice_, c);
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditStreetControlPanel.color")); //$NON-NLS-1$
		++c.gridy;
		editPanel.add(jLabel1,c);
		
		editColorButton_ = new JButton(Messages.getString("EditStreetControlPanel.changeColor")); //$NON-NLS-1$
		editColorButton_.setForeground(editColor_);
		editColorButton_.setActionCommand("editColor"); //$NON-NLS-1$
		editColorButton_.addActionListener(this);
        c.gridx = 1;
		editPanel.add(editColorButton_, c);
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditStreetControlPanel.speed")); //$NON-NLS-1$
		++c.gridy;
		editPanel.add(jLabel1,c);
		
		editSpeed_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		editSpeed_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		editPanel.add(editSpeed_,c);
		
		c.gridx = 0;
		jLabel1 = new JLabel(Messages.getString("EditStreetControlPanel.lanesPerDirection")); //$NON-NLS-1$
		++c.gridy;
		editPanel.add(jLabel1,c);
		
		editLanes_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
		editLanes_.setPreferredSize(new Dimension(60,20));
		c.gridx = 1;
		editPanel.add(editLanes_,c);		
		
		jLabel1 = new JLabel(Messages.getString("EditStreetControlPanel.directions")); //$NON-NLS-1$
		c.gridx = 0;
		++c.gridy;
		editPanel.add(jLabel1,c);
		
		c.gridx = 1;
		String[] choices = {Messages.getString("EditStreetControlPanel.twoWay"), Messages.getString("EditStreetControlPanel.oneWay"), Messages.getString("EditStreetControlPanel.reverse")};  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		editOnewayChoice_ = new JComboBox<String>(choices);
		editOnewayChoice_.setSelectedIndex(0);
		editPanel.add(editOnewayChoice_, c);
		
		c.gridx = 0;
		++c.gridy;
		c.gridwidth = 2;
		editPanel.add(ButtonCreator.getJButton("savestreet.png", "save", Messages.getString("EditStreetControlPanel.save"), this), c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		c.gridx = 0;
		++c.gridy;
		c.gridwidth = 2;
		editPanel.add(ButtonCreator.getJButton("autoTrim.png", "autoTrimMap", Messages.getString("EditStreetControlPanel.autoTrimMap"), this), c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		
		c.gridx = 0;
		++c.gridy;
		c.gridwidth = 2;
		editPanel.add(ButtonCreator.getJButton("trim.png", "trimMap", Messages.getString("EditStreetControlPanel.trimMap"), this), c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		//to consume the rest of the space
		c.gridwidth = 2;
		c.weighty = 1.0;
		++c.gridy;
		editPanel.add(new JPanel(), c);
		return editPanel;
	}
	
	/**
	 * Creates just an empty panel (shown when deleting a street).
	 * 
	 * @return the panel
	 */
	private final JPanel createDeletePanel(){
		JPanel deletePanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		//to consume the rest of the space
		c.gridwidth = 1;
		c.weighty = 1.0;
		++c.gridy;
		deletePanel.add(new JPanel(), c);
		return deletePanel;
	}
	
	/**
	 * Receives a mouse event.
	 * 
	 * @param x	the x coordinate (in map scale)
	 * @param y	the y coordinate (in map scale)
	 */
	public void receiveMouseEvent(int x, int y){
		if(currentMode_ == 0){	//add street
			if(lastPressedX_ == -1 && lastPressedY_ == -1){
				lastPressedX_ = x;
				lastPressedY_ = y;
			} else {
				Node StartNode = null, EndNode = null, tmpnode;
				if(newName_.getText().equals("") || newSpeed_.getText().equals("") || newLanes_.getText().equals("")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					ErrorLog.log(Messages.getString("EditStreetControlPanel.allFieldsForAdding"), 7, getClass().getName(), "addStreet", null); //$NON-NLS-1$ //$NON-NLS-2$
				} else{
					if(newSnap1Checkbox_.getSelectedObjects() != null){	//try to snap to an existing node
						tmpnode = MapHelper.findNearestNode(lastPressedX_, lastPressedY_, 2000, new long[1]);
						StartNode = tmpnode;
						if (StartNode == null){
							int answer = JOptionPane.showConfirmDialog(VanetSimStart.getMainFrame(), Messages.getString("EditStreetControlPanel.1stPointSnappingFailedMessage"), Messages.getString("EditStreetControlPanel.snappingFailed") , JOptionPane.YES_NO_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
							if(answer == JOptionPane.YES_OPTION) StartNode = new Node(lastPressedX_, lastPressedY_);
						}
					} else StartNode = new Node(lastPressedX_, lastPressedY_);
					if(newSnap2Checkbox_.getSelectedObjects() != null){	//try to snap to an existing node
						tmpnode = MapHelper.findNearestNode(x, y, 2000, new long[1]);
						EndNode = tmpnode;			
						if (StartNode != null && EndNode == null){
							int answer = JOptionPane.showConfirmDialog(VanetSimStart.getMainFrame(), Messages.getString("EditStreetControlPanel.2ndPointSnappingFailedMessage"), Messages.getString("EditStreetControlPanel.snappingFailed") , JOptionPane.YES_NO_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
							if(answer == JOptionPane.YES_OPTION) EndNode = new Node(x, y);
						}
					} else EndNode = new Node(x, y);
					if(StartNode != null && EndNode != null){
						StartNode = Map.getInstance().addNode(StartNode);
						EndNode = Map.getInstance().addNode(EndNode);
						Map.getInstance().addStreet(new Street(newName_.getText(), StartNode, EndNode, newStreetTypeChoice_.getSelectedItem().toString(), newOnewayChoice_.getSelectedIndex(), ((Number)newLanes_.getValue()).intValue(), newColor_, Map.getInstance().getRegionOfPoint(StartNode.getX(), StartNode.getY()), ((Number)newSpeed_.getValue()).intValue()*100000/3600));
						Renderer.getInstance().ReRender(true, false);
					}
					lastPressedX_ = -1;
					lastPressedY_ = -1;
				}
			}
		} else if(currentMode_ == 1){	//edit street
			//trim map
			if(selectArea){
				if(minX == -1 && minY == -1){
					minX = x;
					minY = y;
				}
				else{
					maxX = x;
					maxY = y;
					selectArea = false;
					Map.getInstance().autoTrimMap(minX,minY,maxX,maxY);
				}
			}
			//edit streets
			else{
				editStreet_ = MapHelper.findNearestStreet(x, y, 20000, new double[2], new int[2]);	
				if(editStreet_ == null){
					editName_.setText(""); //$NON-NLS-1$
					editLanes_.setText(""); //$NON-NLS-1$
					editSpeed_.setText(""); //$NON-NLS-1$
					editStreetTypeChoice_.setSelectedIndex(0);
					editColor_ = null;
					editColorButton_.setForeground(Color.black);
					editOnewayChoice_.setSelectedIndex(0);
				} else {
					editName_.setText(editStreet_.getName());
					editLanes_.setValue(editStreet_.getLanesCount());
					editSpeed_.setValue(Math.round(editStreet_.getSpeed()*3600.0/100000));
					int tmpIndex = 0;
					for(int i = 0; i < PRESET_TYPES.length;i++) if(PRESET_TYPES[i].equals(editStreet_.getStreetType_())) tmpIndex = i;
					editStreetTypeChoice_.setSelectedIndex(tmpIndex);
					editColor_ = editStreet_.getDisplayColor();
					editColorButton_.setForeground(editColor_);
					if(editStreet_.isOneway()) editOnewayChoice_.setSelectedIndex(1);
					else editOnewayChoice_.setSelectedIndex(0);
				}
				Renderer.getInstance().setMarkedStreet(editStreet_);
				Renderer.getInstance().ReRender(false,false);
			}
		} else if(currentMode_ == 2){	//delete street
			Street tmpstreet = MapHelper.findNearestStreet(x, y, 20000, new double[2], new int[2]);
			if(tmpstreet != null){
				tmpstreet.getStartNode().delOutgoingStreet(tmpstreet);
				tmpstreet.getStartNode().delCrossingStreet(tmpstreet);
				if(tmpstreet.getStartNode().getCrossingStreetsCount()==0) Map.getInstance().delNode(tmpstreet.getStartNode());
				tmpstreet.getEndNode().delOutgoingStreet(tmpstreet);
				tmpstreet.getEndNode().delCrossingStreet(tmpstreet);
				if(tmpstreet.getEndNode().getCrossingStreetsCount()==0) Map.getInstance().delNode(tmpstreet.getEndNode());
				Map.getInstance().delStreet(tmpstreet);
				Renderer.getInstance().ReRender(true, false);
			}
		}
	}
	
	/**
	 * An implemented <code>ActionListener</code> which performs all needed actions when a <code>JButton</code>
	 * is clicked.
	 * 
	 * @param e	an <code>ActionEvent</code>
	 */	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if ("add".equals(command)){ //$NON-NLS-1$
			currentMode_ = 0;
			CardLayout cl = (CardLayout)(cardPanel_.getLayout());
			cl.show(cardPanel_, "add"); //$NON-NLS-1$
		} else if ("edit".equals(command)){ //$NON-NLS-1$
			currentMode_ = 1;
			CardLayout cl = (CardLayout)(cardPanel_.getLayout());
			cl.show(cardPanel_, "edit"); //$NON-NLS-1$
		} else if ("delete".equals(command)){ //$NON-NLS-1$
			currentMode_ = 2;
			CardLayout cl = (CardLayout)(cardPanel_.getLayout());
			cl.show(cardPanel_, "delete"); //$NON-NLS-1$
		} else if("save".equals(command)){ //$NON-NLS-1$
			if(editStreet_ != null && !editName_.getText().equals("") && !editSpeed_.getText().equals("") && !editLanes_.getText().equals("")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				editStreet_.setName(editName_.getText());
				try{
					editSpeed_.commitEdit();
					editLanes_.commitEdit();
					editStreet_.setSpeed((int)Math.round(((Number)editSpeed_.getValue()).intValue()*100000.0/3600));
					editStreet_.setStreetType_(editStreetTypeChoice_.getSelectedItem().toString());
					editStreet_.setLanesCount(((Number)editLanes_.getValue()).intValue());
					editStreet_.setStreetType_(editStreetTypeChoice_.getSelectedItem().toString());
					editStreet_.changeOneWay(editOnewayChoice_.getSelectedIndex());
					if(editOnewayChoice_.getSelectedIndex() == 2) editOnewayChoice_.setSelectedIndex(1);
				}catch (Exception e2) {}
				if(editColor_ != null) editStreet_.setDisplayColor(editColor_);
				Renderer.getInstance().ReRender(true, false);
			}
		} else if ("autoTrimMap".equals(command)){ //$NON-NLS-1$						
			int respons = JOptionPane.showOptionDialog(null, Messages.getString("EditStreetControlPanel.WarningMsgBoxAuto"), "Information", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, "");
			if(respons == 0){
				Map.getInstance().autoTrimMap(-1,-1,-1,-1);
			}
		} else if ("trimMap".equals(command)){ //$NON-NLS-1$
			int respons = JOptionPane.showOptionDialog(null, Messages.getString("EditStreetControlPanel.WarningMsgBox"), "Information", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, "");
			if(respons == 0){
				minX = -1;
				minY = -1;
				maxX = -1;
				maxY = -1;
				selectArea = true;
			}
		} else if ("newColor".equals(command)){ //$NON-NLS-1$
			final JColorChooser chooser;
			if(editColor_ != null) chooser = new JColorChooser(newColor_);
			else chooser = new JColorChooser();
			chooser.addChooserPanel(new StreetsJColorChooserPanel());
			ActionListener okActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					newColor_ = chooser.getColor();
					newColorButton_.setForeground(newColor_);
				}
			};
			JDialog dialog = JColorChooser.createDialog(VanetSimStart.getMainFrame(), Messages.getString("EditStreetControlPanel.changeStreetColorTitle"), true, chooser, okActionListener, null); //$NON-NLS-1$
			dialog.setVisible(true);
		} else if ("editColor".equals(command)){ //$NON-NLS-1$
			final JColorChooser chooser;
			if(editColor_ != null) chooser = new JColorChooser(editColor_);
			else chooser = new JColorChooser();
			chooser.addChooserPanel(new StreetsJColorChooserPanel());
			ActionListener okActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					editColor_ = chooser.getColor();
					editColorButton_.setForeground(editColor_);
				}
			};
			JDialog dialog = JColorChooser.createDialog(VanetSimStart.getMainFrame(), Messages.getString("EditStreetControlPanel.changeStreetColorTitle"), true, chooser, okActionListener, null); //$NON-NLS-1$
			dialog.setVisible(true);
		} else if ("newApplyPreset".equals(command)){ //$NON-NLS-1$
			int i = newPresetChoice_.getSelectedIndex();
			newSpeed_.setValue(PRESET_SPEEDS[i]);
			newColor_ = PRESET_COLORS[i];
			newColorButton_.setForeground(newColor_);
			newLanes_.setValue(PRESET_LANES[i]);
			if(newPresetChoice_.getSelectedIndex() == 0) newStreetTypeChoice_.setSelectedItem("motorway");
			else if(newPresetChoice_.getSelectedIndex() == 1) newStreetTypeChoice_.setSelectedItem("trunk");
			else if(newPresetChoice_.getSelectedIndex() == 2) newStreetTypeChoice_.setSelectedItem("primary");
			else if(newPresetChoice_.getSelectedIndex() == 3) newStreetTypeChoice_.setSelectedItem("secondary");
			else if(newPresetChoice_.getSelectedIndex() == 4) newStreetTypeChoice_.setSelectedItem("tertiary");
			else if(newPresetChoice_.getSelectedIndex() == 5) newStreetTypeChoice_.setSelectedItem("unkown");
			boolean oneway = PRESET_ONEWAY[i];			
			if(oneway){
				if(newOnewayChoice_.getSelectedIndex() == 0) newOnewayChoice_.setSelectedIndex(1);
			} else newOnewayChoice_.setSelectedIndex(0);
		} else if ("editApplyPreset".equals(command)){ //$NON-NLS-1$
			int i = editPresetChoice_.getSelectedIndex();
			editSpeed_.setValue(PRESET_SPEEDS[i]);
			editColor_ = PRESET_COLORS[i];
			editColorButton_.setForeground(editColor_);
			editLanes_.setValue(PRESET_LANES[i]);
			if(editPresetChoice_.getSelectedIndex() == 0) editStreetTypeChoice_.setSelectedItem("motorway");
			else if(editPresetChoice_.getSelectedIndex() == 1) editStreetTypeChoice_.setSelectedItem("trunk");
			else if(editPresetChoice_.getSelectedIndex() == 2) editStreetTypeChoice_.setSelectedItem("primary");
			else if(editPresetChoice_.getSelectedIndex() == 3) editStreetTypeChoice_.setSelectedItem("secondary");
			else if(editPresetChoice_.getSelectedIndex() == 4) editStreetTypeChoice_.setSelectedItem("tertiary");
			else if(editPresetChoice_.getSelectedIndex() == 5) editStreetTypeChoice_.setSelectedItem("unkown");
			boolean oneway = PRESET_ONEWAY[i];
			if(oneway){
				if(editOnewayChoice_.getSelectedIndex() == 0) editOnewayChoice_.setSelectedIndex(1);
			} else editOnewayChoice_.setSelectedIndex(0);
		}
	}
}