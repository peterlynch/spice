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
package net.java.dev.openim.jabber.iq.auth;

import net.java.dev.openim.data.UsersManager;
import net.java.dev.openim.DefaultSessionProcessor;
import net.java.dev.openim.ServerParameters;
import net.java.dev.openim.IMRouter;
import net.java.dev.openim.data.Account;
import net.java.dev.openim.data.jabber.IMIq;
import net.java.dev.openim.data.jabber.User;
import net.java.dev.openim.data.storage.AccountRepositoryHolder;
import net.java.dev.openim.session.IMClientSession;
import net.java.dev.openim.session.IMSession;

/**
 * @version 1.5
 * @author AlAg
 */
public class QueryImpl
    extends DefaultSessionProcessor
    implements Query
{

    private ServerParameters serverParameters;
    private UsersManager usersManager;
    private AccountRepositoryHolder accountHolder;

    //-------------------------------------------------------------------------
    public void process( final IMSession session, final Object context )
        throws Exception
    {

        IMClientSession clientSession = (IMClientSession) session;

        String iqId = ( (IMIq) context ).getId();
        String type = ( (IMIq) context ).getType();

        User user = usersManager.getNewUser();
        clientSession.setUser( user );
        user.setHostname( serverParameters.getHostName() );

        // GET
        if ( IMIq.TYPE_GET.equals( type ) )
        {
            super.process( session, context );
            String s = null;

            Account account = accountHolder.getAccount( user.getName() );
            if ( account == null )
            { // user does not exists
                s = "<iq type='" + IMIq.TYPE_ERROR + "' id='" + iqId + "'>"
                    + "<query xmlns='jabber:iq:auth'><username>" + user.getName() + "</username></query>"
                    + "<error code='401'>Unauthorized</error>" + "</iq>";
            }

            else
            { // user exists
                s = "<iq type='" + IMIq.TYPE_RESULT + "' id='" + iqId + "' from='" + serverParameters.getHostName()
                    + "'>" + "<query xmlns='jabber:iq:auth'>" + "<username>" + user.getName() + "</username>";

                if ( user.isAuthenticationTypeSupported( Account.AUTH_TYPE_PLAIN ) )
                {
                    s += "<password/>";
                }
                if ( user.isAuthenticationTypeSupported( Account.AUTH_TYPE_DIGEST ) )
                {
                    s += "<digest/>";
                }
                s += "<resource/></query></iq>";
            }

            session.writeOutputStream( s );
        }

        // SET
        else if ( IMIq.TYPE_SET.equals( type ) )
        {
            super.process( session, context );

            try
            {
                user.authenticate( Long.toString( session.getId() ) );
                IMRouter router = session.getRouter();
                router.registerSession( clientSession );

                String s = "<iq type='" + IMIq.TYPE_RESULT + "' id='" + iqId + "' />";
                session.writeOutputStream( s );

                // get all enqued message
                //router.deliverQueueMessage( session, user.getName() );

            }

            catch ( Exception e )
            {
                getLogger().debug( e.getMessage(), e );
                String s = "<iq type='" + IMIq.TYPE_ERROR + "' id='" + iqId + "' />";
                session.writeOutputStream( s );
            }
        }

    }

}
