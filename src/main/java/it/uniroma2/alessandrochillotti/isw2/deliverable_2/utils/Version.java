package it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

public class Version implements Comparable<Version> {
	
	private String versionID;
	private String versionName;
	private LocalDateTime beginDate;
	private LocalDateTime endDate;
	private RevCommit lastCommit;
	private ArrayList<ClassFile> files;
	
	public Version(String versionID, String versionName, LocalDateTime beginDate) {
		this.versionID = versionID;
		this.versionName = versionName;
		this.beginDate = beginDate;
	}

	public String getVersionID() {
		return versionID;
	}
	
	public String getVersionName() {
		return versionName;
	}
	
	public LocalDateTime getBeginDate() {
		return beginDate;
	}
	
	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}
	
	public LocalDateTime getEndDate() {
		return endDate;
	}
	
	public RevCommit getLastCommit() {
		return lastCommit;
	}
	
	public void setLastCommit(RevCommit lastCommit) {
		this.lastCommit = lastCommit;
	}
	
	public void setFiles(List<ClassFile> list) {
		this.files = (ArrayList<ClassFile>) list;
	}
	
	public List<ClassFile> getFiles() {
		return files;
	}
	
	@Override
	public int compareTo(Version object) {
		return getBeginDate().compareTo(object.getBeginDate());
	}
	
	/* The following two overrides there are only for respecting contract */	
	@Override
	public boolean equals(Object obj) {
		return false;
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
}
