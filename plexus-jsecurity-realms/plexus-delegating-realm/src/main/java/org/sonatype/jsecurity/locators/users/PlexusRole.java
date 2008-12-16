package org.sonatype.jsecurity.locators.users;

public class PlexusRole
    implements Comparable<PlexusRole>
{
    private String roleId;
    private String name;
    private String source;   
    
    public PlexusRole()
    {
        
    }
    
    public PlexusRole( String roleId, String name, String source )
    {
        this.roleId = roleId;
        this.name = name;
        this.source = source;
    }

    public String getRoleId()
    {
        return roleId;
    }

    public void setRoleId( String roleId )
    {
        this.roleId = roleId;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource( String source )
    {
        this.source = source;
    }

    public int compareTo( PlexusRole o )
    {
        if ( o == null )
            return 1;
        
        return getRoleId().compareTo( o.getRoleId() );
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( roleId == null ) ? 0 : roleId.hashCode() );
        result = prime * result + ( ( source == null ) ? 0 : source.hashCode() );
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
        final PlexusRole other = (PlexusRole) obj;
        if ( roleId == null )
        {
            if ( other.roleId != null )
                return false;
        }
        else if ( !roleId.equals( other.roleId ) )
            return false;
        if ( source == null )
        {
            if ( other.source != null )
                return false;
        }
        else if ( !source.equals( other.source ) )
            return false;
        return true;
    }
}
