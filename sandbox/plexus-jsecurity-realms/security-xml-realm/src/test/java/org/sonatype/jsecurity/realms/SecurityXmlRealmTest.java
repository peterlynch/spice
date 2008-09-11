package org.sonatype.jsecurity.realms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.realm.Realm;
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
    
    private Realm realm;
        
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
        
        realm = ( Realm ) lookup( Realm.class, "SecurityXmlRealm" );
        
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
            AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );
            
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
            AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );
            
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
            AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );
            
            fail( "Authentication should have failed" );
        }
        catch( AuthenticationException e )
        {
            // good
        }
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
}
