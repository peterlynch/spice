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
package org.jsecurity.mgt;

import org.jsecurity.authz.AuthorizationException;
import org.jsecurity.authz.Authorizer;
import org.jsecurity.authz.ModularRealmAuthorizer;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.PermissionResolver;
import org.jsecurity.authz.permission.PermissionResolverAware;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.PrincipalCollection;
import org.jsecurity.util.LifecycleUtils;

import java.util.Collection;
import java.util.List;

/**
 * JSecurity support of a {@link SecurityManager} class hierarchy that delegates all
 * authorization (access control) operations to a wrapped {@link Authorizer Authorizer} instance.  That is,
 * this class implements all the <tt>Authorizer</tt> methods in the {@link SecurityManager SecurityManager}
 * interface, but in reality, those methods are merely passthrough calls to the underlying 'real'
 * <tt>Authorizer</tt> instance.
 *
 * <p>All remaining <tt>SecurityManager</tt> methods not covered by this class or its parents (mostly Session support)
 * are left to be implemented by subclasses.
 *
 * <p>In keeping with the other classes in this hierarchy and JSecurity's desire to minimize configuration whenever
 * possible, suitable default instances for all dependencies will be created upon instantiation.
 *
 * @author Les Hazlewood
 * @since 0.9
 */
public abstract class AuthorizingSecurityManager extends AuthenticatingSecurityManager implements PermissionResolverAware {

    /**
     * The wrapped instance to which all of this <tt>SecurityManager</tt> authorization calls are delegated.
     */
    protected Authorizer authorizer;

    /**
     * Default no-arg constructor.
     */
    public AuthorizingSecurityManager() {
        ensureAuthorizer();
    }

    /**
     * Returns the underlying wrapped <tt>Authorizer</tt> instance to which this <tt>SecurityManager</tt>
     * implementation delegates all of its authorization calls.
     *
     * @return the wrapped <tt>Authorizer</tt> used by this <tt>SecurityManager</tt> implementation.
     */
    public Authorizer getAuthorizer() {
        return authorizer;
    }

