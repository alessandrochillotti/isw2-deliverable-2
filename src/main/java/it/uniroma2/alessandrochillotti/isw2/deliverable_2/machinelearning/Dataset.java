package it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning;

import java.text.DecimalFormat;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options.Sampling;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options.FeatureSelection;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.Remove;

public class Dataset {
	
	private int numberTrainingRelease;
	private Profile profile;
	private Instances trainingSet;
	private Instances testingSet;
	private double trainingPercentage;
	private double defectiveInTrainingPercentage;
	private double defectiveInTestingPercentage;
	
	public Dataset(Instances dataset, Profile profile, int numberTrainingRelease) {
		this.trainingSet = new Instances(dataset, 0);
		this.testingSet = new Instances(dataset, 0);
		this.profile = profile;
		this.numberTrainingRelease = numberTrainingRelease;
	}
	
	public void addToTesting(Instance recordDataset) {
		this.testingSet.add(recordDataset);		
	}
	
	public void addToTraining(Instance recordDataset) {
		this.trainingSet.add(recordDataset);		
	}
	
	public void setup() throws Exception {
		trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
		testingSet.setClassIndex(testingSet.numAttributes()-1);

		// Filter only data
		int[] columns = {0,1};
		Remove removeFilter = new Remove();
		removeFilter.setAttributeIndicesArray(columns);
		removeFilter.setInvertSelection(false);
		removeFilter.setInputFormat(trainingSet);
		
		trainingSet = Filter.useFilter(trainingSet, removeFilter);
		testingSet = Filter.useFilter(testingSet, removeFilter);
	}
	
	public void computeStats(Instances dataset) {
		trainingPercentage = ((double)trainingSet.size()/dataset.size());
		
		int numberAttributes = trainingSet.numAttributes();
		
		int defectiveInTraining = 0;
		int defectiveInTesting = 0;
		
		for (Instance recordDataset: trainingSet) {
			if (recordDataset.stringValue(numberAttributes-1).equals("Yes"))
				defectiveInTraining++;
		}
		
		for (Instance recordDataset: testingSet) {
			if (recordDataset.stringValue(numberAttributes-1).equals("Yes"))
				defectiveInTesting++;
		}
		
		defectiveInTrainingPercentage = ((double)defectiveInTraining/trainingSet.size());
		defectiveInTestingPercentage = ((double)defectiveInTesting/testingSet.size());
	}
	
	public void applyFeatureSelection() throws Exception {
		if (profile.getSelection().equals(FeatureSelection.BEST_FIRST)) {
			AttributeSelection attrSelection = new AttributeSelection();
			attrSelection.setEvaluator(new CfsSubsetEval());
			attrSelection.setSearch(new BestFirst());
			attrSelection.SelectAttributes(trainingSet);
			
			Remove removeFilter = new Remove();
			removeFilter.setAttributeIndicesArray(attrSelection.selectedAttributes());
			removeFilter.setInvertSelection(true);
			removeFilter.setInputFormat(trainingSet);
			
			trainingSet = Filter.useFilter(this.trainingSet, removeFilter);
			testingSet = Filter.useFilter(this.testingSet, removeFilter);			
		}
	}
	
	public void applyBalancing() throws Exception {
		if (profile.getSampling().equals(Sampling.OVERSAMPLING)) {
			Resample resample = new Resample();
			resample.setInputFormat(trainingSet);
			DecimalFormat df = new DecimalFormat("#.##");
			
			resample.setOptions(Utils.splitOptions(String.format("%s %s", "-B 1.0 -Z", df.format(computerMajorityClassPercentage()))));
			
			trainingSet = Filter.useFilter(trainingSet, resample);
		} else if (profile.getSampling().equals(Sampling.UNDERSAMPLING)) {
			SpreadSubsample underSampling = new SpreadSubsample();
			underSampling.setInputFormat(trainingSet);
			underSampling.setOptions(Utils.splitOptions("-M 1.0"));
			
			trainingSet = Filter.useFilter(trainingSet, underSampling);
		} else if (profile.getSampling().equals(Sampling.SMOTE)) {
			SMOTE smote = new SMOTE();
			smote.setInputFormat(trainingSet);
			
			trainingSet = Filter.useFilter(trainingSet, smote);
		}
	}
	
	private double computerMajorityClassPercentage() {
		int buggyClasses = 0;
		Instances dataset = new Instances(trainingSet);
		dataset.addAll(testingSet);
		
		for (Instance recordDataset: dataset) {
			String buggy = recordDataset.stringValue(recordDataset.numAttributes()-1);
			if (buggy.equals("Yes"))
				buggyClasses++;
		}
		
		double percentage = (100 * 2 * buggyClasses/dataset.size());
		
		if (percentage >= 50)
			return percentage;
		else
			return 100-percentage;
	}
	
	public Profile getProfile() {
		return profile;
	}
	
	public Instances getTrainingSet() {
		return trainingSet;
	}
	
	public Instances getTestingSet() {
		return testingSet;
	}
	
	public int getNumberTrainingRelease() {
		return numberTrainingRelease;
	}
	
	public double getTrainingPercentage() {
		return trainingPercentage;
	}
	
	public double getDefectiveInTrainingPercentage() {
		return defectiveInTrainingPercentage;
	}
	
	public double getDefectiveInTestingPercentage() {
		return defectiveInTestingPercentage;
	}
}
