package org.sonatype.idiom;

import java.io.File;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.plexus.plugin.manager.PlexusPluginManager;

@Component(role = Idiom.class)
public class DefaultIdiom
    implements Idiom, Initializable
{
    @Requirement
    private PlexusPluginManager pm;

    @Requirement
    private PlexusContainer container;

    @Configuration(value = "/Users/jvanzyl/js/spice/trunk/plugin-manager/idiom-core/src/test/plugins")
    private File pluginsDirectory;
    
    public void execute( String id )
    {
        IdiomPlugin p = null;

        try
        {
            p = (IdiomPlugin) pm.findPlugin( IdiomPlugin.class, id );
        }
        catch ( ComponentLookupException e )
        {
        }

        try
        {
            p.execute();
        }
        catch ( IdiomException e )
        {
        }
    }

    public void initialize()
        throws InitializationException
    {
        if ( pluginsDirectory.exists() )
        {
            pm.processPlugins( pluginsDirectory );
        }
    }
    
    /*
    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration containerConfiguration )
    {
        containerConfiguration.addComponentDiscoverer( new PluginDiscoverer() );
    }
    */    
}
