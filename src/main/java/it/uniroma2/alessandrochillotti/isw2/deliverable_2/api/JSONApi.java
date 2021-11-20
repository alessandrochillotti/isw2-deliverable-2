package it.uniroma2.alessandrochillotti.isw2.deliverable_2.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.DateManager;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Ticket;

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
		DateManager dateApi = new DateManager(format);
		
		String key = entry.get("key").toString();
		String resolutionDate = entry.getJSONObject("fields").getString("resolutiondate");
		String creationDate = entry.getJSONObject("fields").getString("created");
		
		return new Ticket(key, dateApi.getLocalDate(creationDate), dateApi.getLocalDate(resolutionDate));
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
