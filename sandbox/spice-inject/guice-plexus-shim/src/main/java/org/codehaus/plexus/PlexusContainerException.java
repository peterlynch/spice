package org.codehaus.plexus;

public final class PlexusContainerException
    extends Exception
{
    private static final long serialVersionUID = 1L;

    public PlexusContainerException( final String message )
    {
        super( message );
    }

    public PlexusContainerException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
