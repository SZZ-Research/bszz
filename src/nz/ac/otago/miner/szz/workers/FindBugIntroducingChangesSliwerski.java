package nz.ac.otago.miner.szz.workers;

//{{{ import statements myapp
import nz.ac.otago.miner.szz.model.Header;
import nz.ac.otago.miner.szz.model.DiffHunk;
import nz.ac.otago.miner.szz.model.BugIntroducingCode;
import nz.ac.otago.miner.szz.model.LineType;
import nz.ac.otago.miner.szz.model.Line;
import nz.ac.otago.utils.RepoOperations;
import nz.ac.otago.persistence.LinkedIssueDAO;
import nz.ac.otago.connector.repositories.GitRepository;
//}}}

//{{{ import statements java
import java.util.List;
import java.util.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.Console;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
//}}}

//{{{ import statements log4j and hibernate
import org.apache.log4j.Logger;
import org.hibernate.Transaction;
//}}}

//{{{ import statenents jgit
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.util.IntList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.lib.ObjectReader;
//}}}


public class FindBugIntroducingChangesSliwerski implements Runnable {
	private static final Logger log = Logger.getLogger(FindBugIntroducingChangesSliwerski.class);
	private static String project;
	private String rev;
	private static LinkedIssueDAO lidao;
	private Git git;
	private GitRepository encap;
	private String url;
	private Console c = System.console(); //for debugging purposes
	private List<String> linkedRevs = new ArrayList<String>();
	private List<String> revisionsProcessed = new ArrayList<String>();
	private int threadId;

	//{{{ Constructor() -> FindBugIntroducingChangesSliwerski(String project, LinkedIssueDAO lidao, GitRepository encap, String url)
	public FindBugIntroducingChangesSliwerski(String project, LinkedIssueDAO lidao, 
			GitRepository encap, String url, List<String> linkedRevs, List<String> revisionsProcessed,
			int threadId){
		this.project = project;
		this.lidao = lidao;
		this.encap = encap;
		this.url = url;
		this.linkedRevs = linkedRevs;
		this.revisionsProcessed = revisionsProcessed;
		this.threadId = threadId;
	}
	//}}}

