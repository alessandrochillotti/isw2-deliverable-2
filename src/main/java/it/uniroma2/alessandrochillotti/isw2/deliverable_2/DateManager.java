package it.uniroma2.alessandrochillotti.isw2.deliverable_2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.parameters.Parameters;

public class DateManager {
	
	private String format;
	
	public DateManager(String format) {
		this.format = format;
	}
	
	public LocalDateTime getLocalDate(String date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Parameters.DATE_FORMAT);
		StringBuilder dateParsed = new StringBuilder();
		
		// Parse date
		for (int i = 0; i < format.length(); i++) {
			if (date.charAt(i) != 'T') {
				dateParsed.append(date.charAt(i));
			} else {
				dateParsed.append(format.charAt(i));
			}
		}
		
		return LocalDateTime.parse(dateParsed, formatter);
	}
}
