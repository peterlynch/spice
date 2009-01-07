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
package org.sonatype.jsecurity.locators;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserLocator;
import org.sonatype.jsecurity.locators.users.PlexusUserManager;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUserRoleMapping;

@Component( role = PlexusUserLocator.class, hint = "mappedExternal", description = "Mapped External Users" )
public class MappedUsersPlexusUserLocator
    implements PlexusUserLocator
{

    @Requirement( hint = "additinalRoles" )
    private PlexusUserManager userManager;

    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager configuration;

    public String getSource()
    {
        return "mappedExternal";
    }

    public Set<PlexusUser> listUsers()
    {
        Set<PlexusUser> users = new TreeSet<PlexusUser>();

        List<SecurityUserRoleMapping> userRoleMappings = this.configuration.listUserRoleMappings();
        for ( SecurityUserRoleMapping userRoleMapping : userRoleMappings )
        {
            if ( !SecurityXmlPlexusUserLocator.SOURCE.equals( userRoleMapping.getSource() ) )
            {
                PlexusUser user = this.userManager.getUser( userRoleMapping.getUserId(), userRoleMapping.getSource() );
                if ( user != null )
                {
                    users.add( user );
                }
            }
        }
        
        return users;
    }

    public Set<String> listUserIds()
    {
        Set<String> userIds = new TreeSet<String>();

        List<SecurityUserRoleMapping> userRoleMappings = this.configuration.listUserRoleMappings();
        for ( SecurityUserRoleMapping userRoleMapping : userRoleMappings )
        {
            if ( !SecurityXmlPlexusUserLocator.SOURCE.equals( userRoleMapping.getSource() ) )
            {
                String userId = userRoleMapping.getUserId();
                if ( StringUtils.isNotEmpty( userId ))
                {
                    userIds.add( userId );
                }
            }
        }
        
        return userIds;
    }

    public PlexusUser getUser( String userId )
    {
        // this resource will only list the users
        return null;
    }

    public Set<PlexusUser> searchUserById( String userId )
    {
        // this resource will only list the users
        return new HashSet<PlexusUser>();
    }

    public boolean isPrimary()
    {
        return false;
    }

}
