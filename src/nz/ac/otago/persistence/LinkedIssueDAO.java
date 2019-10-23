package nz.ac.otago.persistence;

import java.util.List;
import nz.ac.otago.miner.szz.model.*;
import java.util.Date;

public abstract class LinkedIssueDAO extends AbstractDAO {

	public abstract List<String> getLinkedRevisions(String project);
	
	public abstract List<String> getLastRevisionsProcessed(String project);

	public abstract void insertProjectRevisionsProcessed(String project, String hash);

	public abstract void insertBugIntroducingCode(BugIntroducingCode bicode);

}
