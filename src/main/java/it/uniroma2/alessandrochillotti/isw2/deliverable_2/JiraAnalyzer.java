package it.uniroma2.alessandrochillotti.isw2.deliverable_2;

/*
 * 
 * The JiraAnalyzer is a tool to retrieve the versions of a project from Jira platform.
 * This software maintain an ArrayList of version of project.
 * 
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.Collections;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.json.JSONException;
import org.json.JSONObject;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Version;

import org.json.JSONArray;

public class JiraAnalyzer {

	private static final Logger LOGGER = Logger.getLogger("JiraAnalyzer");
	
	private String project;
	private List<Version> outcome;
	
	public JiraAnalyzer(String project) {
		this.project = project;
	}
	
	public List<Version> retrieveVersions() throws IOException, JSONException {
		String url = "https://issues.apache.org/jira/rest/api/2/project/" + project;
		JSONObject json = readJsonFromUrl(url);
		JSONArray versions = json.getJSONArray("versions");
		
		outcome = new ArrayList<>();
		
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
		
		return outcome;
	}

	public void writeFile(String projName) {
		String filename = projName + "VersionInfo.csv";
		
		try (FileWriter fileWriter = new FileWriter(filename);){	
			fileWriter.append("Index,Version ID,Version Name,Date");
			fileWriter.append("\n");		
			
			for (int i = 0; i < outcome.size(); i++) {
				Integer index = i + 1;
				Version current = outcome.get(i);
				
				fileWriter.append(index.toString());
				fileWriter.append(",");
				fileWriter.append(current.getVersionID());
				fileWriter.append(",");
				fileWriter.append(current.getVersionName());
				fileWriter.append(",");
				fileWriter.append(current.getDateTime().toString());
				fileWriter.append("\n");
			}
		} catch (IOException e) {
			LOGGER.log(null, "Error in csv writer", e);
		}
	}
	
	public void addRelease(List<Version> sandList, String strDate, String name, String id) {
		LocalDateTime dateTime = LocalDate.parse(strDate).atStartOfDay();
		
		Version incomer = new Version(id, name, dateTime);
		if(!sandList.contains(incomer)) {
			sandList.add(incomer);
		}
	}

	public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		String jsonText = readAll(rd);
		
		rd.close();
		
		return new JSONObject(jsonText);
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