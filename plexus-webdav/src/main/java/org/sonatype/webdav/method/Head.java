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
import org.sonatype.webdav.UnauthorizedException;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Webdav HEAD method.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @plexus.component role="org.sonatype.webdav.Method" role-hint="head"
 * @since 1.0
 */
public class Head
    extends AbstractMethod
{

    public void execute( MethodExecutionContext context, HttpServletRequest req, HttpServletResponse res )
        throws IOException,
            UnauthorizedException
    {
        if ( !authorizeRead( context.getUser(), res ) )
        {
            return;
        }
        // Serve the requested resource, without the data content
        serveResource( context, req, res, false );
    }
}
