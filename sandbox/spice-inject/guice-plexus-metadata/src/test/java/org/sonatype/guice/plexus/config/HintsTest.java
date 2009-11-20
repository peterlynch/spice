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

import java.util.Arrays;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.plexus.annotations.RequirementImpl;

public class HintsTest
    extends TestCase
{
    public void testCanonicalHint()
    {
        assertEquals( "default", Hints.getCanonicalHint( null ) );
        assertEquals( "default", Hints.getCanonicalHint( "" ) );
        assertEquals( "default", Hints.getCanonicalHint( new String( "default" ) ) );
        assertEquals( "foo", Hints.getCanonicalHint( "foo" ) );
    }

    public void testCanonicalHints()
    {
        assertArrayEquals( new String[0], Hints.getCanonicalHints( requirement() ) );
        assertArrayEquals( new String[0], Hints.getCanonicalHints( requirement( "" ) ) );
        assertArrayEquals( new String[] { "default" }, Hints.getCanonicalHints( requirement( "default" ) ) );
        assertArrayEquals( new String[] { "foo" }, Hints.getCanonicalHints( requirement( "foo" ) ) );
        assertArrayEquals( new String[] { "default", "foo" }, Hints.getCanonicalHints( requirement( "", "foo" ) ) );
        assertArrayEquals( new String[] { "foo", "default" }, Hints.getCanonicalHints( requirement( "foo", "" ) ) );
    }

    public void testHintsAreInterned()
    {
        assertSame( "hint", Hints.getCanonicalHint( new String( "hint" ) ) );
        assertSame( "hint", Hints.getCanonicalHints( requirement( new String( "hint" ) ) )[0] );
        final Requirement requirement = requirement( new String( "foo" ), new String( "bar" ) );
        assertSame( "foo", Hints.getCanonicalHints( requirement )[0] );
        assertSame( "bar", Hints.getCanonicalHints( requirement )[1] );
        assertNotSame( new String( "hint" ), Hints.getCanonicalHint( "hint" ) );
        assertEquals( new String( "hint" ), Hints.getCanonicalHint( "hint" ) );
    }

    public void testIsDefaultHint()
    {
        assertTrue( Hints.isDefaultHint( null ) );
        assertTrue( Hints.isDefaultHint( "" ) );
        assertTrue( Hints.isDefaultHint( new String( "default" ) ) );
        assertFalse( Hints.isDefaultHint( "foo" ) );
    }

    private static <T> void assertArrayEquals( final T[] a, final T[] b )
    {
        assertTrue( "Expected: " + Arrays.toString( a ) + "but was: " + Arrays.toString( b ), Arrays.equals( a, b ) );
    }

    private static Requirement requirement( final String... hints )
    {
        return new RequirementImpl( Roles.defer( Object.class ), true, hints );
    }
}
