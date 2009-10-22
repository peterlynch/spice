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
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * {@link Iterable} that iterates over declared members of a class hierarchy.
 */
public final class ClassMembers
    implements Iterable<Member>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Class<?> clazz;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Create an iterable view of members from the given class hierarchy.
     * 
     * @param clazz The leaf class
     */
    public ClassMembers( final Class<?> clazz )
    {
        this.clazz = clazz;
    }

    // ----------------------------------------------------------------------
    // Standard iterable behaviour
    // ----------------------------------------------------------------------

    public Iterator<Member> iterator()
    {
        return new MemberIterator( clazz );
    }

    // ----------------------------------------------------------------------
    // Helper classes
    // ----------------------------------------------------------------------

    /**
     * Useful base for all ready-only {@link Iterator}s.
     */
    private static abstract class ReadOnlyIterator<T>
        implements Iterator<T>
    {
        public ReadOnlyIterator()
        {
        }

        public final void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * {@link Iterator} that iterates over a cached array.
     */
    private static final class ArrayIterator<T>
        extends ReadOnlyIterator<T>
    {
        private final T[] items;

        private int i;

        ArrayIterator( final T[] items )
        {
            this.items = items;
        }

        public boolean hasNext()
        {
            return i < items.length;
        }

        public T next()
        {
            return items[i++]; // FindBugs IT_NO_SUCH_ELEMENT false-positive
        }
    }

    /**
     * Read-only {@link Iterator} that uses rolling {@link View}s to traverse the different members.
     */
    private static final class MemberIterator
        extends ReadOnlyIterator<Member>
    {
        private static final Iterator<Member> EMPTY_ITERATOR = new ArrayIterator<Member>( new Member[0] );

        private Class<?> clazz;

        private View view = View.VIEWS[0];

        private Iterator<Member> e = EMPTY_ITERATOR;

        MemberIterator( final Class<?> clazz )
        {
            this.clazz = clazz;
        }

        public boolean hasNext()
        {
            // check iterator cache
            while ( !e.hasNext() )
            {
                // stop when we reach the top (or the standard Java classes)
                if ( null == clazz || clazz.getName().startsWith( "java." ) )
                {
                    return false;
                }

                // reload cache
                e = new ArrayIterator<Member>( view.elements( clazz ) );

                // prepare next view
                view = view.next();
                if ( view == View.VIEWS[0] )
                {
                    clazz = clazz.getSuperclass(); // time to move onto the parent
                }
            }

            return true;
        }

        public Member next()
        {
            if ( hasNext() )
            {
                // cache is valid
                return e.next();
            }
            throw new NoSuchElementException();
        }
    }

    /**
     * {@link Enum} implementation that provides different views of a class's members.
     */
    private static enum View
    {
        // CONSTRUCTORS
        // {
        // @Override
        // final Member[] elements( final Class<?> clazz )
        // {
        // return clazz.getDeclaredConstructors();
        // }
        // },
        METHODS
        {
            @Override
            final Member[] elements( final Class<?> clazz )
            {
                return clazz.getDeclaredMethods();
            }
        },
        FIELDS
        {
            @Override
            final Member[] elements( final Class<?> clazz )
            {
                return clazz.getDeclaredFields();
            }
        };

        abstract Member[] elements( final Class<?> clazz );

        static final View[] VIEWS = View.values();

        final View next()
        {
            // keep cycling through the different elements
            return VIEWS[( ordinal() + 1 ) % VIEWS.length];
        }
    }
}
