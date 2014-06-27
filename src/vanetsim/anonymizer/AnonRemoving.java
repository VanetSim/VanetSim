package vanetsim.anonymizer;

import java.util.Arrays;

import javax.swing.JLabel;

import vanetsim.localization.Messages;

public class AnonRemoving extends AnonymityMethod {
	private String remMethod;
	private String submethod;
	private double value;

	public AnonRemoving(Data data, JLabel info) {
		super(data, info);
	}

	@Override
	public int anonymize(String[] params) {
		if (!isValid(params)) {
			return -1;
		}
				
		if (submethod == Messages.getString("AnonymizeDataDialog.anonymityMethod.removing.percentage")) {
			removePercentage();
		}
		
		return 0;
	}
	
	private boolean isValid(String[] params) {
		/* params array:
		 * 0: removing method [Random, Top, Bottom]
		 * 1: submethod [Percentage, Distance]
		 * 2: Value of 1
		 */
		remMethod = params[0];
		submethod = params[1];
		try {
			value = Double.parseDouble(params[2]) / 100;
		} catch (NumberFormatException e) {
			return false;
		}

		if (submethod == Messages.getString("AnonymizeDataDialog.anonymityMethod.removing.percentage")) {
			/* if submethod is percentage, then the value needs to be between 0 and 1 */
			if (value < 0.0 || value > 1.0) {
				info.setText(Messages.getString("AnonymizeDataDialog.anonymityMethod.removing.fail1"));
				return false;
			}
		} else if (submethod == Messages.getString("AnonymizeDataDialog.anonymityMethod.removing.distance")) {
			//TODO implement "removing distance"
			info.setText(Messages.getString("not implemented yet"));
			return false;
		}

		info.setText("");
		return true;
	}
	
	private void removePercentage() {
		/* subMethod percentage: */
		/* get total number of elements */
		int total = data.getRowCount();
		/* get absolute number of elements to remove */
		int count = (int) ((double)total * value);

		if (remMethod == RemovingMethods.RANDOM.toString()) {
			for (int i = 0; i < count; i++) {
				/* scale random() to a range of 0 to the total number,
				 * which decreases every round */
				data.removeRow((int)(Math.random() * (total - i)));
			}
		} else if (remMethod == RemovingMethods.TOP.toString()) {
			for (int i = 0; i < count; i++) {
				/* remove always the first */
				data.removeRow(0);
			}
		} else if (remMethod == RemovingMethods.BOTTOM.toString()) {
			for (int i = total - 1; i >= total - count; i--) {
				data.removeRow(i);
			}
		}
	}
}
