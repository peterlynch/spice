package org.sonatype.plexus.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextMapAdapter;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.IOUtil;
import org.mortbay.component.LifeCycle.Listener;
import org.mortbay.jetty.Server;
import org.mortbay.xml.XmlConfiguration;
import org.sonatype.plexus.jetty.LifecycleListenerInfo;
import org.sonatype.plexus.webcontainer.LifecycleListener;

public final class JettyUtils
{
    
    private JettyUtils()
    {
    }
    
    public static void configureServer( Server server, File jettyXml, Context containerContext, Logger logger )
        throws InitializationException
    {
        logger.debug( "Loading configuration from jetty.xml file at: " + jettyXml );
        
        StringWriter sWriter = new StringWriter();
        FileReader fReader = null;
        try
        {
            fReader = new FileReader( jettyXml );
            
            IOUtil.copy( fReader, sWriter );
        }
        catch ( IOException e )
        {
            throw new InitializationException( "Failed to read Jetty configuration from jetty.xml at: " + jettyXml, e );
        }
        finally
        {
            IOUtil.close( fReader );
        }
        
        Interpolator interpolator = new StringSearchInterpolator();
        interpolator.addValueSource( new MapBasedValueSource( new ContextMapAdapter( containerContext ) ) );
        
        String config;
        File tempJettyXml;
        FileWriter fWriter = null;
        try
        {
            config = interpolator.interpolate( sWriter.toString() );
            
            tempJettyXml = File.createTempFile( "jetty.", ".xml" );
            tempJettyXml.deleteOnExit();
            
            fWriter = new FileWriter( tempJettyXml );
            fWriter.write( config );
        }
        catch ( InterpolationException e )
        {
            throw new InitializationException( "Failed to interpolate expressions in jetty.xml at: " + jettyXml, e );
        }
        catch ( IOException e )
        {
            throw new InitializationException( "Failed to write temporary, interpolated jetty.xml file", e );
        }
        finally
        {
            IOUtil.close( fWriter );
        }
        
        try
        {
            logger.debug( "Using interpolated configuration file: " + tempJettyXml + ". This file will be deleted when the system exits." );
            
            new XmlConfiguration( tempJettyXml.toURL() ).configure( server );
        }
        catch ( Exception e )
        {
            throw new InitializationException( "Failed to configure Jetty server using jetty.xml at: " + jettyXml, e );
        }
    }

}
