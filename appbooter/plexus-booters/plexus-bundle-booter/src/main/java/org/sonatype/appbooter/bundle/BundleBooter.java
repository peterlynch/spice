package org.sonatype.appbooter.bundle;

import java.io.File;
import java.io.IOException;
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
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
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
    implements Contextualizable, Initializable, Startable
{
    @Requirement
    private PlexusContainer container;

    @Requirement( role = BundleService.class )
    private Map<String, BundleService> bundleServices;

    private PlexusAppBooter plexusAppBooter;

    private final HashMap<String, ApplicationAppBooter> applications = new HashMap<String, ApplicationAppBooter>();

    public PlexusContainer getContainer()
    {
        return container;
    }

    public Map<String, BundleService> getBundleServices()
    {
        return bundleServices;
    }

    public PlexusAppBooter getPlexusAppBooter()
    {
        return plexusAppBooter;
    }

    public HashMap<String, ApplicationAppBooter> getApplications()
    {
        return applications;
    }

    protected void discoverApplications()
        throws Exception
    {
        File appsDir = new File( getPlexusAppBooter().getBasedir(), "runtime/apps" );

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

                            // create realm (lib + conf)
                            ClassRealm appRealm = prepareChildRealm( "plexus." + appName, appDir );

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

    public void contextualize( Context context )
        throws ContextException
    {
        plexusAppBooter = (PlexusAppBooter) context.get( PlexusAppBooter.class.getName() );
    }

    public void initialize()
        throws InitializationException
    {
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

    // ===

    protected ClassRealm prepareChildRealm( String id, File appBasedir )
        throws IOException
    {
        ClassRealm realm = getContainer().createChildRealm( id );

        File confDir = new File( appBasedir, "conf" );

        File libDir = new File( appBasedir, "lib" );

        // conf is mandatory
        realm.addURL( confDir.toURI().toURL() );

        // libdir is optional
        if ( libDir.isDirectory() )
        {
            File[] constituents = libDir.listFiles();

            if ( constituents != null )
            {
                for ( File constituent : constituents )
                {
                    realm.addURL( constituent.toURI().toURL() );
                }
            }
        }

        return realm;
    }

}
