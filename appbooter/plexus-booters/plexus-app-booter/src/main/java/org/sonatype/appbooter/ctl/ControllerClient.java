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

    private Socket socket;

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
        ControllerUtil.close( socket );
        state = SHUTDOWN;
    }

    /**
     * Set the Controller to shutdown the application if this client's socket is closed.
     *
     * @throws ControlConnectionException
     * @throws IOException
     */
    public void shutdownOnClose()
        throws ControlConnectionException, IOException
    {
        System.out.println( "Requesting Shutdown-On-Close on Port " + port + "..." );

        byte response = sendControlMessage( ControllerVocabulary.SHUTDOWN_ON_CLOSE );

        if ( ControllerVocabulary.SHUTDOWN_ON_CLOSE == response )
        {
            System.out.println( "...Requested Shutdown-On-Close on Port " + port + " Completed." );
        }
        else
        {
            System.out.println( "...Requested Shutdown on Port " + port + " Completed without ACK from server - " + response );
        }

        System.out.println( "Application will shutdown if this client\'s connection closes." );

    }

    /**
     * Set the Controller to ignore this client's socket closing, and continue operating.
     *
     * @throws ControlConnectionException
     * @throws IOException
     */
    public void detachOnClose()
        throws ControlConnectionException, IOException
    {
        System.out.println( "Requesting Detach-On-Close on Port " + port + "..." );

        byte response = sendControlMessage( ControllerVocabulary.DETACH_ON_CLOSE );

        if ( ControllerVocabulary.DETACH_ON_CLOSE == response )
        {
            state = SHUTDOWN;
            System.out.println( "...Requested Detach-On-Close on Port " + port + " Completed" );
        }
        else
        {
            System.out.println( "...Requested Detach-On-Close on Port " + port + " Completed without ACK from server - " + response );
        }

        System.out.println( "Application will -NOT- shutdown if this client's connection closes." );

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

        if ( ControllerVocabulary.SHUTDOWN_SERVICE == response )
        {
            System.out.println( "...Requested Shutdown on Port " + port + " Completed" );
        }
        else
        {
            System.out.println( "...Requested Shutdown on Port " + port + " Completed without ACK from server - " + response );
        }

        close();
    }

    public void stop()
        throws ControlConnectionException, IOException
    {
        System.out.println( "Requesting Stop on Port " + port + "..." );

        byte response = sendControlMessage( ControllerVocabulary.STOP_SERVICE );

        if ( ControllerVocabulary.STOP_SERVICE == response )
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

        if ( ControllerVocabulary.START_SERVICE == response)
        {
            state = RUNNING;
            System.out.println( "...Requested Start on Port " + port + " Completed" );
        }
        else
        {
            System.out.println( "...Requested Start on Port " + port + " Completed without ACK from server - " + response );
        }
    }

    private synchronized byte sendControlMessage( byte command )
        throws ControlConnectionException, IOException
    {
        byte response = 0x0;

        if ( socket == null )
        {
            try
            {
                socket = new Socket( address, port );
            }
            catch( IOException e )
            {
                throw new ControlConnectionException( e );
            }
        }

        socket.getOutputStream().write( command );
        response = (byte) socket.getInputStream().read();

        return response;
    }
}
