package it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options;

public enum Sampling {
	NO_SAMPLING,
	OVERSAMPLING,
	UNDERSAMPLING,
	SMOTE;
	
	public String toStringCSV() {
		if (this.equals(NO_SAMPLING)) {
			return "No";
		} else if (this.equals(OVERSAMPLING)) {
			return "Oversampling";
		} else if (this.equals(UNDERSAMPLING)) {
			return "Undersampling";
		} else {
			return "Smote";
		}
	}
}
