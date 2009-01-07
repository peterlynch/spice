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

import org.xmlpull.v1.XmlPullParser;

import net.java.dev.openim.DefaultSessionProcessor;
import net.java.dev.openim.data.jabber.IMIq;
import net.java.dev.openim.session.IMClientSession;
import net.java.dev.openim.session.IMSession;

/**
 * @version 1.5
 * @author AlAg
 */
public class IqImpl
    extends DefaultSessionProcessor
    implements Iq
{

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

        IMIq iq = new IMIq();
        iq.setId( xpp.getAttributeValue( "", "id" ) );
        iq.setType( xpp.getAttributeValue( "", "type" ) );
        iq.setTo( xpp.getAttributeValue( "", "to" ) );
        iq.setFrom( xpp.getAttributeValue( "", "from" ) );
        getLogger().debug( "Got IQ " + iq );

        super.process( session, iq );

        if ( IMIq.TYPE_RESULT.equals( iq.getType() ) )
        {
            iq.setFrom( ( (IMClientSession) session ).getUser().getJIDAndRessource() );
            session.getRouter().route( session, iq );
        }

    }

}
