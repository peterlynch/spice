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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.sonatype.guice.bean.binders.ParameterKeys;
import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.locators.DefaultBeanLocator;
import org.sonatype.guice.bean.locators.EntryListAdapter;
import org.sonatype.guice.bean.locators.EntryMapAdapter;
import org.sonatype.guice.bean.locators.MutableBeanLocator;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.plexus.binders.PlexusAnnotatedBeanModule;
import org.sonatype.guice.plexus.binders.PlexusBeanManager;
import org.sonatype.guice.plexus.binders.PlexusBindingModule;
import org.sonatype.guice.plexus.binders.PlexusXmlBeanModule;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBean;
import org.sonatype.guice.plexus.config.PlexusBeanConverter;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;
import org.sonatype.guice.plexus.config.PlexusBeanModule;
import org.sonatype.guice.plexus.converters.PlexusDateTypeConverter;
import org.sonatype.guice.plexus.converters.PlexusXmlBeanConverter;
import org.sonatype.guice.plexus.locators.DefaultPlexusBeanLocator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

/**
 * {@link PlexusContainer} shim that delegates to a Plexus-aware Guice {@link Injector}.
 */
@SuppressWarnings( { "unchecked", "rawtypes" } )
public final class DefaultPlexusContainer
    implements MutablePlexusContainer
{
    static
    {
        System.setProperty( "guice.disable.misplaced.annotation.check", "true" );
    }

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String DEFAULT_REALM_NAME = "plexus.core";

    private static final LoggerManager CONSOLE_LOGGER_MANAGER = new ConsoleLoggerManager();

    private static final boolean LOAD_CLASS_FROM_SELF_SUPPORTED;

    static
    {
        boolean loadClassFromSelfSupported = true;
        try
        {
            ClassRealm.class.getDeclaredMethod( "loadClassFromSelf", String.class );
        }
        catch ( final Throwable e )
        {
            loadClassFromSelfSupported = false;
        }
        LOAD_CLASS_FROM_SELF_SUPPORTED = loadClassFromSelfSupported;
    }

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Set<String> realmIds = new HashSet<String>();

    final Map<ClassRealm, List<ComponentDescriptor<?>>> descriptorMap =
        new ConcurrentHashMap<ClassRealm, List<ComponentDescriptor<?>>>();

    final ThreadLocal<ClassRealm> lookupRealm = new ThreadLocal<ClassRealm>();

    final MutableBeanLocator qualifiedBeanLocator = new DefaultBeanLocator();

    final Context context;

    final Map<String, String> variables;

    final ClassRealm containerRealm;

    final PlexusLifecycleManager lifecycleManager;

    final PlexusBeanLocator plexusBeanLocator;

    private final boolean isClassPathScanningEnabled;

    private final Module bootModule = new BootModule();

    private final Module setupModule = new SetupModule();

    private final Module loggerModule = new LoggerModule();

    private LoggerManager loggerManager = CONSOLE_LOGGER_MANAGER;

    private Logger logger;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public DefaultPlexusContainer()
        throws PlexusContainerException
    {
        this( new DefaultContainerConfiguration() );
    }

    @SuppressWarnings( "finally" )
    public DefaultPlexusContainer( final ContainerConfiguration configuration )
        throws PlexusContainerException
    {
        final URL plexusXml = lookupPlexusXml( configuration );

        context = new DefaultContext( configuration.getContext() );
        context.put( PlexusConstants.PLEXUS_KEY, this );
        variables = (Map) new ContextMapAdapter( context );

        containerRealm = lookupContainerRealm( configuration );
        lifecycleManager = new PlexusLifecycleManager( this, context );

        plexusBeanLocator = new DefaultPlexusBeanLocator( qualifiedBeanLocator, configuration.getComponentVisibility() );

        realmIds.add( containerRealm.getId() );
        setLookupRealm( containerRealm );

        final List<PlexusBeanModule> beanModules = new ArrayList<PlexusBeanModule>();

        final ClassSpace space = new URLClassSpace( containerRealm );
        beanModules.add( new PlexusXmlBeanModule( space, variables, plexusXml ) );
        isClassPathScanningEnabled = configuration.getClassPathScanning();
        if ( isClassPathScanningEnabled )
        {
            beanModules.add( new PlexusAnnotatedBeanModule( space, variables ) );
        }

        try
        {
            addPlexusInjector( beanModules, bootModule );
        }
        catch ( final RuntimeException e )
        {
            try
            {
                dispose(); // cleanup as much as possible
            }
            finally
            {
                throw e; // always report original failure
            }
        }
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

    public boolean hasComponent( final Class role, final String hint )
    {
        final Iterator<PlexusBean<?>> i = locate( role, hint ).iterator();
        return i.hasNext() && i.next().getImplementationClass() != null;
    }

    public boolean hasComponent( final Class<?> type, final String role, final String hint )
    {
        try
        {
            return hasComponent( loadRoleClass( type, role ), hint );
        }
        catch ( final ComponentLookupException e )
        {
            return false;
        }
    }

    // ----------------------------------------------------------------------
    // Component descriptor methods
    // ----------------------------------------------------------------------

    public <T> void addComponent( final T component, final java.lang.Class<?> role, final String hint )
    {
        // this is only used in Maven3 tests, so keep it simple...
        qualifiedBeanLocator.add( Guice.createInjector( new AbstractModule()
        {
            @Override
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
        ClassRealm realm = descriptor.getRealm();
        if ( null == realm )
        {
            realm = containerRealm;
            descriptor.setRealm( realm );
        }
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

    public ComponentDescriptor<?> getComponentDescriptor( final String role, final String hint )
    {
        return getComponentDescriptor( null, role, hint );
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( final Class<T> type, final String role, final String hint )
    {
        try
        {
            final Iterator<PlexusBean<T>> i = locate( loadRoleClass( type, role ), hint ).iterator();
            if ( i.hasNext() )
            {
                return newComponentDescriptor( role, i.next() );
            }
        }
        catch ( final ComponentLookupException e ) // NOPMD
        {
            // ignore
        }
        return null;
    }

    public List getComponentDescriptorList( final String role )
    {
        try
        {
            return getComponentDescriptorList( loadRoleClass( role ), role );
        }
        catch ( final ComponentLookupException e )
        {
            return Collections.EMPTY_LIST;
        }
    }

    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList( final Class<T> type, final String role )
    {
        try
        {
            final List<ComponentDescriptor<T>> tempList = new ArrayList<ComponentDescriptor<T>>();
            for ( final PlexusBean<T> bean : locate( loadRoleClass( type, role ) ) )
            {
                tempList.add( newComponentDescriptor( role, bean ) );
            }
            return tempList;
        }
        catch ( final ComponentLookupException e )
        {
            return Collections.EMPTY_LIST;
        }
    }

    public Map getComponentDescriptorMap( final String role )
    {
        try
        {
            return getComponentDescriptorMap( loadRoleClass( role ), role );
        }
        catch ( final ComponentLookupException e )
        {
            return Collections.EMPTY_MAP;
        }
    }

    public <T> Map<String, ComponentDescriptor<T>> getComponentDescriptorMap( final Class<T> type, final String role )
    {
        try
        {
            final Map<String, ComponentDescriptor<T>> tempMap = new LinkedHashMap<String, ComponentDescriptor<T>>();
            for ( final PlexusBean<T> bean : locate( loadRoleClass( type, role ) ) )
            {
                tempMap.put( bean.getKey(), newComponentDescriptor( role, bean ) );
            }
            return tempMap;
        }
        catch ( final ComponentLookupException e )
        {
            return Collections.EMPTY_MAP;
        }
    }

    public List<ComponentDescriptor<?>> discoverComponents( final ClassRealm realm )
    {
        try
        {
            final List<PlexusBeanModule> beanModules = new ArrayList<PlexusBeanModule>();

            final ClassSpace space = new URLClassSpace( realm );
            final List<ComponentDescriptor<?>> descriptors = descriptorMap.remove( realm );
            if ( null != descriptors )
            {
                beanModules.add( new ComponentDescriptorBeanModule( space, descriptors ) );
            }
            if ( realmIds.add( realm.getId() ) )
            {
                beanModules.add( new PlexusXmlBeanModule( space, variables ) );
                if ( isClassPathScanningEnabled )
                {
                    beanModules.add( new PlexusAnnotatedBeanModule( space, variables ) );
                }
            }
            if ( !beanModules.isEmpty() )
            {
                addPlexusInjector( beanModules );
            }
        }
        catch ( final Throwable e )
        {
            getLogger().warn( realm.toString(), e );
        }

        return null; // no-one actually seems to use or check the returned component list!
    }

    public void addPlexusInjector( final List<PlexusBeanModule> beanModules, final Module... customModules )
    {
        final List<Module> modules = new ArrayList<Module>();

        modules.add( setupModule );
        Collections.addAll( modules, customModules );
        modules.add( new PlexusBindingModule( lifecycleManager, beanModules ) );
        modules.add( loggerModule );

        Guice.createInjector( new WireModule( modules.toArray( new Module[modules.size()] ) ) );
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
        qualifiedBeanLocator.clear();
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
    private URL lookupPlexusXml( final ContainerConfiguration configuration )
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
                    getLogger().debug( "Missing container configuration: " + configurationPath );
                }
            }
        }
        return url;
    }

    private <T> Class<T> loadRoleClass( final Class<T> type, final String role )
        throws ComponentLookupException
    {
        return null != type && role.equals( type.getName() ) ? type : (Class) loadRoleClass( role );
    }

    /**
     * Loads the named Plexus role from the current container {@link ClassRealm}.
     * 
     * @param role The Plexus role
     * @return Plexus role class
     */
    private Class loadRoleClass( final String role )
        throws ComponentLookupException
    {
        try
        {
            final ClassRealm realm = getLookupRealm();
            if ( null != realm )
            {
                try
                {
                    // Plexus per-thread lookup
                    return realm.loadClass( role );
                }
                catch ( final ClassNotFoundException e ) // NOPMD
                {
                    // drop-through
                }
            }
            for ( final ClassRealm childRealm : (Collection<ClassRealm>) getClassWorld().getRealms() )
            {
                if ( containerRealm == childRealm.getParentRealm() )
                {
                    // try each plugin in turn without delegating to parent
                    final Class clazz = loadClassFromSelf( childRealm, role );
                    if ( null != clazz )
                    {
                        return clazz;
                    }
                }
            }
            // no plugins? then check the core realm
            return containerRealm.loadClass( role );
        }
        catch ( final Throwable e )
        {
            throw new ComponentLookupException( e, role, Hints.DEFAULT_HINT );
        }
    }

    private Class loadClassFromSelf( final ClassRealm realm, final String role )
    {
        if ( LOAD_CLASS_FROM_SELF_SUPPORTED )
        {
            // try to delay loading from parent
            return realm.loadClassFromSelf( role );
        }
        try
        {
            // fall-back to classic model
            return realm.loadClass( role );
        }
        catch ( final ClassNotFoundException e )
        {
            return null;
        }
    }

    /**
     * Locates instances of the given role, filtered using the given named hints.
     * 
     * @param role The Plexus role
     * @param hints The Plexus hints
     * @return Instances of the given role; ordered according to the given hints
     */
    private <T> Iterable<PlexusBean<T>> locate( final Class<T> role, final String... hints )
    {
        return plexusBeanLocator.locate( TypeLiteral.get( role ), Hints.canonicalHints( hints ) );
    }

    private <T> ComponentDescriptor<T> newComponentDescriptor( final String role, final PlexusBean<T> bean )
    {
        final ComponentDescriptor<T> cd = new ComponentDescriptor<T>();
        cd.setRole( role );
        cd.setRoleHint( bean.getKey() );
        cd.setImplementationClass( bean.getImplementationClass() );
        cd.setDescription( bean.getDescription() );
        return cd;
    }

    final class BootModule
        extends AbstractModule
    {
        @Override
        protected void configure()
        {
            requestInjection( DefaultPlexusContainer.this );
        }
    }

    final class SetupModule
        extends AbstractModule
    {
        final PlexusDateTypeConverter dateConverter = new PlexusDateTypeConverter();

        final PlexusXmlBeanConverter beanConverter = new PlexusXmlBeanConverter();

        @Override
        protected void configure()
        {
            bind( Context.class ).toInstance( context );
            bind( ParameterKeys.PROPERTIES ).toInstance( variables );

            install( dateConverter );

            bind( MutableBeanLocator.class ).toInstance( qualifiedBeanLocator );

            bind( PlexusContainer.class ).to( MutablePlexusContainer.class );
            bind( PlexusBeanConverter.class ).toInstance( beanConverter );
            bind( PlexusBeanLocator.class ).toInstance( plexusBeanLocator );
            bind( PlexusBeanManager.class ).toInstance( lifecycleManager );

            // use provider wrapper to avoid repeated injections later on when configuring plugin injectors
            bind( MutablePlexusContainer.class ).toProvider( Providers.of( DefaultPlexusContainer.this ) );
        }
    }

    final class LoggerModule
        extends AbstractModule
    {
        final LoggerManagerProvider loggerManagerProvider = new LoggerManagerProvider();

        final LoggerProvider loggerProvider = new LoggerProvider();

        @Override
        protected void configure()
        {
            bind( LoggerManager.class ).toProvider( loggerManagerProvider );
            bind( Logger.class ).toProvider( loggerProvider );
        }
    }

    final class LoggerManagerProvider
        implements Provider<LoggerManager>
    {
        public LoggerManager get()
        {
            return getLoggerManager();
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
}
