package org.sonatype.jsecurity.locators.users;

import java.util.HashSet;
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
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( source == null ) ? 0 : source.hashCode() );
        result = prime * result + ( ( userId == null ) ? 0 : userId.hashCode() );
        return result;
    }
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        final PlexusUser other = (PlexusUser) obj;
        if ( source == null )
        {
            if ( other.source != null )
                return false;
        }
        else if ( !source.equals( other.source ) )
            return false;
        if ( userId == null )
        {
            if ( other.userId != null )
                return false;
        }
        else if ( !userId.equals( other.userId ) )
            return false;
        return true;
    }
}
