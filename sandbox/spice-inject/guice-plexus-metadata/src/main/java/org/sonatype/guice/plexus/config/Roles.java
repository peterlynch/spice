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
package org.sonatype.guice.plexus.config;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.Generics;
import org.sonatype.guice.bean.reflect.WeakDeferredClass;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Constants and utility methods for dealing with Plexus roles.
 */
public final class Roles
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Roles()
    {
        // static utility class, not allowed to create instances
    }

    static
    {
        new Roles(); // keep Cobertura coverage happy
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Returns the canonical form of the given Plexus role-hint.
     * 
     * @param role The Plexus role
     * @param hint The Plexus hint
     * @return Canonical role-hint denoting the same component as the given role-hint
     */
    public static String canonicalRoleHint( final String role, final String hint )
    {
        return role + ':' + Hints.canonicalHint( hint );
    }

    /**
     * Deduces the role type based on the given @{@link Requirement} and expected type.
     * 
     * @param requirement The Plexus requirement
     * @param asType The expected type
     * @return "Best-fit" role type
     */
    public static TypeLiteral<?> roleType( final Requirement requirement, final TypeLiteral<?> asType )
    {
        final Type role = requirement.role();
        if ( role != Object.class )
        {
            return TypeLiteral.get( role );
        }
        final Class<?> rawType = asType.getRawType();
        if ( Map.class == rawType )
        {
            // Map<String, T> --> T
            return Generics.typeArgument( asType, 1 );
        }
        if ( List.class == rawType )
        {
            // List<T> --> T
            return Generics.typeArgument( asType, 0 );
        }
        return asType;
    }

    /**
     * Returns the component binding {@link Key} for the given Plexus component.
     * 
     * @param component The Plexus component
     * @return Component binding key denoting the given component
     */
    public static Key<?> componentKey( final Component component )
    {
        return componentKey( component.role(), component.hint() );
    }

    /**
     * Returns the component binding {@link Key} for the given Plexus role-hint.
     * 
     * @param role The Plexus role
     * @param hint The Plexus hint
     * @return Component binding key denoting the given role-hint
     */
    public static <T> Key<T> componentKey( final Class<T> role, final String hint )
    {
        return componentKey( TypeLiteral.get( role ), hint );
    }

    /**
     * Returns the component binding {@link Key} for the given Plexus role-hint.
     * 
     * @param role The Plexus role
     * @param hint The Plexus hint
     * @return Component binding key denoting the given role-hint
     */
    public static <T> Key<T> componentKey( final TypeLiteral<T> role, final String hint )
    {
        if ( Hints.isDefaultHint( hint ) )
        {
            return Key.get( role );
        }
        return Key.get( role, Names.named( Hints.canonicalHint( hint ) ) );
    }

    /**
     * Returns degenerate {@link DeferredClass} for the given Plexus role.
     * 
     * @param role The Plexus role
     * @return Deferred class
     */
    public static <T> DeferredClass<T> defer( final Class<T> role )
    {
        return new DeferredClass<T>()
        {
            public Class<T> get()
            {
                return role;
            }

            public String getName()
            {
                return role.getName();
            }
        };
    }

    /**
     * Returns {@link DeferredClass} for the named role from the given {@link ClassSpace}.
     * 
     * @param space The class space
     * @param role The Plexus role
     * @return Deferred class
     */
    public static DeferredClass<?> defer( final ClassSpace space, final String role )
    {
        return new WeakDeferredClass<Object>( space, role );
    }
}
