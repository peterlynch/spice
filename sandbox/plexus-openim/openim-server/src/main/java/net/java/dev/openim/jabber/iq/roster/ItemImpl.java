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
package net.java.dev.openim.jabber.iq.roster;

import org.xmlpull.v1.XmlPullParser;

import net.java.dev.openim.data.jabber.IMRosterItem;
import net.java.dev.openim.session.IMSession;
import net.java.dev.openim.DefaultSessionProcessor;

/**
 * @version 1.5
 * @author AlAg
 */
public class ItemImpl
    extends DefaultSessionProcessor
    implements Item
{

    public void process( final IMSession session, final Object context )
        throws Exception
    {
        XmlPullParser xpp = session.getXmlPullParser();
        IMRosterItem rosterItem = (IMRosterItem) context;
        rosterItem.setName( xpp.getAttributeValue( "", "name" ) );
        rosterItem.setJID( xpp.getAttributeValue( "", "jid" ) );
        rosterItem.setSubscription( xpp.getAttributeValue( "", "subscription" ) );
        rosterItem.setAsk( xpp.getAttributeValue( "", "ask" ) );
        super.process( session, context );
    }

}
