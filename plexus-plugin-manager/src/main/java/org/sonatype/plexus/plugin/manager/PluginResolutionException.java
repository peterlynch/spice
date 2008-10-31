package org.sonatype.plexus.plugin.manager;

public class PluginResolutionException
    extends Exception
{
    public PluginResolutionException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public PluginResolutionException( String message )
    {
        super( message );
    }

    public PluginResolutionException( Throwable cause )
    {
        super( cause );
    }
}
