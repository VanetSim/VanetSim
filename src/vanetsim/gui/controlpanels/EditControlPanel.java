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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.CyclicBarrier;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;

import vanetsim.ErrorLog;
import vanetsim.VanetSimStart;
import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.map.Node;
import vanetsim.map.Region;
import vanetsim.map.OSM.OSMLoader;
import vanetsim.scenario.Scenario;
import vanetsim.scenario.Vehicle;

/**
 * This class creates all control elements used in the edit tab.
 */
public final class EditControlPanel extends JPanel implements ActionListener {

	/** The necessary constant for serializing. */
	private static final long serialVersionUID = -7019659218394560856L;
	
	/** The button to enable edit mode. */
	private final JRadioButton enableEdit_;
	
	/** The button to disable edit mode. */
	private final JRadioButton disableEdit_;
	
	/** A <code>JComboBox</code> to switch between the different editing tasks. */
	private final JComboBox<String> editChoice_;
	
	/** The panel containing all controls (only visible if in edit mode). */
	private final JPanel editPanel_;
	
	/** A <code>JPanel</code> with <code>CardLayout</code> to switch between editing streets, vehicles and events. */
	private final JPanel editCardPanel_;
	
	/** The tabbed Panel for the edit vehicle tabs */
	 private final JTabbedPane tabbedPane_;
	 
	/** The tabbed Panel for the privacy tabs */
	 private final JTabbedPane privacyTabbedPane_;
	 
	/** The control panel to edit streets. */
	private final EditStreetControlPanel editStreetPanel_ = new EditStreetControlPanel();
	
	/** The control panel to edit vehicles. */
	private final EditVehicleControlPanel editVehiclePanel_ = new EditVehicleControlPanel();
	
	/** The control panel to create,edit or delete one vehicle. */
	private final EditOneVehicleControlPanel editOneVehiclePanel_ = new EditOneVehicleControlPanel();
	
	/** The control panel to edit mix zones. */
	private final MixZonePanel editMixZonePanel_ = new MixZonePanel();
	
	/** The control panel to edit silent periods. */
	private final SilentPeriodPanel editSilentPeriodPanel_ = new SilentPeriodPanel();
	
	/** The control panel to edit slow model. */
	private final SlowPanel editSlowPanel_ = new SlowPanel();
	
	/** The control panel to edit RSUs. */
	private final RSUPanel editRSUPanel_ = new RSUPanel();
	
	/** The control panel to edit Attacker Settings. */
	private final AttackerPanel editAttackerPanel_ = new AttackerPanel();
	
	/** The control panel to edit events. */
	private final EditEventControlPanel editEventPanel_ = new EditEventControlPanel();

	/** The control panel to edit eventspots. */
	private final EditEventSpotsControlPanel editEventSpotsPanel_ = new EditEventSpotsControlPanel();
	
	/** The control panel to edit events. */
	private final EditSettingsControlPanel editSettingsPanel_ = new EditSettingsControlPanel();

	/** The control panel to create,edit or delete traffic lights. */
	private final EditTrafficLightsControlPanel editTrafficLightsPanel_ = new EditTrafficLightsControlPanel();
	
	/** The control panel to edit log configuration. */
	private final EditLogControlPanel editLogControlPanel_ = new EditLogControlPanel();
	
	/** The control panel to edit the ids configuration. */
	private final EditIDSControlPanel editIDSControlPanel_ = new EditIDSControlPanel();
	
	/** The control panel to edit the traffic model. */
	private final EditTrafficModelControlPanel editTrafficModelControlPanel_ = new EditTrafficModelControlPanel();
	
	/** The control panel to edit the data analysis panel. */
	private final EditDataAnalysisControlPanel editDataAnalysisControlPanel_ = new EditDataAnalysisControlPanel();
	
	/** The control panel to edit the traffic model. */
	private final EditPresentationModeControlPanel editPresentationModeControlPanel_ = new EditPresentationModeControlPanel();
	
