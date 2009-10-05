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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.util.Types;

final class PlexusRequirement
{
    static final String DEFAULT_HINT = "default";

    @SuppressWarnings( "unchecked" )
    static Provider<?> getProvider( final TypeEncounter<?> encounter, final Requirement requirement,
                                    final PlexusProperty property )
    {
        final TypeLiteral expectedType = property.getType();
        final TypeLiteral roleType = getRole( expectedType, requirement );
        final String[] hints = getHints( requirement );

        if ( Map.class == expectedType.getRawType() )
        {
            final Provider roleMapProvider = getRoleMapProvider( encounter, roleType );
            return new Provider()
            {
                public Map get()
                {
                    return ( (PlexusRoleMap) roleMapProvider.get() ).getRoleHintMap( hints );
                }
            };
        }

        if ( List.class == expectedType.getRawType() )
        {
            final Provider roleMapProvider = getRoleMapProvider( encounter, roleType );
            return new Provider()
            {
                public List get()
                {
                    return ( (PlexusRoleMap) roleMapProvider.get() ).getRoleHintList( hints );
                }
            };
        }

        if ( hints.length > 0 )
        {
            return encounter.getProvider( Key.get( roleType, Names.named( hints[0] ) ) );
        }

        return encounter.getProvider( Key.get( roleType ) );
    }

    private static TypeLiteral<?> getRole( final TypeLiteral<?> expectedType, final Requirement requirement )
    {
        final Type role = requirement.role();
        if ( role != Object.class )
        {
            return TypeLiteral.get( role );
        }
        if ( Map.class == expectedType.getRawType() )
        {
            return getTypeArgument( expectedType, 1 );
        }
        if ( List.class == expectedType.getRawType() )
        {
            return getTypeArgument( expectedType, 0 );
        }

        return expectedType;
    }

    private static TypeLiteral<?> getTypeArgument( final TypeLiteral<?> genericType, final int index )
    {
        final Type t = ( (ParameterizedType) genericType.getType() ).getActualTypeArguments()[index];
        return TypeLiteral.get( t instanceof WildcardType ? ( (WildcardType) t ).getUpperBounds()[0] : t );
    }

    private static String[] getHints( final Requirement requirement )
    {
        final String[] hints = requirement.hints();
        if ( hints.length > 0 )
        {
            for ( int i = 0; i < hints.length; i++ )
            {
                hints[i] = normalizeHint( hints[i] );
            }
            return hints;
        }
        final String hint = normalizeHint( requirement.hint() );
        if ( DEFAULT_HINT.equals( hint ) )
        {
            return new String[0];
        }
        return new String[] { hint };
    }

    static String normalizeHint( final String hint )
    {
        return hint.length() == 0 ? DEFAULT_HINT : hint;
    }

    @SuppressWarnings( "unchecked" )
    private static <T> Provider<PlexusRoleMap<T>> getRoleMapProvider( final TypeEncounter<?> encounter,
                                                                      final TypeLiteral<T> roleType )
    {
        final Type roleMapType = Types.newParameterizedType( PlexusRoleMap.class, roleType.getType() );
        return (Provider) encounter.getProvider( Key.get( roleMapType ) );
    }
}
