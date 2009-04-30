package org.sonatype.appbooter.bundle;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.sonatype.appbooter.PlexusAppBooter;
import org.sonatype.appbooter.bundle.service.BundleService;

@Component( role = BundleBooter.class )
public class BundleBooter
    extends AbstractLogEnabled
    implements Initializable, Startable
{
    @Requirement
    private PlexusContainer container;

    @Requirement( role = BundleService.class )
    private Map<String, BundleService> bundleServices;

    private File basedir;

    private final HashMap<String, ApplicationAppBooter> applications = new HashMap<String, ApplicationAppBooter>();

    public PlexusContainer getContainer()
    {
        return container;
    }

    public Map<String, BundleService> getBundleServices()
    {
        return bundleServices;
    }

    public File getBasedir()
    {
        return basedir;
    }

    protected void discoverApplications()
        throws Exception
    {
        File appsDir = new File( getBasedir(), "runtime/apps" );

        File[] files = appsDir.listFiles();

        if ( files != null )
        {
            for ( File appDir : files )
            {
                if ( appDir.isDirectory() )
                {
                    String appName = appDir.getName();

                    File confDir = new File( appDir, "conf" );

                    if ( confDir.isDirectory() )
                    {
                        File confFile = new File( confDir, "plexus.xml" );

                        if ( confFile.isFile() )
                        {
                            getLogger().info(
                                              "Discovered Application [" + appName + "] (" + confFile.getAbsolutePath()
                                                  + ")" );

                            File libDir = new File( appDir, "lib" );

                            // create realm (lib + conf)
                            ClassRealm appRealm = getContainer().getContainerRealm().getWorld().newRealm( appName );

                            appRealm
                                    .setParentRealm( getContainer().getContainerRealm().getWorld().getRealm( "plexus" ) );

                            // conf is mandatory
                            appRealm.addURL( confDir.toURI().toURL() );

                            // libdir is optional
                            if ( libDir.isDirectory() )
                            {
                                File[] constituents = libDir.listFiles();

                                if ( constituents != null )
                                {
                                    for ( File constituent : constituents )
                                    {
                                        appRealm.addURL( constituent.toURI().toURL() );
                                    }
                                }
                            }

                            // wrap it up
                            // setup plexus using conf/plexus.xml + conf/plexus.properties
                            ApplicationAppBooter appBooter = new ApplicationAppBooter( appName, appRealm );

                            appBooter.setBasedir( appDir );

                            appBooter.setConfiguration( confFile );

                            // ask for any service
                            for ( BundleService service : bundleServices.values() )
                            {
                                if ( service.handles( appBooter ) )
                                {
                                    appBooter.getBundleServices().add( service );
                                }
                            }

                            // add it to map of apps
                            applications.put( appBooter.getName(), appBooter );
                        }
                        else
                        {
                            getLogger().info( "Application [" + appName + "] missing configuration, skipping it." );
                        }
                    }
                }
            }
        }
    }

    public void initialize()
        throws InitializationException
    {
        Context context = getContainer().getContext();

        if ( context.contains( PlexusAppBooter.class.getName() ) )
        {
            try
            {
                basedir = ( (PlexusAppBooter) context.get( PlexusAppBooter.class.getName() ) ).getBasedir();
            }
            catch ( ContextException e )
            {
            }
        }

        if ( basedir == null )
        {
            basedir = new File( "" ).getAbsoluteFile();
        }

        try
        {
            discoverApplications();
        }
        catch ( Exception e )
        {
            throw new InitializationException( "Unable to initialize!", e );
        }
    }

    public void start()
        throws StartingException
    {
        try
        {
            for ( ApplicationAppBooter application : applications.values() )
            {
                getLogger().info( "Starting Application [" + application.getName() + "]" );

                application.startContainer();
            }
        }
        catch ( PlexusContainerException e )
        {
            throw new StartingException( "Unable to start up!", e );
        }
    }

    public void stop()
        throws StoppingException
    {
        for ( ApplicationAppBooter application : applications.values() )
        {
            getLogger().info( "Stopping Application [" + application.getName() + "]" );

            application.stopContainer();
        }
    }

}
