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
package net.java.dev.openim.jabber.iq.jprivate;

import net.java.dev.openim.DefaultSessionProcessor;

import net.java.dev.openim.data.jabber.IMIq;
import net.java.dev.openim.data.storage.PrivateRepositoryHolder;
import net.java.dev.openim.session.IMClientSession;
import net.java.dev.openim.session.IMSession;

import org.xmlpull.v1.XmlPullParser;

/**
 * @version 1.5
 * @author AlAg
 */
public class QueryImpl
    extends DefaultSessionProcessor
    implements Query
{

    private PrivateRepositoryHolder privateRepository;

    //-------------------------------------------------------------------------

    public void process( final IMSession session, final Object context )
        throws Exception
    {

        IMClientSession clientSession = (IMClientSession) session;
        String type = ( (IMIq) context ).getType();

        // GET
        if ( IMIq.TYPE_GET.equals( type ) )
        {
            get( clientSession, context );
        }
        else if ( IMIq.TYPE_SET.equals( type ) )
        {
            set( clientSession, context );
        }
    }

    //-------------------------------------------------------------------------
    private void get( final IMClientSession session, Object context )
        throws Exception
    {

        final XmlPullParser xpp = session.getXmlPullParser();
        //final String privateName = xpp.getNamespace()+':'+xpp.getName();

        String iqId = ( (IMIq) context ).getId();

        xpp.next();
        String privateKey = xpp.getNamespace() + ':' + xpp.getName();
        String data = privateRepository.getData( session.getUser().getName(), privateKey );
        if ( data == null )
        {
            data = "<" + xpp.getName() + " xmlns='" + xpp.getNamespace() + "'/>";
        }

        getLogger().debug( "Got data (" + privateKey + "): " + data );
        String s = "<iq type='result'";
        s += " from='" + session.getUser().getJIDAndRessource() + "'";
        s += " to='" + session.getUser().getJIDAndRessource() + "'";
        s += " id='" + iqId + "'>";
        s += "<query xmlns='jabber:iq:private'>";
        s += data;
        s += "</query>";
        s += "</iq>";

        session.writeOutputStream( s );

        skip( xpp );
        /*
         while( !( eventType == XmlPullParser.END_TAG 
         && privateName.equals( xpp.getNamespace()+':'+xpp.getName() ) ) ){
         eventType = xpp.next();
         }
         */
    }

    //-------------------------------------------------------------------------
    private void set( final IMClientSession session, final Object context )
        throws Exception
    {

        final XmlPullParser xpp = session.getXmlPullParser();

        //int eventType = xpp.next();
        String privateKey = xpp.getNamespace() + ':' + xpp.getName();

        String data = serialize( xpp ).toString();

        getLogger().debug( "Got private key " + privateKey + " => data: " + data );
        if ( data != null && data.length() > 0 )
        {
            privateRepository.setData( session.getUser().getName(), privateKey, data );
        }

        String iqId = ( (IMIq) context ).getId();

        String s = "<iq type='result'";
        s += " from='" + session.getUser().getJIDAndRessource() + "'";
        s += " to='" + session.getUser().getJIDAndRessource() + "'";
        s += " id='" + iqId + "'/>";

    }

}
