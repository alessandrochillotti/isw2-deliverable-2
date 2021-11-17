package it.uniroma2.alessandrochillotti.isw2.deliverable_2;


import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.json.JSONException;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Version;

public class Analyzer {
	
	private static final Logger LOGGER = Logger.getLogger("Analyzer");
	private static final String PROJ_NAME = "BOOKKEEPER";
	
	public static void main(String[] args) {
		JiraAnalyzer jiraAnalyzer = new JiraAnalyzer();
		
		ArrayList<Version> versions = new ArrayList<>();
		
		try {
			versions = (ArrayList<Version>) jiraAnalyzer.retrieveVersions(PROJ_NAME);
		} catch (JSONException | IOException e) {
			LOGGER.log(null, "JiraAnalyzer exception", e);
		}
		
		// Only for smell
		versions.clear();
	}
}
