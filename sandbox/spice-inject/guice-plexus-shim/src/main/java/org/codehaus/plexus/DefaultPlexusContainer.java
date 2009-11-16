package org.codehaus.plexus;

import java.util.List;
import java.util.Map;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.discovery.ComponentDiscovererManager;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryListener;
import org.codehaus.plexus.component.factory.ComponentFactoryManager;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.source.ConfigurationSource;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;

public class DefaultPlexusContainer
    implements MutablePlexusContainer
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public DefaultPlexusContainer()
    {
        throw new UnsupportedOperationException();
    }

    public DefaultPlexusContainer( ContainerConfiguration c )
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------------

    public PlexusConfiguration getConfiguration()
    {
        throw new UnsupportedOperationException();
    }

    public void setConfiguration( PlexusConfiguration configuration )
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

    // ----------------------------------------------------------------------
    // Context
    // ----------------------------------------------------------------------

    public Context getContext()
    {
        throw new UnsupportedOperationException();
    }

    public void addContextValue( Object key, Object value )
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Logging
    // ----------------------------------------------------------------------

    public LoggerManager getLoggerManager()
    {
        throw new UnsupportedOperationException();
    }

    public void setLoggerManager( LoggerManager loggerManager )
    {
        throw new UnsupportedOperationException();
    }

    public Logger getLogger()
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // ClassWorld Management
    // ----------------------------------------------------------------------

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

    public ClassRealm createChildRealm( String id )
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

    // ----------------------------------------------------------------------------
    // Component Lookup
    // ----------------------------------------------------------------------------

    public Object lookup( String role )
    {
        throw new UnsupportedOperationException();
    }

    public Object lookup( String role, String roleHint )
    {
        throw new UnsupportedOperationException();
    }

    public <T> T lookup( Class<T> type )
    {
        throw new UnsupportedOperationException();
    }

    public <T> T lookup( Class<T> type, String roleHint )
    {
        throw new UnsupportedOperationException();
    }

    public <T> T lookup( Class<T> type, String role, String roleHint )
    {
        throw new UnsupportedOperationException();
    }

    public List<Object> lookupList( String role )
    {
        throw new UnsupportedOperationException();
    }

    public List<Object> lookupList( String role, List<String> roleHints )
    {
        throw new UnsupportedOperationException();
    }

    public <T> List<T> lookupList( Class<T> type )
    {
        throw new UnsupportedOperationException();
    }

    public <T> List<T> lookupList( Class<T> type, List<String> roleHints )
    {
        throw new UnsupportedOperationException();
    }

    public Map<String, Object> lookupMap( String role )
    {
        throw new UnsupportedOperationException();
    }

    public Map<String, Object> lookupMap( String role, List<String> roleHints )
    {
        throw new UnsupportedOperationException();
    }

    public <T> Map<String, T> lookupMap( Class<T> type )
    {
        throw new UnsupportedOperationException();
    }

    public <T> Map<String, T> lookupMap( Class<T> type, List<String> roleHints )
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Component Queries
    // ----------------------------------------------------------------------

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

    public boolean hasComponent( Class<?> type, String role, String roleHint )
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Component Descriptors
    // ----------------------------------------------------------------------

    public ComponentDescriptor<?> getComponentDescriptor( String role )
    {
        throw new UnsupportedOperationException();
    }

    public ComponentDescriptor<?> getComponentDescriptor( String role, String roleHint )
    {
        throw new UnsupportedOperationException();
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( Class<T> type, String role, String roleHint )
    {
        throw new UnsupportedOperationException();
    }

    public Map<String, ComponentDescriptor<?>> getComponentDescriptorMap( String role )
    {
        throw new UnsupportedOperationException();
    }

    public <T> Map<String, ComponentDescriptor<T>> getComponentDescriptorMap( Class<T> type, String role )
    {
        throw new UnsupportedOperationException();
    }

    public List<ComponentDescriptor<?>> getComponentDescriptorList( String role )
    {
        throw new UnsupportedOperationException();
    }

    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList( Class<T> type, String role )
    {
        throw new UnsupportedOperationException();
    }

    public void addComponentDescriptor( ComponentDescriptor<?> componentDescriptor )
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Component Management
    // ----------------------------------------------------------------------

    public void addComponent( Object component, String role )
    {
        throw new UnsupportedOperationException();
    }

    public <T> void addComponent( T component, Class<?> role, String roleHint )
    {
        throw new UnsupportedOperationException();
    }

    public void addComponent( Object component, String role, String roleHint )
    {
        throw new UnsupportedOperationException();
    }

    public void release( Object component )
    {
        throw new UnsupportedOperationException();
    }

    public void releaseAll( Map<String, ?> components )
    {
        throw new UnsupportedOperationException();
    }

    public void releaseAll( List<?> components )
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------------
    // Component Registry
    // ----------------------------------------------------------------------------

    public ComponentRegistry getComponentRegistry()
    {
        throw new UnsupportedOperationException();
    }

    public void setComponentRegistry( ComponentRegistry componentRegistry )
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------------
    // Component Discovery
    // ----------------------------------------------------------------------------

    public ComponentDiscovererManager getComponentDiscovererManager()
    {
        throw new UnsupportedOperationException();
    }

    public void setComponentDiscovererManager( ComponentDiscovererManager componentDiscovererManager )
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

    public List<ComponentDescriptor<?>> discoverComponents( ClassRealm realm )
    {
        throw new UnsupportedOperationException();
    }

    public List<ComponentDescriptor<?>> discoverComponents( ClassRealm realm, Object data )
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------------
    // Component Factories
    // ----------------------------------------------------------------------------

    public ComponentFactoryManager getComponentFactoryManager()
    {
        throw new UnsupportedOperationException();
    }

    public void setComponentFactoryManager( ComponentFactoryManager componentFactoryManager )
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------------
    // Component Realms
    // ----------------------------------------------------------------------------

    public ClassRealm getComponentRealm( String realmId )
    {
        throw new UnsupportedOperationException();
    }

    public void removeComponentRealm( ClassRealm realm )
    {
        throw new UnsupportedOperationException();
    }

    public ClassRealm getLookupRealm( Object component )
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // LifeCycle Management
    // ----------------------------------------------------------------------

    public void dispose()
    {
        throw new UnsupportedOperationException();
    }
}
