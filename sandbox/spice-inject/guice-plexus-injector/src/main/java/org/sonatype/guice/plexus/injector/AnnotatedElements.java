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
import java.util.Iterator;
import java.util.NoSuchElementException;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * {@link Iterable} that supports iteration over all members of a class hierarchy: constructors > methods > fields.
 */
final class AnnotatedElements
    implements Iterable<AnnotatedElement>
{
    private final Class<?> clazz;

    /**
     * Create an iterable view of the given class hierarchy.
     * 
     * @param clazz the leaf class
     */
    AnnotatedElements( final Class<?> clazz )
    {
        this.clazz = clazz;
    }

    public Iterator<AnnotatedElement> iterator()
    {
        return new AnnotatedElementIterator( clazz );
    }

    /**
     * Read-only {@link Iterator} that uses rolling {@link View}s to traverse the different members.
     */
    private static final class AnnotatedElementIterator
        extends ReadOnlyIterator<AnnotatedElement>
    {
        private static final Iterator<AnnotatedElement> EMPTY_ITERATOR =
            new ArrayIterator<AnnotatedElement>( new AnnotatedElement[0] );

        private Class<?> clazz;

        // always begin with the constructors
        private View view = View.CONSTRUCTORS;

        private Iterator<AnnotatedElement> e = EMPTY_ITERATOR;

        AnnotatedElementIterator( final Class<?> clazz )
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

                // move onto the next set of members: constructors > methods > fields
                e = new ArrayIterator<AnnotatedElement>( view.elements( clazz ) );

                // prepare next view, keep rolling through the same three types
                view = view.next();
                if ( view == View.CONSTRUCTORS )
                {
                    clazz = clazz.getSuperclass(); // time to move onto the parent
                }
            }

            return true;
        }

        public AnnotatedElement next()
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
     * Useful base for all ready-only {@link Iterator}s.
     */
    static abstract class ReadOnlyIterator<T>
        implements Iterator<T>
    {
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

        @SuppressWarnings( "IT_NO_SUCH_ELEMENT" )
        public T next()
        {
            return items[i++];
        }
    }

    /**
     * {@link Enum} implementation that provides different views of a class's members (constructors, methods, fields).
     */
    private static enum View
    {
        CONSTRUCTORS
        {
            @Override
            final AnnotatedElement[] elements( final Class<?> clazz )
            {
                return clazz.getDeclaredConstructors();
            }
        },
        METHODS
        {
            @Override
            final AnnotatedElement[] elements( final Class<?> clazz )
            {
                return clazz.getDeclaredMethods();
            }
        },
        FIELDS
        {
            @Override
            final AnnotatedElement[] elements( final Class<?> clazz )
            {
                return clazz.getDeclaredFields();
            }
        };

        abstract AnnotatedElement[] elements( final Class<?> clazz );

        final View next()
        {
            // keep rolling through the same elements of the enumeration
            return View.values()[( ordinal() + 1 ) % View.values().length];
        }
    }
}
