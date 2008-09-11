package org.sonatype.micromailer.imp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * Debug SSL Socket factory. A "naive" one, that will eat SelfSigned ones too, but NOT FOR PRODUCTION USE!
 * 
 * @author cstamas
 * @see http://www.howardism.org/Technical/Java/SelfSignedCerts.html
 */
public abstract class AbstractDebugSecureSocketFactory
    extends SSLSocketFactory
{
    private SSLSocketFactory factory;

    public AbstractDebugSecureSocketFactory( String protocol )
    {
        try
        {
            SSLContext sslcontext = SSLContext.getInstance( protocol );

            sslcontext.init( null, new TrustManager[] { new NaiveTrustManager() }, null );

            factory = (SSLSocketFactory) sslcontext.getSocketFactory();
        }
        catch ( Exception ex )
        {
            // ignore
        }
    }

    public Socket createSocket()
        throws IOException
    {
        return factory.createSocket();
    }

    public Socket createSocket( Socket socket, String s, int i, boolean flag )
        throws IOException
    {
        return factory.createSocket( socket, s, i, flag );
    }

    public Socket createSocket( InetAddress inaddr, int i, InetAddress inaddr1, int j )
        throws IOException
    {
        return factory.createSocket( inaddr, i, inaddr1, j );
    }

    public Socket createSocket( InetAddress inaddr, int i )
        throws IOException
    {
        return factory.createSocket( inaddr, i );
    }

    public Socket createSocket( String s, int i, InetAddress inaddr, int j )
        throws IOException
    {
        return factory.createSocket( s, i, inaddr, j );
    }

    public Socket createSocket( String s, int i )
        throws IOException
    {
        return factory.createSocket( s, i );
    }

    public String[] getDefaultCipherSuites()
    {
        return factory.getDefaultCipherSuites();
    }

    public String[] getSupportedCipherSuites()
    {
        return factory.getSupportedCipherSuites();
    }
}
