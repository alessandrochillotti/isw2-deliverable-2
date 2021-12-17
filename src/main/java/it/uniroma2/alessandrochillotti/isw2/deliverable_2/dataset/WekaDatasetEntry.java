package it.uniroma2.alessandrochillotti.isw2.deliverable_2.dataset;

import java.util.Locale;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.Run;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options.Classifiers;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options.CostSensitive;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options.FeatureSelection;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options.Sampling;
import weka.classifiers.Evaluation;

public class WekaDatasetEntry {
	
	private String dataset;
	private int trainingRelease;
	private double trainingPercentage;
	private double defectiveInTrainingPercentage;
	private double defectiveInTestingPercentage;
	private Classifiers classifier;
	private Sampling balancing;
	private FeatureSelection featureSelection;
	private CostSensitive sensitivity;
	private int truePositive;
	private int falsePositive;
	private int trueNegative;
	private int falseNegative;
	private double precision;
	private double recall;
	private double auc;
	private double kappa;
	
	public WekaDatasetEntry(String dataset, Evaluation evaluation, Run run) {
		this.dataset = dataset;
		this.trainingRelease = run.getData().getNumberTrainingRelease();
		this.trainingPercentage = run.getData().getTrainingPercentage();
		this.defectiveInTrainingPercentage = run.getData().getDefectiveInTrainingPercentage();
		this.defectiveInTestingPercentage = run.getData().getDefectiveInTestingPercentage();
		this.classifier = run.getClassifier();
		this.balancing = run.getData().getProfile().getSampling();
		this.featureSelection = run.getData().getProfile().getSelection();
		this.sensitivity = run.getData().getProfile().getSensitive();
		this.truePositive = (int) evaluation.numTruePositives(0);
		this.falsePositive = (int) evaluation.numFalsePositives(0);
		this.trueNegative = (int) evaluation.numTrueNegatives(0);
		this.falseNegative = (int) evaluation.numFalseNegatives(0);
		this.precision = evaluation.precision(0);
		this.recall = evaluation.recall(0);
		this.auc = evaluation.areaUnderROC(0);
		this.kappa = evaluation.kappa();
	}
	
	public String toCSV() {
		return String.format("%s,%d,%s,%s,%s,%s,%s,%s,%s,%d,%d,%d,%d,%s,%s,%s,%s%n", 
				dataset, trainingRelease, String.format(Locale.US, "%.6f", trainingPercentage), String.format(Locale.US, "%.6f", defectiveInTrainingPercentage), 
				String.format(Locale.US, "%.6f", defectiveInTestingPercentage), classifier.toStringCSV(), balancing.toStringCSV(), featureSelection.toStringCSV(),
				sensitivity.toStringCSV(), truePositive, falsePositive, trueNegative, falseNegative,
				String.format(Locale.US, "%.6f", precision), String.format(Locale.US, "%.6f", recall),
				String.format(Locale.US, "%.6f", auc), String.format(Locale.US, "%.6f", kappa)
		);
	}
}
