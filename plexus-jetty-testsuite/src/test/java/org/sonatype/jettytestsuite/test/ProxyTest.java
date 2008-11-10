package org.sonatype.jettytestsuite.test;

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

public class ProxyTest
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
    }

    @Test
    public void checkWebProxy()
        throws Exception
    {
        SocketAddress sa = new InetSocketAddress( "127.0.0.1", 3102 );
        Proxy p = new Proxy( Proxy.Type.HTTP, sa );

        URL url = new URL( "http://www.google.com/index.html" );
        URLConnection conn = url.openConnection( p );
        conn.getInputStream();

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

    @AfterClass
    public static void stop()
        throws Exception
    {
        server.stop();
    }

}
