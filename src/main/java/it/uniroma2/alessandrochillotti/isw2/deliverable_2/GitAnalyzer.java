package it.uniroma2.alessandrochillotti.isw2.deliverable_2;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.parameters.Parameters;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.ClassFile;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Ticket;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Version;

public class GitAnalyzer {

	private static final Logger LOGGER = Logger.getLogger("Commit ID");

	private Git handleGit;

	public GitAnalyzer(String url, String projName) {
		try {
			this.handleGit = getGit(url, projName);
		} catch (GitAPIException | IOException e) {
			LOGGER.log(Level.SEVERE, "Error in instantiation phase", e);
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
	 * This method return the ordered list of commit of a specific version
	 *
	 * @param version the specific version
	 * @return ordered list of commit
	 */
	public List<RevCommit> getCommits(Version version, boolean first) throws GitAPIException {
		// Get log of commits
		Iterable<RevCommit> log = handleGit.log().call();
		ArrayList<RevCommit> commits = new ArrayList<>();
		
		LocalDateTime beginDate = version.getBeginDate();
		LocalDateTime endDate = version.getEndDate();

		// Take last commit in period [beginDate, endDate)
		for (RevCommit commit: log) {
			LocalDateTime date = getDateCommit(commit);
			
			if (first && date.isBefore(endDate) || date.isAfter(beginDate) && date.isBefore(endDate))
				commits.add(commit);
			
		}
		
		// Order commits
		commits.sort((c1, c2) -> getDateCommit(c1).compareTo(getDateCommit(c2)));
		
		return commits;
	}
	
	/**
	 * This method put commit in corresponding ticket
	 *
	 * @param tickets	list of ticket checked
	 * @throws IOException 
	 * @throws CorruptObjectException 
	 * @throws IncorrectObjectTypeException 
	 * @throws  
	 */
	public void filesTouchedForTicket(List<Ticket> tickets) throws GitAPIException, IOException {		
		// Get log of commits
		Iterable<RevCommit> log = handleGit.log().call();
		
		for (RevCommit commit : log) {
			for (Ticket ticket: tickets) {
				if (commit.getFullMessage().contains(ticket.getKey())) {
					ticket.addFilesTouched(filesInCommit(commit));
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
					if (treeWalk.getPathString().endsWith(Parameters.FILE_TYPE)) {
						ClassFile fileToAdd = new ClassFile(treeWalk.getPathString());
						affectedFiles.add(fileToAdd);
					}	
				}
			}
		} catch (IOException e) {
			LOGGER.log(null, "Affected");
		}

		return affectedFiles;
	}
	
	public void getFiles(List<Version> versions) throws IOException {
		for (Version version: versions) {
			ObjectId treeId = version.getLastCommit().getTree().getId();

			TreeWalk treeWalk = new TreeWalk(handleGit.getRepository());
			treeWalk.reset(treeId);
			
			exploreTreeWalk(treeWalk);
		}
	}
	
	private void exploreTreeWalk(TreeWalk treeWalk) throws IOException {
		List<ClassFile> files = new ArrayList<>();
		
		while (treeWalk.next()) {
			if (treeWalk.isSubtree()) {
				treeWalk.enterSubtree();
			} else {
				if (treeWalk.getPathString().endsWith(Parameters.FILE_TYPE)) {
					ClassFile fileToAdd = new ClassFile(treeWalk.getPathString());
					if (!files.contains(fileToAdd))
						files.add(fileToAdd);
				}	
			}
		}
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
					if (diff.getNewPath().endsWith(Parameters.FILE_TYPE)) {
						files.add(new ClassFile(diff.getNewPath()));
					}
				}
			}
		}
		
		return files;
	}
	
	 /** This method implements the git diff command
	 *
	 * @param oldCommit 	the old commit
	 * @param newCommit 	the new commit
	 * @param file			file to be analyzed
	 */
	public List<Edit> diff(RevCommit oldCommit, RevCommit newCommit, ClassFile file) throws IOException {
		List<DiffEntry> diffList = null;
		ArrayList<Edit> edits = new ArrayList<>();
		
		try (DiffFormatter formatter = new DiffFormatter(null)) {
			formatter.setRepository(handleGit.getRepository());
			
			if (newCommit != null) {
				diffList = formatter.scan(oldCommit.getTree(), newCommit.getTree());
			} else {
				ObjectReader reader = handleGit.getRepository().newObjectReader();
				AbstractTreeIterator newTreeIter = new CanonicalTreeParser(null, reader, oldCommit.getTree());
				AbstractTreeIterator oldTreeIter = new EmptyTreeIterator();
				diffList = formatter.scan(oldTreeIter, newTreeIter);
			}

			for (DiffEntry diff : diffList) {
				if (diff.toString().contains(file.getFullName())) {
					formatter.setDetectRenames(true);
					EditList editList = formatter.toFileHeader(diff).toEditList();
					
					for (Edit editElement : editList)
						edits.add(editElement);
				} else {
					formatter.setDetectRenames(false);
				}
			}
		}
		
		return edits;
	}

}