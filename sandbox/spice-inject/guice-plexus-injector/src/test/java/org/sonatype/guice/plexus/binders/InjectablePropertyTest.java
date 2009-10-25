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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.sonatype.guice.bean.injector.BeanPropertyBinding;

import com.google.inject.ProvisionException;
import com.google.inject.util.Providers;
import com.google.inject.util.Types;

public class InjectablePropertyTest
    extends TestCase
{
    public void testInjectableFieldProperty()
        throws NoSuchFieldException, IllegalAccessException
    {
        final Field field = Example.class.getDeclaredField( "anExampleProperty" );
        checkInjectableProperty( new InjectableFieldProperty<List<String>>( field ) );
    }

    public void testInjectableParamProperty()
        throws NoSuchMethodException, IllegalAccessException, NoSuchFieldException
    {
        final Method method = Example.class.getDeclaredMethod( "anExampleProperty", List.class );
        checkInjectableProperty( new InjectableParamProperty<List<String>>( method ) );
    }

    public void testInjectableParamPropertySetter()
        throws NoSuchMethodException, IllegalAccessException, NoSuchFieldException
    {
        final Method method = Example.class.getDeclaredMethod( "setAnExampleProperty", List.class );
        checkInjectableProperty( new InjectableParamProperty<List<String>>( method ) );
    }

    private void checkInjectableProperty( final InjectableProperty<List<String>> ip )
        throws IllegalAccessException, NoSuchFieldException
    {
        assertEquals( List.class, ip.getType().getRawType() );
        assertEquals( Types.newParameterizedType( List.class, String.class ), ip.getType().getType() );

        assertEquals( "anExampleProperty", ip.getName() );

        final Field providerField = ip.getClass().getDeclaredField( "provider" );
        providerField.setAccessible( true );

        try
        {
            // bypass bind() to get access exception
            providerField.set( ip, Providers.of( null ) );
            ( (BeanPropertyBinding) ip ).injectProperty( new Example() );
            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
        }

        // now check normally with bind()
        final Example example = new Example();

        ip.bind( Providers.of( Arrays.asList( "This", "is", "a", "test" ) ) ).injectProperty( example );
        assertEquals( "is", example.getAnExampleProperty().get( 1 ) );

        try
        {
            ip.bind( Providers.of( (List<String>) null ) ).injectProperty( example );
            if ( ip instanceof InjectableParamProperty<?> )
            {
                fail( "Expected ProvisionException" );
            }
        }
        catch ( final ProvisionException e )
        {
        }

        ip.bind( Providers.of( Collections.singletonList( "Hello" ) ) ).injectProperty( example );
        assertEquals( "Hello", example.getAnExampleProperty().get( 0 ) );
    }

    public void testUnexpectedPropertyType()
        throws NoSuchMethodException, IllegalAccessException
    {
        final Method propertyFactoryMethod =
            PlexusComponentBinder.class.getDeclaredMethod( "newInjectableProperty", AnnotatedElement.class );

        try
        {
            propertyFactoryMethod.setAccessible( true );
            propertyFactoryMethod.invoke( null, Example.class.getDeclaredConstructor() );
            fail( "Expected InvocationTargetException" );
        }
        catch ( final InvocationTargetException e )
        {
        }
    }
}

@SuppressWarnings( "unused" )
class Example
{
    private List<String> anExampleProperty;

    private void anExampleProperty( final List<String> value )
    {
        anExampleProperty = Collections.unmodifiableList( value );
    }

    private void setAnExampleProperty( final List<String> value )
    {
        anExampleProperty = Collections.unmodifiableList( value );
    }

    public List<String> getAnExampleProperty()
    {
        return anExampleProperty;
    }
}
