/**
  * Copyright (C) 2008 Sonatype Inc. 
  * Sonatype Inc, licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in 
  * compliance with the License.  You may obtain a copy of the License at
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
