package org.sonatype.webdav.security;

/**
 * Created by IntelliJ IDEA.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class SimpleUser
    implements User
{
    public static final User ANONYMOUS_USER = new SimpleUser(ANONYMOUS);
    
    private String username;

    private String email;

    public SimpleUser( String username )
    {
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

    public boolean isAnonymous()
    {
        return ANONYMOUS.equals( getUsername() );
    }
}
