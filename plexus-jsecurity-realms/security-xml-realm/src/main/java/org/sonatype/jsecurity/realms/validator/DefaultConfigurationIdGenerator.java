package org.sonatype.jsecurity.realms.validator;

import java.util.Random;

import org.codehaus.plexus.component.annotations.Component;

@Component( role = ConfigurationIdGenerator.class )
public class DefaultConfigurationIdGenerator
    implements ConfigurationIdGenerator
{
    private Random rand = new Random( System.currentTimeMillis() );

    public String generateId()
    {
        return Long.toHexString( System.nanoTime() + rand.nextInt( 2008 ) );
    }

}
