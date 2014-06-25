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

public class AnonRemovingPanel extends AnonMethodPanel {
	private static final long serialVersionUID = 754632178761082570L;
	
	private JComboBox<RemovingMethods> removingMethodBox;
	private JComboBox<String> chooserBox;
	private JTextField numberField;
	private JLabel unit;
	
	public AnonRemovingPanel() {
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
		
		/* method stuff */
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		add(new JLabel(Messages.getString("AnonymizeDataDialog.anonymityMethod.removing.method")), c);	
		c.gridx++;
		removingMethodBox = new JComboBox<>();
		for (RemovingMethods removingMethod : RemovingMethods.values()) {
			removingMethodBox.addItem(removingMethod);
		}
		add(removingMethodBox, c);
		
		/* chooser stuff */
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		chooserBox = new JComboBox<>();
		chooserBox.addItem(Messages.getString("AnonymizeDataDialog.anonymityMethod.removing.percentage"));
		chooserBox.addItem(Messages.getString("AnonymizeDataDialog.anonymityMethod.removing.distance"));
		add(chooserBox, c);
		c.gridx++;
		numberField = new JTextField("10");
		add(numberField, c);
		c.gridx++;
		unit = new JLabel("%");
		add(unit, c);
		
		setBorder(BorderFactory.createLineBorder(Color.black));
	}

	@Override
	public String[] getParameters() {
		String[] params = new String[3];
		params[0] = removingMethodBox.getSelectedItem().toString();
		params[1] = chooserBox.getSelectedItem().toString();
		params[2] = numberField.getText();
		
		return params;
	}

}
