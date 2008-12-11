package org.sonatype.jsecurity.locators.users;

import java.util.Set;

public interface PlexusUserAdditinalRoleLocator
{

    /**
     * Returns a Set of roles for a user that is not managed by this Realm.
     * 
     * @param userId
     * @return
     */
    Set<PlexusRole> getUsersAdditinalRoles( String userId, String source );

}
