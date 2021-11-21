package it.uniroma2.alessandrochillotti.isw2.deliverable_2;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.ClassFile;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Ticket;

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

	private Git getGit(String url, String projName) throws GitAPIException, IOException {
		// If SANDBOX_FOLDER don't exist in user path, then create it
		String folderName = "repo-" + projName.toLowerCase();
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

	/**
	 * This method return the last commit of version
	 *
	 * @param beginDate the minimum date of commit to consider
	 * @param endDate   the maximum date of commit to consider
	 * @return last commit of version
	 */
	public RevCommit getLastCommit(LocalDateTime beginDate, LocalDateTime endDate) throws GitAPIException {
		boolean first = true;
		// Get log of commits
		Iterable<RevCommit> log = handleGit.log().call();
		RevCommit lastCommit = null;

		// Take last commit in period [beginDate, endDate)
		for (RevCommit commit: log) {
			LocalDateTime date = getDateCommit(commit);

			if (date.isAfter(beginDate) && date.isBefore(endDate)) {
				if (first) {
					lastCommit = commit;
					first = false;
				}

				if (date.isAfter(getDateCommit(lastCommit))) {
					lastCommit = commit;
				}
			}
		}
		
		return lastCommit;
	}
	
	/**
	 * This method put commit in corresponding ticket
	 *
	 * @param tickets	list of ticket checked
	 */
	public void commitsInTicket(List<Ticket> tickets) throws GitAPIException {		
		// Get log of commits
		Iterable<RevCommit> log = handleGit.log().call();
		
		for (RevCommit commit : log) {
			for (Ticket ticket : tickets) {
				if (commit.getFullMessage().contains(ticket.getKey())) {
					ticket.addCommit(commit);
					break;
				}
			}
		}
	}

	/**
	 * This method return the list of file that are present in a specific point of
	 * versioning
	 *
	 * @param commit the commit to consider to see files
	 * @return list of name file that are present
	 */
	public List<ClassFile> getFilesCommit(RevCommit commit) {
		List<ClassFile> affectedFiles = new ArrayList<>();
		ObjectId treeId = commit.getTree().getId();

		try (TreeWalk treeWalk = new TreeWalk(handleGit.getRepository())) {
			treeWalk.reset(treeId);
			while (treeWalk.next()) {
				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				} else {
					if (treeWalk.getPathString().endsWith(".java")) {
						ClassFile fileToAdd = new ClassFile(treeWalk.getPathString());
						fileToAdd.addCommit(commit);
						affectedFiles.add(fileToAdd);
					}	
				}
			}
		} catch (IOException e) {
			LOGGER.log(null, "Affected");
		}

		return affectedFiles;
	}
	
	public LocalDateTime getDateCommit(RevCommit commit) {
		PersonIdent currAuthorIdent = commit.getAuthorIdent();
		Date currAuthorDate = currAuthorIdent.getWhen();
		return currAuthorDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}
	
	/** This method return the list of touched files in commit
	 * 
	 * @param	commit	considered commit
	 * @return			list of touched file
	 * 
	 * */
	public List<ClassFile> filesInCommit(RevCommit commit) throws RevisionSyntaxException, IOException {
		ArrayList<ClassFile> files = new ArrayList<>();
		
		Repository repo = handleGit.getRepository();
		try (RevWalk rw = new RevWalk(repo)) {
			RevCommit parent = rw.parseCommit(commit.getParent(0).getId());
			try (DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
				df.setRepository(repo);
				df.setDiffComparator(RawTextComparator.DEFAULT);
				df.setDetectRenames(true);
				List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());
				for (DiffEntry diff : diffs) {
					if (diff.getNewPath().endsWith(".java"))
						files.add(new ClassFile(diff.getNewPath()));
				}
			}
		}
		
		return files;
	}
}