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