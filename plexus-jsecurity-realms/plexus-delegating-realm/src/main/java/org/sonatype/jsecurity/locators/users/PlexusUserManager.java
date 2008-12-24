package org.sonatype.jsecurity.locators.users;

import java.util.Set;

/**
 * The PlexusUserManager is responsible for retrieving user data from different
 * data sources.
 */
public interface PlexusUserManager
{
    public static final String SOURCE_ALL = "all";
    
    /**
     * Retrieve all PlexusUser objects defined by the PlexusUserLocator components
     * @return
     */
    Set<PlexusUser> listUsers( String source );
    
    /**
     * Searches for PlexusUser objects by userId.
     * @return
     */
    Set<PlexusUser> searchUserById( String userId, String source );
    
    /**
     * Retrieve all userids defined by the PlexusUserLocator components (if managing full object
     * list is to heavy handed)
     * @return
     */
    Set<String> listUserIds( String source );
    
    /**
     * Get a PlexusUser object by id from a PlexusUserLocator component
     * @param userId
     * @return
     */
    PlexusUser getUser( String userId );
    
    /**
     * Get a PlexusUser object by id from a PlexusUserLocator component
     * @param userId
     * @param source
     * @return
     */
    PlexusUser getUser( String userId, String source );
    
}
