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

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * {@link Iterable} that iterates over potential bean properties in a class hierarchy.
 */
public final class BeanProperties
    implements Iterable<BeanProperty<?>>
{
    final Iterable<Member> members;

    public BeanProperties( final Class<?> clazz )
    {
        this( new ClassMembers( clazz ) );
    }

    public BeanProperties( final Iterable<Member> members )
    {
        this.members = members;
    }

    public Iterator<BeanProperty<?>> iterator()
    {
        return new BeanPropertyIterator();
    }

    private class BeanPropertyIterator
        implements Iterator<BeanProperty<?>>
    {
        private final Iterator<Member> i;

        private BeanProperty<?> nextProperty;

        public BeanPropertyIterator()
        {
            i = members.iterator();
        }

        public boolean hasNext()
        {
            while ( i.hasNext() )
            {
                final Member m = i.next();
                if ( Modifier.isStatic( m.getModifiers() ) || m.isSynthetic() )
                {
                    continue;
                }
                if ( m instanceof Field )
                {
                    nextProperty = new BeanPropertyField<Object>( (Field) m );
                    return true;
                }
                else if ( m instanceof Method && ( (Method) m ).getParameterTypes().length == 1 )
                {
                    nextProperty = new BeanPropertySetter<Object>( (Method) m );
                    return true;
                }
            }
            return false;
        }

        public BeanProperty<?> next()
        {
            if ( hasNext() )
            {
                return nextProperty;
            }
            throw new NoSuchElementException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
