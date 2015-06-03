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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.AttackLogWriter;
import vanetsim.gui.helpers.EventLogWriter;
import vanetsim.gui.helpers.IDSLogWriter;
import vanetsim.gui.helpers.LocationInformationLogWriter;
import vanetsim.gui.helpers.PrivacyLogWriter;
import vanetsim.gui.helpers.TextAreaLabel;
import vanetsim.localization.Messages;
import vanetsim.scenario.IDSProcessor;
import vanetsim.scenario.KnownPenalties;
import vanetsim.scenario.Vehicle;

/**
 * This class represents the control panel for adding mix zones.
 */
public class EditLogControlPanel extends JPanel implements  FocusListener, ActionListener{

	/** The necessary constant for serializing. */
	private static final long serialVersionUID = -8294786435746799533L;


	/** CheckBox to log attacker data */
	private final JCheckBox logAttackerCheckBox_;	
	
	/** The input field for the log path */
	private final JFormattedTextField logAttackerPath_;

	/** flag to avoid loop when starting file chooser */
	private boolean attackerFlag = true;
	
	/** CheckBox to switch encrypted log on / off. */
	private final JCheckBox encryptedLogging_;	

	/** CheckBox to log privacy data */
	private final JCheckBox logPrivacyCheckBox_;	
	
	/** The input field for the privacy log path */
	private final JFormattedTextField logPrivacyPath_;

	/** flag to avoid loop when starting file chooser */
	private boolean privacyFlag = true;
	
	/** CheckBox to log IDS data */
	private final JCheckBox logIDSCheckBox_;	
	
	/** The input field for the ids log path */
	private final JFormattedTextField logIDSPath_;
	
	/** CheckBox to log Event data */
	private final JCheckBox logEventCheckBox_;	
	
	/** The input field for the event log path */
	private final JFormattedTextField logEventPath_;

	/** flag to avoid loop when starting file chooser */
	private boolean IDSFlag = true;
	
	/** flag to avoid loop when starting file chooser */
	private boolean EventFlag = true;
	
	/** Note to describe add ... */
	TextAreaLabel dummyNote_;
	
	/** FileFilter to choose only ".log" files from FileChooser */
	private FileFilter logFileFilter_;
	
