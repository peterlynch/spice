package org.sonatype.appbooter.bundle.service.jetty;

import java.io.File;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.sonatype.appbooter.bundle.ApplicationAppBooter;
import org.sonatype.appbooter.bundle.service.BundleService;

@Component( role = BundleService.class, hint = "jetty" )
public class JettyBundleService
    extends AbstractLogEnabled
    implements BundleService, Initializable, Startable
{
    @Requirement
    private PlexusContainer plexusContainer;

    @Configuration( "${basedir}/conf/jetty.xml" )
    private File jettyXmlFile;

    private Server server;

    public boolean handles( ApplicationAppBooter application )
    {
        // does it have webapp?
        File webappDir = new File( application.getBasedir(), "webapp" );

        return ( new File( webappDir, "WEB-INF/web.xml" ) ).isFile();
    }

    public void startManage( ApplicationAppBooter application )
    {
        File webappDir = new File( application.getBasedir(), "webapp" );

        // mount the webapp
        try
        {
            WebAppContext webapp = new WebAppContext();
            webapp.setContextPath( "/" + application.getName() );
            webapp.setWar( webappDir.getAbsolutePath() );
            webapp.setAttribute( "plexus", application.getContainer() );
            webapp.setClassLoader( application.getWorld().getRealm( application.getName() ) );

            server.addHandler( webapp );

            webapp.start();
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void stopManage( ApplicationAppBooter application )
    {
        // unmount webapp
    }

    public void initialize()
        throws InitializationException
    {
        try
        {
            server = new Server();

            JettyUtils.configureServer( server, jettyXmlFile, plexusContainer.getContext(), getLogger() );
        }
        catch ( Exception e )
        {
            throw new InitializationException( "Jetty was not started!", e );
        }
    }

    public void start()
        throws StartingException
    {
        try
        {
            server.start();
        }
        catch ( Exception e )
        {
            throw new StartingException( "Cannot start Jetty!", e );
        }
    }

    public void stop()
        throws StoppingException
    {
        try
        {
            server.stop();
        }
        catch ( Exception e )
        {
            throw new StoppingException( "Cannot stop Jetty!", e );
        }
    }
}
