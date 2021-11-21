package it.uniroma2.alessandrochillotti.isw2.deliverable_2.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.DateManager;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Ticket;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Version;

public class JSONApi {
	public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();

		BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		String jsonText = readAll(rd);

		rd.close();

		return new JSONObject(jsonText);
	}

	public JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();

		BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		String jsonText = readAll(rd);
		
		rd.close();
		
		return new JSONArray(jsonText);
	}
	
	public Ticket makeTicket(JSONObject entry, String format) {
		DateManager dateApi = new DateManager();
		JSONObject fields = entry.getJSONObject("fields");
		
		// Create basic Ticket
		String key = entry.get("key").toString();
		String resolutionDate = fields.getString("resolutiondate");
		String creationDate = fields.getString("created");
		
		// Make basic version of ticket
		Ticket ticket = new Ticket(key, dateApi.getLocalDateTime(creationDate, format), dateApi.getLocalDateTime(resolutionDate, format));
		
		// Put info for Affected Version
		JSONArray affectedVersions = fields.getJSONArray("versions");
		for (int i = 0; i < affectedVersions.length(); i++) {
			JSONObject version = affectedVersions.getJSONObject(i);
			
			String id = version.get("id").toString();
			String name = version.get("name").toString();
			LocalDate date = dateApi.getLocalDate(version.get("releaseDate").toString(), "yyyy-MM-dd");
			
			ticket.addAffectedVersion(new Version(id, name, date.atStartOfDay()));
		}
		
		// Put info for Fixed Version
		JSONArray fixedVersions = fields.getJSONArray("fixVersions");
		for (int i = 0; i < fixedVersions.length(); i++) {
			JSONObject version = fixedVersions.getJSONObject(i);
			
			if (version.getBoolean("released")) {
				String id = version.get("id").toString();
				String name = version.get("name").toString();
				LocalDate date = dateApi.getLocalDate(version.get("releaseDate").toString(), "yyyy-MM-dd");
				
				ticket.addAffectedVersion(new Version(id, name, date.atStartOfDay()));
			}
		}
		
		return ticket;
	}

	private String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
}
