package org.sonatype.jsecurity.locators;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserLocator;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;

@Component( role = PlexusUserLocator.class, hint = "SecurityXmlPlexusUserLocator" )
public class SecurityXmlPlexusUserLocator
    implements PlexusUserLocator
{
    public static final String SOURCE = "security-xml";
    
    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager configuration;
    
    public List<PlexusUser> listUsers()
    {
        ArrayList<PlexusUser> users = new ArrayList<PlexusUser>();
        
        for ( CUser user : configuration.listUsers() )
        {
            users.add( toPlexusUser( user ) );
        }

        return users;
    }
    
    public List<String> listUserIds()
    {
        ArrayList<String> userIds = new ArrayList<String>();
        
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
        
        for ( CRole role : ( List<CRole> ) user.getRoles() )
        {
            plexusUser.addRole( toPlexusRole( role ) );
        }
        
        return plexusUser;
    }
    
    protected PlexusRole toPlexusRole( CRole role )
    {
        if ( role == null )
        {
            return null;
        }
        
        PlexusRole plexusRole = new PlexusRole();
        
        plexusRole.setRoleId( role.getId() );
        plexusRole.setName( role.getName() );
        plexusRole.setSource( SOURCE );
        
        return plexusRole;
    }
    
    public String getSource()
    {
        return SOURCE;
    }
}
