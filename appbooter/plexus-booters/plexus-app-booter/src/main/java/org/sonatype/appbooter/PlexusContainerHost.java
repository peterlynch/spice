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
package org.sonatype.appbooter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.util.interpolation.MapBasedValueSource;
import org.codehaus.plexus.util.interpolation.RegexBasedInterpolator;
import org.sonatype.appbooter.ctl.OutOfProcessController;
import org.sonatype.appbooter.ctl.Service;

/**
 * Main class for booting plexus apps in standalone model
 * @version $Id: PlexusContainerHost.java 5337 2008-04-23 21:09:39Z jdcasey $
 * @since 1.0
 */
public class PlexusContainerHost
    implements Service
{

    public static final String CONFIGURATION_FILE_PROPERTY = "plexus.configuration";

    public static final String ENABLE_CONTROL_SOCKET = "plexus.host.control.socket.enabled";

    public static final int CONTROL_PORT = 32001;

    private static final long MANAGEMENT_THREAD_JOIN_TIMEOUT = 5000;

    private static final Object waitObj = new Object();

    private File configuration;

    private ClassWorld world;

    private boolean isStopped;

    private PlexusContainer container;

    private File basedir;

    private Thread managementThread;

    public PlexusContainerHost( ClassWorld world )
    {
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
            System.out.println( "\n\nStarting control socket on port: " + CONTROL_PORT + "\n" );

            try
            {
                managementThread = OutOfProcessController.manage( this, CONTROL_PORT );
                System.out.println( "\n\nStarted control socket on port: " + CONTROL_PORT + "\n" );
            }
            catch ( UnknownHostException e )
            {
                System.out.println( "Unable to start management thread: " + e.getMessage() );
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected Map createContainerContext()
    {
        Map containerContext = new HashMap();

        if ( basedir != null )
        {
            containerContext.put( "basedir", basedir.getAbsolutePath() );
        }

        File containerPropertiesFile = new File( configuration.getParentFile(), "plexus.properties" );

        if ( containerPropertiesFile.exists() )
        {
            Properties containerProperties = new Properties();

            try
            {
                containerProperties.load( new FileInputStream( containerPropertiesFile ) );
            }
            catch ( IOException e )
            {
                System.err.println( "Failed to load plexus properties: " + containerPropertiesFile );
            }

            RegexBasedInterpolator ip = new RegexBasedInterpolator();

            ip.addValueSource( new MapBasedValueSource( containerProperties ) );

            ip.addValueSource( new MapBasedValueSource( System.getProperties() ) );

            for ( Enumeration n = containerProperties.propertyNames(); n.hasMoreElements(); )
            {
                String propertyKey = (String) n.nextElement();

                String propertyValue = ip.interpolate( containerProperties.getProperty( propertyKey ), "" );

                containerContext.put( propertyKey, propertyValue );
            }
        }

        return containerContext;
    }

    public void startContainer()
        throws Exception
    {
        ContainerConfiguration cc = new DefaultContainerConfiguration()
            .setClassWorld( world )
            .setContainerConfiguration( configuration.getAbsolutePath() )
            .setContext( createContainerContext() );

        container = new DefaultPlexusContainer( cc );

    }

    public void stopContainer()
    {
        if ( container != null )
        {
            container.dispose();
            container = null;
        }
    }

    public void start()
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
                    }
                }
                catch ( InterruptedException e )
                {
                    stopContainer();
                    stopManagementThread();
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.exit( 2 );
        }
    }

    public static void main( String[] args,
                             ClassWorld classWorld )
    {
        PlexusContainerHost containerHost = new PlexusContainerHost( classWorld );
        
        System.out.println( "Starting container" );
        if ( Boolean.getBoolean( ENABLE_CONTROL_SOCKET ) )
        {
            Runnable shutdownHook = new ShutdownRunnable( containerHost );
            Runtime.getRuntime().addShutdownHook( new Thread( shutdownHook ) );
        }
        containerHost.start();
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

    public boolean isShutdown()
    {
        return isStopped;
    }

    public void shutdown()
    {   
        isStopped = true;
        System.out.println( "Stopping container" );
        synchronized ( waitObj )
        {
            waitObj.notify();
        }
    }
    
    private void stopManagementThread()
    {
        if ( managementThread != null && managementThread.isAlive() )
        {
            synchronized( managementThread )
            {
                managementThread.interrupt();

                try
                {
                    managementThread.join( PlexusContainerHost.MANAGEMENT_THREAD_JOIN_TIMEOUT );
                }
                catch ( InterruptedException e )
                {
                }
            }
        }
    }
}
