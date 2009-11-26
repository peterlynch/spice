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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextMapAdapter;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.WeakClassSpace;
import org.sonatype.guice.plexus.binders.BeanWatcher;
import org.sonatype.guice.plexus.binders.PlexusBindingModule;
import org.sonatype.guice.plexus.binders.PlexusGuice;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.config.Roles;
import org.sonatype.guice.plexus.scanners.AnnotatedPlexusBeanSource;
import org.sonatype.guice.plexus.scanners.XmlPlexusBeanSource;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.matcher.AbstractMatcher;

public final class DefaultPlexusContainer
    implements PlexusContainer
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String DEFAULT_REALM_NAME = "plexus.core";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassRealm containerRealm;

    private final URL configurationUrl;

    final Context context;

    @Inject
    Logger logger;

    @Inject
    Injector injector;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unused" )
    public DefaultPlexusContainer( final ContainerConfiguration configuration )
        throws PlexusContainerException
    {
        ClassWorld world = configuration.getClassWorld();
        if ( null == world )
        {
            world = new ClassWorld( DEFAULT_REALM_NAME, Thread.currentThread().getContextClassLoader() );
        }
        ClassRealm realm = configuration.getRealm();
        if ( null == realm )
        {
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
            // TODO: log or abort?
        }

        containerRealm = realm;

        final String configurationPath = configuration.getContainerConfiguration();
        if ( null == configurationPath )
        {
            configurationUrl = configuration.getContainerConfigurationURL();
        }
        else
        {
            URL url = getClass().getClassLoader().getResource( StringUtils.stripStart( configurationPath, "/" ) );
            if ( null == url )
            {
                final File file = new File( configurationPath );
                if ( file.isFile() )
                {
                    try
                    {
                        url = file.toURI().toURL();
                    }
                    catch ( final MalformedURLException e )
                    {
                        // TODO: log or abort?
                    }
                }
            }
            if ( null == url )
            {
                // TODO: log or abort?
            }
            configurationUrl = url;
        }

        context = new DefaultContext( configuration.getContext() );
        context.put( PlexusConstants.PLEXUS_KEY, this );
        final Map<?, ?> contextMap = new ContextMapAdapter( context );

        try
        {
            final PlexusLifecycleManager lifecycleManager = new PlexusLifecycleManager();

            final ClassSpace space = new WeakClassSpace( containerRealm );
            final PlexusBeanSource xmlSource = new XmlPlexusBeanSource( configurationUrl, space, contextMap );
            final PlexusBeanSource annSource = new AnnotatedPlexusBeanSource( contextMap );

            PlexusGuice.createInjector( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    bind( PlexusContainer.class ).toInstance( DefaultPlexusContainer.this );
                    bind( Logger.class ).toInstance( new ConsoleLogger( Logger.LEVEL_DEBUG, "" ) );
                    requestInjection( DefaultPlexusContainer.this );
                }
            }, new PlexusBindingModule( lifecycleManager, xmlSource, annSource ) );
        }
        catch ( final Exception e )
        {
            // TODO: log or abort?
            throw new RuntimeException( e );
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Context getContext()
    {
        return context;
    }

    public Object lookup( final String role )
        throws ComponentLookupException
    {
        return lookup( role, Hints.DEFAULT_HINT );
    }

    @SuppressWarnings( "unchecked" )
    public Object lookup( final String role, final String hint )
        throws ComponentLookupException
    {
        try
        {
            return lookup( containerRealm.loadClass( role ), hint );
        }
        catch ( final ClassNotFoundException e )
        {
            throw new ComponentLookupException( e.toString(), role, hint );
        }
    }

    public <T> T lookup( final Class<T> type )
        throws ComponentLookupException
    {
        return lookup( type, Hints.DEFAULT_HINT );
    }

    public <T> T lookup( final Class<T> type, final String hint )
        throws ComponentLookupException
    {
        try
        {
            return injector.getInstance( Roles.componentKey( type, hint ) );
        }
        catch ( final RuntimeException e )
        {
            throw new ComponentLookupException( e.toString(), type.getName(), hint );
        }
        finally
        {
            PlexusGuice.resumeInjections( injector );
        }
    }

    public <T> T lookup( final Class<T> type, final String role, final String hint )
        throws ComponentLookupException
    {
        return role.equals( type.getName() ) ? lookup( type, hint ) : type.cast( lookup( role, hint ) );
    }

    public List<Object> lookupList( final String role )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public <T> List<T> lookupList( final Class<T> type )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public Map<String, Object> lookupMap( final String role )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public <T> Map<String, T> lookupMap( final Class<T> type )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public boolean hasComponent( final Class<?> type )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public boolean hasComponent( final Class<?> type, final String hint )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public boolean hasComponent( final Class<?> type, final String role, final String hint )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public ComponentDescriptor<?> getComponentDescriptor( final String role, final String hint )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( final Class<T> type, final String role, final String hint )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public List<ComponentDescriptor<?>> getComponentDescriptorList( final String role )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList( final Class<T> type, final String role )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public <T> void addComponentDescriptor( final ComponentDescriptor<T> descriptor )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public ClassRealm getContainerRealm()
    {
        return containerRealm;
    }

    public ClassRealm createChildRealm( final String id )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public List<ComponentDescriptor<?>> discoverComponents( final ClassRealm classRealm )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public void removeComponentRealm( final ClassRealm classRealm )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public void release( final Object component )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public void dispose()
    {
        // TODO: handle proper shutdown
    }

    // ----------------------------------------------------------------------
    // Implementation helpers
    // ----------------------------------------------------------------------

    final class PlexusLifecycleManager
        extends AbstractMatcher<Class<?>>
        implements BeanWatcher
    {
        public boolean matches( final Class<?> clazz )
        {
            return LogEnabled.class.isAssignableFrom( clazz ) || Contextualizable.class.isAssignableFrom( clazz )
                || Initializable.class.isAssignableFrom( clazz ) || Startable.class.isAssignableFrom( clazz )
                || Disposable.class.isAssignableFrom( clazz );
        }

        public void afterInjection( final Object injectee )
        {
            try
            {
                if ( injectee instanceof LogEnabled )
                {
                    ( (LogEnabled) injectee ).enableLogging( logger ); // TODO: is this OK?
                }
                if ( injectee instanceof Contextualizable )
                {
                    ( (Contextualizable) injectee ).contextualize( context );
                }
                if ( injectee instanceof Initializable )
                {
                    ( (Initializable) injectee ).initialize();
                }
                if ( injectee instanceof Startable )
                {
                    ( (Startable) injectee ).start();
                }
            }
            catch ( final Exception e )
            {
                // TODO: log or abort?
                System.out.println( e );
            }

            // TODO: handle stop + disposal...
        }
    }
}
