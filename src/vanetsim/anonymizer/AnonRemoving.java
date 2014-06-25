package vanetsim.anonymizer;

import java.util.Arrays;

import javax.swing.JLabel;

import vanetsim.localization.Messages;

public class AnonRemoving extends AnonymityMethod {

	public AnonRemoving(Data data, JLabel info) {
		super(data, info);
	}

	@Override
	public int anonymize(String[] params) {
		// TODO entry point of anonymization.
		// from here you have access to the data and can perform operations on it.
		// If some parameter are missing, write it into 'info' and return with non-zero number
		if (!isValid()) {
			return -1;
		}
		
		/* params array:
		 * 0: removing method [Random, Top, Bottom]
		 * 1: submethod [Percentage, Distance]
		 * 2: Value of 1
		 */
		String remMethod = params[0];
		String submethod = params[1];
		double value = Double.parseDouble(params[2]);
		
		if (remMethod == RemovingMethods.RANDOM.toString()) {
			if (submethod == Messages.getString("AnonymizeDataDialog.anonymityMethod.removing.percentage")) {
				removePercentageRandom(value);
			}
		}
		
		System.out.println(Arrays.toString(params));
		
		return 0;
	}
	
	private boolean isValid() {
		//TODO [MH]
		return true;
	}
	
	private void removePercentageRandom(double value) {
		/* convert % value to a value between 0 and 1 */
		value /= 100;
		/* get total number of elements */
		int total = data.getRowCount();
		/* get absolute number of elements to remove */
		int count = (int) ((double)total * value);
		
		for (int i = 0; i < count; i++) {
			/* scale random() to a range of 0 to the total number */
			data.removeRow((int)Math.random() * total);
		}
	}
}
