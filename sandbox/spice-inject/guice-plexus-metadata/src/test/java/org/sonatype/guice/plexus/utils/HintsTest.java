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
package org.sonatype.guice.plexus.utils;

import junit.framework.TestCase;

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

    public void testHintsAreInterned()
    {
        assertSame( "hint", Hints.getCanonicalHint( new String( "hint" ) ) );
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

    public void testRoleHintKey()
    {
        assertEquals( "java.lang.String", Hints.getRoleHintKey( String.class, null ) );
        assertEquals( "java.lang.String", Hints.getRoleHintKey( String.class, "" ) );
        assertEquals( "java.lang.String", Hints.getRoleHintKey( String.class, new String( "default" ) ) );
        assertEquals( "java.lang.String-foo", Hints.getRoleHintKey( String.class, "foo" ) );
    }
}
