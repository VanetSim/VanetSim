package vanetsim.anonymizer;

import vanetsim.localization.Messages;

public enum AggregationMethods {
	RANDOM {
		public String toString() {
			return Messages.getString("AnonymizeDataDialog.anonymityMethod.aggregation.numOfClusters");
		}	
	}
}