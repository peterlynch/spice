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
package org.sonatype.jsecurity.locators.users;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.jsecurity.locators.SecurityXmlPlexusUserLocator;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUserRoleMapping;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleException;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleMappingException;

@Component( role=PlexusUserManager.class, hint="additinalRoles")
public class AdditinalRolePlexusUserManager
    extends DefaultPlexusUserManager
{
    @Requirement( hint = "resourceMerging" )
    private ConfigurationManager configManager;

    @Override
    public PlexusUser getUser( String userId )
    {
        PlexusUser user = super.getUser( userId );

        if ( user != null )
        {
            this.populateAdditionalRoles( user );
        }

        return user;
    }

    @Override
    public Set<PlexusUser> listUsers( String source )
    {
        Set<PlexusUser> users = super.listUsers( source );
        for ( PlexusUser user : users )
        {
            this.populateAdditionalRoles( user );
        }
        return users;
    }

    @Override
    public Set<PlexusUser> searchUserById( String source, String userId )
    {
        Set<PlexusUser> users = super.searchUserById( source, userId );
        for ( PlexusUser user : users )
        {
            this.populateAdditionalRoles( user );
        }
        return users;
    }

    private void populateAdditionalRoles( PlexusUser user )
    {
        try
        {
            CUserRoleMapping roleMapping = configManager.readUserRoleMapping( user.getUserId(), user.getSource() );

            for ( String roleId : (List<String>) roleMapping.getRoles() )
            {
                user.getRoles().add( this.toPlexusRole( roleId ) );
            }
        }
        catch ( NoSuchRoleMappingException e )
        {
            // this is ok, it will happen most of the time
        }
    }

    // FIXME duplicate code
    protected PlexusRole toPlexusRole( String roleId )
    {
        if ( roleId == null )
        {
            return null;
        }

        try
        {
            CRole role = configManager.readRole( roleId );

            PlexusRole plexusRole = new PlexusRole();

            plexusRole.setRoleId( role.getId() );
            plexusRole.setName( role.getName() );
            plexusRole.setSource( SecurityXmlPlexusUserLocator.SOURCE );

            return plexusRole;
        }
        catch ( NoSuchRoleException e )
        {
            return null;
        }
    }

}
