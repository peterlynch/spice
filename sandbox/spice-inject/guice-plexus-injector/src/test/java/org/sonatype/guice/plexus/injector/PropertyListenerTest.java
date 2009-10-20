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
import java.lang.reflect.Field;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;

public class PropertyListenerTest
    extends TestCase
{
    static class Base
    {
        // empty base class
    }

    static class Component
        extends Base
    {
        String a;

        String b;

        String c;

        String last;
    }

    static class NamedPropertyBinder
        implements PropertyBinder
    {
        public PropertyBinding bindProperty( final AnnotatedElement element )
        {
            if ( element instanceof Field )
            {
                final Field f = (Field) element;
                final String name = f.getName();

                return "last".equals( name ) ? PropertyBinder.LAST_BINDING : new PropertyBinding()
                {
                    public void injectProperty( final Object o )
                    {
                        try
                        {
                            f.set( o, f.getName() + "Value" );
                        }
                        catch ( final IllegalAccessException e )
                        {
                        }
                    }
                };
            }

            return null;
        }
    }

    public void testPropertyListener()
    {
        final Injector injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bindListener( Matchers.any(), new ComponentListener( new ComponentBinder()
                {
                    public <T> PropertyBinder bindComponent( final TypeEncounter<T> encounter, final TypeLiteral<T> type )
                    {
                        return Base.class.isAssignableFrom( type.getRawType() ) ? new NamedPropertyBinder() : null;
                    }
                } ) );
            }
        } );

        injector.getInstance( Base.class );

        final Component component = injector.getInstance( Component.class );

        assertEquals( "aValue", component.a );
        assertEquals( "bValue", component.b );
        assertEquals( "cValue", component.c );

        assertNull( component.last );
        PropertyBinder.LAST_BINDING.injectProperty( component );
        assertNull( component.last );
    }
}