	//{{{ run()
	public void run()  {
		try{
			git = encap.getGit();
			Repository repo = git.getRepository();
			log.info(String.format("thread:%d: %s - %d Linked revisions found... for project %s", threadId, project, linkedRevs.size(), project));
			long logcount = 1;
			for(String newhash : linkedRevs){
				//in case we needed to stop the process
				if(revisionsProcessed.contains(newhash)){
					log.info(String.format("thread:%d: %s - revision %s was processed already!",threadId, project, newhash));
					logcount++;
					log.info(String.format("thread:%d: %s - %d revisions processed of %s", threadId, project, logcount, linkedRevs.size()));
					continue;
				}
				List<BugIntroducingCode> bugchanges = new ArrayList<BugIntroducingCode>();
				try{
					RevCommit newCommit = null;
					try(RevWalk walk = new RevWalk(repo)){
						newCommit = walk.parseCommit(repo.resolve(newhash));
					}
					if(newCommit != null){
						RevCommit oldCommit = getPrevHash(newCommit,repo);
						List<DiffHunk> hunks = getHunks(newCommit,oldCommit);
						bugchanges.addAll(blame(hunks, oldCommit, newCommit, repo)); 
						log.debug(String.format("%s - %d bics found!",project , bugchanges.size()));
					}
				} catch (Exception e) {
					logcount++;
					log.error(String.format("thread:%d: %s - Revision %s ignored due to error: \n%s",threadId, project, newhash, e.getMessage()));
					e.printStackTrace();
					log.info(String.format("thread:%d: %s - %d revisions processed of %s", threadId, project, logcount, linkedRevs.size()));
				}
				persistBugIntroChanges(bugchanges);
				log.info(String.format("thread:%d: %s - %d revisions processed of %s", threadId, project, logcount, linkedRevs.size()));
				logcount++;
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	//}}}

	//{{{ getPrevHash(RevCommit commit)
	private RevCommit getPrevHash(RevCommit commit, Repository repo) throws IOException {
		
		try(RevWalk walk = new RevWalk(repo)){
			walk.markStart(commit);
			int step = 0;
			for(RevCommit rev : walk){
				if(step == 1){
					return rev;
				}
				step++;
			}
			walk.dispose();
		}
		return null;
	}//}}}
	
	//{{{ getHunks(RevCommit newCommit, RevCommit oldCommit)
	private List<DiffHunk> getHunks(RevCommit newCommit, RevCommit oldCommit) throws Exception {

		AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(oldCommit);
		AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(newCommit);
		ByteArrayOutputStream mydiff = new ByteArrayOutputStream();
		try(DiffFormatter formatter = new DiffFormatter(mydiff)){
			formatter.setRepository(git.getRepository());
			formatter.format(oldTreeIterator,newTreeIterator);
		}
		String prevhash = oldCommit.getId().getName();
		String nexthash = newCommit.getId().getName();
		List<Header> headers = RepoOperations.getHeaders(mydiff);
		log.debug(String.format("headers size: %s",headers.size()));
		List<DiffHunk> hunks = RepoOperations.getDiffHunks(mydiff, headers, prevhash, nexthash);
		return hunks;
	}
	//}}}

	//{{{ blame() 
	private List<BugIntroducingCode> blame(List<DiffHunk> hunks, RevCommit oldCommit, RevCommit newCommit, Repository repo) throws Exception {

		List<BugIntroducingCode> bics = new ArrayList<BugIntroducingCode>();
		BlameCommand blamer = new BlameCommand(repo);
		blamer.setStartCommit(oldCommit);
		for(DiffHunk hunk : hunks){
			String filepath = hunk.getHeader().getPreviousPath();
			log.debug("path: " + filepath);
			if(filepath.trim().length() == 0){
				continue;
			}
			blamer.setFilePath(filepath);
			BlameResult blame = blamer.call();
			MyRawText blameContent = new MyRawText(blame.getResultContents().getRawContent());
			List<Line> removedLines = hunk.getRemovedLines();
			for(Line removedLine : removedLines){
				String removed = removedLine.getContent().replaceFirst("-","").trim();
				if(removed.startsWith("-- ")){
					continue;
				}
				//we add -1 because blame starts with line number 0
				int blameLineNumber = removedLine.getPreviousNumber()-1;
				log.debug(String.format("filepath: %s",filepath));
				log.debug(String.format("content: %s",removed));
				log.debug(String.format("%s - previousLineNumber: %d, blameLineNumber: %d", 
							project, 
							removedLine.getPreviousNumber(),
							blameLineNumber));
				String blameLine = "";
				try{
					blameLine = blameContent.getString(blameLineNumber).trim();
				} catch (ArrayIndexOutOfBoundsException outbound) {
					log.error("Ops! Array out of bound, let's skip this one");
					continue;
				}
				int sourceLineNumber = blame.getSourceLine(blameLineNumber);
				log.debug(String.format("%s - ### Comparing removed line:\n#%d - %s\n### Against blameline:\n#%d - %s",
							project,
							removedLine.getPreviousNumber(),
							removed, 
							sourceLineNumber, 
							blameLine));
				//#debug
				//c.readLine("ok move on?!");
				if(blameLine.equals(removed)){
					log.debug(String.format("%s - Yep! we caught the bug intro change!", project));
					String buggyHash = blame.getSourceCommit(blameLineNumber).getName();
					RevCommit buggyCommit = null;
					try(RevWalk walk = new RevWalk(repo)){
						buggyCommit = walk.parseCommit(repo.resolve(buggyHash));
					}
					if(buggyCommit != null){
						PersonIdent author = buggyCommit.getAuthorIdent();
						Date buggyCommitDate = author.getWhen();
						BugIntroducingCode bic = createBugIntroducingCode(sourceLineNumber, 
								                                  blameLine,
								                                  buggyHash,
								                                  newCommit.getId().getName(),
								                                  buggyCommitDate,
								                                  filepath);
						bics.add(bic);
						log.debug(String.format("%s - bics size: %s", project , bics.size()));
					}
				}
			}
		}
		return bics;
	} //}}}
	
	//{{{ persistBugIntroChanges(List<BugIntroducingCode> bugchanges)
	private static synchronized void persistBugIntroChanges(List<BugIntroducingCode> bugchanges) throws Exception{
		Transaction tx = lidao.beginTransaction();
		for(BugIntroducingCode bugchange : bugchanges){
			lidao.insertBugIntroducingCode(bugchange);
			lidao.insertProjectRevisionsProcessed(project,bugchange.getFixRevision());
		}
		tx.commit();
	}
	//}}}

	//{{{ createBugIntroducingCode()
	private BugIntroducingCode createBugIntroducingCode(int lineNumber, String content, String buggyHash, String fixHash, Date szzDate, String path) 
	 throws Exception{

		BugIntroducingCode bic = new BugIntroducingCode();
		bic.setLinenumber(lineNumber);//not the real line number, but we don't need to fix now
		bic.setContent(content); 
		bic.setRevision(buggyHash);
		bic.setFixRevision(fixHash);
		bic.setProject(this.project);
		bic.setSzzDate(szzDate);
		bic.setPath(path);
		return bic;
	}
	//}}}
	
	//{{{ getCanonicalTreeParser(ObjectId commitId)
	private AbstractTreeIterator getCanonicalTreeParser(ObjectId commitId) throws IOException {

		try (RevWalk walk = new RevWalk(git.getRepository())){
			RevCommit commit = walk.parseCommit(commitId);
			ObjectId treeId = commit.getTree().getId();
			try(ObjectReader reader = git.getRepository().newObjectReader()){
				return new CanonicalTreeParser(null, reader, treeId);
			}
		}
	}//}}}

	//{{{ class MyRawText
	class MyRawText extends RawText {

		protected IntList lines;

		public MyRawText(byte[] input) {
			super(input);	
			this.lines = super.lines;
		}
		
		public IntList getLines(){
			return this.lines;
		}

		public int getLinesSize(){
			return (this.lines.size())-2;
		}
	}
	//}}}
	
	/**
	 * Return an ISO 8601 combined date and time string for specified date/time
	 * 
	 * @param date
	 *            Date
	 * @return String with format "yyyy-MM-dd'T'HH:mm:ss'Z'"
	 *
	private static String getISO8601Date(Date date) throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String formatteddate = dateFormat.format(date);
		return formatteddate;
	}*/
}
