package org.sonatype.plexus.jetty.custom;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.mortbay.component.LifeCycle;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.deployer.WebAppDeployer;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.webapp.Configuration;
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
        if ( lifecycle instanceof WebAppDeployer )
        {
            disableTagLibs( (WebAppDeployer) lifecycle );
        }
        else if ( lifecycle instanceof ContextHandlerCollection )
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
                    if ( configs == null )
                    {
                        configs = new Configuration[1];
                    }
                    else
                    {
                        Configuration[] tmp = new Configuration[configs.length + 1];
                        System.arraycopy( configs, 0, tmp, 0, configs.length );
                        
                        configs = tmp;
                    }
                    
                    configs[configs.length - 1] = new DisabledTagLibConfiguration();
                    
                    webapp.setConfigurations( configs );
                }
            }
        }
    }

    private void disableTagLibs( WebAppDeployer wad )
    {
        if ( logger != null/* && logger.isDebugEnabled() */)
        {
            logger.info( "Disabling TLD support for webapps in: " + wad.getWebAppDir() );
        }
        
        String[] configClasses = wad.getConfigurationClasses();
        if ( configClasses == null )
        {
            configClasses = new String[1];
        }
        else
        {
            String[] tmp = new String[configClasses.length + 1];
            System.arraycopy( configClasses, 0, tmp, 0, configClasses.length );
            configClasses = tmp;
        }
        
        configClasses[configClasses.length - 1] = DisabledTagLibConfiguration.class.getName();
        wad.setConfigurationClasses( configClasses );
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
