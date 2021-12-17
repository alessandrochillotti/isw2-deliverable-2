package it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options.Classifiers;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options.CostSensitive;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.RandomForest;

public class Run {
	private Dataset data;
	private Classifiers classifier;
	
	public Run(Dataset data, Classifiers classifier) {
		this.data = data;
		this.classifier = classifier;
	}
	
	public Evaluation perform() throws Exception {
		Profile profile = data.getProfile();
		Classifier actualClassifier;
		
		if (classifier.equals(Classifiers.NAIVE_BAYES)) {
			actualClassifier = new NaiveBayes();
		} else if (classifier.equals(Classifiers.IBK)) {
			actualClassifier = new IBk();
		} else {
			actualClassifier = new RandomForest();
		}
		
		if (!profile.getSensitive().equals(CostSensitive.NO_COST_SENSITIVE)) {
			CostMatrix costMatrix = getCostMatrix(10.0, 1.0);
			CostSensitiveClassifier costSensitive = new CostSensitiveClassifier();
			
			costSensitive.setClassifier(actualClassifier);
			costSensitive.setCostMatrix(costMatrix);
			
			costSensitive.setMinimizeExpectedCost(profile.getSensitive().equals(CostSensitive.SENSITIVE_THRESHOLD));
		}
		
		actualClassifier.buildClassifier(data.getTrainingSet());
				
		Evaluation evaluation = new Evaluation(data.getTestingSet());
				
		evaluation.evaluateModel(actualClassifier, data.getTestingSet());
		
		return evaluation;
	}
	
	private CostMatrix getCostMatrix(double falseNegativeWeigth, double falsePositiveWeigth) {
		CostMatrix costMatrix = new CostMatrix(2);
		
		costMatrix.setCell(0, 0, 0.0);
		costMatrix.setCell(0, 1, falseNegativeWeigth);
		costMatrix.setCell(1, 0, falsePositiveWeigth);
		costMatrix.setCell(1, 1, 0.0);
		
		return costMatrix;
	}
	
	public Dataset getData() {
		return data;
	}
	
	public Classifiers getClassifier() {
		return classifier;
	}
}
