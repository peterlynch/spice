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

import com.google.inject.MembersInjector;

/**
 * {@link MembersInjector} that can apply a series of {@link Setter}s to a given instance.
 */
final class PlexusComponentInjector<T>
    implements MembersInjector<T>
{
    /**
     * Common interface to provide deferred injection of components.
     */
    interface Setter
    {
        /**
         * Inject a component into a member of the given instance.
         * 
         * @param instance an instance requiring injection
         */
        void apply( Object instance );
    }

    final Iterable<Setter> setters;

    PlexusComponentInjector( final Iterable<Setter> setters )
    {
        this.setters = setters;
    }

    public void injectMembers( final T instance )
    {
        for ( final Setter s : setters )
        {
            s.apply( instance );
        }
    }
}
