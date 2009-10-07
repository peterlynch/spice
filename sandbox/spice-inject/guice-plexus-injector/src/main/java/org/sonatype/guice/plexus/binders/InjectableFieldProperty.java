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

import org.sonatype.guice.plexus.injector.PropertyInjector;

import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;

final class InjectableFieldProperty
    implements InjectableProperty, PrivilegedAction<Void>, PropertyInjector
{
    private final Field field;

    private Provider<?> provider;

    InjectableFieldProperty( final Field field )
    {
        this.field = field;
    }

    public TypeLiteral<?> getType()
    {
        return TypeLiteral.get( field.getGenericType() );
    }

    public String getName()
    {
        return field.getDeclaringClass().getName() + '.' + field.getName();
    }

    public PropertyInjector bind( final Provider<?> toProvider )
    {
        if ( !field.isAccessible() )
        {
            AccessController.doPrivileged( this );
        }

        provider = toProvider;

        return this;
    }

    public Void run()
    {
        field.setAccessible( true );
        return null;
    }

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
