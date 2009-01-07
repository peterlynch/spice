/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
import org.sonatype.webdav.security.Authorization;
import org.sonatype.webdav.security.Permission;
import org.sonatype.webdav.security.PropertiesAuthorization;
import org.sonatype.webdav.security.SimpleUser;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class PropertiesAuthorizationTest
    extends PlexusTestCase
{
    PropertiesAuthorization auth;

    public void setUp()
        throws Exception
    {
        super.setUp();

        auth = (PropertiesAuthorization) lookup( Authorization.class.getName(), "properties" );
    }

    public void testValidLogin()
        throws Exception
    {
        assertTrue( auth.authorize( new SimpleUser( "andy" ), Permission.PERMISSION_REPOSITORY_READ) );
    }

    public void testMissingUser()
        throws Exception
    {
        assertFalse( auth.authorize( new SimpleUser( "missing" ), Permission.PERMISSION_REPOSITORY_READ) );
    }

    public void testMissingRole()
        throws Exception
    {
        assertFalse( auth.authorize( new SimpleUser( "andy" ), new Permission( "missing-role" ) ) );
    }
}
