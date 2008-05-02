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
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class OutOfProcessController
    implements Runnable
{

    public static final int DEFAULT_TIMEOUT = 5000;

    public static final long SLEEP_PERIOD = 4000;

    private final Service service;

    private final InetAddress bindAddress;

    private final int port;

    private ServerSocketChannel serverChannel;

    private Selector selector;

    private ByteBuffer buffer = ByteBuffer.allocate( 1 );

    private ByteBuffer ackBuffer = ByteBuffer.wrap( new byte[] {
        ControllerVocabulary.ACK
    } );

    private OutOfProcessController( Service service,
                                    InetAddress bindAddress,
                                    int port )
    {
        this.service = service;
        this.bindAddress = bindAddress;
        this.port = port;
    }

    public static Thread manage( Service service,
                                 int port )
        throws UnknownHostException
    {
        OutOfProcessController ctl = new OutOfProcessController( service,
                                                                 InetAddress.getLocalHost(), port );
        Thread t = new Thread( ctl );
//        t.setDaemon( true );
        t.setPriority( Thread.MIN_PRIORITY );

        t.start();

        return t;
    }

    public void run()
    {
        System.out.println( "Starting server connection." );
        if ( openServerChannel() )
        {
            all:
            while ( !Thread.currentThread().isInterrupted() )
            {
                if ( !serverChannel.isOpen() || !selector.isOpen() )
                {
                    System.out.println( "Server socket or selector is closed. Stopping." );
                    break all;
                }

                int numberReady;
                try
                {
                    numberReady = selector.select();
                }
                catch ( ClosedSelectorException e )
                {
                    break all;
                }
                catch ( ClosedByInterruptException e )
                {
                    continue all;
                }
                catch ( IOException e )
                {
                    e.printStackTrace( System.out );
                    System.out.println( "\n\n\nERROR: Cannot select active keys for control on: "
                                        + bindAddress
                                        + ":"
                                        + port
                                        + "\nSee stacktrace above for more information.\n" );

                    break all;
                }

                if ( numberReady < 1 )
                {
                    continue all;
                }

                Set<SelectionKey> keys = selector.selectedKeys();
                keyProcessing:
                for ( SelectionKey key : keys )
                {
                    SocketChannel channel = accept( key );

                    if ( channel == null )
                    {
                        continue keyProcessing;
                    }

                    try
                    {
                        if ( !readCommand( channel, key ) )
                        {
                            continue keyProcessing;
                        }

                        buffer.flip();
                        byte cmd = buffer.get();
                        switch ( cmd )
                        {
                            case ( ControllerVocabulary.STOP_SERVICE ):
                            {
                                System.out.println( "Received stop command. Stopping application." );
                                break all;
                            }
                            default:
                            {
                                System.out.println( "Received unknown command: 0x" + Integer.toHexString( cmd & 0x0f ) );
                            }
                        }
                    }
                    finally
                    {
                        ChannelUtil.close( channel );
                    }
                }
            }
        }

        System.out.println( "Stopping plexus application." );
        close();
    }

    private boolean readCommand( SocketChannel channel, SelectionKey key )
    {
        System.out.println( "Accepted control connection from: "
                            + channel.socket().getRemoteSocketAddress() );

        buffer.rewind();
        try
        {
//            channel.configureBlocking( false );
//            channel.register( selector, SelectionKey.OP_WRITE );
            int read = channel.read( buffer );
            if ( read > 0 )
            {
                channel.write( ackBuffer );
//                if ( key.isWritable() )
//                {
//                }
//                else
//                {
//                    System.out.println( "Cannot write ACK. Skipping, and closing channel." );
//                }

                return true;
            }
        }
        catch ( AsynchronousCloseException e )
        {
            System.out.println( "Control channel was closed asynchronously." );
        }
        catch ( IOException e )
        {
            e.printStackTrace( System.out );
            System.out.println( "\n\n\nERROR: Failed to read command byte from incoming control connection.\nSee stacktrace above for more information.\n" );
        }

        return false;
    }

    private SocketChannel accept( SelectionKey key )
    {
        SocketChannel channel = null;

        try
        {
            key = selector.selectedKeys().iterator().next();
            if ( key.isAcceptable() )
            {
                channel = ( (ServerSocketChannel) key.channel() ).accept();
            }
            else
            {
                System.out.println( "Control connection cannot be accepted. Ignoring." );
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace( System.out );
            System.out.println( "\n\n\nERROR: Cannot accept connection from control socket on: "
                                + bindAddress
                                + ":"
                                + port
                                + "\nSee stacktrace above for more information.\n" );

            ChannelUtil.close( channel );
        }

        return channel;
    }

    private boolean openServerChannel()
    {
        try
        {
            InetSocketAddress addr = new InetSocketAddress( bindAddress, port );

            System.out.println( "Opening socket channel." );
            serverChannel = ServerSocketChannel.open();

            System.out.println( "Binding to: " + addr );
            serverChannel.socket().bind( addr );

            System.out.println( "Opening selector." );
            selector = Selector.open();

            System.out.println( "Setting socket parameters." );
            serverChannel.configureBlocking( false );
            serverChannel.socket().setSoTimeout( DEFAULT_TIMEOUT );
            serverChannel.socket().setReceiveBufferSize( 1 );

            System.out.println( "Registering selector." );
            serverChannel.register( selector, SelectionKey.OP_ACCEPT );
        }
        catch ( IOException e )
        {
            e.printStackTrace( System.out );
            System.out.println( "\n\n\nERROR: Cannot open control socket on: " + bindAddress + ":"
                                + port + "\nSee stacktrace above for more information.\n" );

            close();
            return false;
        }

        return true;
    }

    private void close()
    {
        service.shutdown();

        ChannelUtil.close( serverChannel );
        ChannelUtil.close( selector );
    }

}
