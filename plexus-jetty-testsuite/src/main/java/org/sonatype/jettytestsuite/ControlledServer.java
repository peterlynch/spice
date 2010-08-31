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
package org.sonatype.jettytestsuite;

import java.io.File;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.sonatype.jettytestsuite.proxy.FileServerServlet;
import org.sonatype.jettytestsuite.proxy.StatusServlet;
import org.sonatype.jettytestsuite.proxy.UnstableFileServerServlet;

/**
 * Provides a proxy server
 *
 * @author marvin
 */
public class ControlledServer
    implements Initializable
{

    /** The Constant ROLE. */
    public static final String ROLE = ControlledServer.class.getName();

    private Context context;

    /** The port. */
    private int port;

    /** The server. */
    private Server server;

    /**
     * Gets the port.
     *
     * @return the port
     */
    public int getPort()
    {
        return port;
    }

    public Context getProxyingContext()
    {
        return context;
    }

    /**
     * Gets the server.
     *
     * @return the server
     */
    public Server getServer()
    {
        return server;
    }

    public String getUrl( String context )
    {
        return "http://localhost:" + getPort() + "/" + context;
    }

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
        setServer( proxy );
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

    // ===
    // Initializable iface

    /**
     * Sets the server.
     *
     * @param server the new server
     */
    public void setServer( Server server )
    {
        this.server = server;
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

    public List<String> addServer( String contextName, final int returnCode )
    {
        StatusServlet servlet = new StatusServlet( returnCode );
        context.addServlet( new ServletHolder( servlet ), "/" + contextName  + "/*");
        return servlet.getAccessedUrls();
    }

    public List<String> addServer( String contextName, final File content )
    {
        return this.addServer( contextName, content, -1 );
    }

    public List<String> addServer( String contextName, final File content, final int speedLimit )
    {
        FileServerServlet servlet = new FileServerServlet( content, speedLimit );
        context.addServlet( new ServletHolder( servlet ), "/" + contextName + "/*" );
        return servlet.getAccessedUrls();
    }

    public List<String> addServer( String contextName, int numberOfTries, int returnCode, File content )
    {
        UnstableFileServerServlet servlet = new UnstableFileServerServlet(numberOfTries, returnCode,  content );
        context.addServlet( new ServletHolder( servlet ), "/" + contextName + "/*" );
        return servlet.getAccessedUrls();
    }


}
