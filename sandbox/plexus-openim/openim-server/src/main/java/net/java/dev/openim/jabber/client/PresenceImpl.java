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
package net.java.dev.openim.jabber.client;

import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import net.java.dev.openim.DefaultSessionProcessor;
import net.java.dev.openim.IMRouter;
import net.java.dev.openim.IMPresenceHolder;
import net.java.dev.openim.SubscriptionManager;
import net.java.dev.openim.data.jabber.IMPresence;
import net.java.dev.openim.data.jabber.IMPresenceImpl;
import net.java.dev.openim.data.jabber.IMRosterItem;
import net.java.dev.openim.session.IMClientSession;
import net.java.dev.openim.session.IMSession;

/**
 * @version 1.5
 * @author AlAg
 */
public class PresenceImpl
    extends DefaultSessionProcessor
    implements Presence
{

    // Requirements
    private IMPresenceHolder presenceHolder;
    private SubscriptionManager subscriptionManager;

    //-------------------------------------------------------------------------
    public void process( final IMSession session, final Object context )
        throws Exception
    {

        IMClientSession clientSession = (IMClientSession) session;

        XmlPullParser xpp = session.getXmlPullParser();

        String type = xpp.getAttributeValue( "", "type" );
        String to = xpp.getAttributeValue( "", "to" );

        String from = xpp.getAttributeValue( "", "from" );

        if ( from == null || from.length() == 0 )
        {
            from = clientSession.getUser().getJIDAndRessource();
        }

        IMPresence presence = new IMPresenceImpl();
        presence.setType( type );
        presence.setFrom( from );

        super.process( session, presence );

        clientSession.setPresence( presence );

        if ( type == null || type.length() == 0 || IMPresence.TYPE_AVAILABLE.equals( type )
            || IMPresence.TYPE_UNAVAILABLE.equals( type ) )
        {
            presenceHolder.setPresence( from, presence );
        }

        getLogger().debug( "Got presence (to " + to + ") " + presence );

        IMRouter router = session.getRouter();
        if ( to == null || to.length() == 0 || to.equals( "null" ) )
        {
            // emit presence associated to roster friends
            List rosterList = clientSession.getUser().getRosterItemList();
            if ( rosterList != null )
            {
                for ( int i = 0, l = rosterList.size(); i < l; i++ )
                {
                    IMRosterItem item = (IMRosterItem) rosterList.get( i );
                    IMPresence localPresence = (IMPresence) presence.clone();
                    localPresence.setTo( item.getJID() );
                    router.route( session, localPresence );
                }
            }
        }

        else
        {
            IMPresence localPresence = (IMPresence) presence.clone();
            localPresence.setTo( to );

            subscriptionManager.process( session, localPresence );

        }

    }

    // ------------------------------------------------------------------------
}
