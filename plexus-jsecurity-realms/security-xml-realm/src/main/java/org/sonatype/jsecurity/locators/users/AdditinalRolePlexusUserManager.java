package org.sonatype.jsecurity.locators.users;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
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
        Set<PlexusUser> users = new TreeSet<PlexusUser>();
        for ( PlexusUser user : users )
        {
            this.populateAdditionalRoles( user );
        }
        return users;
    }

    @Override
    public Set<PlexusUser> searchUserById( String source, String userId )
    {
        Set<PlexusUser> users = new TreeSet<PlexusUser>();
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
            plexusRole.setSource( null );

            return plexusRole;
        }
        catch ( NoSuchRoleException e )
        {
            return null;
        }
    }

}
