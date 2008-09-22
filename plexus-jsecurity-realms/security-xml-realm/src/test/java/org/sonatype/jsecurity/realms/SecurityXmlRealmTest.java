package org.sonatype.jsecurity.realms;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.realm.Realm;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.DefaultConfigurationManager;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
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
    
    private void buildTestAuthenticationConfig( String status ) throws InvalidConfigurationException
    {
        CPrivilege priv = new CPrivilege();
        priv.setId( "priv" );
        priv.setName( "name" );
        priv.setDescription( "desc" );
        priv.setType( "method" );
        
        CProperty prop = new CProperty();
        prop.setKey( "method" );
        prop.setValue( "read" );
        priv.addProperty( prop );
        
        prop = new CProperty();
        prop.setKey( "permission" );
        prop.setValue( "somevalue" );
        priv.addProperty( prop );
        
        configurationManager.createPrivilege( priv );
        
        CRole role = new CRole();
        role.setName( "name" );
        role.setId( "role" );
        role.setDescription( "desc" );
        role.setSessionTimeout( 50 );
        role.addPrivilege( "priv" );
        
        configurationManager.createRole( role );
        
        CUser user = new CUser();
        user.setEmail( "dummyemail" );
        user.setName( "dummyname" );
        user.setStatus( status );
        user.setId( "username" );
        user.setPassword( StringDigester.getSha1Digest( "password" ) );
        user.addRole( "role" );
        
        configurationManager.createUser( user );
        
        configurationManager.save();
    }
}
