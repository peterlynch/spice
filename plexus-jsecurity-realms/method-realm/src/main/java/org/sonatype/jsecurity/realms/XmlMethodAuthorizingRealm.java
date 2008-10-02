package org.sonatype.jsecurity.realms;

import java.util.Collections;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.realm.Realm;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.realms.tools.NoSuchPrivilegeException;

@Component( role = Realm.class, hint = "XmlMethodAuthorizingRealm" )
public class XmlMethodAuthorizingRealm
    extends AbstractXmlAuthorizingRealm
{
    public static final String PRIVILEGE_TYPE_METHOD = "method";

    public static final String PRIVILEGE_PROPERTY_METHOD = "method";

    public static final String PRIVILEGE_PROPERTY_PERMISSION = "permission";

    @Override
    public boolean supports( AuthenticationToken token )
    {
        return false;
    }

    @Override
    protected Set<Permission> getPermissions( String privilegeId )
    {
        try
        {
            CPrivilege privilege = getConfigurationManager().readPrivilege( privilegeId );

            if ( !privilege.getType().equals( PRIVILEGE_TYPE_METHOD ) )
            {
                return Collections.emptySet();
            }

            String permission = getConfigurationManager().getPrivilegeProperty(
                privilege,
                PRIVILEGE_PROPERTY_PERMISSION );

            String method = getConfigurationManager().getPrivilegeProperty( privilege, PRIVILEGE_PROPERTY_METHOD );

            if ( StringUtils.isEmpty( permission ) )
            {
                permission = "*:*";
            }

            if ( StringUtils.isEmpty( method ) )
            {
                method = "*";
            }

            return Collections.singleton( (Permission) new WildcardPermission( permission + ":" + method ) );
        }
        catch ( NoSuchPrivilegeException e )
        {
            return Collections.emptySet();
        }
    }
}
