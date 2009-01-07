/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.jsecurity.realms;

import java.util.Collections;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.realm.Realm;
import org.sonatype.jsecurity.realms.tools.NoSuchPrivilegeException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;

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
            SecurityPrivilege privilege = getConfigurationManager().readPrivilege( privilegeId );

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
