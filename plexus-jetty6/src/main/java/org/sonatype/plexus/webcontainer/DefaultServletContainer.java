/**
 * Copyright (C) 2008 Sonatype Inc. Sonatype Inc, licenses this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.sonatype.plexus.webcontainer;

import java.io.File;
import java.util.Iterator;
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
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.deployer.WebAppDeployer;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppClassLoader;
import org.mortbay.jetty.webapp.WebAppContext;
import org.sonatype.plexus.util.JettyUtils;

/**
 * @author cstamas
 * @plexus.component
 */
//TODO: Being able to send back a specified URL base
//TODO: figure out where the WARs are unpacked, and don't delete them 
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
    private List<org.sonatype.plexus.webcontainer.Connector> connectors;

    /** @plexus.configuration */
    private List<Handler> handlers;

    /** @plexus.configuration */
    private List<Webapp> webapps;

    /** @plexus.configuration */
    //private File webapps;
    private Context context;

    private Archiver archiver = new DefaultArchiver();

    public Server getServer()
    {
        return server;
    }

    public String getDefaultHost()
    {
        return defaultHost;
    }

    public int getDefaultPort()
    {
        return defaultPort;
    }

    public String getJettyXml()
    {
        return jettyXml;
    }
    
    public void setJettyXml( String jettyXml )
    {
        this.jettyXml = jettyXml;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        this.context = context;
    }

    public void initialize()
        throws InitializationException
    {
        // Log.setLog( new PlexusJettyLogger( getLogger() ) );

        server = new Server();

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

                if ( connectors != null && connectors.size() > 0 )
                {
                    for ( int i = 0; i < connectors.size(); i++ )
                    {
                        org.sonatype.plexus.webcontainer.Connector conn = connectors.get( i );
                        Connector jettyConnector = conn.getConnector( context );

                        getLogger().info( "Adding Jetty Connector " + jettyConnector.getClass().getName() + " on port " + jettyConnector.getPort() );
                        getServer().addConnector( jettyConnector );
                    }
                }
                else
                {
                    Connector jettyConnector = new SelectChannelConnector();
                    jettyConnector.setHost( getDefaultHost() );
                    jettyConnector.setPort( getDefaultPort() );

                    getLogger().info( "Adding default Jetty Connector " + jettyConnector.getClass().getName() + " on port " + getDefaultPort() );
                    
                    getServer().addConnector( jettyConnector );
                }

                // webapps

                if ( ( webapps != null && webapps.size() > 0 ) )
                {
                    ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();

                    org.mortbay.jetty.Handler jettyHandler;
                    
                    if ( webapps != null && webapps.size() > 0 )
                    {
                        for ( Webapp webapp : webapps )
                        {
                            if ( webapp.getWarPath().isDirectory() && new File( webapp.getWarPath(), "WEB-INF" ).exists() )
                            {
                                // The case where we have an exploded webapplication. We are also making the assumption here that everything
                                // that his web application needs is already in the classloader being used by the system.
                                jettyHandler = getWebAppContext( webapp, context, contextHandlerCollection );
                                getLogger().info( "Adding Jetty WebAppContext " + jettyHandler.getClass().getName() + " on context path " + webapp.getContextPath() + " from " + webapp.getWarPath() );
                            }                             
                            else if ( webapp.getWarPath().isFile() && webapp.getWarPath().getName().endsWith( ".war" ) )
                            {
                                // The case where we have an exploded webapplication. We need to deal with actually setting up the classloader
                                // properly because we are running plexus which controls the classloader.

                                File webappDir;

                                if ( webapp.getWebappDir() != null )
                                {
                                    webappDir = webapp.getWebappDir();
                                }
                                else
                                {
                                    webappDir = new File( webapp.getWarPath().getParentFile(), webapp.getContextPath() );
                                }

                                if ( !webappDir.exists() )
                                {
                                    webappDir.mkdirs();
                                }

                                archiver.unzip( webapp.getWarPath(), webappDir );
                                jettyHandler = (WebAppContext) getWebAppContext( webapp, context, contextHandlerCollection );
                                getLogger().info( "Adding Jetty WebAppContext " + jettyHandler.getClass().getName() + " on context path " + webapp.getContextPath() + " from " + webapp.getWarPath() );
                            }
                            else if ( webapp.getWarPath().isDirectory() && webapp.getWarPath().getName().equals( "webapps" ) )
                            {
                                getLogger().info( "Processing webapps!" );

                                // The case where we have a WAR file that has not been exploded                                                                                  
                                WebAppDeployer webAppDeployer = new WebAppDeployer();
                                webAppDeployer.setContexts( contextHandlerCollection );
                                webAppDeployer.setExtract( true );
                                webAppDeployer.setAllowDuplicates( false );
                                webAppDeployer.setWebAppDir( webapp.getWarPath().getAbsolutePath() );
                                server.addLifeCycle( webAppDeployer );
                            }                        
                        }
                    }
                    
                    contextHandlerCollection.mapContexts();
                    getServer().addHandler( contextHandlerCollection );                
                }

                // handlers

                /*
                if ( handlers != null && handlers.size() > 0 )
                {
                    for ( Handler handlerInfo : handlers )
                    {
                        ContextHandler jettyHandler = handlerInfo.getHandler( context );
                        jettyHandler.setServer( getServer() );
                        jettyHandlers.add( jettyHandler );
                        getLogger().info( "Adding Jetty Handler " + jettyHandler.getClass().getName() );
                    }
                }
                else
                {
                    DefaultHandler defHandler = new DefaultHandler();
                    defHandler.setServer( server );
                    defHandler.setServeIcon( false );
                    jettyHandlers.add( defHandler );
                    getLogger().info( "Adding default Jetty Handler " + defHandler.getClass().getName() );
                }
                */

                DefaultHandler defHandler = new DefaultHandler();
                defHandler.setServer( server );
                defHandler.setServeIcon( false );
                getLogger().info( "Adding default Jetty Handler " + defHandler.getClass().getName() );
                getServer().addHandler( defHandler );

            }
            catch ( Exception e )
            {
                throw new InitializationException( "Could not initialize ServletContainer!", e );
            }
        }
    }

    protected void deployStandardWebApplication()
    {
    }

    protected void deployPlexusWebApplication()
    {
    }

    public ContextHandler getWebAppContext( Webapp webapp, Context context, ContextHandlerCollection contextHandlerCollection )
        throws Exception
    {
        String contextPath = webapp.getContextPath();

        // Protected against the user not putting a leading slash on the context path.
        if ( !contextPath.startsWith( "/" ) )
        {
            contextPath = "/" + contextPath;
        }            
        
        WebAppContext webAppContext = new WebAppContext( contextHandlerCollection, webapp.getWarPath().getAbsolutePath(), contextPath );
                
        if ( webapp.getContextAttributes() != null )
        {
            for ( Iterator<Object> i = webapp.getContextAttributes().keySet().iterator(); i.hasNext(); )
            {
                String attributeKey = (String) i.next();
                webAppContext.setAttribute( attributeKey, webapp.getContextAttributes().getProperty( attributeKey ) );
            }
        }

        // Put the container for the application into the servlet context
        PlexusContainer container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
        webAppContext.setAttribute( PlexusConstants.PLEXUS_KEY, container );

        File webInfLib = new File( webapp.getWarPath(), "WEB-INF/lib" );
        File webInfClasses = new File( webapp.getWarPath(), "WEB-INF/classes" );

        // If there are no JARs or classes in the web application then this is a plexus web application where
        // everything is coming from the application JARs.
        if ( !webInfLib.exists() && !webInfClasses.exists() )
        {
            webAppContext.setClassLoader( container.getContainerRealm() );
        }
        else
        {
            if ( webapp.useParentLoader() )
            {
                WebAppClassLoader classLoader = new WebAppClassLoader( container.getContainerRealm(), webAppContext );
                webAppContext.setClassLoader( classLoader );
                webAppContext.setParentLoaderPriority( false );
            }
        }
        
        return webAppContext;
    }

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
}
