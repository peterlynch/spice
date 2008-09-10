package org.sonatype.micromailer.imp;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class DefaultAuthenticator
    extends Authenticator
{
    private final String username;

    private final String password;

    public DefaultAuthenticator( String username, String password )
    {
        super();

        this.username = username;

        this.password = password;
    }

    protected PasswordAuthentication getPasswordAuthentication()
    {
        return new PasswordAuthentication( username, password );
    }
}
