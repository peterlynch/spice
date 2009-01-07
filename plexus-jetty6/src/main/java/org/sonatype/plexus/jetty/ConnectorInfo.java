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
package org.sonatype.plexus.jetty;

import org.codehaus.plexus.context.Context;
import org.mortbay.jetty.Connector;

public class ConnectorInfo
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

    public Connector getConnector( Context context )
        throws Exception
    {
        // add some sensible defaults to enable users to specify only port for example
        // if both role and clazz is nil, then
        // TODO: see what port we have and default to HTTP/HTTPS?
        if ( getRole() == null && getClass() == null )
        {
            setClazz( "org.mortbay.jetty.nio.SelectChannelConnector" );
        }

        Connector result = (Connector) instantiate( context );

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
