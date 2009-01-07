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
package org.sonatype.webdav.security;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.webdav.security.Authentication;
import org.sonatype.webdav.security.PropertiesAuthentication;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class PropertiesAuthenticationTest
    extends PlexusTestCase
{
    PropertiesAuthentication authent;

    Map<String, String> session;

    public void setUp()
        throws Exception
    {
        super.setUp();

        authent = (PropertiesAuthentication) lookup( Authentication.class.getName(), "properties" );

        session = new HashMap<String, String>();
    }

    public void testValidLogin()
        throws Exception
    {
        assertNotNull( authent.authenticate( "andy", "williams", session ) );
    }

    public void testMissingUser()
        throws Exception
    {
        assertNull( authent.authenticate( "missing", "missing", session ) );
    }

    public void testWrongPassword()
        throws Exception
    {
        assertNull( authent.authenticate( "andy", "wrong", session ) );
    }
}