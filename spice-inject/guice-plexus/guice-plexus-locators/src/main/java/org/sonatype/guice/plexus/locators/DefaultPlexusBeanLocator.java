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
package org.sonatype.guice.plexus.locators;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.sonatype.guice.plexus.config.MutablePlexusBeanLocator;
import org.sonatype.guice.plexus.config.PlexusBean;

import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * {@link MutablePlexusBeanLocator} that locates beans of various types from zero or more {@link Injector}s.
 */
@Singleton
public final class DefaultPlexusBeanLocator
    implements MutablePlexusBeanLocator
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Set<Injector> injectors = new LinkedHashSet<Injector>();

    // track all bean sequences returned to clients, so we can clean up when they become eligible for GC
    private final List<Reference<PlexusBeans<?>>> exposedBeans = new ArrayList<Reference<PlexusBeans<?>>>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public synchronized <T> Iterable<PlexusBean<T>> locate( final TypeLiteral<T> role, final String... hints )
    {
        final PlexusBeans<T> beans;
        if ( hints.length == 0 )
        {
            // sequence of all beans with this type
            beans = new DefaultPlexusBeans<T>( role );
        }
        else
        {
            // sequence of hinted beans with this type
            beans = new HintedPlexusBeans<T>( role, hints );
        }

        // build up the current bean sequence
        for ( final Injector injector : injectors )
        {
            beans.add( injector );
        }

        for ( int i = 0; i < exposedBeans.size(); i++ )
        {
            // remove any previous GCd sequences
            if ( null == exposedBeans.get( i ).get() )
            {
                exposedBeans.remove( i-- );
            }
        }

        // record sequence so we can update it later on if necessary
        exposedBeans.add( new WeakReference<PlexusBeans<?>>( beans ) );

        return beans;
    }

    @Inject
    public synchronized void add( final Injector injector )
    {
        if ( null == injector || !injectors.add( injector ) )
        {
            return; // not a new injector, nothing to do
        }
        for ( int i = 0; i < exposedBeans.size(); i++ )
        {
            // inform existing sequences about the added injector
            final PlexusBeans<?> beans = exposedBeans.get( i ).get();
            if ( null != beans )
            {
                beans.add( injector );
            }
            else
            {
                // sequence has been GCd
                exposedBeans.remove( i-- );
            }
        }
    }

    public synchronized void remove( final Injector injector )
    {
        if ( null == injector || !injectors.remove( injector ) )
        {
            return; // not an old injector, nothing to do
        }
        for ( int i = 0; i < exposedBeans.size(); i++ )
        {
            // inform existing sequences about the removed injector
            final PlexusBeans<?> beans = exposedBeans.get( i ).get();
            if ( null != beans )
            {
                beans.remove( injector );
            }
            else
            {
                // sequence has been GCd
                exposedBeans.remove( i-- );
            }
        }
    }

    public synchronized void clear()
    {
        for ( int i = 0; i < exposedBeans.size(); i++ )
        {
            final PlexusBeans<?> beans = exposedBeans.get( i ).get();
            if ( null != beans )
            {
                beans.clear();
            }
        }

        exposedBeans.clear();
        injectors.clear();
    }
}