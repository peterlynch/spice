package org.sonatype.jsecurity.realms.tools.dao;

import java.util.List;

import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;

public class SecurityPrivilege
    extends CPrivilege
        implements SecurityItem
{
    boolean readOnly;
    
    public SecurityPrivilege()
    {
    }
    
    public SecurityPrivilege( CPrivilege privilege )
    {
        setDescription( privilege.getDescription() );
        setId( privilege.getId() );
        setName( privilege.getName() );
        setType( privilege.getType() );
        
        if ( privilege.getProperties() != null )
        {
            for ( CProperty prop : ( List<CProperty> ) privilege.getProperties() )
            {
                addProperty( new SecurityProperty( prop ) );
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
