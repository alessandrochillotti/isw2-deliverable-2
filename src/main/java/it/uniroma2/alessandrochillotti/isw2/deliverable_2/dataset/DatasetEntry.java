package it.uniroma2.alessandrochillotti.isw2.deliverable_2.dataset;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.jgit.revwalk.RevCommit;

import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.ClassFile;
import it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils.Version;

public class DatasetEntry {
	
	private Version version;
	private ClassFile file;
	private int size;
	private int addLines;
	private int delLines;
	private int maxChurn;
	private int maxAddLines;
	private int age;
	private boolean buggy;
	private ArrayList<RevCommit> commits;
	
	public DatasetEntry(Version version, ClassFile file, int size) {
		this.version = version;
		this.file = file;
		this.size = size;
		this.addLines = 0;
		this.delLines = 0;
		this.maxChurn = 0;
		this.maxAddLines = 0;
		this.buggy = false;
		commits = new ArrayList<>();
	}
	
	public Version getVersion() {
		return version;
	}
	
	public ClassFile getFile() {
		return file;
	}
	
	public int getSize() {
		return size;
	}
	
	public int getAddLines() {
		return addLines;
	}
	
	private double getAvgAddLines() {
		if (commits.isEmpty())
			return 0;
				
		return ((double)addLines/commits.size());
	}
	
	public void updateChurn(int addition, int deletion) {
		if (addition > maxAddLines) 
			maxAddLines = addition;
		
		addLines += addition;
		delLines += deletion;
		
		int churn = addition - deletion;
		if (churn > maxChurn) 
			maxChurn = churn;
		size += churn;
	}
	
	public void addCommit(RevCommit commit) {
		commits.add(commit);
	}
	
	private int getNumberRevisions() {
		return commits.size();
	}
	
	private double getAvgChurn() {
		if (commits.isEmpty())
			return 0;
		
		return ((double)getChurn()/commits.size());
	}
	
	private int getChurn() {
		return addLines - delLines;
	}
	
	private int getNumberAuthors() {
		ArrayList<String> authors = new ArrayList<>();
		
		for (RevCommit commit: commits) {
			if (!authors.contains(commit.getAuthorIdent().getName())) {
				authors.add(commit.getAuthorIdent().getName());
			}
		}
		
		return authors.size();
	}
	
	public void setAge(LocalDateTime endDate) {		
		age = (int)ChronoUnit.WEEKS.between(file.getCreationDate(), endDate);
	}
	
	public int getAge() {
		return age;
	}
	
	public void setBuggy(boolean buggy) {
		this.buggy = buggy;
	}
	
	public String getBuggy() {
		if(buggy) 
			return "Yes";
		else
			return "No";
	}
	
	public String toCSV() {
		return String.format("%s,%s,%d,%d,%d,%d,%d,%s,%d,%d,%s,%d,%s%n", 
				version.getVersionName(), file.getFullName(), getSize(), getNumberRevisions(), 
				getNumberAuthors(), addLines, maxAddLines, String.format(Locale.US, "%.2f", getAvgAddLines()),
				getChurn(), maxChurn, String.format(Locale.US, "%.2f", getAvgChurn()), getAge(), getBuggy()
		);
	}
}
