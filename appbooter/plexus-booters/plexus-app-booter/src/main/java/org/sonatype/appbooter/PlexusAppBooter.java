package org.sonatype.appbooter;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;

/**
 * The simplest class needed to bring up a Plexus Application. No hokus-pokus, just real stuff.
 * 
 * @author cstamas
 * @since 2.0
 */
public class PlexusAppBooter
{
    public static final String CONFIGURATION_FILE_PROPERTY = "plexus.configuration";

    public static final String DEV_MODE = "plexus.container.dev.mode";

    private ClassWorld world;

    private File configuration;

    private File basedir;

    private List<ContextFiller> contextFillers;

    private List<ContextPublisher> contextPublishers;

    private PlexusContainer container;

    protected static final Object waitObj = new Object();

    public ClassWorld getWorld()
    {
        if ( world == null )
        {
            ClassWorld cw = new ClassWorld();

            try
            {
                cw.newRealm( "default", getClass().getClassLoader() );
            }
            catch ( DuplicateRealmException e )
            {
                // TODO: is this okay?
                // nothing, then we already have something set
            }

            this.world = cw;
        }

        return world;
    }

    public void setWorld( ClassWorld world )
    {
        this.world = world;
    }

    public File getBasedir()
    {
        if ( basedir != null )
        {
            return basedir;
        }

        if ( System.getProperty( "basedir" ) != null )
        {
            basedir = new File( System.getProperty( "basedir" ) ).getAbsoluteFile();
        }

        if ( basedir == null )
        {
            basedir = new File( "" ).getAbsoluteFile();
        }

        return basedir;
    }

    public void setBasedir( File basedir )
    {
        this.basedir = basedir;
    }

    public File getConfiguration()
    {
        if ( configuration == null )
        {
            String configPath = System.getProperty( CONFIGURATION_FILE_PROPERTY );

            if ( configPath == null )
            {
                configuration = new File( getBasedir(), "conf/plexus.xml" );
            }
            else
            {
                configuration = new File( configPath );
            }
        }

        return configuration;
    }

    public void setConfiguration( File configuration )
    {
        this.configuration = configuration;
    }

    public List<ContextFiller> getContextFillers()
    {
        if ( contextFillers == null )
        {
            contextFillers = new ArrayList<ContextFiller>( 2 );

            // the order is important! 1st env variables
            contextFillers.add( new SystemEnvironmentContextFiller() );

            // then system properties, that will override env vars if needed
            contextFillers.add( new SystemPropertiesContextFiller() );
        }

        return contextFillers;
    }

    public void setContextFillers( List<ContextFiller> contextFillers )
    {
        this.contextFillers = contextFillers;
    }

    public List<ContextPublisher> getContextPublishers()
    {
        if ( contextPublishers == null )
        {
            contextPublishers = new ArrayList<ContextPublisher>( 2 );

            // the order is important! 1st system properties
            contextPublishers.add( new SystemPropertiesContextPublisher() );

            // 2nd the terminal
            contextPublishers.add( new TerminalContextPublisher() );
        }

        return contextPublishers;
    }

    public void setContextPublishers( List<ContextPublisher> contextPublishers )
    {
        this.contextPublishers = contextPublishers;
    }

    public PlexusContainer getContainer()
    {
        return container;
    }

    protected Context createContainerContext()
        throws Exception
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

        environment.put( "basedir", getBasedir().getAbsolutePath() );

        for ( ContextFiller filler : getContextFillers() )
        {
            filler.fillContext( this, environment );
        }

        /*
         * A standard source: plexus.properties next to plexus.xml (config file) Iterate through plexus.properties,
         * insert all items into a map add into plexus context using a RegexBasedInterpolator.
         */
        File containerPropertiesFile = new File( getConfiguration().getParentFile(), "plexus.properties" );

        Properties containerProperties = new Properties();

        if ( containerPropertiesFile.exists() )
        {
            containerProperties.load( new FileInputStream( containerPropertiesFile ) );

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

        Map<Object, Object> containerContext = new HashMap<Object, Object>();

        // 1st containerProperties
        for ( Object key : containerProperties.keySet() )
        {
            containerContext.put( key, interpolator.interpolate( (String) containerProperties.get( key ) ) );
        }

        // 2nd environment properties, to step over stuff coming from container properties
        for ( Object key : environment.keySet() )
        {
            containerContext.put( key, interpolator.interpolate( (String) environment.get( key ) ) );
        }

        // Now that we have containerContext with proper values, set them back into System properties and
        // dump them to System.out for reference.
        for ( ContextPublisher publisher : getContextPublishers() )
        {
            publisher.publishContext( this, containerContext );
        }

        return new DefaultContext( containerContext );
    }

    protected void customizeContext( Context context )
    {
        // override if needed
    }

    protected void customizeContainerConfiguration( ContainerConfiguration containerConfiguration )
    {
        // override if needed
    }

    public void startContainer()
        throws PlexusContainerException
    {
        synchronized ( waitObj )
        {
            if ( this.container != null )
            {
                throw new PlexusContainerException( "Container already running!" );
            }

            Context context = null;

            try
            {
                context = createContainerContext();
            }
            catch ( Exception e )
            {
                throw new PlexusContainerException( "Unable to create container context!", e );
            }

            customizeContext( context );

            ContainerConfiguration configuration =
                new DefaultContainerConfiguration().setClassWorld( getWorld() )
                                                   .setContainerConfiguration( getConfiguration().getAbsolutePath() )
                                                   .setContext( context.getContextData() );

            customizeContainerConfiguration( configuration );

            this.container = new DefaultPlexusContainer( configuration );
        }
    }

    public void startContainerAndBlockThread()
        throws PlexusContainerException
    {
        synchronized ( waitObj )
        {
            startContainer();

            try
            {
                waitObj.wait();
            }
            catch ( InterruptedException e )
            {
            }

            stopContainer();
        }
    }

    public void stopContainer()
    {
        synchronized ( waitObj )
        {
            if ( container != null )
            {
                container.dispose();

                container = null;
            }
        }
    }

    public static void main( String[] args, ClassWorld classWorld )
    {
        try
        {
            PlexusAppBooter appBooter = new PlexusAppBooter();

            appBooter.setWorld( classWorld );

            System.out.println( "Starting container" );

            appBooter.startContainerAndBlockThread();
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
