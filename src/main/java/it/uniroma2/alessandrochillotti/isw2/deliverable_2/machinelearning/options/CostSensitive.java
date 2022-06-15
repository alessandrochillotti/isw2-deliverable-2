package it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options;

public enum CostSensitive {
	NO_COST_SENSITIVE,
	SENSITIVE_THRESHOLD,
	SENSITIVE_LEARNING;
	
	public String toStringCSV() {
		if (this.equals(NO_COST_SENSITIVE)) {
			return "No";
		} else if (this.equals(SENSITIVE_THRESHOLD)) {
			return "Sensitive threshold";
		} else {
			return "Sensitive learning";
		}
	}
}
