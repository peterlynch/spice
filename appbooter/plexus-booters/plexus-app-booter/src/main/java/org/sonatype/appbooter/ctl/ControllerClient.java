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
import java.nio.channels.SocketChannel;

/**
 * Using a defined port, the ControllerClient can communicate with a service 
 * that is managed by an OutOfProcessController.
 *
 */
public class ControllerClient
{
    // Port that this client will attempt to connect to
    private final int port;

    // Address of the remote host (defaults to localhost)
    private final InetAddress address;

    
    public static final int RUNNING = 0;
    public static final int STOPPED = 1;
    public static final int SHUTDOWN = 2;
    private int state = RUNNING;

    public ControllerClient( String host,
                             int port )
        throws UnknownHostException
    {
        address = InetAddress.getByName( host );
        this.port = port;
    }

    public ControllerClient( int port )
        throws UnknownHostException
    {
        address = InetAddress.getLocalHost();
        this.port = port;
    }

    public ControllerClient( InetAddress address,
                             int port )
    {
        this.address = address;
        this.port = port;
    }

    public boolean isShutdown()
    {
        return state == SHUTDOWN;
    }
    
    /**
     * Close the Client without accessing the server
     */
    public void close()
    {
        this.state = SHUTDOWN;
    }

    /**
     * Shutdown the remote Controller
     * 
     * @throws ControlConnectionException
     * @throws IOException
     */
    public void shutdown()
        throws ControlConnectionException, IOException
    {
        System.out.println( "Requesting Shutdown on Port " + port + "..." );
        
        byte response = sendControlMessage( ControllerVocabulary.SHUTDOWN_SERVICE );
        
        if ( ControllerVocabulary.ACK == response )
        {
            state = SHUTDOWN;
            System.out.println( "...Requested Shutdown on Port " + port + " Completed" );
        }
        else
        {
            System.out.println( "...Requested Shutdown on Port " + port + " Completed without ACK from server - " + response );
        }

    }
    
    public void stop()
        throws ControlConnectionException, IOException
    {
        System.out.println( "Requesting Stop on Port " + port + "..." );
        
        byte response = sendControlMessage( ControllerVocabulary.STOP_SERVICE );
        
        if ( ControllerVocabulary.ACK == response )
        {
            state = STOPPED;
            System.out.println( "...Requested Stop on Port " + port + " Completed" );
        }
        else
        {
            System.out.println( "...Requested Stop on Port " + port + " Completed without ACK from server - " + response );
        }
    }
     
    public void start()
        throws ControlConnectionException, IOException
    {
        System.out.println( "Requesting Start on Port " + port + "..." );
        
        byte response = sendControlMessage( ControllerVocabulary.START_SERVICE );
        
        if ( ControllerVocabulary.ACK == response)
        {
            state = RUNNING;
            System.out.println( "...Requested Start on Port " + port + " Completed" );
        }
        else
        {
            System.out.println( "...Requested Start on Port " + port + " Completed without ACK from server - " + response );
        }
    }
    
    private byte sendControlMessage( byte command )
        throws ControlConnectionException, IOException
    {
        SocketChannel channel = null;
        
        byte response = 0x0;
    
        try
        {
            try
            {
                InetSocketAddress addr = new InetSocketAddress( address, port );
    
                channel = SocketChannel.open( addr );
            }
            catch( IOException e )
            {
                throw new ControlConnectionException( e );
            }
    
            ByteBuffer buffer = ByteBuffer.allocate( 1 );
    
            // Just send out the STOP command, Controller takes care of the rest
            buffer.put( command );
            buffer.rewind();
    
            channel.write( buffer );
    
            buffer.rewind();
    
            channel.read( buffer );
            buffer.flip();
            
            response = buffer.get();
        }
        finally
        {
            ChannelUtil.close( channel );
        }
        
        return response;
    }
}
