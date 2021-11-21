package it.uniroma2.alessandrochillotti.isw2.deliverable_2;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONException;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.ClassFile;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Ticket;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Version;

public class Analyzer {
	
	/* Parameters */
	private static final String PROJ_NAME = "BOOKKEEPER";
	private static final String REPO_URL = "https://github.com/apache/bookkeeper.git";
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
		Analyzer analyzer = new Analyzer(REPO_URL, PROJ_NAME);
		
		// Retrieve the versions to store, already the half
		ArrayList<Version> versions = null;
		ArrayList<Ticket> bugTickets = null;
		
		LOGGER.log(Level.INFO, "Start analysis");
		
		LOGGER.log(Level.INFO, "1. Retrieve files for each version");
		
		LOGGER.log(Level.INFO, "1.1 Retrieve versions");
		try {
			versions = (ArrayList<Version>) analyzer.getVersions();
		} catch (JSONException | IOException e) {
			LOGGER.log(Level.WARNING, "Retrieve versions exception", e);
			System.exit(1);
		}
		
		LOGGER.log(Level.INFO, "1.2 Discard second half of versions");
		try {
			analyzer.setLastCommits(versions);
		} catch (GitAPIException e) {
			LOGGER.log(Level.WARNING, "Discard second half of versions exception", e);
		}
		
		LOGGER.log(Level.INFO, "1.3 Retrieve files at the end of each version");
		for (int i = 0; i < versions.size(); i++) {
			ArrayList<ClassFile> files = (ArrayList<ClassFile>) analyzer.getFiles(versions.get(i).getLastCommit());
			if (files == null) {
				files = (ArrayList<ClassFile>) analyzer.getFiles(versions.get(i-1).getLastCommit());
			}
			versions.get(i).setFiles(files);
		}
		
		LOGGER.log(Level.INFO, "2. Retrieve bug tickets");
		try {
			bugTickets = (ArrayList<Ticket>) analyzer.getBugTickets();
		} catch (JSONException | IOException | GitAPIException e) {
			LOGGER.log(Level.WARNING, "Retrieve bug commit exception", e);
			System.exit(1);
		}
		
		for (int i = 0; i < bugTickets.size(); i++) {
			Ticket ticket = bugTickets.get(i);
			for (int j = 0; j < ticket.getCommits().size(); j++) {
				RevCommit commit = ticket.getCommits().get(j);
				try {
					analyzer.gitApi.filesInCommit(commit);
				} catch (IOException e) {
					LOGGER.log(Level.WARNING, "Files in commit exception", e);
				}
			}
		}
		
		LOGGER.log(Level.INFO, "End analysis");
	}
	
	public List<Version> getVersions() throws JSONException, IOException {
		List<Version> versions =jiraApi.retrieveVersions();
		
		// Discard the second half for snoring
		int initSize = versions.size();
		for (int i = initSize-1; i >= initSize/2; i--) {
			versions.remove(i);
		}
		
		return versions;
	}
	
	public List<Ticket> getBugTickets() throws JSONException, IOException, GitAPIException {
		// Create a basic version of ticket
		ArrayList<Ticket> tickets = (ArrayList<Ticket>) jiraApi.retrieveBugTickets();

		// Put into each ticket its commits
		gitApi.commitsInTicket(tickets);
		
		return tickets;
	}
	
	public void setLastCommits(List<Version> versions) throws GitAPIException {
		// Get last commit for each release
		for (int i = 0; i < versions.size(); i++) {
			Version current = versions.get(i);
			RevCommit lastCommit = null;
			int j = 0;
			do {
				lastCommit = gitApi.getLastCommit(versions.get(i - j).getBeginDate(), versions.get(i - j).getEndDate());
				j++;
			} while (lastCommit == null);
			current.setLastCommit(lastCommit);
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
