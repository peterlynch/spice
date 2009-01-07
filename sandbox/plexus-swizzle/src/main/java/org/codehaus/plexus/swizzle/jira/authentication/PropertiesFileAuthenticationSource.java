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
package org.codehaus.plexus.swizzle.jira.authentication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * <p>
 * A simple authentication source that uses a properties file. If you format the properties
 * file using the ":" as the delimiter then Ruby's YAML package can also use the configuration.
 * So the following would work with both Java and Ruby:
 * </p>
 *
 * <pre>
 * user: jason
 * password: monkey
 * </pre>
 *
 * @author Jason van Zyl
 * @version $Id$
 */
@Component(role = AuthenticationSource.class)
public class PropertiesFileAuthenticationSource
    implements AuthenticationSource, Initializable
{
    private String login;

    private String password;

    private File propertiesFile;

    public PropertiesFileAuthenticationSource( File propertiesFile )
        throws InitializationException
    {
        this.propertiesFile = propertiesFile;
        initialize();
    }
    
    public void initialize()
        throws InitializationException
    {
        if ( propertiesFile == null )
        {
            propertiesFile = new File( System.getProperty( "user.home" ), "jira.properties" );
        }

        Properties p = new Properties();

        try
        {
            p.load( new FileInputStream( propertiesFile ) );

            login = p.getProperty( "username" );

            if ( login == null )
            {
                throw new InitializationException( "Source contains no login information." );
            }

            password = p.getProperty( "password" );

            if ( password == null )
            {
                throw new InitializationException( "Source contains no password information." );
            }

        }
        catch ( IOException e )
        {
            throw new InitializationException( "Cannot find " + propertiesFile + "for login and password information." );
        }
    }

    public String getLogin()
    {
        return login;
    }

    public String getPassword()
    {
        return password;
    }
}
