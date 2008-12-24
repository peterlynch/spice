package org.codehaus.plexus.swizzle;

import java.io.File;

public interface IssueSubmitter
{
    void submitIssue( IssueSubmissionRequest request )
        throws IssueSubmissionException;

    void submitProblemReportForIssue( String issueKey, File problemReportBundle )
        throws IssueSubmissionException;
}
