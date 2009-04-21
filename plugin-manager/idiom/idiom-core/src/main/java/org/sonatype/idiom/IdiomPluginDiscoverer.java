package org.sonatype.idiom;

import java.io.Reader;

import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.sonatype.idiom.plugin.io.xpp3.IdiomPluginXpp3Reader;

public class IdiomPluginDiscoverer
{
    private IdiomPluginXpp3Reader reader;
    
    public IdiomPluginDiscoverer()
    {
    }

    public String getComponentDescriptorLocation()
    {
        return "META-INF/idiom/idiom-plugin.xml";
    }

    public ComponentSetDescriptor createComponentDescriptors( Reader componentDescriptorConfiguration, String source )
        throws PlexusConfigurationException
    {
        ComponentSetDescriptor csd = new ComponentSetDescriptor();
        
        return csd;
        
    }
}
