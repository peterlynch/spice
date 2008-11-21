package org.sonatype.jsecurity.locators.users;

import java.util.List;

/**
 * The PlexusUserManager is responsible for retrieving user data from different
 * data sources.
 */
public interface PlexusUserManager
{
    /**
     * Retrieve all PlexusUser objects defined by the PlexusUserLocator components
     * @return
     */
    List<PlexusUser> listUsers();
    
    /**
     * Retrieve all userids defined by the PlexusUserLocator components (if managing full object
     * list is to heavy handed)
     * @return
     */
    List<String> listUserIds();
    
    /**
     * Get a PlexusUser object by id from a PlexusUserLocator component
     * @param userId
     * @return
     */
    PlexusUser getUser( String userId );
}
