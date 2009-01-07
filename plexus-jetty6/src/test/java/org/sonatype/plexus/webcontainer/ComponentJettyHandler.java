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
package org.sonatype.plexus.webcontainer;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.DefaultHandler;

public class ComponentJettyHandler
    extends DefaultHandler
{

    public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch )
        throws IOException,
            ServletException
    {
        Request base_request = request instanceof Request ? (Request) request : HttpConnection
            .getCurrentConnection().getRequest();

        if ( response.isCommitted() || base_request.isHandled() )
            return;

        if ( "/handleThis".equals( target ) )
        {
            response.getWriter().println( "I handle this!" );
            
            base_request.setHandled( true );
        }
        else
        {
            super.handle( target, request, response, dispatch );
        }
    }

}
