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

import static org.sonatype.guice.plexus.config.Hints.DEFAULT_HINT;
import static org.sonatype.guice.plexus.config.Hints.getCanonicalHint;
import static org.sonatype.guice.plexus.config.Hints.isDefaultHint;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
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

/**
 * Supplies filtered maps/lists/wildcards of registered Plexus components.
 */
@Singleton
final class PlexusComponents<T>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String MISSING_ROLE_ERROR = "No implementation for %s was bound.";

    private static final String MISSING_ROLE_HINT_ERROR =
        "No implementation for %s annotated with @com.google.inject.name.Named(value=%s) was bound.";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String roleName;

    private final Map<String, Provider<T>> roleMap;

    private final String[] allHints;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @Inject
    PlexusComponents( final Injector injector, final TypeLiteral<T> roleType )
    {
        this.roleName = roleType.toString();

        // find all known bindings for the role, note: excludes Just-In-Time bindings!
        final List<Binding<T>> typeBindings = injector.findBindingsByType( roleType );
        final int numBindings = typeBindings.size();

        final Map<String, Provider<T>> tempMap = new LinkedHashMap<String, Provider<T>>( 2 * numBindings );

        try
        {
            // use explicit query for default, in case it's a Just-In-Time binding
            tempMap.put( DEFAULT_HINT, injector.getProvider( Key.get( roleType ) ) );
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
                // ignore default bindings as we already captured that above
                final String hint = getCanonicalHint( ( (Named) a ).value() );
                if ( !isDefaultHint( hint ) )
                {
                    tempMap.put( hint, b.getProvider() );
                }
            }
        }

        // ordering is recorded in hint array, so can use simpler hash map
        allHints = tempMap.keySet().toArray( new String[tempMap.size()] );
        roleMap = new HashMap<String, Provider<T>>( tempMap );
    }

    // ----------------------------------------------------------------------
    // Shared package-private methods
    // ----------------------------------------------------------------------

    /**
     * Returns a map of Plexus components for the current role, filtered by the given hints.
     * 
     * @param canonicalHints The Plexus hints
     * @return Map of Plexus components with the given hints
     */
    Map<String, T> lookupMap( final String... canonicalHints )
    {
        final String[] hints = canonicalHints.length > 0 ? canonicalHints : allHints;
        final Map<String, T> roleHintMap = new LinkedHashMap<String, T>( 2 * hints.length );
        for ( final String h : hints )
        {
            roleHintMap.put( h, lookupRole( h ) );
        }
        return roleHintMap;
    }

    /**
     * Returns a list of Plexus components for the current role, filtered by the given hints.
     * 
     * @param canonicalHints The Plexus hints
     * @return List of Plexus components with the given hints
     */
    List<T> lookupList( final String... canonicalHints )
    {
        final String[] hints = canonicalHints.length > 0 ? canonicalHints : allHints;
        final List<T> roleHintList = new ArrayList<T>( hints.length );
        for ( final String h : hints )
        {
            roleHintList.add( lookupRole( h ) );
        }
        return roleHintList;
    }

    /**
     * Returns the first component registered for the current role, regardless of hints.
     * 
     * @return Single Plexus component
     */
    T lookupWildcard()
    {
        if ( allHints.length == 0 )
        {
            throw new ProvisionException( String.format( MISSING_ROLE_ERROR, roleName ) );
        }
        return lookupRole( allHints[0] );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Returns the component instance that matches the given Plexus hint.
     * 
     * @param hint The Plexus hint
     * @return Component instance that matches the given hint
     */
    private T lookupRole( final String hint )
    {
        final Provider<T> provider = roleMap.get( hint );
        if ( null == provider )
        {
            throw new ProvisionException( String.format( MISSING_ROLE_HINT_ERROR, roleName, hint ) );
        }
        return provider.get();
    }
}