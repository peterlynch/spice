package org.sonatype.jsecurity.locators.users;

import java.util.Set;

/**
 * Handles retrieving role data from a data source.
 */
public interface PlexusRoleLocator
{
    /**
     * Retrieve all role Ids (if managing full object
     * list is to heavy handed).
     * 
     * @return A Set of all Role Ids from this source.
     */
    public Set<String> listRoleIds();
    
    /**
     * Retrieve all roles from this source.
     * 
     * @return All PlexusRole from this source.
     */
    public Set<PlexusRole> listRoles();
    
    /**
     * Get the source string of this locator
     * @return
     */
    public String getSource();
    
}
