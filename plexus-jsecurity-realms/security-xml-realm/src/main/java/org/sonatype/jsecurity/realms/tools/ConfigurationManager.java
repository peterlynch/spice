
package org.sonatype.jsecurity.realms.tools;

import java.util.List;

import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.jsecurity.realms.validator.ValidationContext;

public interface ConfigurationManager
{
    /**
     * Retrieve all users
     * 
     * @return
     */
    List<SecurityUser> listUsers();
    
    /**
     * Retrieve all roles
     * 
     * @return
     */
    List<SecurityRole> listRoles();
    
    /**
     * Retrieve all privileges
     * 
     * @return
     */
    List<SecurityPrivilege> listPrivileges();
    
    /**
     * Create a new user
     * 
     * @param user
     */
    void createUser( SecurityUser user )
        throws InvalidConfigurationException;
    
    /**
     * Create a new user with a context to validate in
     * 
     * @param user
     */
    void createUser( SecurityUser user, ValidationContext context )
        throws InvalidConfigurationException;
    
    /**
     * Create a new role
     * 
     * @param role
     */
    void createRole( SecurityRole role )
        throws InvalidConfigurationException;
    
    /**
     * Create a new role with a context to validate in
     * 
     * @param role
     */
    void createRole( SecurityRole role, ValidationContext context )
        throws InvalidConfigurationException;
    
    /**
     * Create a new privilege
     * 
     * @param privilege
     */
    void createPrivilege( SecurityPrivilege privilege )
        throws InvalidConfigurationException;
    
    /**
     * Create a new privilege with a context to validate in
     * 
     * @param privilege
     */
    void createPrivilege( SecurityPrivilege privilege, ValidationContext context )
        throws InvalidConfigurationException;
    
    /**
     * Retrieve an existing user
     * 
     * @param id
     * @return
     */
    SecurityUser readUser( String id )
        throws NoSuchUserException;
    
    /**
     * Retrieve an existing role
     * 
     * @param id
     * @return
     */
    SecurityRole readRole( String id )
        throws NoSuchRoleException;
    
    /**
     * Retrieve an existing privilege
     * @param id
     * @return
     */
    SecurityPrivilege readPrivilege( String id )
        throws NoSuchPrivilegeException;
    
    /**
     * Update an existing user
     * 
     * @param user
     */
    void updateUser( SecurityUser user )
        throws InvalidConfigurationException,
        NoSuchUserException;
    
    /**
     * Update an existing user with a context to validate in
     * 
     * @param user
     */
    void updateUser( SecurityUser user, ValidationContext context )
        throws InvalidConfigurationException,
        NoSuchUserException;
    
    /**
     * Update an existing role
     * 
     * @param role
     */
    void updateRole( SecurityRole role )
        throws InvalidConfigurationException,
        NoSuchRoleException;
    
    /**
     * Update an existing role with a context to validate in
     * 
     * @param role
     */
    void updateRole( SecurityRole role, ValidationContext context )
        throws InvalidConfigurationException,
        NoSuchRoleException;
    
    /**
     * Update an existing privilege
     * 
     * @param privilege
     */
    void updatePrivilege( SecurityPrivilege privilege )
        throws InvalidConfigurationException,
        NoSuchPrivilegeException;
    
    /**
     * Update an existing privilege with a context to validate in
     * 
     * @param privilege
     */
    void updatePrivilege( SecurityPrivilege privilege, ValidationContext context )
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
    String getPrivilegeProperty( SecurityPrivilege privilege, String key );
    
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
    
    /**
     * Initialize the context used for validation
     * @return
     */
    ValidationContext initializeContext();
}
