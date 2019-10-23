//{{{ imports
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.util.IntList;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import java.io.File;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Console;
import org.apache.log4j.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.blame.BlameResult;
//}}}

public class GitRepoTest {

	//470476e60e9ccc810a9805a537fef1456cd26dde (4 files changed)
	private static final Logger log = Logger.getLogger(GitRepoTest.class);
	private Git git;
	private Repository localrepo;
	private String pathtorepo;
	private Console c = System.console();
	List<String> removedLines = new ArrayList<String>();
	private RevCommit globalprevcommit;
	Map<String,List<String>> filepathsANDmodifications = new HashMap<String,List<String>>();

	//{{{ main method
	public static void main(String[] args) throws Exception {
		log.info("#### INITIATING!!! ####");
		GitRepoTest grt = new GitRepoTest();
		grt.pathtorepo = "raszzprime";
		String commithash = grt.c.readLine("tell me the hash of the commit to analyze?\n:");
		grt.diffCommit(commithash);
		//grt.showBlame(grt.localrepo, grt.globalprevcommit);
		//grt.readElementsAt();
	}
	//}}}

	//{{{ diffCommit(String hashID)
	public String diffCommit(String hashID) throws Exception {

		//Initialize repositories
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		localrepo = builder.setGitDir(new File(String.format("%s/.git",pathtorepo))).setMustExist(true).build();
		git = new Git(localrepo);
		//get the commit we are looking for
		RevCommit newCommit;
		try (RevWalk walk = new RevWalk(localrepo)){
			newCommit = walk.parseCommit(localrepo.resolve(hashID));
		}
		//String mydiff = getDiffOfCommit(newCommit);
		getDiffOfCommit(newCommit);
		//log.info(String.format("Diff:\n %s \n",mydiff));
		//populateDeletionsPerFile(mydiff);
		return null;
	}//}}}

	//{{{ populateDeletionsPerFile(String mydiff)
	private void populateDeletionsPerFile(String mydiff) throws Exception {

		boolean firstOccurrence = true; //use it to always skip the first occurrence, since it does not represent an actual hunk
		Scanner scan = new Scanner(mydiff).useDelimiter("--- a/"); 
		while(scan.hasNext()){
			if(firstOccurrence){
				firstOccurrence = false;
				scan.next();
				continue;
			}
			String diffcontents = scan.next();
			log.info(String.format("Scanner found: %s",diffcontents));
			processContents(diffcontents);	
			break;
		}
		scan.close();
	}
	//}}}

	//{{{ processContents(String diffcontents)
	private void processContents(String diffcontents) throws Exception {

		boolean pathline = true;
		try (Scanner scan = new Scanner(diffcontents)) {
			String filepath = "";
			while(scan.hasNext()){
				if(pathline){
					pathline = false;
					filepath = scan.nextLine().trim();
					filepathsANDmodifications.put(filepath,new ArrayList<String>());
					continue;
				}
				String line = scan.nextLine().trim();
				if(line.startsWith("-")){
					List<String> removedlines = filepathsANDmodifications.get(filepath);
					removedlines.add(line.replaceFirst("-","").trim());
					filepathsANDmodifications.put(filepath, removedlines);
				}
			}
			//logging the removed lines
			List<String> myremovedlines = filepathsANDmodifications.get(filepath);
			for (String removedline : myremovedlines){
				log.info(String.format("removed line:\n %s\n for file: %s",removedline,filepath));
			}
		}
	}
	//}}}
	
