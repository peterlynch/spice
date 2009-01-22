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
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.mortbay.jetty.Server;
import org.sonatype.plexus.jetty.TestLifeCycleListener;

public class LifeCycleListenerConfigurationTest
    extends TestCase
{

    private PlexusContainer plexusContainer;

    private Server server;

    public void tearDown()
        throws Exception
    {
        if ( server != null )
        {
            server.stop();
        }

        if ( plexusContainer != null )
        {
            plexusContainer.dispose();
        }

        super.tearDown();
    }

    public void testDisableTagLibsListenerConfiguration()
        throws IOException,
            InterpolationException,
            ComponentLookupException
    {
        plexusContainer = getContainer( "disabled-tlds" );

        System.out.println( "Looking up servlet container component." );
        DefaultServletContainer container = (DefaultServletContainer) plexusContainer.lookup( ServletContainer.class );

        System.out.println( "Retrieving server instance." );
        server = container.getServer();
    }

    public void testListenerConfiguration()
        throws IOException,
            InterpolationException,
            ComponentLookupException
    {
        // reset it, just in case.
        TestLifeCycleListener.startingCalled = false;

        plexusContainer = getContainer( "testListener" );

        System.out.println( "Looking up servlet container component." );
        DefaultServletContainer container = (DefaultServletContainer) plexusContainer.lookup( ServletContainer.class );

        System.out.println( "Retrieving server instance." );
        server = container.getServer();

        assertTrue( TestLifeCycleListener.startingCalled );

        // reset it again, just in case.
        TestLifeCycleListener.startingCalled = false;
    }

    private PlexusContainer getContainer( String plexusXmlSuffix )
        throws IOException,
            InterpolationException
    {
        PlexusContainer container = null;

        // ----------------------------------------------------------------------------
        // Context Setup
        // ----------------------------------------------------------------------------

        Map<Object, Object> context = new HashMap<Object, Object>();

        context.put( "basedir", PlexusTestCase.getBasedir() );

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

        String configName = getClass().getName().replace( '.', '/' )
            + ( plexusXmlSuffix == null ? "" : "-" + plexusXmlSuffix ) + ".xml";
        URL configUrl = Thread.currentThread().getContextClassLoader().getResource( configName );
        if ( configUrl == null )
        {
            fail( "Cannot find configuration: " + configName );
        }
        else
        {
            System.out.println( "Using configuration from: " + configUrl );
        }

        ContainerConfiguration containerConfiguration = new DefaultContainerConfiguration()
            .setContainerConfigurationURL( configUrl ).setName( "test" ).setContext( context );

        try
        {
            container = new DefaultPlexusContainer( containerConfiguration );
        }
        catch ( PlexusContainerException e )
        {
            e.printStackTrace();
            fail( "Failed to create plexus container." );
        }

        try
        {
            // set logging to DEBUG
            LoggerManager lm = container.lookup( LoggerManager.class );

            lm.setThreshold( Logger.LEVEL_DEBUG );

            lm.setThresholds( Logger.LEVEL_DEBUG );
        }
        catch ( ComponentLookupException e )
        {
            e.printStackTrace();
            fail( "Failed to lookup plexus LoggerManager." );
        }

        return container;
    }

}
