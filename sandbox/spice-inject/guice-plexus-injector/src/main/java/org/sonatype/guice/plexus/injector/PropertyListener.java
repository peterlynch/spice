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

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * Listens for component types and uses a {@link PropertyBinder} to auto-bind property elements.
 */
public final class PropertyListener
    implements TypeListener
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final PropertyBinder binder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PropertyListener( final PropertyBinder binder )
    {
        this.binder = binder;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <T> void hear( final TypeLiteral<T> literal, final TypeEncounter<T> encounter )
    {
        final Collection<PropertyInjector> propertyInjectors = new ArrayList<PropertyInjector>();

        // iterate over declared members in class hierarchy: constructors > methods > fields
        for ( final AnnotatedElement element : new AnnotatedElements( literal.getRawType() ) )
        {
            final PropertyInjector injector = binder.bindProperty( encounter, element );
            if ( injector != null )
            {
                propertyInjectors.add( injector );
            }
        }

        if ( !propertyInjectors.isEmpty() )
        {
            encounter.register( new ComponentInjector<Object>( propertyInjectors ) );
        }
    }
}
