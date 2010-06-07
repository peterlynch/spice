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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextMapAdapter;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
import org.sonatype.guice.bean.binders.BeanImportModule;
import org.sonatype.guice.bean.locators.EntryListAdapter;
import org.sonatype.guice.bean.locators.EntryMapAdapter;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.plexus.binders.PlexusBeanManager;
import org.sonatype.guice.plexus.binders.PlexusBindingModule;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBeanConverter;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.converters.PlexusDateTypeConverter;
import org.sonatype.guice.plexus.converters.PlexusXmlBeanConverter;
import org.sonatype.guice.plexus.locators.DefaultPlexusBeanLocator;
import org.sonatype.guice.plexus.scanners.PlexusXmlBeanSource;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * {@link PlexusContainer} shim that delegates to a Plexus-aware Guice {@link Injector}.
 */
public final class DefaultPlexusContainer
    implements MutablePlexusContainer
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String DEFAULT_REALM_NAME = "plexus.core";

    private static final LoggerManager CONSOLE_LOGGER_MANAGER = new ConsoleLoggerManager();

    static
    {
        System.setProperty( "guice.plexus.mode", "true" );
    }

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Set<String> realmIds = new HashSet<String>();

    final Map<ClassRealm, List<ComponentDescriptor<?>>> descriptorMap =
        new ConcurrentHashMap<ClassRealm, List<ComponentDescriptor<?>>>();

    final ThreadLocal<ClassRealm> lookupRealm = new ThreadLocal<ClassRealm>();

    final DefaultPlexusBeanLocator beanLocator = new DefaultPlexusBeanLocator();

    final Context context;

    final Map<?, ?> variables;

    final ClassRealm containerRealm;

    final PlexusLifecycleManager lifecycleManager;

    private ContainerSecurityManager securityManager;

    private final SetupModule setupModule = new SetupModule();

    private LoggerManager loggerManager = CONSOLE_LOGGER_MANAGER;

    private Logger logger;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public DefaultPlexusContainer( final ContainerConfiguration configuration )
        throws PlexusContainerException
    {
        final URL configurationUrl = lookupConfigurationUrl( configuration );

        context = new DefaultContext( configuration.getContext() );
        context.put( PlexusConstants.PLEXUS_KEY, this );
        variables = new ContextMapAdapter( context );

        containerRealm = lookupContainerRealm( configuration );
        lifecycleManager = new PlexusLifecycleManager( this, context );

        try
        {
            // optional approach to finding role class loader
            securityManager = new ContainerSecurityManager();
        }
        catch ( final SecurityException e )
        {
            securityManager = null;
        }

        realmIds.add( containerRealm.getId() );

        final PlexusBeanSource xmlSource =
            new PlexusXmlBeanSource( new URLClassSpace( containerRealm ), variables, configurationUrl );

        beanLocator.add( Guice.createInjector( new BeanImportModule( setupModule,
                                                                     new ClassRealmModule( containerRealm ),
                                                                     new PlexusBindingModule( lifecycleManager,
                                                                                              xmlSource ) ) ) );
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
        catch ( final Throwable e )
        {
            throw new ComponentLookupException( e, role.getName(), hint );
        }
    }

    public <T> T lookup( final Class<T> type, final String role, final String hint )
        throws ComponentLookupException
    {
        return lookup( loadRoleClass( type, role ), hint );
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

    @SuppressWarnings( "unchecked" )
    public boolean hasComponent( final Class<?> role, final String hint )
    {
        final Iterator<PlexusBeanLocator.Bean> i = (Iterator) locate( role, hint ).iterator();
        return i.hasNext() && i.next().getImplementationClass() != null;
    }

    public boolean hasComponent( final Class<?> type, final String role, final String hint )
    {
        return hasComponent( loadRoleClass( type, role ), hint );
    }

    // ----------------------------------------------------------------------
    // Component descriptor methods
    // ----------------------------------------------------------------------

    public <T> void addComponent( final T component, final java.lang.Class<?> role, final String hint )
    {
        // this is only used in Maven3 tests, so keep it simple...
        beanLocator.add( Guice.createInjector( new AbstractModule()
        {
            @Override
            @SuppressWarnings( "unchecked" )
            protected void configure()
            {
                if ( Hints.isDefaultHint( hint ) )
                {
                    bind( (Class) role ).toInstance( component );
                }
                else
                {
                    bind( (Class) role ).annotatedWith( Names.named( hint ) ).toInstance( component );
                }
            }
        } ) );
    }

    public <T> void addComponentDescriptor( final ComponentDescriptor<T> descriptor )
    {
        final ClassRealm realm = descriptor.getRealm();
        if ( null != realm )
        {
            List<ComponentDescriptor<?>> descriptors = descriptorMap.get( realm );
            if ( null == descriptors )
            {
                descriptors = new ArrayList<ComponentDescriptor<?>>();
                descriptorMap.put( realm, descriptors );
            }
            descriptors.add( descriptor );
            if ( containerRealm == realm )
            {
                discoverComponents( containerRealm ); // for Maven3 testing
            }
        }
    }

    public ComponentDescriptor<?> getComponentDescriptor( final String role, final String hint )
    {
        return getComponentDescriptor( null, role, hint );
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( final Class<T> type, final String role, final String hint )
    {
        final Iterator<PlexusBeanLocator.Bean<T>> i = locate( loadRoleClass( type, role ), hint ).iterator();
        if ( i.hasNext() )
        {
            return newComponentDescriptor( i.next() );
        }
        return null;
    }

    @SuppressWarnings( "unchecked" )
    public List getComponentDescriptorList( final String role )
    {
        return getComponentDescriptorList( loadRoleClass( role ), role );
    }

    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList( final Class<T> type, final String role )
    {
        final List<ComponentDescriptor<T>> tempList = new ArrayList<ComponentDescriptor<T>>();
        for ( final PlexusBeanLocator.Bean<T> bean : locate( loadRoleClass( type, role ) ) )
        {
            tempList.add( newComponentDescriptor( bean ) );
        }
        return tempList;
    }

    public List<ComponentDescriptor<?>> discoverComponents( final ClassRealm realm )
    {
        try
        {
            final List<PlexusBeanSource> sources = new ArrayList<PlexusBeanSource>();
            final ClassSpace space = new URLClassSpace( realm );

            final List<ComponentDescriptor<?>> descriptors = descriptorMap.remove( realm );
            if ( null != descriptors )
            {
                sources.add( new ComponentDescriptorBeanSource( space, descriptors ) );
            }

            if ( realmIds.add( realm.getId() ) )
            {
                sources.add( new PlexusXmlBeanSource( space, variables ) );
            }

            if ( !sources.isEmpty() )
            {
                beanLocator.add( Guice.createInjector( new BeanImportModule( setupModule,
                                                                             new ClassRealmModule( realm ),
                                                                             new PlexusBindingModule( lifecycleManager,
                                                                                                      sources ) ) ) );
            }
        }
        catch ( final Throwable e )
        {
            getLogger().warn( realm.toString(), e );
        }

        return null; // no-one actually seems to use or check the returned component list!
    }

    // ----------------------------------------------------------------------
    // Class realm methods
    // ----------------------------------------------------------------------

    public ClassWorld getClassWorld()
    {
        return containerRealm.getWorld();
    }

    public ClassRealm getContainerRealm()
    {
        return containerRealm;
    }

    public ClassRealm setLookupRealm( final ClassRealm realm )
    {
        final ClassRealm oldRealm = lookupRealm.get();
        lookupRealm.set( realm );
        return oldRealm;
    }

    public ClassRealm getLookupRealm()
    {
        return lookupRealm.get();
    }

    // ----------------------------------------------------------------------
    // Logger methods
    // ----------------------------------------------------------------------

    public LoggerManager getLoggerManager()
    {
        return loggerManager;
    }

    @Inject( optional = true )
    public void setLoggerManager( final LoggerManager loggerManager )
    {
        if ( null != loggerManager )
        {
            this.loggerManager = loggerManager;
        }
        else
        {
            this.loggerManager = CONSOLE_LOGGER_MANAGER;
        }
        logger = null; // refresh our local logger
    }

    public Logger getLogger()
    {
        if ( null == logger )
        {
            logger = loggerManager.getLoggerForComponent( PlexusContainer.class.getName(), null );
        }
        return logger;
    }

    // ----------------------------------------------------------------------
    // Shutdown methods
    // ----------------------------------------------------------------------

    public void release( final Object component )
    {
        lifecycleManager.unmanage( component );
    }

    public void dispose()
    {
        lifecycleManager.unmanage();
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
                int index = 0;
                while ( index < configurationPath.length() && configurationPath.charAt( index ) == '/' )
                {
                    index++;
                }

                url = getClass().getClassLoader().getResource( configurationPath.substring( index ) );
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
                    getLogger().warn( "Missing container configuration: " + configurationPath );
                }
            }
        }
        return url;
    }

    @SuppressWarnings( "unchecked" )
    private <T> Class<T> loadRoleClass( final Class<T> type, final String role )
    {
        return null != type && role.equals( type.getName() ) ? type : (Class) loadRoleClass( role );
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
        Throwable exception = null;
        try
        {
            // the majority of roles are found here
            return containerRealm.loadClass( role );
        }
        catch ( final Throwable e )
        {
            exception = e;
        }
        final ClassRealm realm = getLookupRealm();
        if ( null != realm && containerRealm != realm )
        {
            try
            {
                // Plexus per-thread lookup
                return realm.loadClass( role );
            }
            catch ( final Throwable e )
            {
                exception = e;
            }
        }
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if ( null != tccl && containerRealm != tccl )
        {
            try
            {
                // standard Java per-thread lookup
                return (Class) tccl.loadClass( role );
            }
            catch ( final Throwable e )
            {
                exception = e;
            }
        }
        if ( null != securityManager )
        {
            try
            {
                // otherwise the caller should be able to see it!
                return securityManager.loadClassFromCaller( role );
            }
            catch ( final Throwable e )
            {
                exception = e;
            }
        }

        throw new TypeNotPresentException( role, exception );
    }

    /**
     * Locates instances of the given role, filtered using the given named hints.
     * 
     * @param role The Plexus role
     * @param hints The Plexus hints
     * @return Instances of the given role; ordered according to the given hints
     */
    private <T> Iterable<PlexusBeanLocator.Bean<T>> locate( final Class<T> role, final String... hints )
    {
        return beanLocator.locate( TypeLiteral.get( role ), hints );
    }

    private <T> ComponentDescriptor<T> newComponentDescriptor( final PlexusBeanLocator.Bean<T> bean )
    {
        final DeferredClass<T> clazz = bean.getImplementationClass();
        final ComponentDescriptor<T> cd = new ComponentDescriptor<T>();
        cd.setRole( clazz.getName() );
        cd.setRoleHint( bean.getKey() );
        cd.setImplementationClass( clazz.load() );
        cd.setDescription( bean.getDescription() );
        return cd;
    }

    final class SetupModule
        extends AbstractModule
    {
        final PlexusDateTypeConverter dateConverter = new PlexusDateTypeConverter();

        final PlexusXmlBeanConverter beanConverter = new PlexusXmlBeanConverter();

        final LoggerProvider loggerProvider = new LoggerProvider();

        @Override
        protected void configure()
        {
            bind( Context.class ).toInstance( context );
            bind( Map.class ).annotatedWith( Names.named( PlexusConstants.PLEXUS_KEY ) ).toInstance( variables );
            bind( Logger.class ).toProvider( loggerProvider );

            bind( MutablePlexusContainer.class ).toInstance( DefaultPlexusContainer.this );
            bind( DefaultPlexusBeanLocator.class ).toInstance( beanLocator );
            bind( PlexusBeanManager.class ).toInstance( lifecycleManager );

            install( dateConverter );
            install( beanConverter );

            bind( PlexusContainer.class ).to( MutablePlexusContainer.class );
            bind( PlexusBeanLocator.class ).to( DefaultPlexusBeanLocator.class );
            bind( PlexusBeanConverter.class ).to( PlexusXmlBeanConverter.class );
        }
    }

    final class LoggerProvider
        implements Provider<Logger>
    {
        public Logger get()
        {
            return getLogger();
        }
    }

    /**
     * Custom {@link SecurityManager} that can load Plexus roles from the calling class loader.
     */
    static final class ContainerSecurityManager
        extends SecurityManager
    {
        // ----------------------------------------------------------------------
        // Constants
        // ----------------------------------------------------------------------

        private static final String CONTAINER_CLASS_NAME = DefaultPlexusContainer.class.getName();

        // ----------------------------------------------------------------------
        // Locally-shared methods
        // ----------------------------------------------------------------------

        /**
         * Loads the given role from the class loader of the first non-container class in the call-stack.
         * 
         * @param role The Plexus role
         * @return Plexus role type
         */
        @SuppressWarnings( "unchecked" )
        Class loadClassFromCaller( final String role )
            throws ClassNotFoundException
        {
            for ( final Class clazz : getClassContext() )
            {
                // simple check to ignore container related context frames
                if ( !clazz.getName().startsWith( CONTAINER_CLASS_NAME ) )
                {
                    return clazz.getClassLoader().loadClass( role );
                }
            }
            throw new ClassNotFoundException( "Cannot find caller" );
        }
    }
}
