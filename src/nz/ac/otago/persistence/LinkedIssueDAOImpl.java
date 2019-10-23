
package nz.ac.otago.persistence;

import java.util.List;
import java.util.ArrayList;
import nz.ac.otago.miner.szz.model.BugIntroducingCode;

import org.hibernate.SQLQuery;

public class LinkedIssueDAOImpl extends LinkedIssueDAO {

	//{{{ getLinkedRevisions(String project) {
	public synchronized List<String> getLinkedRevisions(String project) {

		String sql = "select distinct(revisionnumber) from linkedissuessvn lsvn " +
			"where projectname like :project " +
			"and issuetype = 'Bug' " +
			"order by revisionnumber";
		List<String> revisions = new ArrayList<String>();
		SQLQuery query = currentSession.createSQLQuery(sql);
		query.setParameter("project", project);
		revisions = query.list();
		return revisions;
	}
	//}}}

	//{{{ getLastRevisionsProcessed(String project)
	public synchronized List<String> getLastRevisionsProcessed(String project){

		List<String> lastProcessedRevisions = new ArrayList<String>();
		String sql = "select lastrevisionprocessed from szz_project_lastrevisionprocessed " +
			"where project = :project";
		SQLQuery query = currentSession.createSQLQuery(sql);
		query.setParameter("project",project);
		List<String> results = (List<String>) query.list(); 
		if(results!=null){
			lastProcessedRevisions.addAll(results);
		}
		return lastProcessedRevisions;
	}//}}}

	//{{{ insertProjectRevisionsProcessed(String project, String hash)
	public synchronized void insertProjectRevisionsProcessed(String project, String hash){

		String sql = "insert into szz_project_lastrevisionprocessed (project, lastrevisionprocessed) values (:param1, :param2)";
		executeSQLWithParams(sql,project,hash);
	} //}}}

	//{{{ insertBugIntroducingCode(BugIntroducingCode bicode)
	public synchronized void insertBugIntroducingCode(BugIntroducingCode bicode){

		String sql = "insert into bszzbic (linenumber, path, content, revision, fixrevision, project, szz_date) "+
		       " values (:param1,:param2, :param3, :param4, :param5, :param6, :param7)";
		executeSQLWithParams(sql,bicode.getLinenumber(), bicode.getPath(), bicode.getContent(),
				bicode.getRevision(), bicode.getFixRevision(), bicode.getProject(), 
				bicode.getSzzDate());	       
	}//}}}
}
