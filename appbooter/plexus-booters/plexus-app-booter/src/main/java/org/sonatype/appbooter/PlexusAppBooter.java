package org.sonatype.appbooter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextFactory;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.DefaultBasedirDiscoverer;
import org.sonatype.appcontext.PropertiesFileContextFiller;
import org.sonatype.appcontext.SimpleBasedirDiscoverer;

/**
 * The simplest class needed to bring up a Plexus Application. No hokus-pokus, just real stuff.
 * 
 * @author cstamas
 * @since 2.0
 */
public class PlexusAppBooter
{
    public static final String DEFAULT_NAME = "plexus";

    public static final String CONFIGURATION_FILE_PROPERTY_KEY = ".configuration";

    public static final String CUSTOMIZERS_PROPERTY_KEY = ".appbooter.customizers";

    // ???
    public static final String DEV_MODE = "plexus.container.dev.mode";

    private String name;

    private ClassWorld world;

    private File basedir;

    private File configuration;

    private PlexusContainer container;

    private AppContextFactory appContextFactory = new AppContextFactory();

    private List<PlexusAppBooterCustomizer> customizers;

    private boolean started = false;

    protected static final Object waitObj = new Object();

    public String getName()
    {
        if ( name == null )
        {
            name = DEFAULT_NAME;
        }

        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public ClassWorld getWorld()
    {
        if ( world == null )
        {
            ClassWorld cw = new ClassWorld( "plexus.core", Thread.currentThread().getContextClassLoader() );

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
        if ( basedir == null )
        {
            // The default discoverer encapsulates equivalent logic as used in plexus apps: just use the "basedir"
            // system properties key
            basedir = new DefaultBasedirDiscoverer().discoverBasedir();

            // but, check for nesting
            if ( System.getProperty( getName() + ".basedir" ) != null )
            {
                basedir = new File( System.getProperty( getName() + ".basedir" ) ).getAbsoluteFile();
            }
        }

        return basedir;
    }

    public void setBasedir( File basedir )
    {
        this.basedir = basedir.getAbsoluteFile();
    }

    public File getConfiguration()
    {
        if ( configuration == null )
        {
            String configPath = System.getProperty( getName() + CONFIGURATION_FILE_PROPERTY_KEY );

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

    public PlexusContainer getContainer()
    {
        return container;
    }

    protected Context createContainerContext()
        throws Exception
    {
        AppContextRequest request = appContextFactory.getDefaultAppContextRequest();

        request.setName( getName() );

        // just pass over the already found basedir
        request.setBasedirDiscoverer( new SimpleBasedirDiscoverer( getBasedir() ) );

        // create a properties filler for plexus.properties, that will fail if props file not found
        File containerPropertiesFile;

        String plexusCfg = System.getProperty( "plexus.container.properties.file" );

        if ( plexusCfg != null )
        {
            containerPropertiesFile = new File( plexusCfg );
        }
        else
        {
            containerPropertiesFile = new File( getConfiguration().getParentFile(), "plexus.properties" );
        }

        PropertiesFileContextFiller plexusPropertiesFiller =
            new PropertiesFileContextFiller( containerPropertiesFile, true );

        // add it to fillers as very 1st resource, and leaving others in
        request.getContextFillers().add( 0, plexusPropertiesFiller );

        AppContext response = appContextFactory.getAppContext( request );

        // put the app booter into context too
        response.put( PlexusAppBooter.class.getName(), this );

        // put itself into context, since PlexusContext is created from this, and original would be throwed away
        response.put( AppContext.class.getName(), response );

        // put in the basedir for plexus apps backward compat
        response.put( "basedir", response.getBasedir().getAbsolutePath() );

        return new DefaultContext( response );
    }

    protected void customizeContext( Context context )
    {
        for ( PlexusAppBooterCustomizer customizer : getPlexusAppBooterCustomizers() )
        {
            customizer.customizeContext( context );
        }
    }

    protected void customizeContainerConfiguration( ContainerConfiguration containerConfiguration )
    {
        for ( PlexusAppBooterCustomizer customizer : getPlexusAppBooterCustomizers() )
        {
            customizer.customizeContainerConfiguration( containerConfiguration );
        }
    }

    protected void customizeContainer( PlexusContainer plexusContainer )
    {
        for ( PlexusAppBooterCustomizer customizer : getPlexusAppBooterCustomizers() )
        {
            customizer.customizeContainer( plexusContainer );
        }
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
                new DefaultContainerConfiguration().setClassWorld( getWorld() ).setContainerConfiguration(
                    getConfiguration().getAbsolutePath() ).setContext( context.getContextData() );

            customizeContainerConfiguration( configuration );

            container = new DefaultPlexusContainer( configuration );

            customizeContainer( container );

            started = true;
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

                waitObj.notify();

                started = false;
            }
        }
    }

    protected List<PlexusAppBooterCustomizer> getPlexusAppBooterCustomizers()
    {
        if ( customizers == null )
        {
            customizers = new ArrayList<PlexusAppBooterCustomizer>();

            gatherPlexusAppBooterCustomizers();
        }

        return customizers;
    }

    protected void gatherPlexusAppBooterCustomizers()
    {
        String customizersList = System.getProperty( getName() + CUSTOMIZERS_PROPERTY_KEY );

        if ( StringUtils.isNotBlank( customizersList ) )
        {
            String[] customizersString = StringUtils.split( customizersList, "," );

            for ( int i = 0; i < customizersString.length; i++ )
            {
                String customizerClassName = customizersString[i];

                System.out.println( "Loading PlexusAppBooterCustomizer (implementation=" + customizerClassName + ")." );

                try
                {
                    Class<PlexusAppBooterCustomizer> customizerClass =
                        (Class<PlexusAppBooterCustomizer>) Class.forName( customizerClassName );

                    PlexusAppBooterCustomizer customizer = customizerClass.newInstance();

                    customizers.add( customizer );
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                    // ignore it?
                }
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

    public boolean isStarted()
    {
        return started;
    }
}
