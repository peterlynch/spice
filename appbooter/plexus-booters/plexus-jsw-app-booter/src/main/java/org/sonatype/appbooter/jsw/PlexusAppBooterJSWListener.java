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
package org.sonatype.appbooter.jsw;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.sonatype.appbooter.PlexusAppBooterService;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

/**
 * This class is used to boot a plexus container from the Java Service Wrapper. It implements the WrapperListener
 * interface so that we can directly respond to selected events and also to properly shutdown the container.
 * 
 * @author brianf@sonatype.com
 */
public class PlexusAppBooterJSWListener
    extends PlexusAppBooterService
    implements WrapperListener
{
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
        PlexusAppBooterJSWListener jswListener = new PlexusAppBooterJSWListener();
        
        jswListener.setCommandLineArguments( args );

        jswListener.setWorld( classWorld );

        jswListener.setControlPort( getControlPortFromArgs( args ) );

        WrapperManager.start( jswListener, args );

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

    /**
     * This is the main entry point. It creates an instance of this class and registers it as a listener with the
     * WrapperManager. It then blocks on waitObj indefinitely. The system will be shutdown when the stop() method is
     * called.
     * 
     * @param args
     */
    public static void main( String[] args )
    {
        main( args, null );
    }

}
