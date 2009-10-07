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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.sonatype.guice.plexus.injector.PropertyInjector;

import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;

final class InjectableParamProperty
    implements InjectableProperty, PrivilegedAction<Void>, PropertyInjector
{
    private final Method method;

    private Provider<?> provider;

    InjectableParamProperty( final Method method )
    {
        if ( method.getParameterTypes().length != 1 )
        {
            throw new IllegalArgumentException( "Property \"" + method + "\" has wrong number of args" );
        }

        this.method = method;
    }

    public TypeLiteral<?> getType()
    {
        return TypeLiteral.get( method.getGenericParameterTypes()[0] );
    }

    public String getName()
    {
        return method.getDeclaringClass().getName() + '.' + method.getName();
    }

    public PropertyInjector bind( final Provider<?> toProvider )
    {
        if ( !method.isAccessible() )
        {
            AccessController.doPrivileged( this );
        }

        provider = toProvider;

        return this;
    }

    public Void run()
    {
        method.setAccessible( true );
        return null;
    }

    public void injectProperty( final Object component )
    {
        try
        {
            method.invoke( component, provider.get() );
        }
        catch ( final IllegalAccessException e )
        {
            throw new ProvisionException( e.toString() );
        }
        catch ( final InvocationTargetException e )
        {
            throw new ProvisionException( e.toString() );
        }
    }
}
