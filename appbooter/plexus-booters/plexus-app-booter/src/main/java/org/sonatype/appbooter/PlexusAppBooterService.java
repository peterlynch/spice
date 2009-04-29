package org.sonatype.appbooter;

import java.net.UnknownHostException;

import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.appbooter.ctl.AppBooterServiceException;
import org.sonatype.appbooter.ctl.OutOfProcessController;
import org.sonatype.appbooter.ctl.Service;

/**
 * A flavor that is a little bit more complicated. It spawns a management thread, and opens
 * 
 * @author cstamas
 * @since 2.0
 */
public class PlexusAppBooterService
    extends PlexusAppBooter
    implements Service
{
    public static final String ENABLE_CONTROL_SOCKET = "plexus.host.control.socket.enabled";

    public static final int DEFAULT_CONTROL_PORT = 32001;

    private Thread managementThread;

    private boolean shutdown = false;

    private boolean running = false;

    private int controlPort = DEFAULT_CONTROL_PORT;

    public int getControlPort()
    {
        return controlPort;
    }

    public void setControlPort( int controlPort )
    {
        this.controlPort = controlPort;
    }

    @Override
    public void startContainer()
        throws PlexusContainerException
    {
        super.startContainer();

        startManagementThread();
    }

    @Override
    public void startContainerAndBlockThread()
        throws PlexusContainerException
    {
        try
        {
            running = true;

            startContainer();

            while ( !isShutdown() )
            {
                synchronized ( waitObj )
                {
                    try
                    {
                        waitObj.wait();
                    }
                    catch ( InterruptedException e )
                    {
                    }

                    // If a stop was requested, just stop the container, not everything
                    // as we will have the ability to start at a later time
                    if ( isStopped() || isShutdown() )
                    {
                        stopContainer();
                    }
                    else
                    {
                        startContainer();
                    }

                    // On a shutodwn, we need to take everything down
                    if ( isShutdown() )
                    {
                        // other
                    }
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();

            System.exit( 2 );
        }
    }

    @Override
    public void stopContainer()
    {
        super.stopContainer();

        stopManagementThread();
    }

    protected void startManagementThread()
    {
        if ( Boolean.getBoolean( ENABLE_CONTROL_SOCKET ) )
        {
            System.out.println( "\n\nStarting control socket on port: " + this.controlPort + "\n" );

            synchronized ( managementThread )
            {
                try
                {
                    managementThread = OutOfProcessController.manage( this, this.controlPort );

                    System.out.println( "\n\nStarted control socket on port: " + this.controlPort + "\n" );
                }
                catch ( UnknownHostException e )
                {
                    System.out.println( "Unable to start management thread: " + e.getMessage() );
                }
            }
        }
    }

    protected void stopManagementThread()
    {
        if ( managementThread != null && managementThread.isAlive() )
        {
            synchronized ( managementThread )
            {
                managementThread.interrupt();

                try
                {
                    managementThread.join( 5000 );
                }
                catch ( InterruptedException e )
                {
                    // pass it on.
                    Thread.currentThread().interrupt();
                }

                managementThread = null;
            }
        }
    }

    // Service API

    public boolean isShutdown()
    {
        return shutdown;
    }

    public boolean isStopped()
    {
        return !running;
    }

    public void start()
        throws AppBooterServiceException
    {
        synchronized ( waitObj )
        {
            running = true;

            shutdown = false;

            waitObj.notify();
        }
    }

    public void stop()
        throws AppBooterServiceException
    {
        synchronized ( waitObj )
        {
            running = false;

            shutdown = false;

            waitObj.notify();
        }
    }

    public void shutdown()
        throws AppBooterServiceException
    {
        synchronized ( waitObj )
        {
            running = false;

            shutdown = true;

            waitObj.notify();
        }
    }

    // mains

    private static int getControlPortFromArgs( String[] args )
    {

        // if we need to get more involved we could use commons-cli's CommandLineParser
        // but for a single arg that seems overkill, what would be nice, is if we parsed
        // all of the args and put them in the context of the container.

        int controlPort = DEFAULT_CONTROL_PORT;

        // grunt parsing..
        if ( args != null && args.length == 1 )
        {
            String tmpPortString = args[0];
            if ( StringUtils.isNotEmpty( tmpPortString ) && StringUtils.isNumeric( tmpPortString ) )
            {
                try
                {
                    controlPort = Integer.parseInt( tmpPortString );
                }
                // this should never happen, well, maybe if you pass in a long
                catch ( NumberFormatException e )
                {
                    System.out.println( "Error parsing command line args: " + e.getMessage() );
                    System.out.println( "Using default control port: " + DEFAULT_CONTROL_PORT );
                }
            }
        }

        return controlPort;
    }

    public static void main( String[] args, ClassWorld classWorld )
    {
        try
        {
            PlexusAppBooterService service = new PlexusAppBooterService();

            service.setWorld( classWorld );

            service.setControlPort( getControlPortFromArgs( args ) );

            System.out.println( "Starting container" );

            service.startContainerAndBlockThread();
        }
        catch ( PlexusContainerException e )
        {
            e.printStackTrace();
        }
    }

    public static void main( String[] args )
    {
        main( args, null );
    }

}
