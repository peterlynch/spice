package org.sonatype.jsecurity.realms;

import java.io.File;
import java.util.Collection;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.subject.SimplePrincipalCollection;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.DefaultConfigurationManager;
import org.sonatype.jsecurity.realms.tools.StringDigester;

public class DefaultPlexusSecurityTest
    extends
    PlexusTestCase
{
    public static final String PLEXUS_SECURITY_XML_FILE = "security-xml-file";

    private static final String SECURITY_CONFIG_FILE_PATH = getBasedir() + "/target/jsecurity/security.xml";

    public static final String LOCATOR_PROPERTY_FILE = "realm-locator-property-file";

    private File configFile = new File( SECURITY_CONFIG_FILE_PATH );

    private PlexusSecurity security;

    private DefaultConfigurationManager configurationManager;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        security = (PlexusSecurity) lookup( PlexusSecurity.class );

        configurationManager = (DefaultConfigurationManager) lookup( ConfigurationManager.ROLE );

        configurationManager.clearCache();

        configFile.delete();
    }

    @Override
    protected void customizeContext( Context context )
    {
        context.put( LOCATOR_PROPERTY_FILE, getBasedir() + "/target/test-classes/realm-locator.properties" );

        context.put( PLEXUS_SECURITY_XML_FILE, SECURITY_CONFIG_FILE_PATH );
    }

    public void testSuccessfulAuthentication()
        throws Exception
    {
        buildTestAuthenticationConfig( CUser.STATUS_ACTIVE );

        UsernamePasswordToken upToken = new UsernamePasswordRealmToken( "username", "password", SecurityXmlRealm.class.getName() );

        AuthenticationInfo ai = security.authenticate( upToken );

        String password = new String( (char[]) ai.getCredentials() );

        assertEquals( StringDigester.getSha1Digest( "password" ), password );
    }

    public void testFailedAuthentication()
        throws Exception
    {
        buildTestAuthenticationConfig( CUser.STATUS_ACTIVE );

        UsernamePasswordToken upToken = new UsernamePasswordRealmToken( "username", "badpassword", SecurityXmlRealm.class.getName() );

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

    public void testDisabledAuthentication()
        throws Exception
    {
        buildTestAuthenticationConfig( CUser.STATUS_DISABLED );

        UsernamePasswordToken upToken = new UsernamePasswordRealmToken( "username", "password", SecurityXmlRealm.class.getName() );

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

    public void testInavlidStatusAuthentication()
        throws Exception
    {
        buildTestAuthenticationConfig( "junk" );

        UsernamePasswordToken upToken = new UsernamePasswordRealmToken( "username", "password", SecurityXmlRealm.class.getName() );

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
        buildTestAuthorizationConfig();
        
        assertTrue( security.isPermitted(
            new SimplePrincipalCollection( "username", SecurityXmlRealm.class.getName() ),
            new WildcardPermission( "app:config" ) ) );
        
        assertFalse( security.isPermitted(
            new SimplePrincipalCollection( "username", SecurityXmlRealm.class.getName() ),
            new WildcardPermission( "app:ui" ) ) );
    }
    
    public void testOtherRealmAuthorization()
        throws Exception
    {
        buildTestOtherAuthorizationConfig();
        
        assertTrue( security.isPermitted(
            new SimplePrincipalCollection( "username", MethodRealm.class.getName() ),
            new WildcardPermission( "app:config:read" ) ) );
         
        assertFalse( security.isPermitted(
            new SimplePrincipalCollection( "username", MethodRealm.class.getName() ),
            new WildcardPermission( "app:config:create" ) ) );
    }

    private void buildTestAuthenticationConfig( String status )
    {
        CUser user = new CUser();
        user.setEmail( "dummyemail" );
        user.setName( "dummyname" );
        user.setStatus( status );
        user.setId( "username" );
        user.setPassword( StringDigester.getSha1Digest( "password" ) );

        configurationManager.createUser( user );

        configurationManager.save();
    }

    private void buildTestAuthorizationConfig()
    {
        CProperty permissionProp = new CProperty();
        permissionProp.setKey( "permission" );
        permissionProp.setValue( "app:config" );

        CPrivilege priv = new CPrivilege();
        priv.setId( "priv" );
        priv.setName( "somepriv" );
        priv.setDescription( "somedescription" );
        priv.addProperty( permissionProp );

        configurationManager.createPrivilege( priv );

        CRole role = new CRole();
        role.setId( "role" );
        role.setName( "somerole" );
        role.setDescription( "somedescription" );
        role.setSessionTimeout( 60 );
        role.addPrivilege( priv.getId() );

        configurationManager.createRole( role );

        CUser user = new CUser();
        user.setEmail( "dummyemail" );
        user.setName( "dummyname" );
        user.setStatus( CUser.STATUS_ACTIVE );
        user.setId( "username" );
        user.setPassword( StringDigester.getSha1Digest( "password" ) );
        user.addRole( role.getId() );

        configurationManager.createUser( user );

        configurationManager.save();
    }
    
    private void buildTestOtherAuthorizationConfig()
    {
        CProperty permissionProp = new CProperty();
        permissionProp.setKey( "permission" );
        permissionProp.setValue( "app:config" );
        
        CProperty methodProp = new CProperty();
        methodProp.setKey( "method" );
        methodProp.setValue( "read" );
        
        CPrivilege priv = new CPrivilege();
        priv.setId( "priv" );
        priv.setName( "somepriv" );
        priv.setDescription( "somedescription" );
        priv.addProperty( permissionProp );
        priv.addProperty( methodProp );
        
        configurationManager.createPrivilege( priv );
        
        CRole role = new CRole();
        role.setId( "role" );
        role.setName( "somerole" );
        role.setDescription( "somedescription" );
        role.setSessionTimeout( 60 );
        role.addPrivilege( priv.getId() );
        
        configurationManager.createRole( role );
        
        CUser user = new CUser();
        user.setEmail( "dummyemail" );
        user.setName( "dummyname" );
        user.setStatus( CUser.STATUS_ACTIVE );
        user.setId( "username" );
        user.setPassword( StringDigester.getSha1Digest( "password" ) );
        user.addRole( role.getId() );
        
        configurationManager.createUser( user );
        
        configurationManager.save();
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