	/** If edit mode is currently enabled or not. */
	private boolean editMode_ = true;
	
	/** Tabbed pane for events. */
	private final JTabbedPane tabbedPaneEvents_;
	
	/**
	 * Constructor for this ControlPanel.
	 */
	public EditControlPanel(){
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 0.5;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.insets = new Insets(5,5,5,5);
		
		//the save and load buttons
		add(ButtonCreator.getJButton("newmap.png", "newmap", Messages.getString("EditControlPanel.newMap"), this) , c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		c.gridx = 1;
		add(ButtonCreator.getJButton("newscenario.png", "newscenario", Messages.getString("EditControlPanel.newScenario"), this) , c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		c.gridx = 0;
		++c.gridy;
		add(ButtonCreator.getJButton("savemap.png", "savemap", Messages.getString("EditControlPanel.saveMap"), this), c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		c.gridx = 1;
		add(ButtonCreator.getJButton("savescenario.png", "savescenario", Messages.getString("EditControlPanel.saveScenario"), this), c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		c.gridx = 0;
		++c.gridy;		
		add(ButtonCreator.getJButton("importOSM.png", "importOSM", Messages.getString("EditControlPanel.importOSM"), this) , c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		c.gridx = 1;
		add(ButtonCreator.getJButton("scenariocreatoricon2.png", "openScenarioCreator", Messages.getString("EditControlPanel.openScenarioCreator"), this), c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		c.gridx = 0;
		++c.gridy;

		// Radio buttons to enable/disable editing
		JLabel jLabel1 = new JLabel("<html><b>" + Messages.getString("EditControlPanel.editMode") +"</b></html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		c.gridwidth = 2;
		++c.gridy;
		add(jLabel1,c);
		c.gridwidth = 1;
		ButtonGroup group = new ButtonGroup();
		enableEdit_ = new JRadioButton(Messages.getString("EditControlPanel.enable")); //$NON-NLS-1$
		enableEdit_.setActionCommand("enableEdit"); //$NON-NLS-1$
		enableEdit_.setSelected(true);
		enableEdit_.addActionListener(this);
		group.add(enableEdit_);
		++c.gridy;
		add(enableEdit_,c);
		disableEdit_ = new JRadioButton(Messages.getString("EditControlPanel.disable")); //$NON-NLS-1$
		disableEdit_.setActionCommand("disableEdit"); //$NON-NLS-1$
		disableEdit_.addActionListener(this);
		group.add(disableEdit_);
		c.gridx = 1;
		add(disableEdit_,c);
		c.gridx = 0;
		
		// A Panel containing all edit controls. The controls for editing streets, vehicles and events are outsourced into separate classes
		editPanel_ = new JPanel();
		editPanel_.setLayout(new BorderLayout(0,5));
		String[] choices = { Messages.getString("EditControlPanel.settings"),Messages.getString("EditControlPanel.trafficModel"),Messages.getString("EditControlPanel.dataAnalysis"),Messages.getString("EditControlPanel.presentationMode"), Messages.getString("EditControlPanel.street"), Messages.getString("EditControlPanel.trafficLights"), Messages.getString("EditControlPanel.vehicles"),  Messages.getString("EditControlPanel.privacy"), Messages.getString("EditControlPanel.rsus"), Messages.getString("EditControlPanel.attackers"), Messages.getString("EditControlPanel.event"), Messages.getString("EditControlPanel.ids"), Messages.getString("EditControlPanel.logs")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		editChoice_ = new JComboBox<String>(choices);
		editChoice_.setSelectedIndex(0);
		editChoice_.setMaximumRowCount(100);
		editChoice_.addActionListener(this);
		editPanel_.add(editChoice_, BorderLayout.PAGE_START);
		editCardPanel_ = new JPanel(new CardLayout());
		editCardPanel_.setOpaque(false);
		editCardPanel_.add(editSettingsPanel_, "settings"); //$NON-NLS-1$
		editSettingsPanel_.setOpaque(false);
		editCardPanel_.add(editTrafficModelControlPanel_, "trafficmodel"); //$NON-NLS-1$
		editTrafficModelControlPanel_.setOpaque(false);
		editCardPanel_.add(editDataAnalysisControlPanel_, "dataanalysis"); //$NON-NLS-1$
		editDataAnalysisControlPanel_.setOpaque(false);
		editCardPanel_.add(editPresentationModeControlPanel_, "presentationmode"); //$NON-NLS-1$
		editPresentationModeControlPanel_.setOpaque(false);
		editCardPanel_.add(editStreetPanel_, "street"); //$NON-NLS-1$
		editStreetPanel_.setOpaque(false);
		editCardPanel_.add(editTrafficLightsPanel_, "trafficLights"); //$NON-NLS-1$
		editTrafficLightsPanel_.setOpaque(false);
		
		//tabbed pane for events
		tabbedPaneEvents_ = new JTabbedPane();
		tabbedPaneEvents_.setOpaque(false);
		editEventPanel_.setOpaque(false);
		editEventSpotsPanel_.setOpaque(false);
		tabbedPaneEvents_.add(Messages.getString("EditEventControlPanel.events"), editEventPanel_);
		tabbedPaneEvents_.add(Messages.getString("EditEventControlPanel.eventSpots"), editEventSpotsPanel_);
		editCardPanel_.add(tabbedPaneEvents_, "event"); //$NON-NLS-1$
		
		
		// A tabbed panel for privacy functions
		privacyTabbedPane_ = new JTabbedPane();
		privacyTabbedPane_.setOpaque(false);
		editMixZonePanel_.setOpaque(false);
		editSilentPeriodPanel_.setOpaque(false);
		editSlowPanel_.setOpaque(false);
		privacyTabbedPane_.add(Messages.getString("EditControlPanel.mixZones"), editMixZonePanel_);
		privacyTabbedPane_.add(Messages.getString("EditControlPanel.silentPeriods"), editSilentPeriodPanel_);
		privacyTabbedPane_.add(Messages.getString("EditControlPanel.slow"), editSlowPanel_);
		editCardPanel_.add(privacyTabbedPane_, "privacy"); //$NON-NLS-1$
		
		editCardPanel_.add(editRSUPanel_, "rsus"); //$NON-NLS-1$
		editRSUPanel_.setOpaque(false);
		// A tabbed panel to expand the vehicle functions
		
	//	UIManager.put("TabbedPane.contentOpaque", false);
		tabbedPane_ = new JTabbedPane();
		tabbedPane_.setOpaque(false);
		editVehiclePanel_.setOpaque(false);
		editOneVehiclePanel_.setOpaque(false);
		tabbedPane_.add(Messages.getString("EditVehiclesControlPanel.vehicle1"), editVehiclePanel_);
		tabbedPane_.add(Messages.getString("EditVehiclesControlPanel.vehicle2"), editOneVehiclePanel_);
		editCardPanel_.add(editAttackerPanel_, "attackers"); //$NON-NLS-1$
		editAttackerPanel_.setOpaque(false);
		editCardPanel_.add(tabbedPane_, "vehicles"); //$NON-NLS-1$
		editCardPanel_.add(editIDSControlPanel_, "ids");
		editIDSControlPanel_.setOpaque(false);
		editCardPanel_.add(editLogControlPanel_, "logs"); //$NON-NLS-1$
		editLogControlPanel_.setOpaque(false);
		editPanel_.add(editCardPanel_, BorderLayout.PAGE_END);
	
		editPanel_.setOpaque(false);
		
		c.gridwidth = 2;
		++c.gridy;		
		add(editPanel_,c);
		
		//to consume the rest of the space
		c.weighty = 1.0;
		++c.gridy;
		JPanel pane = new JPanel();
		pane.setOpaque(false);
		add(pane, c);

	}
	
	/**
	 * Receives a mouse event and forwards it to the correct control panel.
	 * 
	 * @param x	the x coordinate
	 * @param y	the y coordinate
	 */
	public void receiveMouseEvent(int x, int y){
		String item = (String)editChoice_.getSelectedItem();
		if(item.equals(Messages.getString("EditControlPanel.street"))){ //$NON-NLS-1$
			editStreetPanel_.receiveMouseEvent(x, y);
		} else if(item.equals(Messages.getString("EditControlPanel.vehicles")) && tabbedPane_.getTitleAt(tabbedPane_.getSelectedIndex()).equals(Messages.getString("EditVehiclesControlPanel.vehicle2"))){ //$NON-NLS-1$
			editOneVehiclePanel_.receiveMouseEvent(x, y);
		} else if(item.equals(Messages.getString("EditControlPanel.privacy"))){ //$NON-NLS-1$
			editMixZonePanel_.receiveMouseEvent(x, y);
		} else if(item.equals(Messages.getString("EditControlPanel.rsus"))){ //$NON-NLS-1$
			editRSUPanel_.receiveMouseEvent(x, y);
		} else if(item.equals(Messages.getString("EditControlPanel.attackers"))){ //$NON-NLS-1$
			editAttackerPanel_.receiveMouseEvent(x, y);
		} else if(item.equals(Messages.getString("EditControlPanel.trafficLights"))){ //$NON-NLS-1$
			editTrafficLightsPanel_.receiveMouseEvent(x, y);
		} else if(item.equals(Messages.getString("EditControlPanel.event")) && tabbedPaneEvents_.getTitleAt(tabbedPaneEvents_.getSelectedIndex()).equals(Messages.getString("EditEventControlPanel.events"))){ //$NON-NLS-1$
			editEventPanel_.receiveMouseEvent(x, y);
		} else if(item.equals(Messages.getString("EditControlPanel.event")) && tabbedPaneEvents_.getTitleAt(tabbedPaneEvents_.getSelectedIndex()).equals(Messages.getString("EditEventControlPanel.eventSpots"))){ //$NON-NLS-1$
			editEventSpotsPanel_.receiveMouseEvent(x, y);
		}
	}
	
	/**
	 * An implemented <code>ActionListener</code> which performs all needed actions when a <code>JButton</code>
	 * is clicked.
	 * 
	 * @param e	an <code>ActionEvent</code>
	 */		
	public void actionPerformed(ActionEvent e){
		
		String command = e.getActionCommand();
		if ("savemap".equals(command)){ //$NON-NLS-1$
			VanetSimStart.getMainControlPanel().changeFileChooser(false, true, false);
			final JFileChooser filechooser = VanetSimStart.getMainControlPanel().getFileChooser();
			int returnVal = filechooser.showSaveDialog(VanetSimStart.getMainFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				Runnable job = new Runnable() {
					public void run() {
						File file = filechooser.getSelectedFile();
						if(filechooser.getAcceptAllFileFilter() != filechooser.getFileFilter() && !file.getName().toLowerCase().endsWith(".xml")) file = new File(file.getAbsolutePath() + ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
						Map.getInstance().save(file, false);
					}
				};
				new Thread(job).start();
			}
		} else if ("savescenario".equals(command)){ //$NON-NLS-1$
			VanetSimStart.getMainControlPanel().changeFileChooser(false, true, false);
			final JFileChooser filechooser = VanetSimStart.getMainControlPanel().getFileChooser();
			int returnVal = filechooser.showSaveDialog(VanetSimStart.getMainFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				Runnable job = new Runnable() {
					public void run() {
						File file = filechooser.getSelectedFile();
						if(filechooser.getAcceptAllFileFilter() != filechooser.getFileFilter() && !file.getName().toLowerCase().endsWith(".xml")) file = new File(file.getAbsolutePath() + ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
						Scenario.getInstance().save(file, false);
					}
				};
				new Thread(job).start();
			}
		} else if ("importOSM".equals(command)) { //$NON-NLS-1$
			VanetSimStart.getMainControlPanel().changeFileChooser(true, true, true);
			int returnVal = VanetSimStart.getMainControlPanel().getFileChooser().showOpenDialog(VanetSimStart.getMainFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION){   
				Runnable job = new Runnable() {
					public void run() {
						OSMLoader.getInstance().loadOSM(VanetSimStart.getMainControlPanel().getFileChooser().getSelectedFile());
					}
				};
				new Thread(job).start();
			}
		} else if ("newmap".equals(command)){ //$NON-NLS-1$
			CyclicBarrier barrier = new CyclicBarrier(2);
			new MapSizeDialog(100000, 100000, 50000, 50000, barrier);
			try {
				barrier.await();
			} catch (Exception e2) {}
			enableEdit_.setSelected(false);
			disableEdit_.setSelected(true);
			Map.getInstance().signalMapLoaded();
		} else if ("newscenario".equals(command)){ //$NON-NLS-1$
			Scenario.getInstance().initNewScenario();
			Scenario.getInstance().setReadyState(true);
			VanetSimStart.getMainControlPanel().getEditPanel().getEditEventPanel().updateList();
			Renderer.getInstance().ReRender(false, false);
		} else if ("enableEdit".equals(command)){ //$NON-NLS-1$
			if(Renderer.getInstance().getTimePassed() > 0){
				enableEdit_.setSelected(false);
				disableEdit_.setSelected(true);
				Renderer.getInstance().setAutoAddMixZones(editMixZonePanel_.getAutoAddMixZones().isSelected());
				ErrorLog.log(Messages.getString("EditControlPanel.editingOnlyOnCleanMap"), 6, this.getName(), "enableEdit", null); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				editMode_ = true;
				if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.street"))){ //$NON-NLS-1$
					Renderer.getInstance().setHighlightNodes(true);
					Renderer.getInstance().ReRender(true, false);
				} else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.event"))){ //$NON-NLS-1$
					Renderer.getInstance().setShowAllBlockings(true);
					Renderer.getInstance().ReRender(true, false);
				} else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.trafficmodel"))){ //$NON-NLS-1$
					Renderer.getInstance().ReRender(true, false);
				} else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.dataAnalysis"))){ //$NON-NLS-1$
					Renderer.getInstance().ReRender(true, false);
				} else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.presentationMode"))){ //$NON-NLS-1$
					Renderer.getInstance().ReRender(true, false);
				} else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.privacy"))){ //$NON-NLS-1$
					Renderer.getInstance().setShowMixZones(true);
					Renderer.getInstance().setHighlightNodes(true);
					Renderer.getInstance().ReRender(true, false);
					editSilentPeriodPanel_.loadAttributes();
					editSlowPanel_.loadAttributes();
				} else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.rsus"))){ //$NON-NLS-1$
					Renderer.getInstance().setShowRSUs(true);
					Renderer.getInstance().setHighlightCommunication(true);
					Renderer.getInstance().ReRender(true, false);
				} else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.attackers"))){ //$NON-NLS-1$
					Renderer.getInstance().setShowVehicles(true);
					Renderer.getInstance().setShowAttackers(true);
					Renderer.getInstance().setShowMixZones(true);
					Renderer.getInstance().setHighlightCommunication(true);
					Renderer.getInstance().ReRender(true, false);
					editLogControlPanel_.refreshGUI();
				} else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.vehicles"))){ //$NON-NLS-1$
					Renderer.getInstance().setShowVehicles(true);
					Renderer.getInstance().ReRender(true, false);
				} else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.trafficLights"))){ //$NON-NLS-1$
					Renderer.getInstance().setHighlightNodes(true);
					Renderer.getInstance().ReRender(false, false);
				} else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.ids"))){ //$NON-NLS-1$
					Renderer.getInstance().ReRender(false, false);
				}else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.logs"))){ //$NON-NLS-1$
				
				Renderer.getInstance().ReRender(false, false);
				}
				editPanel_.setVisible(true);
			}
		} else if ("disableEdit".equals(command)){ //$NON-NLS-1$
			editMode_ = false;
			editPanel_.setVisible(false);
			Renderer.getInstance().setHighlightNodes(false);
			Renderer.getInstance().setShowAllBlockings(false);
			Renderer.getInstance().setShowVehicles(false);
			Renderer.getInstance().setShowMixZones(false);
			Renderer.getInstance().setShowRSUs(false);
			Renderer.getInstance().setShowAttackers(false);
			Renderer.getInstance().setHighlightCommunication(false);
			Renderer.getInstance().setMarkedVehicle(null);
			Renderer.getInstance().setMarkedJunction_(null);
			Renderer.getInstance().ReRender(true, false);
			editMixZonePanel_.updateMixRadius();
			setMaxMixZoneRadius();
			editSilentPeriodPanel_.saveAttributes();
			editIDSControlPanel_.saveAttributes();
			editSlowPanel_.saveAttributes();
		} else if ("comboBoxChanged".equals(command)){ //$NON-NLS-1$
	        String item = (String)editChoice_.getSelectedItem();
	        CardLayout cl = (CardLayout)(editCardPanel_.getLayout());
			Renderer.getInstance().setHighlightNodes(false);
			Renderer.getInstance().setShowAllBlockings(false);
			Renderer.getInstance().setShowVehicles(false);
			Renderer.getInstance().setShowMixZones(false);
			Renderer.getInstance().setShowRSUs(false);
			Renderer.getInstance().setHighlightCommunication(false);
			Renderer.getInstance().setShowAttackers(false);
			Renderer.getInstance().setMarkedJunction_(null);
			Renderer.getInstance().setMarkedVehicle(null);
			Renderer.getInstance().ReRender(true, false);
	        if(Messages.getString("EditControlPanel.street").equals(item)){	//$NON-NLS-1$	        	
				cl.show(editCardPanel_, "street"); //$NON-NLS-1$
				Renderer.getInstance().setHighlightNodes(true);
				Renderer.getInstance().setShowAllBlockings(false);
				Renderer.getInstance().ReRender(true, false);
	        } else if(Messages.getString("EditControlPanel.vehicles").equals(item)){	//$NON-NLS-1$
				cl.show(editCardPanel_, "vehicles"); //$NON-NLS-1$
				Renderer.getInstance().setHighlightNodes(false);
				Renderer.getInstance().setShowAllBlockings(false);
				Renderer.getInstance().setShowVehicles(true);
				Renderer.getInstance().ReRender(true, false);
				
				//reset the text of the add vehicle note
				editOneVehiclePanel_.getAddNote().setForeground(Color.black);
				editOneVehiclePanel_.getAddNote().setText(Messages.getString("EditOneVehicleControlPanel.noteAdd"));
				stateChanged(null);
	        } else if(Messages.getString("EditControlPanel.privacy").equals(item)){	//$NON-NLS-1$
	        	cl.show(editCardPanel_, "privacy"); //$NON-NLS-1$
	        	Renderer.getInstance().setHighlightNodes(true);
	        	Renderer.getInstance().setShowAllBlockings(false);
	        	Renderer.getInstance().setShowMixZones(true);
	        	Renderer.getInstance().ReRender(true, false);
	        	editSilentPeriodPanel_.loadAttributes();
	        	editSlowPanel_.loadAttributes();
	        } else if(Messages.getString("EditControlPanel.trafficModel").equals(item)){	//$NON-NLS-1$
	        	cl.show(editCardPanel_, "trafficmodel"); //$NON-NLS-1$
	        	Renderer.getInstance().ReRender(true, false);
	        } else if(Messages.getString("EditControlPanel.dataAnalysis").equals(item)){	//$NON-NLS-1$
	        	cl.show(editCardPanel_, "dataanalysis"); //$NON-NLS-1$
	        	Renderer.getInstance().ReRender(true, false);
	        } else if(Messages.getString("EditControlPanel.presentationMode").equals(item)){	//$NON-NLS-1$
	        	cl.show(editCardPanel_, "presentationmode"); //$NON-NLS-1$
	        	Renderer.getInstance().ReRender(true, false);
	        } else if(Messages.getString("EditControlPanel.rsus").equals(item)){	//$NON-NLS-1$
	        	cl.show(editCardPanel_, "rsus"); //$NON-NLS-1$
	        	Renderer.getInstance().setShowRSUs(true);
	        	Renderer.getInstance().setHighlightCommunication(true);
	        	Renderer.getInstance().ReRender(true, false);
	        } else if(Messages.getString("EditControlPanel.attackers").equals(item)){	//$NON-NLS-1$
	        	cl.show(editCardPanel_, "attackers"); //$NON-NLS-1$
				Renderer.getInstance().setShowMixZones(true);
				Renderer.getInstance().setShowVehicles(true);
				Renderer.getInstance().setShowAttackers(true);
				Renderer.getInstance().setHighlightCommunication(true);
	        	Renderer.getInstance().ReRender(true, false);
	        } else if(Messages.getString("EditControlPanel.event").equals(item)){	//$NON-NLS-1$
	        	cl.show(editCardPanel_, "event"); //$NON-NLS-1$
	        	Renderer.getInstance().setHighlightNodes(false);
	        	Renderer.getInstance().setShowAllBlockings(true);
	        	Renderer.getInstance().ReRender(true, false);
	        } else if(Messages.getString("EditControlPanel.settings").equals(item)){	//$NON-NLS-1$
	        	cl.show(editCardPanel_, "settings"); //$NON-NLS-1$
	        	Renderer.getInstance().setHighlightNodes(false);
	        	Renderer.getInstance().setShowAllBlockings(false);
	        	Renderer.getInstance().ReRender(true, false);
	        } else if(Messages.getString("EditControlPanel.trafficLights").equals(item)){	//$NON-NLS-1$
	        	cl.show(editCardPanel_, "trafficLights"); //$NON-NLS-1$
	        	Renderer.getInstance().setHighlightNodes(true);
	        	Renderer.getInstance().ReRender(true, false);
	        } else if(Messages.getString("EditControlPanel.logs").equals(item)){	//$NON-NLS-1$
	        	cl.show(editCardPanel_, "logs"); //$NON-NLS-1$
	        	editLogControlPanel_.refreshGUI();
	        	Renderer.getInstance().ReRender(true, false);
	        } else if(Messages.getString("EditControlPanel.ids").equals(item)){	//$NON-NLS-1$
	        	cl.show(editCardPanel_, "ids"); //$NON-NLS-1$
	        	Renderer.getInstance().ReRender(true, false);
	        }
		} 
		else if("openScenarioCreator".equals(command)){
			ResearchSeriesDialog.getInstance().setVisible(true);
		}
	}
	
	/**
	 * Controls the tabbed pane (tabbedPane_) to switch between edit vehicle modes
	 */
	public void stateChanged(ChangeEvent arg0) {
		Renderer.getInstance().setMarkedVehicle(null);
		if(editChoice_.getSelectedItem().toString().equals(Messages.getString("EditControlPanel.vehicles")) && tabbedPane_.getTitleAt(tabbedPane_.getSelectedIndex()).equals(Messages.getString("EditVehiclesControlPanel.vehicle1"))){
		}
		else if(editChoice_.getSelectedItem().toString().equals(Messages.getString("EditControlPanel.vehicles")) && tabbedPane_.getTitleAt(tabbedPane_.getSelectedIndex()).equals(Messages.getString("EditVehiclesControlPanel.vehicle2"))){
			Renderer.getInstance().setMarkedVehicle(null);
			Renderer.getInstance().setShowVehicles(true);
		}
		
		Renderer.getInstance().ReRender(false, false);
	}
	
	public void setEditMode(boolean state){
		if(state) enableEdit_.setSelected(true);
		else disableEdit_.setSelected(true);
		editPanel_.setVisible(state);
		editMode_ = state;
	}
	
	/**
	 * Gets the maximal Mix-Zone radius used in the actual scenario and sets the variable in Vehicle.java
	 */
	public void setMaxMixZoneRadius(){
		Region[][] tmpRegions = Map.getInstance().getRegions();
		
		int regionCountX = Map.getInstance().getRegionCountX();
		int regionCountY = Map.getInstance().getRegionCountY();
		int maxMixRadius = 0;
		
		for(int i = 0; i <= regionCountX-1;i++){
			for(int j = 0; j <= regionCountY-1;j++){
				Region tmpRegion = tmpRegions[i][j];
				
				Node[] mixZones = tmpRegion.getMixZoneNodes();
				
				for(int k = 0;k < mixZones.length; k++){
					if(maxMixRadius < mixZones[k].getMixZoneRadius()) maxMixRadius = mixZones[k].getMixZoneRadius();
				}
			}
		}
		
		Vehicle.setMaxMixZoneRadius(maxMixRadius);
	}
	
	/**
	 * Gets the current edit mode.
	 * 
	 * @return <code>true</code> if editing is currently enabled, <code>false</code> if it's disabled
	 */
	public boolean getEditMode(){
		return editMode_;
	}
	
	/**
	 * Gets the control panel to edit streets.
	 * 
	 * @return the control panel
	 */
	public EditStreetControlPanel getEditStreetPanel(){
		return editStreetPanel_;
	}
	
	/**
	 * Gets the control panel to edit vehicles.
	 * 
	 * @return the control panel
	 */
	public EditVehicleControlPanel getEditVehiclePanel(){
		return editVehiclePanel_;
	}
	
	/**
	 * Gets the control panel to edit events.
	 * 
	 * @return the control panel
	 */
	public EditEventControlPanel getEditEventPanel(){
		return editEventPanel_;
	}
	
	/**
	 * Gets the control panel to edit settings.
	 * 
	 * @return the control panel
	 */
	public EditSettingsControlPanel getEditSettingsPanel(){
		return editSettingsPanel_;
	}

	/**
	 * Gets the tabbed panel for vehicle editing
	 * 
	 * @return the tabbed panel
	 */
	public JTabbedPane getTabbedPane() {
		return tabbedPane_;
	}

	/**
	 * Gets the editOneVehiclePanel to edit one vehicle.
	 * 
	 * @return the editOneVehiclePanel
	 */
	public EditOneVehicleControlPanel getEditOneVehiclePanel() {
		return editOneVehiclePanel_;
	}

	public MixZonePanel getEditMixZonePanel_() {
		return editMixZonePanel_;
	}

	public AttackerPanel getEditAttackerPanel_() {
		return editAttackerPanel_;
	}

	public EditLogControlPanel getEditLogControlPanel_() {
		return editLogControlPanel_;
	}

	/**
	 * @return the editIDSControlPanel_
	 */
	public EditIDSControlPanel getEditIDSControlPanel_() {
		return editIDSControlPanel_;
	}

	/**
	 * @return the editEventSpotsPanel_
	 */
	public EditEventSpotsControlPanel getEditEventSpotsPanel_() {
		return editEventSpotsPanel_;
	}

	public JRadioButton getEnableEdit_() {
		return enableEdit_;
	}

	public JComboBox<String> getEditChoice_() {
		return editChoice_;
	}
}