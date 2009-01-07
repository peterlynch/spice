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
package net.java.dev.openim.jabber.iq.browse;





import net.java.dev.openim.DefaultSessionProcessor;
import net.java.dev.openim.ServerParameters;

import net.java.dev.openim.data.jabber.IMIq;
import net.java.dev.openim.session.IMClientSession;
import net.java.dev.openim.session.IMSession;






/**
 *  @version 1.5
 * @author AlAg
 */
public class QueryImpl extends DefaultSessionProcessor implements Query {

    private ServerParameters serverParameters;    

    //-------------------------------------------------------------------------
    public void process( final IMSession session, final Object context ) throws Exception{
        
        IMClientSession clientSession = (IMClientSession)session;
        String type = ((IMIq)context).getType();

        // GET
        if( IMIq.TYPE_GET.equals( type ) ){
            get( clientSession, context );
        }
        else if( IMIq.TYPE_SET.equals( type ) ){
            set( clientSession, context );
        }        
    }
    
    
    //-------------------------------------------------------------------------
    private void get( final IMClientSession session, Object context ) throws Exception {
        
        
        //final XmlPullParser xpp = session.getXmlPullParser();

        String iqId = ((IMIq)context).getId();
        
        String s = "<iq type='result'";
        s += " from='"+serverParameters.getHostName()+"'";
        s += " to='"+session.getUser().getJIDAndRessource()+"'";
        s += " id='"+iqId+"'";
        s += ">";
        s += "<service jid='"+serverParameters.getHostName()+"' name='OpenIM Server' type='jabber' xmlns='jabber:iq:browse'>"; 
        s += "<item category='service' jid='"+serverParameters.getHostName()+"' name='OpenIM User Directory' type='jud'>";
        s += "<ns>jabber:iq:register</ns>";
        s += "</item>";
        s += "</service></iq>";

        session.writeOutputStream( s );
    }
    
    //-------------------------------------------------------------------------
    private void set( final IMClientSession session, final Object context ) throws Exception {

        //final XmlPullParser xpp = session.getXmlPullParser();
        getLogger().warn( "Skipping jabber:iq:browse:query set" );  
    }
    
}

