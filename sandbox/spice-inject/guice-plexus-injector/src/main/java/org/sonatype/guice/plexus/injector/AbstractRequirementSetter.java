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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;

import com.google.inject.ConfigurationException;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.util.Types;

/**
 * Base {@link PropertyInjector} class that injects a component into a member marked with {@link Requirement}.
 */
abstract class AbstractRequirementSetter
    implements PropertyInjector
{
    private static final Provider<Map<String, ?>> EMPTY_MAP_PROVIDER = new Provider<Map<String, ?>>()
    {
        public Map<String, ?> get()
        {
            return Collections.emptyMap();
        }
    };

    private final TypeEncounter<?> encounter;

    AbstractRequirementSetter( final TypeEncounter<?> encounter )
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
        final Type roleType = getRole( targetType, requirement );
        final String[] hints = getHints( requirement );

        if ( targetType.getRawType() == Map.class )
        {
            return getMapProvider( roleType, hints );
        }
        else if ( targetType.getRawType() == List.class )
        {
            return getListProvider( roleType, hints );
        }

        return getProvider( roleType, hints[0] );
    }

    /**
     * Inject deferred component into a member of the given instance.
     * 
     * @param instance instance being injected
     * @throws Exception
     */
    protected abstract void privilegedApply( Object instance )
        throws Exception;

    public final void inject( final Object instance )
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
                    throw new ProvisionException( e.toString() );
                }
                return null;
            }
        } );
    }

    @SuppressWarnings( "unchecked" )
    private Provider<Map<String, ?>> getMapProvider( final Type roleType )
    {
        try
        {
            return encounter.getProvider( (Key) Key.get( Types.mapOf( String.class, roleType ) ) );
        }
        catch ( final ConfigurationException e )
        {
            return EMPTY_MAP_PROVIDER;
        }
    }

    @SuppressWarnings( "unchecked" )
    private Map<String, Provider<?>> getProviderMap( final Type roleType )
    {
        try
        {
            return (Map) encounter.getProvider( Key.get( Types.mapOf( String.class, Types.providerOf( roleType ) ) ) ).get();
        }
        catch ( final ConfigurationException e )
        {
            return Collections.EMPTY_MAP;
        }
    }

    /**
     * Query Guice to build a map of components for the given type and {@link Requirement}.
     * 
     * @param roleType the plexus role
     * @param hints the plexus hints
     * @return deferred {@link Map} of matching components
     */
    private Provider<Map<String, ?>> getMapProvider( final Type roleType, final String[] hints )
    {
        if ( hints[0].length() == 0 )
        {
            return getMapProvider( roleType );
        }

        final Map<String, Provider<?>> providerMap = getProviderMap( roleType );
        for ( final String h : hints )
        {
            if ( !providerMap.containsKey( h ) )
            {
                encounter.addError( "No Component found for Requirement " + roleType + " hint " + h );
            }
        }

        return new Provider<Map<String, ?>>()
        {
            public Map<String, ?> get()
            {
                final Map<String, Object> filteredMap = new LinkedHashMap<String, Object>();
                for ( final String h : hints )
                {
                    filteredMap.put( h, providerMap.get( h ).get() );
                }
                return filteredMap;
            }

            @Override
            public String toString()
            {
                return "FIXME";// FIXME
            }
        };
    }

    /**
     * Query Guice to build a list of components for the given type and {@link Requirement}.
     * 
     * @param roleType the plexus role
     * @param hints the plexus hints
     * @return deferred {@link List} of matching components
     */
    private Provider<List<?>> getListProvider( final Type roleType, final String[] hints )
    {
        // a list of components is the same as getting a map and using its values
        final Provider<Map<String, ?>> mapProvider = getMapProvider( roleType, hints );

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
                return "FIXME";// FIXME
            }
        };
    }

    /**
     * Query Guice to build a single component for the given type and {@link Requirement}.
     * 
     * @param roleType the plexus role
     * @param hint the plexus hint
     * @return deferred matching component
     */
    private Provider<?> getProvider( final Type roleType, final String hint )
    {
        // single component is same as getting map and using the first value
        final Map<String, Provider<?>> providerMap = getProviderMap( roleType );
        if ( hint.length() > 0 && !providerMap.containsKey( hint ) )
        {
            encounter.addError( "No Component found for Requirement " + roleType + " hint " + hint );
        }
        else if ( providerMap.isEmpty() )
        {
            encounter.addError( "No Components found for Requirement " + roleType );
        }

        return new Provider<Object>()
        {
            public Object get()
            {
                final Provider<?> provider;
                if ( providerMap.containsKey( hint ) )
                {
                    provider = providerMap.get( hint );
                }
                else
                {
                    provider = providerMap.values().iterator().next();
                }
                return provider.get();
            }

            @Override
            public String toString()
            {
                return "FIXME";// FIXME
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
}
