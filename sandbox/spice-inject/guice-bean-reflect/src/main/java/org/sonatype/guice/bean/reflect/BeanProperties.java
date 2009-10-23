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
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Iterable<Member> members;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public BeanProperties( final Class<?> clazz )
    {
        this( new DeclaredMembers( clazz ) );
    }

    public BeanProperties( final Iterable<Member> members )
    {
        this.members = members;
    }

    // ----------------------------------------------------------------------
    // Standard iterable behaviour
    // ----------------------------------------------------------------------

    public Iterator<BeanProperty<?>> iterator()
    {
        return new BeanPropertyIterator();
    }

    // ----------------------------------------------------------------------
    // Helper classes
    // ----------------------------------------------------------------------

    /**
     * Read-only {@link Iterator} that picks out potential bean properties from members.
     */
    private class BeanPropertyIterator
        implements Iterator<BeanProperty<?>>
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Iterator<Member> i;

        private BeanProperty<?> nextProperty;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        BeanPropertyIterator()
        {
            i = members.iterator();
        }

        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------

        public boolean hasNext()
        {
            while ( i.hasNext() )
            {
                final Member member = i.next();
                final int modifiers = member.getModifiers();

                // statics can't be properties and synthetics are just noise
                if ( Modifier.isStatic( modifiers ) || member.isSynthetic() )
                {
                    continue;
                }

                // ignore any final fields, as they should not be updated
                if ( member instanceof Field && !Modifier.isFinal( modifiers ) )
                {
                    nextProperty = new BeanPropertyField<Object>( (Field) member );
                    return true;
                }

                // ignore zero/multi-argument methods, as they can't be setters
                if ( member instanceof Method && ( (Method) member ).getParameterTypes().length == 1 )
                {
                    nextProperty = new BeanPropertySetter<Object>( (Method) member );
                    return true;
                }
            }

            return false;
        }

        public BeanProperty<?> next()
        {
            if ( hasNext() )
            {
                return nextProperty; // initialized by hasNext()
            }
            throw new NoSuchElementException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
