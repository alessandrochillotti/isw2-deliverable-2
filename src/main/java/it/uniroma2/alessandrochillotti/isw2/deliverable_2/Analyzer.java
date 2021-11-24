package it.uniroma2.alessandrochillotti.isw2.deliverable_2;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONException;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.dataset.DatasetBuilder;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.dataset.DatasetEntry;
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
		
		LOGGER.log(Level.INFO, "Start analysis");
		
		LOGGER.log(Level.INFO, "1. Retrieve versions");
		try {
			versions = (ArrayList<Version>) analyzer.getVersions();
		} catch (JSONException | IOException | GitAPIException e) {
			LOGGER.log(Level.WARNING, "Retrieve versions exception", e);
			System.exit(1);
		}
		
		LOGGER.log(Level.INFO, "2. Compute metrics");
		try {
			ArrayList<DatasetEntry> entries = (ArrayList<DatasetEntry>) analyzer.computeMetrics(versions);
			analyzer.datasetApi.buildDataset(entries);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Computation metrics exception", e);
		}

		
		
		LOGGER.log(Level.INFO, "End analysis");
	}
	
	public List<Version> getVersions() throws JSONException, IOException, GitAPIException {
		List<Version> versions =jiraApi.retrieveVersions();
		
		// Discard the second half for snoring
		int initSize = versions.size();
		for (int i = initSize-1; i >= initSize/2; i--) {
			versions.remove(i);
		}
		
		// Fill commits for each version
		for (Version version: versions) {
			version.setCommits(gitApi.getCommits(version));
		}
		
		// Fill files for each version
		for (int i = 0; i < versions.size(); i++) {
			ArrayList<RevCommit> commits = (ArrayList<RevCommit>) versions.get(i).getCommits();
			ArrayList<ClassFile> files = null;
			
			if (!commits.isEmpty()) {
				files = (ArrayList<ClassFile>) gitApi.getFilesCommit(commits.get(commits.size()-1));	
			} else {
				commits = (ArrayList<RevCommit>) versions.get(i-1).getCommits();
				files = (ArrayList<ClassFile>) gitApi.getFilesCommit(commits.get(commits.size()-1));
			}
			
			versions.get(i).setFiles(files);
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
	
	public List<DatasetEntry> computeMetrics(List<Version> versions) throws IOException {
		ArrayList<DatasetEntry> stats = new ArrayList<>();
		Map<String, Integer> sizes = new HashMap<>();
		
		for (Version version: versions) {
			ArrayList<ClassFile> files = (ArrayList<ClassFile>) version.getFiles();
			
			// Initialization sizes of file
			for (ClassFile file: files) {
				sizes.put(file.getFullName(), 0);
			}
		}
		
		Version prevVersion = null;
		for (Version version: versions) {
			ArrayList<ClassFile> files = (ArrayList<ClassFile>) version.getFiles();
			ArrayList<RevCommit> commits = (ArrayList<RevCommit>) version.getCommits();
			
			if (commits.isEmpty() && prevVersion != null)
				files = (ArrayList<ClassFile>) prevVersion.getFiles();
			
			// Compute metrics
			for (ClassFile file: files) {
				DatasetEntry entry = new DatasetEntry(version, file, sizes.get(file.getFullName()));
				
				makeDatasetEntry(entry, commits, file);
				
				entry.setAge(version.getEndDate());
				
				sizes.put(file.getFullName(), entry.getSize());
				stats.add(entry);
			}
			
			prevVersion = version;
		}
		
		return stats;
	}
	
	private void makeDatasetEntry(DatasetEntry entry, List<RevCommit> commits, ClassFile file) throws IOException {
		RevCommit prevCommit = null;
		
		for (RevCommit commit: commits) {
			ArrayList<Edit> edits;
			
			if (prevCommit == null) 
				edits = (ArrayList<Edit>) gitApi.diff(commit, null, file);
			else
				edits = (ArrayList<Edit>) gitApi.diff(prevCommit, commit, file);
			
			if (!edits.isEmpty())
				entry.addCommit(commit);
			
			prevCommit = commit;
			
			// Update stats
			for (Edit edit: edits) {
				if (edit.getBeginA() == 0 && edit.getEndA() == 0) 
					file.setCreationDate(gitApi.getDateCommit(commit));
				
				entry.updateChurn(edit.getEndB()-edit.getBeginB(), edit.getEndA()-edit.getBeginA());
			}
		}
	}
}
