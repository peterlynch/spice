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

public class ControllerClient
{

    private final int port;

    private final InetAddress address;

    private boolean closed;

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
        return closed;
    }
    
    public void close()
    {
        this.closed = true;
    }

    public void shutdown()
        throws ControlConnectionException, IOException
    {
        System.out.println( "Requesting Shutdown on Port " + port + "..." );
        SocketChannel channel = null;

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

            buffer.put( ControllerVocabulary.STOP_SERVICE );
            buffer.rewind();

            channel.write( buffer );

            buffer.rewind();

            channel.read( buffer );
            buffer.flip();

            if ( (byte) 0x0 == buffer.get() )
            {
                closed = true;
            }
        }
        finally
        {
            ChannelUtil.close( channel );
        }
        
        System.out.println( "...Requested Shutdown on Port " + port + " Completed" );

    }

}
