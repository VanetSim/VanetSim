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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import vanetsim.VanetSimStart;
import vanetsim.anonymizer.LogfileTableModel;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.localization.Messages;
import vanetsim.map.Map;

/**
 * A dialog to set map parameters.
 */
public final class AnonymizeDataDialog extends JDialog implements ActionListener, FocusListener {
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = 4882607093689684208L;
	
	private JPanel rTop;
	
	private JPanel rBot;
	
	private JFormattedTextField inputLogfilePath;
	
	private JFormattedTextField outputLogfilePath;
	
	private JTextField formatString;
	
	private JLabel chosenColumnLabel;
	
	private JLabel info;
	
	private JTable table;
	
	private JScrollPane tableScroll;
	
	private JComboBox<String> selectedColumn;
	
	private LogfileTableModel logfileTM;
	
	/** FileFilter to choose only ".log" files from FileChooser */
	private FileFilter logFileFilter_;
	
	/**
	 * Instantiates a new anonymize data dialog.
	 */
	public AnonymizeDataDialog(){
		super(VanetSimStart.getMainFrame(),Messages.getString("AnonymizeDataDialog.title"), false); //$NON-NLS-1$

		setUndecorated(false);
		setLayout(new GridBagLayout());
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		//some basic options
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.insets = new Insets(5,5,5,5);
				
		/* the jtable to display the logfile content */
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 2;
		
		table = new JTable();
//		table.setPreferredScrollableViewportSize(new Dimension(500, 400));
		table.setMinimumSize(new Dimension(500, 400));
		table.setFillsViewportHeight(true);

		// Create the scroll pane and add the table to it.
		tableScroll = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		table.setColumnSelectionAllowed(true);
		table.setRowSelectionAllowed(false);

		// Add the scroll pane to the left hand side of this dialog.
		add(tableScroll,c);
		
		/* right top panel */
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 1;
		c.gridheight = 1;
		
		rTop = new JPanel(new GridBagLayout());
		/* Contraints for right top jpanel */
		GridBagConstraints c2 = new GridBagConstraints();
		c2.fill = GridBagConstraints.BOTH;
		c2.anchor = GridBagConstraints.PAGE_START;
		c2.weightx = 0.0;
		c2.weighty = 0;
		c2.gridx = 0;
		c2.gridy = 0;
		c2.insets = new Insets(5,5,5,5);
		
		c2.gridheight = 1;
		c2.gridwidth = 1;
		rTop.add(new JLabel(Messages.getString("AnonymizeDataDialog.inputLogfilePath")), c2);	
		c2.gridy++;
		inputLogfilePath = new JFormattedTextField();
//		inputLogfilePath.setValue(System.getProperty("user.dir"));
//		inputLogfilePath.setPreferredSize(new Dimension(120,20));
		inputLogfilePath.setName("inputLogfilePath");
		inputLogfilePath.setCaretPosition(inputLogfilePath.getText().length());
		inputLogfilePath.setToolTipText(inputLogfilePath.getText());
		inputLogfilePath.addFocusListener(this);
		rTop.add(inputLogfilePath, c2);
		
		c2.gridy++;
		rTop.add(new JLabel(Messages.getString("AnonymizeDataDialog.formatStringMsg")), c2);
		c2.gridy++;
		formatString = new JTextField();
//		formatString.setPreferredSize(new Dimension(120,20));
		formatString.setName("formatString");
		formatString.addActionListener(this);
		rTop.add(formatString, c2);
		
		c2.gridy++;
		rTop.add(new JLabel(Messages.getString("AnonymizeDataDialog.selectedCol")), c2);
		c2.gridy++;
		selectedColumn = new JComboBox<>();
//		selectedColumn.setPreferredSize(new Dimension(120,20));
		rTop.add(selectedColumn, c2);

		c2.gridy++;
		rTop.add(new JSeparator(), c2);
		c2.gridy++;
		rTop.add(new JLabel(Messages.getString("AnonymizeDataDialog.outputLogfilePath")), c2);
		c2.gridy++;
		outputLogfilePath = new JFormattedTextField();
		outputLogfilePath.setValue(System.getProperty("user.dir"));
//		outputLogfilePath.setPreferredSize(new Dimension(120,20));
		outputLogfilePath.setName("outputLogfilePath");
		outputLogfilePath.setCaretPosition(outputLogfilePath.getText().length());
		outputLogfilePath.setToolTipText(outputLogfilePath.getText());
		outputLogfilePath.addFocusListener(this);
		rTop.add(outputLogfilePath, c2);
		
		c2.gridy++;
		c2.gridx = 0;
		chosenColumnLabel = new JLabel(Messages.getString("AnonymizeDataDialog.chosenColumn"));
		chosenColumnLabel.setVisible(false);
		rTop.add(chosenColumnLabel, c2);
		
		add(rTop, c);
		
		/* right bottom panel */
		c.gridy++;
		add(new JLabel("roflol"), c);
		
		c.weightx = 0;
		c.weighty = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy++;
		info = new JLabel("Long test stating this and that and so on");
		info.setBackground(Color.RED);
		info.setBorder(BorderFactory.createLineBorder(Color.black));
		add(info ,c);
		
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
	
		pack();
		setLocationRelativeTo(VanetSimStart.getMainFrame());
		setVisible(true);
	}
	
	private void setFilePath(JFormattedTextField textField){
		//begin with creation of new file
		JFileChooser fc = new JFileChooser();
		//set directory and ".log" filter
		fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
		fc.setFileFilter(logFileFilter_);
		
		int status = fc.showDialog(this, Messages.getString("EditLogControlPanel.approveButton"));
		
		if(status == JFileChooser.APPROVE_OPTION){
				textField.setValue(fc.getSelectedFile().getAbsolutePath());
				textField.setToolTipText(fc.getSelectedFile().getAbsolutePath());
				textField.setCaretPosition(textField.getText().length());
		}
	}
	
	private void loadData() {
		logfileTM = new LogfileTableModel(inputLogfilePath.getText(), formatString.getText());
		table.setModel(logfileTM);
		for (String str : logfileTM.getColumnNames()) {
			selectedColumn.addItem(str);
		}
		selectedColumn.setSelectedIndex(0);
	}

	/**
	 * An implemented <code>ActionListener</code> which performs the needed actions when the OK-button
	 * is clicked.
	 * 
	 * @param e	an <code>ActionEvent</code>
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(formatString)) {
			if (!inputLogfilePath.getText().equals("")) {
			/* we have the file path and format, so we can load the data into the table */
				loadData();
			} else {
				//TODO [MH] print message
			}
		}
	}

	@Override
	public void focusGained(FocusEvent arg0) {
		if ("inputLogfilePath".equals(((JFormattedTextField) arg0.getSource()).getName())){
			/* to avoid a filechooser loop */
			rTop.requestFocus();
			setFilePath(inputLogfilePath);
			formatString.setText(LogfileTableModel.getFirstLine(inputLogfilePath.getText()));
		}
		else if ("outputLogfilePath".equals(((JFormattedTextField) arg0.getSource()).getName())){
			/* to avoid a filechooser loop */
			rTop.requestFocus();
			setFilePath(outputLogfilePath);
		}
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}