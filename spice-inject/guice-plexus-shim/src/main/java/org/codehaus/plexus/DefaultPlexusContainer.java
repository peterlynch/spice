/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.codehaus.plexus;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextMapAdapter;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.StrongClassSpace;
import org.sonatype.guice.bean.reflect.StrongDeferredClass;
import org.sonatype.guice.plexus.binders.PlexusBindingModule;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.config.PlexusTypeConverter;
import org.sonatype.guice.plexus.config.PlexusTypeLocator;
import org.sonatype.guice.plexus.converters.DateTypeConverter;
import org.sonatype.guice.plexus.converters.XmlTypeConverter;
import org.sonatype.guice.plexus.locators.EntryListAdapter;
import org.sonatype.guice.plexus.locators.EntryMapAdapter;
import org.sonatype.guice.plexus.locators.GuiceTypeLocator;
import org.sonatype.guice.plexus.scanners.AnnotatedPlexusBeanSource;
import org.sonatype.guice.plexus.scanners.XmlPlexusBeanSource;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

/**
 * {@link PlexusContainer} shim that delegates to a Plexus-aware Guice {@link Injector}.
 */
public final class DefaultPlexusContainer
    extends AbstractLogEnabled
    implements PlexusContainer
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String DEFAULT_REALM_NAME = "plexus.core";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Context context;

    private final Map<?, ?> contextMap;

    private final PlexusBeanSource annSource;

    private final ClassRealm containerRealm;

    final PlexusLifecycleManager lifecycleManager;

    @Inject
    private Injector injector;

    @Inject
    private GuiceTypeLocator typeLocator;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public DefaultPlexusContainer( final ContainerConfiguration configuration )
        throws PlexusContainerException
    {
        final URL configurationUrl = lookupConfigurationUrl( configuration );

        context = new DefaultContext( configuration.getContext() );
        context.put( PlexusConstants.PLEXUS_KEY, this );
        contextMap = new ContextMapAdapter( context );

        annSource = new AnnotatedPlexusBeanSource( contextMap );
        containerRealm = lookupContainerRealm( configuration );
        lifecycleManager = new PlexusLifecycleManager();

        final ClassSpace space = new StrongClassSpace( containerRealm );
        final PlexusBeanSource xmlSource = new XmlPlexusBeanSource( configurationUrl, space, contextMap );

        Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                requestInjection( lifecycleManager );

                install( new DateTypeConverter() );
                install( new XmlTypeConverter() );

                bind( PlexusTypeLocator.class ).to( GuiceTypeLocator.class );
                bind( PlexusTypeConverter.class ).to( XmlTypeConverter.class );

                bind( Context.class ).toInstance( context );
                bind( PlexusContainer.class ).toInstance( DefaultPlexusContainer.this );
                bind( Logger.class ).toProvider( new Provider<Logger>()
                {
                    @Inject( optional = true )
                    LoggerManager loggerManager = new ConsoleLoggerManager();

                    public Logger get()
                    {
                        return loggerManager.getLogger( "ROOT" );
                    }
                } );
            }
        }, new PlexusBindingModule( lifecycleManager, xmlSource, annSource ) );
    }

    // ----------------------------------------------------------------------
    // Context methods
    // ----------------------------------------------------------------------

    public Context getContext()
    {
        return context;
    }

    // ----------------------------------------------------------------------
    // Lookup methods
    // ----------------------------------------------------------------------

    public Object lookup( final String role )
        throws ComponentLookupException
    {
        return lookup( role, Hints.DEFAULT_HINT );
    }

    public Object lookup( final String role, final String hint )
        throws ComponentLookupException
    {
        return lookup( loadRoleClass( role ), hint );
    }

    public <T> T lookup( final Class<T> role )
        throws ComponentLookupException
    {
        return lookup( role, Hints.DEFAULT_HINT );
    }

    public <T> T lookup( final Class<T> role, final String hint )
        throws ComponentLookupException
    {
        try
        {
            return locate( role, hint ).iterator().next().getValue();
        }
        catch ( final RuntimeException e )
        {
            throw new ComponentLookupException( e.toString(), role.getName(), hint );
        }
    }

    public <T> T lookup( final Class<T> type, final String role, final String hint )
        throws ComponentLookupException
    {
        return role.equals( type.getName() ) ? lookup( type, hint ) : type.cast( lookup( role, hint ) );
    }

    public List<Object> lookupList( final String role )
        throws ComponentLookupException
    {
        return lookupList( loadRoleClass( role ) );
    }

    public <T> List<T> lookupList( final Class<T> role )
    {
        return new EntryListAdapter<String, T>( locate( role ) );
    }

    public Map<String, Object> lookupMap( final String role )
        throws ComponentLookupException
    {
        return lookupMap( loadRoleClass( role ) );
    }

    public <T> Map<String, T> lookupMap( final Class<T> role )
    {
        return new EntryMapAdapter<String, T>( locate( role ) );
    }

    // ----------------------------------------------------------------------
    // Query methods
    // ----------------------------------------------------------------------

    public boolean hasComponent( final Class<?> role )
    {
        return hasComponent( role, Hints.DEFAULT_HINT );
    }

    public boolean hasComponent( final Class<?> role, final String hint )
    {
        return locate( role, hint ).iterator().hasNext();
    }

    public boolean hasComponent( final Class<?> type, final String role, final String hint )
    {
        return role.equals( type.getName() ) ? hasComponent( type, hint ) : hasComponent( loadRoleClass( role ), hint );
    }

    // ----------------------------------------------------------------------
    // Component descriptor methods
    // ----------------------------------------------------------------------

    public ComponentDescriptor<?> getComponentDescriptor( final String role, final String hint )
    {
        return getComponentDescriptor( Object.class, role, hint );
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( final Class<T> type, final String role, final String hint )
    {
        final ComponentDescriptor<T> descriptor = new ComponentDescriptor<T>();
        descriptor.setRole( role );
        descriptor.setRoleHint( hint );
        return descriptor;
    }

    @SuppressWarnings( "unchecked" )
    public List<ComponentDescriptor<?>> getComponentDescriptorList( final String role )
    {
        return (List) getComponentDescriptorList( Object.class, role );
    }

    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList( final Class<T> type, final String role )
    {
        final List<ComponentDescriptor<T>> tempList = new ArrayList<ComponentDescriptor<T>>();
        for ( final Entry<String, T> entry : locate( type ) )
        {
            final ComponentDescriptor<T> descriptor = new ComponentDescriptor<T>();
            descriptor.setRole( role );
            descriptor.setRoleHint( entry.getKey() );
            tempList.add( descriptor );
        }
        return tempList;
    }

    public <T> void addComponentDescriptor( final ComponentDescriptor<T> descriptor )
        throws CycleDetectedInComponentGraphException
    {
        // TODO: do we need to do anything here?
        getLogger().warn( "TODO DefaultPlexusContainer.addComponentDescriptor(" + descriptor + ")" );
    }

    public List<ComponentDescriptor<?>> discoverComponents( final ClassRealm classRealm )
    {
        final ClassSpace space = new ClassSpace()
        {
            public Class<?> loadClass( final String name )
                throws ClassNotFoundException
            {
                return classRealm.loadClass( name );
            }

            public Enumeration<URL> getResources( final String name )
                throws IOException
            {
                return new URLClassLoader( classRealm.getURLs() ).getResources( name );
            }

            @SuppressWarnings( "unchecked" )
            public DeferredClass<?> deferLoadClass( final String name )
            {
                return new StrongDeferredClass( this, name );
            }
        };

        final PlexusBeanSource xmlSource = new XmlPlexusBeanSource( null, space, contextMap );
        typeLocator.add( injector.createChildInjector( new PlexusBindingModule( lifecycleManager, xmlSource, annSource ) ) );
        return Collections.emptyList();
    }

    // ----------------------------------------------------------------------
    // Class realm methods
    // ----------------------------------------------------------------------

    public ClassRealm getContainerRealm()
    {
        return containerRealm;
    }

    public ClassRealm createChildRealm( final String id )
    {
        try
        {
            return containerRealm.createChildRealm( id );
        }
        catch ( final DuplicateRealmException e )
        {
            return containerRealm.getWorld().getClassRealm( id );
        }
    }

    public void removeComponentRealm( final ClassRealm classRealm )
    {
        // TODO: do we need to do anything here?
        getLogger().warn( "TODO DefaultPlexusContainer.removeComponentRealm(" + classRealm + ")" );
    }

    // ----------------------------------------------------------------------
    // Shutdown methods
    // ----------------------------------------------------------------------

    public void release( final Object component )
    {
        // TODO: do we need to do anything here?
        getLogger().warn( "TODO DefaultPlexusContainer.release(" + component + ")" );
    }

    public void dispose()
    {
        lifecycleManager.dispose();

        containerRealm.setParentRealm( null );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Finds container {@link ClassRealm}, taking existing {@link ClassWorld}s or {@link ClassLoader}s into account.
     * 
     * @param configuration The container configuration
     * @return Container class realm
     */
    private ClassRealm lookupContainerRealm( final ContainerConfiguration configuration )
        throws PlexusContainerException
    {
        ClassRealm realm = configuration.getRealm();
        if ( null == realm )
        {
            ClassWorld world = configuration.getClassWorld();
            if ( null == world )
            {
                world = new ClassWorld( DEFAULT_REALM_NAME, Thread.currentThread().getContextClassLoader() );
            }
            try
            {
                realm = world.getRealm( DEFAULT_REALM_NAME );
            }
            catch ( final NoSuchRealmException e )
            {
                final Iterator<?> realmIterator = world.getRealms().iterator();
                if ( realmIterator.hasNext() )
                {
                    realm = (ClassRealm) realmIterator.next();
                }
            }
        }
        if ( null == realm )
        {
            throw new PlexusContainerException( "Unable to find class realm: " + DEFAULT_REALM_NAME );
        }
        return realm;
    }

    /**
     * Finds container configuration URL, may search the container {@link ClassRealm} and local file-system.
     * 
     * @param configuration The container configuration
     * @return Local or remote URL
     */
    private URL lookupConfigurationUrl( final ContainerConfiguration configuration )
    {
        URL url = configuration.getContainerConfigurationURL();
        if ( null == url )
        {
            final String configurationPath = configuration.getContainerConfiguration();
            if ( null != configurationPath )
            {
                url = getClass().getClassLoader().getResource( StringUtils.stripStart( configurationPath, "/" ) );
                if ( null == url )
                {
                    final File file = new File( configurationPath );
                    if ( file.isFile() )
                    {
                        try
                        {
                            url = file.toURI().toURL();
                        }
                        catch ( final MalformedURLException e ) // NOPMD
                        {
                            // drop through and recover
                        }
                    }
                }
                if ( null == url )
                {
                    ConsoleLoggerManager.LOGGER.warn( "Bad container configuration path: " + configurationPath );
                }
            }
        }
        return url;
    }

    /**
     * Loads the named Plexus role from the current container {@link ClassRealm}.
     * 
     * @param role The Plexus role
     * @return Plexus role class
     */
    @SuppressWarnings( "unchecked" )
    private Class<Object> loadRoleClass( final String role )
    {
        try
        {
            return containerRealm.loadClass( role );
        }
        catch ( final ClassNotFoundException e )
        {
            throw new TypeNotPresentException( role, e );
        }
    }

    /**
     * Locates instances of the given role, filtered using the given named hints.
     * 
     * @param role The Plexus role
     * @param hints The Plexus hints
     * @return Instances of the given role; ordered according to the given hints
     */
    private <T> Iterable<Entry<String, T>> locate( final Class<T> role, final String... hints )
    {
        return typeLocator.locate( TypeLiteral.get( role ), hints );
    }
}
