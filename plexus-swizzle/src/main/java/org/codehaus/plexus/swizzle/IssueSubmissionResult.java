package org.codehaus.plexus.swizzle;

public class IssueSubmissionResult
{
    private String issueUrl;
    private String key;

    public IssueSubmissionResult( String issueUrl, String key )
    {
        this.issueUrl = issueUrl;
        this.key = key;
    }

    public String getIssueUrl()
    {
        return issueUrl;
    }

    public void setIssueUrl( String issueUrl )
    {
        this.issueUrl = issueUrl;
    }        
    
    public String getKey()
    {
        return key;
    }
    
    public void setKey( String key )
    {
        this.key = key;
    }
}
