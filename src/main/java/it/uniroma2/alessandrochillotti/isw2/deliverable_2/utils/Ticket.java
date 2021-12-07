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
	private ArrayList<ClassFile> touchedFiles;
	
	public Ticket(String key, LocalDateTime creationDate, LocalDateTime resolutionDate) {
		this.key = key;
		this.creationDate = creationDate;
		this.resolutionDate = resolutionDate;
		this.affectedVersions = new ArrayList<>();
		this.fixVersions = new ArrayList<>();
		this.touchedFiles = new ArrayList<>();
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
	public void addTouchedFile(ClassFile file) {
		touchedFiles.add(file);
	}
	
	public List<ClassFile> getTouchedFiles(){
		return touchedFiles;
	}
	
	public void addFilesTouched(List<ClassFile> files) {
		if(touchedFiles == null) return;
		
		for(ClassFile newFile: files) {
			boolean present = false;
			
			for(int i = 0; !present && i < touchedFiles.size(); i++) {
				String file = files.get(i).getFullName();
				
				if(file.equals(newFile.getFullName())) present = true;
			}
			
			if(!present) touchedFiles.add(newFile);
		}
	}
	
	@Override
    public boolean equals(Object ticket){
        if(ticket instanceof Ticket){
            Ticket toCompare = (Ticket) ticket;
            return ((Ticket) ticket).getKey().equals(toCompare.getKey());
        }
        return false;
    }
	
	@Override
    public int hashCode(){
        return 1;
    }
}
