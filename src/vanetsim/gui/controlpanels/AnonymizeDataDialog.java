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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import vanetsim.VanetSimStart;
import vanetsim.anonymizer.AnonymityMethods;
import vanetsim.anonymizer.Data;
import vanetsim.anonymizer.LogfileTableModel;
import vanetsim.anonymizer.RemovingMethods;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.localization.Messages;
import vanetsim.map.Map;

/**
 * A dialog to set map parameters.
 */
public final class AnonymizeDataDialog extends JDialog implements ActionListener, FocusListener, ItemListener {
	
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
	
	private JComboBox<AnonymityMethods> anonymityMethod;
	
	private JPanel anonMethodPanel = null;
	
	private LogfileTableModel logfileTM;
	
	/** FileFilter to choose only ".log" files from FileChooser */
	private FileFilter logFileFilter_;
	
	/**
	 * Instantiates a new anonymize data dialog.
	 */
	public AnonymizeDataDialog(){
		super(VanetSimStart.getMainFrame(),Messages.getString("AnonymizeDataDialog.title"), false);

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
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);

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
		
		/* Anonymity method combo box */
		c2.gridy++;
		c2.gridx = 0;
		rTop.add(new JSeparator(), c2);
		c2.gridy++;
		rTop.add(new JLabel(Messages.getString("AnonymizeDataDialog.anonymityMethod")), c2);
		c2.gridy++;
		anonymityMethod = new JComboBox<>();
		/* fill combobox with available methods */
		getAvailableAnonMethods(anonymityMethod);
		anonymityMethod.addItemListener(this);
		rTop.add(anonymityMethod, c2);
		
		/* Panel for anonymity method dependend swing items */
		c2.gridx = 0;
		c2.gridy++;
		c2.gridheight = 3;
		anonMethodPanel = new JPanel(new GridBagLayout());
		anonMethodPanel.setVisible(false);
		createPanelForSelectedAnonMethod();
		rTop.add(anonMethodPanel, c2);
		
		add(rTop, c);
		
		/* right bottom panel */
		c.gridy++;
		rBot = new JPanel(new GridBagLayout());
		/* Contraints for right top jpanel */
		GridBagConstraints c3 = new GridBagConstraints();
		c3.fill = GridBagConstraints.BOTH;
		c3.anchor = GridBagConstraints.PAGE_START;
		c3.weightx = 0.0;
		c3.weighty = 0;
		c3.gridx = 0;
		c3.gridy = 0;
		c3.insets = new Insets(5,5,5,5);
		
		//to consume the rest of the space
		c3.weighty = 1.0;
		JPanel space = new JPanel();
		space.setOpaque(false);
		rBot.add(space, c3);
		
		c3.weighty = 0.0;
		c3.gridy++;
		rBot.add(new JSeparator(), c3);
		c3.gridy++;
		rBot.add(new JLabel(Messages.getString("AnonymizeDataDialog.outputLogfilePath")), c3);
		c3.gridy++;
		outputLogfilePath = new JFormattedTextField();
		outputLogfilePath.setValue(System.getProperty("user.dir"));
//		outputLogfilePath.setPreferredSize(new Dimension(120,20));
		outputLogfilePath.setName("outputLogfilePath");
		outputLogfilePath.setCaretPosition(outputLogfilePath.getText().length());
		outputLogfilePath.setToolTipText(outputLogfilePath.getText());
		outputLogfilePath.addFocusListener(this);
		rBot.add(outputLogfilePath, c3);
		
		add(rBot, c);
		
		c.weightx = 0;
		c.weighty = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy++;
		info = new JLabel(Messages.getString("AnonymizeDataDialog.info.inputfile"));
		info.setForeground(Color.RED);
		info.setFont(new Font(info.getName(), Font.PLAIN, 12));
//		info.setBorder(BorderFactory.createLineBorder(Color.black));
		add(info, c);
		
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

		/* enable deleting of rows */
        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap inputMap = table.getInputMap(condition);
        ActionMap actionMap = table.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		actionMap.put("delete", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (logfileTM.getRowCount() > 0 && table.getSelectedRowCount() > 0) {
					/* remove all selected rows */
					for (int i : table.getSelectedRows()) {
						//TODO [MH] removing of many lines takes too long
						logfileTM.removeRow(i);
					}
					/* tell the UI that there was a change in the data */
					logfileTM.fireTableRowsDeleted(
						table.getSelectedRows()[0], 
						table.getSelectedRows()[table.getSelectedRowCount() - 1]
					);
				}
//				table.editingCanceled(null);
			}
		});
	}
	
	/**
	 * fill the combobox with all avaiable anonymity methods
	 * @param box	the combobox to be filled
	 */
	private void getAvailableAnonMethods(JComboBox<AnonymityMethods> box) {
		for (AnonymityMethods method : AnonymityMethods.values()) {
			box.addItem(method);
		}
	}
	
	private void createPanelForSelectedAnonMethod() {
		System.out.println("here" + anonymityMethod.getSelectedIndex());
		/* if there is no item selected, do nothing */
		if (anonymityMethod.getSelectedIndex() == -1) {
			return;
		}
		/* get selected method */
		AnonymityMethods method = (AnonymityMethods) anonymityMethod.getSelectedItem();
		/* delete active panel content */
		anonMethodPanel.removeAll();
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(5,5,5,5);
		
		switch (method) {
		case AGGREGATION:
			System.out.println("lol3");
			break;
		case REMOVING:
			System.out.println("lol1");
			c.gridheight = 1;
			c.gridwidth = 1;
			anonMethodPanel.add(new JLabel(Messages.getString("AnonymizeDataDialog.anonymityMethod.removing.method")), c);	
			c.gridy++;
			
			JComboBox<RemovingMethods> removingMethodBox = new JComboBox<>();
			for (RemovingMethods removingMethod : RemovingMethods.values()) {
				removingMethodBox.addItem(removingMethod);
			}
			
			anonMethodPanel.add(removingMethodBox, c);
			anonMethodPanel.setVisible(true);
			
//			inputLogfilePath = new JFormattedTextField();
////			inputLogfilePath.setValue(System.getProperty("user.dir"));
////			inputLogfilePath.setPreferredSize(new Dimension(120,20));
//			inputLogfilePath.setName("inputLogfilePath");
//			inputLogfilePath.setCaretPosition(inputLogfilePath.getText().length());
//			inputLogfilePath.setToolTipText(inputLogfilePath.getText());
//			inputLogfilePath.addFocusListener(this);
//			rTop.add(inputLogfilePath, c);
			
			
			break;
		default:
			System.out.println("lol2");
			break;
		}
		anonMethodPanel.revalidate();
		anonMethodPanel.repaint();
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
			info.setText(Messages.getString("AnonymizeDataDialog.info.formatstr"));
			formatString.setText(Data.getFirstLine(inputLogfilePath.getText()));
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

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			/* for now, the ItemListener is only used for anonymity method, therefore we do not need checks */
			createPanelForSelectedAnonMethod();
//			Object item = e.getItem();
//			System.out.println("herheiuor " + item.getClass());
//			if (item instanceof JComboBox<?>) {
//				System.out.println(((JComboBox<?>) item).getSelectedItem());
//			}
			// do something with object
		}
	}
}