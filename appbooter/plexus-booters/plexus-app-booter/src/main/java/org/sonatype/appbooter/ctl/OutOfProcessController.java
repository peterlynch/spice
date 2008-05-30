/**
 * Copyright (C) 2008 Sonatype Inc.
 * Sonatype Inc, licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.sonatype.appbooter.ctl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Manages Service implementations, giving some access to the Service on an open port.
 *
 */
public class OutOfProcessController
    implements Runnable
{

    public static final int DEFAULT_TIMEOUT = 5000;

    public static final long SLEEP_PERIOD = 4000;

    private final Service service;

    private final InetAddress bindAddress;

    private final int port;

    private ServerSocket serverSocket;

    private OutOfProcessController( Service service,
                                    InetAddress bindAddress,
                                    int port )
    {
        this.service = service;
        this.bindAddress = bindAddress;
        this.port = port;
    }

    /**
     * Retrieve a new Thread object that will handle notifying the service when requests are made from other applications.
     * It is expected that the Thread object returned will be maintained manually.
     *
     * @param service Service implementation that will be managed
     * @param port TCP/IP Port number to listen on
     * @return new Thread object that will listen for, and react to messages.  When the object is returned, it will already be running
     * @throws UnknownHostException
     */
    public static Thread manage( Service service,
                                 int port )
        throws UnknownHostException
    {
        OutOfProcessController ctl = new OutOfProcessController( service,
                                                                 InetAddress.getLocalHost(), port );
        Thread t = new Thread( ctl );
        t.setPriority( Thread.MIN_PRIORITY );

        t.start();

        return t;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        System.out.println( "Port " + port + " Controller Thread Started" );
        if ( openServerSocket() )
        {
            boolean stop = false;
            while ( !Thread.currentThread().isInterrupted() && !stop)
            {
                Socket socket = null;
                try
                {
                    try
                    {
                        System.out.println( "Port " + port + " accepting control connections." );
                        socket = serverSocket.accept();
                    }
                    catch ( SocketTimeoutException e )
                    {
                        System.out.println( "Port " + port + " Socket timed out on accept." );
                        continue;
                    }

                    System.out.println( "Port " + port + " setting socket parameters." );
//                    socket.setSoTimeout( DEFAULT_TIMEOUT );
                    socket.setTcpNoDelay( true );

                    while ( true )
                    {
                        byte command = 0x0;
                        try
                        {
                            byte[] data = new byte[1];
                            System.out.println( "Port " + port + " reading from control connection." );
                            int read = (byte) socket.getInputStream().read( data, 0, 1 );
                            if ( read > 0 )
                            {
                                command = data[0];
                            }
                            else
                            {
//                                System.out.println( "Port " + port + " read " + read + " bytes. Sleeping " + SLEEP_PERIOD + "ms before continuing with another read attempt." );
//                                try
//                                {
//                                    Thread.sleep( SLEEP_PERIOD );
//                                }
//                                catch ( InterruptedException e )
//                                {
//                                }

                                continue;
                            }
                        }
                        catch ( SocketException e )
                        {
                            System.out.println( "Port " + port + ": " + e.getMessage() + "...Closing connection." );
                            break;
                        }

                        if ( command > -1 )
                        {
                            switch ( command )
                            {
                                case ( ControllerVocabulary.SHUTDOWN_SERVICE ):
                                {
                                    System.out.println( "Port " + port + " Received shutdown command. Shutting down application." );
                                    stop = true;
                                    break;
                                }
                                case ( ControllerVocabulary.STOP_SERVICE ):
                                {
                                    System.out.println( "Port " + port + " Received stop command.  Stopping application." );
                                    service.stop();
                                    break;
                                }
                                case ( ControllerVocabulary.START_SERVICE ):
                                {
                                    System.out.println( "Port " + port + " Received start command.  Starting application." );
                                    service.start();
                                    break;
                                }
                                case ( ControllerVocabulary.SHUTDOWN_ON_CLOSE ):
                                {
                                    System.out.println( "Port " + port + " Received shutdown-on-close command. Application will be terminated if socket is closed." );
                                    stop = true;
                                    break;
                                }
                                case ( ControllerVocabulary.DETACH_ON_CLOSE ):
                                {
                                    System.out.println( "Port " + port + " Received detach-on-close command. Application will -NOT- be terminated if socket is closed." );
                                    stop = false;
                                    break;
                                }
                                default:
                                {
                                    System.out.println( "Unknown command: 0x" + Integer.toHexString( command & 0xf ));
                                }
                            }

                            socket.getOutputStream().write( command );
                        }
                    }
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    System.out.println( "Port " + port + " Error while servicing control socket: " + e.getMessage() + "\nKilling connection." );
                }
                finally
                {
                    ControllerUtil.close( socket );
                }
            }
        }

        //When we are done managing, time to go ahead and stop the Service as well
        close();

        System.out.println( "Port " + port + " Controller Thread Complete" );
    }

    private boolean openServerSocket()
    {
        try
        {
            System.out.println( "Port " + port + " Opening socket channel for port." );
            serverSocket = new ServerSocket( port );

            System.out.println( "Setting socket parameters." );
            serverSocket.setSoTimeout( DEFAULT_TIMEOUT );
        }
        catch ( IOException e )
        {
            e.printStackTrace( System.out );
            System.out.println( "\n\n\nERROR: Cannot open control socket on: " + bindAddress + ":"
                                + port + "\nSee stacktrace above for more information.\n" );
            return false;
        }

        return true;
    }

    private void close()
    {
        System.out.println( "Controller Closing, requesting controlled service shutdown" );
        service.shutdown();

        ControllerUtil.close( serverSocket );
    }

}
