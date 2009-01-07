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
package net.java.dev.openim.jabber.server;

import java.util.Collection;
import java.util.Iterator;


import org.xmlpull.v1.XmlPullParser;

import net.java.dev.openim.IMPresenceHolder;
import net.java.dev.openim.DefaultSessionProcessor;
import net.java.dev.openim.data.jabber.IMPresence;
import net.java.dev.openim.data.jabber.IMPresenceImpl;
import net.java.dev.openim.session.IMSession;
import net.java.dev.openim.SubscriptionManager;

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

        XmlPullParser xpp = session.getXmlPullParser();
        String type = xpp.getAttributeValue( "", "type" );
        String to = xpp.getAttributeValue( "", "to" );
        String from = xpp.getAttributeValue( "", "from" );

        IMPresence presence = new IMPresenceImpl();
        presence.setType( type );
        presence.setTo( to );
        presence.setFrom( from );

        super.process( session, presence );

        getLogger().debug( "Got presence (to " + to + "): " + presence );

        if ( to == null || to.length() == 0 )
        {
            // emit presence associated to roster friends?
            getLogger().debug( "To is not specified, what should we do?" );
        }
        else
        {
            String presenceType = presence.getType();
            if ( IMPresence.TYPE_PROBE.equals( presenceType ) )
            {
                getLogger().info( "Probed from " + from + " to " + to );
                // check availability
                Collection col = presenceHolder.getPresence( to );
                if ( col != null && !col.isEmpty() )
                {
                    Iterator iter = col.iterator();
                    while ( iter.hasNext() )
                    {
                        IMPresence localPresence = (IMPresence) iter.next();
                        localPresence = (IMPresence) localPresence.clone();
                        localPresence.setTo( from );
                        session.getRouter().route( session, localPresence );
                    }
                }
                // unavailable
                else
                {
                    IMPresence localPresence = new IMPresenceImpl();
                    localPresence.setType( IMPresence.TYPE_UNAVAILABLE );
                    localPresence.setFrom( to );
                    localPresence.setTo( from );
                    session.getRouter().route( session, localPresence );
                }

            }

            else
            {
                IMPresence localPresence = (IMPresence) presence.clone();
                subscriptionManager.process( session, localPresence );
            }

        } // if to null

    }

}
