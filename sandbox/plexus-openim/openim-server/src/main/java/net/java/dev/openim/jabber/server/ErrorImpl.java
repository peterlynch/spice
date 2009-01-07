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

import net.java.dev.openim.DefaultSessionProcessor;
import net.java.dev.openim.data.jabber.IMPresence;
import net.java.dev.openim.session.IMSession;

/**
 * @version 1.5
 * @author AlAg
 */
public class ErrorImpl
    extends DefaultSessionProcessor
    implements Error
{

    public void processText( final IMSession session, final Object context )
        throws Exception
    {
        String errorCodeStr = session.getXmlPullParser().getAttributeValue( "", "code" );
        if ( errorCodeStr != null && errorCodeStr.length() > 0 )
        {
            ( (IMPresence) context ).setErrorCode( Integer.parseInt( errorCodeStr ) );
        }
        ( (IMPresence) context ).setError( session.getXmlPullParser().getText().trim() );
    }

}
