package org.sonatype.jsecurity.locators;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.micromailer.EmailerConfiguration;

public class EmailConfigurationLocatorTest
    extends
    PlexusTestCase
{
    EmailConfigurationLocator emailConfig;
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        emailConfig = ( EmailConfigurationLocator ) lookup( EmailConfigurationLocator.class );
    }
    
    public void testDefault()
        throws Exception
    {
        EmailerConfiguration config = emailConfig.getConfiguration();
        
        assertTrue( config.getMailHost().equals( "mailhost" ) );
        assertTrue( config.getPassword().equals( "password" ) );
        assertTrue( config.getUsername().equals( "username" ) );
        assertTrue( config.getMailPort() == 1234 );
        assertTrue( config.isSsl() );
        assertTrue( config.isTls() );
    }
}
