package org.sonatype.jsecurity.realms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.SimplePrincipalCollection;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.jsecurity.model.io.xpp3.SecurityConfigurationXpp3Writer;
import org.sonatype.jsecurity.realms.tools.StringDigester;

public class SecurityXmlRealmTest
    extends PlexusTestCase
{
    public static final String PLEXUS_SECURITY_XML_FILE = "security-xml-file";
    
    private static final String SECURITY_CONFIG_FILE_PATH = getBasedir() + "/target/jsecurity/security.xml"; 
    
    private File configFile = new File( SECURITY_CONFIG_FILE_PATH );
    
    private SecurityXmlRealm realm;
        
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
        
        configFile.delete();
    }
    
    public void testSuccessfulAuthentication()
        throws Exception
    {
        writeConfig( buildTestAuthenticationConfig() );
        
        UsernamePasswordToken upToken = new UsernamePasswordToken( "username", "password" );
        
        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );
        
        String password = new String( (char[] ) ai.getCredentials() );
        
        assertEquals( StringDigester.getSha1Digest( "password" ), password );        
    }
    
    public void testFailedAuthentication()
        throws Exception
    {
        writeConfig( buildTestAuthenticationConfig() );
        
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
        Configuration config = buildTestAuthenticationConfig();
        
        ( ( CUser )config.getUsers().get( 0 ) ).setStatus( CUser.STATUS_DISABLED );
        
        writeConfig( config );
        
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
        Configuration config = buildTestAuthenticationConfig();
        
        ( ( CUser )config.getUsers().get( 0 ) ).setStatus( "junk" );
        
        writeConfig( config );
        
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
        writeConfig( buildTestAuthorizationConfig() );
        
        AuthorizationInfo ai = realm.getAuthorizationInfo( new SimplePrincipalCollection( "username", realm.getName() ) );
        
        assertEquals( ai.getRoles().size(), 1 );
        
        assertEquals( ai.getRoles().iterator().next(), "role" );
        
        Collection<Permission> permissions = ai.getObjectPermissions();
        
        // Verify the sole read privilege, and all other methods not pass (create/update/delete)
        assertNotImplied( new WildcardPermission( "app:config:create" ), permissions );
        assertImplied( new WildcardPermission( "app:config:read" ), permissions );
        assertNotImplied( new WildcardPermission( "app:config:update" ), permissions );
        assertNotImplied( new WildcardPermission( "app:config:delete" ), permissions );
        
        // Verify other privilege not allowed, even though same method
        assertNotImplied( new WildcardPermission( "app:ui:create" ), permissions );
        assertNotImplied( new WildcardPermission( "app:ui:read" ), permissions );
        assertNotImplied( new WildcardPermission( "app:ui:update" ), permissions );
        assertNotImplied( new WildcardPermission( "app:ui:delete" ), permissions );
    }
    
    private Configuration buildTestAuthenticationConfig()
    {
        Configuration config = new Configuration();
        
        CUser user = new CUser();
        user.setEmail( "dummyemail" );
        user.setName( "dummyname" );
        user.setStatus( CUser.STATUS_ACTIVE );
        user.setUserId( "username" );
        user.setPassword( StringDigester.getSha1Digest( "password" ) );
        
        config.addUser( user );
        
        return config;
    }
    
    private Configuration buildTestAuthorizationConfig()
    {
        Configuration config = new Configuration();
        
        CPrivilege priv = new CPrivilege();
        priv.setId( "priv" );
        priv.setName( "somepriv" );
        priv.setDescription( "somedescription" );
        priv.setMethod( "read" );
        priv.setPermission( "app:config" );
        
        CRole role = new CRole();
        role.setId( "role" );
        role.setName( "somerole" );
        role.setDescription( "somedescription" );
        role.setSessionTimeout( 60 );
        role.addPrivilege( priv.getId() );
        
        CUser user = new CUser();
        user.setEmail( "dummyemail" );
        user.setName( "dummyname" );
        user.setStatus( CUser.STATUS_ACTIVE );
        user.setUserId( "username" );
        user.setPassword( StringDigester.getSha1Digest( "password" ) );
        user.addRole( role.getId() );
        
        config.addPrivilege( priv );
        config.addRole( role );
        config.addUser( user );
        
        return config;
    }
    
    private void writeConfig( Configuration configuration ) 
        throws ContextException, 
            IOException
    {
        configFile.getParentFile().mkdirs();
        
        Writer fw = null;
        
        try
        {
            fw = new OutputStreamWriter( new FileOutputStream( configFile ) );

            SecurityConfigurationXpp3Writer writer = new SecurityConfigurationXpp3Writer();

            writer.write( fw, configuration );
        }
        finally
        {
            if ( fw != null )
            {
                fw.flush();

                fw.close();
            }
        }
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
