
package org.sonatype.jsecurity.realms.tools;

import java.util.List;

import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;

public interface ConfigurationManager
{
    /**
     * Retrieve all users
     * 
     * @return
     */
    List<CUser> listUsers();
    
    /**
     * Retrieve all roles
     * 
     * @return
     */
    List<CRole> listRoles();
    
    /**
     * Retrieve all privileges
     * 
     * @return
     */
    List<CPrivilege> listPrivileges();
    
    /**
     * Create a new user
     * 
     * @param user
     */
    void createUser( CUser user )
        throws InvalidConfigurationException;
    
    /**
     * Create a new role
     * 
     * @param role
     */
    void createRole( CRole role )
        throws InvalidConfigurationException;
    
    /**
     * Create a new privilege
     * 
     * @param privilege
     */
    void createPrivilege( CPrivilege privilege )
        throws InvalidConfigurationException;
    
    /**
     * Retrieve an existing user
     * 
     * @param id
     * @return
     */
    CUser readUser( String id )
        throws NoSuchUserException;
    
    /**
     * Retrieve an existing role
     * 
     * @param id
     * @return
     */
    CRole readRole( String id )
        throws NoSuchRoleException;
    
    /**
     * Retrieve an existing privilege
     * @param id
     * @return
     */
    CPrivilege readPrivilege( String id )
        throws NoSuchPrivilegeException;
    
    /**
     * Update an existing user
     * 
     * @param user
     */
    void updateUser( CUser user )
        throws InvalidConfigurationException,
        NoSuchUserException;
    
    /**
     * Update an existing role
     * 
     * @param role
     */
    void updateRole( CRole role )
        throws InvalidConfigurationException,
        NoSuchRoleException;
    
    /**
     * Update an existing privilege
     * 
     * @param privilege
     */
    void updatePrivilege( CPrivilege privilege )
        throws InvalidConfigurationException,
        NoSuchPrivilegeException;
    
    /**
     * Delete an existing user
     * 
     * @param id
     */
    void deleteUser( String id )
        throws NoSuchUserException;
    
    /**
     * Delete an existing role
     * 
     * @param id
     */
    void deleteRole( String id )
        throws NoSuchRoleException;
    
    /**
     * Delete an existing privilege
     * 
     * @param id
     */
    void deletePrivilege( String id )
        throws NoSuchPrivilegeException;
    
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
    String getPrivilegeProperty( String id, String key )
        throws NoSuchPrivilegeException;
    
    /**
     * Clear the cache and reload from file
     */
    void clearCache();
    
    /**
     * Save to disk what is currently cached in memory 
     */
    void save();
}
