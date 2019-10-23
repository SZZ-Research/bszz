package nz.ac.otago.miner.szz.model;

import java.util.Date;

public class Blame {
	private String revision;
	private String lineNumber;
	private String lineContent;
	private String path;
	private String bug_id;
	private Date date;
	private String project;
	private boolean insideReleasePeriod;
	
	public boolean isInsideReleasePeriod() {
		return insideReleasePeriod;
	}
	public void setInsideReleasePeriod(boolean insideReleasePeriod) {
		this.insideReleasePeriod = insideReleasePeriod;
	}
	public String getRevision() {
		return revision;
	}
	public void setRevision(String revision) {
		this.revision = revision;
	}
	public String getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}
	public String getLineContent() {
		return lineContent;
	}
	public void setLineContent(String lineContent) {
		this.lineContent = lineContent;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getBug_id() {
		return bug_id;
	}
	public void setBug_id(String bug_id) {
		this.bug_id = bug_id;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}
}
