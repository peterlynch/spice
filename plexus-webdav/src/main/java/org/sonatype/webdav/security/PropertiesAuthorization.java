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
package org.sonatype.webdav.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @plexus.component role-hint="properties"
 * @since 1.0
 */
public class PropertiesAuthorization
    implements Authorization
{
    protected Properties properties;

    public boolean authorize( User user, Permission permission )
    {
        String roles = properties.getProperty( user.getUsername() );

        if ( roles == null )
        {
            return false;
        }

        List<String> roleList = Arrays.asList( roles.split( "," ) );
        return roleList.contains( permission.getId() );
    }

    public boolean authorize( User user, Permission permission, String path )
    {
        if ( !authorize( user, permission ) )
        {
            return false;
        }

        return authorizeTree( user.getUsername(), path );
    }

    private boolean authorizeTree( String username, String path )
    {
        String pathUsers = properties.getProperty( path );

        if ( pathUsers != null )
        {
            List<String> userList = Arrays.asList( pathUsers.split( "," ) );

            if ( userList.contains( username ) )
            {
                return true;
            }
        }

        String parent = ( new File( path ) ).getParent();
        if ( parent == null )
        {
            return false;
        }

        return authorizeTree( username, parent );
    }

    public void setProperties( Properties properties )
    {
        this.properties = properties;
    }

    public void loadProperties( String resource )
        throws IOException
    {
        if ( this.properties == null )
        {
            this.properties = new Properties();
        }

        // First see if the resource is a valid file
        File resourceFile = new File( resource );
        if ( resourceFile.exists() )
        {
            this.properties.load( new FileInputStream( resourceFile ) );
        }

        // Otherwise try to load it from the classpath
        this.properties.load( getClass().getClassLoader().getResourceAsStream( resource ) );
    }
}
