package it.uniroma2.alessandrochillotti.isw2.deliverable_2;

/*
 * 
 * The JiraAnalyzer is a tool to retrieve the versions of a project from Jira platform.
 * This software maintain an ArrayList of version of project.
 * 
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.json.JSONException;
import org.json.JSONObject;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.api.JSONApi;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.parameters.Parameters;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Ticket;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Version;

import org.json.JSONArray;

public class JiraAnalyzer {

	private String project;

	public JiraAnalyzer(String project) {
		this.project = project;
	}

	/**
	 * This method return the list of versions of project
	 *
	 * @return list of versions of project
	 */
	public List<Version> retrieveVersions() throws IOException, JSONException {
		String url = "https://issues.apache.org/jira/rest/api/2/project/" + project;
		JSONApi jsonApi = new JSONApi();
		JSONObject json = jsonApi.readJsonFromUrl(url);
		JSONArray versions = json.getJSONArray("versions");

		List<Version> outcome = new ArrayList<>();

		for (int i = 0; i < versions.length(); i++) {
			String name = "";
			String id = "";
			if (versions.getJSONObject(i).has("releaseDate")) {
				if (versions.getJSONObject(i).has("name"))
					name = versions.getJSONObject(i).get("name").toString();
				if (versions.getJSONObject(i).has("id"))
					id = versions.getJSONObject(i).get("id").toString();
				addRelease(outcome, versions.getJSONObject(i).get("releaseDate").toString(), name, id);
			}
		}

		// In this line defines the order to sort the releases
		Collections.sort(outcome);

		// Set end date for each version
		for (int i = 0; i < outcome.size() - 1; i++) {
			outcome.get(i).setEndDate(outcome.get(i + 1).getBeginDate());
		}

		return outcome;
	}

	/**
	 * This method return the bug ticket list of project
	 *
	 * @return list bug ticket list of project
	 */
	public List<Ticket> retrieveBugTickets() throws JSONException, IOException {
		JSONApi jsonApi = new JSONApi();
		List<Ticket> bugTickets = new ArrayList<>();
		Integer j = 0;
		Integer i = 0;
		Integer total = 1;
		
		// Get JSON API for closed bugs w/ AV in the project
		do {
			// Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
			j = i + Parameters.WINDOW_SIZE;
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22" + Parameters.PROJECT_NAME
					+ "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
					+ "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
					+ i.toString() + "&maxResults=" + j.toString();
			JSONObject json = jsonApi.readJsonFromUrl(url);
			JSONArray issues = json.getJSONArray("issues");
			total = json.getInt("total");
			
			// Iterate through each bug ticket
			for (; i < total && i < j; i++) {
				Ticket current = jsonApi.makeTicket(issues.getJSONObject(i%Parameters.WINDOW_SIZE), Parameters.DATE_FORMAT);
				bugTickets.add(current);
			}
		} while (i < total);
		
		return bugTickets;
	}
	
	private void addRelease(List<Version> sandList, String strDate, String name, String id) {
		LocalDateTime dateTime = LocalDate.parse(strDate).atStartOfDay();

		Version incomer = new Version(id, name, dateTime);
		if (!sandList.contains(incomer)) {
			sandList.add(incomer);
		}
	}
}