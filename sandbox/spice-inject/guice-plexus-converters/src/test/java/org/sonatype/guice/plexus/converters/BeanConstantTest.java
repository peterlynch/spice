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
package org.sonatype.guice.plexus.converters;

import static com.google.inject.name.Names.named;
import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;

public class BeanConstantTest
    extends TestCase
{
    @Override
    protected void setUp()
        throws Exception
    {
        Guice.createInjector( new AbstractModule()
        {
            private void bindBean( final String name, final String clazzName, final String content )
            {
                final String xml = "<bean implementation='" + clazzName + "'>" + content + "</bean>";
                bindConstant().annotatedWith( named( name ) ).to( xml );
            }

            @Override
            protected void configure()
            {
                bindBean( "EmptyBean", EmptyBean.class.getName(), "" );
                bindBean( "MissingType", "some.unknown.type", "" );

                bindBean( "BeanWithProperty", BeanWithProperty.class.getName(), "<value>4.2</value>" );
                bindBean( "MissingProperty", EmptyBean.class.getName(), "<value>4.2</value>" );

                bindBean( "MissingDefaultConstructor", MissingDefaultConstructor.class.getName(), "" );
                bindBean( "BrokenDefaultConstructor", BrokenDefaultConstructor.class.getName(), "" );
                bindBean( "MissingStringConstructor", MissingStringConstructor.class.getName(), "text" );
                bindBean( "BrokenStringConstructor", BrokenStringConstructor.class.getName(), "text" );

                install( new XmlTypeConverter() );
            }
        } ).injectMembers( this );
    }

    static class EmptyBean
    {
    }

    static class BeanWithProperty
    {
        float value;

        void setValue( float _value )
        {
            value = _value;
        }
    }

    static class MissingDefaultConstructor
    {
        private MissingDefaultConstructor()
        {
        }
    }

    static class BrokenDefaultConstructor
    {
        public BrokenDefaultConstructor()
        {
            throw new RuntimeException();
        }
    }

    static class MissingStringConstructor
    {
    }

    static class BrokenStringConstructor
    {
        @SuppressWarnings( "unused" )
        public BrokenStringConstructor( final String text )
        {
            throw new RuntimeException();
        }
    }

    @Inject
    Injector injector;

    public void testEmptyBeanConversion()
    {
        assertEquals( EmptyBean.class, getBean( "EmptyBean", Object.class ).getClass() );
    }

    public void testMissingType()
    {
        testFailedConversion( "MissingType", EmptyBean.class );
    }

    public void testPeerClassLoader1()
    {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( null );
            assertEquals( EmptyBean.class, getBean( "EmptyBean", EmptyBean.class ).getClass() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( tccl );
        }
    }

    public void testPeerClassLoader2()
    {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( null );
            assertEquals( EmptyBean.class, getBean( "EmptyBean", Object.class ).getClass() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( tccl );
        }
    }

    public void testBeanWithPropertyConversion()
    {
        assertEquals( 4.2f, ( (BeanWithProperty) getBean( "BeanWithProperty", Object.class ) ).value, 0f );
    }

    public void testMissingPropertyConversion()
    {
        testFailedConversion( "MissingProperty", Object.class );
    }

    public void testMissingDefaultConstructor()
    {
        testFailedConversion( "MissingDefaultConstructor", Object.class );
    }

    public void testBrokenDefaultConstructor()
    {
        testFailedConversion( "BrokenDefaultConstructor", Object.class );
    }

    public void testMissingStringConstructor()
    {
        testFailedConversion( "MissingStringConstructor", Object.class );
    }

    public void testBrokenStringConstructor()
    {
        testFailedConversion( "BrokenStringConstructor", Object.class );
    }

    private Object getBean( final String bindingName, final Class<?> clazz )
    {
        return injector.getInstance( Key.get( clazz, named( bindingName ) ) );
    }

    private void testFailedConversion( final String bindingName, final Class<?> clazz )
    {
        try
        {
            getBean( bindingName, clazz );
            fail( "Expected ConfigurationException" );
        }
        catch ( ConfigurationException e )
        {
            System.out.println( e.toString() );
        }
    }
}
