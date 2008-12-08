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

    @Requirement(hint = "maven")
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

    public ComponentDescriptor<?> getComponentDescriptor( String role, String hint )
    {
        ComponentDescriptor<?> cd = container.getComponentDescriptor( role, hint );       
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
