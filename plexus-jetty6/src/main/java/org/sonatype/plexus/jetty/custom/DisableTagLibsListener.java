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

import java.util.ArrayList;
import java.util.List;

import org.mortbay.component.LifeCycle;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.webapp.Configuration;
import org.mortbay.jetty.webapp.TagLibConfiguration;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;
import org.mortbay.log.Logger;

public class DisableTagLibsListener
    implements LifeCycle.Listener
{

    public void lifeCycleFailure( LifeCycle lifecycle, Throwable cause )
    {
    }

    public void lifeCycleStarting( LifeCycle lifecycle )
    {
        if ( lifecycle instanceof ContextHandlerCollection )
        {
            disableTagLibs( (ContextHandlerCollection) lifecycle );
        }
    }

    private void disableTagLibs( ContextHandlerCollection collection )
    {
        Logger logger = Log.getLogger( getClass().getName() );
        
        Handler[] childHandlers = collection.getChildHandlers();
        
        if ( childHandlers != null && childHandlers.length > 0 )
        {
            for ( Handler child : childHandlers )
            {
                if ( child instanceof WebAppContext )
                {
                    WebAppContext webapp = (WebAppContext) child;
                    
                    if ( logger != null )
                    {
                        logger.info( "Disabling TLD support for: {} (context path: {})", webapp.getDisplayName(), webapp.getContextPath() );
                    }
                    
                    Configuration[] configs = webapp.getConfigurations();
                    if ( configs != null )
                    {
                        List<Configuration> toSet = new ArrayList<Configuration>();
                        
                        for ( int i = 0; i < configs.length; i++ )
                        {
                            if ( !( configs[i] instanceof TagLibConfiguration ) )
                            {
                                toSet.add( configs[i] );
                            }
                        }
                        
                        boolean hasDisabledTaglibs = false;
                        for ( Configuration configuration : toSet )
                        {
                            if ( configuration instanceof DisabledTagLibConfiguration )
                            {
                                hasDisabledTaglibs = true;
                                break;
                            }
                        }
                        
                        if ( !hasDisabledTaglibs )
                        {
                            toSet.add( new DisabledTagLibConfiguration() );
                        }
                        
                        webapp.setConfigurations( toSet.toArray( configs ) );
                    }
                    else
                    {
                        String[] configClasses = webapp.getConfigurationClasses();
                        if ( configClasses == null )
                        {
                            configClasses = new String[1];
                            configClasses[0] = DisabledTagLibConfiguration.class.getName();
                        }
                        else
                        {
                            List<String> toSet = new ArrayList<String>();
                            for ( int i = 0; i < configClasses.length; i++ )
                            {
                                toSet.add( configClasses[i] );
                            }
                            
                            toSet.remove( TagLibConfiguration.class.getName() );
                            
                            if ( !toSet.contains( DisabledTagLibConfiguration.class.getName() ) )
                            {
                                toSet.add( DisabledTagLibConfiguration.class.getName() );
                            }
                            
                            configClasses = toSet.toArray( configClasses );
                        }
                        
                        webapp.setConfigurationClasses( configClasses );
                    }
                    
                    if ( logger != null && logger.isDebugEnabled() )
                    {
                        StringBuilder builder = new StringBuilder();
                        if ( webapp.getConfigurations() != null )
                        {
                            for ( Configuration configuration : webapp.getConfigurations() )
                            {
                                builder.append( "\n" ).append( configuration.getClass().getName() );
                            }
                            
                            logger.debug( "\n\nThe following configurations are in use for this webapp: {}", builder, null );
                        }
                        
                        builder.setLength( 0 );
                        if ( webapp.getConfigurationClasses() != null )
                        {
                            for ( String configClass : webapp.getConfigurationClasses() )
                            {
                                builder.append( "\n" ).append( configClass );
                            }
                            
                            logger.debug( "\n\nThe following configurationClasses are in use for this webapp: {}", builder, null );
                        }
                    }
                }
            }
        }
    }

    public void lifeCycleStarted( LifeCycle lifecycle )
    {
        // NOP
    }

    public void lifeCycleStopped( LifeCycle lifecycle )
    {
        // NOP
    }

    public void lifeCycleStopping( LifeCycle lifecycle )
    {
        // NOP
    }

}
