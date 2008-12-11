package org.sonatype.jsecurity.locators.users;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractTestUserLocator implements PlexusUserLocator
{


    public PlexusUser getUser( String userId )
    {
        Set<PlexusUser> users = this.listUsers();
        
        for ( PlexusUser plexusUser : users )
        {
            if( plexusUser.getUserId().equals( userId ))
            {
                return plexusUser;
            }
        }
        
        return null;
    }

    public boolean isPrimary()
    {
        return false;
    }

    public Set<String> listUserIds()
    {   
        Set<String> result = new HashSet<String>();
        for ( PlexusUser plexusUser : this.listUsers() )
        {
            result.add( plexusUser.getUserId() );
        }
        return result;
    }

    public Set<PlexusUser> searchUserById( String userId )
    {
        // TODO: not sure what this should actually be... regex?
        
        Set<PlexusUser> result = new HashSet<PlexusUser>();
        for ( PlexusUser plexusUser : this.listUsers() )
        {
            if( plexusUser.getUserId().toLowerCase().startsWith( userId.toLowerCase() ) )
            {
                result.add( plexusUser );
            }
        }
        return result;
    }
    
    protected PlexusRole createFakeRole(String roleId )
    {
        PlexusRole role = new PlexusRole();
        role.setName( roleId );
        role.setRoleId( roleId );
        role.setSource( this.getSource() );
        
        return role;
    }
    
}
