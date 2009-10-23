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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.Generics;

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
     * Deduces the role type based on the given @{@link Requirement} and expected type.
     * 
     * @param requirement The Plexus requirement
     * @param expectedType The expected type
     * @return The appropriate role type
     */
    public static TypeLiteral<?> getRole( final Requirement requirement, final TypeLiteral<?> expectedType )
    {
        final Type role = requirement.role();
        if ( role != Object.class )
        {
            return TypeLiteral.get( role );
        }
        final Class<?> rawType = expectedType.getRawType();
        if ( Map.class == rawType )
        {
            // Map<String, T> --> T
            return Generics.getTypeArgument( expectedType, 1 );
        }
        if ( List.class == rawType )
        {
            // List<T> --> T
            return Generics.getTypeArgument( expectedType, 0 );
        }
        return expectedType;
    }

    /**
     * Returns the role-hint binding annotation for the given Plexus component.
     * 
     * @param component The Plexus component
     * @return Role-hint binding annotation denoting the given component
     */
    public static Annotation roleHint( final Component component )
    {
        return roleHint( component.role(), component.hint() );
    }

    /**
     * Returns the canonical binding annotation for the given Plexus role-hint.
     * 
     * @param role The Plexus role
     * @param hint The Plexus hint
     * @return Canonical binding annotation denoting the given role-hint
     */
    public static Annotation roleHint( final Class<?> role, final String hint )
    {
        return Names.named( Hints.isDefaultHint( hint ) ? role.getName() : role.getName() + '-' + hint );
    }
}
