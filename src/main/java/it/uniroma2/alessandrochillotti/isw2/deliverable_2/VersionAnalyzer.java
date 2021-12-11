package it.uniroma2.alessandrochillotti.isw2.deliverable_2;

import java.util.ArrayList;
import java.util.List;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Ticket;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Version;

public class VersionAnalyzer {
	
	private ArrayList<Version> versions;
	
	public VersionAnalyzer(List<Version> versions) {
		this.versions = (ArrayList<Version>) versions;
	}
	
	public List<Version> getIntermediateVersions(Version begin, Version end) {
		ArrayList<Version> intermediateVersions = new ArrayList<>();
		int start = 0;
		int last = 0;
		
		// Find start and last index
		for (int i = 0; i < versions.size(); i++) {
			Version version = versions.get(i);
			if (version.getVersionName().equals(begin.getVersionName())) {
				start = i;
			}
			if (version.getVersionName().equals(end.getVersionName())) {
				last = i;
			}
		}
		
		// Take the versions
		for (int i = start; i < last; i++) {
			intermediateVersions.add(versions.get(i));
		}
		
		return intermediateVersions;
	}
	
	public int getNumberIntermediateVersions(Version begin, Version end) {
		return getIntermediateVersions(begin, end).size();
	}
	
	public Version getOpeningVersion(Ticket ticket) {
		if (ticket.getCreationDate().isBefore(versions.get(0).getBeginDate())) 
			return versions.get(0);
		
		for (Version version: versions) {
			if (ticket.getCreationDate().isAfter(version.getBeginDate()) && ticket.getCreationDate().isBefore(version.getEndDate()))
				return version;
		}
		
		return null;
	}
	
	public int getIndexVersion(Version version) {
		for(int i = 0; i < versions.size(); i++) {
			if (versions.get(i).getVersionName().equals(version.getVersionName())) 
				return i;
		}
		
		return -1;
	}
	
	/**
	 * 
	 * @param version
	 * @param jump	number of jumps to back
	 * @return
	 */
	public Version subtraction(Version version, int jump) {
		int index = 0;
		for(int i = 0; i < versions.size(); i++) {
			if (versions.get(i).getVersionName().equals(version.getVersionName()))
				index = i;
		}
		
		return versions.get(index - jump);
	}
	
	public int numberVersion() {
		return versions.size();
	}
}
