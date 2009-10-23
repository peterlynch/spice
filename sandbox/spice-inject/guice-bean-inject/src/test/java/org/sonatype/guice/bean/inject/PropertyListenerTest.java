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
package org.sonatype.guice.bean.inject;

import junit.framework.TestCase;

import org.sonatype.guice.bean.reflect.BeanProperty;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.TypeEncounter;

public class PropertyListenerTest
    extends TestCase
{
    static class Base
    {
        // empty base class
    }

    static class Bean
        extends Base
    {
        String a;

        String b;

        String c;

        String last;
    }

    static class NamedPropertyBinder
        implements BeanPropertyBinder
    {
        public <T> BeanPropertyBinding bindProperty( final BeanProperty<T> property )
        {
            return "last".equals( property.getName() ) ? BeanPropertyBinder.LAST_BINDING : new BeanPropertyBinding()
            {
                @SuppressWarnings( "unchecked" )
                public void injectProperty( final Object bean )
                {
                    property.set( bean, (T) ( property.getName() + "Value" ) );
                }
            };
        }
    }

    public void testPropertyListener()
    {
        final BeanPropertyBinder namedPropertyBinder = new NamedPropertyBinder();
        final Injector injector = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bindListener( new AbstractMatcher<TypeLiteral<?>>()
                {
                    public boolean matches( TypeLiteral<?> type )
                    {
                        return Base.class.isAssignableFrom( type.getRawType() );
                    }
                }, new BeanListener( new BeanBinder()
                {
                    public <T> BeanPropertyBinder bindBean( final TypeEncounter<T> encounter, final TypeLiteral<T> type )
                    {
                        return namedPropertyBinder;
                    }
                } ) );
            }
        } );

        injector.getInstance( Base.class );

        final Bean bean = injector.getInstance( Bean.class );

        assertEquals( "aValue", bean.a );
        assertEquals( "bValue", bean.b );
        assertEquals( "cValue", bean.c );

        assertNull( bean.last );
        BeanPropertyBinder.LAST_BINDING.injectProperty( bean );
        assertNull( bean.last );
    }
}
