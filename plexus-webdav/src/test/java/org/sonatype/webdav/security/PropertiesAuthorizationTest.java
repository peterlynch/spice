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
