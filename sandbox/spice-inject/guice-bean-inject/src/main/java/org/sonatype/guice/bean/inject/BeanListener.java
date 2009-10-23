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
package org.sonatype.guice.bean.inject;

import static org.sonatype.guice.bean.inject.BeanPropertyBinder.LAST_BINDING;

import java.util.ArrayList;
import java.util.Collection;

import org.sonatype.guice.bean.reflect.BeanProperties;
import org.sonatype.guice.bean.reflect.BeanProperty;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * {@link TypeListener} that listens for bean types and arranges for their properties to be injected.
 */
public final class BeanListener
    implements TypeListener
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanBinder beanBinder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public BeanListener( final BeanBinder beanBinder )
    {
        this.beanBinder = beanBinder;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <T> void hear( final TypeLiteral<T> type, final TypeEncounter<T> encounter )
    {
        final BeanPropertyBinder propertyBinder = beanBinder.bindBean( encounter, type );
        if ( null == propertyBinder )
        {
            return; // no properties to bind
        }

        final Collection<BeanPropertyBinding> bindings = new ArrayList<BeanPropertyBinding>();
        for ( final BeanProperty<Object> property : new BeanProperties( type.getRawType() ) )
        {
            try
            {
                final BeanPropertyBinding binding = propertyBinder.bindProperty( property );
                if ( binding == LAST_BINDING )
                {
                    break; // no more bindings
                }
                if ( binding != null )
                {
                    bindings.add( binding );
                }
            }
            catch ( final RuntimeException e )
            {
                encounter.addError( e );
            }
        }

        if ( !bindings.isEmpty() )
        {
            encounter.register( new BeanInjector<Object>( bindings ) );
        }
    }
}
