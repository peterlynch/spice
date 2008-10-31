package org.sonatype.plexus.plugin.manager;

import org.codehaus.plexus.PlexusTestCase;

public class PlexusPluginManagerTest
    extends PlexusTestCase
{
    public void testPluginManager()
        throws Exception
    {
        PlexusPluginManager pm = (PlexusPluginManager) lookup( PlexusPluginManager.class );
        assertNotNull( pm );
        
        PluginResolutionRequest request = new PluginResolutionRequest()
            .addRemoteRepository( "http://repo1.maven.org/maven2" )
            .setArtifactMetadata( "org.apache.maven.plugins:maven-clean-plugin:2.2" );
        
        PluginResolutionResult result = pm.resolve( request );
    }
}
