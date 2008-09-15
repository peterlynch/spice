package org.sonatype.jsecurity.realms.tools;

import java.util.List;

import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;

/**
 * This class should really be doing an uber generic clone, but it is this for now
 */
public class ObjectCloner
{
    @SuppressWarnings("unchecked")
    protected static final CUser clone( CUser user )
    {
        if ( user == null )
        {
            return null;
        }
        
        CUser cloned = new CUser();
        
        cloned.setEmail( user.getEmail() );
        cloned.setName( user.getName() );
        cloned.setPassword( user.getPassword() );
        cloned.setStatus( user.getStatus() );
        cloned.setId( user.getId() );
     
        if ( user.getRoles() != null )
        {
            for ( String roleId : ( List<String> ) user.getRoles() )
            {
                cloned.addRole( roleId );
            }
        }
        
        return cloned;
    }
    
    @SuppressWarnings("unchecked")
    protected static final CRole clone( CRole role )
    {
        if ( role == null )
        {
            return null;
        }
        
        CRole cloned = new CRole();
        
        cloned.setDescription( role.getDescription() );
        cloned.setId( role.getId() );
        cloned.setName( role.getName() );
        cloned.setSessionTimeout( role.getSessionTimeout() );
        
        if ( role.getRoles() != null )
        {
            for ( String roleId : ( List<String> ) role.getRoles() )
            {
                cloned.addRole( roleId );
            }
        }
        
        if ( role.getPrivileges() != null )
        {
            for ( String privilegeId : ( List<String> ) role.getPrivileges() )
            {
                cloned.addPrivilege( privilegeId );
            }
        }
        
        return cloned;
    }
    
    @SuppressWarnings("unchecked")
    protected static final CPrivilege clone( CPrivilege privilege )
    {
        if ( privilege == null )
        {
            return privilege;
        }
        
        CPrivilege cloned = new CPrivilege();
        
        cloned.setDescription( privilege.getDescription() );
        cloned.setId( privilege.getId() );
        cloned.setName( privilege.getName() );
        cloned.setType( privilege.getType() );
        
        if ( privilege.getProperties() != null )
        {
            for ( CProperty prop : ( List<CProperty> ) privilege.getProperties() )
            {
                cloned.addProperty( clone( prop ) );
            }
        }
        
        return cloned;
    }
    
    protected static final CProperty clone( CProperty property )
    {
        if ( property == null )
        {
            return null;
        }
        
        CProperty cloned = new CProperty();
        
        cloned.setKey( property.getKey() );
        cloned.setValue( property.getValue() );
        
        return cloned;
    }
}
