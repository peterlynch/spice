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
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.util.Types;

/**
 * Creates {@link Provider}s for property elements annotated with @{@link Requirement}.
 */
final class PlexusRequirementFactory
    implements PropertyProviderFactory<Requirement>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final TypeEncounter<?> encounter;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusRequirementFactory( final TypeEncounter<?> encounter )
    {
        this.encounter = encounter;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public <T> Provider<T> lookup( final Requirement requirement, final InjectableProperty<T> property )
    {
        // extract the various requirement parameters
        final TypeLiteral expectedType = property.getType();
        final TypeLiteral roleType = Roles.getRole( requirement, expectedType );
        final String[] canonicalHints = Hints.getCanonicalHints( requirement );

        if ( Map.class == expectedType.getRawType() )
        {
            // build map of Plexus components
            final Provider<PlexusComponentFinder<?>> componentFinder = getComponentFinder( roleType );
            return new Provider()
            {
                public Map get()
                {
                    return componentFinder.get().getComponentMap( canonicalHints );
                }
            };
        }

        if ( List.class == expectedType.getRawType() )
        {
            // build list of Plexus components
            final Provider<PlexusComponentFinder<?>> componentFinder = getComponentFinder( roleType );
            return new Provider()
            {
                public List get()
                {
                    return componentFinder.get().getComponentList( canonicalHints );
                }
            };
        }

        if ( canonicalHints.length == 0 || Hints.isDefaultHint( canonicalHints[0] ) )
        {
            return encounter.getProvider( Key.get( roleType ) );
        }

        return encounter.getProvider( Key.get( roleType, Names.named( canonicalHints[0] ) ) );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Returns a {@link Provider} that can provide a {@link PlexusComponentFinder} for the given role type.
     * 
     * @param roleType The Plexus role type
     * @return Provider that provides a component finder for the given role type
     */
    @SuppressWarnings( "unchecked" )
    private <T> Provider<PlexusComponentFinder<T>> getComponentFinder( final TypeLiteral<T> roleType )
    {
        final Type finderType = Types.newParameterizedType( PlexusComponentFinder.class, roleType.getType() );
        return (Provider) encounter.getProvider( Key.get( finderType ) );
    }
}
