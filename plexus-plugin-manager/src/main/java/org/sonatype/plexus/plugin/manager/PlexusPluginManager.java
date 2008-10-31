package org.sonatype.plexus.plugin.manager;

public interface PlexusPluginManager
{
    PluginResolutionResult resolve( PluginResolutionRequest request )
        throws PluginResolutionException;        
}
