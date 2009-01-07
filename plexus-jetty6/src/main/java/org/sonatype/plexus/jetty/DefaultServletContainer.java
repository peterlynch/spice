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
package org.sonatype.plexus.jetty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.mortbay.component.LifeCycle.Listener;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.deployer.WebAppDeployer;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.sonatype.plexus.jetty.custom.PlexusContainerHolder;
import org.sonatype.plexus.util.JettyUtils;

/**
 * The Class ServletServer. Heavily based on Joakim Erfeldt's work in wagon-webdav tests.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultServletContainer
    extends AbstractLogEnabled
    implements ServletContainer, Contextualizable, Initializable, Startable
{

    /** The server. */
    private Server server;

    /** @plexus.configuration default-value="*" */
    private String defaultHost = "*";

    /** @plexus.configuration default-value="8080" */
    private int defaultPort = 8081;
    
    /** @plexus.configuration */
    private String jettyXml;
    
    /** @plexus.configuration */
    private List<LifecycleListenerInfo> lifecycleListenerInfos;

    /** @plexus.configuration */
    private List<ConnectorInfo> connectorInfos;

    /** @plexus.configuration */
    private List<HandlerInfo> handlerInfos;

    /** @plexus.configuration */
    private List<WebappInfo> webappInfos;

    /** @plexus.configuration */
    private File webapps;

    private Context context;

    public Server getServer()
    {
        return server;
    }

    public void setServer( Server server )
    {
        this.server = server;
    }

    public String getDefaultHost()
    {
        return defaultHost;
    }

    public void setDefaultHost( String host )
    {
        this.defaultHost = host;
    }

    public int getDefaultPort()
    {
        return defaultPort;
    }

    public void setDefaultPort( int port )
    {
        this.defaultPort = port;
    }

    public String getJettyXml()
    {
        return jettyXml;
    }

    public void setJettyXml( String jettyXml )
    {
        this.jettyXml = jettyXml;
    }

    // ===
    // Plexus lifecycle

    public void contextualize( Context context )
        throws ContextException
    {
        this.context = context;
    }

    public void initialize()
        throws InitializationException
    {
        // Log.setLog( new PlexusJettyLogger( getLogger() ) );

        setServer( new Server() );
        
        if ( jettyXml != null && new File( jettyXml ).isFile() )
        {
            JettyUtils.configureServer( getServer(), new File( jettyXml ), context, getLogger() );
        }
        else
        {
            if ( jettyXml != null && new File( jettyXml ).isFile() )
            {
                getLogger().debug( "Cannot load: " + jettyXml + "; it is not a valid configuration file." );
            }
            
            try
            {
                // connectors
                if ( connectorInfos != null && connectorInfos.size() > 0 )
                {
                    for ( int i = 0; i < connectorInfos.size(); i++ )
                    {
                        Connector conn  = connectorInfos.get( i ).getConnector( context );

                        getLogger().info(
                            "Adding Jetty Connector " + conn.getClass().getName() + " on port "
                                + conn.getPort() );
                        
                        getServer().addConnector( conn );
                    }
                }
                else
                {
                    Connector conn = new SelectChannelConnector();

                    conn.setHost( getDefaultHost() );

                    conn.setPort( getDefaultPort() );

                    getLogger().info(
                        "Adding default Jetty Connector " + conn.getClass().getName() + " on port "
                            + getDefaultPort() );
                    
                    getServer().addConnector( conn );
                }

                // customizations, such as disabling TLD scanning.
                List<Listener> listeners = new ArrayList<Listener>(); 
                
                if ( lifecycleListenerInfos != null && !lifecycleListenerInfos.isEmpty() )
                {
                    for ( LifecycleListenerInfo listenerInfo : lifecycleListenerInfos )
                    {
                        if ( listenerInfo != null )
                        {
                            try
                            {
                                Listener listener = listenerInfo.getListener( context );
                                
                                listeners.add( listener );
                            }
                            catch ( Exception e )
                            {
                                throw new InitializationException( "Could not initialize ServletContainer!", e );
                            }
                        }
                    }
                }
                
                // gathering stuff

                Handler handler;

                // webapps

                if ( ( webappInfos != null && webappInfos.size() > 0 ) || ( webapps != null && webapps.isDirectory() ) )
                {
                    ContextHandlerCollection ctxHandler = new ContextHandlerCollection();
                    if ( !listeners.isEmpty() )
                    {
                        for ( Listener listener : listeners )
                        {
                            if ( listener != null )
                            {
                                ctxHandler.addLifeCycleListener( listener );
                            }
                        }
                    }
                    
                    if ( webappInfos != null && webappInfos.size() > 0 )
                    {
                        for ( WebappInfo webappInfo : webappInfos )
                        {
                            handler = (WebAppContext) webappInfo.getWebAppContext( context, ctxHandler );

                            getLogger().info(
                                "Adding Jetty WebAppContext " + handler.getClass().getName() + " on context path "
                                    + webappInfo.getContextPath() + " from " + webappInfo.getWarPath() );
                        }

                    }
                    else if ( webapps != null && webapps.isDirectory() )
                    {
                        WebAppDeployer webAppDeployer = new WebAppDeployer();

                        if ( !listeners.isEmpty() )
                        {
                            for ( Listener listener : listeners )
                            {
                                if ( listener != null )
                                {
                                    webAppDeployer.addLifeCycleListener( listener );
                                }
                            }
                        }
                        
                        webAppDeployer.setContexts( ctxHandler );

                        webAppDeployer.setExtract( true );

                        webAppDeployer.setAllowDuplicates( false );

                        webAppDeployer.setWebAppDir( webapps.getAbsolutePath() );
                        
                        getServer().addLifeCycle( webAppDeployer );
                    }

                    ctxHandler.mapContexts();
                    getServer().addHandler( ctxHandler );
                }

                // handlers

                if ( handlerInfos != null && handlerInfos.size() > 0 )
                {
                    for ( HandlerInfo handlerInfo : handlerInfos )
                    {
                        handler = handlerInfo.getHandler( context );
                        handler.setServer( getServer() );

                        getLogger().info( "Adding Jetty Handler " + handler.getClass().getName() );

                        getServer().addHandler( handler );
                    }
                }
                else
                {
                    DefaultHandler defHandler = new DefaultHandler();

                    defHandler.setServer( server );
                    
                    defHandler.setServeIcon( false );
                    
                    getLogger().info( "Adding default Jetty Handler " + defHandler.getClass().getName() );
                    
                    getServer().addHandler( defHandler );
                }
            }
            catch ( Exception e )
            {
                throw new InitializationException( "Could not initialize ServletContainer!", e );
            }
        }
        
    }

    // ===
    // Startable iface

    public void start()
        throws StartingException
    {
        try
        {
            PlexusContainerHolder.set( (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY ) );
            getServer().start();
        }
        catch ( Exception e )
        {
            throw new StartingException( "Error starting embedded Jetty server.", e );
        }
    }

    public void stop()
        throws StoppingException
    {
        try
        {
            getServer().stop();
            PlexusContainerHolder.clear();
        }
        catch ( Exception e )
        {
            throw new StoppingException( "Error stopping embedded Jetty server.", e );
        }
    }

    // ===
    // Private stuff

}
