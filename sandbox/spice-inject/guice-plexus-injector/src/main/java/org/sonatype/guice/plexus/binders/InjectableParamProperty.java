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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.guice.bean.injector.BeanPropertyBinding;

import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;

/**
 * {@link InjectableProperty} backed by a single-parameter setter {@link Method}.
 */
final class InjectableParamProperty<T>
    implements InjectableProperty<T>, PrivilegedAction<Void>, BeanPropertyBinding
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Pattern SETTER_PATTERN = Pattern.compile( "^set(\\p{javaUpperCase})(.*)" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Method method;

    private Provider<T> provider;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    InjectableParamProperty( final Method method )
    {
        if ( method.getParameterTypes().length != 1 )
        {
            throw new IllegalArgumentException( "Property \"" + method + "\" has wrong number of args" );
        }

        this.method = method;
    }

    // ----------------------------------------------------------------------
    // InjectableProperty methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public TypeLiteral<T> getType()
    {
        return (TypeLiteral) TypeLiteral.get( method.getGenericParameterTypes()[0] );
    }

    public String getName()
    {
        final String name = method.getName();

        final Matcher matcher = SETTER_PATTERN.matcher( name );
        if ( matcher.matches() )
        {
            return matcher.group( 1 ).toLowerCase() + matcher.group( 2 );
        }

        return name;
    }

    public BeanPropertyBinding bind( final Provider<T> toProvider )
    {
        provider = toProvider;

        if ( !method.isAccessible() )
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
        method.setAccessible( true );
        return null;
    }

    // ----------------------------------------------------------------------
    // PropertyBinding methods
    // ----------------------------------------------------------------------

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
