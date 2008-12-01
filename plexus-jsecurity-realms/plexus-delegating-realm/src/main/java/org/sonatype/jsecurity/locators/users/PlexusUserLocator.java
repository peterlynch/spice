package org.sonatype.jsecurity.locators.users;

import java.util.List;

/**
 * Handles retrieving user data from a data source
 */
public interface PlexusUserLocator
{
    /**
     * Retrieve all PlexusUser objects
     * @return
     */
    List<PlexusUser> listUsers();
    
    /**
     * Retrieve all userids (if managing full object
     * list is to heavy handed)
     * @return
     */
    List<String> listUserIds();
    
    /**
     * Get a PlexusUser object by id
     * @param userId
     * @return
     */
    PlexusUser getUser( String userId );
    
    /**
     * With multiple locators allowed, only one can be defined as primary.
     * This is where primary data will be retrieved (Name, Email, etc.).
     * @return
     */
    boolean isPrimary();
    
    /**
     * Get the source string of this locator
     * @return
     */
    String getSource();
}
