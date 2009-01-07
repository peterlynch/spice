/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.jettytestsuite;

import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * The Class ServletServer. Heavily based on Joakim Erfeldt's work in wagon-webdav tests.
 * 
 * @author cstamas
 */
public class ServletServer
    implements Initializable, Startable
{

    /** The Constant ROLE. */
    public static final String ROLE = ServletServer.class.getName();

    /** The server. */
    private Server server;

    /** The port. */
    private int port;

    /** The webapp contexts. */
    private List<WebappContext> webappContexts;

    /**
     * Gets the server.
     * 
     * @return the server
     */
    public Server getServer()
    {
        return server;
    }

    /**
     * Sets the server.
     * 
     * @param server the new server
     */
    public void setServer( Server server )
    {
        this.server = server;
    }

    /**
     * Gets the port.
     * 
     * @return the port
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Sets the port.
     * 
     * @param port the new port
     */
    public void setPort( int port )
    {
        this.port = port;
    }

    /**
     * Gets the webapp contexts.
     * 
     * @return the webapp contexts
     */
    public List<WebappContext> getWebappContexts()
    {
        return webappContexts;
    }

    /**
     * Sets the webapp contexts.
     * 
     * @param webappContexts the new webapp contexts
     */
    public void setWebappContexts( List<WebappContext> webappContexts )
    {
        this.webappContexts = webappContexts;
    }

    public String getUrl( String context )
    {
        return "http://localhost:" + getPort() + "/" + context;
    }

    // ===
    // Initializable iface

    /*
     * (non-Javadoc)
     * 
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable#initialize()
     */
    public void initialize()
        throws InitializationException
    {
        setServer( new Server() );

        Connector connector = new SelectChannelConnector();
        connector.setPort( getPort() );
        server.setConnectors( new Connector[] { connector } );

        ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
        getServer().addHandler( contextHandlerCollection );

        if ( getWebappContexts() != null )
        {
            for ( WebappContext webappContext : getWebappContexts() )
            {
                try
                {
                    Context context = null;
                    if ( webappContext.getAuthenticationInfo() != null )
                    {
                        context = new Context(
                            contextHandlerCollection,
                            webappContext.getContextPath(),
                            Context.SESSIONS | Context.SECURITY );

                        HashUserRealm userRealm = new HashUserRealm( "default" );
                        userRealm.setConfig( webappContext.getAuthenticationInfo().getCredentialsFilePath() );

                        Constraint constraint = new Constraint(
                            webappContext.getAuthenticationInfo().getAuthMethod(),
                            Constraint.ANY_ROLE );
                        constraint.setAuthenticate( true );

                        ConstraintMapping constraintMapping = new ConstraintMapping();
                        constraintMapping.setPathSpec( "/*" );
                        constraintMapping.setConstraint( constraint );

                        context.getSecurityHandler().setUserRealm( userRealm );
                        context.getSecurityHandler().setAuthMethod(
                            webappContext.getAuthenticationInfo().getAuthMethod() );
                        context.getSecurityHandler().setConstraintMappings(
                            new ConstraintMapping[] { constraintMapping } );
                    }
                    else
                    {
                        context = new Context(
                            contextHandlerCollection,
                            webappContext.getContextPath(),
                            Context.SESSIONS | Context.NO_SECURITY );
                    }
                    context.setDisplayName( webappContext.getName() );

                    for ( ServletInfo servletInfo : webappContext.getServletInfos() )
                    {
                        ServletHolder servletHolder = context.addServlet( servletInfo.getServletClass(), servletInfo
                            .getMapping() );
                        for ( Map.Entry<Object, Object> entry : servletInfo.getParameters().entrySet() )
                        {
                            servletHolder.setInitParameter( entry.getKey().toString(), entry.getValue().toString() );
                        }
                    }
                }
                catch ( Exception e )
                {
                    throw new InitializationException(
                        "Unable to initialize webapp context " + webappContext.getName(),
                        e );
                }
            }
        }
    }

    // ===
    // Startable iface

    /*
     * (non-Javadoc)
     * 
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable#start()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable#stop()
     */
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
