package nz.ac.otago.connector.factory;

//{{{ imports
import nz.ac.otago.connector.GitConnector;
import nz.ac.otago.connector.repositories.GitRepository;
import nz.ac.otago.utils.RepoOperations;
import org.apache.log4j.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.lib.Repository;


import java.io.File;
//}}}

public class GitConnectorFactory extends ConnectorFactory {

	private static final Logger log = Logger.getLogger(GitConnectorFactory.class);

	@Override
	public GitConnector createConnector(String user, String password, String path, String url) throws Exception {
		GitConnector connector = null;
		
		try {
			String repoPath = String.format(".gitfiles/%s",path);
			File repoDir = new File(repoPath);
			if(!repoDir.exists()){
				RepoOperations.cloneGitRepo(url, repoPath);
			}
			connector = new GitConnector();
			//Initialize Git Repository
			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			log.info(String.format("repo dir: %s",repoDir.getAbsolutePath()));
			Repository localrepo = builder.setGitDir(new File(String.format("%s/%s",repoPath,".git/"))).setMustExist(true).build();
			Git git = new Git(localrepo);
			connector.setEncapsulation(new GitRepository(git));
			return connector;
		}
		catch(Exception e) {
			log.info(String.format("there's a problem when cloning the repo %s ", url));
			throw e;
		}
	}

}
