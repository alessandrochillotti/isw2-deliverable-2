package it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options;

public enum FeatureSelection {
	NO_SELECTION,
	BEST_FIRST;
	
	public String toStringCSV() {
		if (this.equals(NO_SELECTION)) {
			return "No";
		} else {
			return "Best First";
		}
	}
}
