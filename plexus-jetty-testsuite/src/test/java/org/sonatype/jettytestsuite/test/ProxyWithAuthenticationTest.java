package org.sonatype.jettytestsuite.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.jettytestsuite.ProxyServer;

import sun.misc.BASE64Encoder;

public class ProxyWithAuthenticationTest
{
    private static ProxyServer server;

    @BeforeClass
    public static void runServer()
        throws Exception
    {
        ContainerConfiguration containerConfiguration = new DefaultContainerConfiguration().setName( "test" );
        PlexusContainer container = new DefaultPlexusContainer( containerConfiguration );

        server = (ProxyServer) container.lookup( ProxyServer.ROLE );
        server.start();
        server.getProxyServlet().setUseAuthentication( true );
        server.getProxyServlet().getAuthentications().put( "admin", "123" );
    }

    @Test
    public void validUser()
        throws Exception
    {
        SocketAddress sa = new InetSocketAddress( "127.0.0.1", 3102 );
        Proxy p = new Proxy( Proxy.Type.HTTP, sa );

        URL url = new URL( "http://www.google.com/index.html" );
        HttpURLConnection con = (HttpURLConnection) url.openConnection( p );

        BASE64Encoder encoder = new BASE64Encoder();
        String encodedUserPwd = encoder.encode( "admin:123".getBytes() );
        con.setRequestProperty( "Proxy-Authorization", "Basic " + encodedUserPwd );
        con.getInputStream();

        for ( int i = 0; i < 100; i++ )
        {
            Thread.sleep( 200 );

            List<String> uris = server.getAccessedUris();
            for ( String uri : uris )
            {
                if ( uri.contains( "google.com" ) )
                {
                    return;
                }
            }
        }

        Assert.fail( "Proxy was not able to access google.com" );
    }

    @Test( expected = IOException.class )
    public void invalidUser()
        throws Exception
    {
        SocketAddress sa = new InetSocketAddress( "127.0.0.1", 3102 );
        Proxy p = new Proxy( Proxy.Type.HTTP, sa );

        URL url = new URL( "http://www.google.com/index.html" );
        HttpURLConnection con = (HttpURLConnection) url.openConnection( p );

        BASE64Encoder encoder = new BASE64Encoder();
        String encodedUserPwd = encoder.encode( "admin:1234".getBytes() );
        con.setRequestProperty( "Proxy-Authorization", "Basic " + encodedUserPwd );
        con.getInputStream();

        Assert.fail( "Proxy was not able to access google.com" );
    }

    @Test( expected = IOException.class )
    public void withoutUser()
        throws Exception
    {
        SocketAddress sa = new InetSocketAddress( "127.0.0.1", 3102 );
        Proxy p = new Proxy( Proxy.Type.HTTP, sa );

        URL url = new URL( "http://www.google.com/index.html" );
        URLConnection con = url.openConnection( p );

        con.getInputStream();

        Assert.fail( "Proxy was not able to access google.com" );
    }

    @AfterClass
    public static void stop()
        throws Exception
    {
        server.stop();
    }

}
