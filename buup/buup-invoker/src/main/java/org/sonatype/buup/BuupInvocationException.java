package org.sonatype.buup;

public class BuupInvocationException
    extends Exception
{
    private static final long serialVersionUID = -1004719704957379266L;

    public BuupInvocationException( String message )
    {
        super( message );
    }

    public BuupInvocationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
