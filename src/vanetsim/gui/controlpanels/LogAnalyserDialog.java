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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.text.DecimalFormat;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import vanetsim.VanetSimStart;
import vanetsim.gui.helpers.LogAnalyser;
import vanetsim.gui.helpers.TextAreaLabel;
import vanetsim.localization.Messages;

/**
 * A dialog to create,edit and delete vehicle type files.
 */


public final class LogAnalyserDialog extends JDialog implements ActionListener{
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = -2918735809479587896L;

	/** The field to show and save the current vehicle type file path. */
	private final JFormattedTextField filePath_ = new JFormattedTextField();
	
	/** RadioButton choose Silent Period Log */
	private JRadioButton silentPeriodLog_;
	
	/** RadioButton choose Mix-Zone Log */
	private JRadioButton mixZoneLog_;
	
	/** RadioButton choose Slow-Log Log */
	private JRadioButton slowLog_;
	
	/** A JComboBox to switch between vehicles types. */
	private JComboBox<String> chooseAttackType_ = new JComboBox<String>();	
	
	/** The field to tune calculated time */
	private final JFormattedTextField tuneTime_ = new JFormattedTextField(new DecimalFormat());
	
	/** The field to tune time  buffer*/
	private final JFormattedTextField timeBuffer_ = new JFormattedTextField();
	
	/** The field to rate the bigger streets */
	private final JFormattedTextField bigStreet_ = new JFormattedTextField(new DecimalFormat());
	
	/** The field to rate the smaller streets */
	private final JFormattedTextField smallStreet_ = new JFormattedTextField(new DecimalFormat());
	
	/** The field to rate if car stays on same street */
	private final JFormattedTextField staysOnStreet_ = new JFormattedTextField(new DecimalFormat());
	
	/** The field to rate if car leaves on same street */
	private final JFormattedTextField leavesStreet_ = new JFormattedTextField(new DecimalFormat());
	
	/** The field to rate if car makes uturn */
	private final JFormattedTextField makesUTurn_ = new JFormattedTextField(new DecimalFormat());
	
	/** The field to set a limit for the angle off which a vehicle can change during a silent period */
	private final JFormattedTextField limitToAngle_ = new JFormattedTextField();
	
	/** Maximal search time for slow model */
	private final JFormattedTextField maxSlowSearchTime_ = new JFormattedTextField();
	
	/** The JLabel for maxSlowSearchTime */
	private final JLabel maxSlowSearchTimeLabel_;

	/** The JLabel to tune calculated time */
	private final JLabel tuneTimeLabel_;
	
	/** The JLabel to tune time  buffer*/
	private final JLabel timeBufferLabel_;
	
	/** The JLabel to rate the bigger streets */
	private final JLabel bigStreetLabel_;
	
	/** The JLabel to rate the smaller streets */
	private final JLabel smallStreetLabel_;
	
	/** The JLabel to rate if car stays on same street */
	private final JLabel staysOnStreetLabel_;
	
	/** The JLabel to rate if car leaves on same street */
	private final JLabel leavesStreetLabel_;
	
	/** The JLabel to rate if car makes u-turn */
	private final JLabel makesUTurnLabel_;
	
	/** The JLabel for limitToAngle_ */
	private final JLabel limitToAngleLabel_;
	
	/** The JLabel for advancedDataFilePath_ */
	private final JLabel advancedDataFilePathLabel_;
	
	/** FileFilter to choose only ".log" files from FileChooser */
	private FileFilter logFileFilter_;
	
	/** Note to describe the standard attack. */
	private TextAreaLabel standardAttackNote_;
	
	/** An area to display text information. */
	private final JTextArea informationTextArea_;
	
	/** The field to load additional information about scenario */
	private final JFormattedTextField advancedDataFilePath_ = new JFormattedTextField();
	
	/** The checkbox to activate and deactivate probability in advanced mix analysis. */
	private final JCheckBox useProbability_;
	
	/** The JLabel for  useProbability_*/
	private final JLabel useProbabilityLabel_;
	
	/** button to load file */
	private final JButton btnOpenAdvanced_;
	
