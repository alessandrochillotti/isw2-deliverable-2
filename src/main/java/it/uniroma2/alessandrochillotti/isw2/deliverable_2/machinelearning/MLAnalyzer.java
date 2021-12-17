package it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options.Sampling;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.dataset.WekaDatasetBuilder;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.dataset.WekaDatasetEntry;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options.Classifiers;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options.CostSensitive;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.machinelearning.options.FeatureSelection;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.parameters.Parameters;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class MLAnalyzer {
	
	private static final Logger LOGGER = Logger.getLogger("MLAnalyzer");
	private static final int VERSION_COLUMN = 0;
	private Instances dataset;
	
	public MLAnalyzer(File csvFile) {
		try {
			csvToArff(csvFile);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "csvToArff", e);
		}
	}
	
	public static void main(String[] args) {
		MLAnalyzer mlAnalyzer = new MLAnalyzer(new File(String.format("%s%s-dataset.csv", Parameters.DATASET_DIRECTORY, Parameters.PROJECT.toLowerCase())));
		try {
			mlAnalyzer.performRuns();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "performRuns", e);
		}
	}
	
	public void performRuns() throws Exception {
		ArrayList<Profile> profiles = new ArrayList<>();
		ArrayList<Dataset> datas = new ArrayList<>();
		
		// Prepare profiles
		for (FeatureSelection selection: FeatureSelection.values()) {
			for (Sampling sampling: Sampling.values()) {
				for (CostSensitive costSensitive: CostSensitive.values()) {
					profiles.add(new Profile(selection, sampling, costSensitive));
				}
			}
		}
		
		// Prepare each dataset for classifiers
		for (Profile profile: profiles) {
			datas.addAll(walkForward(profile));
		}
		
		// Setup and perform run
		ArrayList<WekaDatasetEntry> entries = new ArrayList<>();
		for (Dataset currentDataset: datas) {
			currentDataset.setup();
			currentDataset.computeStats(dataset);
			currentDataset.applyFeatureSelection();
			currentDataset.applyBalancing();
			
			for (Classifiers classifier: Classifiers.values()) {
				Run currentRun = new Run(currentDataset, classifier);
				entries.add(new WekaDatasetEntry(Parameters.PROJECT, currentRun.perform(), currentRun));
			}
		}
		
		WekaDatasetBuilder datasetBuilder = new WekaDatasetBuilder(Parameters.PROJECT);
		datasetBuilder.buildDataset(entries);
	}
	
	private void csvToArff(File csvFile) throws IOException {
		CSVLoader loader = new CSVLoader();
		loader.setSource(csvFile);
		dataset = loader.getDataSet();
		
		ArffSaver saver = new ArffSaver();
		saver.setInstances(dataset);
		saver.setFile(new File(String.format("%s%s-dataset.arff", Parameters.DATASET_DIRECTORY, Parameters.PROJECT.toLowerCase())));
		saver.writeBatch();
	}
	
	private List<Dataset> walkForward(Profile profile) {
		ArrayList<Dataset> datasets = new ArrayList<>();
		
		Attribute versionAttribute = dataset.attribute("Version");
		int numVersion = versionAttribute.numValues();
	
		ArrayList<String> versions = new ArrayList<>();
		for (int i = 0; i < numVersion; i++) {
			versions.add(versionAttribute.value(i));
		}
		
		for (int i = 1; i < numVersion; i++) {
			Dataset currentDataset = new Dataset(dataset, profile, i);
			
			for (Instance recordDataset: dataset) {
				if (versions.indexOf(recordDataset.stringValue(VERSION_COLUMN)) == i)
					currentDataset.addToTesting(recordDataset);
				else if (versions.indexOf(recordDataset.stringValue(VERSION_COLUMN)) < i)
					currentDataset.addToTraining(recordDataset);
			}
			
			datasets.add(currentDataset);
		}
		
		return datasets;
	}
	
}
