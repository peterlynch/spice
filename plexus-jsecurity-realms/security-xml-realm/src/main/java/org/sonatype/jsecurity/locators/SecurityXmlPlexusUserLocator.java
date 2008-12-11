package org.sonatype.jsecurity.locators;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserLocator;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.model.CUserRoleMapping;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleException;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleMappingException;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;

@Component( role = PlexusUserLocator.class, hint = "SecurityXmlPlexusUserLocator" )
public class SecurityXmlPlexusUserLocator
    implements PlexusUserLocator
{
    public static final String SOURCE = "security-xml";

    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager configuration;

    @Requirement
    private Logger logger;

    public Set<PlexusUser> listUsers()
    {
        Set<PlexusUser> users = new HashSet<PlexusUser>();

        for ( CUser user : configuration.listUsers() )
        {
            users.add( toPlexusUser( user ) );
        }

        return users;
    }

    public Set<String> listUserIds()
    {
        Set<String> userIds = new HashSet<String>();

        for ( CUser user : configuration.listUsers() )
        {
            userIds.add( user.getId() );
        }

        return userIds;
    }

    public PlexusUser getUser( String userId )
    {
        try
        {
            PlexusUser user = toPlexusUser( configuration.readUser( userId ) );
            return user;
        }
        catch ( NoSuchUserException e )
        {
            return null;
        }
    }

    public Set<PlexusUser> searchUserById( String userId )
    {
        Set<PlexusUser> users = new HashSet<PlexusUser>();

        for ( CUser user : configuration.listUsers() )
        {
            if ( user.getId().toLowerCase().startsWith( userId ) )
            {
                users.add( this.toPlexusUser( user ) );
            }
        }
        return users;
    }

    public boolean isPrimary()
    {
        // This locator will never be primary, if left standalone will
        // act as primary, otherwise other locators will be treated as primary
        return false;
    }

    protected PlexusUser toPlexusUser( CUser user )
    {
        if ( user == null )
        {
            return null;
        }

        PlexusUser plexusUser = new PlexusUser();

        plexusUser.setUserId( user.getId() );
        plexusUser.setName( user.getName() );
        plexusUser.setEmailAddress( user.getEmail() );
        plexusUser.setSource( SOURCE );

        CUserRoleMapping roleMapping;
        try
        {
            roleMapping = this.configuration.readUserRoleMapping( user.getId(), null );

            if ( roleMapping != null )
            {
                for ( String role : (List<String>) roleMapping.getRoles() )
                {
                    PlexusRole plexusRole = toPlexusRole( role );
                    if ( plexusRole != null )
                    {
                        plexusUser.addRole( plexusRole );
                    }
                }
            }
        }
        catch ( NoSuchRoleMappingException e )
        {
            this.logger.debug( "No user role mapping found for user: "+ user.getId() );

        }

        return plexusUser;
    }

    protected PlexusRole toPlexusRole( String roleId )
    {
        if ( roleId == null )
        {
            return null;
        }

        try
        {
            CRole role = configuration.readRole( roleId );

            PlexusRole plexusRole = new PlexusRole();

            plexusRole.setRoleId( role.getId() );
            plexusRole.setName( role.getName() );
            plexusRole.setSource( SOURCE );

            return plexusRole;
        }
        catch ( NoSuchRoleException e )
        {
            return null;
        }
    }

    public String getSource()
    {
        return SOURCE;
    }

}
