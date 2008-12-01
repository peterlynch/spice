package org.sonatype.jsecurity.locators.users;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component( role = PlexusUserManager.class )
public class DefaultPlexusUserManager
    implements PlexusUserManager
{
    public static final String SOURCE_ALL = "all";
    
    @Requirement( role = PlexusUserLocator.class )
    private List<PlexusUserLocator> locators;
    
    public List<PlexusUser> listUsers( String source )
    {
        ArrayList<PlexusUser> users = new ArrayList<PlexusUser>();
        
        for ( PlexusUserLocator locator : locators )
        {
            if ( SOURCE_ALL.equals( source )
                || locator.getSource().equals( source ) )
            {
                users.addAll( locator.listUsers() );
            }
        }
        
        Collections.sort( users );

        return users;
    }
    
    public List<String> listUserIds( String source )
    {
        ArrayList<String> userIds = new ArrayList<String>();
        
        for ( PlexusUserLocator locator : locators )
        {
            if ( SOURCE_ALL.equals( source )
                || locator.getSource().equals( source ) )
            {
                userIds.addAll( locator.listUserIds() );
            }
        }
        
        Collections.sort( userIds );

        return userIds;
    }
    
    public PlexusUser getUser( String userId )
    {
        PlexusUserLocator primary = getPrimaryLocator();
        
        if ( primary != null )
        {
            return primary.getUser( userId );
        }
        else
        {            
            for ( PlexusUserLocator locator : locators )
            {
                PlexusUser found = locator.getUser( userId );
                
                if ( found != null )
                {
                    return found;
                }
            }
        }

        return null;
    }
    
    private PlexusUserLocator getPrimaryLocator()
    {
        for ( PlexusUserLocator locator : locators )
        {
            if ( locator.isPrimary() )
            {
                return locator;
            }
        } 
        
        return null;
    }
}
