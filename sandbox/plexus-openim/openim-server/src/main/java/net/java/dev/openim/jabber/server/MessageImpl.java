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
package net.java.dev.openim.jabber.server;

import org.xmlpull.v1.XmlPullParser;

import net.java.dev.openim.DefaultSessionProcessor;

import net.java.dev.openim.IMRouter;
import net.java.dev.openim.data.jabber.IMMessage;
import net.java.dev.openim.session.IMClientSession;
import net.java.dev.openim.session.IMSession;

/**
 * @version 1.5
 * @author AlAg
 */
public class MessageImpl
    extends DefaultSessionProcessor
    implements Message
{

    //-------------------------------------------------------------------------

    public void process( final IMSession session, final Object context )
        throws Exception
    {

        XmlPullParser xpp = session.getXmlPullParser();

        for ( int i = 0, l = xpp.getAttributeCount(); i < l; i++ )
        {
            getLogger().debug(
                               "Attribut ns: " + xpp.getAttributeNamespace( i ) + " name: " + xpp.getAttributeName( i )
                                   + " value: " + xpp.getAttributeValue( i ) );
        }

        IMMessage message = new IMMessage();
        message.setTo( xpp.getAttributeValue( "", "to" ) );
        message.setType( xpp.getAttributeValue( "", "type" ) );

        if ( session.getConnectionType() == IMSession.C2S_CONNECTION )
        {
            message.setFrom( ( (IMClientSession) session ).getUser().getJIDAndRessource() );
        }
        else
        {
            message.setFrom( xpp.getAttributeValue( "", "from" ) );
        }

        super.process( session, message );

        IMRouter router = session.getRouter();
        router.route( session, message );

        /*        
         String iqMsg = session.getMessageData().getId();
         
         String s = "<iq type='"+IqData.TYPE_RESULT+"' id='"+iqId+"'>"
         +"<query xmlns='jabber:iq:roster'>"
         +"<item jid='romeo@montague.net' name='Romeo' subscription='both'/>"
         +"</query></iq>";

         
         session.writeOutputStream( s );            
         */
    }

}
