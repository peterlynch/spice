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

import java.lang.reflect.Member;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.inject.Singleton;

import junit.framework.TestCase;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
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
        static void setName( final String name )
        {
        }
    }

    static class C
    {
        final String id = "name";

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

        private void setName( final String name )
        {
        }
    }

    static class F
    {
        void setName()
        {
        }

        void setName( final String firstName, final String lastName )
        {
        }
    }

    static class G
    {
        void setName( final String name )
        {
        }

        void setName()
        {
        }

        String name;

        void setName( final String firstName, final String lastName )
        {
        }

        void name( final String _name )
        {
        }
    }

    static class H
    {
        List<String> names;

        void setMap( final Map<BigDecimal, Float> map )
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
        private volatile String id = "test";

        private static Internal internal = new Internal();

        static class Internal
        {
            private String m_id;
        }

        @Override
        public void setId( final String _id )
        {
            internal.m_id = _id;
        }

        @Override
        public String toString()
        {
            return id + "@" + internal.m_id;
        }
    }

    static class J
    {
        @Singleton
        @Named( "bar" )
        String name;

        @Singleton
        @Named( "foo" )
        void setName( final String name )
        {
        }
    }

    static class K
    {
        String a;

        String b;

        String c;
    }

    static class L
    {
        void setName( final String name )
        {
            throw new RuntimeException();
        }
    }

    public void testInterface()
    {
        for ( final BeanProperty<?> bp : new BeanProperties( A.class ) )
        {
            fail( "Expected no bean properties" );
        }
    }

    public void testEmptyClass()
    {
        for ( final BeanProperty<?> bp : new BeanProperties( B.class ) )
        {
            fail( "Expected no bean properties" );
        }
    }

    public void testPropertyField()
    {
        final Iterator<BeanProperty<Object>> i = new BeanProperties( C.class ).iterator();
        assertEquals( "name", i.next().getName() );
        assertFalse( i.hasNext() );
    }

    public void testPropertyParam()
    {
        final Iterator<BeanProperty<Object>> i = new BeanProperties( D.class ).iterator();
        assertEquals( "name", i.next().getName() );
        assertFalse( i.hasNext() );
    }

    public void testPropertySetter()
    {
        final Iterator<BeanProperty<Object>> i = new BeanProperties( E.class ).iterator();
        assertEquals( "name", i.next().getName() );
        assertFalse( i.hasNext() );
    }

    public void testSkipInvalidSetters()
    {
        for ( final BeanProperty<?> bp : new BeanProperties( F.class ) )
        {
            fail( "Expected no bean properties" );
        }
    }

    public void testPropertyCombination()
    {
        final Iterator<BeanProperty<Object>> i = new BeanProperties( G.class ).iterator();
        assertEquals( "name", i.next().getName() );
        assertEquals( "name", i.next().getName() );
        assertEquals( "name", i.next().getName() );
        assertFalse( i.hasNext() );

        try
        {
            i.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( final NoSuchElementException e )
        {
        }

        try
        {
            i.remove();
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }
    }

    public void testConstructor()
        throws NoSuchMethodException
    {
        final Iterable<Member> members = Collections.singleton( (Member) String.class.getConstructor() );
        final Iterator<BeanProperty<Object>> i = new BeanProperties( members ).iterator();
        assertFalse( i.hasNext() );
    }

    public void testPropertyType()
    {
        final Iterator<BeanProperty<Object>> i = new BeanProperties( H.class ).iterator();
        assertEquals( TypeLiteral.get( Types.mapOf( BigDecimal.class, Float.class ) ), i.next().getType() );
        assertEquals( TypeLiteral.get( Types.listOf( String.class ) ), i.next().getType() );
    }

    public void testPropertyUpdate()
    {
        final Iterator<BeanProperty<Object>> i = new BeanProperties( I.class ).iterator();
        final BeanProperty<Object> a = i.next();
        final BeanProperty<Object> b = i.next();
        final BeanProperty<Object> c = i.next();
        assertFalse( i.hasNext() );

        final I component = new I();

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
            @SuppressWarnings( "unchecked" )
            final BeanProperty<Object> p = new BeanPropertyField( A.class.getDeclaredField( "name" ) );
            p.set( new Object(), "test" );
            fail( "Expected RuntimeException" );
        }
        catch ( final NoSuchFieldException e )
        {
            fail( "Expected RuntimeException" );
        }
        catch ( final RuntimeException e )
        {
        }

        try
        {
            @SuppressWarnings( "unchecked" )
            final BeanProperty<Object> p = new BeanPropertySetter( D.class.getDeclaredMethod( "name", String.class ) );
            p.set( new Object(), "test" );
            fail( "Expected RuntimeException" );
        }
        catch ( final NoSuchMethodException e )
        {
            fail( "Expected RuntimeException" );
        }
        catch ( final RuntimeException e )
        {
        }
    }

    public void testPropertyAnnotations()
    {
        final Iterator<BeanProperty<Object>> i = new BeanProperties( J.class ).iterator();
        assertEquals( "foo", i.next().getAnnotation( Named.class ).value() );
        assertEquals( "bar", i.next().getAnnotation( Named.class ).value() );
        assertFalse( i.hasNext() );
    }

    public void testPropertyIteration()
    {
        final Iterator<BeanProperty<Object>> i = new BeanProperties( K.class ).iterator();
        assertTrue( i.hasNext() );
        assertTrue( i.hasNext() );
        assertEquals( "a", i.next().getName() );
        assertTrue( i.hasNext() );
        assertTrue( i.hasNext() );
        assertEquals( "b", i.next().getName() );
        assertTrue( i.hasNext() );
        assertTrue( i.hasNext() );
        assertEquals( "c", i.next().getName() );
        assertFalse( i.hasNext() );
        assertFalse( i.hasNext() );
    }

    public void testBadPropertySetter()
    {
        try
        {
            final Iterator<BeanProperty<Object>> i = new BeanProperties( L.class ).iterator();
            i.next().set( new L(), "TEST" );
            fail( "Expected RuntimeException" );
        }
        catch ( final RuntimeException e )
        {
        }
    }
}
