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
package org.sonatype.guice.plexus.binder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.plexus.binder.PlexusComponentInjector.Setter;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.util.Types;

/**
 * Base {@link Setter} class that injects a component into a member marked with {@link Requirement}.
 */
abstract class AbstractPlexusSetter
    implements Setter
{
    private final TypeEncounter<?> encounter;

    AbstractPlexusSetter( final TypeEncounter<?> encounter )
    {
        this.encounter = encounter;
    }

    /**
     * Query Guice to find the appropriate component(s) for the given type and {@link Requirement}.
     * 
     * @param targetType the target type
     * @param requirement the requirement
     * @return Guice-backed {@link Provider} that provides component(s) matching the requirement
     */
    protected final Provider<?> lookup( final TypeLiteral<?> targetType, final Requirement requirement )
    {
        final Type role = getRole( targetType, requirement );
        final String[] hints = getHints( requirement );

        if ( targetType.getRawType() == Map.class )
        {
            return getMapProvider( role, hints );
        }
        else if ( targetType.getRawType() == List.class )
        {
            return getListProvider( role, hints );
        }

        return encounter.getProvider( getRequirementKey( role, hints[0] ) );
    }

    /**
     * Inject deferred component into a member of the given instance.
     * 
     * @param instance instance being injected
     * @throws Exception
     */
    protected abstract void privilegedApply( Object instance )
        throws Exception;

    public final void apply( final Object instance )
    {
        // need this when using reflection inside a web-application
        AccessController.doPrivileged( new PrivilegedAction<Object>()
        {
            public Object run()
            {
                try
                {
                    privilegedApply( instance );
                }
                catch ( final Exception e )
                {
                    throw new ProvisionException( e.getLocalizedMessage(), e );
                }
                return null;
            }
        } );
    }

    /**
     * Query Guice to build a map of components for the given type and {@link Requirement}.
     * 
     * @param role the plexus role
     * @param hints the plexus hints
     * @return deferred {@link Map} of matching components
     */
    @SuppressWarnings( "unchecked" )
    private Provider<Map<String, ?>> getMapProvider( final Type role, final String[] hints )
    {
        if ( hints[0].isEmpty() )
        {
            // @Requirement with no hints means inject all implementations (assume Guice multibinder)
            return (Provider) encounter.getProvider( Key.get( Types.mapOf( String.class, role ) ) );
        }

        final Map<String, Provider> providers = new LinkedHashMap();
        for ( final String h : hints )
        {
            // convert each hint into a named Guice query for the appropriate interface
            providers.put( h, encounter.getProvider( getRequirementKey( role, h ) ) );
        }

        return new Provider()
        {
            public Map<String, ?> get()
            {
                // we can now use the deferred map to build our actual component map
                final Map<String, Object> map = new LinkedHashMap<String, Object>();
                for ( final Entry<String, Provider> e : providers.entrySet() )
                {
                    map.put( e.getKey(), e.getValue().get() );
                }
                return map;
            }

            @Override
            public String toString()
            {
                return providers.toString();
            }
        };
    }

    /**
     * Query Guice to build a list of components for the given type and {@link Requirement}.
     * 
     * @param role the plexus role
     * @param hints the plexus hints
     * @return deferred {@link List} of matching components
     */
    private Provider<List<?>> getListProvider( final Type role, final String[] hints )
    {
        // a list of components is the same as getting a map and using its values
        final Provider<Map<String, ?>> mapProvider = getMapProvider( role, hints );

        return new Provider<List<?>>()
        {
            public List<?> get()
            {
                // use the underlying map to build a list of components
                return new ArrayList<Object>( mapProvider.get().values() );
            }

            @Override
            public String toString()
            {
                return mapProvider.toString();
            }
        };
    }

    /**
     * Find the role type that best matches the target and the given {@link Requirement}s.
     * 
     * @param targetType the target type
     * @param requirement the requirement
     * @return appropriate type for the role
     */
    private static Type getRole( final TypeLiteral<?> targetType, final Requirement requirement )
    {
        final Type role = requirement.role();
        if ( role != Object.class )
        {
            return role; // specific role wins
        }
        if ( Map.class == targetType.getRawType() )
        {
            // extract the map's value type
            return getTypeArgument( targetType, 1 );
        }
        if ( List.class == targetType.getRawType() )
        {
            // extract the list's element type
            return getTypeArgument( targetType, 0 );
        }

        // just use the actual type
        return targetType.getType();
    }

    /**
     * Collect the known hints from the given {@link Requirement}, always returns a non-empty array.
     * 
     * @param requirement the requirement
     * @return array of hints
     */
    private static String[] getHints( final Requirement requirement )
    {
        final String[] hints = requirement.hints();
        if ( hints.length > 0 )
        {
            return hints; // specific hints win
        }

        // accept single hint (may be empty string)
        return new String[] { requirement.hint() };
    }

    /**
     * Extract the actual type argument from a generic type, such as {@literal List<String>}.
     * 
     * @param genericType the generic type
     * @param index the argument index
     * @return specific type argument used in the generic type declaration
     */
    private static Type getTypeArgument( final TypeLiteral<?> genericType, final int index )
    {
        final Type type = ( (ParameterizedType) genericType.getType() ).getActualTypeArguments()[index];
        if ( type instanceof WildcardType )
        {
            // upper bound is the sub-type (rather confusingly)
            return ( (WildcardType) type ).getUpperBounds()[0];
        }
        return type;
    }

    /**
     * Convert a plexus role/hint combination into the appropriate Guice binding {@link Key}.
     * 
     * @param role the plexus role
     * @param hint the plexus hint
     * @return appropriate Guice binding {@link Key}
     */
    private static Key<?> getRequirementKey( final Type role, final String hint )
    {
        return hint.isEmpty() ? Key.get( role ) : Key.get( role, Names.named( hint ) );
    }
}
