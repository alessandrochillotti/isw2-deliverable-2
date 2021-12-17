package it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options;

public enum Classifiers {
	RANDOM_FOREST,
	NAIVE_BAYES,
	IBK;
	
	public String toStringCSV() {
		if (this.equals(RANDOM_FOREST)) {
			return "Random Forest";
		} else if (this.equals(NAIVE_BAYES)) {
			return "Naive Bayes";
		} else {
			return "Ibk";
		}
	}
}
