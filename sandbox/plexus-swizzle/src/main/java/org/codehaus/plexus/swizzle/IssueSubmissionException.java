package org.codehaus.plexus.swizzle;

public class IssueSubmissionException
    extends Exception
{
    public IssueSubmissionException( String arg0, Throwable arg1 )
    {
        super( arg0, arg1 );
    }

    public IssueSubmissionException( String arg0 )
    {
        super( arg0 );
    }

    public IssueSubmissionException( Throwable arg0 )
    {
        super( arg0 );
    }
}
