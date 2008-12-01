package org.sonatype.jsecurity.locators.users;

public class PlexusRole
    implements Comparable<PlexusRole>
{
    private String roleId;
    private String name;
    private String source;   
    
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
    public boolean equals( Object obj )
    {
        if ( obj == null
            || ( !PlexusRole.class.isAssignableFrom( obj.getClass() ) ) )
        {
            return false;
        }
        
        return getRoleId().equals( ( ( PlexusRole ) obj ).getRoleId() );
    }
}
