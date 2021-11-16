package it.uniroma2.alessandrochillotti.isw2.deliverable_2;

import java.io.*;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

public class GitAnalyzer {

	private static final String URL = "https://github.com/alessandrochillotti/helloworld-git-analysis.git";
	private static final String STRING_TO_FOUND = "Added";
	private static final Logger LOGGER = Logger.getLogger("Commit ID");
	private static final String SANDBOX_FOLDER = "git-analysis";
	
	public static void main(String[] args) {
		GitAnalyzer rc = new GitAnalyzer();
		try {
			rc.getCommitID(rc.getGit(URL, System.getProperty("user.home")), STRING_TO_FOUND);
		} catch (GitAPIException | IOException e) {
			LOGGER.log(null, "context", e);
		}
	}
	
	public Git getGit(String url, String pathRoot) throws GitAPIException, IOException {
		
		// If SANDBOX_FOLDER don't exist in user path, then create it
		new File(pathRoot, SANDBOX_FOLDER).mkdir();
		File dir = new File(pathRoot, SANDBOX_FOLDER);
		
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

	public void getCommitID(Git git, String stringToFound) throws GitAPIException {

		// Get log of commits
		Iterable<RevCommit> log = git.log().call();

		// Print all commit that containt the word STRING_TO_FOUND
		for (RevCommit element : log) {
			String commentCommit = element.getFullMessage();
			if (commentCommit.contains(stringToFound)) {
				LOGGER.info(element.getName());
			}
		}
	}
}