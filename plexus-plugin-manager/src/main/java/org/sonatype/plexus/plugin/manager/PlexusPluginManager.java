package org.sonatype.plexus.plugin.manager;

import java.util.List;

import org.apache.maven.mercury.artifact.Artifact;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentRepositoryException;
import org.codehaus.plexus.configuration.PlexusConfigurationException;

public interface PlexusPluginManager
{
    PluginResolutionResult resolve( PluginResolutionRequest request )
        throws PluginResolutionException;   
    
    ClassRealm createClassRealm( List<Artifact> artifacts );
    
    List<ComponentDescriptor> discoverComponents( PlexusContainer container, ClassRealm realm ) 
        throws PlexusConfigurationException, ComponentRepositoryException;  
    
    // registering component listeners
}
