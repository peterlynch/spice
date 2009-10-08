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
import java.lang.reflect.Member;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;

public class PropertyListenerTest
    extends TestCase
{
    static class Component
    {
        String a;

        String b;

        String c;
    }

    public void testPropertyListener()
    {
        final Component component = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bindListener( Matchers.any(), new PropertyListener( new PropertyBinder()
                {
                    public PropertyBinding bindProperty( final TypeEncounter<?> encounter,
                                                          final AnnotatedElement element )
                    {
                        if ( Component.class == ( (Member) element ).getDeclaringClass() && element instanceof Field )
                        {
                            return new PropertyBinding()
                            {
                                public void apply( final Object o )
                                {
                                    final Field f = (Field) element;
                                    try
                                    {
                                        f.set( o, f.getName() );
                                    }
                                    catch ( final IllegalAccessException e )
                                    {
                                        throw new RuntimeException( e );
                                    }
                                }
                            };
                        }
                        return null;
                    }
                } ) );
            }
        } ).getInstance( Component.class );

        assertEquals( "a", component.a );
        assertEquals( "b", component.b );
        assertEquals( "c", component.c );
    }
}
