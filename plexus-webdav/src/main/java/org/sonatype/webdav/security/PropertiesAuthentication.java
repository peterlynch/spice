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
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * Created by IntelliJ IDEA.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @plexus.component role-hint="properties"
 * @since 1.0
 */
public class PropertiesAuthentication
    extends AbstractLogEnabled
    implements Authentication
{
    protected Properties properties;

    public User authenticate( String username, String password, Object session )
    {
        String user = properties.getProperty( username );

        if ( user == null )
        {
            return null;
        }

        if ( user.equals( password ) )
        {
            return new SimpleUser( username );
        }

        return null;
    }

    public User authenticate( HttpServletRequest req, HttpServletResponse res, HttpSession session )
    {
        String token = extractBasicAuthHeader( req );
        int delim;

        if ( token == null || ( delim = token.indexOf( ':' ) ) == -1 )
        {
            challenge( req, res, "Sonatype Webdav" );
            return null;
        }

        String username = token.substring( 0, delim );
        String password = token.substring( delim + 1 );

        User ret = authenticate( username, password, session );

        if ( ret == null )
        {
            // invalid credentials... rechallenge
            challenge( req, res, "Sonatype Webdav" );
        }

        return ret;
    }

    public void challenge( User user, HttpServletRequest req, HttpServletResponse res, HttpSession session )
        throws IOException
    {
        if ( user.isAnonymous() )
        {
            challenge( req, res, "Sonatype Webdav" );
        }
        else
        {
            res.sendError( HttpServletResponse.SC_FORBIDDEN, "Access forbidden." );
        }

    }

    public void setProperties( Properties properties )
    {
        this.properties = properties;
    }

    public String extractBasicAuthHeader( HttpServletRequest req )
    {
        String header = req.getHeader( "Authorization" );

        // in tomcat this is : authorization=Basic YWRtaW46TWFuYWdlMDc=
        if ( header == null )
        {
            header = req.getHeader( "authorization" );
        }

        if ( ( header != null ) && header.startsWith( "Basic " ) )
        {
            String base64Token = header.substring( 6 );
            String token = new String( Base64.decodeBase64( base64Token.getBytes() ) );

            return token;
        }
        else
        {
            return null;
        }
    }

    protected void challenge( HttpServletRequest req, HttpServletResponse res, String realmName )
    {
        res.addHeader( "WWW-Authenticate", "Basic realm=\"" + realmName + "\"" );
        String message = "You must provide a username and password to access this resource.";

        try
        {
            res.sendError( HttpServletResponse.SC_UNAUTHORIZED, message );
        }
        catch ( IOException e )
        {
            getLogger().error( "IOException during challenge.", e );
        }
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
