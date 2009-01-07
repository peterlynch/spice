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

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Session;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.micromailer.imp.DefaultAuthenticator;

public class EmailerConfiguration
{
    private boolean storeMails = false;

    private boolean sendMails = true;

    private String mailHost = null;

    private int mailPort = 25;

    private String bounceAddress = null;

    private String username = null;

    private String password = null;

    private boolean isTls = false;

    private boolean isSsl = false;

    private boolean debug = false;

    // ==

    private static final String MAIL_HOST = "mail.smtp.host";

    private static final String MAIL_PORT = "mail.smtp.port";

    private static final String MAIL_SMTP_FROM = "mail.smtp.from";

    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";

    private static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";

    private static final String MAIL_TRANSPORT_TLS = "mail.smtp.starttls.enable";

    private static final String MAIL_SMTP_SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback";

    private static final String MAIL_SMTP_SOCKET_FACTORY_CLASS = "mail.smtp.socketFactory.class";

    private static final String MAIL_SMTP_SOCKET_FACTORY_PORT = "mail.smtp.socketFactory.port";

    private static final String MAIL_DEBUG = "mail.debug";

    private transient Session session;

    public boolean isStoreMails()
    {
        return storeMails;
    }

    public void setStoreMails( boolean storeMails )
    {
        this.storeMails = storeMails;
    }

    public boolean isSendMails()
    {
        return sendMails;
    }

    public void setSendMails( boolean sendMails )
    {
        this.sendMails = sendMails;
    }

    public String getMailHost()
    {
        return mailHost;
    }

    public void setMailHost( String mailHost )
    {
        this.mailHost = mailHost;
    }

    public int getMailPort()
    {
        return mailPort;
    }

    public void setMailPort( int mailPort )
    {
        this.mailPort = mailPort;
    }

    public String getBounceAddress()
    {
        return bounceAddress;
    }

    public void setBounceAddress( String bounceAddress )
    {
        this.bounceAddress = bounceAddress;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public boolean isTls()
    {
        return isTls;
    }

    public void setTls( boolean isTls )
    {
        this.isTls = isTls;
    }

    public boolean isSsl()
    {
        return isSsl;
    }

    public void setSsl( boolean isSsl )
    {
        if ( isSsl && getMailPort() == 25 )
        {
            // the port is not set, set it automatically as convenience, user can override it
            setMailPort( 465 );
        }

        this.isSsl = isSsl;
    }

    public boolean isDebug()
    {
        return debug;
    }

    public void setDebug( boolean debug )
    {
        this.debug = debug;
    }

    public Authenticator getAuthenticator()
    {
        if ( getUsername() != null )
        {
            return new DefaultAuthenticator( getUsername(), getPassword() );
        }
        else
        {
            return null;
        }
    }

    // ==
    // Session factory

    public Session getSession()
        throws MailCompositionMessagingException
    {
        if ( this.session == null )
        {
            Properties properties = new Properties( System.getProperties() );

            properties.setProperty( MAIL_TRANSPORT_PROTOCOL, "smtp" );

            if ( StringUtils.isEmpty( getMailHost() ) )
            {
                setMailHost( properties.getProperty( MAIL_HOST ) );
            }

            if ( StringUtils.isEmpty( getMailHost() ) )
            {
                throw new MailCompositionMessagingException( "Cannot find valid hostname for mail session" );
            }

            properties.setProperty( MAIL_PORT, String.valueOf( getMailPort() ) );
            properties.setProperty( MAIL_HOST, getMailHost() );
            properties.setProperty( MAIL_DEBUG, String.valueOf( isDebug() ) );

            Authenticator authenticator = getAuthenticator();

            if ( authenticator != null )
            {
                properties.setProperty( MAIL_SMTP_AUTH, "true" );
                properties.setProperty( MAIL_TRANSPORT_TLS, isTls() ? "true" : "false" );

                if ( isTls() )
                {
                    if ( isDebug() )
                    {
                        properties.setProperty( MAIL_SMTP_SOCKET_FACTORY_FALLBACK, "false" );
                        java.security.Security.setProperty(
                            "ssl.SocketFactory.provider",
                            "org.sonatype.micromailer.imp.DebugTLSSocketFactory" );
                    }
                }
            }

            if ( isSsl() )
            {
                properties.setProperty( MAIL_SMTP_SOCKET_FACTORY_PORT, String.valueOf( getMailPort() ) );
                properties.setProperty( MAIL_SMTP_SOCKET_FACTORY_FALLBACK, "false" );
                if ( isDebug() )
                {
                    java.security.Security.setProperty(
                        "ssl.SocketFactory.provider",
                        "org.sonatype.micromailer.imp.DebugSSLSocketFactory" );
                }
                else
                {
                    properties.setProperty( MAIL_SMTP_SOCKET_FACTORY_CLASS, "javax.net.ssl.SSLSocketFactory" );
                }
            }

            if ( getBounceAddress() != null )
            {
                properties.setProperty( MAIL_SMTP_FROM, getBounceAddress() );
            }

            this.session = Session.getInstance( properties, authenticator );
        }

        return this.session;
    }
}
