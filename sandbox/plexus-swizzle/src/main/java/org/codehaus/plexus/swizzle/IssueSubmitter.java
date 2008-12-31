package org.codehaus.plexus.swizzle;

public interface IssueSubmitter
{
    void submitIssue( IssueSubmissionRequest request )
        throws IssueSubmissionException;
}
