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
package org.sonatype.security.realms.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.io.xpp3.SecurityConfigurationXpp3Reader;

public abstract class AbstractStaticSecurityResource
    implements StaticSecurityResource
{
    @Inject
    @Requirement
    private Logger logger;

    protected boolean dirty = false;

    public boolean isDirty()
    {
        return dirty;
    }

    protected void setDirty( boolean dirty )
    {
        this.dirty = dirty;
    }

    protected abstract String getResourcePath();

    public Configuration getConfiguration()
    {
        String resourcePath = this.getResourcePath();

        if ( StringUtils.isNotEmpty( resourcePath ) )
        {
            Reader fr = null;
            InputStream is = null;

            this.logger.debug( "Loading static security config from " + resourcePath );

            try
            {
                is = getClass().getResourceAsStream( resourcePath );
                SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();

                fr = new InputStreamReader( is );
                return reader.read( fr );
            }
            catch ( IOException e )
            {
                this.logger.error( "IOException while retrieving configuration file", e );
            }
            catch ( XmlPullParserException e )
            {
                this.logger.error( "Invalid XML Configuration", e );
            }
            finally
            {
                IOUtil.close( fr );
                IOUtil.close( is );
            }
        }
        
        // any other time just return null
        return null;
    }
}
