package org.sonatype.plexus.plugin.manager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.mercury.MavenDependencyProcessor;
import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;
import org.apache.maven.mercury.plexus.PlexusMercury;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.transport.api.Server;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/*

h2. Concerns for the plugin manager

h3. resolving the dependencies of a plugin
 - these could be resolved remotely at runtime or,
 - they could be resolved from a local repository
 - i think we need a simple dependency model here that is more mercury related and not Maven related i.e no POMs
 - workspace resolver
 - we need tools to pre-populate this repository

h3. create an isolated classloader 

h3. lookup the plugin with a configuration

h3. execute the plugin

h3. plugins may have to deal with particular actions when a plugin is 
  - installed
  - loaded
  - unloaded
  - update
  - uninstalled

h3. plugins should be able to have specific metadata for a plugin model and that be translated
  - dependencies
  - resources
  - configuration
  - extension points of plugins

For a particular application plugin there will be a declarative descriptor for that plugin type. 

- nexus
  - the plugin class
  - UI to contribute
  - what JS to hook into the UI
  - what resources to load into the UI
- having packed or unpacked plugins, and positioning resources if necessary
 - maven can work out of the classloader, nexus plugins probably couldn't given the js and image resources  
  
We need to look at Maven, and Nexus as use cases and figure out what each of them needs to be able to do  

- now what is really the difference between this and loading a component in plexus
  - custom classloading capability
  - remote resolution of dependencies
- do we want a model for sharing information among plugins, is this more like an extension point
- do we need a sort of bus for application data
- do we need a dictionary for our applications like Apple does. We could easily hook into this and this is the model we need to follow
- how many of our REST services do not map directory to a method in the application interface?

- research extension points versus plugins

from igor:
two plugins A and B, both depend on the same library but use different versions, say lib 1.0 and lib 2.0
when debugger hits a breakpoint inside a class from the library, IDE needs to know which version of library the class comes from 
 
 */

@Component(role = PlexusPluginManager.class)
public class DefaultPlexusPluginManager
    implements PlexusPluginManager
{
    @Requirement
    private PlexusMercury mercury;

    public PluginResolutionResult resolve( PluginResolutionRequest request )
        throws PluginResolutionException
    {
        PluginResolutionResult result = new PluginResolutionResult();
        
        List<Repository> repos = new ArrayList<Repository>();
        
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
            Server repository = new Server( "id", url  );
            
            // We really don't want to hardcode the repository type
            repos.add( new RemoteRepositoryM2( repository ) );                        
        }
        
        try
        {
            List<ArtifactBasicMetadata> res = (List<ArtifactBasicMetadata>) 
                mercury.resolve( repos, new MavenDependencyProcessor(), ArtifactScopeEnum.runtime, request.getArtifactMetadata() );

            List<Artifact> artifacts = mercury.read( repos, res );
            
            result.setArtifacts( artifacts );
        }
        catch ( RepositoryException e )
        {
            throw new PluginResolutionException( "Cannot retrieve plugin dependencies", e );
        }

        return result;
    }
}
