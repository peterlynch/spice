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
            
            File tempDir = new File( System.getProperty( "java.io.tmpdir" ) );
            if ( !tempDir.exists() )
            {
                tempDir.mkdirs();
            }
            
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
