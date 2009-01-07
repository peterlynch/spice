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
package org.sonatype.webdav.method;

import org.sonatype.webdav.MethodExecutionContext;
import org.sonatype.webdav.Resource;
import org.sonatype.webdav.ResourceCollection;
import org.sonatype.webdav.ResourceException;
import org.sonatype.webdav.UnauthorizedException;
import org.sonatype.webdav.WebdavStatus;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Webdav DELETE method.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @plexus.component role="org.sonatype.webdav.Method" role-hint="delete"
 * @since 1.0
 */
public class Delete
    extends AbstractWebdavMethod
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

        if ( isLocked( req ) )
        {
            res.sendError( WebdavStatus.SC_LOCKED );
            return;
        }

        deleteResource( context, req, res );
    }

    /**
     * Delete a resource.
     * 
     * @param req Servlet request
     * @param resp Servlet response
     * @return boolean true if the copy is successful
     */
    protected boolean deleteResource( MethodExecutionContext context, HttpServletRequest req, HttpServletResponse resp )
        throws IOException,
            UnauthorizedException
    {

        String path = getRelativePath( req );

        return deleteResource( context, path, req, resp, true );

    }

    /**
     * Delete a resource.
     * 
     * @param path Path of the resource which is to be deleted
     * @param req Servlet request
     * @param resp Servlet response
     * @param setStatus Should the response status be set on successful completion
     */
    protected boolean deleteResource( MethodExecutionContext context, String path, HttpServletRequest req,
        HttpServletResponse resp, boolean setStatus )
        throws IOException,
            UnauthorizedException
    {

        if ( ( path.toUpperCase().startsWith( "/WEB-INF" ) ) || ( path.toUpperCase().startsWith( "/META-INF" ) ) )
        {
            resp.sendError( WebdavStatus.SC_FORBIDDEN );
            return false;
        }

        String ifHeader = req.getHeader( "If" );
        if ( ifHeader == null )
        {
            ifHeader = "";
        }

        String lockTokenHeader = req.getHeader( "Lock-Token" );
        if ( lockTokenHeader == null )
        {
            lockTokenHeader = "";
        }

        if ( isLocked( path, ifHeader + lockTokenHeader ) )
        {
            resp.sendError( WebdavStatus.SC_LOCKED );
            return false;
        }

        boolean exists = true;
        Object object = null;
        try
        {
            object = resourceCollection.lookup( context, path );
        }
        catch ( ResourceException e )
        {
            exists = false;
        }

        if ( !exists )
        {
            resp.sendError( WebdavStatus.SC_NOT_FOUND );
            return false;
        }

        boolean collection = ( object instanceof ResourceCollection );

        if ( !collection )
        {
            try
            {
                resourceCollection.delete( context, path );
            }
            catch ( ResourceException e )
            {
                resp.sendError( WebdavStatus.SC_INTERNAL_SERVER_ERROR );
                return false;
            }
        }
        else
        {
            Hashtable errorList = new Hashtable();

            deleteCollection( context, req, resourceCollection, path, errorList );
            try
            {
                resourceCollection.delete( context, path );
            }
            catch ( ResourceException e )
            {
                errorList.put( path, new Integer( WebdavStatus.SC_INTERNAL_SERVER_ERROR ) );
            }

            if ( !errorList.isEmpty() )
            {

                sendReport( req, resp, errorList );
                return false;

            }

        }
        if ( setStatus )
        {
            resp.setStatus( WebdavStatus.SC_NO_CONTENT );
        }
        return true;

    }

    /**
     * Deletes a collection.
     * 
     * @param resources Resources implementation associated with the context
     * @param path Path to the collection to be deleted
     * @param errorList Contains the list of the errors which occurred
     */
    private void deleteCollection( MethodExecutionContext context, HttpServletRequest req,
        ResourceCollection resources, String path, Hashtable errorList )
        throws UnauthorizedException
    {

        if ( debug > 1 )
        {
            getLogger().info( "Delete:" + path );
        }

        if ( ( path.toUpperCase().startsWith( "/WEB-INF" ) ) || ( path.toUpperCase().startsWith( "/META-INF" ) ) )
        {
            errorList.put( path, new Integer( WebdavStatus.SC_FORBIDDEN ) );
            return;
        }

        String ifHeader = req.getHeader( "If" );
        if ( ifHeader == null )
        {
            ifHeader = "";
        }

        String lockTokenHeader = req.getHeader( "Lock-Token" );
        if ( lockTokenHeader == null )
        {
            lockTokenHeader = "";
        }

        Enumeration enumeration = null;
        try
        {
            enumeration = resources.list( context, path );
        }
        catch ( ResourceException e )
        {
            getLogger().error(
                "WebdavServlet: org.sonatype.webdav.naming exception listing resourceCollection for " + path );
            errorList.put( path, new Integer( WebdavStatus.SC_INTERNAL_SERVER_ERROR ) );
            return;
        }

        while ( enumeration.hasMoreElements() )
        {
            Object ncPair = enumeration.nextElement();

            String childName = path;

            if ( !childName.equals( "/" ) )
            {
                childName += "/";
            }

            if ( ncPair instanceof ResourceCollection )
            {
                childName = ( (ResourceCollection) ncPair ).getPath();
            }
            else
            {
                childName += ( (Resource) ncPair ).getName();
            }

            if ( isLocked( childName, ifHeader + lockTokenHeader ) )
            {
                errorList.put( childName, new Integer( WebdavStatus.SC_LOCKED ) );
            }
            else
            {
                try
                {
                    Object object = resources.lookup( context, childName );
                    if ( object instanceof ResourceCollection )
                    {
                        deleteCollection( context, req, resources, childName, errorList );
                    }

                    try
                    {
                        resources.delete( context, childName );
                    }
                    catch ( ResourceException e )
                    {
                        if ( !( object instanceof ResourceCollection ) )
                        {
                            // If it's not a collection, then it's an unknown
                            // error
                            errorList.put( childName, new Integer( WebdavStatus.SC_INTERNAL_SERVER_ERROR ) );
                        }
                    }
                }
                catch ( ResourceException e )
                {
                    errorList.put( childName, new Integer( WebdavStatus.SC_INTERNAL_SERVER_ERROR ) );
                }
            }

        }

    }
}
