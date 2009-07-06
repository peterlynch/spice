package org.sonatype.plugin.metadata.plexus;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.repository.ComponentDescriptor;

public class PlexusComponentGleanerResponse
{
    private final PlexusComponentGleanerRequest request;

    private ComponentDescriptor<?> componentDescriptor;

    private Map<Class<?>, Object> markerAnnotations;

    public PlexusComponentGleanerResponse( PlexusComponentGleanerRequest request )
    {
        this.request = request;
    }

    public PlexusComponentGleanerRequest getRequest()
    {
        return request;
    }

    public ComponentDescriptor<?> getComponentDescriptor()
    {
        return componentDescriptor;
    }

    public void setComponentDescriptor( ComponentDescriptor<?> componentDescriptor )
    {
        this.componentDescriptor = componentDescriptor;
    }

    public Map<Class<?>, Object> getMarkerAnnotations()
    {
        if ( markerAnnotations == null )
        {
            markerAnnotations = new HashMap<Class<?>, Object>();
        }

        return markerAnnotations;
    }
}
