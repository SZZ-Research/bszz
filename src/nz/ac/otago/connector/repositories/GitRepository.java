package nz.ac.otago.connector.repositories;

import org.eclipse.jgit.api.Git;

public class GitRepository {

	public GitRepository(Git git){
		this.git = git;
	}

	private Git git;
	
	/**
	 * Get git.
	 *
	 * @return git as Git.
	 */
	public Git getGit() {
	    return git;
	}
	
	/**
	 * Set git.
	 *
	 * @param git the value to set.
	 */
	public void setGit(Git git) {
	    this.git = git;
	}
}
