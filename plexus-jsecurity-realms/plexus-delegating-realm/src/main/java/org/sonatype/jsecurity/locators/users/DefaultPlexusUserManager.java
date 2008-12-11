package org.sonatype.jsecurity.locators.users;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;

@Component( role = PlexusUserManager.class )
public class DefaultPlexusUserManager
    implements PlexusUserManager
{

    @Requirement( role = PlexusUserLocator.class )
    private List<PlexusUserLocator> userlocators;

    public Set<PlexusUser> listUsers( String source )
    {
        Set<PlexusUser> users = new TreeSet<PlexusUser>();

        // FIXME add something that would make the primary user win over the other realms?

        for ( PlexusUserLocator locator : userlocators )
        {
            if ( SOURCE_ALL.equals( source ) || locator.getSource().equals( source ) )
            {
                users.addAll( locator.listUsers() );
            }
        }

        return users;
    }

    

    public Set<PlexusUser> searchUserById( String source, String userId )
    {
        Set<PlexusUser> users = new TreeSet<PlexusUser>();

        for ( PlexusUserLocator locator : userlocators )
        {
            if ( SOURCE_ALL.equals( source ) || locator.getSource().equals( source ) )
            {
                users.addAll( locator.searchUserById( userId ) );
            }
        }

        return users;
    }

    public Set<String> listUserIds( String source )
    {
        Set<String> userIds = new TreeSet<String>();

        for ( PlexusUserLocator locator : userlocators )
        {
            if ( SOURCE_ALL.equals( source ) || locator.getSource().equals( source ) )
            {
                userIds.addAll( locator.listUserIds() );
            }
        }

        return userIds;
    }

    public PlexusUser getUser( String userId )
    {
        PlexusUserLocator primary = getPrimaryLocator();
        PlexusUser user = null;
        if ( primary != null )
        {
            user =  primary.getUser( userId );
        }
        
        if( user == null)
        {
            for ( PlexusUserLocator locator : userlocators )
            {
                PlexusUser found = locator.getUser( userId );

                if ( found != null )
                {
                    break;
                }
            }
        }

        return user;
    }

    private PlexusUserLocator getPrimaryLocator()
    {
        for ( PlexusUserLocator locator : userlocators )
        {
            if ( locator.isPrimary() )
            {
                return locator;
            }
        }

        return null;
    }

}
