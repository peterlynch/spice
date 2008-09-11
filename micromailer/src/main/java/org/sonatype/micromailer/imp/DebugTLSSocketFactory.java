package org.sonatype.micromailer.imp;

import javax.net.SocketFactory;

/**
 * Debug TLS Socket factory. A "naive" one, that will eat SelfSigned ones too, but NOT FOR PRODUCTION USE!
 * 
 * @author cstamas
 * @see http://www.howardism.org/Technical/Java/SelfSignedCerts.html
 */
public class DebugTLSSocketFactory
    extends AbstractDebugSecureSocketFactory
{
    public DebugTLSSocketFactory()
    {
        super( "TLS" );
    }

    public static SocketFactory getDefault()
    {
        return new DebugTLSSocketFactory();
    }
}
