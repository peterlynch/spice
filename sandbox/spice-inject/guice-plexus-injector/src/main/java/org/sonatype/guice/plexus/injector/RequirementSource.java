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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.spi.TypeEncounter;

final class RequirementSource
    implements PropertySource<Requirement>
{
    private Provider<Injector> injectorProvider;

    private Injector cachedInjector = null;

    RequirementSource( final TypeEncounter<?> encounter )
    {
        injectorProvider = encounter.getProvider( Injector.class );
    }

    public Requirement getAnnotation( final AnnotatedElement element )
    {
        return element.getAnnotation( Requirement.class );
    }

    public Provider<?> getProvider( final TypeLiteral<?> expectedType, final Requirement requirement )
    {
        final TypeLiteral<?> roleType = getRole( expectedType, requirement );
        final String[] hints = getHints( requirement );

        if ( expectedType.getRawType() == Map.class )
        {
            return getMapProvider( roleType, hints );
        }
        else if ( expectedType.getRawType() == List.class )
        {
            return getListProvider( roleType, hints );
        }

        return getProvider( roleType, hints[0] );
    }

    private <T> Provider<Map<String, T>> getMapProvider( final TypeLiteral<T> roleType, final String... hints )
    {
        return new Provider<Map<String, T>>()
        {
            public Map<String, T> get()
            {
                final List<Binding<T>> bindings = findBindingsByRole( roleType, hints );

                final int numBindings = bindings.size();
                final Map<String, T> map = new LinkedHashMap<String, T>( numBindings );
                for ( int i = 0; i < numBindings; i++ )
                {
                    final Binding<T> b = bindings.get( i );
                    final Annotation annotation = b.getKey().getAnnotation();
                    if ( annotation == null )
                    {
                        map.put( "", b.getProvider().get() );
                    }
                    else if ( annotation instanceof Named )
                    {
                        map.put( ( (Named) annotation ).value(), b.getProvider().get() );
                    }
                }
                return map;
            }
        };
    }

    private <T> Provider<List<T>> getListProvider( final TypeLiteral<T> roleType, final String... hints )
    {
        return new Provider<List<T>>()
        {
            public List<T> get()
            {
                final List<Binding<T>> bindings = findBindingsByRole( roleType, hints );

                final int numBindings = bindings.size();
                final List<T> list = new ArrayList<T>( numBindings );
                for ( int i = 0; i < numBindings; i++ )
                {
                    final Binding<T> b = bindings.get( i );
                    final Annotation annotation = b.getKey().getAnnotation();
                    if ( annotation == null || annotation instanceof Named )
                    {
                        list.add( b.getProvider().get() );
                    }
                }
                return list;
            }
        };
    }

    private <T> Provider<T> getProvider( final TypeLiteral<T> roleType, final String hint )
    {
        if ( hint.length() > 0 )
        {
            return new Provider<T>()
            {
                public T get()
                {
                    return getInjector().getInstance( Key.get( roleType, Names.named( hint ) ) );
                }
            };
        }

        return new Provider<T>()
        {
            public T get()
            {
                try
                {
                    return getInjector().getInstance( Key.get( roleType ) );
                }
                catch ( final RuntimeException e )
                {
                    final List<Binding<T>> bindings = findBindingsByRole( roleType );

                    final int numBindings = bindings.size();
                    for ( int i = 0; i < numBindings; i++ )
                    {
                        final Binding<T> b = bindings.get( i );
                        final Annotation annotation = b.getKey().getAnnotation();
                        if ( annotation == null || annotation instanceof Named )
                        {
                            return b.getProvider().get();
                        }
                    }

                    throw new ProvisionException( "No implementation for " + roleType + " was bound." );
                }
            }
        };
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
        return requirement.hints().length == 0 ? new String[] { requirement.hint() } : requirement.hints();
    }

    Injector getInjector()
    {
        if ( null == cachedInjector )
        {
            cachedInjector = injectorProvider.get();
            injectorProvider = null;
        }
        return cachedInjector;
    }

    <T> List<Binding<T>> findBindingsByRole( final TypeLiteral<T> roleType, final String... hints )
    {
        final Injector injector = getInjector();

        if ( hints.length == 0 || hints[0].length() == 0 )
        {
            return injector.findBindingsByType( roleType );
        }

        final List<Binding<T>> bindings = new ArrayList<Binding<T>>();
        for ( final String h : hints )
        {
            bindings.add( injector.getBinding( Key.get( roleType, Names.named( h ) ) ) );
        }

        return bindings;
    }
}
