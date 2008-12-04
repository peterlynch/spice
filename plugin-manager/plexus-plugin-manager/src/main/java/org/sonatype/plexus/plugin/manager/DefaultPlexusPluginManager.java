package org.sonatype.plexus.plugin.manager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.mercury.MavenDependencyProcessor;
import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.plexus.PlexusMercury;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.transport.api.Server;
import org.codehaus.plexus.MutablePlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.component.repository.exception.ComponentRepositoryException;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

// 1 the metadata -> model plugin/mojo descriptor
// 2 tools for doing the mapping
// 3 the component model -> interfaces for the plugin

/*
 * 
 * h2. Concerns for the plugin manager
 * 
 * h3. resolving the dependencies of a plugin - these could be resolved remotely at runtime or, -
 * they could be resolved from a local repository - i think we need a simple dependency model here
 * that is more mercury related and not Maven related i.e no POMs - workspace resolver - we need
 * tools to pre-populate this repository
 * 
 * h3. create an isolated classloader
 * 
 * h3. lookup the plugin with a configuration
 * 
 * h3. execute the plugin
 * 
 * h3. plugins may have to deal with particular actions when a plugin is - installed - loaded -
 * unloaded - update - uninstalled
 * 
 * h3. plugins should be able to have specific metadata for a plugin model and that be translated -
 * dependencies - resources - configuration - extension points of plugins
 * 
 * For a particular application plugin there will be a declarative descriptor for that plugin type.
 * 
 * - nexus - the plugin class - UI to contribute - what JS to hook into the UI - what resources to
 * load into the UI - having packed or unpacked plugins, and positioning resources if necessary -
 * maven can work out of the classloader, nexus plugins probably couldn't given the js and image
 * resources
 * 
 * We need to look at Maven, and Nexus as use cases and figure out what each of them needs to be
 * able to do
 * 
 * - now what is really the difference between this and loading a component in plexus - custom
 * classloading capability - remote resolution of dependencies - do we want a model for sharing
 * information among plugins, is this more like an extension point - do we need a sort of bus for
 * application data - do we need a dictionary for our applications like Apple does. We could easily
 * hook into this and this is the model we need to follow - how many of our REST services do not map
 * directory to a method in the application interface?
 * 
 * - research extension points versus plugins
 * 
 * from igor: two plugins A and B, both depend on the same library but use different versions, say
 * lib 1.0 and lib 2.0 when debugger hits a breakpoint inside a class from the library, IDE needs to
 * know which version of library the class comes from
 */

@Component(role = PlexusPluginManager.class)
public class DefaultPlexusPluginManager
    implements PlexusPluginManager
{
    @Requirement
    private PlexusMercury mercury;

    @Requirement
    private Logger logger;

    @Requirement
    private PlexusContainer container;

    @Requirement
    private DependencyProcessor dependencyProcessor;

    public PluginResolutionResult resolve( PluginResolutionRequest request )
        throws PluginResolutionException
    {
        PluginResolutionResult result = new PluginResolutionResult();

        List<Repository> repositories = new ArrayList<Repository>();

        for ( Iterator<File> i = request.getLocalRepositories().iterator(); i.hasNext(); )
        {
            File repository = i.next();
            if ( !repository.exists() )
            {
                repository.mkdirs();
            }
            // Oleg 200.12.04: all IDs need to be unique as they are used by the cache to identify metadata
            // it's not that critical for local repo, but better safe'n sorry
            LocalRepositoryM2 localRepository = new LocalRepositoryM2( "local-"+repository.getName(), repository, dependencyProcessor );
            repositories.add( localRepository );
        }

        for ( Iterator<String> i = request.getRemoteRepositories().iterator(); i.hasNext(); )
        {
            String remoteRepository = i.next();

            URL url;

            try
            {
                url = new URL( remoteRepository );
            }
            catch ( MalformedURLException e )
            {
                throw new PluginResolutionException( "Bad URL" );
            }

            //!! Why do repositories need IDs. We probably want to avoid this.
            // Oleg 200.12.04: all IDs need to be unique as they are used by the cache to identify metadata
            Server repository = new Server( "id", url );

            // We really don't want to hardcode the repository type
            repositories.add( new RemoteRepositoryM2( repository, dependencyProcessor ) );
        }

        try
        {
            logger.info( "Start metadata resolution." );

            List<ArtifactMetadata> res = (List<ArtifactMetadata>) mercury.resolve( repositories, ArtifactScopeEnum.runtime, request.getArtifactMetadata() );

            logger.info( "Resolution OK." );

            List<Artifact> artifacts = mercury.read( repositories, res );

            // Is the first artifact in the list going to be the plugin artifact? because I need to be able to grab that
            // that artifact to process it in a certain way.

            logger.info( "Artifact retrieval OK." );

            result.setArtifacts( artifacts );
        }
        catch ( RepositoryException e )
        {
            throw new PluginResolutionException( "Cannot retrieve plugin dependencies", e );
        }

        return result;
    }

    public ClassRealm createClassRealm( String id )
    {
        ClassRealm realm = container.createChildRealm( id );
        return realm;
    }

    public ClassRealm createClassRealm( List<Artifact> artifacts )
    {
        // This realm id is going to have to be unique. I need to be able to rename it afterward.
        ClassRealm realm = container.createChildRealm( "realm" );

        for ( Iterator<Artifact> i = artifacts.iterator(); i.hasNext(); )
        {
            Artifact artifact = i.next();

            try
            {
                realm.addURL( artifact.getFile().toURI().toURL() );
            }
            catch ( MalformedURLException e )
            {
                // Won't happen, the files have already been successfully downloaded.                
            }
        }

        return realm;
    }

    public List<ComponentDescriptor<?>> discoverComponents( ClassRealm realm )
    {
        try
        {
            List<ComponentDescriptor<?>> components = container.discoverComponents( realm );

            /*
            for ( Iterator<ComponentDescriptor> i = components.iterator(); i.hasNext(); )
            {
                ComponentDescriptor cd = i.next();
                System.out.println( cd.getRole() + " : " + cd.getRoleHint());
            }
            */

            return components;
        }
        catch ( PlexusConfigurationException e )
        {
            return null;
        }
        catch ( ComponentRepositoryException e )
        {
            return null;
        }
    }

    public ComponentDescriptor getComponentDescriptor( String role, String hint )
    {
        ComponentDescriptor cd = container.getComponentDescriptor( role, hint );       
        return cd;
    }

    public Object findPlugin( Class pluginClass, String hint )
        throws ComponentLookupException
    {
        return container.lookup( pluginClass, hint );
    }

    public void processPlugins( File pluginsDirectory )
    {                        
        File[] plugins = pluginsDirectory.listFiles();
        
        for ( int i = 0; i < plugins.length; i++ )
        {
            File plugin = plugins[i];

            if ( !plugin.getName().endsWith( ".jar" ) )
            {
                continue;
            }
                        
            ClassRealm realm = createClassRealm( "realm" );

            try
            {
                realm.addURL( plugin.toURL() );
            }
            catch ( MalformedURLException e )
            {
                // Won't happen
            }

            discoverComponents( realm );            
        }        
    }
}
