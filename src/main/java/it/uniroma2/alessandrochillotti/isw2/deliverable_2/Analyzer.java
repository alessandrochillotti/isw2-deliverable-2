package it.uniroma2.alessandrochillotti.isw2.deliverable_2;


import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Version;

public class Analyzer {
	
	/* Parameters */
	private static final String URL = "https://github.com/apache/bookkeeper.git";
	private static final String STRING_TO_FOUND = "Added";
	private static final String PROJ_NAME = "BOOKKEEPER";
	
	private static final Logger LOGGER = Logger.getLogger("Analyzer");
	
	
	public static void main(String[] args) {
		JiraAnalyzer jiraAnalyzer = new JiraAnalyzer();
		ArrayList<Version> versions = new ArrayList<>();
		GitAnalyzer gitAnalyzer = null;
		
		// Try to instantiate GitAnalyzer
		try {
			gitAnalyzer = new GitAnalyzer(URL);
		} catch (GitAPIException | IOException e) {
			LOGGER.log(null, "GitAnalyzer creation exception", e);
			System.exit(1);
		}
		
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
		
		try {
			gitAnalyzer.getCommitID(STRING_TO_FOUND);
		} catch (GitAPIException e) {
			LOGGER.log(null, "GitAnalyzer getCommitID exception", e);
		}
		
		
	}
}
