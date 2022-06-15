package it.uniroma2.alessandrochillotti.isw2.deliverable_2.analyzers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONException;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.dataset.DatasetBuilder;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.dataset.DatasetEntry;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.parameters.Parameters;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.ClassFile;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Ticket;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Version;

public class Analyzer {

	/* Parameters */
	private static final Logger LOGGER = Logger.getLogger("Analyzer");

	private String project;
	private JiraAnalyzer jiraApi;
	private GitAnalyzer gitApi;
	private DatasetBuilder datasetApi;

	public Analyzer(String projName) throws JSONException {
		this.project = projName.toUpperCase();
		this.jiraApi = new JiraAnalyzer(project);
		this.gitApi = new GitAnalyzer(Parameters.makeUrl(project), project);

		this.datasetApi = new DatasetBuilder(project);
	}

	public static void main(String[] args) {
		Analyzer analyzer = null;
		ArrayList<Version> versions = null;
		ArrayList<Ticket> bugTickets = null;
		ArrayList<DatasetEntry> entries = null;

		// Select the project: BOOKKEEPER o ZOOKKEEPER
		try (Scanner scanner = new Scanner(System.in)) {
			analyzer = new Analyzer(Parameters.PROJECT);
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "New analyzer exception", e);
			System.exit(1);
		}

		// Start analysis
		LOGGER.log(Level.INFO, "Start analysis");
		LOGGER.log(Level.INFO, "1. Metrics computation");
		LOGGER.log(Level.INFO, "1.1 Retrieve versions");
		try {
			versions = (ArrayList<Version>) analyzer.getVersions();
		} catch (JSONException | IOException | GitAPIException e) {
			LOGGER.log(Level.WARNING, "Retrieve versions exception", e);
			System.exit(1);
		}

		LOGGER.log(Level.INFO, "1.2 Compute metrics");
		try {
			entries = (ArrayList<DatasetEntry>) analyzer.computeMetrics(versions);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Computation metrics exception", e);
		}

		LOGGER.log(Level.INFO, "2 Buggyness computation");
		LOGGER.log(Level.INFO, "2.1 Retrieve bug tickets");
		try {
			bugTickets = (ArrayList<Ticket>) analyzer.getBugTickets();
		} catch (JSONException | IOException | GitAPIException e) {
			LOGGER.log(Level.WARNING, "Retrieve bug tickets exception", e);
			System.exit(1);
		}
		
		LOGGER.log(Level.INFO, "2.2 Compute buggyness classes");
		analyzer.checkBuggyClasses(entries, bugTickets, versions);

		LOGGER.log(Level.INFO, "3. Build dataset");
		try {
			analyzer.datasetApi.buildDataset(entries);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Build dataset exception", e);
		}

		LOGGER.log(Level.INFO, "End analysis");
	}

	/**
	 * 
	 * @return list of version
	 */
	public List<Version> getVersions() throws JSONException, IOException, GitAPIException {
		List<Version> versions = jiraApi.retrieveVersions();

		// Discard the second half for snoring
		int initSize = versions.size();
		for (int i = initSize - 1; i >= initSize / 2; i--) {
			versions.remove(i);
		}

		// Fill commits for each version
		boolean first = true;
		for (Version version : versions) {
			version.setCommits(gitApi.getCommits(version, first));
			first = false;
		}

		// Fill files for each version
		for (int i = 0; i < versions.size(); i++) {
			ArrayList<RevCommit> commits = (ArrayList<RevCommit>) versions.get(i).getCommits();
			ArrayList<ClassFile> files = null;

			if (!commits.isEmpty()) {
				files = (ArrayList<ClassFile>) gitApi.getFilesCommit(commits.get(commits.size() - 1));
			} else {
				commits = (ArrayList<RevCommit>) versions.get(i - 1).getCommits();
				files = (ArrayList<ClassFile>) gitApi.getFilesCommit(commits.get(commits.size() - 1));
			}

			versions.get(i).setFiles(files);
		}

		return versions;
	}

	/**
	 * 
	 * @return list of bug ticket
	 */
	public List<Ticket> getBugTickets() throws JSONException, IOException, GitAPIException {
		// Create a basic version of ticket
		ArrayList<Ticket> tickets = (ArrayList<Ticket>) jiraApi.retrieveBugTickets();
		
		// Put into each ticket its files	
		gitApi.filesTouchedForTicket(tickets);
		
		// Apply proportion method
		jiraApi.proportionMethod(tickets);
		
		return tickets;
	}

	/**
	 * 
	 * @param versions the list of versions
	 * @return list of dataset entry
	 */
	public List<DatasetEntry> computeMetrics(List<Version> versions) throws IOException {
		ArrayList<DatasetEntry> stats = new ArrayList<>();
		Map<String, Integer> sizes = new HashMap<>();

		// Initialization sizes of file
		for (Version version : versions) {
			ArrayList<ClassFile> files = (ArrayList<ClassFile>) version.getFiles();

			for (ClassFile file : files) {
				sizes.put(file.getFullName(), 0);
			}
		}

		Version prevVersion = null;
		for (Version version : versions) {
			ArrayList<ClassFile> files = (ArrayList<ClassFile>) version.getFiles();
			ArrayList<RevCommit> commits = (ArrayList<RevCommit>) version.getCommits();

			if (commits.isEmpty() && prevVersion != null)
				files = (ArrayList<ClassFile>) prevVersion.getFiles();

			// Compute metrics
			for (ClassFile file : files) {
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

		for (RevCommit commit : commits) {
			ArrayList<Edit> edits;

			if (prevCommit == null)
				edits = (ArrayList<Edit>) gitApi.diff(commit, null, file);
			else
				edits = (ArrayList<Edit>) gitApi.diff(prevCommit, commit, file);

			if (!edits.isEmpty())
				entry.addCommit(commit);

			prevCommit = commit;

			// Update stats
			for (Edit edit : edits) {
				if (edit.getBeginA() == 0 && edit.getEndA() == 0)
					file.setCreationDate(gitApi.getDateCommit(commit));

				entry.updateChurn(edit.getEndB() - edit.getBeginB(), edit.getEndA() - edit.getBeginA());
			}
		}
	}

	/**
	 * 
	 * @param entries    list of dataset entries to complete
	 * @param bugTickets list of bug ticket of project
	 */
	public void checkBuggyClasses(List<DatasetEntry> entries, List<Ticket> bugTickets, List<Version> versions) {
		// Prepare map to performance
		Map<String, Map<String, DatasetEntry>> table = prepareMapForBuggyList(entries, versions);

		for (Ticket ticket : bugTickets) {
			for (ClassFile file : ticket.getTouchedFiles()) {
				for (Version version : ticket.getAffectedVersions()) {
					Map<String, DatasetEntry> versionEntries = table.get(version.getVersionName());
					if (versionEntries != null) {
						DatasetEntry entry = versionEntries.get(file.getFullName());
						if (entry != null)
							entry.setBuggy(true);
					}
				}
			}
		}
	}
	
	private Map<String, Map<String, DatasetEntry>> prepareMapForBuggyList(List<DatasetEntry> entries, List<Version> versions) {
		Map<String, Map<String, DatasetEntry>> table = new HashMap<>();
		for (Version version : versions) {
			Map<String, DatasetEntry> raw = new HashMap<>();
			for (DatasetEntry entry : entries) {
				if (entry.getVersion().getVersionName().equals(version.getVersionName()))
					raw.put(entry.getFile().getFullName(), entry);
			}
			table.put(version.getVersionName(), raw);
		}
		
		return table;
	}
}
