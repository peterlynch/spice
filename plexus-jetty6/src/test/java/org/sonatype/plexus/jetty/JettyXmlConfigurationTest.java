package org.sonatype.plexus.jetty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
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
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.ajp.Ajp13SocketConnector;

public class JettyXmlConfigurationTest
    extends TestCase
{
    
    public void testConfigureAJPConnector()
        throws Exception
    {
        String jettyXmlName = "jetty-ajp-only.xml";
        PlexusContainer container = getContainer( getJettyXmlPath( jettyXmlName ) );
        
        Server server = null;
        try
        {
            DefaultServletContainer servletContainer = (DefaultServletContainer) container.lookup( ServletContainer.class );
            
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

    private PlexusContainer getContainer( String jettyXmlPath )
        throws IOException, InterpolationException
    {
        PlexusContainer container = null;
        
        // ----------------------------------------------------------------------------
        // Context Setup
        // ----------------------------------------------------------------------------

        Map<Object, Object> context = new HashMap<Object, Object>();

        context.put( "basedir", getBasedir() );
        context.put( "jetty.xml", jettyXmlPath );

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

        ContainerConfiguration containerConfiguration = new DefaultContainerConfiguration()
            .setName( "test" )
            .setContext( context );

        String resource = getConfigurationName( null );
        
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream( resource );
        if ( stream == null )
        {
            stream = new FileInputStream( new File( "src/test/resources", resource ) );
        }
        
        StringWriter sWriter = new StringWriter();
        IOUtil.copy( stream, sWriter );
        
        Interpolator interp = new StringSearchInterpolator();
        interp.addValueSource( new MapBasedValueSource( context ) );
        
        String configContent = interp.interpolate( sWriter.toString() );
        
        File plexusConf = File.createTempFile( "plexus.config.", ".temp.xml" );
        plexusConf.deleteOnExit();
        
        FileUtils.fileWrite( plexusConf.getAbsolutePath(), configContent );

        containerConfiguration.setContainerConfiguration( plexusConf.getAbsolutePath() );

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
                fail( "Cannot find Jetty configuration file: " + jettyXmlName + " (tried classpath and base-path src/test/resources/jetty-xmls)" );
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

}
