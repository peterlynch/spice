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

import static com.google.inject.name.Names.named;
import static org.sonatype.guice.plexus.utils.Hints.NO_HINTS;
import static org.sonatype.guice.plexus.utils.Hints.getCanonicalHint;
import static org.sonatype.guice.plexus.utils.Hints.isDefaultHint;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
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
        final TypeLiteral roleType = getRole( requirement, expectedType );
        final String[] canonicalHints = getCanonicalHints( requirement );

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

        if ( canonicalHints.length == 0 || isDefaultHint( canonicalHints[0] ) )
        {
            return encounter.getProvider( Key.get( roleType ) );
        }

        return encounter.getProvider( Key.get( roleType, named( canonicalHints[0] ) ) );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Extracts the role type from the given @{@link Requirement} and expected property type.
     * 
     * @param requirement The Plexus requirement
     * @param expectedType The expected property type
     * @return The appropriate role type
     */
    private static TypeLiteral<?> getRole( final Requirement requirement, final TypeLiteral<?> expectedType )
    {
        final Type role = requirement.role();
        if ( role != Object.class )
        {
            return TypeLiteral.get( role );
        }
        if ( Map.class == expectedType.getRawType() )
        {
            // Map<String, T> --> T
            return getTypeArgument( expectedType, 1 );
        }
        if ( List.class == expectedType.getRawType() )
        {
            // List<T> --> T
            return getTypeArgument( expectedType, 0 );
        }
        return expectedType;
    }

    /**
     * Extracts a type argument from a generic type, for example {@code String} from {@code List<String>}.
     * 
     * @param genericType The generic type
     * @param index The type argument index
     * @return Selected type argument
     */
    private static TypeLiteral<?> getTypeArgument( final TypeLiteral<?> genericType, final int index )
    {
        final Type t = ( (ParameterizedType) genericType.getType() ).getActualTypeArguments()[index];
        return TypeLiteral.get( t instanceof WildcardType ? ( (WildcardType) t ).getUpperBounds()[0] : t );
    }

    /**
     * Extracts an array of Plexus hints from the given @{@link Requirement}.
     * 
     * @param requirement The Plexus requirement
     * @return Array of hints
     */
    private static String[] getCanonicalHints( final Requirement requirement )
    {
        final String[] hints = requirement.hints();
        if ( hints.length > 0 )
        {
            for ( int i = 0; i < hints.length; i++ )
            {
                hints[i] = getCanonicalHint( hints[i] );
            }
            return hints;
        }
        final String hint = requirement.hint();
        if ( hint.length() > 0 )
        {
            return new String[] { hint };
        }
        return NO_HINTS;
    }

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
