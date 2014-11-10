package vanetsim.gui.controlpanels;


import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import vanetsim.ErrorLog;
import vanetsim.VanetSimStart;
import vanetsim.VanetSimStarter;
import vanetsim.gpstracing.GPSPrecalculation;
import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.gui.helpers.ReRenderManager;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.scenario.Scenario;
import vanetsim.simulation.SimulationMaster;
import vanetsim.simulation.WorkerThread;



public final class GPSControlPanel extends JPanel implements ActionListener, ChangeListener, ItemListener{

	/** A panel which includes the buttons for loading the GPS the simulation. Uses a <code>CardLayout</code>. */
	private final JPanel LoadJPanel_;
	
	/** The input field for the minimum Line in Parsing File. */
	private final JFormattedTextField minLine_;
	
	/** The input field for the maximum Line in Parsing File.. */
	private final JFormattedTextField maxLine_;
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** A Label for the GPS Traces ComboBox. */
	private JLabel chooseGPSTracesLabel_;

	/** A JComboBox to switch between traffic models. */
	private JComboBox<String> chooseGPSTraces_;
	
	/** A Label for the GOS Info Text Box */
	private JLabel Infobox_;
	
	public GPSControlPanel() {
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
				
				
				
				//Auswahlbereich Traces
				c.gridx = 0;
				chooseGPSTracesLabel_ = new JLabel(Messages.getString("GPSControlPanel.ChooseGPSTracesPanel")); //$NON-NLS-1$
				++c.gridy;
				add(chooseGPSTracesLabel_,c);
				chooseGPSTraces_ = new JComboBox<String>();
				chooseGPSTraces_.setActionCommand("chooseGPSTraces");
				chooseGPSTraces_.addItem("GPS Traces Hamburg"); //TODO:Welche Karte
				chooseGPSTraces_.addItem("GPS Traces New York");
				chooseGPSTraces_.addItem("GPS Traces Shanghai");
				chooseGPSTraces_.addItem("GPS Traces San Francisco");
				chooseGPSTraces_.addActionListener(this);
				c.gridx = 1;
				add(chooseGPSTraces_, c);
				
				c.gridx = 0;
				JLabel jLabel1 = new JLabel(Messages.getString("GPSControlPanel.minLine")); //$NON-NLS-1$
				++c.gridy;
				add(jLabel1,c);		
				minLine_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
				minLine_.setValue(0);
				minLine_.setPreferredSize(new Dimension(60,20));
				c.gridx = 1;
				add(minLine_,c);
				c.gridx = 2; 
				
				c.gridx = 0;
				jLabel1 = new JLabel(Messages.getString("GPSControlPanel.maxLine")); //$NON-NLS-1$
				++c.gridy;
				add(jLabel1,c);		
				maxLine_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
				maxLine_.setValue(250);
				maxLine_.setPreferredSize(new Dimension(60,20));;
				c.gridx = 1;
				add(maxLine_,c);
				
				//TODO: Load Button
				LoadJPanel_ = new JPanel(new CardLayout());

				LoadJPanel_.add(ButtonCreator.getJButton("start.png", "load", Messages.getString("GPSControlPanel.load"), this), "load"); 
				++c.gridy;
				c.gridwidth = 1;
				add(LoadJPanel_, c);
				
				
				//to consume the rest of the space
				c.weighty = 1.0;
				++c.gridy;
				JPanel space = new JPanel();
				space.setOpaque(false);
				add(space, c);

				
				
				
	}

	
	public void setLoading(){
		CardLayout cl = (CardLayout)(LoadJPanel_.getLayout());
		cl.show(LoadJPanel_, "load"); //$NON-NLS-1$
	}
	
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		String command = arg0.getActionCommand();
		
		
		int minLineValue = ((Number)minLine_.getValue()).intValue();
		int maxLineValue = ((Number)maxLine_.getValue()).intValue();		
		
		// 4 ->San Francisco ,  3 -> Shanghai, 5 -> New York -> 6 Hamburg
		if("Trace File".equals(command)){	
			//display Traces selection related gui elements
			if(((String)chooseGPSTraces_.getSelectedItem()).equals("GPS Traces Hamburg")) WorkerThread.setSimulationMode_(6);
			else if(((String)chooseGPSTraces_.getSelectedItem()).equals("GPS Traces New York")) WorkerThread.setSimulationMode_(5);
			else if(((String)chooseGPSTraces_.getSelectedItem()).equals("GPS Traces Shanghai")) WorkerThread.setSimulationMode_(3);
			else if(((String)chooseGPSTraces_.getSelectedItem()).equals("GPS Traces San Francisco")) WorkerThread.setSimulationMode_(4);
		}
			chooseGPSTracesLabel_.setVisible(true);
			chooseGPSTraces_.setVisible(true);

		if("chooseGPSTraces".equals(command)){
			
			if(((String)chooseGPSTraces_.getSelectedItem()).equals("GPS Traces Hamburg")) WorkerThread.setSimulationMode_(6);
			else if(((String)chooseGPSTraces_.getSelectedItem()).equals("GPS Traces New York")) WorkerThread.setSimulationMode_(5);
			else if(((String)chooseGPSTraces_.getSelectedItem()).equals("GPS Traces Shanghai")) WorkerThread.setSimulationMode_(3);
			else if(((String)chooseGPSTraces_.getSelectedItem()).equals("GPS Traces San Francisco")) WorkerThread.setSimulationMode_(4);
		}
		if ("load".equals(command)){ //$NON-NLS-1$
			if(((String)chooseGPSTraces_.getSelectedItem()).equals("GPS Traces Hamburg")){ 
				GPSPrecalculation.openMap(6); 
				System.out.println("Starting Parsing now");
				//GPSPrecalculation.runParser(6, 0, 0);
				GPSPrecalculation.precalculateRoute(6, 0, 0);
				}
			else if(((String)chooseGPSTraces_.getSelectedItem()).equals("GPS Traces New York")){ 
				GPSPrecalculation.openMap(5); 
				//GPSPrecalculation.runParser(5, minLineValue, maxLineValue);
				System.out.println("Starting Parsing now");
				GPSPrecalculation.precalculateRoute(5, minLineValue, maxLineValue);
				}
			else if(((String)chooseGPSTraces_.getSelectedItem()).equals("GPS Traces Shanghai")){ 
				GPSPrecalculation.openMap(3); 
				System.out.println("Starting Parsing now");
				//GPSPrecalculation.runParser(3, minLineValue, maxLineValue);
				GPSPrecalculation.precalculateRoute(3, minLineValue, maxLineValue);
				}
			else if(((String)chooseGPSTraces_.getSelectedItem()).equals("GPS Traces San Francisco")){ 
				GPSPrecalculation.openMap(4); 
				System.out.println("Starting Parsing now");
				//GPSPrecalculation.runParser(4, minLineValue, maxLineValue);
				GPSPrecalculation.precalculateRoute(4, minLineValue, maxLineValue);
				}
		

			
		}
	}
	
	
	//TODO: Rewrite - is this actually needed?
	public void LoadGPSSimulation(){
			CardLayout cl = (CardLayout)(LoadJPanel_.getLayout());
			//TODO: Do all steps here
		}		
	

	@Override
	public void itemStateChanged(ItemEvent e) {
		
	}


	@Override
	public void stateChanged(ChangeEvent e) {
		
	}
	
}
