package org.sonatype.micromailer;

import java.util.Collection;

/**
 * A registry that holds count of existing mail types.
 * 
 * @author cstamas
 */
public interface MailTypeSource
{
    Collection<MailType> getKnownMailTypes();

    MailType getMailType( String id );
}
