package org.sonatype.jsecurity.locators.users;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractTestRoleLocator implements PlexusRoleLocator
{

    public Set<PlexusRole> listRoles()
    {
        Set<String> ids = this.listRoleIds();
        
        Set<PlexusRole> roles = new HashSet<PlexusRole>();
        
        for ( String id : ids )
        {
            PlexusRole role = new PlexusRole();
            roles.add( role );
            role.setName( id );
            role.setRoleId( id );
            role.setSource( this.getSource() );
        }
        
        return roles;
    }
    
}