    /**
     * Sets the underlying <tt>Authorizer</tt> instance to which this <tt>SecurityManager</tt> implementation will
     * delegate all of its authorization calls.
     *
     * @param authorizer the <tt>Authorizer</tt> this <tt>SecurityManager</tt> should wrap and delegate all of its
     *                   authorization calls to.
     */
    public void setAuthorizer(Authorizer authorizer) {
        if (authorizer == null) {
            String msg = "Authorizer argument cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        this.authorizer = authorizer;
    }

    protected void ensureAuthorizer() {
        Authorizer authorizer = getAuthorizer();
        if (authorizer == null) {
            authorizer = createAuthorizer();
            setAuthorizer(authorizer);
        }
    }

    protected Authorizer createAuthorizer() {
        return new ModularRealmAuthorizer();
    }

    /**
     * Sets the <tt>PermissionResolver</tt> instance that will be passed on to the underlying default wrapped
     * {@link Authorizer Authorizer}.
     *
     * <p>This is a convenience method:  it allows you to configure an application-wide
     * <tt>PermissionResolver</tt> on the <tt>SecurityManager</tt> instance, and it will trickle its way down to the
     * 'real' authorizer and/or underlying Realms.  This is easier to configure at the <tt>SecurityManager</tt> level
     * than constructing your own object graph just to configure a <tt>PermissionResolver</tt> instance on objects
     * deep in the graph.
     *
     * @param permissionResolver the <tt>PermissionResolver</tt> instance to set on the wrapped <tt>Authorizer</tt>
     * @throws IllegalStateException if the underlying <code>Authorizer</code> does not implement the
     *                               {@link PermissionResolverAware PermissionResolverAware} interface, which ensures that the resolver can be registered.
     */
    public void setPermissionResolver(PermissionResolver permissionResolver) {
        Authorizer authz = getAuthorizer();
        if (authz instanceof PermissionResolverAware) {
            ((PermissionResolverAware) authz).setPermissionResolver(permissionResolver);
        } else {
            String msg = "Underlying Authorizer instance does not implement the " +
                    PermissionResolverAware.class.getName() + " interface.  This is required to support " +
                    "passthrough configuration of a PermissionResolver.";
            throw new IllegalStateException(msg);
        }
    }

    public void setRealms(Collection<Realm> realms) {
        super.setRealms(realms);
        Authorizer authz = getAuthorizer();
        if (authz instanceof ModularRealmAuthorizer) {
            ((ModularRealmAuthorizer) authz).setRealms(realms);
        }
    }

    /**
     * Template hook for subclasses to implement destruction/cleanup logic.  This will be called before this
     * instance's <tt>Authorizer</tt> instance will be cleaned up.
     */
    protected void beforeAuthorizerDestroyed() {
    }

    /**
     * Cleanup method that destroys/cleans up the wrapped {@link #getAuthorizer Authorizer} instance.
     */
    protected void destroyAuthorizer() {
        LifecycleUtils.destroy(getAuthorizer());
    }

    /**
     * Implementation of parent class's template hook for destruction/cleanup logic.
     *
     * <p>This implementation ensures subclasses are cleaned up first by calling
     * {@link #beforeAuthorizerDestroyed() beforeAuthorizerDestroyed()} and then actually cleans up the
     * wrapped <tt>Authorizer</tt> via the {@link #destroyAuthorizer() desroyAuthorizer()} method.
     */
    protected void beforeAuthenticatorDestroyed() {
        beforeAuthorizerDestroyed();
        destroyAuthorizer();
    }

    public boolean isPermitted(PrincipalCollection principals, String permissionString) {
        ensureRealms();
        return getAuthorizer().isPermitted(principals, permissionString);
    }

    public boolean isPermitted(PrincipalCollection principals, Permission permission) {
        ensureRealms();
        return getAuthorizer().isPermitted(principals, permission);
    }

    public boolean[] isPermitted(PrincipalCollection principals, String... permissions) {
        ensureRealms();
        return getAuthorizer().isPermitted(principals, permissions);
    }

    public boolean[] isPermitted(PrincipalCollection principals, List<Permission> permissions) {
        ensureRealms();
        return getAuthorizer().isPermitted(principals, permissions);
    }

    public boolean isPermittedAll(PrincipalCollection principals, String... permissions) {
        ensureRealms();
        return getAuthorizer().isPermittedAll(principals, permissions);
    }

    public boolean isPermittedAll(PrincipalCollection principals, Collection<Permission> permissions) {
        ensureRealms();
        return getAuthorizer().isPermittedAll(principals, permissions);
    }

    public void checkPermission(PrincipalCollection principals, String permission) throws AuthorizationException {
        ensureRealms();
        getAuthorizer().checkPermission(principals, permission);
    }

    public void checkPermission(PrincipalCollection principals, Permission permission) throws AuthorizationException {
        ensureRealms();
        getAuthorizer().checkPermission(principals, permission);
    }

    public void checkPermissions(PrincipalCollection principals, String... permissions) throws AuthorizationException {
        ensureRealms();
        getAuthorizer().checkPermissions(principals, permissions);
    }

    public void checkPermissions(PrincipalCollection principals, Collection<Permission> permissions) throws AuthorizationException {
        ensureRealms();
        getAuthorizer().checkPermissions(principals, permissions);
    }

    public boolean hasRole(PrincipalCollection principals, String roleIdentifier) {
        ensureRealms();
        return getAuthorizer().hasRole(principals, roleIdentifier);
    }

    public boolean[] hasRoles(PrincipalCollection principals, List<String> roleIdentifiers) {
        ensureRealms();
        return getAuthorizer().hasRoles(principals, roleIdentifiers);
    }

    public boolean hasAllRoles(PrincipalCollection principals, Collection<String> roleIdentifiers) {
        ensureRealms();
        return getAuthorizer().hasAllRoles(principals, roleIdentifiers);
    }

    public void checkRole(PrincipalCollection principals, String role) throws AuthorizationException {
        ensureRealms();
        getAuthorizer().checkRole(principals, role);
    }

    public void checkRoles(PrincipalCollection principals, Collection<String> roles) throws AuthorizationException {
        ensureRealms();
        getAuthorizer().checkRoles(principals, roles);
    }
}
