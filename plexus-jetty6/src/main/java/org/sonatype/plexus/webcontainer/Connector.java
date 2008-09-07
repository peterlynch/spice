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

import org.codehaus.plexus.context.Context;

public class Connector
    extends JettyComponent
{

    private String host = "localhost";

    private int port = 8081;

    private int headerBufferSize = -1;

    private int requestBufferSize = -1;

    private int responseBufferSize = -1;

    public String getHost()
    {
        return host;
    }

    public void setHost( String host )
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    public int getHeaderBufferSize()
    {
        return headerBufferSize;
    }

    public void setHeaderBufferSize( int headerBufferSize )
    {
        this.headerBufferSize = headerBufferSize;
    }

    public int getRequestBufferSize()
    {
        return requestBufferSize;
    }

    public void setRequestBufferSize( int requestBufferSize )
    {
        this.requestBufferSize = requestBufferSize;
    }

    public int getResponseBufferSize()
    {
        return responseBufferSize;
    }

    public void setResponseBufferSize( int responseBufferSize )
    {
        this.responseBufferSize = responseBufferSize;
    }

    public org.mortbay.jetty.Connector getConnector( Context context )
        throws Exception
    {
        // add some sensible defaults to enable users to specify only port for example
        // if both role and clazz is nil, then
        // TODO: see what port we have and default to HTTP/HTTPS?
        if ( getRole() == null && getClass() == null )
        {
            setClazz( "org.mortbay.jetty.nio.SelectChannelConnector" );
        }

        org.mortbay.jetty.Connector result = (org.mortbay.jetty.Connector) instantiate( context );

        result.setHost( getHost() );

        result.setPort( getPort() );

        if ( getHeaderBufferSize() > 0 )
        {
            result.setHeaderBufferSize( getHeaderBufferSize() );
        }

        if ( getRequestBufferSize() > 0 )
        {
            result.setRequestBufferSize( getRequestBufferSize() );
        }

        if ( getResponseBufferSize() > 0 )
        {
            result.setResponseBufferSize( getResponseBufferSize() );
        }

        return result;
    }
}
