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

import junit.framework.TestCase;

public class OutOfProcessControllerTest
    extends TestCase
{

    public void testThreadInterruption()
        throws UnknownHostException
    {
        printTestStart();

        TestService svc = new TestService();

        Thread managementThread = OutOfProcessController.manage( svc, 32001 );

        synchronized ( managementThread )
        {
            managementThread.interrupt();

            if ( managementThread.isAlive() )
            {
                try
                {
                    System.out.println( "Joining management thread." );
                    managementThread.join( /* OutOfProcessController.SLEEP_PERIOD + 1000 */);
                }
                catch ( InterruptedException e )
                {
                    System.out.println( "Interrupted." );
                }
            }
        }

        assertFalse( "Thread should have died.", managementThread.isAlive() );
        assertTrue( "Service should have been shutdown.", svc.shutdown );
    }

    private void printTestStart()
    {
        StackTraceElement element = new Throwable().getStackTrace()[1];
        System.out.println( "\n\n\nRunning test: " + element.getMethodName() + "\n\n" );
    }

    public void testStopStartCommand()
        throws IOException, InterruptedException
    {
        printTestStart();

        TestService svc = new TestService();

        int port = 32001;

        Thread managementThread = OutOfProcessController.manage( svc, port );

        Thread.sleep( 100 );

        SocketChannel channel = SocketChannel.open( new InetSocketAddress( InetAddress.getLocalHost(), port ) );
        ByteBuffer buffer = ByteBuffer.allocate( 1 );

        buffer.put( ControllerVocabulary.STOP_SERVICE );
        buffer.rewind();

        channel.write( buffer );

        buffer.rewind();
        
        Thread.sleep( 100 );

        channel.read( buffer );
        buffer.flip();

        assertEquals( "ACK not received", ControllerVocabulary.ACK, buffer.get() );
        assertTrue( "Service should be stopped.", svc.stopped );
        
        channel.close();

        Thread.sleep( 100 );

        channel = SocketChannel.open( new InetSocketAddress( InetAddress.getLocalHost(), port ) );
        buffer = ByteBuffer.allocate( 1 );

        buffer.put( ControllerVocabulary.START_SERVICE );
        buffer.rewind();

        channel.write( buffer );

        buffer.rewind();
        
        Thread.sleep( 100 );

        channel.read( buffer );
        buffer.flip();

        assertEquals( "ACK not received", ControllerVocabulary.ACK, buffer.get() );
        assertFalse( "Service should not be stopped.", svc.stopped );
        
        channel.close();
        
        Thread.sleep( 100 );

        channel = SocketChannel.open( new InetSocketAddress( InetAddress.getLocalHost(), port ) );
        buffer = ByteBuffer.allocate( 1 );

        buffer.put( ControllerVocabulary.SHUTDOWN_SERVICE );
        buffer.rewind();

        channel.write( buffer );

        buffer.rewind();

        Thread.sleep( 100 );
        
        channel.read( buffer );
        buffer.flip();

        assertEquals( "ACK not received", ControllerVocabulary.ACK, buffer.get() );
        assertTrue( "Service should be shutdown.", svc.shutdown );

        synchronized ( managementThread )
        {
            if ( managementThread.isAlive() )
            {
                try
                {
                    System.out.println( "Joining management thread." );
                    managementThread.join( /* OutOfProcessController.SLEEP_PERIOD + 1000 */);
                }
                catch ( InterruptedException e )
                {
                    System.out.println( "Interrupted." );
                }
            }
        }

        assertFalse( "Thread should have died.", managementThread.isAlive() );
    }

    public void testShutdownCommand()
        throws IOException, InterruptedException
    {
        printTestStart();

        TestService svc = new TestService();

        int port = 32001;

        Thread managementThread = OutOfProcessController.manage( svc, port );

        Thread.sleep( 100 );

        SocketChannel channel = SocketChannel.open( new InetSocketAddress( InetAddress.getLocalHost(), port ) );
        ByteBuffer buffer = ByteBuffer.allocate( 1 );

        buffer.put( ControllerVocabulary.SHUTDOWN_SERVICE );
        buffer.rewind();

        channel.write( buffer );

        buffer.rewind();

        channel.read( buffer );
        buffer.flip();

        assertEquals( ControllerVocabulary.ACK, buffer.get() );

        synchronized ( managementThread )
        {
            if ( managementThread.isAlive() )
            {
                try
                {
                    System.out.println( "Joining management thread." );
                    managementThread.join( /* OutOfProcessController.SLEEP_PERIOD + 1000 */);
                }
                catch ( InterruptedException e )
                {
                    System.out.println( "Interrupted." );
                }
            }
        }

        assertFalse( "Thread should have died.", managementThread.isAlive() );
        assertTrue( "Service should have been shutdown.", svc.shutdown );
    }

    public void testUnknownCommand()
        throws IOException, InterruptedException
    {
        printTestStart();

        TestService svc = new TestService();

        int port = 32001;

        Thread managementThread = OutOfProcessController.manage( svc, port );

        Thread.sleep( 100 );

        SocketChannel channel = SocketChannel.open( new InetSocketAddress( InetAddress.getLocalHost(), port ) );
        ByteBuffer buffer = ByteBuffer.allocate( 1 );

        buffer.put( (byte) 0x0 );
        buffer.rewind();

        channel.write( buffer );

        buffer.rewind();

        channel.read( buffer );
        buffer.flip();

        assertEquals( ControllerVocabulary.ACK, buffer.get() );
        assertFalse( "Service should NOT have been shutdown.", svc.shutdown );

        synchronized ( managementThread )
        {
            managementThread.interrupt();

            if ( managementThread.isAlive() )
            {
                try
                {
                    System.out.println( "Joining management thread." );
                    managementThread.join( /* OutOfProcessController.SLEEP_PERIOD + 1000 */);
                }
                catch ( InterruptedException e )
                {
                    System.out.println( "Interrupted." );
                }
            }
        }

        assertFalse( "Thread should have died.", managementThread.isAlive() );
        assertTrue( "Service should have been shutdown.", svc.shutdown );
    }

    private static final class TestService
        implements Service
    {
        boolean shutdown = false;

        boolean stopped = false;

        public boolean isShutdown()
        {
            return shutdown;
        }

        public void shutdown()
        {
            shutdown = true;
        }

        public boolean isStopped()
        {
            return stopped;
        }

        public void stop()
        {
            stopped = true;
        }

        public void start()
        {
            stopped = false;
        }
    }
}
