package it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

public class Ticket {
	private String key;
	private LocalDateTime creationDate;
	private LocalDateTime resolutionDate;
	private ArrayList<Version> affectedVersions;
	private ArrayList<Version> fixVersions;
	private ArrayList<RevCommit> commits; 
	
	public Ticket(String key, LocalDateTime creationDate, LocalDateTime resolutionDate) {
		this.key = key;
		this.creationDate = creationDate;
		this.resolutionDate = resolutionDate;
		affectedVersions = new ArrayList<>();
		fixVersions = new ArrayList<>();
		commits = new ArrayList<>();
	}

	public String getKey() {
		return key;
	}
	
	public LocalDateTime getCreationDate() {
		return creationDate;
	}
	
	public LocalDateTime getResolutionDate() {
		return resolutionDate;
	}
	
	public void addAffectedVersion(Version affectedVersion) {
		affectedVersions.add(affectedVersion);
	}
	
	public List<Version> getAffectedVersions() {
		return affectedVersions;
	}
	
	public void addFixVersion(Version fixVersion) {
		 fixVersions.add(fixVersion);
	}
	
	public List<Version> getFixVersion() {
		return fixVersions;
	}
	
	public void addCommit(RevCommit commit) {
		commits.add(commit);
	}
	
	public List<RevCommit> getCommits() {
		return commits;
	}
}
