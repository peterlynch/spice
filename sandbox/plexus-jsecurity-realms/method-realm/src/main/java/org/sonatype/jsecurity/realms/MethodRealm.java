package org.sonatype.jsecurity.realms;

import java.util.Collections;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;

/**
 * @plexus.component role="org.jsecurity.realm.Realm" role-hint="MethodRealm"
 *
 */
public class MethodRealm
    extends SecurityXmlRealm
{    
    @Override
    protected Set<Permission> getPermissions( String privilegeId )
    {
        CPrivilege privilege = getConfigurationManager().readPrivilege( privilegeId );
        
        if ( privilege != null )
        {
            String permissionString = getConfigurationManager().getPrivilegeProperty( privilege, "permission" );
            
            if ( StringUtils.isEmpty( permissionString ) )
            {
                permissionString = "*:*";
            }
            
            String methodString = getConfigurationManager().getPrivilegeProperty( privilege, "method" );
            
            if ( StringUtils.isEmpty( methodString ) )
            {
                methodString = "*";
            }

            Permission permission = new WildcardPermission( permissionString + ":" + methodString );
            
            return Collections.singleton( permission );
        }       

        return Collections.emptySet();
    }
}
