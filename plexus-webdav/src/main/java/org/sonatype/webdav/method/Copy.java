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
package org.sonatype.webdav.method;

import org.sonatype.webdav.MethodExecutionContext;
import org.sonatype.webdav.Resource;
import org.sonatype.webdav.ResourceCollection;
import org.sonatype.webdav.ResourceException;
import org.sonatype.webdav.UnauthorizedException;
import org.sonatype.webdav.WebdavStatus;
import org.sonatype.webdav.util.RequestUtil;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Webdav COPY method.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @plexus.component role="org.sonatype.webdav.Method" role-hint="copy"
 * @since 1.0
 */
public class Copy
    extends Delete
{

    public void execute( MethodExecutionContext context, HttpServletRequest req, HttpServletResponse res )
        throws IOException,
            UnauthorizedException
    {
        if ( !authorizeWrite( context.getUser(), res ) )
        {
            return;
        }

        if ( isReadOnly() )
        {
            res.sendError( WebdavStatus.SC_FORBIDDEN );
            return;
        }

        copyResource( context, req, res );
    }

    /**
     * Copy a resource.
     * 
     * @param req Servlet request
     * @param resp Servlet response
     * @return boolean true if the copy is successful
     */
    boolean copyResource( MethodExecutionContext context, HttpServletRequest req, HttpServletResponse resp )
        throws IOException,
            UnauthorizedException
    {

        // Parsing destination header

        String destinationPath = req.getHeader( "Destination" );

        if ( destinationPath == null )
        {
            resp.sendError( WebdavStatus.SC_BAD_REQUEST );
            return false;
        }

        // Remove url encoding from destination
        destinationPath = RequestUtil.URLDecode( destinationPath, "UTF8" );

        int protocolIndex = destinationPath.indexOf( "://" );
        if ( protocolIndex >= 0 )
        {
            // if the Destination URL contains the protocol, we can safely
            // trim everything upto the first "/" character after "://"
            int firstSeparator = destinationPath.indexOf( "/", protocolIndex + 4 );
            if ( firstSeparator < 0 )
            {
                destinationPath = "/";
            }
            else
            {
                destinationPath = destinationPath.substring( firstSeparator );
            }
        }
        else
        {
            String hostName = req.getServerName();
            if ( ( hostName != null ) && ( destinationPath.startsWith( hostName ) ) )
            {
                destinationPath = destinationPath.substring( hostName.length() );
            }

            int portIndex = destinationPath.indexOf( ":" );
            if ( portIndex >= 0 )
            {
                destinationPath = destinationPath.substring( portIndex );
            }

            if ( destinationPath.startsWith( ":" ) )
            {
                int firstSeparator = destinationPath.indexOf( "/" );
                if ( firstSeparator < 0 )
                {
                    destinationPath = "/";
                }
                else
                {
                    destinationPath = destinationPath.substring( firstSeparator );
                }
            }
        }

        // Normalise destination path (remove '.' and '..')
        destinationPath = normalize( destinationPath );

        String contextPath = req.getContextPath();
        if ( ( contextPath != null ) && ( destinationPath.startsWith( contextPath ) ) )
        {
            destinationPath = destinationPath.substring( contextPath.length() );
        }

        String pathInfo = req.getPathInfo();
        if ( pathInfo != null )
        {
            String servletPath = req.getServletPath();
            if ( ( servletPath != null ) && ( destinationPath.startsWith( servletPath ) ) )
            {
                destinationPath = destinationPath.substring( servletPath.length() );
            }
        }

        if ( debug > 0 )
        {
            getLogger().info( "Dest path :" + destinationPath );
        }

        if ( ( destinationPath.toUpperCase().startsWith( "/WEB-INF" ) )
            || ( destinationPath.toUpperCase().startsWith( "/META-INF" ) ) )
        {
            resp.sendError( WebdavStatus.SC_FORBIDDEN );
            return false;
        }

        String path = getRelativePath( req );

        if ( ( path.toUpperCase().startsWith( "/WEB-INF" ) ) || ( path.toUpperCase().startsWith( "/META-INF" ) ) )
        {
            resp.sendError( WebdavStatus.SC_FORBIDDEN );
            return false;
        }

        if ( destinationPath.equals( path ) )
        {
            resp.sendError( WebdavStatus.SC_FORBIDDEN );
            return false;
        }

        // Parsing overwrite header

        boolean overwrite = true;
        String overwriteHeader = req.getHeader( "Overwrite" );

        if ( overwriteHeader != null )
        {
            if ( overwriteHeader.equalsIgnoreCase( "T" ) )
            {
                overwrite = true;
            }
            else
            {
                overwrite = false;
            }
        }

        // Overwriting the destination

        boolean exists = true;
        try
        {
            resourceCollection.lookup( context, destinationPath );
        }
        catch ( ResourceException e )
        {
            exists = false;
        }

        if ( overwrite )
        {

            // Delete destination resource, if it exists
            if ( exists )
            {
                if ( !deleteResource( context, destinationPath, req, resp, true ) )
                {
                    return false;
                }
            }
            else
            {
                resp.setStatus( WebdavStatus.SC_CREATED );
            }

        }
        else
        {

            // If the destination exists, then it's a conflict
            if ( exists )
            {
                resp.sendError( WebdavStatus.SC_PRECONDITION_FAILED );
                return false;
            }

        }

        // Copying source to destination

        Hashtable errorList = new Hashtable();

        boolean result = copyResource( context, errorList, path, destinationPath );

        if ( ( !result ) || ( !errorList.isEmpty() ) )
        {

            sendReport( req, resp, errorList );
            return false;

        }

        // Removing any lock-null resource which would be present at
        // the destination path
        lockNullResources.remove( destinationPath );

        return true;

    }

    /**
     * Copy a collection.
     * 
     * @param errorList Hashtable containing the list of errors which occurred during the copy operation
     * @param source Path of the resource to be copied
     * @param dest Destination path
     */
    boolean copyResource( MethodExecutionContext context, Hashtable errorList, String source, String dest )
        throws UnauthorizedException
    {

        if ( debug > 1 )
        {
            getLogger().info( "Copy: " + source + " To: " + dest );
        }

        Object object;
        try
        {
            object = resourceCollection.lookup( context, source );
        }
        catch ( ResourceException e )
        {
            errorList.put( dest, new Integer( WebdavStatus.SC_CONFLICT ) );
            return false;
        }

        if ( object instanceof ResourceCollection )
        {

            try
            {
                resourceCollection.createSubcontext( context, dest );
            }
            catch ( ResourceException e )
            {
                errorList.put( dest, new Integer( WebdavStatus.SC_CONFLICT ) );
                return false;
            }

            try
            {
                Enumeration enumeration = resourceCollection.list( context, source );

                while ( enumeration.hasMoreElements() )
                {
                    Object ncPair = enumeration.nextElement();
                    String childDest = dest;
                    if ( !childDest.equals( "/" ) )
                    {
                        childDest += "/";
                    }
                    String childSrc = source;
                    if ( !childSrc.equals( "/" ) )
                    {
                        childSrc += "/";
                    }

                    if ( ncPair instanceof ResourceCollection )
                    {
                        childDest = ( (ResourceCollection) ncPair ).getPath().substring( source.length() );
                        childSrc = ( (ResourceCollection) ncPair ).getPath();
                    }
                    else
                    {
                        childDest += ( (Resource) ncPair ).getName();
                        childSrc += ( (Resource) ncPair ).getName();
                    }

                    copyResource( context, errorList, childSrc, childDest );
                }
            }
            catch ( ResourceException e )
            {
                e.printStackTrace();
                errorList.put( dest, new Integer( WebdavStatus.SC_INTERNAL_SERVER_ERROR ) );
                return false;
            }

        }
        else
        {
            Resource clone = null;
            try
            {
                clone = ( (Resource) object ).copy( context );

                resourceCollection.add( context, dest, clone );
            }
            catch ( ResourceException e )
            {
                // the destination resource does not exist...
                errorList.put( source, new Integer( WebdavStatus.SC_CONFLICT ) );

                // tidy up stale resources
                try
                {
                    clone.remove( context );
                }
                catch ( Exception re )
                {
                    // it did not get as far as making the copy
                }

                return false;
            }
        }
        return true;

    }

}
