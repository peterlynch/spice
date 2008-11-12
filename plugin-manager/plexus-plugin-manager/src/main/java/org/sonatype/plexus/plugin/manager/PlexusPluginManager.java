package org.sonatype.plexus.plugin.manager;

import java.util.List;

import org.apache.maven.mercury.artifact.Artifact;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public interface PlexusPluginManager
{
    // we couldn't really make a plugin execution request because we don't know what method to
    // execute ...
    // - someone would need to make a custom plugin execution request or a plugin manager
    //   so that we would know what kind of method to execute

    PluginResolutionResult resolve( PluginResolutionRequest request )
        throws PluginResolutionException;

    // This is not general enough for people who don't use classworlds. it would be nice if they did
    // but i need to detach the realm from the classworld. it's nice having the encapsulation but it
    // really makes creating a new realm a pain in the ass
    ClassRealm createClassRealm( List<Artifact> artifacts );

    // This is also very plexus specific but do i care. If I'm trying to make something that's
    // appealing to people using things other then plexus it matter. could we just do this
    // for pico for example.
    List<ComponentDescriptor> discoverComponents( ClassRealm realm );

    ComponentDescriptor getComponentDescriptor( String role, String hint );

    // We need to find all available plugins without loading them.
    
    // Looking up the plugin by the class might not be the best thing to do especially if the class is not
    // loaded yet.
    Object findPlugin( Class pluginClass, String hint )
        throws ComponentLookupException;

    Object findPlugin( String role, String hint )
        throws ComponentLookupException;

    // registering component listeners
}
