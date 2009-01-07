/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package net.java.dev.openim;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.ServiceLocator;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Serviceable;

/**
 * @version 1.5
 * @author AlAg
 */
public class IMServerImpl
    extends AbstractLogEnabled
    implements IMServer, Initializable, Disposable, Serviceable
{
    private ServiceLocator serviceLocator;

    // Requirements
    private ServerParameters serverParameters;

    // Autoconfigs
    private int listenBacklog;

    private String bindAddress;

    // -------------------------------------------------------------------------
    public void service( ServiceLocator serviceLocator )
    {
        this.serviceLocator = serviceLocator;
    }

    // -------------------------------------------------------------------------
    public void initialize()
    {
        try
        {

            InetAddress bindTo = null;
            if ( bindAddress != null && bindAddress.length() > 0 )
            {
                bindTo = InetAddress.getByName( bindAddress );
            }

            ServerSocketFactory factory = ServerSocketFactory.getDefault();
            ServerSocket clientServerSocket = factory.createServerSocket( serverParameters.getLocalClientPort(),
                                                                          listenBacklog, bindTo );
            new ConnectionManager( clientServerSocket, serverParameters.getLocalClientThreadPool() ).start();
            ServerSocket serverServerSocket = factory.createServerSocket( serverParameters.getLocalServerPort(),
                                                                          listenBacklog, bindTo );
            new ConnectionManager( serverServerSocket, serverParameters.getLocalServerThreadPool() ).start();

            // java -Djavax.net.ssl.keyStore=mySrvKeystore -Djavax.net.ssl.keyStorePassword=123456 MyServer
            // keytool -keystore mySrvKeystore -keypasswd 123456 -genkey -keyalg RSA -alias mycert
            ServerSocketFactory sslfactory = SSLServerSocketFactory.getDefault();
            ServerSocket sslClientServerSocket = sslfactory.createServerSocket( serverParameters
                .getLocalSSLClientPort(), listenBacklog, bindTo );
            new ConnectionManager( sslClientServerSocket, serverParameters.getLocalSSLClientThreadPool() ).start();

            ServerSocket sslServerServerSocket = sslfactory.createServerSocket( serverParameters
                .getLocalSSLServerPort(), listenBacklog, bindTo );
            new ConnectionManager( sslServerServerSocket, serverParameters.getLocalSSLServerThreadPool() ).start();

            String s = "Server '" + serverParameters.getHostName() + "' initialized on" + " server2server port "
                + serverParameters.getLocalServerPort() + " SSL-server2server port "
                + serverParameters.getLocalSSLServerPort() + " client2server port "
                + serverParameters.getLocalClientPort() + " SSL-client2server port "
                + serverParameters.getLocalSSLClientPort();

            getLogger().info( s );
            // System.out.println( s );
        }
        catch ( Exception e )
        {
            getLogger().error( e.getMessage(), e );
        }
    }

    // -------------------------------------------------------------------------
    public void dispose()
    {
        getLogger().debug( "Disposing Server" );
    }

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    public class ConnectionManager
        extends Thread
    {

        private ServerSocket serverSocket;
        private int poolSize;

        public ConnectionManager( ServerSocket serverSocket, int poolSize )
        {
            this.serverSocket = serverSocket;
            this.poolSize = poolSize;
        }

        public void run()
        {

            ExecutorService pool = Executors.newFixedThreadPool( poolSize );
            for ( ;; )
            {
                try
                {
                    pool.execute( new Handler( serverSocket.accept() ) );
                }
                catch ( IOException ex )
                {
                }
            }
            //pool.shutdown();

        }
    }

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    class Handler
        implements Runnable
    {
        private final Socket socket;

        public Handler( Socket socket )
        {
            this.socket = socket;
        }

        public void run()
        {
            try
            {
                IMConnectionHandler imConnectionHandler = (IMConnectionHandler) serviceLocator
                    .lookup( IMConnectionHandler.class.getName(), "IMConnectionHandler" );
                imConnectionHandler.handleConnection( socket );
            }
            catch ( Exception e )
            {
                getLogger().error( e.getMessage(), e );
            }
        }
    }
}
