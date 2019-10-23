package nz.ac.otago.connector;

import nz.ac.otago.connector.repositories.GitRepository;

public class GitConnector extends Connector<GitRepository> {
	private String repoUrl;

	public String getRepoUrl() {
		return repoUrl;
	}

	public void setRepoUrl(String repoUrl) {
		this.repoUrl = repoUrl;
	}
	
}
