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

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.util.Types;

/**
 * Creates {@link Provider}s for properties with @{@link Requirement} metadata.
 */
final class PlexusRequirements
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final TypeEncounter<?> encounter;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusRequirements( final TypeEncounter<?> encounter )
    {
        this.encounter = encounter;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public <T> Provider<T> lookup( final Requirement requirement, final BeanProperty<T> property )
    {
        // deduce requirement from metadata + property details
        final TypeLiteral expectedType = property.getType();
        final TypeLiteral roleType = Roles.getRole( requirement, expectedType );
        final String[] canonicalHints = Hints.getCanonicalHints( requirement );

        if ( Map.class == expectedType.getRawType() )
        {
            final Provider<PlexusComponents> components = getComponentsForRole( roleType );
            return new Provider()
            {
                public Map<String, T> get()
                {
                    return components.get().lookupMap( canonicalHints );
                }
            };
        }
        else if ( List.class == expectedType.getRawType() )
        {
            final Provider<PlexusComponents> components = getComponentsForRole( roleType );
            return new Provider()
            {
                public List<T> get()
                {
                    return components.get().lookupList( canonicalHints );
                }
            };
        }
        else if ( canonicalHints.length == 0 )
        {
            final Provider<PlexusComponents> components = getComponentsForRole( roleType );
            return new Provider()
            {
                public Object get()
                {
                    return components.get().lookupWildcard();
                }
            };
        }

        return encounter.getProvider( Roles.componentKey( roleType, canonicalHints[0] ) );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Returns a {@link Provider} that can provide a {@link PlexusComponents} for the given role type.
     * 
     * @param roleType The reified Plexus role
     * @return Provider that provides components for the given role
     */
    @SuppressWarnings( "unchecked" )
    private Provider<PlexusComponents> getComponentsForRole( final TypeLiteral roleType )
    {
        final Type providerType = Types.newParameterizedType( PlexusComponents.class, roleType.getType() );
        return (Provider) encounter.getProvider( Key.get( providerType ) );
    }
}
