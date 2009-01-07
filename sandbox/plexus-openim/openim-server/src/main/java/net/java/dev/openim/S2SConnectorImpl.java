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

import java.net.Socket;
import java.net.InetSocketAddress;

import java.io.IOException;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.xmlpull.v1.XmlPullParser;

import net.java.dev.openim.session.SessionsManager;
import net.java.dev.openim.session.IMServerSession;

/**
 * @version 1.5
 * @author AlAg
 */

public class S2SConnectorImpl
    extends AbstractLogEnabled
    implements S2SConnector, Runnable
{

    // Requirements
    private ServerParameters serverParameters;

    // Configuration
    private int deliveryConnectionTimout;

    // Locals
    private SessionsManager sessionsManager;
    private IMConnectionHandler connectionHandler;
    private String toHostName;
    private IMServerSession session;
    private IMRouter router;

    private volatile boolean isAlive = false;
    private volatile boolean ready = false;
    private volatile boolean sendResult = false;
    private volatile boolean sendVerify = false;
    private volatile String verifyDialbackValue;
    private volatile String verifyId;

    //-------------------------------------------------------------------------    
    public void setToHostname( String toHostname )
    {
        this.toHostName = toHostname;
    }

    //-------------------------------------------------------------------------    
    public void setRouter( IMRouter router )
    {
        this.router = router;
    }

    //-------------------------------------------------------------------------    
    public void setIMConnectionHandler( IMConnectionHandler connectionHandler )
    {
        this.connectionHandler = connectionHandler;
    }

    //-------------------------------------------------------------------------
    public void setSessionsManager( SessionsManager sessionsManager )
    {
        this.sessionsManager = sessionsManager;
    }

    //----------------------------------------------------------------------
    public IMServerSession getSession()
        throws Exception
    {
        if ( session == null )
        {
            session = sessionsManager.getNewServerSession();
            session.setRouter( router );
            session.setRemoteHostname( toHostName );
        }
        return session;
    }

    //----------------------------------------------------------------------
    public boolean isAlive()
    {
        return isAlive;
    }

    //----------------------------------------------------------------------
    public void run()
    {
        isAlive = true;
        try
        {

            //Socket socket = new Socket( toHostname, m_serverParameters.getRemoteServerPort() );
            Socket socket = new Socket();
            InetSocketAddress insa = new InetSocketAddress( toHostName, serverParameters.getRemoteServerPort() );
            getLogger().debug(
                               "Trying to connect (timeout " + deliveryConnectionTimout + " ms) to " + toHostName + ":"
                                   + serverParameters.getRemoteServerPort() );
            socket.connect( insa, deliveryConnectionTimout );
            getLogger().info(
                              "Connection to " + toHostName + ":" + serverParameters.getRemoteServerPort()
                                  + " successfull" );
            //socket.setKeepAlive( true );

            IMServerSession session = getSession();
            session.setup( socket );

            final XmlPullParser xpp = session.getXmlPullParser();

            int eventType = xpp.getEventType();
            while ( eventType != XmlPullParser.START_DOCUMENT )
            {
                eventType = xpp.getEventType();
            }

            // initial connection string
            String s = "<?xml version='1.0' encoding='" + session.getEncoding() + "' ?>";
            s += "<stream:stream xmlns:stream='http://etherx.jabber.org/streams' " + "xmlns='jabber:server' " + "to='"
                + toHostName + "' " + "from='" + serverParameters.getHostName() + "' " + "id='" + session.getId()
                + "' " + "xmlns:db='jabber:server:dialback'>";

            session.writeOutputStream( s );

            ready = true;
            if ( sendVerify )
            {
                sendVerify( verifyDialbackValue, verifyId );
            }
            if ( sendResult )
            {
                sendResult();
            }
            connectionHandler.process( session );
        }
        catch ( Exception e )
        {
            getLogger().error( "L2R " + toHostName + " session exception: " + e.getMessage(), e );
        }
        finally
        {
            isAlive = false;
            if ( !session.isClosed() )
            {
                getLogger().info( "Release session " + session.getId() );
                sessionsManager.release( session );
            }
            // unlock all thread
            synchronized ( session )
            {
                session.notifyAll();
            }
        }
    }

    //----------------------------------------------------------------------
    public void sendResult()
        throws IOException
    {

        if ( !ready )
        {
            sendResult = true;
        }
        else
        {

            if ( session.getDialbackValue() == null )
            {
                String dialbackValue = Long.toString( session.getId() );
                session.setDialbackValue( dialbackValue );

                String s = "<db:result from='" + serverParameters.getHostName() + "' to='" + toHostName + "'>";
                s += dialbackValue;
                s += "</db:result>";
                getLogger().info( "Started dialback validation for host " + toHostName + " id " + session.getId() );
                session.writeOutputStream( s );
            }
        }

    }

    //----------------------------------------------------------------------
    public void sendVerify( String dialbackValue, String id )
        throws IOException
    {
        if ( !ready )
        {
            sendVerify = true;
            verifyDialbackValue = dialbackValue;
            verifyId = id;
        }
        else
        {
            String s = "<db:verify from='" + serverParameters.getHostName() + "' to='" + toHostName + "' id='" + id
                + "'>";
            s += dialbackValue;
            s += "</db:verify>";
            session.writeOutputStream( s );
        }
    }

} // class
