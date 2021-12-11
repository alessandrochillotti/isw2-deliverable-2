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
	private JSONApi jsonApi;
	private List<Version> versions;
	private List<Ticket> bugTickets;
	private VersionAnalyzer versionApi;

	public JiraAnalyzer(String project) throws JSONException {
		this.project = project;
		this.jsonApi = new JSONApi();
		this.versions = new ArrayList<>();
		this.bugTickets = new ArrayList<>();
	}

	/**
	 * This method return the list of versions of project
	 *
	 * @return list of versions of project
	 */
	public List<Version> retrieveVersions() throws IOException, JSONException {
		String url = "https://issues.apache.org/jira/rest/api/2/project/" + project;
		JSONObject json = jsonApi.readJsonFromUrl(url);
		JSONArray jsonVersions = json.getJSONArray("versions");

		for (int i = 0; i < jsonVersions.length(); i++) {
			String name = "";
			String id = "";
			if (jsonVersions.getJSONObject(i).has("releaseDate")) {
				if (jsonVersions.getJSONObject(i).has("name"))
					name = jsonVersions.getJSONObject(i).get("name").toString();
				if (jsonVersions.getJSONObject(i).has("id"))
					id = jsonVersions.getJSONObject(i).get("id").toString();
				addRelease(versions, jsonVersions.getJSONObject(i).get("releaseDate").toString(), name, id);
			}
		}

		// In this line defines the order to sort the releases
		Collections.sort(versions);

		// Set end date for each version
		for (int i = 0; i < versions.size() - 1; i++) {
			versions.get(i).setEndDate(versions.get(i + 1).getBeginDate());
		}
		
		// Create structure for versions analyzer
		ArrayList<Version> versionsToStructure = new ArrayList<>();
		for (Version version: versions) {
			versionsToStructure.add(version);
		}

		versionApi = new VersionAnalyzer(versionsToStructure);
		
		return versions;
	}

	/**
	 * This method return the bug ticket list of project
	 *
	 * @return list bug ticket list of project
	 */
	public List<Ticket> retrieveBugTickets() throws JSONException, IOException {
		Integer j = 0;
		Integer i = 0;
		Integer total = 1;
		
		// Get JSON API for closed bugs w/ AV in the project
		do {
			// Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
			j = i + Parameters.WINDOW_SIZE;
			
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project%20%3D%20" + project + "%20"
					+ "AND%20issuetype%20%3D%20Bug%20"
					+ "AND%20status%20in%20%28Resolved%2C%20Closed%29%20"
					+ "AND%20resolution%20%3D%20Fixed%20"
					+ "AND%20fixVersion%20in%20releasedVersions%28%29%20"
					+ "AND%20created%20%3C%3D%20" + versions.get(versions.size()-1).getEndDate().toString().substring(0, 10) + "%20"
					+ "ORDER%20BY%20created%20ASC"
					+ "&fields=key,resolutiondate,versions,created,fixVersions"
					+ "&startAt=" + i.toString() + "&maxResults=" + j.toString();
			
			JSONObject json = jsonApi.readJsonFromUrl(url);
			JSONArray issues = json.getJSONArray("issues");
			total = json.getInt("total");
			
			// Iterate through each bug ticket
			for (; i < total && i < j; i++) {
				Ticket current = jsonApi.makeTicket(issues.getJSONObject(i%Parameters.WINDOW_SIZE), Parameters.DATE_FORMAT);
				if (!current.getAffectedVersions().isEmpty() && !current.getFixVersion().isEmpty())
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
	
	public void proportionMethod(List<Ticket> tickets) {
		double[] pSum = new double[versions.size()];
		int[] cntTicket = new int[versions.size()];
		int indexVersion = 0;
		int i = 0;
		
		// Clean list removing ticket in first version without AV
		cleanTickets(tickets);
		
		while (i < tickets.size()) {
			Ticket ticket = tickets.get(i);
			indexVersion = versionApi.getIndexVersion(versionApi.getOpeningVersion(ticket));
			double actualPSum = 0;
			int actualCountTicket = 0;
			
			for (int j = 0; j < indexVersion; j++) {
				actualPSum = actualPSum + pSum[j];
				actualCountTicket = actualCountTicket + cntTicket[j];
			}
			
			boolean malformed = updateProportion(ticket, actualPSum, actualCountTicket);
			
			if (!malformed) {
				Version fv = ticket.getFixVersion().get(ticket.getFixVersion().size()-1);
				Version iv = ticket.getAffectedVersions().get(0);
				Version ov = versionApi.getOpeningVersion(ticket);
				
				double fvIv = versionApi.getNumberIntermediateVersions(iv, fv);
				double fvOv = versionApi.getNumberIntermediateVersions(ov, fv);
				if (fvOv != 0) { 
					double currentP = fvIv / fvOv; 
					pSum[indexVersion] += currentP;
				}
				cntTicket[indexVersion]++;
			} else {
				tickets.remove(i);
				i--;
			}	
			
			i++;
		}
	}
	
	private void cleanTickets(List<Ticket> tickets) {
		int cnt = 0;
		int indexVersion = 0;
		
		while(cnt < tickets.size() && indexVersion == 0) {
			Ticket ticket = tickets.get(cnt);
			indexVersion = versionApi.getIndexVersion(versionApi.getOpeningVersion(ticket));
			
			if (indexVersion == 0 && ticket.getAffectedVersions().isEmpty() || 
					ticket.getAffectedVersions().get(0).getVersionName().equals(ticket.getFixVersion().get(0).getVersionName())) {
				tickets.remove(cnt);
				cnt--;
			}
			
			cnt++;
		}
	}
	
	private boolean updateProportion(Ticket ticket, double actualPSum, int actualCountTicket) {
		boolean malformed = false;
		if (ticket.getAffectedVersions().isEmpty()) {
			Version fv = ticket.getFixVersion().get(ticket.getFixVersion().size()-1);
			Version ov = versionApi.getOpeningVersion(ticket);
			
			// predicted IV = FV - (FV - OV) * current_p / N ticket
			int fvOv = versionApi.getNumberIntermediateVersions(ov, fv);
			if (fvOv != 0 && actualCountTicket != 0) {
				Version predictedIv = versionApi.subtraction(fv, (int) (fvOv*Math.round(actualPSum/actualCountTicket)));
				
				ArrayList<Version> affectedVersions = (ArrayList<Version>) versionApi.getIntermediateVersions(predictedIv, fv);
				
				for (Version av: affectedVersions) {
					ticket.addAffectedVersion(av);
				}
			} else {
				malformed = true;
			}
		}
		return malformed;
	}
}