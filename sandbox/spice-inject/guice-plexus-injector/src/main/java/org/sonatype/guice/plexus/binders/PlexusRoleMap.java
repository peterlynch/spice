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

import static org.sonatype.guice.plexus.utils.Hints.DEFAULT_HINT;
import static org.sonatype.guice.plexus.utils.Hints.getCanonicalHint;
import static org.sonatype.guice.plexus.utils.Hints.isDefaultHint;

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
    private static final String MISSING_BINDING_ERROR =
        "No implementation for %s annotated with @com.google.inject.name.Named(value=%s) was bound.";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final TypeLiteral<T> roleType;

    private final Map<String, Provider<T>> roleMap;

    private final String[] allHints;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @Inject
    PlexusRoleMap( final Injector injector, final TypeLiteral<T> roleType )
    {
        this.roleType = roleType;

        // find all known bindings for the role, note: excludes Just-In-Time bindings!
        final List<Binding<T>> typeBindings = injector.findBindingsByType( roleType );
        final int numBindings = typeBindings.size();

        roleMap = new LinkedHashMap<String, Provider<T>>();

        try
        {
            // use explicit query for default, in case it's a Just-In-Time binding
            roleMap.put( DEFAULT_HINT, injector.getProvider( Key.get( roleType ) ) );
        }
        catch ( final ConfigurationException e ) // NOPMD
        {
            // safe to ignore, as a default component may not always be available
        }

        // @Named bindings => Plexus hints
        for ( int i = 0; i < numBindings; i++ )
        {
            final Binding<T> b = typeBindings.get( i );
            final Annotation a = b.getKey().getAnnotation();
            if ( a instanceof Named )
            {
                final String hint = getCanonicalHint( ( (Named) a ).value() );
                if ( !isDefaultHint( hint ) )
                {
                    roleMap.put( hint, b.getProvider() );
                }
            }
        }

        allHints = roleMap.keySet().toArray( new String[roleMap.size()] );
    }

    // ----------------------------------------------------------------------
    // Package-private methods
    // ----------------------------------------------------------------------

    Map<String, T> getRoleHintMap( final String... canonicalHints )
    {
        final String[] hints = canonicalHints.length > 0 ? canonicalHints : allHints;
        final Map<String, T> roleHintMap = new LinkedHashMap<String, T>();
        for ( final String h : hints )
        {
            final Provider<T> provider = roleMap.get( h );
            if ( null == provider )
            {
                throw new ProvisionException( String.format( MISSING_BINDING_ERROR, roleType, h ) );
            }
            roleHintMap.put( h, provider.get() );
        }
        return Collections.unmodifiableMap( roleHintMap );
    }

    List<T> getRoleHintList( final String... canonicalHints )
    {
        return Collections.unmodifiableList( new ArrayList<T>( getRoleHintMap( canonicalHints ).values() ) );
    }
}