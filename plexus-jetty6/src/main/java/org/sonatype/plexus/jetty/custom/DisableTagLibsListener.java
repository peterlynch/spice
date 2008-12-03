package org.sonatype.plexus.jetty.custom;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.mortbay.component.LifeCycle;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.webapp.Configuration;
import org.mortbay.jetty.webapp.TagLibConfiguration;
import org.mortbay.jetty.webapp.WebAppContext;

public class DisableTagLibsListener
    implements LifeCycle.Listener, LogEnabled
{

    private Logger logger;

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
        Handler[] childHandlers = collection.getChildHandlers();
        
        if ( childHandlers != null && childHandlers.length > 0 )
        {
            for ( Handler child : childHandlers )
            {
                if ( child instanceof WebAppContext )
                {
                    WebAppContext webapp = (WebAppContext) child;
                    
                    if ( logger != null/* && logger.isDebugEnabled() */)
                    {
                        logger.info( "Disabling TLD support for: " + webapp.getDisplayName() + " (context path: " + webapp.getContextPath() + ")" );
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
                    
                    if ( logger != null )
                    {
                        StringBuilder builder = new StringBuilder();
                        if ( webapp.getConfigurations() != null )
                        {
                            for ( Configuration configuration : webapp.getConfigurations() )
                            {
                                builder.append( "\n" ).append( configuration.getClass().getName() );
                            }
                            
                            logger.info( "\n\nThe following configurations are in use for this webapp:" + builder.toString() );
                        }
                        
                        builder.setLength( 0 );
                        if ( webapp.getConfigurationClasses() != null )
                        {
                            for ( String configClass : webapp.getConfigurationClasses() )
                            {
                                builder.append( "\n" ).append( configClass );
                            }
                            
                            logger.info( "\n\nThe following configurationClasses are in use for this webapp:" + builder.toString() );
                        }
                    }
                }
            }
        }
    }

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
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
