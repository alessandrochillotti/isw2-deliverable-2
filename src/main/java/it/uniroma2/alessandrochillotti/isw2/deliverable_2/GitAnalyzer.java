package it.uniroma2.alessandrochillotti.isw2.deliverable_2;

import java.io.*;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

public class GitAnalyzer {

	private static final Logger LOGGER = Logger.getLogger("Commit ID");
	private static final String SANDBOX_FOLDER = "repo-bookkeeper";
	
	private Git handleGit;
	
	public GitAnalyzer(String url) throws GitAPIException, IOException {
		this.handleGit = getGit(url);
	}
	
	public Git getGit(String url) throws GitAPIException, IOException {
		// If SANDBOX_FOLDER don't exist in user path, then create it
		new File(System.getProperty("user.home"), SANDBOX_FOLDER).mkdir();
		File dir = new File(System.getProperty("user.home"), SANDBOX_FOLDER);
		
		Git git;

		// If the directory is not empty, then I refresh the directory
		if (dir.list().length == 0) {
			git = Git.cloneRepository().setURI(url).setDirectory(dir).call();
		} else {
			git = Git.open(dir);
			git.pull();
			git.checkout();
		}

		return git;
	}

	public void getCommitID(String stringToFound) throws GitAPIException {
		// Get log of commits
		Iterable<RevCommit> log = handleGit.log().call();

		// Print all commit that contain the word STRING_TO_FOUND
		for (RevCommit element : log) {
			String commentCommit = element.getFullMessage();
			if (commentCommit.contains(stringToFound)) {
				LOGGER.info(element.getName());
			}
		}
	}
}