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
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.NotImplementedException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.StreamPumper;
import org.sonatype.appbooter.ctl.AppBooterServiceException;
import org.sonatype.appbooter.ctl.ControlConnectionException;
import org.sonatype.appbooter.ctl.ControllerClient;
import org.sonatype.plexus.classworlds.io.ClassworldsConfWriter;
import org.sonatype.plexus.classworlds.io.ClassworldsIOException;
import org.sonatype.plexus.classworlds.io.VelocityLogChute;
import org.sonatype.plexus.classworlds.model.ClassworldsAppConfiguration;
import org.sonatype.plexus.classworlds.model.ClassworldsRealmConfiguration;
import org.sonatype.plexus.classworlds.validator.ClassworldsModelValidator;
import org.sonatype.plexus.classworlds.validator.ClassworldsValidationResult;

/**
 * Start a Plexus application, and optionally wait for Ctl-C to shut it down. Otherwise, continue execution with the
 * application still running (useful for integration testing). The application is started in a separate process, with a
 * control port listening for administrative commands.
 */
public abstract class AbstractForkedAppBooter
    extends AbstractLogEnabled
    implements ForkedAppBooter
{

    /**
     * If true, do NOT wait for CTL-C to terminate the application, just start it and return. Future calls to stop() or
     * direct use of the {@link ControllerClient} API can manage the application once started.
     * 
     * @plexus.configuration default-value="false"
     */
    private boolean disableBlocking;

    /**
     * Turns on debug mode, which uses the debugJavaCmd to start the plexus application instead of the normal javaCmd.
     * 
     * @plexus.configuration default-value="false"
     */
    private boolean debug;

    /**
     * Java command used to start the Plexus application under normal (non-debug) circumstances.
     * 
     * @plexus.configuration default-value="java"
     */
    private String javaCmd;

    /**
     * Substitutes the given port into the expression '@DEBUG_PORT@' in your debugJavaCmd.
     * 
     * @plexus.configuration default-value="5005"
     */
    private int debugPort;

    /**
     * Substitutes 'y' or 'n' into the expression '@DEBUG_SUSPEND@' in your debugJavaCmd.
     * 
     * @plexus.configuration default-value="true"
     */
    private boolean debugSuspend;

    /**
     * Java command used to start the Plexus application into debugging mode, which is meant to allow attachment of a
     * remote application debugger via JPDA, etc.
     * 
     * @plexus.configuration default-value="java -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=@DEBUG_SUSPEND@,address=@DEBUG_PORT@ -Djava.compiler=NONE"
     */
    private String debugJavaCmd;

    /**
     * The class containing the main method that will be used to start up the Plexus container to initialize the
     * application. <br/>
     * CAUTION! Be sure you understand the ramifications before changing this!
     * 
     * @plexus.configuration default-value="org.sonatype.appbooter.PlexusContainerHost"
     */
    private String launcherClass;

    /**
     * @plexus.configuration
     */
    private File classworldsJar;

    /**
     * @plexus.configuration
     */
    private File classworldsConf;

    /**
     * System properties passed on to the new java process.
     * 
     * @plexus.configuration
     */
    private Map<String, String> systemProperties;

    /** @plexus.configuration default-value="${basedir}/src/main/plexus/plexus.xml" */
    private File configuration;

    /** @plexus.configuration default-value="${basedir}" */
    private File basedir;

    /** @plexus.configuration default-value="${basedir}/target/appbooter.tmp" */
    private File tempDir;

    /**
     * Number of milliseconds to wait after the application starts.
     * 
     * @plexus.configuration default-value="5000"
     */
    private int sleepAfterStart = 5000;

    /**
     * Uses DEFAULT_CONTROL_PORT from {@link PlexusContainerHost} by default. <br/>
     * This is the port used to administer the remote application. If you execute with disableBlocking == true, you may
     * need to know this port to use the {@link ControllerClient} API directly (from integration-test JUnit code, for
     * instance).
     * 
     * @plexus.configuration default-value="-1"
     */
    private int controlPort;

    private ControllerClient controlClient;

    StreamPumper outPumper = null;

    StreamPumper errPumper = null;

    /**
     * @plexus.configuration
     */
    private File containerProperties;

    /**
     * Returns the location of the plexus platform jar, i.e. plexus-platform-base.jar
     * 
     * @return
     * @throws AppBooterServiceException
     */
    public abstract File getPlatformFile()
        throws AppBooterServiceException;

    /**
     * For test purposes.
     * 
     * @param platformFile
     */
    public abstract void setPlatformFile( File platformFile );

    /**
     * Returns the Classworlds configuration for the process to be started.
     * 
     * @return
     */
    public abstract ClassworldsRealmConfiguration getClassworldsRealmConfig();

    @SuppressWarnings( "unchecked" )
    public void start()
        throws AppBooterServiceException
    {
        // Get the control objects for the 2 running threads we will be handling, the first (controlClient) is our
        // access to the PlexusContainerHost
        // NOTE: currently only PlexusContainerHost is supported as launcher class, as there are a number of areas
        // throughout where it is directly
        // referenced.
        // The second object we get (controlServiceClient) is for this object is strictly used for the ShutdownHook to
        // be able to stop.
        if ( !configuration.exists() )
        {
            throw new AppBooterServiceException(
                                                 "There is no plexus.xml file present. Make sure you are in a directory where a Plexus application lives." );
        }

        try
        {
            controlClient =
                new ControllerClient( controlPort > -1 ? controlPort : PlexusAppBooterService.DEFAULT_CONTROL_PORT );
        }
        catch ( UnknownHostException e )
        {
            throw new AppBooterServiceException(
                                                 "Remote-control client for plexus application cannot resolve localhost.",
                                                 e );
        }

        // Just in case stop() isn't called at some point later, and this is a non-blocking instance, here is the backup
        // plan
        getLogger().info( "Enabling shutdown hook for remote plexus application." );
        Runtime.getRuntime().addShutdownHook( new Thread( new ShutdownHook() ) );

        Commandline cli = buildCommandLine();

        getLogger().debug( "Executing: " + cli );
        executeCommandLine( cli );

        if ( debug )
        {
            // this is not working at windows
            // getLogger().info( "\n\n\nWaiting for you to attach your debugger. Press -ENTER- when attached." );
            // try
            // {
            // System.in.read();
            // }
            // catch ( IOException e )
            // {
            // getLogger().info( "Failed to read from System.in. Proceeding anyway..." );
            // }
        }
        else
        {
            getLogger().info( "Sleeping " + this.sleepAfterStart / 1000 + " seconds for application to start." );
            try
            {
                Thread.sleep( this.sleepAfterStart );
            }
            catch ( InterruptedException e )
            {
            }
        }

        if ( !disableBlocking )
        {
            try
            {
                controlClient.shutdownOnClose();
            }
            catch ( ControlConnectionException e )
            {
                throw new AppBooterServiceException(
                                                     "Failed to send shutdown-on-close command to application host. You may need to terminate the application manually.",
                                                     e );
            }
            catch ( IOException e )
            {
                throw new AppBooterServiceException(
                                                     "Failed to send shutdown-on-close command to application host. You may need to terminate the application manually.",
                                                     e );
            }

            try
            {
                synchronized ( this )
                {
                    try
                    {
                        wait();
                    }
                    catch ( InterruptedException e )
                    {
                    }
                }
            }
            finally
            {
                stopStreamPumps();
                controlClient.close();
            }
        }
    }

    protected Commandline buildCommandLine()
        throws AppBooterServiceException
    {
        Commandline cli = new Commandline();

        String cmd = javaCmd;
        if ( debug )
        {
            cmd = debugJavaCmd;
            cmd = StringUtils.replace( cmd, "@DEBUG_PORT@", String.valueOf( debugPort ) );
            cmd = StringUtils.replace( cmd, "@DEBUG_SUSPEND@", ( debugSuspend ? "y" : "n" ) );
        }

        String[] baseCommand = cmd.split( " " );

        cli.setExecutable( baseCommand[0] );
        if ( baseCommand.length > 1 )
        {
            for ( int i = 1; i < baseCommand.length; i++ )
            {
                cli.createArg().setLine( baseCommand[i] );
            }
        }

        if ( classworldsJar != null )
        {
            cli.createArg().setLine( "-Dbasedir=\'" + basedir.getAbsolutePath() + "\'" );

            cli.createArg().setLine(
                                     "-D" + PlexusAppBooterService.DEFAULT_NAME
                                         + PlexusAppBooterService.ENABLE_CONTROL_SOCKET + "=" + Boolean.TRUE.toString() );

            if ( classworldsConf != null )
            {
                cli.createArg().setLine( "-Dclassworlds.conf=\'" + classworldsConf.getAbsolutePath() + "\'" );
            }

            if ( containerProperties != null )
            {
                cli.createArg().setLine(
                                         "-Dplexus.container.properties.file='" + containerProperties.getAbsolutePath()
                                             + "\'" );
            }

            cli.createArg().setLine( "-cp" );
            cli.createArg().setLine( "\'" + classworldsJar.getAbsolutePath() + "\'" );
            cli.createArg().setLine( "org.codehaus.plexus.classworlds.launcher.Launcher" );
        }
        else
        {
            // old way
            File platformFile = getPlatformFile();
            ClassworldsAppConfiguration config = buildConfig();
            classworldsConf = writeConfig( config );

            cli.createArg().setLine( "-Dclassworlds.conf=\'" + classworldsConf.getAbsolutePath() + "\'" );

            // old way
            cli.createArg().setLine( "-jar" );
            cli.createArg().setLine( "\'" + platformFile.getAbsolutePath() + "\'" );
        }

        // add the control port if it was defined
        if ( controlPort > -1 )
        {
            cli.createArg().setLine( Integer.toString( controlPort ) );
        }

        if ( outputDebugMessages() )
        {
            getLogger().info( "Executing:\n\n" + StringUtils.join( cli.getCommandline(), " " ) );
        }

        return cli;
    }

    private File writeConfig( ClassworldsAppConfiguration config )
        throws AppBooterServiceException
    {
        File classworldsConf = new File( tempDir, "classworlds.conf" );

        try
        {
            ClassworldsConfWriter writer = new ClassworldsConfWriter();

            Properties velocityProperties = writer.getDefaultVelocityProperties();

            VelocityLogChute.setPlexusLogger( getLogger() );
            velocityProperties.setProperty( "runtime.log.logsystem.class", VelocityLogChute.class.getName() );

            writer.write( classworldsConf, config, velocityProperties );
            getLogger().info( "Wrote classworlds.conf to: " + classworldsConf );
        }
        catch ( ClassworldsIOException e )
        {
            throw new AppBooterServiceException( e.getMessage(), e );
        }

        if ( outputDebugMessages() )
        {
            getLogger().info( "Saving Classworlds configuration at: " + classworldsConf.getAbsolutePath() );
        }

        return classworldsConf;
    }

    protected boolean outputDebugMessages()
    {
        return debug || getLogger().isDebugEnabled();
    }

    private ClassworldsAppConfiguration buildConfig()
        throws AppBooterServiceException
    {
        ClassworldsRealmConfiguration rootRealmConfig = this.getClassworldsRealmConfig();

        ClassworldsAppConfiguration config = new ClassworldsAppConfiguration();

        config.setMainClass( launcherClass );
        config.addRealmConfiguration( rootRealmConfig );
        config.setMainRealm( rootRealmConfig.getRealmId() );

        Map<String, String> sysProps = new HashMap<String, String>();

        if ( systemProperties != null && !systemProperties.isEmpty() )
        {
            getLogger().info( "Using system properties:\n\n" + systemProperties );
            sysProps.putAll( systemProperties );
        }

        // allow the override of the basedir...
        // NOTE, this MUST be after the putAll, because this value gets lost. (its not suppose to...)
        sysProps.put( "basedir", basedir.getAbsolutePath() );

        sysProps.put( PlexusAppBooterService.DEFAULT_NAME + PlexusAppBooterService.CONFIGURATION_FILE_PROPERTY_KEY,
                      configuration.getAbsolutePath() );
        sysProps.put( PlexusAppBooterService.DEFAULT_NAME + PlexusAppBooterService.ENABLE_CONTROL_SOCKET,
                      Boolean.TRUE.toString() );

        // cli wins
        for ( Map.Entry<Object, Object> e : System.getProperties().entrySet() )
        {
            String key = (String) e.getKey();
            if ( key.startsWith( SYSPROP_PLEXUS ) )
            {
                sysProps.put( key, (String) e.getValue() );
            }
        }

        config.setSystemProperties( sysProps );

        ClassworldsValidationResult vr = new ClassworldsModelValidator().validate( config );
        if ( vr.hasErrors() )
        {
            throw new AppBooterServiceException( vr.render() );
        }

        return config;
    }

    private void stopStreamPumps()
    {
        if ( outPumper != null )
        {
            outPumper.close();
        }

        if ( errPumper != null )
        {
            errPumper.close();
        }
    }

    protected final class ShutdownHook
        implements Runnable
    {
        protected ShutdownHook()
        {
        }

        public void run()
        {
            // If not closed normally...
            if ( controlClient != null && controlClient.isOpen() )
            {
                getLogger().info( "ShutdownHook is closing the client connection." );
                // Do it now
                controlClient.close();
            }
        }
    }

    private void executeCommandLine( Commandline cli )
        throws AppBooterServiceException
    {
        StreamConsumer out = new StreamConsumer()
        {
            public void consumeLine( String line )
            {
                getLogger().info( line );
            }
        };

        Process p = null;

        try
        {
            p = cli.execute();

            outPumper = new StreamPumper( p.getInputStream(), out );
            errPumper = new StreamPumper( p.getErrorStream(), out );

            outPumper.setPriority( Thread.MIN_PRIORITY + 1 );
            errPumper.setPriority( Thread.MIN_PRIORITY + 1 );

            outPumper.start();
            errPumper.start();
        }
        catch ( CommandLineException e )
        {
            throw new AppBooterServiceException( "Failed to execute plexus application: " + e.getMessage(), e );
        }
    }

    /*
     * (non-Javadoc)
     * @see org.sonatype.appbooter.ForkedAppBooter#stop()
     */
    public void stop()
        throws AppBooterServiceException
    {
        getLogger().info( "Stopping plexus application." );
        ControllerClient client = null;
        try
        {
            client =
                new ControllerClient( controlPort > -1 ? controlPort : PlexusAppBooterService.DEFAULT_CONTROL_PORT );
            client.shutdown();
        }
        catch ( ControlConnectionException e )
        {
            throw new AppBooterServiceException( "Failed to connect to plexus application for shutdown.", e );
        }
        catch ( UnknownHostException e )
        {
            throw new AppBooterServiceException( "Failed to connect to plexus application for shutdown.", e );
        }
        catch ( IOException e )
        {
            throw new AppBooterServiceException( "Failed to connect to plexus application for shutdown.", e );
        }
        finally
        {
            if ( client != null )
            {
                client.close();
            }
        }
    }

    public boolean isShutdown()
    {
        throw new NotImplementedException( "Method 'isShutdown' is not implemented." );
    }

    public boolean isStopped()
    {
        throw new NotImplementedException( "Method 'isStopped' is not implemented." );
    }

    public void shutdown()
        throws AppBooterServiceException
    {
        this.stop();
        if ( !getLogger().isDebugEnabled() && !debug && tempDir != null && tempDir.exists() )
        {
            getLogger().info( "Cleaning up appbooter temp directory: " + tempDir );
            try
            {
                FileUtils.deleteDirectory( tempDir );
            }
            catch ( IOException e )
            {
                throw new AppBooterServiceException( "Failed to delete appbooter temp dir: " + tempDir, e );
            }
        }
        else
        {
            getLogger().info( "Debug mode is enabled; LEAVING appbooter temp directory for inspection: " + tempDir );
        }
    }

    public void setDisableBlocking( boolean disableBlocking )
    {
        this.disableBlocking = disableBlocking;
    }

    public void setDebug( boolean debug )
    {
        this.debug = debug;
    }

    public void setJavaCmd( String javaCmd )
    {
        this.javaCmd = javaCmd;
    }

    public void setDebugPort( int debugPort )
    {
        this.debugPort = debugPort;
    }

    public void setDebugSuspend( boolean debugSuspend )
    {
        this.debugSuspend = debugSuspend;
    }

    public void setDebugJavaCmd( String debugJavaCmd )
    {
        this.debugJavaCmd = debugJavaCmd;
    }

    public void setLauncherClass( String launcherClass )
    {
        this.launcherClass = launcherClass;
    }

    public void setSystemProperties( Map<String, String> systemProperties )
    {
        this.systemProperties = systemProperties;
    }

    public void setConfiguration( File configuration )
    {
        this.configuration = configuration;
    }

    public void setBasedir( File basedir )
    {
        this.basedir = basedir;
    }

    public void setTempDir( File tempDir )
    {
        this.tempDir = tempDir;
    }

    public void setSleepAfterStart( int sleepAfterStart )
    {
        this.sleepAfterStart = sleepAfterStart;
    }

    public void setControlPort( int controlPort )
    {
        this.controlPort = controlPort;
    }

    public void setControlClient( ControllerClient controlClient )
    {
        this.controlClient = controlClient;
    }

    public ControllerClient getControllerClient()
    {
        return this.controlClient;
    }

}