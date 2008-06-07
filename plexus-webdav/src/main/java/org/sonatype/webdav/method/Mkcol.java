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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;

import org.sonatype.webdav.MethodExecutionContext;
import org.sonatype.webdav.ResourceException;
import org.sonatype.webdav.UnauthorizedException;
import org.sonatype.webdav.WebdavStatus;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Webdav MKCOL method.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @plexus.component role="org.sonatype.webdav.Method" role-hint="mkcol"
 * @since 1.0
 */
public class Mkcol
    extends AbstractWebdavMethod
{

    public void execute( MethodExecutionContext context, HttpServletRequest req, HttpServletResponse res )
        throws IOException, UnauthorizedException
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

        String path = getRelativePath( req );

        if ( ( path.toUpperCase().startsWith( "/WEB-INF" ) ) || ( path.toUpperCase().startsWith( "/META-INF" ) ) )
        {
            res.sendError( WebdavStatus.SC_FORBIDDEN );
            return;
        }

        boolean exists = true;
        try
        {
            resourceCollection.lookup( context, path );
        }
        catch ( ResourceException e )
        {
            exists = false;
        }

        // Can't create a collection if a resource already exists at the given
        // path
        if ( exists )
        {
            // Get allowed methods
            StringBuffer methodsAllowed = determineMethodsAllowed( context, resourceCollection, req );

            res.addHeader( "Allow", methodsAllowed.toString() );

            res.sendError( WebdavStatus.SC_METHOD_NOT_ALLOWED );
            return;
        }

        if ( req.getInputStream().available() > 0 )
        {
            DocumentBuilder documentBuilder = getDocumentBuilder();
            try
            {
                Document document = documentBuilder.parse( new InputSource( req.getInputStream() ) );
                // TODO : Process this request body
                res.sendError( WebdavStatus.SC_NOT_IMPLEMENTED );
                return;

            }
            catch ( SAXException saxe )
            {
                // Parse error - assume invalid content
                res.sendError( WebdavStatus.SC_BAD_REQUEST );
                return;
            }
        }

        boolean result = true;
        try
        {
            resourceCollection.createSubcontext( context, path );
        }
        catch ( ResourceException e )
        {
            result = false;
        }

        if ( !result )
        {
            res.sendError( WebdavStatus.SC_CONFLICT, WebdavStatus.getStatusText( WebdavStatus.SC_CONFLICT ) );
        }
        else
        {
            res.setStatus( WebdavStatus.SC_CREATED );
            // Removing any lock-null resource which would be present
            lockNullResources.remove( path );
        }
    }
}
