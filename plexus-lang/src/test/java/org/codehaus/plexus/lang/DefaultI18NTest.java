/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
package org.codehaus.plexus.lang;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.codehaus.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact codehaus@codehaus.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.codehaus.org/>.
 */

import org.codehaus.plexus.PlexusTestCase;

import java.util.Locale;

/**
 * Tests the API of the
 * {@link org.codehaus.plexus.lang.I18N}.
 * <br>
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 */
public class DefaultI18NTest
    extends PlexusTestCase
{
    private I18N i18n;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        /* Set an unsupported locale to default to ensure we do not get unexpected matches */
        Locale.setDefault( new Locale( "jp" ) );

        i18n = (I18N) lookup( I18N.ROLE );
    }

    public void testLocalization()
    {
        String s0 = i18n.getString( null, null, "key1" );

        assertEquals( "Unable to retrieve localized text for locale: default", s0, "[] value1" );

        String s1 = i18n.getString( null, new Locale( "en", "US" ), "key2" );

        assertEquals( "Unable to retrieve localized text for locale: en-US", s1, "[en_US] value2" );

        String s2 = i18n.getString( "org.codehaus.plexus.lang.BarBundle", new Locale( "ko", "KR" ), "key3" );

        assertEquals( "Unable to retrieve localized text for locale: ko-KR", s2, "[ko] value3" );

        String s3 = i18n.getString( "org.codehaus.plexus.lang.BarBundle", new Locale( "ja" ), "key1" );

        assertEquals( "Unable to fall back from non-existant locale: jp", "[] value1", s3 );

        String s4 = i18n.getString( "org.codehaus.plexus.lang.FooBundle", new Locale( "fr" ), "key3" );

        assertEquals( "Unable to retrieve localized text for locale: fr", s4, "[fr] value3" );

        String s5 = i18n.getString( "org.codehaus.plexus.lang.FooBundle", new Locale( "fr", "FR" ), "key3" );

        assertEquals( "Unable to retrieve localized text for locale: fr-FR", s5, "[fr] value3" );

        String s6 = i18n.getString( "org.codehaus.plexus.lang.i18n", null, "key1" );

        assertEquals( "Unable to retrieve localized properties for locale: default", "[] value1", s6 );

        Locale old = Locale.getDefault();
        Locale.setDefault(Locale.FRENCH);
        try
        {
            String s7 = i18n.getString( "org.codehaus.plexus.lang.i18n", Locale.ENGLISH, "key1" );

            assertEquals( "Not picking up new default locale: fr", "[fr] value1", s7 );

            String s8 = i18n.getString( "org.codehaus.plexus.lang.i18n", Locale.ITALIAN, "key1" );

            assertEquals( "Unable to retrieve localized properties for locale: it", "[it] value1", s8 );

        } finally {
            Locale.setDefault(old);
        }
    }

    public void testLocalizedMessagesWithFormatting()
    {
        // Format methods

        String s6 = i18n.format( "org.codehaus.plexus.lang.i18n", null, "thanks.message", "jason" );

        assertEquals( s6, "Thanks jason!" );

        String s7 = i18n.format( "org.codehaus.plexus.lang.i18n", null, "thanks.message1", "jason", "van zyl" );

        assertEquals( s7, "Thanks jason van zyl!" );

        String s8 = i18n.format( "org.codehaus.plexus.lang.i18n", null, "thanks.message2", new Object[]{ "jason", "van zyl" } );

        assertEquals( s8, "Thanks jason van zyl!" );
    }

    public void testLocalizedMessagesWithNonStandardLocale()
    {
        String s0 = i18n.getString( "name", new Locale( "xx" ) );

        assertEquals( "plexus", s0 );
    }
}
