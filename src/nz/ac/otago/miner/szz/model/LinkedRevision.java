package nz.ac.otago.miner.szz.model;

import java.util.Date;

public class LinkedRevision {
	private Date reportingDate;
	private String revision;
	
	public Date getReportingDate() {
		return reportingDate;
	}

	public void setReportingDate(Date reportingDate) {
		this.reportingDate = reportingDate;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}
}
