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

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Requirement;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.credential.Sha1CredentialsMatcher;
import org.jsecurity.authz.AuthorizationException;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.SimpleAuthorizationInfo;
import org.jsecurity.cache.HashtableCache;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.PrincipalCollection;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserManager;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleException;

public abstract class AbstractXmlAuthorizingRealm
    extends AuthorizingRealm
    implements Realm
{
    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager configuration;
    
    @Requirement(role=PlexusUserManager.class, hint="additinalRoles")
    private PlexusUserManager userManager;

    public AbstractXmlAuthorizingRealm()
    {
        setCredentialsMatcher( new Sha1CredentialsMatcher() );
        setAuthorizationCache( new HashtableCache( null ) );
    }

    @Override
    public String getName()
    {
        return AbstractXmlAuthorizingRealm.class.getName();
    }

    @Override
    public boolean supports( AuthenticationToken token )
    {
        return false;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {
        return null;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {
        if ( principals == null )
        {
            throw new AuthorizationException( "Cannot authorize with no principals." );
        }

        String username = (String) principals.iterator().next();

        PlexusUser user = this.userManager.getUser( username );
        
        if ( user == null )
        {
            throw new AuthorizationException( "User '" + username + "' cannot be retrieved." );
        }

        LinkedList<String> rolesToProcess = new LinkedList<String>();
        Set<PlexusRole> roles = user.getRoles();
        
        if ( roles != null )
        {
            for ( PlexusRole plexusRole : roles )
            {
                rolesToProcess.add( plexusRole.getRoleId() );
            }
        }

        Set<String> roleIds = new LinkedHashSet<String>();
        Set<Permission> permissions = new LinkedHashSet<Permission>();
        while ( !rolesToProcess.isEmpty() )
        {
            String roleId = rolesToProcess.removeFirst();
            if ( !roleIds.contains( roleId ) )
            {
                CRole role;
                try
                {
                    role = configuration.readRole( roleId );
                    roleIds.add( roleId );

                    // process the roles this role has
                    rolesToProcess.addAll( role.getRoles() );

                    // add the permissions this role has
                    List<String> privilegeIds = role.getPrivileges();
                    for ( String privilegeId : privilegeIds )
                    {
                        Set<Permission> set = getPermissions( privilegeId );
                        permissions.addAll( set );
                    }
                }
                catch ( NoSuchRoleException e )
                {
                    // skip
                }
            }
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo( roleIds );
        info.setObjectPermissions( permissions );

        return info;
    }

    protected abstract Set<Permission> getPermissions( String privilegeId );

    protected ConfigurationManager getConfigurationManager()
    {
        return configuration;
    }
}
