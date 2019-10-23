package nz.ac.otago.miner.szz.model;

import java.util.List;

public class LineNode {
	private long lineNumber;
	private long revision;
	private String type;
	private LineNode evolution;
	private String content;
	private List<LineNode> spawns;	
	
	public List<LineNode> getSpawns() {
		return spawns;
	}
	public void setSpawns(List<LineNode> spawns) {
		this.spawns = spawns;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}	
	public long getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(long lineNumber) {
		this.lineNumber = lineNumber;
	}
	public long getRevision() {
		return revision;
	}
	public void setRevision(long revision) {
		this.revision = revision;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public LineNode getEvolution() {
		return evolution;
	}
	public void setEvolution(LineNode evolution) {
		this.evolution = evolution;
	}
}
