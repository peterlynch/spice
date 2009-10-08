/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jsecurity.authz;

import org.jsecurity.authc.Account;
import org.jsecurity.authc.SimpleAccount;
import org.jsecurity.subject.PrincipalCollection;

import java.util.*;

/**
 * <p>A simple implementation of the {@link AuthorizingAccount} interface that is useful
 * for many realms.  This implementation caches an internal collection of roles and permissions
 * in order to perform authorization checks for a particular user.</p>
 *
 * <p>See {@link AuthorizingAccount} interface for reasons why this class is deprecated.</p>
 *
 * @author Jeremy Haile
 * @author Les Hazlewood
 * @deprecated
 * @since 0.2
 */
public class SimpleAuthorizingAccount extends SimpleAccount implements AuthorizingAccount {

    protected Set<SimpleRole> simpleRoles;

    /*--------------------------------------------
    |         C O N S T R U C T O R S           |
    ============================================*/
    public SimpleAuthorizingAccount() {
    }

    public SimpleAuthorizingAccount(Object principal, Object credentials, String realmName) {
        super(principal, credentials, realmName);
    }

    public SimpleAuthorizingAccount(Collection principals, Object credentials, String realmName) {
        super(principals, credentials, realmName);
    }

    public SimpleAuthorizingAccount(PrincipalCollection principals, Object credentials) {
        super(principals, credentials);
    }

    public SimpleAuthorizingAccount(Object principal, Object credentials, String realmName, Set<String> roleNames, Set<Permission> permissions) {
        this(principal, credentials, realmName);
        addRoles(roleNames);
        //only create a private role if there are permissions
        if (permissions != null && !permissions.isEmpty()) {
            addPrivateRole(getPrincipals(), permissions);
        }
    }

    public SimpleAuthorizingAccount(Collection principals, Object credentials, String realmName, Set<String> roleNames, Set<Permission> permissions) {
        this(principals, credentials, realmName);
        addRoles(roleNames);
        //only create a private role if there are permissions:
        if (permissions != null && !permissions.isEmpty()) {
            addPrivateRole(getPrincipals(), permissions);
        }
    }

    public SimpleAuthorizingAccount(PrincipalCollection principals, Object credentials, String realmName, Set<String> roleNames, Set<Permission> permissions) {
        this(principals, credentials, realmName);
        addRoles(roleNames);
        //only create a private role if there are permissions:
        if (permissions != null && !permissions.isEmpty()) {
            addPrivateRole(getPrincipals(), permissions);
        }
    }

    /*--------------------------------------------
    |               M E T H O D S               |
    ============================================*/
    @SuppressWarnings({"unchecked"})
    public void merge(Account otherAccount) {
        super.merge(otherAccount);
        if (otherAccount instanceof SimpleAuthorizingAccount) {
            SimpleAuthorizingAccount other = (SimpleAuthorizingAccount) otherAccount;
            Set<SimpleRole> otherRoles = other.getSimpleRoles();
            if (otherRoles != null && !otherRoles.isEmpty()) {
                for (SimpleRole otherRole : otherRoles) {
                    merge(otherRole);
                }
            }
        }
    }

    protected void merge(SimpleRole role) {
        SimpleRole existing = getRole(role.getName());
        if (existing != null) {
            Set<Permission> rolePerms = role.getPermissions();
            if (rolePerms != null && !rolePerms.isEmpty()) {
                existing.addAll(rolePerms);
            }
        } else {
            add(role);
        }
    }

    protected void addPrivateRole(PrincipalCollection principals, Collection<Permission> perms) {
        SimpleRole privateRole = createPrivateRole(principals);
        if (perms != null && !perms.isEmpty()) {
            privateRole.addAll(perms);
        }
        add(privateRole);
    }

    protected String getPrivateRoleName(PrincipalCollection principals) {
        return getClass().getName() + "_PRIVATE_ROLE_" + PrincipalCollection.class.getName();
    }

    protected SimpleRole createPrivateRole(PrincipalCollection principals) {
        String privateRoleName = getPrivateRoleName(principals);
        return new SimpleRole(privateRoleName);
    }

    public Set<SimpleRole> getSimpleRoles() {
        return simpleRoles;
    }

