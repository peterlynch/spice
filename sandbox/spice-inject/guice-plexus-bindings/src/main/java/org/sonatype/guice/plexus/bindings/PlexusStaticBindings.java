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
package org.sonatype.guice.plexus.bindings;

import static com.google.inject.name.Names.named;
import static org.sonatype.guice.plexus.config.Hints.getCanonicalHint;
import static org.sonatype.guice.plexus.config.Hints.isDefaultHint;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.plexus.config.PlexusBeanSource;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Named;

/**
 * Guice {@link Module} that converts Plexus components into {@link Named} bindings.
 */
public final class PlexusStaticBindings
    extends AbstractModule
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final PlexusBeanSource beanSource;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusStaticBindings( final PlexusBeanSource beanSource )
    {
        this.beanSource = beanSource;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    @SuppressWarnings( "unchecked" )
    protected void configure()
    {
        for ( final Class<?> clazz : beanSource.findPlexusBeans() )
        {
            final Component spec = beanSource.getBeanMetadata( clazz ).getComponent();

            final Class<?> role = spec.role();
            final String hint = getCanonicalHint( spec.hint() );

            // bind role + optional hint -> implementation
            final AnnotatedBindingBuilder abb = bind( role );
            final LinkedBindingBuilder lbb = isDefaultHint( hint ) ? abb : abb.annotatedWith( named( hint ) );
            final ScopedBindingBuilder sbb = clazz.equals( role ) ? lbb : lbb.to( clazz );

            if ( "load-on-start".equals( spec.instantiationStrategy() ) )
            {
                startingKeys.add( isDefaultHint( hint ) ? Key.get( role ) : Key.get( role, named( hint ) ) );
            }
            else if ( !"per-lookup".equals( spec.instantiationStrategy() ) )
            {
                sbb.in( Scopes.SINGLETON );
            }
        }

        requestInjection( this );
    }

    private final List<Key<?>> startingKeys = new ArrayList<Key<?>>();

    @Inject
    public void start( final Injector injector )
    {
        for ( Key<?> key : startingKeys )
        {
            try
            {
                Object instance = injector.getInstance( key );
                Method start = instance.getClass().getMethod( "start" );
                start.invoke( instance );
            }
            catch ( Exception e )
            {
            }
        }
    }
}
