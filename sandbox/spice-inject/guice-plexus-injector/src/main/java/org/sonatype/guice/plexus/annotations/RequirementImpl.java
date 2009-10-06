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
package org.sonatype.guice.plexus.annotations;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Runtime implementation of Plexus @{@link Requirement} annotation.
 */
public final class RequirementImpl
    implements Requirement
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String[] EMPTY_HINTS = {};

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Class<?> role;

    private final String hint;

    private final String[] hints;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public RequirementImpl( final Class<?> role, final String... hints )
    {
        if ( null == role || null == hints || Arrays.asList( hints ).contains( null ) )
        {
            throw new IllegalArgumentException( "@Requirement cannot contain null values" );
        }

        this.role = role;

        if ( hints.length == 1 )
        {
            hint = hints[0];
            this.hints = EMPTY_HINTS;
        }
        else
        {
            hint = "";
            this.hints = hints;
        }
    }

    // ----------------------------------------------------------------------
    // Annotation properties
    // ----------------------------------------------------------------------

    public Class<?> role()
    {
        return role;
    }

    public String hint()
    {
        return hint;
    }

    public String[] hints()
    {
        return hints;
    }

    // ----------------------------------------------------------------------
    // Standard annotation behaviour
    // ----------------------------------------------------------------------

    @Override
    public int hashCode()
    {
        return ( 127 * "role".hashCode() ^ role.hashCode() ) + ( 127 * "hint".hashCode() ^ hint.hashCode() )
            + ( 127 * "hints".hashCode() ^ Arrays.hashCode( hints ) );
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( rhs instanceof Requirement )
        {
            final Requirement req = (Requirement) rhs;

            return role.equals( req.role() ) && hint.equals( req.hint() ) && Arrays.equals( hints, req.hints() );
        }

        return false;
    }

    @Override
    public String toString()
    {
        return String.format( "@%s(hints=%s, role=%s, hint=%s)", Requirement.class.getName(), Arrays.toString( hints ),
                              role, hint );
    }

    public Class<? extends Annotation> annotationType()
    {
        return Requirement.class;
    }
}