    public void setSimpleRoles(Set<SimpleRole> simpleRoles) {
        this.simpleRoles = simpleRoles;
    }

    public SimpleRole getRole(String name) {
        Collection<SimpleRole> roles = getSimpleRoles();
        if (roles != null && !roles.isEmpty()) {
            for (SimpleRole role : roles) {
                if (role.getName().equals(name)) {
                    return role;
                }
            }
        }
        return null;
    }

    public Set<Permission> getPermissions() {
        Set<Permission> permissions = new HashSet<Permission>();
        for (SimpleRole role : simpleRoles) {
            permissions.addAll(role.getPermissions());
        }
        return permissions;
    }

    public Set<String> getRolenames() {
        Set<String> rolenames = new HashSet<String>();
        for (SimpleRole role : simpleRoles) {
            rolenames.add(role.getName());
        }
        return rolenames;
    }

    public void addRole(String roleName) {
        SimpleRole existing = getRole(roleName);
        if (existing == null) {
            SimpleRole role = new SimpleRole(roleName);
            add(role);
        }
    }

    public void add(SimpleRole role) {
        Set<SimpleRole> roles = getSimpleRoles();
        if (roles == null) {
            roles = new LinkedHashSet<SimpleRole>();
            setSimpleRoles(roles);
        }
        roles.add(role);
    }

    public void addRoles(Set<String> roleNames) {
        if (roleNames != null && !roleNames.isEmpty()) {
            for (String name : roleNames) {
                addRole(name);
            }
        }
    }

    public void addAll(Collection<SimpleRole> roles) {
        if (roles != null && !roles.isEmpty()) {
            Set<SimpleRole> existingRoles = getSimpleRoles();
            if (existingRoles == null) {
                existingRoles = new LinkedHashSet<SimpleRole>(roles.size());
                setSimpleRoles(existingRoles);
            }
            existingRoles.addAll(roles);
        }

    }

    public boolean hasRole(String roleName) {
        return getRole(roleName) != null;
    }

    public boolean isPermitted(Permission permission) {
        Collection<SimpleRole> roles = getSimpleRoles();
        if (roles != null && !roles.isEmpty()) {
            for (SimpleRole role : roles) {
                if (role.isPermitted(permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean[] hasRoles(List<String> roleIdentifiers) {
        boolean[] result;
        if (roleIdentifiers != null && !roleIdentifiers.isEmpty()) {
            int size = roleIdentifiers.size();
            result = new boolean[size];
            int i = 0;
            for (String roleName : roleIdentifiers) {
                result[i++] = hasRole(roleName);
            }
        } else {
            result = new boolean[0];
        }
        return result;
    }

    public boolean hasAllRoles(Collection<String> roleIdentifiers) {
        if (roleIdentifiers != null && !roleIdentifiers.isEmpty()) {
            for (String roleName : roleIdentifiers) {
                if (!hasRole(roleName)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean[] isPermitted(List<Permission> permissions) {
        boolean[] result;
        if (permissions != null && !permissions.isEmpty()) {
            int size = permissions.size();
            result = new boolean[size];
            int i = 0;
            for (Permission p : permissions) {
                result[i++] = isPermitted(p);
            }
        } else {
            result = new boolean[0];
        }
        return result;
    }

    public boolean isPermittedAll(Collection<Permission> permissions) {
        if (permissions != null && !permissions.isEmpty()) {
            for (Permission p : permissions) {
                if (!isPermitted(p)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void checkPermission(Permission permission) throws AuthorizationException {
        if (!isPermitted(permission)) {
            String msg = "User is not permitted [" + permission + "]";
            throw new UnauthorizedException(msg);
        }
    }

    public void checkPermissions(Collection<Permission> permissions) throws AuthorizationException {
        if (permissions != null && !permissions.isEmpty()) {
            for (Permission p : permissions) {
                checkPermission(p);
            }
        }
    }

    public void checkRole(String role) {
        if (!hasRole(role)) {
            String msg = "User does not have role [" + role + "]";
            throw new UnauthorizedException(msg);
        }
    }

    public void checkRoles(Collection<String> roles) {
        if (roles != null && !roles.isEmpty()) {
            for (String roleName : roles) {
                checkRole(roleName);
            }
        }
    }
}