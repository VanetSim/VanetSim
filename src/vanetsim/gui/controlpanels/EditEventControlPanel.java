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
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import vanetsim.ErrorLog;
import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.gui.helpers.EventJListRenderer;
import vanetsim.gui.helpers.TextAreaLabel;
import vanetsim.localization.Messages;
import vanetsim.scenario.events.Event;
import vanetsim.scenario.events.EventList;
import vanetsim.scenario.events.StartBlocking;
import vanetsim.scenario.events.StopBlocking;


/**
 * The control panel for editing events.
 */
public final class EditEventControlPanel extends JPanel implements ActionListener{
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = -8161612114065521616L;
	
	/** The list with all events. */
	private final JList<Event> list_;
	
	/** The (data) model behind the list. */
	private final DefaultListModel<Event> listModel_ = new DefaultListModel<Event>();
	
	/** A combo box for selecting the event type. */
	private final JComboBox<String> eventTypeChoice_;
	
	/** An input field for the time in milliseconds. */
	private final JFormattedTextField timeTextField_ = new JFormattedTextField(NumberFormat.getIntegerInstance());

	/** An input field for the x coordinate. */
	private final JFormattedTextField xTextField_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
	
	/** An input field for the y coordinate. */
	private final JFormattedTextField yTextField_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
	
	/** A combo box for selecting the direction. */
	private final JComboBox<String> directionChoice_;
	
	/** An input field for the amount of lanes. */
	private final JFormattedTextField lanesTextField_ = new JFormattedTextField(NumberFormat.getIntegerInstance());

	/** CheckBox to activate if it is a fake message */
	private final JCheckBox isFake_;	
	
	/** A combo box for selecting the message type. */
	private final JComboBox<String> penaltyType_;
	
