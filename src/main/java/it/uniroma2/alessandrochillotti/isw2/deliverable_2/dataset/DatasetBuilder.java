package it.uniroma2.alessandrochillotti.isw2.deliverable_2.dataset;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class DatasetBuilder {

	private static final Logger LOGGER = Logger.getLogger("DatasetBuilder");
	public static final String HEADER = "Version,Name,Size,NR,NAuth,LOC added,MAX LOC added,AVG LOC added,Churn,MAX Churn,AVG Churn,Age,Buggy\n";

	private FileWriter handleFile;

	public DatasetBuilder(String projName) {
		try {
			this.handleFile = new FileWriter(projName.toLowerCase() + "-dataset.csv");
		} catch (IOException e) {
			LOGGER.log(null, "Error in instantiation phase", e);
		}
	}
	
	public void buildDataset(List<DatasetEntry> entries) throws IOException {
		handleFile.append(HEADER);
		
		for (DatasetEntry entry: entries) {
			handleFile.append(entry.toCSV());
		}
		
		handleFile.flush();
	}
}
