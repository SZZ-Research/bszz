package nz.ac.otago.utils;

//{{{ import statements
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.apache.log4j.Logger;

import nz.ac.otago.miner.szz.model.Header;
import nz.ac.otago.miner.szz.model.DiffHunk;
import nz.ac.otago.miner.szz.model.Line;
import nz.ac.otago.miner.szz.model.LineType;

import java.io.File;
import org.eclipse.jgit.api.Git;


//}}}

public class RepoOperations {
	
	private static final Logger log = Logger.getLogger(RepoOperations.class);

	//{{{ getHeaders(ByteArrayOutputStream diff)
	public static List<Header> getHeaders(ByteArrayOutputStream diff) throws IOException {

		List<Header> headers = new ArrayList<Header>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(diff.toByteArray())));
		try {
			String prevFilePath = "";
			String nextFilePath = "";
			while (br.ready()) {
				String line = br.readLine();
				line = line.trim();
				log.debug(String.format("Line: %s", line));
				if(isPrevFilePath(line)){
					log.debug("it is a prevfile path!");
					prevFilePath = line;
					prevFilePath = prevFilePath.replace("--- a/","");
					continue;
				} else if (isNextFilePath(line)) {
					log.debug("it is a nextfile path!");
					nextFilePath = line;
					nextFilePath = nextFilePath.replace("+++ b/","");
					continue;
				} else if (isHunkHeader(line)) {
					log.debug("it is a hunk header!");
					Header header = new Header();
					header.setPreviousPath(prevFilePath);
					header.setNextPath(nextFilePath);
					header.setHeader(line);
					headers.add(header);
				}
			}
		} finally {
			br.close();
		}
		return headers;
	}//}}}
	
	//{{{ getDiffHunks( too many !)
	public static List<DiffHunk> getDiffHunks(ByteArrayOutputStream diff, 
						  List<Header> headers, 
						  String prevhash, 
						  String nexthash)
						  throws IOException {

		List<DiffHunk> hunks = new ArrayList<DiffHunk>();
		for (Header header : headers) {
			List<Line> deletionsBuffer = new ArrayList<Line>();
			BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(diff.toByteArray())));
			try {
				//used to build the evolution relationship when a modification occurs
				boolean linkageFlag = false;
				LineType previousType = LineType.CONTEXT;
				boolean headerFound = false;
				//@@ -[9],10 +9,10 @@
				int prevRevContextStartNumber = getPrevContextStartingLineNumber(header.getHeader());
				//@@ -9,10 +[9],10 @@
				int nextRevContextStartNumber = getNextContextStartingLineNumber(header.getHeader());
				//@@ -9,[10] +9,10 @@
				//int prevRevContextLineRange = getPrevContextLineRange(header);
				//@@ -9,10 +9,[10] @@
				//int nextRevContextLineRange = getNextContextLineRange(header);
				//we will need this when we adjust the content of a line to infer the number of the following revision
				int context_difference = nextRevContextStartNumber - prevRevContextStartNumber; 
				int prevSync =  prevRevContextStartNumber; 
				int nextSync = nextRevContextStartNumber;
				int deletions = 0;
				int additions = 0;
				boolean firstOccurrenceFound = false; //track when we reach the first context line
				boolean startDelCount = false;
				boolean startAddCount = false;
				DiffHunk hunk = new DiffHunk();
				hunk.setHeader(header);
				while (br.ready()) {
					String line = br.readLine();
					log.debug(String.format("Line: %s",line));
					if (!headerFound && line.trim().equals(header.getHeader())) {
						log.debug(String.format("ok achamos o header: %s",line));
						headerFound = true;
						continue;
					}
					if (headerFound) {
						Line lineobj = new Line();
						lineobj.setPreviousPath(header.getPreviousPath());
						lineobj.setNextPath(header.getNextPath());
						lineobj.setContent(line);
						log.debug(String.format("setei o content da linha, que eh: \n%s",line));
						if (!isHunkHeader(line)) {
							log.debug("ok nao eh uma linha representando header!!!");
							if (isDeletion(line)) {
								log.debug("eh uma delecao!!!!!!!!");
								lineobj.setAdditions(additions);
								lineobj.setDeletions(deletions);
								deletions++;
								// comming from an addition
								if (linkageFlag) {
									deletionsBuffer.clear();
								}
								lineobj.setType(LineType.DELETION);
								if(!firstOccurrenceFound){
									firstOccurrenceFound = true;     
									startDelCount = true;
								} else if (!startDelCount){  //i think it doesnt happen in practice
									startDelCount = true;
								} else {
									prevSync++;
								}
								lineobj.setPreviousNumber(prevSync);
								lineobj.setPrevhash(prevhash);
								lineobj.setContext_difference(context_difference);
								deletionsBuffer.add(lineobj);
								linkageFlag = false;
								previousType = LineType.DELETION;
							} else if (isAddition(line)) {
								log.debug("eh uma adicao!!!!!!!!");
								lineobj.setDeletions(deletions);
								lineobj.setAdditions(additions);
								additions++;
								if (previousType == LineType.DELETION) {
									linkageFlag = true;
								}
								lineobj.setType(LineType.ADDITION);
								if(!firstOccurrenceFound){
									firstOccurrenceFound = true;     
									startAddCount = true;
								} else if (!startAddCount){ //very rare case
									startAddCount = true;
								} else {
									nextSync++;
								}
								lineobj.setNumber(nextSync);
								lineobj.setNexthash(nexthash);
								lineobj.setContext_difference(context_difference);
								previousType = LineType.ADDITION;
								if (linkageFlag) {
									lineobj.getOrigins().addAll(deletionsBuffer);
									//for (Line deletion : deletionsBuffer) {
									//	deletion.getEvolutions().add(lineobj);
									//}
								}
							} else {
								if(!firstOccurrenceFound){ 
									firstOccurrenceFound = true;     
									startDelCount = true;
									startAddCount = true;
								} else { 
									if(!startDelCount){
										startDelCount = true;
									} else {
										prevSync++;
									}	
									if(!startAddCount){
										startAddCount = true;
									} else {
										nextSync++;
									}
								}
								lineobj.setType(LineType.CONTEXT);
								lineobj.setPrevhash(prevhash);
								lineobj.setNexthash(nexthash);
								lineobj.setPreviousNumber(prevSync);
								lineobj.setNumber(nextSync);
								previousType = LineType.CONTEXT;
								linkageFlag = false;
								deletionsBuffer.clear();
							}
							//these lines don't have to be bothered anymore
							lineobj.setFoundInDiffHunks(true);
							hunk.getContent().add(lineobj);
						} else {
							break;
						}
					}
				}
				hunks.add(hunk);
			} finally {
				br.close();
			}
		}
		return hunks;
	} //}}}

	//{{{ getPrevContextStartingLineNumber(String header)
	public static int getPrevContextStartingLineNumber(String header) {

		String[] tokens = header.split(" ");
		String toAnalyze = tokens[1];
		String[] tokens2 = toAnalyze.split(",");
		String lineNumberStr = tokens2[0].replace("-", "");
		int lineNumber = Integer.valueOf(lineNumberStr);
		return lineNumber;
	}
	//}}}
	
	//{{{ getNextContextStartingLineNumber(Stirng header)
	public static int getNextContextStartingLineNumber(String header) {

		String[] tokens = header.split(" ");
		String toAnalyze = tokens[2];
		String[] tokens2 = toAnalyze.split(",");
		String lineNumberStr = tokens2[0].replace("+", "");
		int lineNumber = Integer.valueOf(lineNumberStr);
		return lineNumber;
	}
	//}}}
	
	//{{{ isHunkHeader(String line)
	public static boolean isHunkHeader(String line) {

		Pattern pattern = Pattern.compile("@@\\s-\\d+,\\d+\\s\\+\\d+,\\d+\\s@@");
		Matcher matcher = pattern.matcher(line);
		return matcher.find();
	}
	//}}}
	
	//{{{ isDeletion(String line)
	public static boolean isDeletion(String line) {

		boolean result1;
		Pattern pattern = Pattern.compile("^(\\-)");
		Matcher matcher = pattern.matcher(line.trim());
		result1 = matcher.find();
		return result1;
	}
	//}}}
	
	//{{{ isAddition(String line)
	public static boolean isAddition(String line) {

		boolean result1;
		Pattern pattern = Pattern.compile("^(\\+)");
		Matcher matcher = pattern.matcher(line.trim());
		result1 = matcher.find();
		return result1;
	}
	//}}}
	
	//{{{ isPrevFilePath(String line)
	public static boolean isPrevFilePath(String line) {

		boolean result1;
		Pattern pattern = Pattern.compile("^(--- a/)");
		Matcher matcher = pattern.matcher(line.trim());
		result1 = matcher.find();
		return result1;
	}
	//}}}

	//{{{ isNextFilePath(String line)
	public static boolean isNextFilePath(String line) {

		boolean result1;
		Pattern pattern = Pattern.compile("^(\\+\\+\\+ b/)");
		Matcher matcher = pattern.matcher(line.trim());
		result1 = matcher.find();
		return result1;
	}
	//}}}
	
	//{{{ cloneGitRepo(String repoUrl, String repoPath)
	public static Git cloneGitRepo(String repoUrl, String repoPath) throws Exception {
		Git git = Git.cloneRepository().setURI(repoUrl).setDirectory(new File(repoPath)).call();
		return git;
	}
	//}}}

}
