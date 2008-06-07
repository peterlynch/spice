/**
  * Copyright (C) 2008 Sonatype Inc. 
  * Sonatype Inc, licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in 
  * compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
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
