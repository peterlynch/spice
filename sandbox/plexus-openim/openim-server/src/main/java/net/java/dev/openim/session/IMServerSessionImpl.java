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
package net.java.dev.openim.session;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import net.java.dev.openim.ServerParameters;

/**
 * @version 1.5
 * @author AlAg
 */
public class IMServerSessionImpl
    extends AbstractIMSession
    implements IMServerSession, Initializable
{

    private ServerParameters serverParameters;

    private String remoteHostname;

    private volatile boolean dialbackValid;
    private volatile String dialbackValue;

    private IMServerSession twinSession;

    //-------------------------------------------------------------------------
    public void initialize()
        throws InitializationException
    {
        dialbackValid = false;
        disposed = new Boolean( false );
        synchronized ( lastSessionId )
        {
            sessionId = lastSessionId.longValue();
            lastSessionId = new Long( sessionId + 1 );
        }
    }

    //-------------------------------------------------------------------------
    public void close()
    {
        getLogger().debug( "Closing session id " + getId() );

        synchronized ( disposed )
        {
            dialbackValid = false;
            dialbackValue = null;
            try
            {
                if ( twinSession != null )
                {
                    twinSession.setTwinSession( null );
                }

            }
            catch ( Exception e )
            {
                getLogger().warn( "Session dispose failed (stage1): " + e.getMessage(), e );
            }

            try
            {
                writeOutputStream( "</stream:stream>" );
            }
            catch ( Exception e )
            {
                getLogger().warn( "Session dispose failed (stage2): " + e.getMessage() );
            }

            try
            {
                getLogger().debug( "Session " + sessionId + " closed" );

                if ( socket != null && !socket.isClosed() )
                {
                    socket.close();
                    outputStreamWriter.close();
                }
            }
            catch ( Exception e )
            {
                getLogger().warn( "Session dispose failed (stage3): " + e.getMessage(), e );
            }
            getLogger().debug( "Session " + sessionId + " disposed " );
        } // synchro
        disposed = new Boolean( true );
    }

    //-------------------------------------------------------------------------
    //-------------------------------------------------------------------------
    public boolean getDialbackValid()
    {
        return dialbackValid;
    }

    //-------------------------------------------------------------------------
    public void setDialbackValid( boolean value )
    {
        int ctype = getConnectionType();
        if ( ctype == S2S_R2L_CONNECTION || ctype == S2S_L2R_CONNECTION )
        {
            dialbackValid = value;
        }
    }

    //-------------------------------------------------------------------------
    public String getDialbackValue()
    {
        return dialbackValue;
    }

    //-------------------------------------------------------------------------
    public void setDialbackValue( String dialback )
    {
        dialbackValue = dialback;
    }

    //-------------------------------------------------------------------------
    public IMServerSession getTwinSession()
    {
        return twinSession;
    }

    //-------------------------------------------------------------------------
    public void setTwinSession( IMServerSession session )
    {
        twinSession = session;
    }

    //-------------------------------------------------------------------------
    //-------------------------------------------------------------------------
    public final String getRemoteHostname()
    {
        return remoteHostname;
    }

    //-------------------------------------------------------------------------
    public final void setRemoteHostname( final String remoteHostname )
    {
        this.remoteHostname = remoteHostname;
    }

    //-------------------------------------------------------------------------
    public int getConnectionType()
    {
        int type = UNKNOWN_CONNECTION;

        if ( socket != null )
        {
            if ( socket.getLocalPort() == serverParameters.getLocalServerPort()
                || socket.getLocalPort() == serverParameters.getLocalSSLServerPort() )
            {
                type = S2S_R2L_CONNECTION;
            }
            else if ( socket.getPort() == serverParameters.getRemoteServerPort() )
            {
                type = S2S_L2R_CONNECTION;
            }
        }

        return type;
    }

}
