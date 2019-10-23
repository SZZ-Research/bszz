package nz.ac.otago.miner.szz.model;

import java.util.*;
import java.math.*;

public class RevisionFutureBugs {
	private long revision;
	private BigDecimal percent;
	private List<String> issues;

	public RevisionFutureBugs(){
		issues = new ArrayList<String>();
	}

	/**
	 * Get revision.
	 *
	 * @return revision as long.
	 */
	public long getRevision()
	{
	    return revision;
	}

	/**
	 * Set revision.
	 *
	 * @param revision the value to set.
	 */
	public void setRevision(long revision)
	{
	    this.revision = revision;
	}

	/**
	 * Get percent.
	 *
	 * @return percent as BigDecimal.
	 */
	public BigDecimal getPercent()
	{
	    return percent;
	}

	public int getNumberOfIssues(){
		return issues.size();
	}

	/**
	 * Set percent.
	 *
	 * @param percent the value to set.
	 */
	public void setPercent(BigDecimal percent)
	{
	    this.percent = percent;
	}

	/**
	 * Get issues.
	 *
	 * @return issues as String.
	 */
	public List<String> getIssues()
	{
	    return issues;
	}

	/**
	 * Set issues.
	 *
	 * @param issues the value to set.
	 */
	public void setIssues(List<String> issues)
	{
	    this.issues = issues;
	}
}
