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
package org.sonatype.guice.bean.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.TypeLiteral;

/**
 * {@link BeanProperty} backed by a single-parameter setter {@link Method}.
 */
public final class BeanPropertySetter<T>
    implements BeanProperty<T>, PrivilegedAction<Void>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Pattern SETTER_PATTERN = Pattern.compile( "^set(\\p{javaUpperCase})(.*)" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Method method;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public BeanPropertySetter( final Method method )
    {
        this.method = method;
    }

    // ----------------------------------------------------------------------
    // InjectableProperty methods
    // ----------------------------------------------------------------------

    public <A extends Annotation> A getAnnotation( final Class<A> annotationType )
    {
        return method.getAnnotation( annotationType );
    }

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
            return Character.toLowerCase( matcher.group( 1 ).charAt( 0 ) ) + matcher.group( 2 );
        }

        return name;
    }

    public <B> void set( final B bean, final T value )
    {
        if ( !method.isAccessible() )
        {
            // ensure we can update the property
            AccessController.doPrivileged( this );
        }

        try
        {
            method.invoke( bean, value );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( "Error calling bean setter: " + method + " reason: " + e );
        }
    }

    // ----------------------------------------------------------------------
    // PrivilegedAction methods
    // ----------------------------------------------------------------------

    public Void run()
    {
        method.setAccessible( true );
        return null;
    }
}
