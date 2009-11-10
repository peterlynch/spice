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
package org.sonatype.guice.plexus.binders;

import static com.google.inject.name.Names.named;
import static org.sonatype.guice.plexus.config.Hints.getCanonicalHint;
import static org.sonatype.guice.plexus.config.Hints.isDefaultHint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.inject.BeanBinder;
import org.sonatype.guice.bean.inject.BeanListener;
import org.sonatype.guice.bean.inject.PropertyBinder;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * Guice {@link Module} that registers a custom {@link TypeListener} to auto-bind Plexus beans.
 */
public final class PlexusBindingModule
    extends AbstractModule
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Map<Class<?>, PlexusBeanMetadata> beanMap;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Create a new {@link Module} that binds Plexus beans using the given sources.
     * 
     * @param sources The bean sources
     */
    public PlexusBindingModule( final PlexusBeanSource... sources )
    {
        final Map<Class<?>, PlexusBeanMetadata> tempMap = new HashMap<Class<?>, PlexusBeanMetadata>();
        for ( final PlexusBeanSource source : sources )
        {
            for ( final Class<?> clazz : source.findBeanImplementations() )
            {
                tempMap.put( clazz, source.getBeanMetadata( clazz ) );
            }
        }

        beanMap = Collections.unmodifiableMap( tempMap );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    protected void configure()
    {
        for ( Entry<Class<?>, PlexusBeanMetadata> entry : beanMap.entrySet() )
        {
            bindPlexusBean( entry.getKey(), entry.getValue().getComponent() );
        }

        bindListener( new PlexusBeanMatcher(), new BeanListener( new PlexusBeanBinder() ) );
    }

    @SuppressWarnings( "unchecked" )
    private void bindPlexusBean( final Class<?> clazz, final Component component )
    {
        final Class<?> role = component.role();
        final String hint = getCanonicalHint( component.hint() );

        final ScopedBindingBuilder sbb;

        // bind role + optional hint -> implementation
        final AnnotatedBindingBuilder abb = bind( role );
        if ( !isDefaultHint( hint ) )
        {
            sbb = abb.annotatedWith( named( hint ) ).to( clazz );
        }
        else
        {
            sbb = clazz.equals( role ) ? abb : abb.to( clazz );
        }

        final String strategy = component.instantiationStrategy();
        if ( "load-on-start".equals( strategy ) )
        {
            // NOPMD TODO: rework startup beans
        }
        else if ( !"per-lookup".equals( strategy ) )
        {
            sbb.in( Scopes.SINGLETON );
        }
    }

    final class PlexusBeanMatcher
        extends AbstractMatcher<TypeLiteral<?>>
    {
        public boolean matches( TypeLiteral<?> type )
        {
            final Class<?> clazz = type.getRawType();

            return beanMap.containsKey( clazz ) || clazz.isAnnotationPresent( Component.class );
        }
    }

    final class PlexusBeanBinder
        implements BeanBinder
    {
        public <B> PropertyBinder bindBean( TypeLiteral<B> type, TypeEncounter<B> encounter )
        {
            final Class<?> clazz = type.getRawType();

            final PlexusBeanMetadata metadata = beanMap.get( clazz );
            if ( metadata != null )
            {
                return new PlexusPropertyBinder( encounter, metadata );
            }

            return new PlexusPropertyBinder( encounter, new JitMetadata( clazz ) );
        }
    }

    private static final class JitMetadata
        implements PlexusBeanMetadata
    {
        private final Component component;

        JitMetadata( Class<?> clazz )
        {
            component = clazz.getAnnotation( Component.class );
        }

        public Component getComponent()
        {
            return component;
        }

        public Configuration getConfiguration( BeanProperty<?> property )
        {
            return property.getAnnotation( Configuration.class );
        }

        public Requirement getRequirement( BeanProperty<?> property )
        {
            return property.getAnnotation( Requirement.class );
        }
    }
}
