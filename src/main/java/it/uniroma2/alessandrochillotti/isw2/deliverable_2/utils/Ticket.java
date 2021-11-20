package it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils;

import java.time.LocalDateTime;

public class Ticket {
	private String key;
	private LocalDateTime creationDate;
	private LocalDateTime resolutionDate;
	
	public Ticket(String key, LocalDateTime creationDate, LocalDateTime resolutionDate) {
		this.key = key;
		this.creationDate = creationDate;
		this.resolutionDate = resolutionDate;
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
}
