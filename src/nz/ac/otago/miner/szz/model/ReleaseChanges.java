package nz.ac.otago.miner.szz.model;

public class ReleaseChanges {
	private String bug_id;
	private String type;
	private String action;
	private String path;
	
	public String getBug_id() {
		return bug_id;
	}
	public void setBug_id(String bug_id) {
		this.bug_id = bug_id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
}
