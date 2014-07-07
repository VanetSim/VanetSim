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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import vanetsim.VanetSimStart;
import vanetsim.anonymizer.AnonMethodPanel;
import vanetsim.anonymizer.AnonRemoving;
import vanetsim.anonymizer.AnonRemovingPanel;
import vanetsim.anonymizer.AnonymityMethod;
import vanetsim.anonymizer.AnonymityMethodsEnum;
import vanetsim.anonymizer.Data;
import vanetsim.anonymizer.LogfileTableModel;
import vanetsim.anonymizer.RemovingMethods;
import vanetsim.localization.Messages;

/**
 * A dialog to set map parameters.
 */
public final class AnonymizeDataDialog extends JDialog implements ActionListener, FocusListener, ItemListener {
	
	/** The necessary constant for serializing. */
	private static final long serialVersionUID = 4882607093689684208L;
	
	private JPanel rTop;
	
	private JPanel rBot;
	
	private JFormattedTextField inputLogfilePath;
	
	private JButton inputLogfileButton;
	
	private JFormattedTextField outputLogfilePath;

	private JButton outputLogfileButton;
	
	private JTextField formatString;
	
	private JLabel chosenColumnLabel;
	
	private JButton anonymizeButton;
	
	private JButton processButton;
	
	private JButton saveToLogFileButton;
	
	private JLabel info;
	
	private JTable table;
	
	private JScrollPane tableScroll;
	
	private TitledBorder tableBorder;
	
//	private JComboBox<String> selectedColumn;
	
	private JComboBox<AnonymityMethodsEnum> anonymityMethod;
	
	private JPanel anonMethodPanel = null;
	
	private TitledBorder anonMethodPanelBorder;
	
	private LogfileTableModel logfileTM;
	
	/** FileFilter to choose only ".log" files from FileChooser */
	private FileFilter logFileFilter_;
	
	private boolean isPathValid = false;
	
	/**
	 * Instantiates a new anonymize data dialog.
	 */
	public AnonymizeDataDialog(){
		super(VanetSimStart.getMainFrame(),Messages.getString("AnonymizeDataDialog.title"), false);

		setUndecorated(false);
		setLayout(new GridBagLayout());
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		createDialogUI();

		/* define a file filter for log files */
		logFileFilter_ = new FileFilter() {
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(".log"); //$NON-NLS-1$
			}

			public String getDescription() {
				return Messages.getString("EditLogControlPanel.logFiles") + " (*.log)"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		};
	}
	
	
	
	private void setFilePath(JFormattedTextField textField){
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
	
	private void displayRowCountAndSelectedCount() {
		int count;
		if (logfileTM != null) {
			count = logfileTM.getRowCount();
		} else {
			count = 0;
		}
		
		tableBorder.setTitle(String.format(
			Messages.getString("AnonymizeDataDialog.tableBorder"),
			count,
			table.getSelectedRowCount()
		));
	}
	
	private void loadData() {
		logfileTM = new LogfileTableModel();
		if (!logfileTM.initializeTM(inputLogfilePath.getText(), formatString.getText())) {
			/* there was an error with parsing */
			info.setText(Messages.getString("AnonymizeDataDialog.info.parsingNotValid"));
			return;
		}

		table.setModel(logfileTM);
		
		/* print number of elements in the table */
		displayRowCountAndSelectedCount();

		/* enable deleting of rows */
        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap inputMap = table.getInputMap(condition);
        ActionMap actionMap = table.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		actionMap.put("delete", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (logfileTM.getRowCount() > 0 && table.getSelectedRowCount() > 0) {
					/* remove all selected rows */
					Data data = logfileTM.getData();
					for (int i : table.getSelectedRows()) {
						//TODO [MH] removing of many lines takes too long
						data.removeRow(i);
					}
					/* tell the UI that there was a change in the data */
					logfileTM.fireTableRowsDeleted(
						table.getSelectedRows()[0], 
						table.getSelectedRows()[table.getSelectedRowCount() - 1]
					);
					displayRowCountAndSelectedCount();
				}
//				table.editingCanceled(null);
			}
		});

