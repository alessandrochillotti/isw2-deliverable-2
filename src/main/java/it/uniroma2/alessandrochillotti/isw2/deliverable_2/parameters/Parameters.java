package it.uniroma2.alessandrochillotti.isw2.deliverable_2.parameters;

public class Parameters {
	
	private Parameters () {
		throw new IllegalStateException("Parameters class must not be instantiated");
	}
	
	public static final String PROJECT = "ZOOKEEPER";
	public static final String DATASET_DIRECTORY = "./dataset/";
	public static final String WEKA_RESULTS_DIRECTORY = "./weka-results/";
	public static final int WINDOW_SIZE = 50;
	public static final String FILE_NAME = "record.csv";
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String FILE_TYPE = ".java";
	
	public static String makeUrl(String project) {
		return String.format("https://github.com/apache/%s.git", project);
	}
}
