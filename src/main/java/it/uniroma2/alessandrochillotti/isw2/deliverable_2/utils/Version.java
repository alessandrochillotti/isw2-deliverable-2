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
	private ArrayList<RevCommit> commits;
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
	
	public List<RevCommit> getCommits() {
		return commits;
	}
	
	public RevCommit getLastCommit() {
		if (commits.isEmpty()) 
			return null;
			
		return commits.get(commits.size()-1);
	}
	
	public void setCommits(List<RevCommit> commits) {
		this.commits = (ArrayList<RevCommit>) commits;
	}
	
	public void setFiles(List<ClassFile> list) {
		this.files = (ArrayList<ClassFile>) list;
	}
	
	public List<ClassFile> getFiles() {
		return files;
	}
	
	public boolean containFile(ClassFile file) {
		String nameTarget = file.getFullName();
		
		for (int i = 0; i < files.size(); i++) {
			String name = files.get(i).getFullName();
		
			if (name.equals(nameTarget)) 
				return true;
		}
		
		return false;
	}
	
	public boolean isBefore(Version toCompare) {
		return beginDate.isBefore(toCompare.getBeginDate());
	}
	
	@Override
	public int compareTo(Version object) {
		return getBeginDate().compareTo(object.getBeginDate());
	}
	
	
	@Override
	public boolean equals(Object version) {
		return false;
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
}
