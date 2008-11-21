package org.sonatype.jsecurity.locators.users;

public class PlexusUser
    implements Comparable<PlexusUser>
{
    private String userId;
    private String name;
    private String emailAddress;
    public String getUserId()
    {
        return userId;
    }
    public void setUserId( String userId )
    {
        this.userId = userId;
    }
    public String getName()
    {
        return name;
    }
    public void setName( String name )
    {
        this.name = name;
    }
    public String getEmailAddress()
    {
        return emailAddress;
    }
    public void setEmailAddress( String emailAddress )
    {
        this.emailAddress = emailAddress;
    }
    public int compareTo( PlexusUser o )
    {
        if ( o == null )
            return 1;
        
        return getUserId().compareTo( o.getUserId() );
    }
    @Override
    public boolean equals( Object obj )
    {
        if ( obj == null
            || ( !PlexusUser.class.isAssignableFrom( obj.getClass() ) ) )
        {
            return false;
        }
        
        return getUserId().equals( ( ( PlexusUser ) obj ).getUserId() );
    }
}
