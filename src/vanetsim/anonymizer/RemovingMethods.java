package vanetsim.anonymizer;

import vanetsim.localization.Messages;

public enum RemovingMethods {
	RANDOM {
		public String toString() {
			return Messages.getString("AnonymizeDataDialog.anonymityMethod.removing.random");
		}	
	},
	
	TOP {
		public String toString() {
			return Messages.getString("AnonymizeDataDialog.anonymityMethod.removing.top");
		}	
	},
	
	BOTTOM {
		public String toString() {
			return Messages.getString("AnonymizeDataDialog.anonymityMethod.removing.bottom");
		}
	}
}
