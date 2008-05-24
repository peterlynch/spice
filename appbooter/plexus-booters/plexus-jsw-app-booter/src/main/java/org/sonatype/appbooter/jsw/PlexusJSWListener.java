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
package org.sonatype.appbooter.jsw;

import java.io.File;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.sonatype.appbooter.PlexusContainerHost;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

/**
 * This class is used to boot a plexus container from the Java Service Wrapper. It implements the WrapperListener
 * interface so that we can directly respond to selected events and also to properly shutdown the container.
 * 
 * @author brianf@sonatype.com
 */
public class PlexusJSWListener
    extends PlexusContainerHost
    implements WrapperListener
{
    /**
     * Used to block the main method.
     */
    private static final Object waitObj = new Object();

    /**
     * Main Constructor
     * 
     * @param world instance of classworlds passed to the container
     */
    public PlexusJSWListener( ClassWorld world )
    {
        super( world );
    }

    /**
     * Constructor with File configuration.
     * 
     * @param world instance of classworlds passed to the container
     * @param configuration file of the plexus configuration
     */
    public PlexusJSWListener( ClassWorld world, File configuration )
    {
        super( world, configuration );
    }

    /**
     * Called whenever the native wrapper code traps a system control signal against the Java process. It is up to the
     * callback to take any actions necessary. Possible values are: WrapperManager.WRAPPER_CTRL_C_EVENT,
     * WRAPPER_CTRL_CLOSE_EVENT, WRAPPER_CTRL_LOGOFF_EVENT, or WRAPPER_CTRL_SHUTDOWN_EVENT
     */
    public void controlEvent( int event )
    {
        if ( ( event == WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT ) && WrapperManager.isLaunchedAsService() )
        {
            // Ignore
            if ( WrapperManager.isDebugEnabled() )
            {
                WrapperManager.log( WrapperManager.WRAPPER_LOG_LEVEL_DEBUG, "PlexusJSWListener: controlEvent(" + event
                    + ") Ignored" );
            }
        }
        else
        {
            if ( WrapperManager.isDebugEnabled() )
            {
                WrapperManager.log( WrapperManager.WRAPPER_LOG_LEVEL_DEBUG, "PlexusJSWListener: controlEvent(" + event
                    + ") Stopping" );
            }

            WrapperManager.stop( 0 );
            // Will not get here.
        }
    }

    /**
     * Starts the container in a non-blocking fashion.
     */
    public Integer start( String[] arg0 )
    {
        WrapperManager.log( WrapperManager.WRAPPER_LOG_LEVEL_INFO, "Starting the Plexus Container." );

        try
        {
            WrapperManager.signalStarting( 30000 );

            startContainer();
        }
        catch ( Exception e )
        {
            WrapperManager.log( WrapperManager.WRAPPER_LOG_LEVEL_FATAL, "Unable to start the Container: "
                + e.getMessage() );

            return ( 2 );
        }
        return null;
    }

    /**
     * Stops the container in a non-blocking fashion.
     */
    public int stop( int arg0 )
    {
        WrapperManager.log( WrapperManager.WRAPPER_LOG_LEVEL_INFO, "Stopping the Plexus Container." );

        stopContainer();

        WrapperManager.log( WrapperManager.WRAPPER_LOG_LEVEL_INFO, "Plexus Container stopped." );

        return 0;
    }

    /**
     * This is the main entry point. It creates an instance of this class and registers it as a listener with the
     * WrapperManager. It then blocks on waitObj indefinitely. The system will be shutdown when the stop() method is
     * called.
     * 
     * @param args
     */
    public static void main( String[] args, ClassWorld classWorld )
    {
        // Start everything up and register as a listener.
        PlexusJSWListener containerHost = new PlexusJSWListener( classWorld );

        WrapperManager.start( containerHost, args );

        // once the wrapper is booted, the start() method will be called to actually get everything running.

        // now wait forever.
        try
        {
            synchronized ( waitObj )
            {
                waitObj.wait();
            }
        }
        catch ( InterruptedException e )
        {
        }
    }

}
