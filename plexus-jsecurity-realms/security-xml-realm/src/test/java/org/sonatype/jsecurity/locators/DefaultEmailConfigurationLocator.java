package org.sonatype.jsecurity.locators;

import org.sonatype.micromailer.EmailerConfiguration;

public class DefaultEmailConfigurationLocator
    implements EmailConfigurationLocator
{
    public EmailerConfiguration getConfiguration()
    {
        EmailerConfiguration config = new EmailerConfiguration();
        config.setUsername( "username" );
        config.setPassword( "password" );
        config.setMailHost( "mailhost" );
        config.setMailPort( 1234 );
        config.setSsl( true );
        config.setTls( true );
        
        return config;
    }
}
