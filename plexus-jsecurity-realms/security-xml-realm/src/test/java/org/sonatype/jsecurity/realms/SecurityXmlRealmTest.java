package org.sonatype.jsecurity.realms;

import java.io.File;
import java.util.Collection;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.SimplePrincipalCollection;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.DefaultConfigurationManager;
import org.sonatype.jsecurity.realms.tools.StringDigester;

public class SecurityXmlRealmTest
    extends PlexusTestCase
{
    public static final String PLEXUS_SECURITY_XML_FILE = "security-xml-file";
    
    private static final String SECURITY_CONFIG_FILE_PATH = getBasedir() + "/target/jsecurity/security.xml"; 
    
    private File configFile = new File( SECURITY_CONFIG_FILE_PATH );
    
    private SecurityXmlRealm realm;
    
    private DefaultConfigurationManager configurationManager;
        
    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );
        
        context.put( PLEXUS_SECURITY_XML_FILE, SECURITY_CONFIG_FILE_PATH );
    }
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        realm = ( SecurityXmlRealm ) lookup( Realm.class, "SecurityXmlRealm" );
        
        configurationManager = ( DefaultConfigurationManager ) lookup( ConfigurationManager.ROLE );
        
        configurationManager.clearCache();
        
        configFile.delete();
    }
    
    public void testSuccessfulAuthentication()
        throws Exception
    {
        buildTestAuthenticationConfig( CUser.STATUS_ACTIVE );
        
        UsernamePasswordToken upToken = new UsernamePasswordToken( "username", "password" );
        
        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );
        
        String password = new String( (char[] ) ai.getCredentials() );
        
        assertEquals( StringDigester.getSha1Digest( "password" ), password );        
    }
    
    public void testFailedAuthentication()
        throws Exception
    {
        buildTestAuthenticationConfig( CUser.STATUS_ACTIVE );
        
        UsernamePasswordToken upToken = new UsernamePasswordToken( "username", "badpassword" );
        
        try
        {
            realm.getAuthenticationInfo( upToken );
            
            fail( "Authentication should have failed" );
        }
        catch( AuthenticationException e )
        {
            // good
        }   
    }
    
    public void testDisabledAuthentication()
        throws Exception
    {
        buildTestAuthenticationConfig( CUser.STATUS_DISABLED );
        
        UsernamePasswordToken upToken = new UsernamePasswordToken( "username", "password" );
        
        try
        {
            realm.getAuthenticationInfo( upToken );
            
            fail( "Authentication should have failed" );
        }
        catch( AuthenticationException e )
        {
            // good
        }
    }
    
    public void testInavlidStatusAuthentication()
        throws Exception
    {
        buildTestAuthenticationConfig( "junk" );
        
        UsernamePasswordToken upToken = new UsernamePasswordToken( "username", "password" );
        
        try
        {
            realm.getAuthenticationInfo( upToken );
            
            fail( "Authentication should have failed" );
        }
        catch( AuthenticationException e )
        {
            // good
        }
    }
    
    public void testAuthorization()
        throws Exception
    {
        buildTestAuthorizationConfig();
        
        AuthorizationInfo ai = realm.getAuthorizationInfo( new SimplePrincipalCollection( "username", realm.getName() ) );
        
        assertEquals( ai.getRoles().size(), 1 );
        
        assertEquals( ai.getRoles().iterator().next(), "role" );
        
        Collection<Permission> permissions = ai.getObjectPermissions();
        
        // Verify the permission
        assertImplied( new WildcardPermission( "app:config" ), permissions );
        
        // Verify other permission not allowed
        assertNotImplied( new WildcardPermission( "app:ui" ), permissions );
    }
    
    public void testMutability()
        throws Exception
    {
        buildTestAuthorizationConfig();
        
        AuthorizationInfo ai = realm.getAuthorizationInfo( new SimplePrincipalCollection( "username", realm.getName() ) );
        
        assertEquals( ai.getRoles().size(), 1 );
        
        assertEquals( ai.getRoles().iterator().next(), "role" );
        
        Collection<Permission> permissions = ai.getObjectPermissions();
        
        assertImplied( new WildcardPermission( "app:config" ), permissions );
        assertNotImplied( new WildcardPermission( "app:ui" ), permissions );
        
        updateTestAuthorizationConfig();
        
        // Still same, cache not cleared
        ai = realm.getAuthorizationInfo( new SimplePrincipalCollection( "username", realm.getName() ) );
        
        assertEquals( ai.getRoles().size(), 1 );
        
        assertEquals( ai.getRoles().iterator().next(), "role" );
        
        permissions = ai.getObjectPermissions();
        
        assertImplied( new WildcardPermission( "app:config" ), permissions );
        assertNotImplied( new WildcardPermission( "app:ui" ), permissions );
        
        // Clear the cache and should now show changes
        realm.clearCache();
        
        ai = realm.getAuthorizationInfo( new SimplePrincipalCollection( "username", realm.getName() ) );
        
        assertEquals( ai.getRoles().size(), 1 );
        
        assertEquals( ai.getRoles().iterator().next(), "role" );
        
        permissions = ai.getObjectPermissions();
        
        assertNotImplied( new WildcardPermission( "app:config" ), permissions );
        assertImplied( new WildcardPermission( "app:ui" ), permissions );
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
    
    private void updateTestAuthorizationConfig()
    {
        CPrivilege priv = configurationManager.readPrivilege( "priv" );
        
        assertTrue( priv != null );
        
        ( ( CProperty ) priv.getProperties().get( 0 ) ).setValue( "app:ui" );
        
        configurationManager.updatePrivilege( priv );
        
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
