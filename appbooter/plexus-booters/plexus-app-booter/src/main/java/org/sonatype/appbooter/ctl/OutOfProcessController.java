/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.appbooter.ctl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Manages Service implementations, giving some access to the Service on an open port.
 *
 */
public class OutOfProcessController
    extends Thread
    implements Runnable
{
    
    /*
     * NOTE: This class is fairly complex. This is because we don't want to execute the management behavior
     * in the current thread. Instead, a new thread is created to manage the ServerSocket that's created to
     * allow remote connections to manage the service. From here, a new thread is created per socket connection,
     * to prevent a zombie connection from blocking out other live management connections, thereby deadlocking
     * the whole system.
     * 
     * The main class, OutOfProcessController, actually extends Thread to override the interrupt method and
     * allow it to forcibly close the network sockets in play. This seems necessary since the socket.getInputStream().read(..)
     * method doesn't seem to respond to Thread.interrupt() on all platforms (notably, I've had trouble with socket code running
     * on FreeBSD and Solaris).
     */

    public static final int DEFAULT_TIMEOUT = 5000;

    public static final long SLEEP_PERIOD = 4000;

    private boolean interrupted = false;
    
    private final CtlRunnable runnable;
    
    private OutOfProcessController( CtlRunnable runnable )
    {
        super( runnable );
        this.runnable = runnable;
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
        CtlRunnable ctl = new CtlRunnable( service, InetAddress.getLocalHost(), port );
        
        OutOfProcessController controller = new OutOfProcessController( ctl );
        controller.setPriority( Thread.MIN_PRIORITY );
        controller.start();

        return controller;
    }

    private static final class CtlRunnable implements Runnable
    {
        private final Service service;

        private final InetAddress bindAddress;

        private final int port;
        
        private ExecutorService executor = Executors.newCachedThreadPool();
        
        private Set<Socket> sockets = new HashSet<Socket>();

        private ServerSocket serverSocket;
        
        private boolean stop = false;
        
        public CtlRunnable( Service service, InetAddress bindAddress, int port )
        {
            this.service = service;
            this.bindAddress = bindAddress;
            this.port = port;
        }

        public void run()
        {
            if ( openServerSocket() )
            {
                while ( !Thread.currentThread().isInterrupted() && !stop )
                {
                    Socket socket = null;
                    try
                    {
                        try
                        {
                            socket = serverSocket.accept();
                        }
                        catch ( SocketTimeoutException e )
                        {
                            continue;
                        }
                        
                        socket.setTcpNoDelay( true );

                        sockets.add( socket );
                        executor.execute( new SocketHandler( this, service, socket ) );
                    }
                    catch ( IOException e )
                    {
                        if ( e instanceof SocketException )
                        {
                            System.out.println( "Shutdown in progress. No longer accepting socket connections." );
                        }
                        else
                        {
                            e.printStackTrace();
                            System.out.println( "Port " + port + " Error while servicing control socket: " + e.getMessage() + "\nKilling connection." );
                        }
                        
                        ControllerUtil.close( socket );
                    }
                }
            }

            //When we are done managing, time to go ahead and stop the Service as well
            try
            {
                close();
            }
            catch ( AppBooterServiceException e )
            {
                e.printStackTrace();
                System.out.println( "\n\n\nERROR: Unable to shutdown the process."  );
            }

            System.out.println( "Port " + port + " Controller Thread Complete" );
        }
        
        private boolean openServerSocket()
        {
            try
            {
                serverSocket = new ServerSocket( port );
                
                System.out.println( "Control socket listening on: " + serverSocket.getLocalSocketAddress() );
            }
            catch ( IOException e )
            {
                e.printStackTrace( System.out );
                System.out.println( "\n\n\nERROR: Cannot open control socket on: " + bindAddress + ":"
                                    + port + "\nSee stacktrace above for more information.\n" );
                
                ControllerUtil.close( serverSocket );
                return false;
            }

            return true;
        }

        public void close() throws AppBooterServiceException
        {
            System.out.println( "Controller closing..." );
            service.shutdown();

            if ( serverSocket != null )
            {
                ControllerUtil.close( serverSocket );
            }
            
            stop = true;
            
            // following example from: http://java.sun.com/javase/6/docs/api/index.html?java/util/concurrent/ExecutorService.html
            executor.shutdown();
            try
            {
                if ( !executor.awaitTermination( 5, TimeUnit.SECONDS ) )
                {
                    executor.shutdownNow();
                    if ( !executor.awaitTermination( 5, TimeUnit.SECONDS ) )
                    {
                        System.out.println( "\n\n\nWARNING: Failed to stop one or more socket-handler threads: executor pool did not terminate.\n\n\n" );
                    }
                }
            }
            catch ( InterruptedException e )
            {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            // ---
            
            if ( sockets != null && !sockets.isEmpty() )
            {
                for ( Socket socket : sockets )
                {
                    if ( socket != null )
                    {
                        System.out.println( "Closing connection from: " + socket.getRemoteSocketAddress() );
                    }
                    
                    ControllerUtil.close( socket );
                }
            }
            
            System.out.println( "...Controller closed." );
        }

    }

    @Override
    public void interrupt()
    {
        super.interrupt();
        
        if ( interrupted )
        {
            return;
        }
        
        System.out.println( "Control thread has been interrupted, and should close shortly.");
        
        interrupted = true;
        
        try
        {
            runnable.close();
        }
        catch ( AppBooterServiceException e )
        {
            e.printStackTrace();
            System.out.println( "Error closing management socket on port: " + runnable.port + ". Reason: " + e.getMessage() );
        }
    }

    @Override
    public boolean isInterrupted()
    {
        return interrupted;
    }
    
    private static final class SocketHandler implements Runnable
    {

        private final CtlRunnable ctlRunnable;
        private final Service service;
        private final Socket socket;

        public SocketHandler( CtlRunnable ctlRunnable, Service service, Socket socket )
        {
            this.ctlRunnable = ctlRunnable;
            this.service = service;
            this.socket = socket;
        }

        public void run()
        {
            boolean doShutdown = false;
            try
            {
                boolean stopLooping = false;
                while ( !stopLooping )
                {
                    byte command = 0x0;
                    try
                    {
                        byte[] data = new byte[1];
                        int read = (byte) socket.getInputStream().read( data, 0, 1 );
                        if ( read > 0 )
                        {
                            command = data[0];
                        }
                        else
                        {
                            continue;
                        }
                    }
                    catch ( SocketException e )
                    {
                        break;
                    }

                    if ( command > -1 )
                    {
                        switch ( command )
                        {
                            case ( ControllerVocabulary.SHUTDOWN_SERVICE ):
                            {
                                System.out.println( "Port " + ctlRunnable.port + " Received shutdown command. Shutting down application." );
                                doShutdown = true;
                                stopLooping = true;
                                break;
                            }
                            case ( ControllerVocabulary.STOP_SERVICE ):
                            {
                                System.out.println( "Port " + ctlRunnable.port + " Received stop command.  Stopping application." );
                                service.stop();
                                break;
                            }
                            case ( ControllerVocabulary.START_SERVICE ):
                            {
                                System.out.println( "Port " + ctlRunnable.port + " Received start command.  Starting application." );
                                service.start();
                                break;
                            }
                            case ( ControllerVocabulary.SHUTDOWN_ON_CLOSE ):
                            {
                                System.out.println( "Port " + ctlRunnable.port + " Received shutdown-on-close command. Application will be terminated if socket is closed." );
                                doShutdown = true;
                                stopLooping = false;
                                break;
                            }
                            case ( ControllerVocabulary.DETACH_ON_CLOSE ):
                            {
                                System.out.println( "Port " + ctlRunnable.port + " Received detach-on-close command. Application will -NOT- be terminated if socket is closed." );
                                ctlRunnable.stop = false;
                                doShutdown = false;
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
            catch ( SocketException e )
            {
                System.out.println( "Port " + ctlRunnable.port + " Control socket closed. Cleaning up." );
            }
            catch ( IOException e )
            {
                if ( "Socket Closed".equals( e.getMessage() ) )
                {
                    System.out.println( "Port " + ctlRunnable.port + " Control socket closed. Cleaning up." );
                }
                else
                {
                    e.printStackTrace();
                    System.out.println( "Port " + ctlRunnable.port + " Error while servicing control socket: " + e.getMessage() + "\nKilling connection." );
                }
            }
            catch ( AppBooterServiceException e )
            {
                e.printStackTrace();
                System.out.println( "Port " + ctlRunnable.port + " Error while servicing control socket: " + e.getMessage() + "\nKilling connection." );
            }
            finally
            {
                System.out.println( "Shutdown management thread? " + doShutdown );
                if ( doShutdown )
                {
                    try
                    {
                        ctlRunnable.close();
                    }
                    catch ( AppBooterServiceException e )
                    {
                        e.printStackTrace();
                        System.out.println( "Port " + ctlRunnable.port + " Error while shutting down service: " + e.getMessage() );
                    }
                }
                else
                {
                    ctlRunnable.sockets.remove( socket.getRemoteSocketAddress().toString() );
                    ControllerUtil.close( socket );
                }
            }
        }
    }
}
