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
package org.sonatype.jettytestsuite;

import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.sonatype.jettytestsuite.proxy.MonitorableProxyServlet;

/**
 * Provides a proxy server
 *
 * @author marvin
 */
public class ProxyServer
    implements Initializable, Startable
{

    public MonitorableProxyServlet getProxyServlet()
    {
        return proxyServlet;
    }

    /** The Constant ROLE. */
    public static final String ROLE = ProxyServer.class.getName();

    /** The server. */
    private Server server;

    /** The port. */
    private int port;

    private MonitorableProxyServlet proxyServlet;

    private Context context;

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

    public String getUrl( String context )
    {
        return "http://localhost:" + getPort() + "/" + context;
    }

    public List<String> getAccessedUris()
    {
        if ( proxyServlet == null )
        {
            return null;
        }

        return proxyServlet.getAccessedUris();
    }

    public Context getProxyingContext()
    {
        return context;
    }

    // ===
    // Initializable iface

    /*
     * (non-Javadoc)
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable#initialize()
     */
    public void initialize()
        throws InitializationException
    {
        Server proxy = new Server();
        Connector connector = new SocketConnector();
        connector.setPort( getPort() );
        proxy.addConnector( connector );
        context = new Context( proxy, "/", 0 );
        proxyServlet = new MonitorableProxyServlet();
        context.addServlet( new ServletHolder( proxyServlet ), "/" );

        setServer( proxy );
    }

    // ===
    // Startable iface

    /*
     * (non-Javadoc)
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
