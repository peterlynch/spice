package org.sonatype.plexus.jetty.custom;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.mortbay.component.LifeCycle;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;
import org.mortbay.log.Logger;
import org.sonatype.plexus.jetty.DefaultServletContainer;

/**
 * Inject the Plexus container that was initialized in {@link PlexusContainerHolder} (usually, via {@link DefaultServletContainer})
 * into {@link WebAppContext} instances that were constructed via a custom jetty.xml configuration file.
 *  
 * @author jdcasey
 *
 */
public class InjectExistingPlexusListener
    implements LifeCycle.Listener
{

    public void lifeCycleFailure( LifeCycle event, Throwable cause )
    {
    }

    public void lifeCycleStarted( LifeCycle event )
    {
    }

    public void lifeCycleStarting( LifeCycle lifecycle )
    {
        if ( lifecycle instanceof ContextHandlerCollection )
        {
            Logger logger = Log.getLogger( getClass().getName() );

            Handler[] childHandlers = ((ContextHandlerCollection) lifecycle).getChildHandlers();

            if ( childHandlers != null && childHandlers.length > 0 )
            {
                for ( Handler child : childHandlers )
                {
                    if ( child instanceof WebAppContext )
                    {
                        WebAppContext webapp = (WebAppContext) child;

                        if ( logger != null )
                        {
                            logger.info( "Injecting Plexus container for: {} (context path: {})", webapp.getDisplayName(),
                                         webapp.getContextPath() );
                        }

                        PlexusContainer container = PlexusContainerHolder.get();

                        webapp.setAttribute( PlexusConstants.PLEXUS_KEY, container );

                        webapp.setClassLoader( container.getContainerRealm() );
                    }
                }
            }
        }
    }

    public void lifeCycleStopped( LifeCycle event )
    {
    }

    public void lifeCycleStopping( LifeCycle event )
    {
    }

}
