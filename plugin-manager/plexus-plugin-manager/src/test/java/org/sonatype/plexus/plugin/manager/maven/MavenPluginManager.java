package org.sonatype.plexus.plugin.manager.maven;

import java.util.List;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.sonatype.plexus.plugin.manager.DefaultPlexusPluginManager;
import org.sonatype.plexus.plugin.manager.PlexusPluginManager;

@Component(role = PlexusPluginManager.class, hint = "maven")
public class MavenPluginManager
    extends DefaultPlexusPluginManager
{
    // Right now for discoverying components I have a discoverer and a listener. This seems kind of redundant.
    // So during the container intialization I have to register the discoverer and the listener so this means
    // I could change the system at runtime which is kind of crappy. I would prefer to have something like:
    //
    // public List<ComponentDescriptor> discoverComponents( ClassRealm realm, new MyPluginDiscoverer() )
    //
    // Discoverer: know where to find specific plugin descriptors
    // Translation: ability to interpret the plugin descriptor as the necessary plexus component descriptor
    // Extensible: being able to have custom information that can be utilized by the application
    // Listener (right now): Just does some extra shit like keeping track of what's in process which i can't actually
    // remember why that was useful and I have to do crappy instanceof checks to make sure it's the plugin of the 
    // target system. i should just filter those out or direct discovery events at the target.
    //
    // We should use XStream for this and allow people to use it in their plugin disoverers
    
    @Override
    public List<ComponentDescriptor> discoverComponents( ClassRealm realm )
    {
        // Might be easier here to just pass in a discovery adapter instead of registering discoverers
        return super.discoverComponents( realm );
    }

    public String getComponentDescriptorLocation()
    {
        return "META-INF/maven/plugin.xml";
    }
}
