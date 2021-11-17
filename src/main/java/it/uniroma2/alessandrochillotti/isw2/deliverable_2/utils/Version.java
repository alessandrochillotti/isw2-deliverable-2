package it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils;

import java.time.LocalDateTime;

public class Version implements Comparable<Version> {
	
	private String versionID;
	private String versionName;
	private LocalDateTime date;
	
	public Version(String versionID, String versionName, LocalDateTime date) {
		this.versionID = versionID;
		this.versionName = versionName;
		this.date = date;
	}
	
	public String getVersionID() {
		return versionID;
	}
	
	public String getVersionName() {
		return versionName;
	}
	
	public LocalDateTime getDateTime() {
		return date;
	}
	
	@Override
	public int compareTo(Version object) {
		return getDateTime().compareTo(object.getDateTime());
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
