package it.uniroma2.alessandrochillotti.isw2.deliverable_2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class DatasetBuilder {

	private static final Logger LOGGER = Logger.getLogger("DatasetBuilder");
	
	private FileWriter handleFile;

	public DatasetBuilder(String projName) {
		try {
			this.handleFile = new FileWriter(projName.toLowerCase() + "-dataset.csv");
		} catch (IOException e) {
			LOGGER.log(null, "Error in instantiation phase", e);
		}
	}

	public void makeHeader() throws IOException {
		handleFile.append("Version,File Name,Buggy");
		handleFile.append("\n");
	}

	public void insertFilesVersion(String version, List<String> list) throws IOException {
		for (int i = 0; i < list.size(); i++) {
			handleFile.append(version+","+list.get(i)+", No");
			handleFile.append("\n");
		}
		handleFile.flush();
	}
}