	/** start button */
	private JButton btnStart_;
	
	/** stop button */
	private JButton btnStop_;
	
	/** progress bar */
	private JProgressBar progressBar;
	
	/** button to copy results */
	private final JButton copyResultsBtn;

	/** log-analyser logic */
	private LogAnalyser logAnalyser_;

	/**
	 * Constructor. Creating GUI items.
	 */
	public LogAnalyserDialog(){
		
		logAnalyser_ = new LogAnalyser(this);
		
		//some JDialog options
		setVisible(false);
		setSize(1000, 550);
		setUndecorated(true);
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
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 0.3;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;	
		c.insets = new Insets(5,5,5,5);

		
		//start building gui
		filePath_.setEditable(false);
		filePath_.setPreferredSize(new Dimension(150,20));
		add(filePath_, c);
	

		JPanel OpenFilePanel = new JPanel();
		c.gridx = 1;
		add(OpenFilePanel, c);
		
		//add navigation
		OpenFilePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JButton btnOpen_ = new JButton(Messages.getString("LogAnalyserDialog.btnOpen"));
		btnOpen_.setActionCommand("openFile");
		btnOpen_.setPreferredSize(new Dimension(80,20));
		btnOpen_.addActionListener(this);
		OpenFilePanel.add(btnOpen_);
		
		JButton btnAdd = new JButton("+");
		btnAdd.setActionCommand("addFile");
		btnAdd.setPreferredSize(new Dimension(20,20));
		btnAdd.addActionListener(this);
		OpenFilePanel.add(btnAdd);
		
		JButton btnDelete = new JButton("-");
		btnDelete.setActionCommand("deleteFile");
		btnDelete.setPreferredSize(new Dimension(20,20));
		btnDelete.addActionListener(this);
		OpenFilePanel.add(btnDelete);
		
		JButton btnShow = new JButton(Messages.getString("LogAnalyserDialog.btnShow"));
		btnShow.setActionCommand("showJobs");
		btnShow.setPreferredSize(new Dimension(120,20));
		btnShow.addActionListener(this);
		OpenFilePanel.add(btnShow);
		
		JButton btnSave = new JButton(Messages.getString("LogAnalyserDialog.btnSave"));
		btnSave.setActionCommand("saveJobs");
		btnSave.setPreferredSize(new Dimension(120,20));
		btnSave.addActionListener(this);
		OpenFilePanel.add(btnSave);
		
		//buttons to control simulation
		c.gridx = 2;
		JPanel TypePanel = new JPanel();
		add(TypePanel, c);
		
		TypePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		btnStart_ = new JButton(Messages.getString("LogAnalyserDialog.startBtn"));
		btnStart_.setActionCommand("startAnalysing");
		btnStart_.setPreferredSize(new Dimension(156,20));
		btnStart_.addActionListener(this);
		TypePanel.add(btnStart_);
		
		btnStop_ = new JButton(Messages.getString("LogAnalyserDialog.stopBtn"));
		btnStop_.setActionCommand("stopAnalysing");
		btnStop_.setPreferredSize(new Dimension(156,20));
		btnStop_.addActionListener(this);
		btnStop_.setEnabled(false);
		TypePanel.add(btnStop_);

		//Radio button to choice mix-zone
		ButtonGroup group = new ButtonGroup();
		mixZoneLog_ = new JRadioButton(Messages.getString("LogAnalyserDialog.MixZoneLog")); //$NON-NLS-1$
		mixZoneLog_.setActionCommand("chooseMixZone"); //$NON-NLS-1$
		mixZoneLog_.addActionListener(this);
		mixZoneLog_.setSelected(true);
		group.add(mixZoneLog_);
		c.gridx = 0;
		c.gridwidth = 2;
		++c.gridy;
		add(mixZoneLog_,c);
		
		//progress bar
		progressBar = new JProgressBar(0, 100);
		progressBar.setSize(240, 20);
        progressBar.setValue(0);
		c.gridx = 2;
		c.gridwidth = 1;
        add(progressBar, c);
        progressBar.setVisible(false);
        
        copyResultsBtn = new JButton(Messages.getString("LogAnalyserDialog.copyResults"));
        copyResultsBtn.setActionCommand("copyResults");
        copyResultsBtn.setPreferredSize(new Dimension(80,20));
        copyResultsBtn.addActionListener(this);
        add(copyResultsBtn, c);
        
        //Radio button to choose silent-periods
		silentPeriodLog_ = new JRadioButton(Messages.getString("LogAnalyserDialog.SilentPeriodLog")); //$NON-NLS-1$
		silentPeriodLog_.setActionCommand("chooseSilentPeriod"); //$NON-NLS-1$
		silentPeriodLog_.addActionListener(this);
		group.add(silentPeriodLog_);
        c.gridx = 0;
        c.gridwidth = 2;
		++c.gridy;
		add(silentPeriodLog_,c);

		//Radio button to choose slow
        slowLog_ = new JRadioButton(Messages.getString("LogAnalyserDialog.SlowLog")); //$NON-NLS-1$
        slowLog_.setActionCommand("chooseSlow"); //$NON-NLS-1$
        slowLog_.addActionListener(this);
		group.add(slowLog_);
        c.gridx = 0;
		++c.gridy;
		add(slowLog_,c);
		
		
		c.ipadx = 300;
		c.ipady = 400;
        c.gridx = 2;
        c.gridwidth = 1;
        c.gridheight = 13;
       

        //information area to display log information
        informationTextArea_ = new JTextArea(20,1);
		informationTextArea_.setEditable(false);
		informationTextArea_.setLineWrap(true);
		JScrollPane scrolltext = new JScrollPane(informationTextArea_);
		add(scrolltext, c);     
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		
		
		//choose attack type
		c.gridwidth = 1;
		c.gridx = 0;
		++c.gridy;
		add(new JLabel(Messages.getString("LogAnalyserDialog.selectType")), c); //$NON-NLS-1$
		c.gridx = 1;
		add(chooseAttackType_, c);
		chooseAttackType_.addItem("Standard");
		chooseAttackType_.addItem("Advanced");
		chooseAttackType_.addActionListener(this);		
		
		//label to show information about the attack types
		c.gridwidth = 2;
		standardAttackNote_ = new TextAreaLabel(Messages.getString("LogAnalyserDialog.standardMixAttackNote")); //$NON-NLS-1$
		++c.gridy;
		c.gridx = 0;
		add(standardAttackNote_, c);
		standardAttackNote_.setVisible(true);
		
		//preferences for the different attacks
		c.gridwidth = 1;
		++c.gridy;
		tuneTimeLabel_ = new JLabel(Messages.getString("LogAnalyserDialog.tuneTime"));
		add(tuneTimeLabel_, c); //$NON-NLS-1$
		c.gridx = 1;
		tuneTime_.setValue(new Double(2.2));
		add(tuneTime_, c);

		c.gridx = 0;
		++c.gridy;
		timeBufferLabel_ = new JLabel(Messages.getString("LogAnalyserDialog.timeBuffer"));
		add(timeBufferLabel_, c); //$NON-NLS-1$
		c.gridx = 1;
		timeBuffer_.setValue(new Double(0));
		add(timeBuffer_, c);

		c.gridx = 0;		
		++c.gridy;
		limitToAngleLabel_ = new JLabel(Messages.getString("LogAnalyserDialog.limitToAngle"));
		add(limitToAngleLabel_, c); //$NON-NLS-1$
		
		c.gridx = 1;
		limitToAngle_.setValue(new Double(170));
		limitToAngleLabel_.setVisible(false);
		limitToAngle_.setVisible(false);
		add(limitToAngle_, c);
		
		c.gridx = 0;
		advancedDataFilePathLabel_ = new JLabel(Messages.getString("LogAnalyserDialog.advancedDataFilePathLabel"));
		add(advancedDataFilePathLabel_, c); //$NON-NLS-1$
		advancedDataFilePathLabel_.setVisible(false);
		c.gridx = 1;	
		advancedDataFilePath_.setEditable(false);
		advancedDataFilePath_.setPreferredSize(new Dimension(355,20));
		btnOpenAdvanced_ = new JButton(Messages.getString("LogAnalyserDialog.btnOpen"));
		btnOpenAdvanced_.setActionCommand("openAdvancedLogFile");
		btnOpenAdvanced_.setPreferredSize(new Dimension(80,20));
		btnOpenAdvanced_.addActionListener(this);
		
		JPanel space = new JPanel();
		space.setPreferredSize(new Dimension(5,1));
		
		JPanel wrapper = new JPanel( new FlowLayout(0, 0, FlowLayout.LEADING) );
		wrapper.add( advancedDataFilePath_ );
		wrapper.add( space );
		wrapper.add( btnOpenAdvanced_ );

		btnOpenAdvanced_.setVisible(false);
		advancedDataFilePath_.setVisible(false);
		add(wrapper, c);
		

		c.gridx = 0;
		++c.gridy;
		useProbabilityLabel_ = new JLabel(Messages.getString("LogAnalyserDialog.useProbability"));
		add(useProbabilityLabel_,c); //$NON-NLS-1$	
		useProbability_ = new JCheckBox();
		c.gridx = 1;
		add(useProbability_,c);
		useProbability_.setSelected(true);
		useProbabilityLabel_.setVisible(false);
		useProbability_.setVisible(false);
		

		++c.gridy;
		c.gridx = 0;
		bigStreetLabel_ = new JLabel(Messages.getString("LogAnalyserDialog.bigStreet"));
		add(bigStreetLabel_, c); //$NON-NLS-1$
		c.gridx = 1;
		bigStreet_.setValue(new Double(0.9));
		add(bigStreet_, c);
		
		c.gridx = 0;
		maxSlowSearchTimeLabel_ = new JLabel(Messages.getString("LogAnalyserDialog.maxSlowSearchTimeLabel"));
		add(maxSlowSearchTimeLabel_, c); //$NON-NLS-1$
		c.gridx = 1;
		maxSlowSearchTime_.setValue(10000);
		add(maxSlowSearchTime_, c);
		c.gridx = 0;
		maxSlowSearchTimeLabel_.setVisible(false);
		maxSlowSearchTime_.setVisible(false);
		
		++c.gridy;
		smallStreetLabel_ = new JLabel(Messages.getString("LogAnalyserDialog.smallStreet"));
		add(smallStreetLabel_, c); //$NON-NLS-1$
		c.gridx = 1;
		smallStreet_.setValue(new Double(0.85));
		add(smallStreet_, c);
		c.gridx = 0;
		
		++c.gridy;
		staysOnStreetLabel_ = new JLabel(Messages.getString("LogAnalyserDialog.staysOnStreet"));
		add(staysOnStreetLabel_, c); //$NON-NLS-1$
		c.gridx = 1;
		staysOnStreet_.setValue(new Double(0.9));
		add(staysOnStreet_, c);
		c.gridx = 0;
		
		++c.gridy;
		leavesStreetLabel_ = new JLabel(Messages.getString("LogAnalyserDialog.leavesStreet"));
		add(leavesStreetLabel_, c); //$NON-NLS-1$
		c.gridx = 1;
		leavesStreet_.setValue(new Double(0.9));
		add(leavesStreet_, c);
		c.gridx = 0;
		
		++c.gridy;
		makesUTurnLabel_ = new JLabel(Messages.getString("LogAnalyserDialog.makesUTurn"));
		add(makesUTurnLabel_, c); //$NON-NLS-1$
		c.gridx = 1;
		makesUTurn_.setValue(new Double(0.1));
		add(makesUTurn_, c);
		c.gridx = 0;
		
		
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
		add(new JPanel(), c);
		
		setLocationRelativeTo(VanetSimStart.getMainFrame());
		setVisible(true);
	}
	

