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

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.sonatype.guice.bean.injector.BeanPropertyBinding;

import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;

/**
 * {@link InjectableProperty} backed by a {@link Field}.
 */
final class InjectableFieldProperty<T>
    implements InjectableProperty<T>, PrivilegedAction<Void>, BeanPropertyBinding
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Field field;

    private Provider<T> provider;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    InjectableFieldProperty( final Field field )
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

    public BeanPropertyBinding bind( final Provider<T> toProvider )
    {
        provider = toProvider;

        if ( !field.isAccessible() )
        {
            // make sure we can apply the binding
            AccessController.doPrivileged( this );
        }

        return this; // save on object creation
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

    // ----------------------------------------------------------------------
    // PropertyBinding methods
    // ----------------------------------------------------------------------

    public void injectProperty( final Object component )
    {
        try
        {
            field.set( component, provider.get() );
        }
        catch ( final IllegalAccessException e )
        {
            throw new ProvisionException( e.toString() );
        }
    }
}
