package it.uniroma2.alessandrochillotti.isw2.deliverable_2;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONException;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.ClassFile;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Ticket;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Version;

public class Analyzer {
	
	/* Parameters */
	private static final String URL = "https://github.com/apache/bookkeeper.git";
	private static final String PROJ_NAME = "BOOKKEEPER";
	private static final Logger LOGGER = Logger.getLogger("Analyzer");
	
	private JiraAnalyzer jiraApi;
	private GitAnalyzer gitApi;
	private DatasetBuilder datasetApi;
	
	public Analyzer(String url, String projName) {
		this.jiraApi = new JiraAnalyzer(projName);
		this.gitApi = new GitAnalyzer(url, projName);
		this.datasetApi = new DatasetBuilder(projName);
	}
	
	public static void main(String[] args) {
		Analyzer analyzer = new Analyzer(URL, PROJ_NAME);
		
		// Retrieve the versions to store, already the half
		ArrayList<Version> versions = null; 
		ArrayList<Ticket> tickets = null;
		
		// Fill versions, set last commit and load files for each versions
		versions = (ArrayList<Version>) analyzer.getVersions();
		analyzer.setLastCommits(versions);
		for (int i = 0; i < versions.size(); i++) {
			ArrayList<ClassFile> files = (ArrayList<ClassFile>) analyzer.getFiles(versions.get(i).getLastCommit());
			if (files == null) {
				files = (ArrayList<ClassFile>) analyzer.getFiles(versions.get(i-1).getLastCommit());
			}
			versions.get(i).setFiles(files);
		}
		
		// Fill bug tickets
		try {
			tickets = (ArrayList<Ticket>) analyzer.getBugTickets();
		} catch (JSONException | IOException e) {
			LOGGER.log(null, "Get bug tickets exception", e);
			System.exit(1);
		}
		
		LOGGER.info(tickets.get(0).getKey());
	}
	
	public List<Version> getVersions() {
		List<Version> versions = new ArrayList<>();
		
		// Retrieve versions of Jira project
		try {
			versions = jiraApi.retrieveVersions();
		} catch (JSONException | IOException e) {
			LOGGER.log(null, "RetrieveVersions exception", e);
		}
		
		// Discard the second half for snoring
		int initSize = versions.size();
		for (int i = initSize-1; i >= initSize/2; i--) {
			versions.remove(i);
		}
		
		return versions;
	}
	
	public List<Ticket> getBugTickets() throws JSONException, IOException {
		return jiraApi.retrieveBugTickets();
	}
	
	public void setLastCommits(List<Version> versions) {
		// Get last commit for each release
		for (int i = 0; i < versions.size(); i++) {
			try {
				Version current = versions.get(i);
				RevCommit lastCommit = null;
				int j = 0;
				do {
					lastCommit = gitApi.getLastCommit(versions.get(i - j).getBeginDate(), versions.get(i - j).getEndDate());
					j++;
				} while (lastCommit == null);
				current.setLastCommit(lastCommit);
			} catch (GitAPIException e) {
				LOGGER.log(null, "Insert files version exception", e);
			}
		}
	}
	
	public List<ClassFile> getFiles(RevCommit commit) {
		return gitApi.getFilesCommit(commit);
	}
	
	
	
	public void datasetCreation(List<Version> versions) throws IOException {
		// Creation of dataset
		datasetApi.makeHeader();
		
		// Fill dataset with name of files
		for (int i = 0; i < versions.size(); i++) {
			Version current = versions.get(i);
			datasetApi.insertFilesVersion(versions.get(i).getVersionName(), current.getFiles());	
		}
	}
}
