/**
  * Copyright (C) 2008 Sonatype Inc. 
  * Sonatype Inc, licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in 
  * compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
package org.sonatype.webdav.method;

import org.sonatype.webdav.MethodExecutionContext;
import org.sonatype.webdav.Resource;
import org.sonatype.webdav.ResourceException;
import org.sonatype.webdav.UnauthorizedException;
import org.sonatype.webdav.WebdavStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Webdav PUT method.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @plexus.component role="org.sonatype.webdav.Method" role-hint="put"
 * @since 1.0
 */
public class Put
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

        if ( isLocked( req ) )
        {
            res.sendError( WebdavStatus.SC_LOCKED );
            return;
        }

        {
            if ( isReadOnly() )
            {
                res.sendError( HttpServletResponse.SC_FORBIDDEN );
                return;
            }

            String path = getRelativePath( req );

            boolean exists = true;
            try
            {
                resourceCollection.lookup( context, path );
            }
            catch ( ResourceException e )
            {
                exists = false;
            }

            boolean result = true;

            // Temp. content file used to support partial PUT
            File contentFile = null;

            Range range = parseContentRange( req, res );

            InputStream resourceInputStream = null;

            // Append data specified in ranges to existing content for this
            // resource - create a temp. file on the local filesystem to
            // perform this operation
            // Assume just one range is specified for now
            if ( range != null )
            {
                contentFile = executePartialPut( context, req, range, path );
                resourceInputStream = new FileInputStream( contentFile );
            }
            else
            {
                resourceInputStream = req.getInputStream();
            }

            try
            {
                Resource newResource = resourceCollection.createResource( context, path );
                newResource.setContent( resourceInputStream );

                // FIXME: Add attributes
                if ( exists )
                {
                    resourceCollection.replace( context, path, newResource );
                }
                else
                {
                    resourceCollection.add( context, path, newResource );
                }
            }
            catch ( ResourceException e )
            {
                e.printStackTrace();
                result = false;
            }

            // Bugzilla 40326: at this point content file should be safe to delete
            // as it's no longer referenced. Let's not rely on deleteOnExit because
            // it's a memory leak, as noted in this Bugzilla issue.
            try
            {
                if ( range != null )
                {
                    contentFile.delete();
                }
            }
            catch ( Exception e )
            {
                getLogger().error( "DefaultServlet.doPut: couldn't delete temporary file: " + e.getMessage() );
            }

            if ( result )
            {
                if ( exists )
                {
                    res.setStatus( HttpServletResponse.SC_NO_CONTENT );
                }
                else
                {
                    res.setStatus( HttpServletResponse.SC_CREATED );
                }
            }
            else
            {
                res.sendError( HttpServletResponse.SC_CONFLICT );
            }
        }

        String path = getRelativePath( req );

        // Removing any lock-null resource which would be present
        lockNullResources.remove( path );
    }
}
