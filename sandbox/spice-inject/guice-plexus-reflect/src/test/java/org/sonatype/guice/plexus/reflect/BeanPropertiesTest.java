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
package org.sonatype.guice.plexus.reflect;

import java.lang.reflect.Member;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

@SuppressWarnings( "unused" )
public class BeanPropertiesTest
    extends TestCase
{
    static interface A
    {
        String name = "";
    }

    static class B
    {
        static void setName( String name )
        {
        }
    }

    static class C
    {
        String name;
    }

    static interface D
    {
        void name( String name );
    }

    static class E
    {
        public E()
        {
        }

        private void setName( String name )
        {
        }
    }

    static class F
    {
        void setName()
        {
        }

        void setName( String firstName, String lastName )
        {
        }
    }

    static class G
    {
        void setName( String name )
        {
        }

        void setName()
        {
        }

        String name;

        void setName( String firstName, String lastName )
        {
        }

        void name( String _name )
        {
        }
    }

    static class H
    {
        List<String> names;

        void setMap( Map<BigDecimal, Float> map )
        {
        }
    }

    static abstract class IBase<T>
    {
        public abstract void setId( T id );
    }

    @SuppressWarnings( "synthetic-access" )
    static class I
        extends IBase<String>
    {
        private String id = "test";

        private static Internal internal = new Internal();

        static class Internal
        {
            private String m_id;
        }

        @Override
        public void setId( String _id )
        {
            internal.m_id = _id;
        }

        @Override
        public String toString()
        {
            return id + "@" + internal.m_id;
        }
    }

    public void testInterface()
    {
        for ( BeanProperty<?> bp : new BeanProperties( A.class ) )
        {
            fail( "Expected no bean properties" );
        }
    }

    public void testEmptyClass()
    {
        for ( BeanProperty<?> bp : new BeanProperties( B.class ) )
        {
            fail( "Expected no bean properties" );
        }
    }

    public void testPropertyField()
    {
        Iterator<BeanProperty<?>> i = new BeanProperties( C.class ).iterator();
        assertEquals( "name", i.next().getName() );
        assertFalse( i.hasNext() );
    }

    public void testPropertyParam()
    {
        Iterator<BeanProperty<?>> i = new BeanProperties( D.class ).iterator();
        assertEquals( "name", i.next().getName() );
        assertFalse( i.hasNext() );
    }

    public void testPropertySetter()
    {
        Iterator<BeanProperty<?>> i = new BeanProperties( E.class ).iterator();
        assertEquals( "name", i.next().getName() );
        assertFalse( i.hasNext() );
    }

    public void testSkipInvalidSetters()
    {
        for ( BeanProperty<?> bp : new BeanProperties( F.class ) )
        {
            fail( "Expected no bean properties" );
        }
    }

    public void testPropertyCombination()
    {
        Iterator<BeanProperty<?>> i = new BeanProperties( G.class ).iterator();
        assertEquals( "name", i.next().getName() );
        assertEquals( "name", i.next().getName() );
        assertEquals( "name", i.next().getName() );
        assertFalse( i.hasNext() );

        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( NoSuchElementException e )
        {
        }

        try
        {
            i.remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException e )
        {
        }
    }

    public void testConstructor()
        throws NoSuchMethodException
    {
        Iterable<Member> members = Collections.singleton( (Member) String.class.getConstructor() );
        Iterator<BeanProperty<?>> i = new BeanProperties( members ).iterator();
        assertFalse( i.hasNext() );
    }

    public void testPropertyType()
    {
        Iterator<BeanProperty<?>> i = new BeanProperties( H.class ).iterator();
        assertEquals( TypeLiteral.get( Types.mapOf( BigDecimal.class, Float.class ) ), i.next().getType() );
        assertEquals( TypeLiteral.get( Types.listOf( String.class ) ), i.next().getType() );
    }

    @SuppressWarnings( "unchecked" )
    public void testPropertyUpdate()
    {
        Iterator<BeanProperty> i = (Iterator) new BeanProperties( I.class ).iterator();
        BeanProperty<String> a = i.next();
        BeanProperty<String> b = i.next();
        BeanProperty<String> c = i.next();
        assertFalse( i.hasNext() );

        I component = new I();

        a.set( component, "---" );
        b.set( component, "foo" );
        c.set( component, "bar" );

        assertEquals( "foo@bar", component.toString() );

        c.set( component, "---" );
        b.set( component, "abc" );
        a.set( component, "xyz" );

        assertEquals( "abc@xyz", component.toString() );
    }

    public void testIllegalAccess()
    {
        try
        {
            BeanProperty<Object> p = new BeanPropertyField<Object>( A.class.getDeclaredField( "name" ) );
            p.set( new Object(), "test" );
            fail( "Expected ProvisionException" );
        }
        catch ( NoSuchFieldException e )
        {
            fail( "Expected ProvisionException" );
        }
        catch ( ProvisionException e )
        {
        }

        try
        {
            BeanProperty<Object> p = new BeanPropertySetter<Object>( D.class.getDeclaredMethod( "name", String.class ) );
            p.set( new Object(), "test" );
            fail( "Expected ProvisionException" );
        }
        catch ( NoSuchMethodException e )
        {
            fail( "Expected ProvisionException" );
        }
        catch ( ProvisionException e )
        {
        }
    }
}
