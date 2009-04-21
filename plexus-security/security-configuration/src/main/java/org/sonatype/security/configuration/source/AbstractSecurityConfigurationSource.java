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
package org.sonatype.security.configuration.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.jsecurity.model.io.xpp3.SecurityConfigurationXpp3Reader;
import org.sonatype.jsecurity.model.io.xpp3.SecurityConfigurationXpp3Writer;

/**
 * Abstract class that encapsulates Modello model loading and saving with interpolation.
 * 
 * @author cstamas
 */
public abstract class AbstractSecurityConfigurationSource
    extends AbstractConfigurationSource
    implements SecurityConfigurationSource
{

    /** The configuration. */
    private Configuration configuration;

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration( Configuration configuration )
    {
        this.configuration = configuration;
    }

    /**
     * Called by subclasses when loaded configuration is rejected for some reason.
     */
    protected void rejectConfiguration( String message )
    {
        this.configuration = null;

        if ( message != null )
        {
            getLogger().warn( message );
        }
    }

    /**
     * Load configuration.
     * 
     * @param file the file
     * @return the configuration
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void loadConfiguration( InputStream is )
        throws IOException
    {
        setConfigurationUpgraded( false );

        Reader fr = null;

        try
        {
            SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();

            fr = new InputStreamReader( is );

            configuration = reader.read( fr );
        }
        catch ( XmlPullParserException e )
        {
            rejectConfiguration( "Security configuration file was not loaded, it has the wrong structure." );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "security.xml is broken:", e );
            }
        }
        finally
        {
            if ( fr != null )
            {
                fr.close();
            }
        }

        // check the model version if loaded
        if ( configuration != null && !Configuration.MODEL_VERSION.equals( configuration.getVersion() ) )
        {
            rejectConfiguration( "Security configuration file was loaded but discarded, it has the wrong version number." );
        }

        if ( getConfiguration() != null )
        {
            getLogger().debug( "Configuration loaded succesfully." );
        }
    }

    /**
     * Save configuration.
     * 
     * @param file the file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void saveConfiguration( OutputStream os, Configuration configuration )
        throws IOException
    {
        Writer fw = null;
        try
        {
            fw = new OutputStreamWriter( os );

            SecurityConfigurationXpp3Writer writer = new SecurityConfigurationXpp3Writer();

            writer.write( fw, configuration );
        }
        finally
        {
            if ( fw != null )
            {
                fw.flush();

                fw.close();
            }
        }
    }

    /**
     * Returns the default source of ConfigurationSource. May be null.
     */
    public SecurityConfigurationSource getDefaultsSource()
    {
        return null;
    }

}