	//{{{ getDiffOfCommit(RevCommit newCommit)
	private String getDiffOfCommit(RevCommit newCommit) throws IOException {

		RevCommit oldCommit = getPrevHash(newCommit);
		this.globalprevcommit = oldCommit;
		log.debug(String.format("hash of the old commit: %s",oldCommit.getId().getName()));
		if(oldCommit == null){
			return "start of the repo!!";
		}
		AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(oldCommit);
		AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(newCommit);
		ByteArrayOutputStream mydiff = new ByteArrayOutputStream();
		try(DiffFormatter formatter = new DiffFormatter(mydiff)){
			formatter.setRepository(git.getRepository());
			formatter.format(oldTreeIterator,newTreeIterator);
		}
		//String mydiff = outputStream.toString();
		//return mydiff;
		String prevhash = oldCommit.getId().getName();
		String nexthash = newCommit.getId().getName();
		List<Header> headers = RepoOperations.getHeaders(mydiff);
		log.debug(String.format("headers size: %s",headers.size()));
		List<DiffHunk> hunks = RepoOperations.getDiffHunks(mydiff, headers, prevhash, nexthash);

		for(DiffHunk hunk : hunks){
			log.info(String.format("Hunk: %s with number of lines %d",hunk.getHeader().getHeader(),hunk.getContent().size()));
			for(Line line : hunk.getContent()){
				log.debug(String.format("Line: %s",line.getContent()));
				if(line.getType() == LineType.DELETION){
					log.info(String.format("Removed line: #%d %s", line.getPreviousNumber(), line.getContent()));
				}
			}
		}
		return "";
	}//}}}

	//{{{ getModifiedPaths()
       //	public List<String> getModifiedPaths(){
       //		List<String> modifiedPaths = new ArrayList<String>();
       //		RevCommit oldCommit = getPrevHash(newCommit);
       //		this.globalprevcommit = oldCommit;
       //		log.info(String.format("hash of the old commit: %s",oldCommit.getId().getName()));
       //		if(oldCommit == null){
       //			return "start of the repo!!";
       //		}
       //		AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(oldCommit);
       //		AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(newCommit);
       //		try(DiffFormatter formatter = new DiffFormatter(outputStream)){
       //			formatter.setRepository(git.getRepository());
       //			List<DiffEntry> diffEntries = formatter.scan(oldTreeIterator,newTreeIterator);
       //			for(DiffEntry dEntry : diffEntries){
       //				log.info(String.format("old path: %s", dEntry.getOldPath()));
       //				modifiedPaths.add(dEntry.getOldPath());
       //			}
       //		}
       //		return modifiedPaths;
       //	}
	//}}}

	//{{{ getPrevHash(RevCommit commit)
	private RevCommit getPrevHash(RevCommit commit) throws IOException {
		
		try(RevWalk walk = new RevWalk(localrepo)){
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

//{{{ other tests
	//test clone
//	public void run() throws Exception {
//		File myrepo = new File("unite.vim");
//		Git git = Git.cloneRepository()
//			.setURI("https://github.com/Shougo/unite.vim.git")
//			.setDirectory(myrepo)
//			.call();
//
//		localrepo = git.getRepository();
//		String commithash = "hash";
//
//		RevWalk revWalk = new RevWalk(localrepo);
//		RevCommit mycommit = revWalk.parseCommit(commithash);
//
//	}
//}}}

	//{{{ showBlame()
	public void showBlame(Repository repository, RevCommit prevcommit) throws Exception {

		BlameCommand blamer = new BlameCommand(repository);
		blamer.setStartCommit(prevcommit);
		//we have to iterate each file in the commit
		for(Map.Entry<String,List<String>> entry : filepathsANDmodifications.entrySet()){
			String filepath = entry.getKey();
			blamer.setFilePath(filepath);
			List<String> removedlines = entry.getValue();
			BlameResult blame = blamer.call();
			MyRawText resultContents = new MyRawText(blame.getResultContents().getRawContent());
			IntList lines = resultContents.getLines();

			for(int i = 0; i < resultContents.getLinesSize(); i++){
				log.info(String.format("content for line %d: \n commit-hash: %s \n commit-content: %s",
							i,
							blame.getSourceCommit(i).getName(),
							resultContents.getString(i)));
			}
		}

		
	}

	//}}}

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
	
}
