
package org.sonatype.jsecurity.realms.tools;

import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;

public interface ConfigurationManager
{
    String ROLE = ConfigurationManager.class.getName();
    
    /**
     * Create a new user
     * 
     * @param user
     */
    void createUser( CUser user );
    
    /**
     * Create a new role
     * 
     * @param role
     */
    void createRole( CRole role );
    
    /**
     * Create a new privilege
     * 
     * @param privilege
     */
    void createPrivilege( CPrivilege privilege );
    
    /**
     * Retrieve an existing user
     * 
     * @param id
     * @return
     */
    CUser readUser( String id );
    
    /**
     * Retrieve an existing role
     * 
     * @param id
     * @return
     */
    CRole readRole( String id );
    
    /**
     * Retrieve an existing privilege
     * @param id
     * @return
     */
    CPrivilege readPrivilege( String id );
    
    /**
     * Update an existing user
     * 
     * @param user
     */
    void updateUser( CUser user );
    
    /**
     * Update an existing role
     * 
     * @param role
     */
    void updateRole( CRole role );
    
    /**
     * Update an existing privilege
     * 
     * @param privilege
     */
    void updatePrivilege( CPrivilege privilege );
    
    /**
     * Delete an existing user
     * 
     * @param id
     */
    void deleteUser( String id );
    
    /**
     * Delete an existing role
     * 
     * @param id
     */
    void deleteRole( String id );
    
    /**
     * Delete an existing privilege
     * 
     * @param id
     */
    void deletePrivilege( String id );
    
    /**
     * Helper method to retrieve a property from the privilege
     * 
     * @param privilege
     * @param key
     * @return
     */
    String getPrivilegeProperty( CPrivilege privilege, String key );
    
    /**
     * Helper method to retrieve a property from the privilege
     * @param id
     * @param key
     * @return
     */
    String getPrivilegeProperty( String id, String key );
    
    /**
     * Clear the cache and reload from file
     */
    void clearCache();
    
    /**
     * Save to disk what is currently cached in memory 
     */
    void save();
}
