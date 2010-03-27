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
package org.sonatype.guice.bean.scanners;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.locators.EntryListAdapter;
import org.sonatype.guice.bean.locators.EntryMapAdapter;
import org.sonatype.guice.bean.locators.NamedWatchableAdapter;
import org.sonatype.guice.bean.locators.Watchable;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

final class WatchedProvider<K extends Annotation, V>
    implements Provider<Watchable<Entry<K, V>>>
{
    @Inject
    private BeanLocator locator;

    @Inject
    private TypeLiteral<K> qualifierType;

    @Inject
    private TypeLiteral<V> beanType;

    @SuppressWarnings( "unchecked" )
    public Watchable<Entry<K, V>> get()
    {
        if ( qualifierType.getRawType() != Annotation.class )
        {
            return locator.locate( Key.get( beanType, (Class) qualifierType.getRawType() ) );
        }
        return locator.locate( Key.get( beanType ) );
    }
}

final class WatchedHintProvider<V>
    implements Provider<Watchable<Entry<String, V>>>
{
    @Inject
    private WatchedProvider<Named, V> provider;

    public Watchable<Entry<String, V>> get()
    {
        return new NamedWatchableAdapter<V>( provider.get() );
    }
}

final class ListProvider<T>
    implements Provider<List<T>>
{
    @Inject
    private WatchedProvider<Annotation, T> provider;

    public List<T> get()
    {
        return new EntryListAdapter<Annotation, T>( provider.get() );
    }
}

final class MapProvider<K extends Annotation, V>
    implements Provider<Map<K, V>>
{
    @Inject
    private WatchedProvider<K, V> provider;

    public Map<K, V> get()
    {
        return new EntryMapAdapter<K, V>( provider.get() );
    }
}

final class MapHintProvider<V>
    implements Provider<Map<String, V>>
{
    @Inject
    private WatchedHintProvider<V> provider;

    public Map<String, V> get()
    {
        return new EntryMapAdapter<String, V>( provider.get() );
    }
}
