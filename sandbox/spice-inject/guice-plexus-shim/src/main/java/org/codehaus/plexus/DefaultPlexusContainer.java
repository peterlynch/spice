package org.codehaus.plexus;

import java.util.List;
import java.util.Map;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.discovery.ComponentDiscovererManager;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryListener;
import org.codehaus.plexus.component.factory.ComponentFactoryManager;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentDescriptorListener;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.component.repository.exception.ComponentRepositoryException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.configuration.source.ConfigurationSource;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;

public class DefaultPlexusContainer
    implements MutablePlexusContainer
{
    public void addComponent( Object component, String role )
        throws ComponentRepositoryException
    {
        throw new UnsupportedOperationException();
    }

    public <T> void addComponent( T component, Class<?> type, String roleHint, ClassRealm classRealm )
        throws ComponentRepositoryException
    {
        throw new UnsupportedOperationException();
    }

    public ClassRealm setLookupRealm( ClassRealm realm )
    {
        throw new UnsupportedOperationException();
    }

    public ClassRealm getLookupRealm()
    {
        throw new UnsupportedOperationException();
    }

    public DefaultPlexusContainer()
        throws PlexusContainerException
    {
        throw new UnsupportedOperationException();
    }

    public DefaultPlexusContainer( ContainerConfiguration c )
        throws PlexusContainerException
    {
        throw new UnsupportedOperationException();
    }

    public ClassRealm createChildRealm( String id )
    {
        throw new UnsupportedOperationException();
    }

    public Object lookup( String role )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    public Object lookup( String role, String roleHint )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    public <T> T lookup( Class<T> type )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    public <T> T lookup( Class<T> type, String roleHint )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public <T> T lookup( Class<T> type, String role, String roleHint )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    public <T> T lookup( ComponentDescriptor<T> descriptor )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    public List<Object> lookupList( String role )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    public List<Object> lookupList( String role, List<String> roleHints )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    public <T> List<T> lookupList( Class<T> type )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    public <T> List<T> lookupList( Class<T> type, List<String> roleHints )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    public Map<String, Object> lookupMap( String role )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    public Map<String, Object> lookupMap( String role, List<String> roleHints )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    public <T> Map<String, T> lookupMap( Class<T> type )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    public <T> Map<String, T> lookupMap( Class<T> type, List<String> roleHints )
        throws ComponentLookupException
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasComponent( String role )
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasComponent( String role, String roleHint )
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasComponent( Class<?> type )
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasComponent( Class<?> type, String roleHint )
    {
        throw new UnsupportedOperationException();
    }

    public ComponentDescriptor<?> getComponentDescriptor( String role )
    {
        throw new UnsupportedOperationException();
    }

    public ComponentDescriptor<?> getComponentDescriptor( String role, String roleHint )
    {
        throw new UnsupportedOperationException();
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( Class<T> type )
    {
        throw new UnsupportedOperationException();
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( Class<T> type, String roleHint )
    {
        throw new UnsupportedOperationException();
    }

    public Map<String, ComponentDescriptor<?>> getComponentDescriptorMap( String role )
    {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public <T> Map<String, ComponentDescriptor<T>> getComponentDescriptorMap( Class<T> type, String role )
    {
        throw new UnsupportedOperationException();
    }

    public List<ComponentDescriptor<?>> getComponentDescriptorList( String role )
    {
        throw new UnsupportedOperationException();
    }

    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList( Class<T> type )
    {
        throw new UnsupportedOperationException();
    }

    public void addComponentDescriptor( ComponentDescriptor<?> componentDescriptor )
        throws ComponentRepositoryException
    {
        throw new UnsupportedOperationException();
    }

    public void release( Object component )
        throws ComponentLifecycleException
    {
        throw new UnsupportedOperationException();
    }

    public void releaseAll( Map<String, ?> components )
        throws ComponentLifecycleException
    {
        throw new UnsupportedOperationException();
    }

    public void releaseAll( List<?> components )
        throws ComponentLifecycleException
    {
        throw new UnsupportedOperationException();
    }

    public void dispose()
    {
        throw new UnsupportedOperationException();
    }

    public void addContextValue( Object key, Object value )
    {
        throw new UnsupportedOperationException();
    }

    public ClassWorld getClassWorld()
    {
        throw new UnsupportedOperationException();
    }

    public void setClassWorld( ClassWorld classWorld )
    {
        throw new UnsupportedOperationException();
    }

    public ClassRealm getContainerRealm()
    {
        throw new UnsupportedOperationException();
    }

    public void setContainerRealm( ClassRealm containerRealm )
    {
        throw new UnsupportedOperationException();
    }

    public Context getContext()
    {
        throw new UnsupportedOperationException();
    }

    public void addComponentDescriptorListener( ComponentDescriptorListener<?> listener )
    {
        throw new UnsupportedOperationException();
    }

    public void removeComponentDescriptorListener( ComponentDescriptorListener<?> listener )
    {
        throw new UnsupportedOperationException();
    }

    public Logger getLogger()
    {
        throw new UnsupportedOperationException();
    }

    public void registerComponentDiscoveryListener( ComponentDiscoveryListener listener )
    {
        throw new UnsupportedOperationException();
    }

    public void removeComponentDiscoveryListener( ComponentDiscoveryListener listener )
    {
        throw new UnsupportedOperationException();
    }

    public ComponentRegistry getComponentRegistry()
    {
        throw new UnsupportedOperationException();
    }

    public void setComponentRegistry( ComponentRegistry componentRegistry )
    {
        throw new UnsupportedOperationException();
    }

    public ComponentDiscovererManager getComponentDiscovererManager()
    {
        throw new UnsupportedOperationException();
    }

    public void setComponentDiscovererManager( ComponentDiscovererManager componentDiscovererManager )
    {
        throw new UnsupportedOperationException();
    }

    public ComponentFactoryManager getComponentFactoryManager()
    {
        throw new UnsupportedOperationException();
    }

    public void setComponentFactoryManager( ComponentFactoryManager componentFactoryManager )
    {
        throw new UnsupportedOperationException();
    }

    public PlexusConfiguration getConfiguration()
    {
        throw new UnsupportedOperationException();
    }

    public void setConfiguration( PlexusConfiguration configuration )
    {
        throw new UnsupportedOperationException();
    }

    public ClassRealm getComponentRealm( String realmId )
    {
        throw new UnsupportedOperationException();
    }

    public void removeComponentRealm( ClassRealm realm )
        throws PlexusContainerException
    {
        throw new UnsupportedOperationException();
    }

    public ClassRealm getLookupRealm( Object component )
    {
        throw new UnsupportedOperationException();
    }

    public void setConfigurationSource( ConfigurationSource configurationSource )
    {
        throw new UnsupportedOperationException();
    }

    public ConfigurationSource getConfigurationSource()
    {
        throw new UnsupportedOperationException();
    }

    public LoggerManager getLoggerManager()
    {
        throw new UnsupportedOperationException();
    }

    public void setLoggerManager( LoggerManager loggerManager )
    {
        throw new UnsupportedOperationException();
    }

    public List<ComponentDescriptor<?>> discoverComponents( ClassRealm realm )
        throws PlexusConfigurationException, ComponentRepositoryException
    {
        throw new UnsupportedOperationException();
    }
}
