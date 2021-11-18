package it.uniroma2.alessandrochillotti.isw2.deliverable_2;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

public class GitAnalyzer {

	private static final Logger LOGGER = Logger.getLogger("Commit ID");

	private Git handleGit;

	public GitAnalyzer(String url, String projName) {
		try {
			this.handleGit = getGit(url, projName);
		} catch (GitAPIException | IOException e) {
			LOGGER.log(null, "Error in instantiation phase", e);
		}
	}

	public Git getGit(String url, String projName) throws GitAPIException, IOException {
		// If SANDBOX_FOLDER don't exist in user path, then create it
		String folderName = "repo-"+projName.toLowerCase();
		new File(System.getProperty("user.home"), folderName).mkdir();
		File dir = new File(System.getProperty("user.home"), folderName);

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

	/**
    * This method return the list of file that are present in a specific point of versioning, considering the last commit in this version. 
    *
    * @param	beginDate	the minimum date of commit to consider
    * @param	endDate		the maximum date of commit to consider
    * @return				list of name file that are present
    */
	public List<String> getFilesLastCommit(LocalDateTime beginDate, LocalDateTime endDate) throws GitAPIException {
		boolean first = true;
		// Get log of commits
		Iterable<RevCommit> currentCommit = handleGit.log().call();
		RevCommit lastCommit = null;
		
		// Take last commit in period [beginDate, endDate)
		for (RevCommit element : currentCommit) {
			if (first) {
				lastCommit = element;
				first = false;
			}
			
			// Compute LocalDateTime current commit
			PersonIdent currAuthorIdent = element.getAuthorIdent();
			Date currAuthorDate = currAuthorIdent.getWhen();
			LocalDateTime currAuthorDateTime = currAuthorDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			
			if (currAuthorDateTime.isAfter(beginDate) && currAuthorDateTime.isBefore(endDate)) {
				// Compute LocalDateTime current last commit
				PersonIdent lastAuthorIdent = lastCommit.getAuthorIdent();
				Date lastAuthorDate = lastAuthorIdent.getWhen();
				LocalDateTime lastAuthorDateTime = lastAuthorDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				
				if (currAuthorDateTime.isAfter(lastAuthorDateTime)) {
					lastCommit = element;
				}
			}
		}
		
		if (lastCommit != null) 
			return affetctedFiles(lastCommit);
		else 
			return Collections.emptyList();
	}
	
	private List<String> affetctedFiles(RevCommit commit) {
		List<String> affectedFiles = new ArrayList<>();
		
		ObjectId treeId = commit.getTree().getId();
		
		try (TreeWalk treeWalk = new TreeWalk(handleGit.getRepository())) {
			try {
				treeWalk.reset(treeId);
				while (treeWalk.next()) {
					if (treeWalk.isSubtree()) {
						treeWalk.enterSubtree();
					} else {
						if (treeWalk.getPathString().contains(".java"))
							affectedFiles.add(treeWalk.getPathString());
					}
				}
			} catch (IOException e) {
				LOGGER.log(null, "IOException", e);
			}
		}
		
		return affectedFiles;
	}
}