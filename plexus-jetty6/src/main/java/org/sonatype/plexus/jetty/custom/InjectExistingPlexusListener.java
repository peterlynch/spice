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
