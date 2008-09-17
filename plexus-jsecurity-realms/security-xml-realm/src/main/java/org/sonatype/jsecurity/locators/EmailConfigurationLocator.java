package org.sonatype.jsecurity.locators;

import org.sonatype.micromailer.EmailerConfiguration;

public interface EmailConfigurationLocator
{
    String ROLE = EmailConfigurationLocator.class.getName();
    
    EmailerConfiguration getConfiguration();
}
