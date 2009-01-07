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
package org.sonatype.appbooter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.appbooter.ctl.OutOfProcessController;
import org.sonatype.appbooter.ctl.Service;

/**
 * Main class for booting plexus apps in standalone model
 * 
 * @version $Id: PlexusContainerHost.java 5337 2008-04-23 21:09:39Z jdcasey $
 * @since 1.0
 */
public class PlexusContainerHost
    implements Service
{
    public static final String CONFIGURATION_FILE_PROPERTY = "plexus.configuration";

    public static final String ENABLE_CONTROL_SOCKET = "plexus.host.control.socket.enabled";

    public static final String DEV_MODE = "plexus.container.dev.mode";

    public static final int DEFAULT_CONTROL_PORT = 32001;

    private static final long MANAGEMENT_THREAD_JOIN_TIMEOUT = 5000;

    private static final Object waitObj = new Object();

    private File configuration;

    private ClassWorld world;

    private int controlPort = DEFAULT_CONTROL_PORT;

    private boolean isShutdown;

    private boolean isStopped;

    private PlexusContainer container;

    private File basedir;

    private Thread managementThread;

    private static final String PLEXUS_ENV_VAR_PREFIX = "PLEXUS_";

    private static final String PLEXUS_SYSTEM_PROP_PREFIX = "plexus.";

    public PlexusContainerHost( ClassWorld world, int controlPort )
    {
        this.controlPort = controlPort;

        this.world = world;

        String configPath = System.getProperty( CONFIGURATION_FILE_PROPERTY );
        if ( configPath == null )
        {
            basedir = new File( System.getProperty( "basedir" ) );
            configuration = new File( basedir, "conf/plexus.xml" );
        }
        else
        {
            configuration = new File( configPath );
        }

        initManagementThread();
    }

    public PlexusContainerHost( ClassWorld world )
    {
        this( world, DEFAULT_CONTROL_PORT );
    }

    public PlexusContainerHost( ClassWorld world, File configuration )
    {
        this.world = world;

        this.configuration = configuration;

        initManagementThread();
    }

    private void initManagementThread()
    {
        if ( Boolean.getBoolean( ENABLE_CONTROL_SOCKET ) )
        {
            System.out.println( "\n\nStarting control socket on port: " + this.controlPort + "\n" );

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

    /**
     * The Plexus Container Context map is created by inspecting various sources from environment (env vars, system
     * props, plexus.properties) to allow users to override values as needed without tampering with config files.
     * 
     * @return
     * @throws InterpolationException
     */
    protected Map<Object, Object> createContainerContext()
        throws InterpolationException
    {
        // environment is a map of properties that comes from "environment": env vars and JVM system properties.
        // Keys found in this map are collected in this order, and the latter added will always replace any pre-existing
        // key:
        //
        // - basedir is put initially
        // - env vars
        // - system properties (will "stomp" env vars)
        //
        // As next step, the plexus.properties file is searched. If found, it will be loaded and filtered out for any
        // key that exists in environment map, and finally interpolation will be made against the "union" of those two.
        // The interpolation sources used in interpolation are: plexusProperties, environment and
        // System.getProperties().
        // The final interpolated values are put into containerContext map and returned.
        Map<Object, Object> environment = new HashMap<Object, Object>();

        Map<Object, Object> containerContext = new HashMap<Object, Object>();

        if ( basedir != null )
        {
            environment.put( "basedir", basedir.getAbsolutePath() );
        }

        /*
         * Iterate through environment variables, insert all items into a map (making sure to do translation needed,
         * remove PLEXUS_ , change all _ to - and convert to lower case)
         */
        Map<String, String> envMap = System.getenv();

        for ( String key : envMap.keySet() )
        {
            if ( key.toUpperCase().startsWith( PLEXUS_ENV_VAR_PREFIX ) && key.length() > PLEXUS_ENV_VAR_PREFIX.length() )
            {
                String plexusKey = key.toLowerCase().substring( PLEXUS_ENV_VAR_PREFIX.length() ).replace( '_', '-' );

                environment.put( plexusKey, envMap.get( key ) );
            }
        }

        /*
         * Iterate through system properties, insert all items into a map (making sure to do the translation needed,
         * remove plexus. )
         */
        Properties sysProps = System.getProperties();

        for ( Object obj : sysProps.keySet() )
        {
            String key = obj.toString();

            if ( key.startsWith( PLEXUS_SYSTEM_PROP_PREFIX ) && key.length() > PLEXUS_SYSTEM_PROP_PREFIX.length() )
            {
                String plexusKey = key.substring( PLEXUS_SYSTEM_PROP_PREFIX.length() );

                environment.put( plexusKey, sysProps.get( obj ) );
            }
        }

        /*
         * Iterate through plexus.properties, insert all items into a map add into plexus context using a
         * RegexBasedInterpolator.
         */
        File containerPropertiesFile = new File( configuration.getParentFile(), "plexus.properties" );

        Properties containerProperties = new Properties();

        if ( containerPropertiesFile.exists() )
        {
            try
            {
                containerProperties.load( new FileInputStream( containerPropertiesFile ) );
            }
            catch ( IOException e )
            {
                System.err.println( "Failed to load plexus properties: " + containerPropertiesFile );

                containerProperties.clear();
            }

            // filter the keys in containerProperties with keys from environment
            for ( Object envKey : environment.keySet() )
            {
                containerProperties.remove( envKey );
            }
        }

        // interpolate what we have
        Interpolator interpolator = new RegexBasedInterpolator();

        interpolator.addValueSource( new MapBasedValueSource( containerProperties ) );
        interpolator.addValueSource( new MapBasedValueSource( System.getProperties() ) );
        interpolator.addValueSource( new MapBasedValueSource( environment ) );

        for ( Object key : containerProperties.keySet() )
        {
            containerContext.put( key, interpolator.interpolate( (String) containerProperties.get( key ) ) );
        }
        for ( Object key : environment.keySet() )
        {
            containerContext.put( key, interpolator.interpolate( (String) environment.get( key ) ) );
        }

        // Now that we have containerContext with proper values, set them back into System properties and
        // dump them to System.out for reference.
        for ( Entry<Object, Object> entry : containerContext.entrySet() )
        {
            String key = (String) entry.getKey();

            String value = (String) entry.getValue();

            // adjust the key name and put it back to System properties
            String sysPropKey = PLEXUS_SYSTEM_PROP_PREFIX + key;

            if ( System.getProperty( sysPropKey ) == null )
            {
                System.setProperty( sysPropKey, (String) value );
            }

            // dump it to System.out
            System.out.println( "Property with KEY: '" + key + "', VALUE: '" + value + "' inserted into plexus context." );
        }

        return containerContext;
    }

    /**
     * This method will start the container, this is a non-blocking method, and will return once container has started
     * 
     * @throws Exception
     */
    public void startContainer()
        throws Exception
    {
        ContainerConfiguration cc = new DefaultContainerConfiguration()
            .setClassWorld( world ).setContainerConfiguration( configuration.getAbsolutePath() ).setContext(
                createContainerContext() );

        container = new DefaultPlexusContainer( cc );

    }

    /**
     * Destroy the running container
     */
    public void stopContainer()
    {
        if ( container != null )
        {
            container.dispose();
            container = null;
        }
    }

    /**
     * This method will start the container, this is a blocking method, and will return once interrupted and told to
     * shutdown
     */
    public void startPlexusContainer()
    {
        try
        {
            startContainer();

            while ( !isShutdown() )
            {
                try
                {
                    synchronized ( waitObj )
                    {
                        waitObj.wait();

                        // If a stop was requested, just stop the container, not everything
                        // as we will have the ability to start at a later time
                        if ( isStopped() )
                        {
                            stopContainer();
                        }
                        // On a shutodwn, we need to take everything down
                        else if ( isShutdown() )
                        {
                            stopContainer();
                            stopManagementThread();
                        }
                        // If neither, we have been notified to start the container
                        else
                        {
                            startContainer();
                        }
                    }
                }
                catch ( InterruptedException e )
                {
                    // If a stop was requested, just stop the container, not everything
                    // as we will have the ability to start at a later time
                    if ( isStopped() )
                    {
                        stopContainer();
                    }
                    // On a shutodwn, we need to take everything down
                    else if ( isShutdown() )
                    {
                        stopContainer();
                        stopManagementThread();
                    }
                    // If neither, we have been notified to start the container
                    else
                    {
                        startContainer();
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

    public static void main( String[] args, ClassWorld classWorld )
    {
        // get the port from args
        int controlPort = getControlPortFromArgs( args );

        PlexusContainerHost containerHost = new PlexusContainerHost( classWorld, controlPort );

        System.out.println( "Starting container" );
        if ( Boolean.getBoolean( ENABLE_CONTROL_SOCKET ) )
        {
            Runnable shutdownHook = new ShutdownRunnable( containerHost );
            Runtime.getRuntime().addShutdownHook( new Thread( shutdownHook ) );
        }
        containerHost.startPlexusContainer();
    }

    /**
     * Parses the <code>args</code> if a single element exists and is a integer the value returned. Otherwise the
     * <code>DEFAULT_CONTROL_PORT</code> is used.
     * 
     * @param args Command line arguments.
     * @return The control port.
     */
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

    private static final class ShutdownRunnable
        implements Runnable
    {
        private PlexusContainerHost host;

        private ShutdownRunnable( PlexusContainerHost host )
        {
            this.host = host;
        }

        public void run()
        {
            host.stopManagementThread();
        }
    }

    private void stopManagementThread()
    {
        if ( managementThread != null && managementThread.isAlive() )
        {
            synchronized ( managementThread )
            {
                managementThread.interrupt();

                try
                {
                    managementThread.join( PlexusContainerHost.MANAGEMENT_THREAD_JOIN_TIMEOUT );
                }
                catch ( InterruptedException e )
                {
                    // pass it on.
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public boolean isShutdown()
    {
        return isShutdown;
    }

    public void shutdown()
    {
        isShutdown = true;
        synchronized ( waitObj )
        {
            waitObj.notify();
        }
    }

    public boolean isStopped()
    {
        return isStopped;
    }

    public void stop()
    {
        isStopped = true;
        synchronized ( waitObj )
        {
            waitObj.notify();
        }
    }

    public void start()
    {
        isStopped = false;
        synchronized ( waitObj )
        {
            waitObj.notify();
        }
    }

}
