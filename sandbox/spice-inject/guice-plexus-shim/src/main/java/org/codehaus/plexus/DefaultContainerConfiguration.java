package org.codehaus.plexus;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.ComponentRepository;

public final class DefaultContainerConfiguration
    implements ContainerConfiguration
{
    public ContainerConfiguration setClassWorld( final ClassWorld classWorld )
    {
        return this;
    }

    public ContainerConfiguration setComponentRepository( final ComponentRepository repository )
    {
        return this;
    }

    public ContainerConfiguration setContainerConfiguration( final String configuration )
    {
        return this;
    }

    public ContainerConfiguration setContext( final Map<?, ?> context )
    {
        return this;
    }

    public Map<?, ?> getContext()
    {
        return new HashMap<Object, Object>();
    }
}
