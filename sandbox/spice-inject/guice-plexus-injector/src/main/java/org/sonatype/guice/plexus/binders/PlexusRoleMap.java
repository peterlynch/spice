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
package org.sonatype.guice.plexus.binders;

import static org.sonatype.guice.plexus.utils.PlexusConstants.DEFAULT_HINT;
import static org.sonatype.guice.plexus.utils.PlexusConstants.getCanonicalHint;
import static org.sonatype.guice.plexus.utils.PlexusConstants.isDefaultHint;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

@Singleton
final class PlexusRoleMap<T>
{
    private final TypeLiteral<T> roleType;

    private final Map<String, Provider<T>> roleMap;

    private final String[] allHints;

    @Inject
    PlexusRoleMap( final Injector injector, final TypeLiteral<T> roleType )
    {
        this.roleType = roleType;

        // find all known bindings for the role, note: excludes Just-In-Time bindings!
        final List<Binding<T>> typeBindings = injector.findBindingsByType( roleType );
        final int numBindings = typeBindings.size();

        roleMap = new LinkedHashMap<String, Provider<T>>( 1 + numBindings );

        try
        {
            // use explicit query for default, in case it's a Just-In-Time binding
            roleMap.put( DEFAULT_HINT, injector.getProvider( Key.get( roleType ) ) );
        }
        catch ( final ConfigurationException e ) // NOPMD
        {
            // safe to ignore, as default component not always available
        }

        // @Named bindings => Plexus hints
        for ( int i = 0; i < numBindings; i++ )
        {
            final Binding<T> b = typeBindings.get( i );
            final Annotation a = b.getKey().getAnnotation();
            if ( a instanceof Named )
            {
                final String hint = ( (Named) a ).value();
                if ( isDefaultHint( hint ) )
                {
                    throw new ProvisionException( "Default binding " + b + " should not have a @Named annotation" );
                }
                roleMap.put( getCanonicalHint( hint ), b.getProvider() );
            }
        }

        allHints = roleMap.keySet().toArray( new String[roleMap.size()] );
    }

    Map<String, T> getRoleHintMap( final String... selectedHints )
    {
        final String[] hints = selectedHints.length > 0 ? selectedHints : allHints;
        final Map<String, T> roleHintMap = new LinkedHashMap<String, T>( hints.length );
        for ( final String h : hints )
        {
            final Provider<T> provider = roleMap.get( h );
            if ( null == provider )
            {
                throw new ProvisionException( "No implementation for " + roleType
                    + " annotated with @com.google.inject.name.Named(value=" + h + ") was bound." );
            }
            roleHintMap.put( h, provider.get() );
        }
        return Collections.unmodifiableMap( roleHintMap );
    }

    List<T> getRoleHintList( final String... selectedHints )
    {
        return Collections.unmodifiableList( new ArrayList<T>( getRoleHintMap( selectedHints ).values() ) );
    }
}