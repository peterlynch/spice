package org.sonatype.micromailer;

import java.io.IOException;

/**
 * A storage for mail requests.
 * 
 * @author cstamas
 */
public interface MailStorage
{
    MailRequest loadMailRequest( String requestId )
        throws IOException;

    void saveMailRequest( MailRequest request )
        throws IOException;
}
