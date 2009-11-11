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

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.inject.BeanBinder;
import org.sonatype.guice.bean.inject.BeanListener;
import org.sonatype.guice.bean.inject.PropertyBinder;
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

    final PlexusBeanSource[] sources;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusBindingModule()
    {
        sources = new PlexusBeanSource[] { new AnnotatedBeanSource() };
    }

    public PlexusBindingModule( final PlexusBeanSource... sources )
    {
        this.sources = sources;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    protected void configure()
    {
        for ( final PlexusBeanSource source : sources )
        {
            for ( final Class<?> clazz : source.findBeanImplementations() )
            {
                bindPlexusBean( clazz, source.getBeanMetadata( clazz ).getComponent() );
            }
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
            sbb.asEagerSingleton();
        }
        else if ( !"per-lookup".equals( strategy ) )
        {
            sbb.in( Scopes.SINGLETON );
        }
    }

    final class PlexusBeanMatcher
        extends AbstractMatcher<TypeLiteral<?>>
    {
        public boolean matches( final TypeLiteral<?> type )
        {
            final Class<?> clazz = type.getRawType();
            for ( final PlexusBeanSource source : sources )
            {
                if ( source.getBeanMetadata( clazz ) != null )
                {
                    return true;
                }
            }
            return false;
        }
    }

    final class PlexusBeanBinder
        implements BeanBinder
    {
        public <B> PropertyBinder bindBean( final TypeLiteral<B> type, final TypeEncounter<B> encounter )
        {
            final Class<?> clazz = type.getRawType();
            for ( final PlexusBeanSource source : sources )
            {
                final PlexusBeanMetadata metadata = source.getBeanMetadata( clazz );
                if ( metadata != null )
                {
                    return new PlexusPropertyBinder( encounter, metadata );
                }
            }
            return null;
        }
    }
}
