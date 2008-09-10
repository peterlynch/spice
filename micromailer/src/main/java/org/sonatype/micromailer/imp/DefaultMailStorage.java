package org.sonatype.micromailer.imp;

import java.io.IOException;

import org.sonatype.micromailer.MailRequest;
import org.sonatype.micromailer.MailStorage;

/**
 * A "default" (a doing nothing) MailStorage implementation.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultMailStorage
    implements MailStorage
{
    public MailRequest loadMailRequest( String requestId )
        throws IOException
    {
        throw new UnsupportedOperationException( "NullMailStorage is unable to load mail!" );
    }

    public void saveMailRequest( MailRequest request )
        throws IOException
    {
        // silently do a big nothing
    }
}
