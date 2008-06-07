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