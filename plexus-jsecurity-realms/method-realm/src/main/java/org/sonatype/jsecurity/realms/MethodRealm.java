package org.sonatype.jsecurity.realms;

import java.util.Collections;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.sonatype.jsecurity.model.CPrivilege;

/**
 * @plexus.component role="org.jsecurity.realm.Realm" role-hint="MethodRealm"
 *
 */
public class MethodRealm
    extends SecurityXmlRealm
{    
    @Override
    public String getName()
    {
        return MethodRealm.class.getName();
    }
    
    @Override
    protected Set<Permission> getPermissions( String privilegeId )
    {
        CPrivilege privilege = getConfigurationManager().readPrivilege( privilegeId );
        
        if ( privilege != null )
        {
            String permission = getConfigurationManager().getPrivilegeProperty( privilege, "permission" );
            String method = getConfigurationManager().getPrivilegeProperty( privilege, "method" );
            
            if ( StringUtils.isEmpty( permission ) )
            {
                permission = "*:*";
            }

            if ( StringUtils.isEmpty( method ) )
            {
                method = "*";
            }
            
            return Collections.singleton( ( Permission ) new WildcardPermission( permission + ":" + method) );
        }       

        return Collections.emptySet();
    }
}
