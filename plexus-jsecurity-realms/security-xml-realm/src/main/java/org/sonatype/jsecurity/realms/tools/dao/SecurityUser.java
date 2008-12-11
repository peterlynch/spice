package org.sonatype.jsecurity.realms.tools.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonatype.jsecurity.model.CUser;

public class SecurityUser
    extends CUser
        implements SecurityItem
{
    boolean readOnly;
    
    private Set<String> roles = new HashSet<String>();
    
    public SecurityUser()
    {
    }
    
    public SecurityUser( CUser user )
    {
        this( user, false );
    }
    
    public SecurityUser( CUser user, boolean readOnly )
    {
        this( user, false, null );
    }
    
    public SecurityUser( CUser user, boolean readOnly, List<String> roles )
    {
        setEmail( user.getEmail() );
        setName( user.getName() );
        setPassword( user.getPassword() );
        setStatus( user.getStatus() );
        setId( user.getId() );
        setReadOnly( readOnly );
     
        if ( roles != null )
        {
            for ( String roleId : roles )
            {
                addRole( roleId );
            }
        }
    }
    
    public Set<String> getRoles()
    {
        return roles;
    }

    public void addRole( String roleId)
    {
        this.roles.add( roleId );
    }
    
    public void setRoles( Set<String> roles )
    {
        this.roles = roles;
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