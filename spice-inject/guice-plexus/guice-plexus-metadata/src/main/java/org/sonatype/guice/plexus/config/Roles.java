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
import org.sonatype.guice.bean.reflect.TypeParameters;

import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Utility methods for dealing with Plexus roles.
 */
public final class Roles
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String MISSING_COMPONENT_ERROR = "No implementation for %s was bound.";

    private static final String MISSING_COMPONENT_WITH_HINT_ERROR =
        "No implementation for %s annotated with @Named(value=%s) was bound.";

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
        return Hints.isDefaultHint( hint ) ? role : role + ':' + hint;
    }

    /**
     * Returns the canonical role-hint for the given Plexus component.
     * 
     * @param component The Plexus component
     * @return Canonical role-hint denoting the given component
     */
    public static String canonicalRoleHint( final Component component )
    {
        return canonicalRoleHint( component.role().getName(), component.hint() );
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
            return TypeParameters.get( asType, 1 );
        }
        if ( List.class == rawType )
        {
            // List<T> --> T
            return TypeParameters.get( asType, 0 );
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
        if ( Hints.isDefaultHint( hint ) )
        {
            return Key.get( role );
        }
        return Key.get( role, Names.named( Hints.canonicalHint( hint ) ) );
    }

    /**
     * Returns the component binding {@link Key} for the given Plexus role-hint.
     * 
     * @param role The Plexus role
     * @param hint The Plexus hint
     * @return Component binding key denoting the given role-hint
     */
    @SuppressWarnings( "unchecked" )
    public static <T> Key<T> componentKey( final TypeLiteral<T> role, final String hint )
    {
        return (Key<T>) componentKey( role.getRawType(), hint );
    }

    /**
     * Throws a {@link ProvisionException} detailing the missing Plexus component.
     * 
     * @param role The Plexus role
     * @param hint The Plexus hint
     */
    public static <T> T throwMissingComponentException( final TypeLiteral<T> role, final String hint )
    {
        if ( Hints.isDefaultHint( hint ) )
        {
            throw new ProvisionException( String.format( MISSING_COMPONENT_ERROR, role ) );
        }
        throw new ProvisionException( String.format( MISSING_COMPONENT_WITH_HINT_ERROR, role, hint ) );
    }

    /**
     * Removes any non-Java identifiers from the name and converts it to camelCase.
     * 
     * @param name The element name
     * @return CamelCased name with no dashes
     */
    public static String camelizeName( final String name )
    {
        StringBuilder buf = null;

        final int length = name.length();
        for ( int i = 0; i < length; i++ )
        {
            if ( !Character.isJavaIdentifierPart( name.charAt( i ) ) )
            {
                buf = new StringBuilder( name.substring( 0, i ) );
                break;
            }
        }

        if ( null == buf )
        {
            return name; // nothing to camelize
        }

        boolean capitalize = true;
        for ( int i = buf.length() + 1; i < length; i++ )
        {
            final char c = name.charAt( i );
            if ( !Character.isJavaIdentifierPart( c ) )
            {
                capitalize = true;
            }
            else if ( capitalize )
            {
                buf.append( Character.toUpperCase( c ) );
                capitalize = false;
            }
            else
            {
                buf.append( c );
            }
        }

        return buf.toString();
    }
}
