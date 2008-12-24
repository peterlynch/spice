package org.codehaus.plexus.swizzle.jira.authentication;

public class DefaultAuthenticationSource
    implements AuthenticationSource
{
    private String login;
    private String password;
        
    public DefaultAuthenticationSource( String login, String password )
    {
        this.login = login;
        this.password = password;
    }

    public String getLogin()
    {
        return login;
    }

    public String getPassword()
    {
        return password;
    }
}
