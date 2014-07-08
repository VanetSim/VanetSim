package vanetsim.anonymizer;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import vanetsim.localization.Messages;

public class AnonAggregationPanel extends AnonMethodPanel {
	private static final long serialVersionUID = 754632178761082570L; //Was ist das??
	
	private JTextField numberField;
	private JLabel unit;
	
	public AnonAggregationPanel() {
		setLayout(new GridBagLayout());
		/* 
		 * We have 5 components and choose a WxH=2x3 layout 
		 * - all components have a height of 1
		 * - weights stand next to the scetch
		 * 
		 * |---------------------------|
		 * |  method | methBox|        | 1,1,1
		 * |---------------------------|
		 * |  chooser| input  | unit   | 1,1,1
		 * |---------------------------|
		 */
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(5,5,5,5);
		
				
		/* chooser stuff */
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		unit = new JLabel(Messages.getString("AnonymizeDataDialog.anonymityMethod.aggregation.numOfClusters"));
		add(unit, c);
		c.gridx++;
		numberField = new JTextField("20", 2);
		add(numberField, c);
		c.gridy++;
		add(new JLabel(Messages.getString("AnonymizeDataDialog.anonymityMethod.aggregation.hint")), c);	
		
		
//		setBorder(BorderFactory.createLineBorder(Color.black));
	}

	@Override
	public String[] getParameters() {
		String[] params = new String[1];
		params[0] = numberField.getText();		
		return params;
	}

}
