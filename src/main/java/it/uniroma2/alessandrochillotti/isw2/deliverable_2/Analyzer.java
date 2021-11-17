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
		GitAnalyzer gitAnalyzer = new GitAnalyzer();
		
		ArrayList<Version> versions = new ArrayList<>();
		
		// Retrieve versions of Jira project
		try {
			versions = (ArrayList<Version>) jiraAnalyzer.retrieveVersions(PROJ_NAME);
		} catch (JSONException | IOException e) {
			LOGGER.log(null, "JiraAnalyzer exception", e);
		}
		
		// Discard the second half for snoring
		int initSize = versions.size();
		for (int i = initSize-1; i >= initSize/2; i--) {
			versions.remove(i);
		}
		
		gitAnalyzer.printCommitID();
	}
}
