package org.sonatype.plexus.plugin.manager;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

// Can i give this plugin manager magical OSGi adaptive powers. I think so. Can I get this stuff
// registered
// as OSGi components and would this allow good extension power to m2e.
public class PlexusPluginManagerTest
    extends PlexusTestCase
{
    public void testPluginManager()
        throws Exception
    {
        /*
        PlexusPluginManager pm = (PlexusPluginManager) lookup( PlexusPluginManager.class );
        
        assertNotNull( pm );

        File localRepository = new File( getBasedir(), "target/local-repo" );

        PluginResolutionRequest request = new PluginResolutionRequest()
            .addLocalRepository( localRepository ).addRemoteRepository( "http://repo1.maven.org/maven2" )
            .setArtifactMetadata( "org.apache.maven.plugins:maven-clean-plugin:2.2" );

        PluginResolutionResult result = pm.resolve( request );

        ClassRealm realm = pm.createClassRealm( result.getArtifacts() );

        realm.display();
        */

        /*
          
        // The component discovery mechanism here needs to be specific to the type of
        // plugin we have here so let's start with the maven specific one here. Basically
        // We want to get plugin specific descriptors here.
        //
        // What we can do here by default is discover plexus components as plugins
        // but then we have the problem of knowing which component is the plugin if the plugin
        // happens to use other plexus components itself.
        //
        // I need to deterministically know what the target component is. I need some
        // way to look it up.
        //
        // In maven we know this by using the artifact id as the role hint.
        List<ComponentDescriptor<?>> components = pm.discoverComponents( realm );
        System.out.println( "size = " + components.size() );
        assertTrue( components.size() > 0 );
        
        String role = "org.apache.maven.plugin.Mojo";
        String hint = "org.apache.maven.plugins:maven-clean-plugin:2.2:clean";        
        
        ComponentDescriptor<?> cd = pm.getComponentDescriptor( role, hint );        
        assertEquals( role, cd.getRole() );
        assertEquals( hint, cd.getRoleHint() );
        assertEquals( "org.apache.maven.plugin.clean.CleanMojo", cd.getImplementation() );
        
        */
        
        // So now I can verify that the plugin I want has been discovered
        // But I should have metadata about the plugin that I want to run so let's pretend
        // this is the clean plugin.

        // Now I need some special processing to grab the metadata and do the right thing
        // Maven: driven by the POM     
        // Nexus: driven by some components declared and retrieved and place and loaded.
        // separate the retrieval and loading.

        // A maven plugins deps are loaded by its POM, but we should lock this down after the build
        // A nexus plugin should have a model for dependencies

        // API from services
        
        // The base plugin interface needs to be loaded in a base classloader before the children
        // can be built on top of it.
        
        /*
        Object component = pm.findPlugin( Mojo.class, hint );
        ClassLoader cl = Mojo.class.getClassLoader();
        boolean a = cl instanceof URLClassLoader;
        URLClassLoader base = (URLClassLoader)cl;
        
        System.out.println( Mojo.class.getClassLoader() + "URLClassLoader " + a );
        System.out.println( component.getClass().getClassLoader() );
        
        assertNotNull( component );
        
        PlexusConfiguration pc = new XmlPlexusConfiguration( "configuration" );
        PlexusConfiguration x = pc.addChild( "outputDirectory" );
        x.setValue( "/tmp/foo" );
                
        System.out.println( pc );
        */
        
        // Now I need to configure this thing before I can run it. All the logic to produce a configuration
        // is currently in the plugin manager. I also need all the validation we had for the parameters.
                
        //component.execute();        
    }
}