	/**
	 * Constructor.
	 */
	@SuppressWarnings("unchecked")
	public EditEventControlPanel(){
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
		c.insets = new Insets(5,5,5,5);
		
        //Create the list and put it in a scroll pane.
		JLabel jLabel1 = new JLabel("<html><u><b>" + Messages.getString("EditEventControlPanel.eventList") + "</b></u></html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		add(jLabel1,c);
		updateList();
        list_ = new JList<Event>(listModel_);
        list_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list_.setCellRenderer(new EventJListRenderer());
        list_.setSelectedIndex(0);
        list_.setVisibleRowCount(10);
        JScrollPane listScrollPane = new JScrollPane(list_);
        listScrollPane.setPreferredSize(new Dimension(100,200));        
        ++c.gridy;
        c.weighty = 1.0;
        add(listScrollPane, c);
        c.weighty = 0;
        
        //add buttons and input fields
        c.gridwidth = 1;
        c.gridx = 1;
        ++c.gridy;
        add(ButtonCreator.getJButton("delEvent.png", "delEvent", Messages.getString("EditEventControlPanel.deleteEvent"), this),c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        c.gridx = 0;
        ++c.gridy;
        jLabel1 = new JLabel(Messages.getString("EditEventControlPanel.type")); //$NON-NLS-1$
		add(jLabel1,c);
        
		c.gridx = 1;
        String[] choices = {Messages.getString("EditEventControlPanel.startBlocking"), Messages.getString("EditEventControlPanel.stopBlocking")};  //$NON-NLS-1$ //$NON-NLS-2$
		eventTypeChoice_ = new JComboBox<String>(choices);
		eventTypeChoice_.setSelectedIndex(0);
		add(eventTypeChoice_, c);
		
        
        c.gridx = 0;
        ++c.gridy;
        jLabel1 = new JLabel(Messages.getString("EditEventControlPanel.penaltyType")); //$NON-NLS-1$
		add(jLabel1,c);
        
		c.gridx = 1;
        String[] choices3 = {"HUANG_EEBL", "HUANG_PCN", "HUANG_RHCN", "HUANG_RFN", "HUANG_SVA", "HUANG_CCW", "HUANG_CVW", "HUANG_CL", "HUANG_EVA"};  //$NON-NLS-1$
        penaltyType_ = new JComboBox<String>(choices3);
        penaltyType_.setSelectedIndex(0);
		add(penaltyType_, c);
		
		c.gridx = 0;
		++c.gridy;
        jLabel1 = new JLabel(Messages.getString("EditEventControlPanel.time")); //$NON-NLS-1$
		add(jLabel1,c);
		
		c.gridx = 1;
        timeTextField_.setPreferredSize(new Dimension(60,20));
        timeTextField_.setValue(0);
		add(timeTextField_,c);
		
		c.gridx = 0;
		++c.gridy;
        jLabel1 = new JLabel(Messages.getString("EditEventControlPanel.y")); //$NON-NLS-1$
		add(jLabel1,c);
		
		c.gridx = 1;
        xTextField_.setPreferredSize(new Dimension(60,20));
        xTextField_.setValue(0);
		add(xTextField_,c);
		
		c.gridx = 0;
		++c.gridy;
		jLabel1 = new JLabel(Messages.getString("EditEventControlPanel.x")); //$NON-NLS-1$
		add(jLabel1,c);
		
		c.gridx = 1;
        yTextField_.setPreferredSize(new Dimension(60,20));
        yTextField_.setValue(0);
		add(yTextField_,c);
		
		c.gridx = 0;
		++c.gridy;
		jLabel1 = new JLabel(Messages.getString("EditEventControlPanel.direction")); //$NON-NLS-1$
		add(jLabel1,c);
		
		c.gridx = 1;
		String[] choices2 = {Messages.getString("EditEventControlPanel.both"), Messages.getString("EditEventControlPanel.fromStartNode"), Messages.getString("EditEventControlPanel.fromEndNode")};  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		directionChoice_ = new JComboBox<String>(choices2);
		directionChoice_.setSelectedIndex(0);
		add(directionChoice_, c);
		
		c.gridx = 0;
		++c.gridy;
		jLabel1 = new JLabel(Messages.getString("EditEventControlPanel.lanes")); //$NON-NLS-1$
		add(jLabel1,c);
		
		c.gridx = 1;
        lanesTextField_.setPreferredSize(new Dimension(60,20));
        lanesTextField_.setValue(1);
		add(lanesTextField_,c);
		
		++c.gridy;
		c.gridx = 0;
		add(new JLabel(Messages.getString("EditEventControlPanel.isFake")),c);	
		
		isFake_ = new JCheckBox();
		isFake_.setSelected(false);
		isFake_.setActionCommand("isFake"); //$NON-NLS-1$
		c.gridx = 1;
		add(isFake_,c);
		isFake_.addActionListener(this);	
		
		++c.gridy;
		add(ButtonCreator.getJButton("addEvent.png", "addEvent", Messages.getString("EditEventControlPanel.addEvent"), this),c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		TextAreaLabel jlabel1 = new TextAreaLabel(Messages.getString("EditEventControlPanel.note")); //$NON-NLS-1$
		++c.gridy;
		c.gridx = 0;
		c.gridwidth = 2;
		add(jlabel1, c);	
	}
	
	/**
	 * Receives a mouse event.
	 * 
	 * @param x	the x coordinate (in map scale)
	 * @param y	the y coordinate (in map scale)
	 */
	public void receiveMouseEvent(int x, int y){
		xTextField_.setValue(x);
		yTextField_.setValue(y);
	}
	
	/**
	 * Update the event list.
	 */
	public void updateList(){
		Iterator<Event> eventIterator = EventList.getInstance().getIterator();
		listModel_.clear();
		while(eventIterator.hasNext()){
			listModel_.addElement(eventIterator.next());
		
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
		if("addEvent".equals(command)){ //$NON-NLS-1$
			String item = (String)eventTypeChoice_.getSelectedItem();
			try{
				xTextField_.commitEdit();
				yTextField_.commitEdit();
				if(Messages.getString("EditEventControlPanel.stopBlocking").equals(item)) EventList.getInstance().addEvent(new StopBlocking(((Number)timeTextField_.getValue()).intValue(),((Number)xTextField_.getValue()).intValue(),((Number)yTextField_.getValue()).intValue())); //$NON-NLS-1$
				else if(Messages.getString("EditEventControlPanel.startBlocking").equals(item)){ //$NON-NLS-1$
					int direction = 0;
					String item2 = (String)directionChoice_.getSelectedItem();
					if (item2.equals(Messages.getString("EditEventControlPanel.fromStartNode"))) direction = 1; //$NON-NLS-1$
					else if (item2.equals(Messages.getString("EditEventControlPanel.fromEndNode"))) direction = -1; //$NON-NLS-1$
					EventList.getInstance().addEvent(new StartBlocking(((Number)timeTextField_.getValue()).intValue(),((Number)xTextField_.getValue()).intValue(),((Number)yTextField_.getValue()).intValue(), direction, ((Number)lanesTextField_.getValue()).intValue(), isFake_.isSelected(), penaltyType_.getSelectedItem().toString())); //$NON-NLS-1$
				}
				updateList();
				Renderer.getInstance().ReRender(false, false);
			} catch (Exception e2) { ErrorLog.log(Messages.getString("EditEventControlPanel.errorCreatingEvent"), 6, getClass().getName(), "actionPerformed", e2);} //$NON-NLS-1$ //$NON-NLS-2$
		} else if("delEvent".equals(command)){ //$NON-NLS-1$
			if(list_.getSelectedIndex() > -1){
				Event deleteEvent = (Event)list_.getSelectedValue();
				boolean doDelete = true;
				if(deleteEvent instanceof StartBlocking){
					StartBlocking startBlockingEvent = (StartBlocking) deleteEvent;
					if (startBlockingEvent.getStopBlockingEvent() != null){
						ErrorLog.log(Messages.getString("EditEventControlPanel.deletionOfStartBlockingNotPossible"), 6, getClass().getName(), "delEvent", null); //$NON-NLS-1$ //$NON-NLS-2$
						doDelete = false;
					}
				}
				if(doDelete){
					listModel_.remove(list_.getSelectedIndex());
					EventList.getInstance().delEvent(deleteEvent);
					Renderer.getInstance().ReRender(false, false);
				}
			}
		}
	}
}