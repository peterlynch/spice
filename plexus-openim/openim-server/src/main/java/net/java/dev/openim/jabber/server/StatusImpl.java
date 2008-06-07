/*
 * BSD License http://open-im.net/bsd-license.html
 * Copyright (c) 2003, OpenIM Project http://open-im.net
 * All rights reserved.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenIM project. For more
 * information on the OpenIM project, please see
 * http://open-im.net/
 */
package net.java.dev.openim.jabber.server;

import net.java.dev.openim.DefaultSessionProcessor;
import net.java.dev.openim.data.jabber.IMPresence;
import net.java.dev.openim.session.IMSession;

/**
 * @version 1.5
 * @author AlAg
 */
public class StatusImpl
    extends DefaultSessionProcessor
    implements Status
{

    public void processText( final IMSession session, final Object context )
        throws Exception
    {
        ( (IMPresence) context ).setStatus( session.getXmlPullParser().getText().trim() );
    }

}
