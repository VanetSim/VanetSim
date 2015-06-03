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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import vanetsim.localization.Messages;

/**
 * This class creates the control elements on the right side of the DrawingArea. The control panels itself are
 * packed into a <code>JTabbedPane</code> and can be found in separate classes.
 */
public final class MainControlPanel extends JPanel implements ChangeListener{

	/** The necessary constant for serializing. */
	private static final long serialVersionUID = 5716484200018988368L;
	
	/** The pane for the tabs. */
	private final JTabbedPane tabbedPane_ = new JTabbedPane();
	
	/** The simulate control panel. */
	private final SimulateControlPanel simulatePanel_ = new SimulateControlPanel();

	/** The edit control panel. */
	private final EditControlPanel editPanel_ = new EditControlPanel();
	
	/** The reporting control panel. */
	private final ReportingControlPanel reportingPanel_ = new ReportingControlPanel();
	
	/** The about control panel. */
	private final AboutControlPanel aboutPanel_ = new AboutControlPanel();
	
	/** The file filter for XML files. Used in the central file chooser. */
	private final FileFilter xmlFileFilter_;
	
	/** The file filter for OpenStreetMap files. Used in the central file chooser. */
	private final FileFilter osmFileFilter_;	
	
	/** A central <code>JFileChooser</code> so that the directory stays saved. */
	private JFileChooser fileChooser_ = null;	

	/** flag to toggle the bar*/
	private boolean hideBar_ = false;
	
