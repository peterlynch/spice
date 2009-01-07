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
package net.java.dev.openim;

import java.util.List;
import java.net.InetAddress;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * @version 1.5
 * @author AlAg
 */
public class ServerParametersImpl
    extends AbstractLogEnabled
    implements ServerParameters, Initializable
{

    private List<String> hostnames;

    private int localClientPort;
    private int localClientThreadPool;

    private int localSSLClientPort;
    private int localSSLClientThreadPool;
    
    private int localServerPort;
    private int localServerThreadPool;
    
    private int localSSLServerPort;
    private int localSSLServerThreadPool;
    
    private int remoteServerPort;

    //-------------------------------------------------------------------------
    public void initialize()
        throws InitializationException
    {
        if ( hostnames.size() == 0 )
        {
            try
            {
                hostnames.add( InetAddress.getLocalHost().getHostName() );
            }
            catch ( java.net.UnknownHostException e )
            {
                throw new InitializationException( e.getMessage(), e );
            }
        }

    }

    //-------------------------------------------------------------------------
    public final int getLocalClientPort()
    {
        return localClientPort;
    }

    //-------------------------------------------------------------------------
    public final int getLocalSSLClientPort()
    {
        return localSSLClientPort;
    }

    //-------------------------------------------------------------------------
    public final int getLocalServerPort()
    {
        return localServerPort;
    }

    //-------------------------------------------------------------------------
    public final int getLocalSSLServerPort()
    {
        return localSSLServerPort;
    }

    //-------------------------------------------------------------------------
    public final List getHostNameList()
    {
        return hostnames;
    }

    //-------------------------------------------------------------------------
    public final String getHostName()
    {
        return (String) hostnames.get( 0 );
    }

    //-------------------------------------------------------------------------
    public final int getRemoteServerPort()
    {
        return remoteServerPort;
    }
    
    
    //-------------------------------------------------------------------------
    public int getLocalClientThreadPool()
    {
        return localClientThreadPool;
    }
    //-------------------------------------------------------------------------
    public int getLocalSSLServerThreadPool()
    {
        return localSSLServerThreadPool;
    }
    //-------------------------------------------------------------------------
    public int getLocalServerThreadPool()
    {
        return localServerThreadPool;
    }
    //-------------------------------------------------------------------------
    public int getLocalSSLClientThreadPool()
    {
        return localSSLClientThreadPool;
    }

    
    
    
    
}
