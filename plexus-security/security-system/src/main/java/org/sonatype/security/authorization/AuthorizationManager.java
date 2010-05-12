package org.sonatype.security.authorization;

import java.util.Set;

import org.sonatype.configuration.validation.InvalidConfigurationException;

/**
 * A DAO for Roles and Privileges comming from a given source.
 * 
 * @author Brian Demers
 */
public interface AuthorizationManager
{
    /**
     * The Id if this AuthorizationManager;
     * 
     * @return
     */
    public String getSource();

    /**
     * If this AuthorizationManager is writable.
     * 
     * @return
     */
    boolean supportsWrite();

    // **************
    // ROLE CRUDS
    // **************
    /**
     * Returns the all Roles from this AuthorizationManager. NOTE: this call could be slow when coming from an external
     * source (i.e. a database) TODO: Consider removing this method.
     * 
     * @return
     */
    public Set<Role> listRoles();

    /**
     * Returns a Role base on an Id.
     * 
     * @param roleId
     * @return
     * @throws NoSuchRoleException
     */
    public Role getRole( String roleId )
        throws NoSuchRoleException;

    /**
     * Adds a role to this AuthorizationManager.
     * 
     * @param role
     * @return
     * @throws InvalidConfigurationException
     */
    public Role addRole( Role role )
        throws InvalidConfigurationException;

    /**
     * Updates a role in this AuthorizationManager.
     * 
     * @param role
     * @return
     * @throws NoSuchRoleException
     * @throws InvalidConfigurationException
     */
    public Role updateRole( Role role )
        throws NoSuchRoleException, InvalidConfigurationException;

    /**
     * Removes a role in this AuthorizationManager.
     * 
     * @param roleId
     * @throws NoSuchRoleException
     */
    public void deleteRole( String roleId )
        throws NoSuchRoleException;

    // Privilege CRUDS
    /**
     * Returns the all Privileges from this AuthorizationManager.
     * 
     * @return
     */
    public Set<Privilege> listPrivileges();

    /**
     * Returns a Privilege base on an Id.
     * 
     * @param privilegeId
     * @return
     * @throws NoSuchPrivilegeException
     */
    public Privilege getPrivilege( String privilegeId )
        throws NoSuchPrivilegeException;

    /**
     * Adds a Privilege to this AuthorizationManager.
     * 
     * @param privilege
     * @return
     * @throws InvalidConfigurationException
     */
    public Privilege addPrivilege( Privilege privilege )
        throws InvalidConfigurationException;

    /**
     * Updates a Privilege in this AuthorizationManager.
     * 
     * @param privilege
     * @return
     * @throws NoSuchPrivilegeException
     * @throws InvalidConfigurationException
     */
    public Privilege updatePrivilege( Privilege privilege )
        throws NoSuchPrivilegeException, InvalidConfigurationException;

    /**
     * Removes a Privilege in this AuthorizationManager.
     * 
     * @param privilegeId
     * @throws NoSuchPrivilegeException
     */
    public void deletePrivilege( String privilegeId )
        throws NoSuchPrivilegeException;
}
