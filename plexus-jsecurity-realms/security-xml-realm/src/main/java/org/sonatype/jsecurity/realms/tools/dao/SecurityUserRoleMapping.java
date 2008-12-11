package org.sonatype.jsecurity.realms.tools.dao;

import java.util.List;

import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.model.CUserRoleMapping;

public class SecurityUserRoleMapping
    extends CUserRoleMapping
    implements SecurityItem
{

    public SecurityUserRoleMapping()
    {
    }

    public SecurityUserRoleMapping( CUserRoleMapping mapping )
    {
        this( mapping, false );
    }

    public SecurityUserRoleMapping( CUserRoleMapping mapping, boolean readOnly )
    {
        this.setReadOnly( readOnly );
        this.setSource( mapping.getSource() );
        this.setUserId( mapping.getUserId() );

        if ( mapping.getRoles() != null )
        {
            for ( String roleId : (List<String>) mapping.getRoles() )
            {
                addRole( roleId );
            }
        }

    }

    boolean readOnly;

    public boolean isReadOnly()
    {
        return readOnly;
    }

    public void setReadOnly( boolean readOnly )
    {
        this.readOnly = readOnly;
    }

}
