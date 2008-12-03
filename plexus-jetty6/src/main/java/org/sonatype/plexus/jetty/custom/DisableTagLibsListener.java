package org.sonatype.plexus.jetty.custom;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.mortbay.component.LifeCycle;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.deployer.WebAppDeployer;
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
                        int idx = -1;
                        for ( int i = 0; i < configs.length; i++ )
                        {
                            if ( configs[i] instanceof TagLibConfiguration )
                            {
                                configs[i] = new DisabledTagLibConfiguration();
                                idx = i;
                                break;
                            }
                        }
                        if ( idx < 0 )
                        {
                            Configuration[] newConfigs = new Configuration[configs.length + 1];
                            
                            System.arraycopy( configs, 0, newConfigs, 0, configs.length );
                            newConfigs[newConfigs.length - 1] = new DisabledTagLibConfiguration();
                            
                            configs = newConfigs;
                        }
                        
                        webapp.setConfigurations( configs );
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
                            int idx = -1;
                            for ( int i = 0; i < configClasses.length; i++ )
                            {
                                if ( TagLibConfiguration.class.getName().equals( configClasses[i] ) )
                                {
                                    idx = i;
                                    break;
                                }
                            }
                            
                            // FIXME: Figure out how some webapps get the disabled taglib config twice in configurationClasses!!
                            if ( idx < 0 )
                            {
                                String[] newConfigClasses = new String[ configClasses.length + 1];
                                
                                System.arraycopy( configClasses, 0, newConfigClasses, 0, configClasses.length );
                                newConfigClasses[newConfigClasses.length - 1] = DisabledTagLibConfiguration.class.getName();
                                
                                configClasses = newConfigClasses;
                            }
                            else
                            {
                                configClasses[idx] = DisabledTagLibConfiguration.class.getName();
                            }
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
