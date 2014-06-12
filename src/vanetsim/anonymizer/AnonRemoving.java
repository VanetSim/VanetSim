package vanetsim.anonymizer;

import javax.swing.JLabel;

public class AnonRemoving extends AnonymityMethod {

	public AnonRemoving(Data data, JLabel info) {
		super(data, info);
	}

	@Override
	public int anonymize(String[] params) {
		// TODO entry point of anonymization.
		// from here you have access to the data and can perform operations on it.
		// If some parameter are missing, write it into 'info' and return with non-zero number
		
		System.out.println(info.getText());
		return 0;
	}

}
