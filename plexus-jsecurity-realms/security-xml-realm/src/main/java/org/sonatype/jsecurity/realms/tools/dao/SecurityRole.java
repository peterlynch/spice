package org.sonatype.jsecurity.realms.tools.dao;

import java.util.List;

import org.sonatype.jsecurity.model.CRole;

public class SecurityRole
    extends CRole
        implements SecurityItem
{
    boolean readOnly;
    
    public SecurityRole()
    {
    }
    
    public SecurityRole( CRole role )
    {
        setDescription( role.getDescription() );
        setId( role.getId() );
        setName( role.getName() );
        setSessionTimeout( role.getSessionTimeout() );
        
        if ( role.getRoles() != null )
        {
            for ( String roleId : ( List<String> ) role.getRoles() )
            {
                addRole( roleId );
            }
        }
        
        if ( role.getPrivileges() != null )
        {
            for ( String privilegeId : ( List<String> ) role.getPrivileges() )
            {
                addPrivilege( privilegeId );
            }
        }
    }
    
    public boolean isReadOnly()
    {
        return readOnly;
    }
    
    public void setReadOnly( boolean readOnly )
    {
        this.readOnly = readOnly;
    }
}