/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.locators;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * {@link Watchable} sequence of qualified beans backed by bindings from one or more {@link Injector}s.
 */
final class GuiceBeans<Q extends Annotation, T>
    implements Watchable<Entry<Q, T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Key<T> key;

    private List<InjectorBeans<Q, T>> injectorBeans;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    GuiceBeans( final Key<T> key )
    {
        this.key = key;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public synchronized Iterator<Entry<Q, T>> iterator()
    {
        if ( null == injectorBeans )
        {
            return Collections.EMPTY_LIST.iterator();
        }
        // "copy-on-read" - concatenate all beans
        final List combinedBeans = new ArrayList();
        for ( int i = 0, size = injectorBeans.size(); i < size; i++ )
        {
            combinedBeans.addAll( injectorBeans.get( i ) );
        }
        return combinedBeans.iterator();
    }

    public synchronized boolean subscribe( final Watcher<Entry<Q, T>> watcher )
    {
        return false; // TODO Auto-generated method stub
    }

    public synchronized boolean unsubscribe( final Watcher<Entry<Q, T>> watcher )
    {
        return false; // TODO Auto-generated method stub
    }

    public synchronized boolean add( final Injector injector )
    {
        final InjectorBeans<Q, T> newBeans = new InjectorBeans<Q, T>( injector, key );
        if ( newBeans.isEmpty() )
        {
            return false; // nothing to add
        }
        if ( null == injectorBeans )
        {
            injectorBeans = new ArrayList<InjectorBeans<Q, T>>( 4 );
        }
        return injectorBeans.add( newBeans );
    }

    public synchronized boolean remove( final Injector injector )
    {
        if ( null == injectorBeans )
        {
            return false; // nothing to remove
        }
        for ( final InjectorBeans<Q, T> beans : injectorBeans )
        {
            if ( injector == beans.injector )
            {
                return injectorBeans.remove( beans );
            }
        }
        return false;
    }
}