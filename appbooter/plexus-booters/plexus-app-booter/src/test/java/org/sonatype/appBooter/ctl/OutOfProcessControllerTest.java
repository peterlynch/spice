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
import java.nio.channels.SocketChannel;

import org.sonatype.appBooter.ctl.OutOfProcessController;

import junit.framework.TestCase;

public class OutOfProcessControllerTest
    extends TestCase
{

    public void testThreadInterruption()
        throws UnknownHostException
    {
        TestService svc = new TestService();

        Thread managementThread = OutOfProcessController.manage( svc, 32001 );

        synchronized ( managementThread )
        {
            managementThread.interrupt();

            try
            {
                managementThread.join( 5000 );
            }
            catch ( InterruptedException e )
            {
                System.out.println( "Interrupted." );
            }
        }

        assertFalse( "Thread should have died.", managementThread.isAlive() );
        assertTrue( "Service should have been closed.", svc.closed );
    }

    public void testStopCommand()
        throws IOException, InterruptedException
    {
        TestService svc = new TestService();

        int port = 32001;

        Thread managementThread = OutOfProcessController.manage( svc, port );

        Thread.sleep( 100 );

        SocketChannel channel = SocketChannel.open( new InetSocketAddress(
                                                                           InetAddress.getLocalHost(),
                                                                           port ) );
        ByteBuffer buffer = ByteBuffer.allocate( 1 );

        buffer.put( ControllerVocabulary.STOP_SERVICE );
        buffer.rewind();

        channel.write( buffer );

        buffer.rewind();

        channel.read( buffer );
        buffer.flip();

        assertEquals( ControllerVocabulary.ACK, buffer.get() );

        synchronized ( managementThread )
        {
            try
            {
                managementThread.join( 5000 );
            }
            catch ( InterruptedException e )
            {
                System.out.println( "Interrupted." );
            }
        }

        assertFalse( "Thread should have died.", managementThread.isAlive() );
        assertTrue( "Service should have been closed.", svc.closed );
    }

    public void testUnknownCommand()
        throws IOException, InterruptedException
    {
        TestService svc = new TestService();

        int port = 32001;

        Thread managementThread = OutOfProcessController.manage( svc, port );

        Thread.sleep( 100 );

        SocketChannel channel = SocketChannel.open( new InetSocketAddress(
                                                                           InetAddress.getLocalHost(),
                                                                           port ) );
        ByteBuffer buffer = ByteBuffer.allocate( 1 );

        buffer.put( (byte) 0x0 );
        buffer.rewind();

        channel.write( buffer );

        buffer.rewind();

        channel.read( buffer );
        buffer.flip();

        assertEquals( ControllerVocabulary.ACK, buffer.get() );
        assertFalse( "Service should NOT have been closed.", svc.closed );

        synchronized ( managementThread )
        {
            managementThread.interrupt();

            try
            {
                managementThread.join( 5000 );
            }
            catch ( InterruptedException e )
            {
                System.out.println( "Interrupted." );
            }
        }

        assertFalse( "Thread should have died.", managementThread.isAlive() );
        assertTrue( "Service should have been closed.", svc.closed );
    }

    private static final class TestService
        implements Service
    {
        boolean closed = false;

        public boolean isShutdown()
        {
            return closed;
        }

        public void shutdown()
        {
            closed = true;
        }
    }
}
