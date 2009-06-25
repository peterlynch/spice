package org.sonatype.plugin.metadata.gleaner;

public class GleanerException
    extends Exception
{
    private static final long serialVersionUID = -4244638887256916163L;

    public GleanerException()
    {
        super();
    }

    public GleanerException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public GleanerException( String message )
    {
        super( message );
    }

    public GleanerException( Throwable cause )
    {
        super( cause );
    }
}