package vanetsim.anonymizer;

import vanetsim.localization.Messages;

public enum AnonymityMethods {
	REMOVING {
		public String toString() {
			return Messages.getString("AnonymizeDataDialog.anonymityMethod.removing");
		}	
	},
	
	AGGREGATION {
		public String toString() {
			return Messages.getString("AnonymizeDataDialog.anonymityMethod.aggregation");
		}	
	}
}