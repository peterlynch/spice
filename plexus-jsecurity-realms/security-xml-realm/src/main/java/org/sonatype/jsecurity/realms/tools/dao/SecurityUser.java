package org.sonatype.jsecurity.realms.tools.dao;

import java.util.List;

import org.sonatype.jsecurity.model.CUser;

public class SecurityUser
    extends CUser
        implements SecurityItem
{
    boolean readOnly;
    
    public SecurityUser()
    {
    }
    
    public SecurityUser( CUser user )
    {
        setEmail( user.getEmail() );
        setName( user.getName() );
        setPassword( user.getPassword() );
        setStatus( user.getStatus() );
        setId( user.getId() );
     
        if ( user.getRoles() != null )
        {
            for ( String roleId : ( List<String> ) user.getRoles() )
            {
                addRole( roleId );
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