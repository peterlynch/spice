/**
  * Copyright (C) 2008 Sonatype Inc. 
  * Sonatype Inc, licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in 
  * compliance with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
package org.sonatype.plexus.jetty;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.deployer.WebAppDeployer;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.xml.XmlConfiguration;
import org.xml.sax.SAXException;

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
    private String jettyXmlUrl;

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

    public String getJettyXmlUrl()
    {
        return jettyXmlUrl;
    }

    public void setJettyXmlUrl( String jettyXmlUrl )
    {
        this.jettyXmlUrl = jettyXmlUrl;
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
        
        if ( jettyXmlUrl != null )
        {
            getLogger().debug( "Loading configuration from jetty.xml file at: " + jettyXmlUrl );
            
            try
            {
                new XmlConfiguration( new File( jettyXmlUrl ).toURL() ).configure( getServer() );
            }
            catch ( SAXException e )
            {
                getLogger().error( "Failed to load configuration from jetty.xml at: " + jettyXmlUrl, e );
            }
            catch ( IOException e )
            {
                getLogger().error( "Failed to load configuration from jetty.xml at: " + jettyXmlUrl, e );
            }
            catch ( Exception e )
            {
                getLogger().error( "Failed to configure server instance from jetty.xml at: " + jettyXmlUrl, e );
            }
            
            getLogger().debug( "Configuration from jetty.xml will now be overridden by any Jetty configuration defined for the component: " + getClass().getName() );
        }

        try
        {
            // connectors

            Connector[] connectors;

            if ( connectorInfos != null && connectorInfos.size() > 0 )
            {
                connectors = new Connector[connectorInfos.size()];

                for ( int i = 0; i < connectorInfos.size(); i++ )
                {
                    connectors[i] = connectorInfos.get( i ).getConnector( context );

                    getLogger().info(
                        "Adding Jetty Connector " + connectors[i].getClass().getName() + " on port "
                            + connectors[i].getPort() );
                }
            }
            else
            {
                connectors = new Connector[1];

                connectors[0] = new SelectChannelConnector();

                connectors[0].setHost( getDefaultHost() );

                connectors[0].setPort( getDefaultPort() );

                getLogger().info(
                    "Adding default Jetty Connector " + connectors[0].getClass().getName() + " on port "
                        + getDefaultPort() );
            }
            getServer().setConnectors( connectors );

            // gathering stuff

            int jettyHandlers = ( handlerInfos != null ? handlerInfos.size() : 0 )
                + ( webappInfos != null ? webappInfos.size() : 0 );

            List<Handler> handlers = new ArrayList<Handler>( jettyHandlers );

            Handler handler;

            // webapps

            if ( ( webappInfos != null && webappInfos.size() > 0 ) || ( webapps != null && webapps.isDirectory() ) )
            {
                ContextHandlerCollection ctxHandler = new ContextHandlerCollection();

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

                    webAppDeployer.setContexts( ctxHandler );

                    webAppDeployer.setExtract( true );

                    webAppDeployer.setAllowDuplicates( false );

                    webAppDeployer.setWebAppDir( webapps.getAbsolutePath() );
                    
                    server.addLifeCycle( webAppDeployer );
                }

                ctxHandler.mapContexts();

                handlers.add( ctxHandler );
            }

            // handlers

            if ( handlerInfos != null && handlerInfos.size() > 0 )
            {
                for ( HandlerInfo handlerInfo : handlerInfos )
                {
                    handler = handlerInfo.getHandler( context );

                    handler.setServer( getServer() );

                    handlers.add( handler );

                    getLogger().info( "Adding Jetty Handler " + handler.getClass().getName() );
                }
            }
            else
            {
                DefaultHandler defHandler = new DefaultHandler();

                defHandler.setServer( server );
                
                defHandler.setServeIcon( false );
                
                handlers.add( defHandler );

                getLogger().info( "Adding default Jetty Handler " + defHandler.getClass().getName() );
            }

            // register them with server

            getServer().setHandlers( handlers.toArray( new Handler[handlers.size()] ) );

        }
        catch ( Exception e )
        {
            throw new InitializationException( "Could not initialize ServletContainer!", e );
        }
    }

    // ===
    // Startable iface

    public void start()
        throws StartingException
    {
        try
        {
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
        }
        catch ( Exception e )
        {
            throw new StoppingException( "Error stopping embedded Jetty server.", e );
        }
    }

    // ===
    // Private stuff

}
