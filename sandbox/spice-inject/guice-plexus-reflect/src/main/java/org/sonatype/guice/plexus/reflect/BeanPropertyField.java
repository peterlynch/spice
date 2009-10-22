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
package org.sonatype.guice.plexus.reflect;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;

/**
 * {@link BeanProperty} backed by a {@link Field}.
 */
public final class BeanPropertyField<T>
    implements BeanProperty<T>, PrivilegedAction<Void>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Field field;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public BeanPropertyField( final Field field )
    {
        this.field = field;
    }

    // ----------------------------------------------------------------------
    // InjectableProperty methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public TypeLiteral<T> getType()
    {
        return (TypeLiteral) TypeLiteral.get( field.getGenericType() );
    }

    public String getName()
    {
        return field.getName();
    }

    public void set( final Object component, final T value )
    {
        if ( !field.isAccessible() )
        {
            // make sure we can apply the binding
            AccessController.doPrivileged( this );
        }

        try
        {
            field.set( component, value );
        }
        catch ( final Exception e )
        {
            throw new ProvisionException( "Error updating bean field: " + field + " reason: " + e );
        }
    }

    // ----------------------------------------------------------------------
    // PrivilegedAction methods
    // ----------------------------------------------------------------------

    public Void run()
    {
        // enable private injection
        field.setAccessible( true );
        return null;
    }
}
