package it.uniroma2.alessandrochillotti.isw2.deliverable_2.analyzers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

public class CommitAnalyzer {
	private RevCommit commit;
	
	public CommitAnalyzer(RevCommit commit) {
		this.commit = commit;
	}
	
	public LocalDateTime getCommitDate() {
		PersonIdent lastAuthorIdent = commit.getAuthorIdent();
		Date lastAuthorDate = lastAuthorIdent.getWhen();
		return lastAuthorDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}
}
