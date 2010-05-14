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
package org.sonatype.guice.plexus.scanners;

import java.lang.annotation.Annotation;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.LoadedClass;
import org.sonatype.guice.bean.scanners.QualifiedBeanRegistry;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.Roles;
import org.sonatype.guice.plexus.config.Strategies;

import com.google.inject.Key;
import com.google.inject.name.Named;

/**
 * Enhanced Plexus component map with additional book-keeping.
 */
final class PlexusComponentRegistry
    extends QualifiedBeanRegistry
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Component LOAD_ON_START_PLACEHOLDER =
        new ComponentImpl( Object.class, "", Strategies.LOAD_ON_START, "" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<String, Component> components = new HashMap<String, Component>();

    private final Map<String, DeferredClass<?>> implementations = new HashMap<String, DeferredClass<?>>();

    private final Set<String> deferredNames = new HashSet<String>();

    final ClassSpace space;

    private ClassSpace disambiguatedSpace;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusComponentRegistry( final ClassSpace space )
    {
        this.space = space;
    }

    // ----------------------------------------------------------------------
    // Locally-shared methods
    // ----------------------------------------------------------------------

    @Override
    public void add( final Key<?> key, final Class<?> beanType )
    {
        final Annotation qualifier = key.getAnnotation();
        if ( qualifier instanceof Named )
        {
            final Class<?> role = key.getTypeLiteral().getRawType();
            final String hint = ( (Named) qualifier ).value();

            final Component component;
            if ( beanType.isAnnotationPresent( javax.inject.Singleton.class )
                || beanType.isAnnotationPresent( com.google.inject.Singleton.class ) )
            {
                component = new ComponentImpl( role, hint, Strategies.SINGLETON, "" );
            }
            else
            {
                component = new ComponentImpl( role, hint, Strategies.PER_LOOKUP, "" );
            }

            final String roleHint = Roles.canonicalRoleHint( component );
            implementations.put( roleHint, new LoadedClass<Object>( beanType ) );
            components.put( roleHint, component );
        }
    }

    /**
     * @return Current class space
     */
    ClassSpace getSpace()
    {
        return space;
    }

    /**
     * Records that the given Plexus component should be loaded when the container starts.
     * 
     * @param role The Plexus role
     * @param hint The Plexus hint
     */
    void loadOnStart( final String role, final String hint )
    {
        final String key = Roles.canonicalRoleHint( role, hint );
        final Component c = components.get( key );
        if ( null == c )
        {
            components.put( key, LOAD_ON_START_PLACEHOLDER );
        }
        else if ( !Strategies.LOAD_ON_START.equals( c.instantiationStrategy() ) )
        {
            components.put( key, new ComponentImpl( c.role(), c.hint(), Strategies.LOAD_ON_START, c.description() ) );
        }
    }

    /**
     * Registers the given component, automatically disambiguating between implementations bound multiple times.
     * 
     * @param role The Plexus role
     * @param hint The Plexus hint
     * @param instantiationStrategy The instantiation strategy
     * @param description The component description
     * @param implementation The implementation
     * @return The implementation the component was successfully registered with; otherwise {@code null}
     */
    String addComponent( final String role, final String hint, final String instantiationStrategy,
                         final String description, final String implementation )
    {
        final Class<?> clazz = loadRole( role, implementation );
        if ( null == clazz )
        {
            return null;
        }

        final String canonicalHint = Hints.canonicalHint( hint );
        final String key = Roles.canonicalRoleHint( role, canonicalHint );

        /*
         * COMPONENT...
         */
        final Component oldComponent = components.get( key );
        if ( null == oldComponent )
        {
            components.put( key, new ComponentImpl( clazz, canonicalHint, instantiationStrategy, description ) );
        }
        else if ( LOAD_ON_START_PLACEHOLDER == oldComponent )
        {
            components.put( key, new ComponentImpl( clazz, canonicalHint, Strategies.LOAD_ON_START, description ) );
        }

        /*
         * ...IMPLEMENTATION
         */
        final DeferredClass<?> oldImplementation = implementations.get( key );
        if ( null == oldImplementation )
        {
            final DeferredClass<?> newImplementation;
            if ( deferredNames.add( implementation ) )
            {
                newImplementation = space.deferLoadClass( implementation );
            }
            else
            {
                newImplementation = getDisambiguatedClassSpace().deferLoadClass( implementation );
            }
            implementations.put( key, newImplementation );
            return newImplementation.getName();
        }
        else if ( oldImplementation.getName().equals( implementation ) )
        {
            return implementation; // merge configuration
        }

        debug( "Duplicate implementations found for Plexus component " + key );
        debug( "Saw: " + oldImplementation.getName() + " and: " + implementation );

        return null;
    }

    /**
     * @return Plexus component map
     */
    Map<Component, DeferredClass<?>> getComponents()
    {
        final Map<Component, DeferredClass<?>> map = new HashMap<Component, DeferredClass<?>>();
        for ( final Entry<String, DeferredClass<?>> i : implementations.entrySet() )
        {
            map.put( components.get( i.getKey() ), i.getValue() );
        }
        return map;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Attempts to load the given Plexus role, checks constructors for concrete types.
     * 
     * @param role The Plexus role
     * @param implementation The implementation
     * @return Loaded Plexus role
     */
    private Class<?> loadRole( final String role, final String implementation )
    {
        try
        {
            final Class<?> clazz = space.loadClass( role );
            if ( implementation.equals( role ) )
            {
                // check constructors will load
                clazz.getDeclaredConstructors();
            }
            return clazz;
        }
        catch ( final Throwable e )
        {
            // not all roles are needed, so just note any we can't load
            debug( "Ignoring Plexus role: " + role + " [" + e + "]" );
            return null;
        }
    }

    /**
     * @return Disambiguated class space
     */
    private ClassSpace getDisambiguatedClassSpace()
    {
        if ( null == disambiguatedSpace )
        {
            disambiguatedSpace = AccessController.doPrivileged( new PrivilegedAction<DisambiguatedClassSpace>()
            {
                public DisambiguatedClassSpace run()
                {
                    return new DisambiguatedClassSpace( space );
                }
            } );
        }
        return disambiguatedSpace;
    }

    /**
     * Logs the given debug message to the SLF4J logger if available; otherwise to JUL.
     * 
     * @param message The debug message
     */
    private static void debug( final String message )
    {
        try
        {
            org.slf4j.LoggerFactory.getLogger( PlexusComponentRegistry.class ).debug( message );
        }
        catch ( final Throwable ignore )
        {
            Logger.getLogger( PlexusComponentRegistry.class.getName() ).fine( message );
        }
    }
}
