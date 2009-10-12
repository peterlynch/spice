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

import org.sonatype.guice.plexus.injector.PropertyBinding;

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
        checkInjectableProperty( new InjectableFieldProperty( field ) );
    }

    public void testInjectableParamProperty()
        throws NoSuchMethodException, IllegalAccessException, NoSuchFieldException
    {
        final Method method = Example.class.getDeclaredMethod( "setAnExampleProperty", List.class );
        checkInjectableProperty( new InjectableParamProperty( method ) );
    }

    private void checkInjectableProperty( final InjectableProperty ip )
        throws IllegalAccessException, NoSuchFieldException
    {
        assertEquals( List.class, ip.getType().getRawType() );
        assertEquals( Types.newParameterizedType( List.class, String.class ), ip.getType().getType() );

        assertEquals( "org.sonatype.guice.plexus.binders.example.anexampleproperty", ip.getName().toLowerCase() );

        final Field providerField = ip.getClass().getDeclaredField( "provider" );
        providerField.setAccessible( true );

        try
        {
            // bypass bind() to get access exception
            providerField.set( ip, Providers.of( null ) );
            ( (PropertyBinding) ip ).apply( new Example() );
            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
        }

        // now check normally with bind()
        final Example example = new Example();

        ip.bind( Providers.of( Arrays.asList( "This", "is", "a", "test" ) ) ).apply( example );
        assertEquals( "is", example.getAnExampleProperty().get( 1 ) );

        try
        {
            ip.bind( Providers.of( null ) ).apply( example );
            if ( ip instanceof InjectableParamProperty )
            {
                fail( "Expected ProvisionException" );
            }
        }
        catch ( final ProvisionException e )
        {
        }

        ip.bind( Providers.of( Collections.singletonList( "Hello" ) ) ).apply( example );
        assertEquals( "Hello", example.getAnExampleProperty().get( 0 ) );
    }

    public void testUnexpectedPropertyType()
        throws NoSuchMethodException, IllegalAccessException
    {
        final Method propertyFactoryMethod =
            PlexusPropertyBinder.class.getDeclaredMethod( "newInjectableProperty", AnnotatedElement.class );

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

    private void setAnExampleProperty( final List<String> value )
    {
        anExampleProperty = Collections.unmodifiableList( value );
    }

    public List<String> getAnExampleProperty()
    {
        return anExampleProperty;
    }
}
