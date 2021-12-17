package it.uniroma2.alessandrochillotti.isw2.deliverable_2.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateApi {
	
	public LocalDateTime getLocalDateTime(String date, String format) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
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
	
	public LocalDate getLocalDate(String date, String format) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		StringBuilder dateParsed = new StringBuilder();
		
		// Parse date
		for (int i = 0; i < format.length(); i++) {
			if (date.charAt(i) != 'T') {
				dateParsed.append(date.charAt(i));
			} else {
				dateParsed.append(format.charAt(i));
			}
		}
		
		return LocalDate.parse(dateParsed, formatter);
	}
}
