package org.sonatype.jsecurity.locators.users;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlexusUser
    implements Comparable<PlexusUser>
{
    private String userId;
    private String name;
    private String emailAddress;
    private String source;
    private Set<PlexusRole> roles = new HashSet<PlexusRole>();
    
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
    public String getSource()
    {
        return source;
    }
    public void setSource( String source )
    {
        this.source = source;
    }
    public Set<PlexusRole> getRoles()
    {
        return roles;
    }
    public void setRoles( Set<PlexusRole> roles )
    {
        this.roles = roles;
    }
    public void addRole( PlexusRole role )
    {
        this.roles.add( role );
    }
    public void removeRole( PlexusRole role )
    {
        for ( PlexusRole existingRole : this.roles )
        {
            if ( existingRole.equals( role ) )
            {
                this.roles.remove( existingRole );
                break;
            }
        }
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
