package it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

public class ClassFile {
	private String fullName;
	private LocalDateTime creationDate;
	private List<RevCommit> commits;
	private List<PersonIdent> authors;
	
	public ClassFile(String fullName) {
		this.fullName = fullName;
		this.commits = new ArrayList<>();
		this.authors = new ArrayList<>();
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public LocalDateTime getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}
	
	public void addCommit(RevCommit commit) {
		commits.add(commit);
	}
	
	public void addAuthor(PersonIdent author) {
		authors.add(author);
	}
	
	public List<PersonIdent> getAuthors() {
		return authors;
	}
}
