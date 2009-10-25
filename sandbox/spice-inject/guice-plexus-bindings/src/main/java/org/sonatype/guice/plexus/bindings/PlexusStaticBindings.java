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

import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.component.annotations.Component;

import com.google.inject.AbstractModule;
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

    private final Map<Class<?>, Component> componentMap;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusStaticBindings( final Map<Class<?>, Component> componentMap )
    {
        this.componentMap = componentMap;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    @SuppressWarnings( "unchecked" )
    protected void configure()
    {
        for ( final Entry<Class<?>, Component> e : componentMap.entrySet() )
        {
            final Class<?> clazz = e.getKey();
            final Component spec = e.getValue();

            // bind role + optional hint -> implementation
            final AnnotatedBindingBuilder abb = bind( spec.role() );
            final String hint = getCanonicalHint( spec.hint() );
            final LinkedBindingBuilder lbb = isDefaultHint( hint ) ? abb : abb.annotatedWith( named( hint ) );
            final ScopedBindingBuilder sbb = lbb.to( clazz );

            // assume anything other than "per-lookup" is a singleton
            if ( !"per-lookup".equals( spec.instantiationStrategy() ) )
            {
                sbb.in( Scopes.SINGLETON );
            }
        }
    }
}
