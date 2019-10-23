package nz.ac.otago.miner.szz;

//{{{ imports
import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import java.io.Console;

import nz.ac.otago.persistence.LinkedIssueDAO;
import nz.ac.otago.persistence.factory.FactoryDAO;
import nz.ac.otago.connector.Connector;
import nz.ac.otago.connector.GitConnector;
import nz.ac.otago.enums.ConnectorType;
import nz.ac.otago.enums.DAOType;
import nz.ac.otago.connector.factory.GitConnectorFactory;
import nz.ac.otago.miner.Miner;
import nz.ac.otago.miner.szz.model.BugIntroducingCode;
import nz.ac.otago.miner.szz.model.DiffHunk;
import nz.ac.otago.miner.szz.model.Line;
import nz.ac.otago.miner.szz.model.LineType;
import nz.ac.otago.miner.szz.model.LinkedRevision;
import nz.ac.otago.miner.szz.model.Project;
import nz.ac.otago.miner.szz.model.RelationTypes;
import nz.ac.otago.miner.szz.workers.FindBugIntroducingChangesSliwerski;
import nz.ac.otago.connector.repositories.GitRepository;
//}}}

public class SzzSliwerski extends Miner {

	private static final Logger log = Logger.getLogger(SzzSliwerski.class);
	private String project;
	private LinkedIssueDAO liDao;
	private GitConnector connector;
	private String repoUrl;
	private GitRepository encapsulation;
	private boolean entiredb;
	private Console cons = System.console();

	public static void main(String[] args) throws Exception {
		String user = "";
		String password = "";

		SzzSliwerski szz = new SzzSliwerski();
		String project = szz.cons.readLine("Okay, which project should I work on?\n"); 
		log.info(String.format("project being analyzed: %s",project.toUpperCase()));
		String url = szz.getProperty(project.toUpperCase(),"/projects.properties");
		log.info(String.format("url being analyzed: %s",url));
		Map<ConnectorType, Connector> cs = new HashMap<ConnectorType, Connector>();
		GitConnector c = null;
		try {
			log.info("creating connector and cloning repo...");
			c = new GitConnectorFactory().createConnector(user, password, project, url);
		} catch (Exception e){ 
			log.error(String.format("could not initialize GitConnector for url %s",url));
			throw e;
		}
		cs.put(ConnectorType.GIT, c);
		szz.setConnectors(cs);
		Map p = new HashMap();
		try {
			p.put("project", project);
			szz.setParameters(p);
			szz.executeMining();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void findBugIntroducingChanges() throws Exception {
		try{
			FindBugIntroducingChangesSliwerski worker = new FindBugIntroducingChangesSliwerski(this.project,
					this.liDao, 
					encapsulation, 
					this.repoUrl);
			log.info("initializing the runner!");
			worker.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void performSetup() throws Exception {
		log.info("perform setup ... ");
		try {
			this.project = (String) this.getParameters().get("project");
			connector = (GitConnector) connectors.get(ConnectorType.GIT);
			repoUrl = connector.getRepoUrl();
			encapsulation = connector.getEncapsulation();
			liDao = (FactoryDAO.getFactoryDAO(DAOType.HIBERNATE)).getLinkedIssueDAO();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void performMining() throws Exception {
		log.info("perform mining...");
		findBugIntroducingChanges();
	}

}

