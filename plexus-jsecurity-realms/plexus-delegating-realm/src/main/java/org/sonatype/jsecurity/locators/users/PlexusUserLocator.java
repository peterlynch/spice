package org.sonatype.jsecurity.locators.users;

import java.util.Set;

/**
 * Handles retrieving user data from a data source
 */
public interface PlexusUserLocator
{
    /**
     * Retrieve all PlexusUser objects
     * @return
     */
    Set<PlexusUser> listUsers();
    
    /**
     * Searches for PlexusUser objects by userId.
     * @return
     */
    Set<PlexusUser> searchUserById( String userId );
    
    /**
     * Retrieve all userids (if managing full object
     * list is to heavy handed)
     * @return
     */
    Set<String> listUserIds();
    
    /**
     * Get a PlexusUser object by id
     * @param userId
     * @return
     */
    PlexusUser getUser( String userId );
    
//    /**
//     * Returns a Set of roles for a user that is not managed by this Realm.
//     * @param userId
//     * @return
//     */
//    Set<PlexusRole> getUsersAdditinalRoles( String userId );
    
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