	/**
	 * An implemented <code>ActionListener</code> which performs the needed actions
	 * 
	 * @param e	an <code>ActionEvent</code>
	 */
	@SuppressWarnings("deprecation")
	public void actionPerformed(ActionEvent e) {	
		String command = e.getActionCommand();

		//the attack type has been selected
		if ("comboBoxChanged".equals(command)){
			//update gui elements
			updateGUI();			
		}
		//a advanced log file has to be selected
		else if(("openAdvancedLogFile").equals(command)){
			//start with open file dialog
			JFileChooser fc = new JFileChooser();

			//set directory and ".log" filter
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			fc.addChoosableFileFilter(logFileFilter_);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(logFileFilter_);
			
			int status = fc.showOpenDialog(this);
			
			if(status == JFileChooser.APPROVE_OPTION){
				advancedDataFilePath_.setValue(fc.getSelectedFile().getAbsoluteFile());
				
				logAnalyser_.setAdvancedFilePathChanged(true);
				logAnalyser_.setAdvancedFilePath_(advancedDataFilePath_.getText());
			}		
		}	
		//a log file has to be selected
		else if(("openFile").equals(command)){
			//start with open file dialog
			JFileChooser fc = new JFileChooser();

			//set directory and ".log" filter
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			fc.addChoosableFileFilter(logFileFilter_);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(logFileFilter_);
			
			int status = fc.showOpenDialog(this);
			
			if(status == JFileChooser.APPROVE_OPTION){
				filePath_.setValue(fc.getSelectedFile().getAbsoluteFile());
				
				logAnalyser_.setFilePathChanged(true);
				
				if(mixZoneLog_.isSelected()) logAnalyser_.setLogType("mixzone");
				if(silentPeriodLog_.isSelected()) logAnalyser_.setLogType("silentperiod");
				if(slowLog_.isSelected()) logAnalyser_.setLogType("slow");
				if(chooseAttackType_.getSelectedItem().equals("Standard")) logAnalyser_.setAttackType("standard");
				if(chooseAttackType_.getSelectedItem().equals("Advanced")) logAnalyser_.setAttackType("advanced");
			}		
		}
		//mix-zone radio button has been selected
		else if("chooseMixZone".equals(command)){
			//update combobox options. Be careful ActionListener has to be removed before editing JComboBox items
			chooseAttackType_.removeActionListener(this);
			chooseAttackType_.removeAllItems();
			chooseAttackType_.addItem("Standard");
			chooseAttackType_.addItem("Advanced");
			chooseAttackType_.addActionListener(this);
			
			updateGUI();
		}
		//silent-period radio button has been selected
		else if("chooseSilentPeriod".equals(command)){
			//update combobox options. Be careful ActionListener has to be removed before editing JComboBox items
			chooseAttackType_.removeActionListener(this);
			chooseAttackType_.removeAllItems();
			chooseAttackType_.addItem("Standard");
			chooseAttackType_.addItem("Advanced");
			chooseAttackType_.addActionListener(this);
			
			updateGUI();
		}
		else if("chooseSlow".equals(command)){
			//update combobox options. Be careful ActionListener has to be removed before editing JComboBox items
			chooseAttackType_.removeActionListener(this);
			chooseAttackType_.removeAllItems();
			chooseAttackType_.addItem("Standard");
			chooseAttackType_.addItem("Advanced");
			chooseAttackType_.addActionListener(this);
			
			updateGUI();
		}
		
		//the start button has been clicked. Set preferences and start the next job
		else if("startAnalysing".equals(command)){
			if(mixZoneLog_.isSelected()) logAnalyser_.setLogType("mixzone");
			if(silentPeriodLog_.isSelected()) logAnalyser_.setLogType("silentperiod");
			if(slowLog_.isSelected()) logAnalyser_.setLogType("slow");
			if(chooseAttackType_.getSelectedItem().equals("Standard")) logAnalyser_.setAttackType("standard");
			if(chooseAttackType_.getSelectedItem().equals("Advanced")) logAnalyser_.setAttackType("advanced");

			logAnalyser_.startNextJob(true);
		}
		//the stop button has been clicked. Stop all simulations and reset GUI
		else if("stopAnalysing".equals(command)){
			if(logAnalyser_.getOperation_() != null){
				logAnalyser_.getOperation_().stop();
				logAnalyser_.setOperation_(null);
				btnStart_.setEnabled(true);
				btnStop_.setEnabled(false);
				progressBar.setVisible(false);
				copyResultsBtn.setVisible(true);
			}
		}		
		//copy button for the information area
		else if("copyResults".equals(command)){
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(informationTextArea_.getText()), null);
		}
		//deletes the next job in line
		else if("deleteFile".equals(command)){
			if(logAnalyser_.getJobs().size() > 0) {
				logAnalyser_.updateInformationArea("\nDeleted job:\n" + logAnalyser_.getJobs().get(logAnalyser_.getJobs().size()-1) + "\n");
				logAnalyser_.getJobs().remove(logAnalyser_.getJobs().size()-1);
			}
		}	
		//adds a new job
		else if("addFile".equals(command)){
			if(!filePath_.getText().equals("")){
				if(mixZoneLog_.isSelected()) logAnalyser_.setLogType("mixzone");
				if(silentPeriodLog_.isSelected()) logAnalyser_.setLogType("silentperiod");
				if(slowLog_.isSelected()) logAnalyser_.setLogType("slow");
				if(chooseAttackType_.getSelectedItem().equals("Standard")) logAnalyser_.setAttackType("standard");
				if(chooseAttackType_.getSelectedItem().equals("Advanced")) logAnalyser_.setAttackType("advanced");
				
				logAnalyser_.setTimeBufferValue(Double.parseDouble(getTimeBuffer_().getValue().toString()));
				logAnalyser_.setTuneTimeValue(Double.parseDouble(getTuneTime_().getValue().toString()));
				logAnalyser_.setBiggerStreetValue(Double.parseDouble(getBigStreet_().getValue().toString()));
				logAnalyser_.setSmallerStreetValue(Double.parseDouble(getSmallStreet_().getValue().toString()));
				logAnalyser_.setDrivesStraigthValue(Double.parseDouble(getStaysOnStreet_().getValue().toString()));
				logAnalyser_.setTurnsValue(Double.parseDouble(getLeavesStreet_().getValue().toString()));
				logAnalyser_.setMakesUTurnValue(Double.parseDouble(getMakesUTurn_().getValue().toString()));
				logAnalyser_.setLimitToAngle(Double.parseDouble(getLimitToAngle_().getValue().toString()));
				logAnalyser_.setUseProbability(useProbability_.isSelected());
				logAnalyser_.setMaxSlowSearchTime(Double.parseDouble(getMaxSlowSearchTime_().getValue().toString()));
				if(mixZoneLog_.isSelected() && chooseAttackType_.getSelectedItem().equals("Advanced")) logAnalyser_.setAdvancedFilePath_(advancedDataFilePath_.getText());
				else logAnalyser_.setAdvancedFilePath_("0");
				
				logAnalyser_.updateInformationArea("\nAdded job:");
				logAnalyser_.addJob(filePath_.getText());
			}
		}
		//show all jobs
		else if("showJobs".equals(command)){
			boolean isEmpty = true;
			for(String j:logAnalyser_.getJobs()) {
				isEmpty = false;
				logAnalyser_.updateInformationArea("\n\njob:\n" + j + "\n");
			}
			if(isEmpty) logAnalyser_.updateInformationArea("\n" + Messages.getString("LogAnalyserDialog.jobsEmpty"));
		}
		//save jobs to a file (jobs.txt)
		else if("saveJobs".equals(command)){
			logAnalyser_.saveJobsToFile();
		}
	}
	
	
	
	/**
	 * Methode to update GUI Elements when changing mode
	 */
	public void updateGUI(){
		tuneTimeLabel_.setVisible(false);
		tuneTime_.setVisible(false);
		timeBufferLabel_.setVisible(false);
		timeBuffer_.setVisible(false);
		bigStreetLabel_.setVisible(false);
		bigStreet_.setVisible(false);
		smallStreetLabel_.setVisible(false);
		smallStreet_.setVisible(false);
		staysOnStreetLabel_.setVisible(false);
		staysOnStreet_.setVisible(false);
		leavesStreetLabel_.setVisible(false);
		leavesStreet_.setVisible(false);
		makesUTurnLabel_.setVisible(false);
		makesUTurn_.setVisible(false);
		useProbabilityLabel_.setVisible(false);
		useProbability_.setVisible(false);
		btnOpenAdvanced_.setVisible(false);
		advancedDataFilePath_.setVisible(false);
		limitToAngle_.setVisible(false);
		limitToAngleLabel_.setVisible(false);
		advancedDataFilePathLabel_.setVisible(false);
		maxSlowSearchTimeLabel_.setVisible(false);
		maxSlowSearchTime_.setVisible(false);
		
		if(mixZoneLog_.isSelected()){
			if(chooseAttackType_.getSelectedItem().toString().equals("Standard")){
				tuneTimeLabel_.setVisible(true);
				tuneTime_.setVisible(true);
				timeBufferLabel_.setVisible(true);
				timeBuffer_.setVisible(true);
				bigStreetLabel_.setVisible(true);
				bigStreet_.setVisible(true);
				smallStreetLabel_.setVisible(true);
				smallStreet_.setVisible(true);
				staysOnStreetLabel_.setVisible(true);
				staysOnStreet_.setVisible(true);
				leavesStreetLabel_.setVisible(true);
				leavesStreet_.setVisible(true);
				makesUTurnLabel_.setVisible(true);
				makesUTurn_.setVisible(true);
				
				standardAttackNote_.setText(Messages.getString("LogAnalyserDialog.standardMixAttackNote"));
			}
			else if(chooseAttackType_.getSelectedItem().toString().equals("Advanced")){
				timeBufferLabel_.setVisible(true);
				timeBuffer_.setVisible(true);
				useProbabilityLabel_.setVisible(true);
				useProbability_.setVisible(true);
				btnOpenAdvanced_.setVisible(true);
				advancedDataFilePath_.setVisible(true);
				advancedDataFilePathLabel_.setVisible(true);

				standardAttackNote_.setText(Messages.getString("LogAnalyserDialog.advancedMixAttackNote"));
			}
		}
		else if(silentPeriodLog_.isSelected()){
			tuneTimeLabel_.setVisible(true);
			tuneTime_.setVisible(true);
			
			standardAttackNote_.setText(Messages.getString("LogAnalyserDialog.standardSilentPeriodAttackNote"));
			if(chooseAttackType_.getSelectedItem().toString().equals("Standard")){
				
			}
			else if(chooseAttackType_.getSelectedItem().toString().equals("Advanced")){
				standardAttackNote_.setText(Messages.getString("LogAnalyserDialog.advancedSilentPeriodAttackNote"));
				
				limitToAngle_.setVisible(true);
				limitToAngleLabel_.setVisible(true);
			}
		}
		else if(slowLog_.isSelected()){
			tuneTimeLabel_.setVisible(true);
			tuneTime_.setVisible(true);
			
			standardAttackNote_.setText(Messages.getString("LogAnalyserDialog.standardSlowAttackNote"));
			if(chooseAttackType_.getSelectedItem().toString().equals("Standard")){
				maxSlowSearchTimeLabel_.setVisible(true);
				maxSlowSearchTime_.setVisible(true);
			}
		}
	}
	
	
	/**
	 * Methode is evoked when closing JDialog
	 */
	public void closeDialog(){
		//close JDialog
		this.dispose();
	}	
	
	
	
	/**
	 * Gets the tune time value set in the gui
	 * 
	 * @return the tune time value
	 */
	public JFormattedTextField getTuneTime_() {
		return tuneTime_;
	}

	/**
	 * Gets the time buffer value set in the gui
	 * 
	 * @return the time buffer value
	 */
	public JFormattedTextField getTimeBuffer_() {
		return timeBuffer_;
	}

	/**
	 * Gets the big street value set in the gui
	 * 
	 * @return the big street value
	 */
	public JFormattedTextField getBigStreet_() {
		return bigStreet_;
	}

	/**
	 * Gets the small street value set in the gui
	 * 
	 * @return the small street value
	 */
	public JFormattedTextField getSmallStreet_() {
		return smallStreet_;
	}

	/**
	 * Gets the stays on street value set in the gui
	 * 
	 * @return the stays on street value
	 */
	public JFormattedTextField getStaysOnStreet_() {
		return staysOnStreet_;
	}

	/**
	 * Gets the leaves street value set in the gui
	 * 
	 * @return the leaves street value
	 */
	public JFormattedTextField getLeavesStreet_() {
		return leavesStreet_;
	}

	/**
	 * Gets the makes u-turn value set in the gui
	 * 
	 * @return the makes u-turn value
	 */
	public JFormattedTextField getMakesUTurn_() {
		return makesUTurn_;
	}

	/**
	 * Gets the log file path set in the gui
	 * 
	 * @return the log file path
	 */
	public JFormattedTextField getFilePath_() {
		return filePath_;
	}

	/**
	 * Gets the log file path for advanced attacks set in the gui
	 * 
	 * @return the log file path for advanced attacks
	 */
	public JFormattedTextField getAdvancedDataFilePath_() {
		return advancedDataFilePath_;
	}

	/**
	 * Gets the the probabilities flag set in the gui
	 * 
	 * @return <code>true</code> if the probabilities should be used in the attack
	 */
	public JCheckBox getUseProbability_() {
		return useProbability_;
	}

	/**
	 * Gets the start button element of the GUI
	 * 
	 * @return the start button element of the GUI
	 */
	public JButton getBtnStart_() {
		return btnStart_;
	}

	/**
	 * Gets the stop button element of the GUI
	 * 
	 * @return the stop button element of the GUI
	 */
	public JButton getBtnStop_() {
		return btnStop_;
	}

	/**
	 * Gets the progressbar element of the GUI
	 * 
	 * @return the progressbar element of the GUI
	 */
	public JProgressBar getProgressBar() {
		return progressBar;
	}

	/**
	 * Sets the progress bar
	 * 
	 * @param progressBar the new JProgressBar
	 */
	public void setProgressBar(JProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	/**
	 * Gets the limit to angle element of the GUI
	 * 
	 * @return the limit to angle element of the GUI
	 */
	public JFormattedTextField getLimitToAngle_() {
		return limitToAngle_;
	}

	/**
	 * Gets the information text area element of the GUI
	 * 
	 * @return the information text area of the GUI
	 */
	public JTextArea getInformationTextArea_() {
		return informationTextArea_;
	}

	/**
	 * Gets the copy button element of the GUI
	 * 
	 * @return the copy button element of the GUI
	 */
	public JButton getCopyResultsBtn() {
		return copyResultsBtn;
	}

	/**
	 * Gets the radio button silent-period element of the GUI
	 * 
	 * @return the radio button silent-period element of the GUI
	 */
	public JRadioButton getSilentPeriodLog_() {
		return silentPeriodLog_;
	}

	/**
	 * Sets the silent-period radio button
	 * 
	 * @param silentPeriodLog_ the new JRadioButton
	 */
	public void setSilentPeriodLog_(JRadioButton silentPeriodLog_) {
		this.silentPeriodLog_ = silentPeriodLog_;
	}

	/**
	 * Gets the radio button mix-zone element of the GUI
	 * 
	 * @return the radio button mix-zone element of the GUI
	 */
	public JRadioButton getMixZoneLog_() {
		return mixZoneLog_;
	}

	/**
	 * Sets the mix zone radio button
	 * 
	 * @param mixZoneLog_ the new JRadioButton
	 */
	public void setMixZoneLog_(JRadioButton mixZoneLog_) {
		this.mixZoneLog_ = mixZoneLog_;
	}

	/**
	 * Gets the choose attack type combo box element of the GUI
	 * 
	 * @return the choose attack type combo box element of the GUI
	 */
	public JComboBox<String> getChooseAttackType_() {
		return chooseAttackType_;
	}

	/**
	 * Sets the choose attack type combo box
	 * 
	 * @param chooseAttackType_ the new JComboBox
	 */
	public void setChooseAttackType_(JComboBox<String> chooseAttackType_) {
		this.chooseAttackType_ = chooseAttackType_;
	}


	/**
	 * @return the maxSlowSearchTime_
	 */
	public JFormattedTextField getMaxSlowSearchTime_() {
		return maxSlowSearchTime_;
	}

}