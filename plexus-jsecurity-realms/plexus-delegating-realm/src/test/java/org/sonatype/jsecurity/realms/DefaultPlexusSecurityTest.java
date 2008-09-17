package org.sonatype.jsecurity.realms;

import java.util.Collection;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.subject.SimplePrincipalCollection;

public class DefaultPlexusSecurityTest
    extends
    PlexusTestCase
{
    public static final String LOCATOR_PROPERTY_FILE = "realm-locator-property-file";

    private PlexusSecurity security;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        security = (PlexusSecurity) lookup( PlexusSecurity.class );
    }

    @Override
    protected void customizeContext( Context context )
    {
        context.put( LOCATOR_PROPERTY_FILE, getBasedir() + "/target/test-classes/realm-locator.properties" );
    }

    public void testSuccessfulAuthentication()
        throws Exception
    {
        UsernamePasswordToken upToken = new UsernamePasswordRealmToken( "username", "password", FakeRealm.class.getName() );

        AuthenticationInfo ai = security.authenticate( upToken );

        String password = ( String ) ai.getCredentials();

        assertEquals( "password", password );
    }

    public void testFailedAuthentication()
        throws Exception
    {
        UsernamePasswordToken upToken = new UsernamePasswordRealmToken( "username", "badpassword", FakeRealm.class.getName() );

        try
        {
            security.authenticate( upToken );

            fail( "Authentication should have failed" );
        }
        catch ( AuthenticationException e )
        {
            // good
        }
    }

    public void testAuthorization()
        throws Exception
    {   
        assertTrue( security.isPermitted(
            new SimplePrincipalCollection( "username", FakeRealm.class.getName() ),
            new WildcardPermission( "test:perm" ) ) );
        
        assertFalse( security.isPermitted(
            new SimplePrincipalCollection( "username", FakeRealm.class.getName() ),
            new WildcardPermission( "other:perm" ) ) );
    }

    public static void assertImplied( Permission testPermission, Collection<Permission> assignedPermissions )
    {
        for ( Permission assignedPermission : assignedPermissions )
        {
            if ( assignedPermission.implies( testPermission ) )
            {
                return;
            }
        }
        fail( "Expected " + testPermission + " to be implied by " + assignedPermissions );
    }

    public static void assertNotImplied( Permission testPermission, Collection<Permission> assignedPermissions )
    {
        for ( Permission assignedPermission : assignedPermissions )
        {
            if ( assignedPermission.implies( testPermission ) )
            {
                fail( "Expected " + testPermission + " not to be implied by " + assignedPermission );
            }
        }
    }
}
