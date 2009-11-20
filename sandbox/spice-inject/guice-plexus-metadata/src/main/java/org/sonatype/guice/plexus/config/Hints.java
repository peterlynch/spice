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

import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Constants and utility methods for dealing with Plexus hints.
 */
public final class Hints
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    public static final String[] NO_HINTS = {};

    public static final String DEFAULT_HINT = "default";

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    private Hints()
    {
        // static utility class, not allowed to create instances
    }

    static
    {
        new Hints(); // keep Cobertura coverage happy
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Returns the canonical form of the given Plexus hint.
     * 
     * @param hint The Plexus hint
     * @return Canonical hint denoting the same component as the given hint
     */
    public static String getCanonicalHint( final String hint )
    {
        // interning hints is a good idea because there's a lot of duplication
        return null == hint || hint.length() == 0 ? DEFAULT_HINT : hint.intern();
    }

    /**
     * Determines if the given Plexus hint denotes the default component.
     * 
     * @param hint The Plexus hint
     * @return {@code true} if the given hint denotes the default component
     */
    public static boolean isDefaultHint( final String hint )
    {
        return DEFAULT_HINT.equals( hint ) || null == hint || hint.length() == 0;
    }

    /**
     * Returns the canonical form of the given Plexus hints.
     * 
     * @param hints The Plexus hints
     * @return Array of canonical hints
     */
    public static String[] getCanonicalHints( final String... hints )
    {
        for ( int i = 0; i < hints.length; i++ )
        {
            hints[i] = getCanonicalHint( hints[i] );
        }
        return hints;
    }

    /**
     * Returns the Plexus hints contained in the given @{@link Requirement}.
     * 
     * @param requirement The Plexus requirement
     * @return Array of canonical hints
     */
    public static String[] getCanonicalHints( final Requirement requirement )
    {
        final String[] hints = requirement.hints();
        if ( hints.length > 0 )
        {
            return getCanonicalHints( hints );
        }
        final String hint = requirement.hint();
        if ( hint.length() > 0 )
        {
            return getCanonicalHints( hint );
        }
        return NO_HINTS;
    }
}
