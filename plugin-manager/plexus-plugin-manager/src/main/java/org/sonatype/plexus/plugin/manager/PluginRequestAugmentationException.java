package org.sonatype.plexus.plugin.manager;

public class PluginRequestAugmentationException
    extends Exception
{
    public PluginRequestAugmentationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public PluginRequestAugmentationException( String message )
    {
        super( message );
    }

    public PluginRequestAugmentationException( Throwable cause )
    {
        super( cause );
    }        
}
