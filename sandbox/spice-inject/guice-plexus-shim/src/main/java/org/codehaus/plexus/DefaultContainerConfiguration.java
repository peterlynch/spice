package org.codehaus.plexus;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.discovery.ComponentDiscoverer;
import org.codehaus.plexus.component.discovery.ComponentDiscovererManager;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryListener;
import org.codehaus.plexus.component.factory.ComponentFactoryManager;
import org.codehaus.plexus.configuration.source.ConfigurationSource;
import org.codehaus.plexus.container.initialization.ContainerInitializationPhase;
import org.codehaus.plexus.lifecycle.LifecycleHandler;
import org.codehaus.plexus.lifecycle.LifecycleHandlerManager;

public class DefaultContainerConfiguration
    implements ContainerConfiguration
{
    private String name;

    private Map<Object, Object> context;

    private String containerConfiguration;

    public ContainerConfiguration setName( String name )
    {
        this.name = name;

        return this;
    }

    public ContainerConfiguration setContext( Map<Object, Object> context )
    {
        this.context = context;

        return this;
    }

    public ContainerConfiguration setClassWorld( ClassWorld classWorld )
    {
        throw new UnsupportedOperationException();
    }

    public ContainerConfiguration setRealm( ClassRealm realm )
    {
        throw new UnsupportedOperationException();
    }

    public ContainerConfiguration setContainerConfiguration( String containerConfiguration )
    {
        this.containerConfiguration = containerConfiguration;

        return this;
    }

    public String getContainerConfiguration()
    {
        return containerConfiguration;
    }

    public ContainerConfiguration setContainerConfigurationURL( URL containerConfiguration )
    {
        throw new UnsupportedOperationException();
    }

    public URL getContainerConfigurationURL()
    {
        throw new UnsupportedOperationException();
    }

    public String getName()
    {
        return name;
    }

    public Map<Object, Object> getContext()
    {
        return context;
    }

    public ClassWorld getClassWorld()
    {
        throw new UnsupportedOperationException();
    }

    public ClassRealm getRealm()
    {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings( "unused" )
    public ContainerConfiguration setInitializationPhases( ContainerInitializationPhase[] initializationPhases )
    {
        throw new UnsupportedOperationException();
    }

    public ContainerInitializationPhase[] getInitializationPhases()
    {
        throw new UnsupportedOperationException();
    }

    public ContainerConfiguration addComponentDiscoveryListener( ComponentDiscoveryListener componentDiscoveryListener )
    {
        throw new UnsupportedOperationException();
    }

    public ContainerConfiguration addComponentDiscoverer( ComponentDiscoverer componentDiscoverer )
    {
        throw new UnsupportedOperationException();
    }

    public ContainerConfiguration setComponentDiscovererManager( ComponentDiscovererManager componentDiscovererManager )
    {
        throw new UnsupportedOperationException();
    }

    public ComponentDiscovererManager getComponentDiscovererManager()
    {
        throw new UnsupportedOperationException();
    }

    public ComponentFactoryManager getComponentFactoryManager()
    {
        throw new UnsupportedOperationException();
    }

    public ContainerConfiguration setComponentFactoryManager( ComponentFactoryManager componentFactoryManager )
    {
        throw new UnsupportedOperationException();
    }

    public ContainerConfiguration addLifecycleHandler( LifecycleHandler lifecycleHandler )
    {
        throw new UnsupportedOperationException();
    }

    public ContainerConfiguration setLifecycleHandlerManager( LifecycleHandlerManager lifecycleHandlerManager )
    {
        throw new UnsupportedOperationException();
    }

    public LifecycleHandlerManager getLifecycleHandlerManager()
    {
        throw new UnsupportedOperationException();
    }

    public ContainerConfiguration setConfigurationSource( ConfigurationSource configurationSource )
    {
        throw new UnsupportedOperationException();
    }

    public ConfigurationSource getConfigurationSource()
    {
        throw new UnsupportedOperationException();
    }

    public ContainerConfiguration addComponentDiscoverer( Class<?> clazz )
    {
        throw new UnsupportedOperationException();
    }

    public ContainerConfiguration addComponentDiscoveryListener( Class<?> clazz )
    {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings( "unchecked" )
    public List<Class> getComponentDiscoverers()
    {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings( "unchecked" )
    public List<Class> getComponentDiscoveryListeners()
    {
        throw new UnsupportedOperationException();
    }
}
