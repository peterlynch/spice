package org.sonatype.micromailer.imp;

import java.util.Collection;
import java.util.Map;

import org.sonatype.micromailer.MailType;
import org.sonatype.micromailer.MailTypeSource;

/**
 * A mail type source.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultMailTypeSource
    implements MailTypeSource
{
    /**
     * @plexus.requirement role="org.sonatype.micromailer.MailType"
     */
    private Map<String, MailType> mailTypes;

    public Collection<MailType> getKnownMailTypes()
    {
        return mailTypes.values();
    }

    public MailType getMailType( String id )
    {
        if ( mailTypes.containsKey( id ) )
        {
            return mailTypes.get( id );
        }
        else
        {
            return null;
        }
    }
}
