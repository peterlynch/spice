package org.codehaus.plexus;

import java.util.Map;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.ComponentRepository;

public interface ContainerConfiguration
{
    ContainerConfiguration setClassWorld( ClassWorld classWorld );

    ContainerConfiguration setComponentRepository( ComponentRepository repository );

    ContainerConfiguration setContainerConfiguration( String configuration );

    ContainerConfiguration setContext( Map<?, ?> context );

    Map<?, ?> getContext();
}
