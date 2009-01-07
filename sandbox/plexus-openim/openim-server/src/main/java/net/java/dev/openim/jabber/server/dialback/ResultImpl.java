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
package net.java.dev.openim.jabber.server.dialback;

import net.java.dev.openim.DefaultSessionProcessor;
import net.java.dev.openim.session.IMServerSession;
import net.java.dev.openim.session.IMSession;

import org.xmlpull.v1.XmlPullParser;

/**
 * @version 1.0
 * @author AlAg
 */
public class ResultImpl
    extends DefaultSessionProcessor
    implements Result
{

    private String dialbackValue;


    //-------------------------------------------------------------------------
    public void process( final IMSession session, final Object context )
        throws Exception
    {

        IMServerSession serverSession = (IMServerSession) session;

        XmlPullParser xpp = session.getXmlPullParser();
        //String to = xpp.getAttributeValue( "", "to" );
        String from = xpp.getAttributeValue( "", "from" );
        String type = xpp.getAttributeValue( "", "type" );

        if ( from != null && from.length() > 0 )
        {
            serverSession.setRemoteHostname( from );
        }

        super.process( session, context );

        //String id = xpp.getAttributeValue( "", "id" );

        if ( "valid".equals( type ) )
        {
            getLogger().debug( "Result valid from " + from );
            serverSession.setDialbackValid( true );
            synchronized ( session )
            {
                session.notifyAll();
            }
        }
        else if ( dialbackValue != null )
        {
            getLogger().debug( "Verify " + from + " dialback " + dialbackValue );
            if ( serverSession.getTwinSession() == null )
            {
                session.getRouter().getS2SConnectorManager().verifyRemoteHost( from, dialbackValue,
                                                                               Long.toString( session.getId() ),
                                                                               serverSession );
            }
        }

    }

    //-------------------------------------------------------------------------
    public void processText( final IMSession session, final Object context )
        throws Exception
    {
        dialbackValue = session.getXmlPullParser().getText().trim();
    }

}
