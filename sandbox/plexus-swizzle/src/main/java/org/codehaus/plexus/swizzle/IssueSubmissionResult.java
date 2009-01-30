package org.codehaus.plexus.swizzle;

public class IssueSubmissionResult
{
    private String issueUrl;

    public IssueSubmissionResult( String issueUrl )
    {
        this.issueUrl = issueUrl;
    }

    public String getIssueUrl()
    {
        return issueUrl;
    }

    public void setIssueUrl( String issueUrl )
    {
        this.issueUrl = issueUrl;
    }        
}