	/**
	 * Constructor for the main control panel.
	 */
	public MainControlPanel(){
		java.awt.EventQueue.invokeLater ( new Runnable() {

			public void run() {
				JFileChooser tmpChooser = new JFileChooser();
				tmpChooser.setMultiSelectionEnabled(false);
				fileChooser_ = tmpChooser;	//now it's ready and we can set the global filechooser

			}
		} );

		xmlFileFilter_ = new FileFilter(){
			public boolean accept(File f) {
				if (f.isDirectory()) return true;
				return f.getName().toLowerCase().endsWith(".xml"); //$NON-NLS-1$
			}
			public String getDescription () { 
				return Messages.getString("MainControlPanel.xmlFiles") + " (*.xml)"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		};
		
		osmFileFilter_ = new FileFilter(){
			public boolean accept(File f) {
				if (f.isDirectory()) return true;
				return f.getName().toLowerCase().endsWith(".osm"); //$NON-NLS-1$
			}
			public String getDescription () { 
				return Messages.getString("MainControlPanel.openStreetMapFiles") + " (*.osm)"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		};
		
		// set some layout settings and a fixed size
		setLayout(new GridBagLayout());		
		Dimension size = simulatePanel_.getPreferredSize();
		size.setSize(size.width + 155, size.height < 800? 800: size.height);
		setMinimumSize(new Dimension(size.width+50,400));
		editPanel_.setMinimumSize(size);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		
		
		tabbedPane_.addTab(Messages.getString("MainControlPanel.simulateTab"), simulatePanel_); //$NON-NLS-1$
		tabbedPane_.addTab(Messages.getString("MainControlPanel.editTab"), editPanel_); //$NON-NLS-1$
		tabbedPane_.addTab(Messages.getString("MainControlPanel.reporting"), reportingPanel_); //$NON-NLS-1$
		tabbedPane_.addTab(Messages.getString("MainControlPanel.about"), aboutPanel_); //$NON-NLS-1$
		tabbedPane_.setMinimumSize(new Dimension(size.width+50, 400));
		tabbedPane_.addChangeListener(this);
	
		UIManager.put("TabbedPane.contentOpaque", false);
		JScrollPane scrollPane = new JScrollPane(tabbedPane_);
		tabbedPane_.setOpaque(false);
		simulatePanel_.setOpaque(false);
		editPanel_.setOpaque(false);
		reportingPanel_.setOpaque(false);
		aboutPanel_.setOpaque(false);
		
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);

		JViewport jv = scrollPane.getViewport();  
		jv.setViewPosition(new Point(0,0)); 
		scrollPane.getVerticalScrollBar().setValue(0);
		add(scrollPane, c);
	}

	/**
	 * Gets the central <code>JFileChooser</code>. If it does not exist, it waits until it's created
	 * in the separate thread started from the constructor.
	 * 
	 * @return the file chooser
	 */
	public JFileChooser getFileChooser(){
		if(fileChooser_ == null){	// wait until it's ready
			do{
				try{
					Thread.sleep(1);
				} catch (Exception e){};
			} while (fileChooser_ == null);
		}
		return fileChooser_;
	}
	
	/**
	 * Changes the available file choosers of the central <code>JFileChooser</code>.
	 * 
	 * @param acceptAll	adds a file chooser to select all files
	 * @param acceptXML	adds a file chooser to select XML files
	 * @param acceptOSM	adds a file chooser to select OpenStreetMap-files (*.osm)
	 */
	public void changeFileChooser(boolean acceptAll, boolean acceptXML, boolean acceptOSM){
		if(fileChooser_ == null){	// wait until it's ready
			do{
				try{
					Thread.sleep(1);
				} catch (Exception e){};
			} while (fileChooser_ == null);
		}
		fileChooser_.resetChoosableFileFilters();
		if(acceptAll) fileChooser_.setAcceptAllFileFilterUsed(true);
		else fileChooser_.setAcceptAllFileFilterUsed(true);
		if(acceptOSM){				
			fileChooser_.addChoosableFileFilter(osmFileFilter_);
			fileChooser_.setFileFilter(osmFileFilter_);
		}
		if(acceptXML){
			fileChooser_.addChoosableFileFilter(xmlFileFilter_);
			fileChooser_.setFileFilter(xmlFileFilter_);
		}
	}

	/**
	 * Gets the control panel on the simulation tab.
	 * 
	 * @return the control panel
	 */
	public SimulateControlPanel getSimulatePanel(){
		return simulatePanel_;
	}

	/**
	 * Gets the control panel on the edit tab.
	 * 
	 * @return the control panel
	 */
	public EditControlPanel getEditPanel(){
		return editPanel_;
	}
	
	/**
	 * Gets the control panel on the reporting tab.
	 * 
	 * @return the control panel
	 */
	public ReportingControlPanel getReportingPanel(){
		return reportingPanel_;
	}
	
	/**
	 * Gets the currently selected tab component.
	 * 
	 * @return	the currently selected tab component
	 */
	public Component getSelectedTabComponent(){
		return tabbedPane_.getSelectedComponent();
	}
	
	
	public void activateEditPane(){
		tabbedPane_.setSelectedIndex(1);
	}
	
	
	/**
	 * An implemented <code>ChangeListener</code> for the tabs.
	 * 
	 * @param e a <code>ChangeEvent</code>
	 */	
	public void stateChanged(ChangeEvent e){
		
		if(tabbedPane_.getSelectedComponent() instanceof ReportingControlPanel){
			reportingPanel_.setActive(true);
		} else {
			reportingPanel_.setActive(false);
		}
	}
	
	/**
	 * resize the side bar
	 */
	public void resizeSideBar(boolean maxOut){
		if(maxOut){
			Dimension newSize = simulatePanel_.getPreferredSize();
			newSize.setSize(newSize.width + 300, newSize.height < 800? 800: newSize.height);
			setMinimumSize(new Dimension(newSize.width+50,400));
			editPanel_.setMinimumSize(newSize);
		}
		else{
			Dimension newSize = simulatePanel_.getPreferredSize();
			newSize.setSize(newSize.width + 120, newSize.height < 800? 800: newSize.height);
			setMinimumSize(new Dimension(newSize.width+50,400));
			editPanel_.setMinimumSize(newSize);
		}
	
		this.revalidate();
		this.repaint();
	}
	
	public void tooglePanel(){
		Dimension size = simulatePanel_.getPreferredSize();
		hideBar_ = !hideBar_;
		if(hideBar_){
			size.setSize(0, size.height < 800? 800: size.height);
		}
		else{
			size.setSize(size.width+155, size.height < 800? 800: size.height);
		}
		setMinimumSize(new Dimension(size.width+50,400));
		editPanel_.setMinimumSize(new Dimension(size.width, size.height));
		this.revalidate();
		this.repaint();
	}
	
	/**
	 * switch to specific tab
	 */
	public void switchToTab(int tabNr){
		tabbedPane_.setSelectedIndex(tabNr);
	}
}