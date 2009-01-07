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
package net.java.dev.openim.data.jabber;

import net.java.dev.openim.data.Deferrable;
import net.java.dev.openim.data.jabber.IMPresence;
import net.java.dev.openim.tools.XMLToString;

/**
 * @version 1.5
 * @author AlAg
 */
public class DeferrableIMPresenceImpl
    implements IMPresence, Deferrable, java.io.Serializable
{

    private static final long serialVersionUID = 15L;

    private String to;
    private String from;
    private String type;
    private String show;
    private String priority;
    private String status;
    private String error;
    private Integer errorCode;

    public DeferrableIMPresenceImpl()
    {
    }

    public DeferrableIMPresenceImpl( IMPresence presence )
    {

        this.to = presence.getTo();
        this.from = presence.getFrom();

        this.type = presence.getType();
        this.show = presence.getShow();
        this.priority = presence.getPriority();
        this.status = presence.getStatus();

    }

    public final void setTo( String to )
    {
        this.to = to;
    }

    public final String getTo()
    {
        return to;
    }

    public final void setFrom( String from )
    {
        this.from = from;
    }

    public final String getFrom()
    {
        return from;
    }

    public final void setType( String type )
    {
        this.type = type;
    }

    public final String getType()
    {
        return type;
    }

    public final void setShow( String show )
    {
        this.show = show;
    }

    public final String getShow()
    {
        return show;
    }

    public final void setPriority( String priority )
    {
        this.priority = priority;
    }

    public final String getPriority()
    {
        return priority;
    }

    public final void setStatus( String status )
    {
        this.status = status;
    }

    public final String getStatus()
    {
        return status;
    }

    public final void setError( String error )
    {
        this.error = error;
    }

    public void setErrorCode( int errorCode )
    {
        this.errorCode = new Integer( errorCode );
    }

    public Object clone()
    {
        DeferrableIMPresenceImpl clone = new DeferrableIMPresenceImpl();

        clone.to = to;
        clone.from = from;
        clone.type = type;
        clone.show = show;
        clone.priority = priority;
        clone.status = status;
        clone.error = error;
        clone.errorCode = errorCode;

        return clone;
    }

    public String toString()
    {

        XMLToString presence = new XMLToString( "presence" );
        presence.addFilledAttribut( "to", to );
        presence.addFilledAttribut( "from", from );
        presence.addFilledAttribut( "type", type );

        if ( priority != null )
        {
            XMLToString priority = new XMLToString( "priority" );
            priority.addTextNode( this.priority );
            presence.addElement( priority );
        }

        if ( show != null )
        {
            XMLToString show = new XMLToString( "show" );
            show.addTextNode( this.show );
            presence.addElement( show );
        }

        if ( status != null )
        {
            XMLToString status = new XMLToString( "status" );
            status.addTextNode( this.status );
            presence.addElement( status );
        }

        if ( error != null )
        {
            XMLToString error = new XMLToString( "error" );
            error.addTextNode( this.error );
            if ( errorCode != null )
            {
                error.addFilledAttribut( "code", errorCode.toString() );
            }
            presence.addElement( error );
        }

        return presence.toString();
    }

}
