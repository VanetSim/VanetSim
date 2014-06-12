package vanetsim.anonymizer;

import javax.swing.JLabel;

public abstract class AnonymityMethod {
	/* The data on which to operate on */
	Data data;
	/* The info line to display a hint if something is wrong (i.e. wrong parameters) */
	JLabel info;
	
	public AnonymityMethod(Data data, JLabel info) {
		this.data = data;
		this.info = info;
	}
	
	public abstract int anonymize(String[] params);
}
