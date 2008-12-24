package org.sonatype.jsecurity.locators.users;

import java.util.Set;
import java.util.TreeSet;

public abstract class AbstractTestRoleLocator implements PlexusRoleLocator
{

    public Set<PlexusRole> listRoles()
    {
        Set<String> ids = this.listRoleIds();
        
        Set<PlexusRole> roles = new TreeSet<PlexusRole>();
        
        for ( String id : ids )
        {
            PlexusRole role = new PlexusRole();
            role.setName( id );
            role.setRoleId( id );
            role.setSource( this.getSource() );
            roles.add( role );
        }
        
        return roles;
    }
    
}
