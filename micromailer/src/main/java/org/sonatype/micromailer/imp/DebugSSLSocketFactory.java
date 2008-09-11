package org.sonatype.micromailer.imp;

import javax.net.SocketFactory;

/**
 * Debug SSL Socket factory. A "naive" one, that will eat SelfSigned ones too, but NOT FOR PRODUCTION USE!
 * 
 * @author cstamas
 * @see http://www.howardism.org/Technical/Java/SelfSignedCerts.html
 */
public class DebugSSLSocketFactory
    extends AbstractDebugSecureSocketFactory
{
    public DebugSSLSocketFactory()
    {
        super( "SSL" );
    }

    public static SocketFactory getDefault()
    {
        return new DebugSSLSocketFactory();
    }
}
