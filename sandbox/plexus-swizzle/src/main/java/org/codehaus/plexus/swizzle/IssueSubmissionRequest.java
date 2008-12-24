package org.codehaus.plexus.swizzle;

import java.io.File;

public class IssueSubmissionRequest
{
    private String projectId;
    private String summary;    
    private String description;
    private String reporter;
    private String assignee;
    private File problemReportBundle;
    public String getProjectId()
    {
        return projectId;
    }
    public void setProjectId( String projectId )
    {
        this.projectId = projectId;
    }
    public String getSummary()
    {
        return summary;
    }
    public void setSummary( String summary )
    {
        this.summary = summary;
    }
    public String getDescription()
    {
        return description;
    }
    public void setDescription( String description )
    {
        this.description = description;
    }
    public String getReporter()
    {
        return reporter;
    }
    public void setReporter( String reporter )
    {
        this.reporter = reporter;
    }
    public String getAssignee()
    {
        return assignee;
    }
    public void setAssignee( String assignee )
    {
        this.assignee = assignee;
    }
    public File getProblemReportBundle()
    {
        return problemReportBundle;
    }
    public void setProblemReportBundle( File problemReportBundle )
    {
        this.problemReportBundle = problemReportBundle;
    }

    
}
