package org.sonatype.micromailer;

import javax.mail.Authenticator;

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

}
