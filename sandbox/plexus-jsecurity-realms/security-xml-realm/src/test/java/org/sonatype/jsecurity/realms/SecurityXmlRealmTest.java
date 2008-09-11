package org.sonatype.jsecurity.realms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.realm.Realm;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.jsecurity.model.io.xpp3.SecurityConfigurationXpp3Writer;

public class SecurityXmlRealmTest
    extends PlexusTestCase
{
    public static final String PLEXUS_SECURITY_XML_FILE = "security-xml-file";
    
    private Realm realm;
        
    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );
        
        context.put( PLEXUS_SECURITY_XML_FILE, getBasedir() + "/target/jsecurity/security.xml" );
    }
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        realm = ( Realm ) lookup( Realm.class, "SecurityXmlRealm" );
    }
    
    public void testAuthentication()
        throws Exception
    {
        writeConfig( buildTestAuthenticationConfig() );
        
        UsernamePasswordToken upToken = new UsernamePasswordToken( "username", "password" );
        
        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );
    }
    
    private Configuration buildTestAuthenticationConfig()
    {
        Configuration config = new Configuration();
        
        CUser user = new CUser();
        user.setEmail( "dummyemail" );
        user.setName( "dummyname" );
        user.setStatus( CUser.STATUS_ACTIVE );
        user.setUserId( "username" );
        user.setPassword( "password" );
        
        config.addUser( user );
        
        return config;
    }
    
    private void writeConfig( Configuration configuration ) 
        throws ContextException, 
            IOException
    {
        String filename = ( String ) getContainer().getContext().get( PLEXUS_SECURITY_XML_FILE );
        
        File file = new File( filename );
        
        file.getParentFile().mkdirs();
        
        Writer fw = null;
        
        try
        {
            fw = new OutputStreamWriter( new FileOutputStream( file ) );

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