		info.setText(Messages.getString("AnonymizeDataDialog.info.chooseAnonymizeMethod"));
	}
	
	/**
	 * fill the combobox with all available anonymity methods
	 * @param box	the combobox to be filled
	 */
	private void getAvailableAnonMethods(JComboBox<AnonymityMethodsEnum> box) {
		for (AnonymityMethodsEnum method : AnonymityMethodsEnum.values()) {
			box.addItem(method);
		}
	}
	
	private void createPanelForSelectedAnonMethod() {
		/* if there is no item selected, do nothing */
		if (anonymityMethod.getSelectedIndex() == -1) {
			return;
		}
		/* get selected method */
		AnonymityMethodsEnum method = (AnonymityMethodsEnum) anonymityMethod.getSelectedItem();
		/* delete active panel content */
		anonMethodPanel.removeAll();
		/* write the new name of the method panel */
		nameMethodPanel();
		
		switch (method) {
		case AGGREGATION:
			break;
		case REMOVING:
//			createRemovingMethodPanel();
			anonMethodPanel.add(new AnonRemovingPanel());
			anonMethodPanel.setVisible(true);
			break;
		default:
			break;
		}
		anonMethodPanel.revalidate();
		anonMethodPanel.repaint();
	}
	
	private void nameMethodPanel() {
		anonMethodPanelBorder.setTitle(
			anonymityMethod.getSelectedItem() + 
			" " + 
			Messages.getString("AnonymizeDataDialog.anonymityMethod.preferences")
		);
	}
	
	private void doPathCheckAndFirstLine() {
		if (inputLogfilePath.getText().equals("") || 
			inputLogfilePath.getText().equals(Messages.getString("AnonymizeDataDialog.inputLogfilePath"))
        ) {
			info.setText(Messages.getString("AnonymizeDataDialog.info.wrongInput"));
		} else {
			String firstLine;
			if ((firstLine = Data.getFirstLine(inputLogfilePath.getText())) == null) {
				info.setText(Messages.getString("AnonymizeDataDialog.info.wrongInput"));
			} else {
				isPathValid = true;
				info.setText(String.format(Messages.getString("AnonymizeDataDialog.info.formatstr"), firstLine));
			}					
		}
	}
	
	private void chooseAnonymizer() {
		AnonymityMethod method = null;
		String[] params = null;
		
		/* check whether data to anonymize is available */
		if (logfileTM == null || logfileTM.getData() == null) {
			info.setText(Messages.getString("AnonymizeDataDialog.info.anonymizeButtonPressed"));
			return;
		}
		
		switch ((AnonymityMethodsEnum) anonymityMethod.getSelectedItem()) {
		case AGGREGATION:
			break;
		case REMOVING:
			method = new AnonRemoving(logfileTM.getData(), info);
			break;
		default:
			/* can not happen */
			assert(false);
			break;
		}
		/* pass parameters of the GUI to the anonymize() method */
		method.anonymize(((AnonMethodPanel)anonMethodPanel.getComponent(0)).getParameters());
		/* after anonymization took place, refresh the table */
		logfileTM.fireTableDataChanged();
		displayRowCountAndSelectedCount();
	}

	/**
	 * An implemented <code>ActionListener</code> which performs the needed actions when the OK-button
	 * is clicked.
	 * 
	 * @param e	an <code>ActionEvent</code>
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(formatString) || e.getSource().equals(processButton)) {
			if (isPathValid) {
				/* we have the file path and format, so we can load the data into the table */
				loadData();
			}
			/* else: do not do anything, because info already says to specify an input file */
		} else if (e.getSource().equals(anonymizeButton)) {
			/* conditions are checked in chooseAnonymizer() */
			chooseAnonymizer();
		} else if (e.getSource().equals(inputLogfileButton)) {
			setFilePath(inputLogfilePath);
			doPathCheckAndFirstLine();
		} else if (e.getSource().equals(outputLogfileButton)) {
			//TODO [MH] output logfile
			setFilePath(outputLogfilePath);
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (e.getSource().equals(inputLogfilePath)) {
			inputLogfilePath.selectAll();
		} else if (e.getSource().equals(formatString)) {
			formatString.selectAll();
		} else if (e.getSource().equals(outputLogfilePath)){
			//TODO [MH] selectAll does not work
			outputLogfilePath.selectAll();
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		/* when the focus is lost, check whether the user typed a valid logfile path and if so, load the first line */
		if (e.getSource().equals(inputLogfilePath)) {
			doPathCheckAndFirstLine();
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			/* for now, the ItemListener is only used for anonymity method, therefore we do not need checks */
			createPanelForSelectedAnonMethod();
		}
	}
	
	/* Helper functions, which are only called once to build the UI */
	/**
	 * create the interface of the whole dialog
	 */
	private void createDialogUI() {
		GridBagConstraints c = new GridBagConstraints();
		/* In which direction to fill a component when resizing */
		c.fill = GridBagConstraints.BOTH;
		/* set outer margin between each component */
		c.insets = new Insets(5,5,5,5);
		/* if a component is smaller than its display area, center it */
		c.anchor = GridBagConstraints.CENTER;
		
		/* 
		 * We have 4 components and choose a WxH=3x3 layout: 
		 * - JTable					2x2
		 * - Right top JPanel		1x1
		 * - right bottom JPanel	1x1
		 * - Info line				3x1
		 * |-------------|--------|
		 * |             |  rTop  |
		 * |    table    |--------|
		 * |             |  rBot  |
		 * |-------------|--------|
		 * |       info line      |
		 * |----------------------|
		 */
				
		/* the jtable to display the logfile content */
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 2;
		/* give equal height to table and rTop,rBot when resizing */
		c.weighty = 0.5;
		/* give more width to the table than to rTop, rBot when resizing */
		c.weightx = 0.8;
		
		table = new JTable();
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);
		table.setFillsViewportHeight(true);
		// Create the scroll pane and add the table to it.
		tableScroll = new JScrollPane(table);
		tableBorder = BorderFactory.createTitledBorder(
			BorderFactory.createEmptyBorder(), 
			String.format(Messages.getString("AnonymizeDataDialog.tableBorder"), 0, 0), 
			TitledBorder.DEFAULT_JUSTIFICATION, 
			TitledBorder.BOTTOM
		);
		tableScroll.setBorder(tableBorder);
		add(tableScroll, c);
		
		/* right top panel stuff */
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		/* give equal height to table and rTop,rBot when resizing */
		c.weighty = 0.5;
		/* give more width to the table than to rTop, rBot when resizing */
		c.weightx = 0.2;
		
		rTop = new JPanel(new GridBagLayout());
		createRightTopPanel();
		add(rTop, c);
		
		/* right bottom panel stuff */
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		/* give equal height to table and rTop,rBot when resizing */
		c.weighty = 0.5;
		/* give more width to the table than to rTop, rBot when resizing */
		c.weightx = 0.2;
		
		/* for the bottom panel we want it to always 'touch' the bottom without resizing in height */
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.PAGE_END;
		
		rBot = new JPanel(new GridBagLayout());
		createRightBottomPanel();
		add(rBot, c);

		/* info line */
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 3;
		c.gridheight = 1;
		/* do not increase height of info line when resizing */
		c.weighty = 0.0;
		/* give all width to the info line, since its the only component in this row */
		c.weightx = 1.0;

		info = new JLabel(Messages.getString("AnonymizeDataDialog.info.inputfile"));
		info.setForeground(Color.RED);
		info.setFont(new Font(info.getName(), Font.PLAIN, 12));
		info.setBorder(BorderFactory.createTitledBorder(Messages.getString("AnonymizeDataDialog.info.info")));
		info.setPreferredSize(new Dimension(0, 55));
		add(info, c);
		
		pack();
		setLocationRelativeTo(VanetSimStart.getMainFrame());
		setVisible(true);
	}
	
	private void createRightTopPanel() {
		/* Contraints for right top jpanel */
		GridBagConstraints c = new GridBagConstraints();
		/* set outer margin between each component */
		c.insets = new Insets(5,5,5,5);
		/* if a component is smaller than its display area, center it */
		c.anchor = GridBagConstraints.CENTER;
		/* In which direction to fill a component when resizing */		
		c.fill = GridBagConstraints.BOTH;
		
		/* 
		 * We have a lot of components and choose a WxH=6x3 layout 
		 * - all components have a height of 1
		 * - weights stand next to the scetch
		 * 
		 * |---------------------------|
		 * |  input  | inTxt  | inBut  | 1,1,1
		 * |---------------------------|
		 * |  format | foTxt  |procBut | 1,1,1
		 * |---------------------------|
		 * |             hline         | 3
		 * |---------------------------|
		 * |  anonM  |anonBox |        | 1,1,1
		 * |---------------------------|
		 * |        anonPanel          | 3
		 * |---------------------------|
		 * |         |doButton|        | 1,1,1
		 * |---------------------------|
		 */
		
		/* input stuff */
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		/* height should only be given to the anonPanel when resizing */
		c.weighty = 0.0;
		/* width should only be given to the text field when resizing */
		c.weightx = 0.0;

		rTop.add(new JLabel(Messages.getString("AnonymizeDataDialog.input")), c);	
		c.gridx++;
		/* width should only be given to the text field when resizing */
		c.weightx = 1.0;
		inputLogfilePath = new JFormattedTextField();
//		inputLogfilePath.setValue(System.getProperty("user.dir"));
//		inputLogfilePath.setPreferredSize(new Dimension(400,10));
		inputLogfilePath.setName("inputLogfilePath");
		inputLogfilePath.setCaretPosition(inputLogfilePath.getText().length());
		inputLogfilePath.setToolTipText(Messages.getString("AnonymizeDataDialog.inputLogfilePath"));
		inputLogfilePath.setText(Messages.getString("AnonymizeDataDialog.inputLogfilePath"));
		inputLogfilePath.addFocusListener(this);
		rTop.add(inputLogfilePath, c);
		c.gridx++;
		/* width should only be given to the text field when resizing */
		c.weightx = 0.0;
		//TODO Load Button graphic
		inputLogfileButton = new JButton("L");
		inputLogfileButton.addActionListener(this);
		rTop.add(inputLogfileButton, c);
		
		/* format stuff */
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		/* height should only be given to the anonPanel when resizing */
		c.weighty = 0.0;
		/* width should only be given to the text field when resizing */
		c.weightx = 0.0;
		
		rTop.add(new JLabel(Messages.getString("AnonymizeDataDialog.format")), c);
		c.gridx++;
		/* width should only be given to the text field when resizing */
		c.weightx = 1.0;
		formatString = new JTextField();
//		formatString.setPreferredSize(new Dimension(120,20));
		formatString.setName("formatString");
		formatString.setText(Messages.getString("AnonymizeDataDialog.formatStringMsg"));
		formatString.setToolTipText(Messages.getString("AnonymizeDataDialog.formatStringMsg"));
		formatString.addActionListener(this);
		formatString.addFocusListener(this);
		rTop.add(formatString, c);
		c.gridx++;
		/* width should only be given to the text field when resizing */
		c.weightx = 0.0;
		//TODO Load Button graphic
		processButton = new JButton("P");
		processButton.addActionListener(this);
		rTop.add(processButton, c);

		/* hline stuff */
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 3;
		c.gridheight = 1;
		/* height should only be given to the anonPanel when resizing */
		c.weighty = 0.0;
		/* width should only be given to the text field when resizing */
		c.weightx = 1.0;
		
		rTop.add(new JSeparator(), c);
		
		//TODO [MH] column chooser: move it into the anonMethodPanel where needed
//		/* column chooser stuff */
//		c.gridx = 0;
//		c.gridy = 3;
//		c.gridwidth = 1;
//		c.gridheight = 1;
//		/* height should only be given to the anonPanel when resizing */
//		c.weighty = 0.0;
//		/* width should only be given to the text field when resizing */
//		c.weightx = 0.0;
//		
//		rTop.add(new JLabel(Messages.getString("AnonymizeDataDialog.column")), c);
//		c.gridx++;
//		/* width should only be given to the text field when resizing */
//		c.weightx = 1.0;
//		selectedColumn = new JComboBox<>();
////		selectedColumn.setPreferredSize(new Dimension(120,20));
//		rTop.add(selectedColumn, c);
		
		
		/* Anonymity method combo box stuff */
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		/* height should only be given to the anonPanel when resizing */
		c.weighty = 0.0;
		/* width should only be given to the text field when resizing */
		c.weightx = 0.0;
		
		rTop.add(new JLabel(Messages.getString("AnonymizeDataDialog.method")), c);
		c.gridx++;
		/* width should only be given to the text field when resizing */
		c.weightx = 1.0;
		anonymityMethod = new JComboBox<>();
		/* fill combobox with available methods */
		getAvailableAnonMethods(anonymityMethod);
		anonymityMethod.addItemListener(this);
		rTop.add(anonymityMethod, c);
		
		/* Panel for anonymity method dependend swing items */
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 3;
		c.gridheight = 1;
		/* height should only be given to the anonPanel when resizing */
		c.weighty = 1.0;
		/* width should only be given to the text field when resizing */
		c.weightx = 1.0;
		
		//this maximizes the inner anonPanels
//		anonMethodPanel = new JPanel(new BorderLayout());
		anonMethodPanel = new JPanel();
		
		anonMethodPanel.setVisible(false);
		anonMethodPanelBorder = BorderFactory.createTitledBorder("");
		/* get the correct name based on the chosen anonymity method */
		nameMethodPanel();
		anonMethodPanel.setBorder(anonMethodPanelBorder);
		createPanelForSelectedAnonMethod();
		rTop.add(anonMethodPanel, c);
		
		/* anonymize button stuff */
		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 1;
		c.gridheight = 1;
		/* height should only be given to the anonPanel when resizing */
		c.weighty = 0.0;
		/* width should only be given to the text field when resizing */
		c.weightx = 1.0;
		
		//TODO Load Button graphic
		anonymizeButton = new JButton("Anonymize!");
		anonymizeButton.addActionListener(this);
		rTop.add(anonymizeButton, c);
	}
	
	private void createRightBottomPanel() {
		/* Contraints for right bot jpanel */
		GridBagConstraints c = new GridBagConstraints();
		/* set outer margin between each component */
		c.insets = new Insets(5,5,5,5);
		/* if a component is smaller than its display area, put it on the bottom part */
		c.anchor = GridBagConstraints.PAGE_END;
		/* In which direction to fill a component when resizing */		
		c.fill = GridBagConstraints.HORIZONTAL;
		
		/* 
		 * We have 5 components and choose a WxH=3x3 layout 
		 * - all components have a height of 1
		 * - weights stand next to the scetch
		 * 
		 * |---------------------------|
		 * |             hline         | 3
		 * |---------------------------|
		 * |  output | outPath| outBut | 1,1,1
		 * |---------------------------|
		 * |         |saveBut |        | 1,1,1
		 * |---------------------------|
		 */
		
		
		//to consume the rest of the space
//		c.weighty = 1.0;
//		JPanel space = new JPanel();
//		space.setOpaque(false);
//		rBot.add(space, c);
		
		/* hline stuff */
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.gridheight = 1;
		/* use the full available width when resizing for hline, outPath and saveButton */
		c.weightx = 1.0;
		/* do not change height when resizing */
		c.weighty = 0.0;
		
		rBot.add(new JSeparator(), c);
		
		/* output stuff */
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		/* use the full available width when resizing for hline, outPath and saveButton */
		c.weightx = 0.0;
		/* do not change height when resizing */
		c.weighty = 0.0;
		
		rBot.add(new JLabel(Messages.getString("AnonymizeDataDialog.output")), c);
		c.gridx++;
		/* use the full available width when resizing for hline, outPath and saveButton */
		c.weightx = 1.0;
		outputLogfilePath = new JFormattedTextField();
		outputLogfilePath.setValue(System.getProperty("user.dir"));
//		outputLogfilePath.setPreferredSize(new Dimension(120,20));
//		outputLogfilePath.setName("outputLogfilePath");
//		outputLogfilePath.setCaretPosition(outputLogfilePath.getText().length());
		outputLogfilePath.setToolTipText(outputLogfilePath.getText());
		outputLogfilePath.addFocusListener(this);
		rBot.add(outputLogfilePath, c);
		c.gridx++;
		/* use the full available width when resizing for hline, outPath and saveButton */
		c.weightx = 0.0;
		//TODO Load Button graphic
		outputLogfileButton = new JButton("L");
		outputLogfileButton.setName("outputLogfileButton");
		outputLogfileButton.addActionListener(this);
		rBot.add(outputLogfileButton, c);
		
		/* save button stuff */
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		/* use the full available width when resizing for hline, outPath and saveButton */
		c.weightx = 1.0;
		/* do not change height when resizing */
		c.weighty = 0.0;
		
		//TODO Load Button graphic
		saveToLogFileButton = new JButton("Save to logfile!");
		saveToLogFileButton.addActionListener(this);
		rBot.add(saveToLogFileButton, c);
	}
}