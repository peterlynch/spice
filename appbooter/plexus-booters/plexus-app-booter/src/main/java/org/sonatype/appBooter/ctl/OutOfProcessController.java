 /**
  * Copyright (C) 2008 Sonatype, Inc. 
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
package org.sonatype.appBooter.ctl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class OutOfProcessController
    implements Runnable
{

    private static final int DEFAULT_TIMEOUT = 1000;

    private final Service service;

    private final InetAddress bindAddress;

    private final int port;

    private ServerSocketChannel serverChannel;

    private SocketChannel channel;

    private ByteBuffer buffer = ByteBuffer.allocate( 1 );

    private ByteBuffer ackBuffer = ByteBuffer.wrap( new byte[]{ ControllerVocabulary.ACK } );

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
        t.setDaemon( true );

        t.start();

        return t;
    }

    public void run()
    {
        if ( !openServerChannel() )
        {
            return;
        }

        while ( !Thread.currentThread().isInterrupted() )
        {
            Boolean result = readCommand();

            if ( result == null )
            {
                close();
                return;
            }
            else if ( result == Boolean.FALSE )
            {
                continue;
            }

            byte cmd = buffer.get();
            switch ( cmd )
            {
                case ( ControllerVocabulary.STOP_SERVICE   ):
                {
                    close();
                    return;
                }
                default:
                {
                    System.out.println( "Unknown command: " + Integer.toHexString( cmd & 0x0f ) );
                }
            }
        }

        if ( Thread.currentThread().isInterrupted() )
        {
            System.out.println( "Control thread interrupted. Stopping plexus application." );
            close();
        }
    }

    /**
     * NOTE: Here, Boolean.class is used to indicate a response with three possible
     * values (3VL).<br/>
     * <ul>
     *   <li>null indicates that the server should exit.</li>
     *   <li>Boolean.FALSE indicates that the accept() loop should continue without processing the current command.</li>
     *   <li>Boolean.TRUE indicates that the current loop iteration should proceed by processing the current command.</li>
     * </ul>
     */
    private Boolean readCommand()
    {
        ChannelUtil.close( channel );
        if ( !serverChannel.isOpen() )
        {
            return null;
        }

        try
        {
            channel = serverChannel.accept();
        }
        catch( ClosedByInterruptException e )
        {
            close();
            return null;
        }
        catch ( IOException e )
        {
            e.printStackTrace( System.out );
            System.out.println( "\n\n\nERROR: Cannot accept connection from control socket on: "
                                + bindAddress
                                + ":"
                                + port
                                + "\nSee stacktrace above for more information.\n" );

            close();
            return null;
        }

        if ( channel == null )
        {
            return Boolean.FALSE;
        }
        else
        {
            System.out.println( "Accepted control connection from: " + channel.socket().getRemoteSocketAddress() );
        }

        buffer.rewind();
        try
        {
            channel.read( buffer );
            channel.write( ackBuffer );
        }
        catch ( AsynchronousCloseException e )
        {
            return Boolean.FALSE;
        }
        catch ( IOException e )
        {
            e.printStackTrace( System.out );
            System.out.println( "\n\n\nERROR: Failed to read command byte from incoming control connection.\nSee stacktrace above for more information.\n" );

            ChannelUtil.close( channel );

            return Boolean.FALSE;
        }

        buffer.flip();

        return Boolean.TRUE;
    }

    private boolean openServerChannel()
    {
        try
        {
            InetSocketAddress addr = new InetSocketAddress( bindAddress, port );

            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking( false );

            serverChannel.socket().bind( addr );
            serverChannel.socket().setSoTimeout( DEFAULT_TIMEOUT );
            serverChannel.socket().setReceiveBufferSize( 1 );
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

    private void close( )
    {
        service.shutdown();

        ChannelUtil.close( channel );
        ChannelUtil.close( serverChannel );
    }

}
