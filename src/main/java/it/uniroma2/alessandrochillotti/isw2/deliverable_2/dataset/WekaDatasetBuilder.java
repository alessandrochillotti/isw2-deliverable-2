package it.uniroma2.alessandrochillotti.isw2.deliverable_2.dataset;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.parameters.Parameters;

public class WekaDatasetBuilder {
	
	private static final Logger LOGGER = Logger.getLogger("DatasetBuilder");
	private static final String HEADER = "Dataset, #TrainingRelease, %Training, %Defective in training,"
			+ "%Defective in testing, Classifier, Balancing, Feature Selection, Sensitivity, TP, FP, TN, FN, Precision,"
			+ "Recall, AUC, Kappa\n";
	
	private FileWriter handleFile;

	public WekaDatasetBuilder(String projName) {
		try {
			this.handleFile = new FileWriter(String.format("%s%s%s", Parameters.WEKA_RESULTS_DIRECTORY, projName.toLowerCase(), "-weka-result.csv"));
		} catch (IOException e) {
			LOGGER.log(null, "Error in instantiation phase", e);
		}
	}
	
	public void buildDataset(List<WekaDatasetEntry> entries) throws IOException {
		handleFile.append(HEADER);
		
		for (WekaDatasetEntry entry: entries) {
			handleFile.append(entry.toCSV());
		}
		
		handleFile.flush();
	}
}
