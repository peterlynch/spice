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
import java.net.Socket;
import java.net.UnknownHostException;

import junit.framework.TestCase;

public class OutOfProcessControllerTest
    extends TestCase
{

    private Thread managementThread;
    
    public void tearDown() throws Exception
    {
        if ( managementThread != null && managementThread.isAlive() )
        {
            System.out.println( "Cleaning up lingering management thread." );
            managementThread.interrupt();
        }
    }

    public void testThreadInterruption()
        throws UnknownHostException
    {
        printTestStart();

        TestService svc = new TestService();

        managementThread = OutOfProcessController.manage( svc, 32001 );

        synchronized ( managementThread )
        {
            System.out.println( "interrupting management thread." );
            managementThread.interrupt();
            System.out.println( "interrupted." );

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
                    Thread.currentThread().interrupt();
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
        throws Throwable
    {
        printTestStart();

        TestService svc = new TestService();

        int port = 32001;

        managementThread = OutOfProcessController.manage( svc, port );

        Thread.sleep( 100 );

        System.out.println( "Stopping service." );
        sendCommand( port, ControllerVocabulary.STOP_SERVICE );
        System.out.println( "should be stopped" );

        Thread.sleep( 100 );

        assertTrue( "Service should be stopped.", svc.stopped );

        System.out.println( "Starting service." );
        sendCommand( port, ControllerVocabulary.START_SERVICE );
        System.out.println( "should be started" );

        Thread.sleep( 100 );

        assertFalse( "Service should not be stopped.", svc.stopped );

        System.out.println( "Shutting down service." );
        sendCommand( port, ControllerVocabulary.SHUTDOWN_SERVICE );
        System.out.println( "should be shutdown" );

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
        throws Throwable
    {
        TestSocketRunnable r = new TestSocketRunnable( port, command );
        Thread t = new Thread( r );
        t.start();
        
        try
        {
            if ( t.isAlive() )
            {
                t.join( 5000 );
            }
            
            if ( t.isAlive() )
            {
                System.out.println( "Forcing test-socket shutdown." );
                
                t.interrupt();
                ControllerUtil.close( r.sock );
                
                fail( "Control socket had to be closed by force." );
            }
        }
        catch ( InterruptedException e )
        {
            System.out.println( "Interrupted while waiting for test command to be issued: " + e.getMessage() );
            Thread.currentThread().interrupt();
        }
        
        if ( r != null && r.exception != null )
        {
            throw r.exception;
        }
    }

    public void testShutdownCommand()
        throws Throwable
    {
        printTestStart();

        TestService svc = new TestService();

        int port = 32001;

        managementThread = OutOfProcessController.manage( svc, port );

        Thread.sleep( 100 );

        System.out.println( "Shutting down service." );
        sendCommand( port, ControllerVocabulary.SHUTDOWN_SERVICE );
        System.out.println( "should be shutdown." );
        
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

        managementThread = OutOfProcessController.manage( svc, port );

        Thread.sleep( 100 );

        Socket sock = new Socket( "127.0.0.1", port );
        sock.setTcpNoDelay( true );
        sock.setSoLinger( true, 0 );

        byte[] data = {
            ControllerVocabulary.SHUTDOWN_ON_CLOSE
        };
        sock.getOutputStream().write( data, 0, 1 );

        sock.getInputStream().read( data );

        assertEquals( "ACK not received", ControllerVocabulary.SHUTDOWN_ON_CLOSE, data[0] );

        assertFalse( "Service should not shutdown until socket is closed.", svc.shutdown );

        System.out.println( "Closing control socket." );
        sock.close();
        System.out.println( "Control socket closed. Service should be shutting down." );

        Thread.sleep( 2000 );

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
        throws Throwable
    {
        printTestStart();

        TestService svc = new TestService();

        int port = 32001;

        managementThread = OutOfProcessController.manage( svc, port );

        Thread.sleep( 100 );

        Socket sock = new Socket( "127.0.0.1", port );
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
        throws Throwable
    {
        printTestStart();

        TestService svc = new TestService();

        int port = 32001;

        managementThread = OutOfProcessController.manage( svc, port );

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
    
    private static final class TestSocketRunnable implements Runnable
    {
        
        private final int port;
        private final byte command;
        private Throwable exception;
        private Socket sock;

        private TestSocketRunnable( int port, byte command )
        {
            this.port = port;
            this.command = command;
        }

        public void run()
        {
            try
            {
                System.out.println( "Attempting to connect..." );
                sock = new Socket( "127.0.0.1", port );
                System.out.println( "Connected to: " + sock.getRemoteSocketAddress() );
                sock.setTcpNoDelay( true );
                sock.setSoLinger( true, 1 );

                byte[] data = {
                    command
                };
                
                System.out.println( "Writing control command: " + Integer.toHexString( command ) );
                sock.getOutputStream().write( data, 0, 1 );

                System.out.println( "Reading ACK" );
                sock.getInputStream().read( data );

                assertEquals( "ACK not received", command, data[0] );

                sock.close();
            }
            catch ( Exception e )
            {
                exception = e;
            }
        }
        
    }
}