	/**
	 * Constructor, creating GUI items.
	 */
	public EditLogControlPanel() {
		setLayout(new GridBagLayout());
		
		// global layout settings
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		
		c.gridwidth = 1;

		c.insets = new Insets(5,5,5,5);
		
		c.gridx = 0;
		add(new JLabel(Messages.getString("EditLogControlPanel.attackerLogLabel")),c); //$NON-NLS-1$
		
		c.gridx = 1;
		logAttackerCheckBox_ = new JCheckBox();
		logAttackerCheckBox_.setSelected(false);
		logAttackerCheckBox_.setActionCommand("logAttackerData"); //$NON-NLS-1$
		logAttackerCheckBox_.addActionListener(this);
		add(logAttackerCheckBox_,c);	
		
		c.gridwidth = 2;
		++c.gridy;
		c.gridx = 0;	
		add(new JLabel(Messages.getString("EditLogControlPanel.attackerPathLabel")),c); //$NON-NLS-1$

		logAttackerPath_ = new JFormattedTextField();
		logAttackerPath_.setValue(System.getProperty("user.dir"));
		logAttackerPath_.setPreferredSize(new Dimension(100,20));
		logAttackerPath_.setName("attackerPath");
		logAttackerPath_.setCaretPosition(logAttackerPath_.getText().length());
		logAttackerPath_.setToolTipText(logAttackerPath_.getText());
		logAttackerPath_.addFocusListener(this);
		c.gridx = 1;
		add(logAttackerPath_,c);
		
		//Checkbox to enable encrypted logging
		c.gridx = 0;
		++c.gridy;
		add(new JLabel(Messages.getString("AttackerPanel.EncryptedLoggingLabel")),c);	
		
		encryptedLogging_ = new JCheckBox();
		encryptedLogging_.setSelected(false);
		encryptedLogging_.setActionCommand("encryptedLogging"); //$NON-NLS-1$
		c.gridx = 1;
		add(encryptedLogging_,c);
		encryptedLogging_.addActionListener(this);	
	      
		++c.gridy;
		c.gridwidth = 2;
		c.gridx = 0;	
		add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		
		++c.gridy;
		c.gridx = 0;
		add(new JLabel(Messages.getString("EditLogControlPanel.privacyLogLabel")),c); //$NON-NLS-1$
		
		c.gridx = 1;
		logPrivacyCheckBox_ = new JCheckBox();
		logPrivacyCheckBox_.setSelected(false);
		logPrivacyCheckBox_.setActionCommand("logPrivacyData"); //$NON-NLS-1$
		logPrivacyCheckBox_.addActionListener(this);
		add(logPrivacyCheckBox_,c);	
		
		c.gridwidth = 2;
		++c.gridy;
		c.gridx = 0;	
		add(new JLabel(Messages.getString("EditLogControlPanel.privacyPathLabel")),c); //$NON-NLS-1$

		logPrivacyPath_ = new JFormattedTextField();
		logPrivacyPath_.setValue(System.getProperty("user.dir"));
		logPrivacyPath_.setName("privacyPath");
		logPrivacyPath_.setPreferredSize(new Dimension(100,20));
		logPrivacyPath_.setCaretPosition(logPrivacyPath_.getText().length());
		logPrivacyPath_.setToolTipText(logPrivacyPath_.getText());
		logPrivacyPath_.addFocusListener(this);
		c.gridx = 1;
		add(logPrivacyPath_,c);
		
		++c.gridy;
		c.gridwidth = 2;
		c.gridx = 0;	
		add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		
		++c.gridy;
		c.gridx = 0;
		add(new JLabel(Messages.getString("EditLogControlPanel.IDSLogLabel")),c); //$NON-NLS-1$
		
		c.gridx = 1;
		logIDSCheckBox_ = new JCheckBox();
		logIDSCheckBox_.setSelected(false);
		logIDSCheckBox_.setActionCommand("logIDSData"); //$NON-NLS-1$
		logIDSCheckBox_.addActionListener(this);
		add(logIDSCheckBox_,c);	
		
		c.gridwidth = 2;
		++c.gridy;
		c.gridx = 0;	
		add(new JLabel(Messages.getString("EditLogControlPanel.IDSPathLabel")),c); //$NON-NLS-1$

		logIDSPath_ = new JFormattedTextField();
		logIDSPath_.setValue(System.getProperty("user.dir"));
		logIDSPath_.setName("IDSPath");
		logIDSPath_.setPreferredSize(new Dimension(100,20));
		logIDSPath_.setCaretPosition(logIDSPath_.getText().length());
		logIDSPath_.setToolTipText(logIDSPath_.getText());
		logIDSPath_.addFocusListener(this);
		c.gridx = 1;
		add(logIDSPath_,c);
		
		++c.gridy;
		c.gridwidth = 2;
		c.gridx = 0;	
		add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		++c.gridy;
		c.gridx = 0;
		add(new JLabel(Messages.getString("EditLogControlPanel.EventLogLabel")),c); //$NON-NLS-1$
		
		c.gridx = 1;
		logEventCheckBox_ = new JCheckBox();
		logEventCheckBox_.setSelected(false);
		logEventCheckBox_.setActionCommand("logEventData"); //$NON-NLS-1$
		logEventCheckBox_.addActionListener(this);
		add(logEventCheckBox_,c);	
		
		c.gridwidth = 2;
		++c.gridy;
		c.gridx = 0;	
		add(new JLabel(Messages.getString("EditLogControlPanel.EventPathLabel")),c); //$NON-NLS-1$

		logEventPath_ = new JFormattedTextField();
		logEventPath_.setValue(System.getProperty("user.dir"));
		logEventPath_.setName("EventPath");
		logEventPath_.setPreferredSize(new Dimension(100,20));
		logEventPath_.setCaretPosition(logEventPath_.getText().length());
		logEventPath_.setToolTipText(logEventPath_.getText());
		logEventPath_.addFocusListener(this);
		c.gridx = 1;
		add(logEventPath_,c);

	      		
		//define FileFilter for fileChooser
		logFileFilter_ = new FileFilter(){
			public boolean accept(File f) {
				if (f.isDirectory()) return true;
				return f.getName().toLowerCase().endsWith(".log"); //$NON-NLS-1$
			}
			public String getDescription () { 
				return Messages.getString("EditLogControlPanel.logFiles") + " (*.log)"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		};

			
		//to consume the rest of the space
		c.weighty = 1.0;
		++c.gridy;
		JPanel space = new JPanel();
		space.setOpaque(false);
		add(space, c);
	}
	
	public void setFilePath(JFormattedTextField textField){
		//begin with creation of new file
		JFileChooser fc = new JFileChooser();
		//set directory and ".log" filter
		fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setFileFilter(logFileFilter_);
		
		int status = fc.showDialog(this, Messages.getString("EditLogControlPanel.approveButton"));
		
		if(status == JFileChooser.APPROVE_OPTION){
				textField.setValue(fc.getSelectedFile().getAbsolutePath());
				textField.setToolTipText(fc.getSelectedFile().getAbsolutePath());
				textField.setCaretPosition(textField.getText().length());
		}
	}
	
	

	@Override
	public void focusGained(FocusEvent arg0) {
		// TODO Auto-generated method stub
		if("privacyPath".equals(((JFormattedTextField) arg0.getSource()).getName()) && attackerFlag){
			attackerFlag = false;
			setFilePath(logPrivacyPath_);
		}
		else{
			logAttackerPath_.setCaretPosition(logAttackerPath_.getText().length());
			attackerFlag = true;
		}
		
		if("attackerPath".equals(((JFormattedTextField) arg0.getSource()).getName()) && privacyFlag) {
			privacyFlag = false;
			setFilePath(logAttackerPath_);
		}	
		else {
			logPrivacyPath_.setCaretPosition(logPrivacyPath_.getText().length());
			privacyFlag = true;
		}

		if("IDSPath".equals(((JFormattedTextField) arg0.getSource()).getName()) && IDSFlag) {
			IDSFlag = false;
			setFilePath(logIDSPath_);
		}	
		else {
			logIDSPath_.setCaretPosition(logIDSPath_.getText().length());
			IDSFlag = true;
		}
		
		if("EventPath".equals(((JFormattedTextField) arg0.getSource()).getName()) && EventFlag) {
			EventFlag = false;
			setFilePath(logEventPath_);
		}	
		else {
			logEventPath_.setCaretPosition(logEventPath_.getText().length());
			EventFlag = true;
		}
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		String command = arg0.getActionCommand();

		if("logAttackerData".equals(command)){	
			Vehicle.setAttackerDataLogged_(logAttackerCheckBox_.isSelected());
			if(logAttackerCheckBox_.isSelected()){
				AttackLogWriter.setLogPath(logAttackerPath_.getValue().toString());
				if(Renderer.getInstance().getAttackerVehicle() == null) JOptionPane.showMessageDialog(null, Messages.getString("AttackerPanel.MsgBox"), Messages.getString("AttackerPanel.MsgBoxTitel"), JOptionPane.OK_OPTION);
			}
			if(!logAttackerCheckBox_.isSelected()){
				encryptedLogging_.setSelected(false);
				Vehicle.setAttackerEncryptedDataLogged_(false);
			}
		}
		else if("encryptedLogging".equals(command)){
			Vehicle.setAttackerEncryptedDataLogged_(encryptedLogging_.isSelected());
			if(encryptedLogging_.isSelected() && !logAttackerCheckBox_.isSelected()){
				logAttackerCheckBox_.setSelected(true);
				Vehicle.setAttackerDataLogged_(logAttackerCheckBox_.isSelected());
				AttackLogWriter.setLogPath(logAttackerPath_.getValue().toString());
				if(Renderer.getInstance().getAttackerVehicle() == null) JOptionPane.showMessageDialog(null, Messages.getString("AttackerPanel.MsgBox"), Messages.getString("AttackerPanel.MsgBoxTitel"), JOptionPane.OK_OPTION);
			}
		}
		else if("logPrivacyData".equals(command)){
			Vehicle.setPrivacyDataLogged_(logPrivacyCheckBox_.isSelected());
			PrivacyLogWriter.setLogPath(logPrivacyPath_.getValue().toString());
		}
		else if("logIDSData".equals(command)){
			IDSProcessor.setLogIDS_(logIDSCheckBox_.isSelected());
			IDSLogWriter.setLogPath(logIDSPath_.getValue().toString());
			LocationInformationLogWriter.setLogPath(logIDSPath_.getValue().toString());
		}
		else if("logEventData".equals(command)){
			EventLogWriter.setLogPath(logEventPath_.getValue().toString());
			EventLogWriter.log("Time," + "PenaltyType," + "X," + "Y," + "Sender," + "Receiver");
			KnownPenalties.setLogEvents_(logEventCheckBox_.isSelected());
		}
	}
	
	

	
	public JCheckBox getEncryptedLogging_() {
		return encryptedLogging_;
	}

	public JCheckBox getLogAttackerCheckBox_() {
		return logAttackerCheckBox_;
	}
	
	public void refreshGUI() {
		logAttackerCheckBox_.setSelected(Vehicle.isAttackerDataLogged_());
	}

	public JCheckBox getLogPrivacyCheckBox_() {
		return logPrivacyCheckBox_;
	}

	/**
	 * @return the logIDSCheckBox_
	 */
	public JCheckBox getLogIDSCheckBox_() {
		return logIDSCheckBox_;
	}

	/**
	 * @return the logIDSPath_
	 */
	public JFormattedTextField getLogIDSPath_() {
		return logIDSPath_;
	}

	public JFormattedTextField getLogPrivacyPath_() {
		return logPrivacyPath_;
	}

	/**
	 * @return the logEventCheckBox_
	 */
	public JCheckBox getLogEventCheckBox_() {
		return logEventCheckBox_;
	}

	/**
	 * @return the logEventPath_
	 */
	public JFormattedTextField getLogEventPath_() {
		return logEventPath_;
	}
}