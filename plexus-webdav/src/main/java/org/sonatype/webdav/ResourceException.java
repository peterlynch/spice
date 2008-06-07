package org.sonatype.webdav;

/**
 * @author Jason van Zyl
 */
public class ResourceException
    extends Exception
{
    public ResourceException( String s )
    {
        super( s );
    }

    public ResourceException( String s, Throwable throwable )
    {
        super( s, throwable );
    }

    public ResourceException( Throwable throwable )
    {
        super( throwable );
    }
}
