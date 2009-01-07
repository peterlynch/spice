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
import java.net.Socket;

import junit.framework.TestCase;

public class UserDrivenOutOfProcessControllerTest
    extends TestCase
{

    private void printTestStart()
    {
        StackTraceElement element = new Throwable().getStackTrace()[1];
        System.out.println( "\n\n\nRunning test: " + element.getMethodName() + "\n\n" );
    }

    public void test_UserDriven_ReadBlocking()
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

        System.out.println( "Press -ENTER- to terminate test." );
        System.in.read();

        sock.close();

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
