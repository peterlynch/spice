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
package org.sonatype.plexus.webcontainer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.util.IOUtil;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.ajp.Ajp13SocketConnector;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.rewrite.RewriteHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

public class JettyXmlConfigurationTest
    extends TestCase
{

    private String defaultPort;

    private String testConnectorPort;

    private String testAJPPort;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        defaultPort = System.getProperty( "default-jetty-port", "18088" );
        testConnectorPort = System.getProperty( "test-connector-port", "18081" );
        testAJPPort = System.getProperty( "test-ajp-port", "18009" );
    }

    public void testConfigureAJPConnector()
        throws Exception
    {
        String jettyXmlName = "jetty-with-ajp.xml";
        PlexusContainer container = getContainer( getJettyXmlPath( jettyXmlName ) );

        Server server = null;
        try
        {
            DefaultServletContainer servletContainer =
                (DefaultServletContainer) container.lookup( ServletContainer.class );

            server = servletContainer.getServer();
            assertNotNull( server );

            Connector[] connectors = server.getConnectors();
            boolean foundAjpConnector = false;
            for ( int i = 0; i < connectors.length; i++ )
            {
                if ( connectors[i] instanceof Ajp13SocketConnector )
                {
                    foundAjpConnector = true;
                    break;
                }
            }

            assertTrue( "Should have found AJP connector.", foundAjpConnector );
        }
        finally
        {
            if ( server != null )
            {
                server.stop();
            }
        }
    }

    public void testConfigureRewriteHandler()
        throws Exception
    {
        String jettyXmlName = "jetty-with-rewrite-handler.xml";
        PlexusContainer container = getContainer( getJettyXmlPath( jettyXmlName ) );

        Server server = null;
        try
        {
            DefaultServletContainer servletContainer =
                (DefaultServletContainer) container.lookup( ServletContainer.class );

            server = servletContainer.getServer();
            assertNotNull( server );

            Handler[] handlers = server.getHandlers();
            if ( handlers == null )
            {
                handlers = new Handler[] { server.getHandler() };
            }

            boolean foundRewriteHandler = false;
            for ( int i = 0; i < handlers.length; i++ )
            {
                if ( handlers[i] instanceof RewriteHandler )
                {
                    foundRewriteHandler = true;
                    break;
                }
            }

            assertTrue( "Should have found rewrite handler.", foundRewriteHandler );
        }
        finally
        {
            if ( server != null )
            {
                server.stop();
            }
        }
    }

    public void testUseConfigurationFromComponentDefinition()
        throws Exception
    {
        PlexusContainer container = getContainer( null );

        Server server = null;
        try
        {
            DefaultServletContainer servletContainer =
                (DefaultServletContainer) container.lookup( ServletContainer.class );

            server = servletContainer.getServer();
            assertNotNull( server );

            Connector[] connectors = server.getConnectors();
            assertEquals( 1, connectors.length );
            assertTrue( connectors[0] instanceof SelectChannelConnector );
            assertEquals( "localhost", connectors[0].getHost() );
            assertEquals( servletContainer.getPort(), connectors[0].getPort() );

            Handler[] handlers = server.getHandlers();
            if ( handlers == null )
            {
                handlers = new Handler[] { server.getHandler() };
            }

            boolean foundDefaultHandler = false;
            for ( int i = 0; i < handlers.length; i++ )
            {
                if ( handlers[i] instanceof DefaultHandler )
                {
                    foundDefaultHandler = true;
                    break;
                }
            }

            assertTrue( "Should have found DefaultHandler", foundDefaultHandler );
        }
        finally
        {
            if ( server != null )
            {
                server.stop();
            }
        }
    }

    private PlexusContainer getContainer( String jettyXmlPath )
        throws IOException, InterpolationException
    {
        PlexusContainer container = null;

        // ----------------------------------------------------------------------------
        // Context Setup
        // ----------------------------------------------------------------------------

        Map<Object, Object> context = new HashMap<Object, Object>();

        context.put( "basedir", getBasedir() );
        if ( jettyXmlPath != null )
        {
            context.put( "jetty.xml", jettyXmlPath );
            context.put( "default-jetty-port", defaultPort );
            context.put( "test-connector-port", testConnectorPort );
            context.put( "test-ajp-port", testAJPPort );
        }

        boolean hasPlexusHome = context.containsKey( "plexus.home" );

        if ( !hasPlexusHome )
        {
            File f = PlexusTestCase.getTestFile( "target/plexus-home" );

            if ( !f.isDirectory() )
            {
                f.mkdir();
            }

            context.put( "plexus.home", f.getAbsolutePath() );
        }

        // ----------------------------------------------------------------------------
        // Configuration
        // ----------------------------------------------------------------------------

        ContainerConfiguration containerConfiguration =
            new DefaultContainerConfiguration().setName( "test" ).setContext( context );

        String resource = getConfigurationName( null );

        containerConfiguration.setContainerConfiguration( resource );

        try
        {
            container = new DefaultPlexusContainer( containerConfiguration );
        }
        catch ( PlexusContainerException e )
        {
            e.printStackTrace();
            fail( "Failed to create plexus container." );
        }

        return container;
    }

    private String getJettyXmlPath( String jettyXmlName )
    {
        String result = null;

        ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        URL res = cloader.getResource( "jetty-xmls/" + jettyXmlName );
        if ( res == null )
        {
            System.out.println( "Can't find jetty-xml: " + jettyXmlName + " on classpath; trying filesystem." );
            File f = new File( "src/test/resources/jetty-xmls/", jettyXmlName );

            if ( !f.isFile() )
            {
                fail( "Cannot find Jetty configuration file: " + jettyXmlName
                    + " (tried classpath and base-path src/test/resources/jetty-xmls)" );
            }

            result = f.getAbsolutePath();
        }
        else
        {
            result = res.getPath();
        }

        System.out.println( "Jetty configuration path is: '" + result + "'" );
        return result;
    }

    public static String getBasedir()
    {
        String basedir = System.getProperty( "basedir" );

        if ( basedir == null )
        {
            basedir = new File( "" ).getAbsolutePath();
        }

        return basedir;
    }

    protected String getConfigurationName( String subname )
    {
        return getClass().getName().replace( '.', '/' ) + ".xml";
    }

    public void testTwoContexts()
        throws Exception
    {
        // artifactory migration plugin does use 2 contexts!

        String jettyXmlName = "jetty-two-war-context.xml";
        PlexusContainer container = getContainer( getJettyXmlPath( jettyXmlName ) );

        Server server = null;
        try
        {
            DefaultServletContainer servletContainer =
                (DefaultServletContainer) container.lookup( ServletContainer.class );

            server = servletContainer.getServer();
            assertNotNull( server );

            ContextHandlerCollection handler = (ContextHandlerCollection) server.getHandler();
            assertNotNull( handler );
            assertEquals( 2, handler.getHandlers().length );

            WebAppContext h1 = (WebAppContext) handler.getHandlers()[0];
            assertEquals( "/context1", h1.getContextPath() );

            WebAppContext h2 = (WebAppContext) handler.getHandlers()[1];
            assertEquals( "/context2", h2.getContextPath() );

            assertTrue( server.isStarted() );

            URL u = new URL( "http://localhost:" + testConnectorPort + "/context1" );
            URLConnection conn = u.openConnection();
            String content = IOUtil.toString( conn.getInputStream() );
            assertTrue( content, content.contains( "Hello World!" ) );

            u = new URL( "http://localhost:" + testConnectorPort + "/context2" );
            conn = u.openConnection();
            content = IOUtil.toString( conn.getInputStream() );
            assertTrue( content, content.contains( "Hello Webapp0!" ) );
        }
        finally
        {
            if ( server != null )
            {
                server.stop();
            }
        }
    }
}
