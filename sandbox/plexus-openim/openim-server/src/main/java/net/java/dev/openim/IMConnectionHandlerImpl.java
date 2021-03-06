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
package net.java.dev.openim;

import net.java.dev.openim.session.IMSession;
import net.java.dev.openim.session.SessionsManager;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.xmlpull.v1.XmlPullParser;

/**
 * @version 1.5
 * @author AlAg
 */
public class IMConnectionHandlerImpl
    extends DefaultSessionProcessor
    implements IMConnectionHandler, Initializable, Disposable
{

    private ServerParameters serverParameters;
    private SessionsManager sessionsManager;
    private IMRouter router;
    private S2SConnectorManager s2sConnectorManager;

    
    //-------------------------------------------------------------------------
    public void initialize()
        throws InitializationException
    {
        s2sConnectorManager.setConnectionHandler( this );
        router.setS2SConnectorManager( s2sConnectorManager );
    }



    //-------------------------------------------------------------------------
    public void handleConnection( java.net.Socket socket )
        throws java.io.IOException, java.net.ProtocolException
    {
        getLogger().info( "Connection from " + socket.getRemoteSocketAddress() );

        IMSession session = null;
        try
        {

            if ( socket.getLocalPort() == serverParameters.getLocalClientPort()
                || socket.getLocalPort() == serverParameters.getLocalSSLClientPort() )
            {
                session = sessionsManager.getNewClientSession();
            }
            else
            {
                session = sessionsManager.getNewServerSession();
            }

            session.setRouter( router );

            getLogger().debug(
                               "######## [" + serverParameters.getHostName() + "] New session instance: "
                                   + session.getId() );
            session.setup( socket );
            //session.setHostname( m_serverParameters.getHostName() );

            //socket.setKeepAlive( true );

            final XmlPullParser xpp = session.getXmlPullParser();

            int eventType = xpp.getEventType();
            while ( eventType != XmlPullParser.START_DOCUMENT )
            {
                eventType = xpp.getEventType();
            }

            String s = "<?xml version='1.0' encoding='" + session.getEncoding() + "' ?>";
            session.writeOutputStream( s );

            process( session );
        }

        catch ( java.net.SocketException e )
        {
            String s = e.getMessage();
            getLogger().info( s );
        }

        catch ( java.io.EOFException e )
        {
            getLogger().info( e.getMessage() );
        }

        catch ( Exception e )
        {
            getLogger().error( e.getMessage(), e );
            throw new java.io.IOException( e.getMessage() );
        }

        finally
        {
            try
            {
                if ( session != null )
                {
                    if ( sessionsManager != null )
                    {
                        getLogger().info( "Release session " + session.getId() );
                        sessionsManager.release( session );
                    }
                }
                if ( !socket.isClosed() )
                {
                    socket.close();
                }
            }
            catch ( Exception e )
            {
                getLogger().error( e.getMessage(), e );
                throw new java.io.IOException( e.getMessage() );
            }
        }
        getLogger().info( "Disconnected session " + session.getId() );
    }

    public void dispose()
    {
        getLogger().debug( "Disposing Router" );
        // We must stop all sessions!
        // Hope the pull parser stops gracefully!
        router.releaseSessions();

        // Unfortunately we may also have sessions that was never authenticated
        // and therefore is not yet part of the router sessions
        sessionsManager.releaseSessions();

    }

}
