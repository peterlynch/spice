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
package org.sonatype.guice.plexus.injector;

import static org.sonatype.guice.plexus.injector.PropertyBinder.LAST_BINDING;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * {@link TypeListener} that listens for component types and arranges for their properties to be injected.
 */
public final class ComponentListener
    implements TypeListener
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ComponentBinder componentBinder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ComponentListener( final ComponentBinder componentBinder )
    {
        this.componentBinder = componentBinder;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <T> void hear( final TypeLiteral<T> type, final TypeEncounter<T> encounter )
    {
        final PropertyBinder propertyBinder = componentBinder.bindComponent( encounter, type );
        if ( null == propertyBinder )
        {
            return; // no properties to bind
        }

        final Collection<PropertyBinding> bindings = new ArrayList<PropertyBinding>();
        for ( final AnnotatedElement element : new AnnotatedElements( type.getRawType() ) )
        {
            final PropertyBinding binding = propertyBinder.bindProperty( element );
            if ( binding == LAST_BINDING )
            {
                break; // no more bindings
            }
            if ( binding != null )
            {
                bindings.add( binding );
            }
        }

        if ( !bindings.isEmpty() )
        {
            encounter.register( new ComponentInjector<Object>( bindings ) );
        }
    }
}
