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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.configuration.ConfigurationException;

/**
 * A special "static" configuration source, that always return a factory provided defaults for Security configuration. It
 * is unmodifiable, since it actually reads the bundled config file from the module's JAR.
 * 
 * @author cstamas
 */
@Component( role = SecurityConfigurationSource.class, hint = "static" )
public class StaticConfigurationSource
    extends AbstractSecurityConfigurationSource
{
    
    private static final String STATIC_SECURITY_RESOURCE = "/META-INF/security/security.xml";
    
    @Requirement
    private Logger logger;
    
    /**
     * Gets the configuration using getResourceAsStream from "/META-INF/security/security.xml".
     */
    public InputStream getConfigurationAsStream()
        throws IOException
    {
        return getClass().getResourceAsStream( STATIC_SECURITY_RESOURCE );
    }

    public Configuration loadConfiguration()
        throws ConfigurationException,
            IOException
    {
        if( getClass().getResource( STATIC_SECURITY_RESOURCE ) != null )
        {
            loadConfiguration( getConfigurationAsStream() );
        }
        else
        {
            this.logger.warn( "Default static security configuration not found in classpath: "+ STATIC_SECURITY_RESOURCE );
        }
        
        Configuration configuration = getConfiguration();

        return configuration;
    }

    /**
     * This method will always throw UnsupportedOperationException, since SecurityDefaultsConfigurationSource is read only.
     */
    public void storeConfiguration()
        throws IOException
    {
        throw new UnsupportedOperationException( "The SecurityDefaultsConfigurationSource is static source!" );
    }

    /**
     * Static configuration has no default source, hence it cannot be defalted. Always returns false.
     */
    public boolean isConfigurationDefaulted()
    {
        return false;
    }

}
