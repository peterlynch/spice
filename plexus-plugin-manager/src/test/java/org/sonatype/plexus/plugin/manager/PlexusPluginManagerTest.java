package org.sonatype.plexus.plugin.manager;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

public class PlexusPluginManagerTest
    extends PlexusTestCase
{
    public void testPluginManager()
        throws Exception
    {
        PlexusPluginManager pm = (PlexusPluginManager) lookup( PlexusPluginManager.class );
        assertNotNull( pm );
     
        File localRepository = new File( getBasedir(), "target/local-repo" );
                
        PluginResolutionRequest request = new PluginResolutionRequest()
            .addLocalRepository( localRepository )
            .addRemoteRepository( "http://repo1.maven.org/maven2" )            
            .setArtifactMetadata( "org.apache.maven.plugins:maven-clean-plugin:2.2" );
        
        PluginResolutionResult result = pm.resolve( request );
        
        ClassRealm realm = pm.createClassRealm( result.getArtifacts() );
        
        realm.display();
    }
}
