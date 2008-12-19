package org.sonatype.jsecurity.locators;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserLocator;
import org.sonatype.jsecurity.locators.users.PlexusUserManager;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUserRoleMapping;

@Component( role = PlexusUserLocator.class, hint = "allConfigured", description = "All Configured Users" )
public class ConfiguredUsersPlexusUserLocator
    implements PlexusUserLocator
{

    @Requirement( hint = "additinalRoles" )
    private PlexusUserManager userManager;

    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager configuration;

    public String getSource()
    {
        return "allConfigured";
    }

    public Set<PlexusUser> listUsers()
    {
        Set<PlexusUser> users = this.userManager.listUsers( SecurityXmlPlexusUserLocator.SOURCE );

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
        Set<String> userIds = this.userManager.listUserIds( SecurityXmlPlexusUserLocator.SOURCE );

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