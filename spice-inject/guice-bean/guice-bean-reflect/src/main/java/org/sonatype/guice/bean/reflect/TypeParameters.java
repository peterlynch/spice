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
package org.sonatype.guice.bean.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import com.google.inject.TypeLiteral;

/**
 * Utility methods for dealing with generic type parameters.
 */
public final class TypeParameters
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final TypeLiteral<?>[] NO_TYPE_LITERALS = {};

    private static final TypeLiteral<?> OBJECT_TYPE_LITERAL = TypeLiteral.get( Object.class );

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private TypeParameters()
    {
        // static utility class, not allowed to create instances
    }

    static
    {
        new TypeParameters(); // keep Cobertura coverage happy
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Get all type parameters from a generic type, for example {@code [Foo,Bar]} from {@code Map<Foo,Bar>}.
     * 
     * @param genericType The generic type
     * @return Array of type parameters
     */
    public static TypeLiteral<?>[] get( final TypeLiteral<?> genericType )
    {
        final Type type = genericType.getType();
        if ( type instanceof ParameterizedType )
        {
            final Type[] arguments = ( (ParameterizedType) type ).getActualTypeArguments();
            final TypeLiteral<?>[] parameters = new TypeLiteral[arguments.length];
            for ( int i = 0; i < arguments.length; i++ )
            {
                parameters[i] = expand( arguments[i] );
            }
            return parameters;
        }
        if ( type instanceof GenericArrayType )
        {
            return new TypeLiteral[] { expand( ( (GenericArrayType) type ).getGenericComponentType() ) };
        }
        return NO_TYPE_LITERALS;
    }

    /**
     * Get an indexed type parameter from a generic type, for example {@code Bar} from {@code Map<Foo,Bar>}.
     * 
     * @param genericType The generic type
     * @param index The parameter index
     * @return Indexed type parameter; {@code TypeLiteral<Object>} if the given type is a raw class
     */
    public static TypeLiteral<?> get( final TypeLiteral<?> genericType, final int index )
    {
        final Type type = genericType.getType();
        if ( type instanceof ParameterizedType )
        {
            return expand( ( (ParameterizedType) type ).getActualTypeArguments()[index] );
        }
        if ( type instanceof GenericArrayType )
        {
            if ( 0 == index )
            {
                return expand( ( (GenericArrayType) type ).getGenericComponentType() );
            }
            throw new ArrayIndexOutOfBoundsException( index );
        }
        return OBJECT_TYPE_LITERAL;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Expands wild-card types where possible, for example {@code Bar} from {@code ? extends Bar}.
     * 
     * @param type The generic type
     * @return Widened type that is still assignment-compatible with the original.
     */
    private static TypeLiteral<?> expand( final Type type )
    {
        return TypeLiteral.get( type instanceof WildcardType ? ( (WildcardType) type ).getUpperBounds()[0] : type );
    }
}
