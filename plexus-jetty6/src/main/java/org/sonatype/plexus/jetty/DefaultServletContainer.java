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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private String jettyXml;

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
        return jettyXml;
    }

    public void setJettyXmlUrl( String jettyXmlUrl )
    {
        this.jettyXml = jettyXmlUrl;
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
        
        if ( jettyXml != null )
        {
            getLogger().debug( "Loading configuration from jetty.xml file at: " + jettyXml );
            
            try
            {
                new XmlConfiguration( new File( jettyXml ).toURL() ).configure( getServer() );
            }
            catch ( SAXException e )
            {
                getLogger().error( "Failed to load configuration from jetty.xml at: " + jettyXml, e );
            }
            catch ( IOException e )
            {
                getLogger().error( "Failed to load configuration from jetty.xml at: " + jettyXml, e );
            }
            catch ( Exception e )
            {
                getLogger().error( "Failed to configure server instance from jetty.xml at: " + jettyXml, e );
            }
            
        }
        
        Map<String, String> configuredEndpoints = new LinkedHashMap<String, String>();
        
        StringBuilder msg = new StringBuilder( "The following connectors were configured from: " + jettyXml + ":" );
        
        Connector[] existingConnectors = getServer().getConnectors();
        for ( int i = 0; existingConnectors != null && i < existingConnectors.length; i++ )
        {
            String endpoint = endpointKey( existingConnectors[i].getHost(), existingConnectors[i].getPort() );
            String className = existingConnectors[i].getClass().getName();
            
            configuredEndpoints.put( endpoint, className );
            
            msg.append( "\n" ).append( endpoint ).append( " (" ).append( className ).append( ")" );
        }
        
        getLogger().debug( msg.toString() );

        try
        {
            // connectors
            if ( connectorInfos != null && connectorInfos.size() > 0 )
            {
                for ( int i = 0; i < connectorInfos.size(); i++ )
                {
                    ConnectorInfo info = connectorInfos.get( i );
                    String endpoint = endpointKey( info.getHost(), info.getPort() );
                    
                    if ( configuredEndpoints.containsKey( endpoint ) )
                    {
                        getLogger().info(
                                          "Skipping component-defined connector for: " + endpoint
                                              + ".\nIt has been overridden from: " + jettyXml
                                              + "\nusing connector of type: " + configuredEndpoints.get( endpoint ) );
                    }
                    else
                    {
                        Connector conn  = connectorInfos.get( i ).getConnector( context );

                        getLogger().info(
                            "Adding Jetty Connector " + conn.getClass().getName() + " on port "
                                + conn.getPort() );
                        
                        getServer().addConnector( conn );
                    }
                }
            }
            else
            {
                String endpoint = endpointKey( getDefaultHost(), getDefaultPort() );
                if ( configuredEndpoints.containsKey( endpoint ) )
                {
                    getLogger().info(
                                     "Skipping component-defined default connector: " + endpoint
                                         + ".\nIt has been overridden from: " + jettyXml
                                         + "\nusing connector of type: " + configuredEndpoints.get( endpoint ) );
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
            }

            // gathering stuff

            Handler[] handlers = getServer().getHandlers();
            Map<String, Handler> handlersByClassname = new LinkedHashMap<String, Handler>();
            for ( int j = 0; handlers != null && j < handlers.length; j++ )
            {
                handlersByClassname.put( handlers[j].getClass().getName(), handlers[j] );
            }
            
            Handler handler;

            // webapps

            if ( ( webappInfos != null && webappInfos.size() > 0 ) || ( webapps != null && webapps.isDirectory() ) )
            {
                boolean handlerIsNew = false;
                ContextHandlerCollection ctxHandler = (ContextHandlerCollection) handlersByClassname.get( ContextHandlerCollection.class.getName() );
                
                if ( ctxHandler == null )
                {
                    getLogger().debug( "Constructing new ContextHandlerCollection for webapp deployment." );
                    ctxHandler = new ContextHandlerCollection();
                    handlerIsNew = true;
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

                    webAppDeployer.setContexts( ctxHandler );

                    webAppDeployer.setExtract( true );

                    webAppDeployer.setAllowDuplicates( false );

                    webAppDeployer.setWebAppDir( webapps.getAbsolutePath() );
                    
                    getServer().addLifeCycle( webAppDeployer );
                }

                ctxHandler.mapContexts();

                if ( handlerIsNew )
                {
                    getServer().addHandler( ctxHandler );
                }
            }

            // handlers

            if ( handlerInfos != null && handlerInfos.size() > 0 )
            {
                for ( HandlerInfo handlerInfo : handlerInfos )
                {
                    handler = handlerInfo.getHandler( context );
                    if ( handlersByClassname.containsKey( handler.getClass().getName() ) )
                    {
                        getLogger().info(
                                          "Skipping component-defined handler: " + handler
                                              + ".\nIt has been overridden from: " + jettyXml
                                              + "\nusing handler of type: "
                                              + handlersByClassname.get( handler.getClass().getName() ) );
                    }
                    else
                    {
                        handler.setServer( getServer() );

                        getLogger().info( "Adding Jetty Handler " + handler.getClass().getName() );

                        getServer().addHandler( handler );
                    }
                }
            }
            else
            {
                if ( handlersByClassname.containsKey( DefaultHandler.class.getName() ) )
                {
                    getLogger().info(
                                      "Skipping component-defined DefaultHandler.\nIt has been overridden from: "
                                          + jettyXml );
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
        }
        catch ( Exception e )
        {
            throw new InitializationException( "Could not initialize ServletContainer!", e );
        }
    }

    private String endpointKey( String host, int port )
    {
        return ( host == null ? "" : host ) + ":" + port;
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
