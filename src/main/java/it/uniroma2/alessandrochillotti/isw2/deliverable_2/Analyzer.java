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
	private static final String PROJ_NAME = "BOOKKEEPER";
	
	private static final Logger LOGGER = Logger.getLogger("Analyzer");
	
	public static void main(String[] args) {
		// Entities to analyze project
		JiraAnalyzer jiraAnalyzer = new JiraAnalyzer(PROJ_NAME);
		GitAnalyzer gitAnalyzer = new GitAnalyzer(URL, PROJ_NAME);
		DatasetBuilder datasetBuilder = new DatasetBuilder(PROJ_NAME);
		
		// Information to store result
		ArrayList<Version> versions = new ArrayList<>();
		
		// Retrieve versions of Jira project
		try {
			versions = (ArrayList<Version>) jiraAnalyzer.retrieveVersions();
		} catch (JSONException | IOException e) {
			LOGGER.log(null, "RetrieveVersions exception", e);
		}
		
		// Discard the second half for snoring
		int initSize = versions.size();
		for (int i = initSize-1; i >= initSize/2; i--) {
			versions.remove(i);
		}
		
		// Creation of dataset
		try {
			datasetBuilder.makeHeader();
		} catch (IOException e) {
			LOGGER.log(null, "Make header exception", e);
		}
		
		// Get last commit for each release
		for (int i = 0; i < versions.size(); i++) {
			try {
				Version current = versions.get(i);
				current.setLastCommit(gitAnalyzer.getLastCommit(current.getBeginDate(), current.getEndDate()));
			} catch (GitAPIException e) {
				LOGGER.log(null, "Insert files version exception", e);
			}
		}
		
		// Fill dataset with name of files
		for (int i = 0; i < versions.size(); i++) {
			try {
				Version current = versions.get(i);
				if (current.getLastCommit() != null)
					datasetBuilder.insertFilesVersion(versions.get(i).getVersionName(), gitAnalyzer.getFilesCommit(current.getLastCommit()));
			} catch (IOException e) {
				LOGGER.log(null, "Insert files version exception", e);
			}
		}
	}
	
}
