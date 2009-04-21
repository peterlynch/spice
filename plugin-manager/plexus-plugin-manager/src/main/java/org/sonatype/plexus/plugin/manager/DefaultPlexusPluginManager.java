/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.plexus.plugin.manager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;
import org.apache.maven.mercury.plexus.PlexusMercury;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.transport.api.Server;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.component.repository.exception.ComponentRepositoryException;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.logging.Logger;

@Component(role = PlexusPluginManager.class)
public class DefaultPlexusPluginManager
    implements PlexusPluginManager
{
    @Requirement
    private Logger logger;

    @Requirement
    private PlexusMercury mercury;

    @Requirement
    private PlexusContainer container;

    public PluginResolutionResult resolve( PluginResolutionRequest request )
        throws PluginResolutionException
    {                      
        PluginResolutionResult result = new PluginResolutionResult();

        List<Repository> repositories = new ArrayList<Repository>();

        for ( Iterator<File> i = request.getLocalRepositories().iterator(); i.hasNext(); )
        {
            File repository = i.next();
                                        
            try
            {
                LocalRepositoryM2 localRepository = mercury.constructLocalRepositoryM2( "local-"+repository.getName(), repository, null, null, null, null );
                
                repositories.add( localRepository );                
            }
            catch ( RepositoryException e )
            {
                throw new PluginResolutionException( "Error creating local repository: ", e );
            }            
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
                throw new PluginResolutionException( "Bad URL", e );
            }

            Server repository = new Server( "id", url );

            // We really don't want to hardcode the repository type
            repositories.add( new RemoteRepositoryM2( repository, null ) );
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
        return container.createChildRealm( id );
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
            return container.discoverComponents( realm );
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
        return container.getComponentDescriptor( role, hint );       
    }

    public Object findPlugin( Class pluginClass, String hint )
        throws ComponentLookupException
    {
        return container.lookup( pluginClass, hint );
    }

    public List<ComponentDescriptor<?>> processPlugins( File pluginsDirectory )
    {       
        List<ComponentDescriptor<?>> componentDescriptors = new ArrayList<ComponentDescriptor<?>>();
            
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

            componentDescriptors.addAll( discoverComponents( realm ) );            
        }     
        
        return componentDescriptors;
    }

    // Default implementation does nothign to the request
    public PluginResolutionRequest augment( PluginResolutionRequest request )
        throws PluginRequestAugmentationException
    {
        return request;
    }
}
