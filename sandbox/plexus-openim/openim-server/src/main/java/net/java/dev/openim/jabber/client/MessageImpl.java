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
package net.java.dev.openim.jabber.client;

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

        String type = xpp.getAttributeValue( "", "type" );

        IMMessage message = new IMMessage();

        String to = xpp.getAttributeValue( "", "to" );
        String from = xpp.getAttributeValue( "", "from" );

        if ( session instanceof IMClientSession )
        {
            if ( from == null || from.length() == 0 )
            {
                from = ( (IMClientSession) session ).getUser().getJIDAndRessource();
            }
            // Handle ping your self
            if ( to == null || to.length() == 0 )
            {
                to = ( (IMClientSession) session ).getUser().getJIDAndRessource();
            } // end of if ()

        }

        message.setTo( to );
        message.setFrom( from );
        message.setType( type );

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
