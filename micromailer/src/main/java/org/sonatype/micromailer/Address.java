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
package org.sonatype.micromailer;

import java.io.UnsupportedEncodingException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.codehaus.plexus.util.StringUtils;

/**
 * The address used in To, From, Sender, etc. fields. The mailbox with optional "personal" name of the mailbox.
 * 
 * @author cstamas
 */
public class Address
{
    private String mailAddress;

    private String personal;

    public Address( String mailAddress )
    {
        super();

        if ( StringUtils.isEmpty( mailAddress ) )
        {
            throw new IllegalArgumentException( "The mailAddress cannot be null!" );
        }

        this.mailAddress = mailAddress;

        this.personal = null;
    }

    public Address( String mailAddress, String personal )
    {
        this( mailAddress );

        this.personal = personal;
    }

    public String getMailAddress()
    {
        return mailAddress;
    }

    public void setMailAddress( String mailAddress )
    {
        this.mailAddress = mailAddress;
    }

    public String getPersonal()
    {
        return personal;
    }

    public void setPersonal( String personal )
    {
        this.personal = personal;
    }

    public InternetAddress getInternetAddress( String encoding )
        throws AddressException,
            UnsupportedEncodingException
    {
        InternetAddress adr = new InternetAddress( getMailAddress(), getPersonal(), encoding );

        adr.validate();

        return adr;
    }

    public String toString()
    {
        if ( StringUtils.isEmpty( getPersonal() ) )
        {
            return "<" + getMailAddress() + ">";
        }
        else
        {
            return "\"" + getPersonal() + "\" <" + getMailAddress() + ">";
        }
    }
}
