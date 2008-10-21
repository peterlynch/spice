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
import java.net.Socket;
import java.net.UnknownHostException;

import junit.framework.TestCase;

public class OutOfProcessControllerTest
    extends TestCase
{

    public void testThreadInterruption()
        throws UnknownHostException, InterruptedException
    {
        printTestStart();

        TestService svc = new TestService();

        Thread managementThread = OutOfProcessController.manage( svc, 32001 );

        synchronized ( managementThread )
        {
			Thread.currentThread().sleep( 6 );
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

        sendCommand( port, ControllerVocabulary.STOP_SERVICE );

        Thread.sleep( 100 );

        assertTrue( "Service should be stopped.", svc.stopped );

        sendCommand( port, ControllerVocabulary.START_SERVICE );

        Thread.sleep( 100 );

        assertFalse( "Service should not be stopped.", svc.stopped );

        sendCommand( port, ControllerVocabulary.SHUTDOWN_SERVICE );

        Thread.sleep( 100 );

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

    private void sendCommand( int port,
                              byte command )
        throws IOException
    {
        Socket sock = new Socket( InetAddress.getLocalHost(), port );
        sock.setTcpNoDelay( true );
        sock.setSoLinger( true, 0 );

        byte[] data = {
            command
        };
        sock.getOutputStream().write( data, 0, 1 );

        sock.getInputStream().read( data );

        assertEquals( "ACK not received", command, data[0] );

        sock.close();
    }

    public void testShutdownCommand()
        throws IOException, InterruptedException
    {
        printTestStart();

        TestService svc = new TestService();

        int port = 32001;

        Thread managementThread = OutOfProcessController.manage( svc, port );

        Thread.sleep( 100 );

        sendCommand( port, ControllerVocabulary.SHUTDOWN_SERVICE );

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

    public void testShutdownOnCloseCommand()
        throws IOException, InterruptedException
    {
        printTestStart();

        TestService svc = new TestService();

        int port = 32001;

        Thread managementThread = OutOfProcessController.manage( svc, port );

        Thread.sleep( 100 );

        Socket sock = new Socket( InetAddress.getLocalHost(), port );
        sock.setTcpNoDelay( true );
        sock.setSoLinger( true, 0 );

        byte[] data = {
            ControllerVocabulary.SHUTDOWN_ON_CLOSE
        };
        sock.getOutputStream().write( data, 0, 1 );

        sock.getInputStream().read( data );

        assertEquals( "ACK not received", ControllerVocabulary.SHUTDOWN_ON_CLOSE, data[0] );

        assertFalse( "Service should not shutdown until socket is closed.", svc.shutdown );

        sock.close();

        Thread.sleep( 100 );

        assertTrue( "Service should have been shutdown when socket was closed.", svc.shutdown );

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

    public void testDetachOnCloseCommand()
        throws IOException, InterruptedException
    {
        printTestStart();

        TestService svc = new TestService();

        int port = 32001;

        Thread managementThread = OutOfProcessController.manage( svc, port );

        Thread.sleep( 100 );

        Socket sock = new Socket( InetAddress.getLocalHost(), port );
        sock.setTcpNoDelay( true );
        sock.setSoLinger( true, 0 );

        byte[] data = {
            ControllerVocabulary.SHUTDOWN_ON_CLOSE
        };
        sock.getOutputStream().write( data, 0, 1 );

        sock.getInputStream().read( data );

        assertEquals( "ACK not received", ControllerVocabulary.SHUTDOWN_ON_CLOSE, data[0] );

        // now, cancel the shutdown-on-close command, to allow the service to persist after the socket closes.
        data[0] = ControllerVocabulary.DETACH_ON_CLOSE;
        sock.getOutputStream().write( data, 0, 1 );

        sock.getInputStream().read( data );

        assertEquals( "ACK not received", ControllerVocabulary.DETACH_ON_CLOSE, data[0] );

        sock.close();

        Thread.sleep( 100 );

        assertFalse( "Service should not be shutdown until shutdown command is given.",
                     svc.shutdown );

        sendCommand( port, ControllerVocabulary.SHUTDOWN_SERVICE );

        Thread.sleep( 100 );

        assertTrue( "Service should have been shutdown when socket was closed.", svc.shutdown );

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

    public void testUnknownCommand()
        throws IOException, InterruptedException
    {
        printTestStart();

        TestService svc = new TestService();

        int port = 32001;

        Thread managementThread = OutOfProcessController.manage( svc, port );

        Thread.sleep( 100 );

        sendCommand( port, (byte) 0x0 );

        Thread.sleep( 100 );

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

        System.out.println( "management thread should have died." );

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
