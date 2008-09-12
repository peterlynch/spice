package org.sonatype.jsecurity.realms;

import java.util.Collection;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authz.AuthorizationException;
import org.jsecurity.authz.Permission;
import org.jsecurity.cache.HashtableCache;
import org.jsecurity.mgt.DefaultSecurityManager;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.PrincipalCollection;
import org.jsecurity.subject.RememberMeManager;
import org.sonatype.jsecurity.selectors.RealmCriteria;
import org.sonatype.jsecurity.selectors.RealmSelector;

/*

As you probably know by now, JSecurity interfaces with a data source of any type via a Realm, which is essentially a 
security-specific DAO.  It can do both authentication and authorization.

However, you can easily configure a Realm to do one or both.

For Authentication:

The Realm interface specifies the 'supports(AuthenticationToken token)' method.  If you don't want a realm to participate in 
authentication ever, you can always return false from that method.

If you want the realm to perform authentications for only a special case, you can do a very simple check using a 
custom AuthenticationToken subclass:

For example, if you subclass UsernamePasswordToken to be something like RealmUsernamePasswordToken which has one additional 
property, say, 'realmName', then that name can be inspected in the Realm.supports implementation:

//This says this realm will only perform an authentication if the specified token
//matches this realm's name:
public boolean supports (AuthenticationToken token ) {
    if ( token instanceof RealmUsernamePasswordToken ) {
        if ( getName().equals((RealmUsernamePasswordToken)token).getRealmName() ) {
            return true;
        }
    }
    return false;
}

This will ensure that the realm only authenticates tokens which are 'directed to it' so to speak.  You could do this in 
other ways too, depending on what your matching criteria is (application 'domain', etc).

You should also be aware that the SecurityManager (and its underlying Authenticator, which coordinates login 
attempts across Realms) uses a 'ModularAuthenticationStrategy' (http://www.jsecurity.org/api/org/jsecurity/authc/pam/ModularAuthenticationStrategy.html) 
to determine what should happen during a multi-realm authentication attempt.  By default, this is an 'AllSuccessfulAuthentionStrategy', 
which requires all Realms to authenticate successfully.

Naturally if you want only one or some realms to perform authentication based on the token, you can't use this. Either the 
'AtLeastOneSuccessfulModularAuthenticationStrategy' or 'FirstSuccessfulModularAuthenticationStrategy' will work (please see their 
JavaDoc to understand their subtle difference).  You can configure this by calling securityManager.setModularAuthenticationStrategy, 
or if using web.xml or jsecurity.ini, 

authcStrategy = strategy.fully.qualified.class.name
securityManager.modularAuthenticationStrategy = $authcStrategy

You could surely implement your own strategy as well, depending on what you're trying to do, but the existing implementations 
should be sufficient.  I'm not saying you shouldn't implement your own, but I'd think twice if it is really necessary.

For Authorization:

Any of our AbstractRealm implementations have a method 'doGetAuthorizationInfo(PrincipalCollection principals)', which return
authorization data used to perform a security check.  If you don't want your Realm to participate in authorization, always 
return null from that method.  The realm will still be consulted during security checks, but this will cause it to return 
false for everything (hasRole(anyArgument) == false, hasPermission(anyArgument) == false, etc).

This is almost always sufficient for all use cases we've come across.  If you really want ultimate control over exactly 
what happens during Authorization and coordinate realms manually, you could always implement your own Authorizer implementation 
and inject that into the securityManager:

authorizer = fully.qualified.class.name
securityManager.authorizer = $authorizer

If this is not done, the default implementation used by the SecurityManager is a 'ModularRealmAuthorizer'.  But again, I 
would think twice if this is really necessary.

That should do it - via these things, you can easily control which realms (and thus data sources) participate in 
authentication or authorization or both.

For the simplest way without knowing much more about requirements, I definitely recommend the custom AuthenticationToken 
subclass and the corresponding 'supports' method implementations and specify one of the other ModularAuthenticationStrategy implementations.

Oh also, don't forget - a PrincipalCollection, which is attributed to every logged-in subject (subject.getPrincipals()) can 
return principal information specific to a Realm that it acquired during the successful authentication process:

subject.getPrincipals().fromRealm( realmName ) == the principal(s) from only that realm.

You could use this in application logic to show only certain things based on the associated realm.  Your realm implementations could 
also use that in their security checks:

principals.fromRealm( this.getName() ) 

We need to allow:

1. a single realm
2. multiple realms
3. partitioned authentication and authorization

*/

/**
 * @plexus.component role="org.sonatype.jsecurity.realms.PlexusSecurity"
 */
@Component(role = PlexusSecurity.class)
public class DefaultPlexusSecurity
    extends DefaultSecurityManager
    implements PlexusSecurity, Realm, Initializable
{
    public static final String NAME = "Plexus Security Realm";

    //@Requirement
    private RememberMeManager rememberMeManager;

    //@Requirement
    /** @plexus.requirement */
    private RealmSelector realmSelector;
    
    public Realm selectRealm()
    {
        return realmSelector.selectRealm( new RealmCriteria() );
    }

    // JSecurity Realm Implementation

    public String getName()
    {
        return NAME;
    }

    // Authentication
    
    public AuthenticationInfo getAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {
        return selectRealm().getAuthenticationInfo( token );
    }

    public boolean supports( AuthenticationToken token )
    {
        return selectRealm().supports( token );
    }

    // Authorization
    
    public void checkPermission( PrincipalCollection subjectPrincipal, String permission )
        throws AuthorizationException
    {
        selectRealm().checkPermission( subjectPrincipal, permission );
    }

    public void checkPermission( PrincipalCollection subjectPrincipal, Permission permission )
        throws AuthorizationException
    {
        selectRealm().checkPermission( subjectPrincipal, permission );
    }

    public void checkPermissions( PrincipalCollection subjectPrincipal, String... permissions )
        throws AuthorizationException
    {
        selectRealm().checkPermissions( subjectPrincipal, permissions );
    }

    public void checkPermissions( PrincipalCollection subjectPrincipal, Collection<Permission> permissions )
        throws AuthorizationException
    {
        selectRealm().checkPermissions( subjectPrincipal, permissions );
    }

    public void checkRole( PrincipalCollection subjectPrincipal, String roleIdentifier )
        throws AuthorizationException
    {
        selectRealm().checkRole( subjectPrincipal, roleIdentifier );
    }

    public void checkRoles( PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers )
        throws AuthorizationException
    {
        selectRealm().checkRoles( subjectPrincipal, roleIdentifiers );
    }

    public boolean hasAllRoles( PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers )
    {
        return selectRealm().hasAllRoles( subjectPrincipal, roleIdentifiers );
    }

    public boolean hasRole( PrincipalCollection subjectPrincipal, String roleIdentifier )
    {
        return selectRealm().hasRole( subjectPrincipal, roleIdentifier );
    }

    public boolean[] hasRoles( PrincipalCollection subjectPrincipal, List<String> roleIdentifiers )
    {
        return selectRealm().hasRoles( subjectPrincipal, roleIdentifiers );
    }

    public boolean isPermitted( PrincipalCollection principals, String permission )
    {
        return selectRealm().isPermitted( principals, permission );
    }

    public boolean isPermitted( PrincipalCollection subjectPrincipal, Permission permission )
    {
        return selectRealm().isPermitted( subjectPrincipal, permission );
    }

    public boolean[] isPermitted( PrincipalCollection subjectPrincipal, String... permissions )
    {
        return selectRealm().isPermitted( subjectPrincipal, permissions );
    }

    public boolean[] isPermitted( PrincipalCollection subjectPrincipal, List<Permission> permissions )
    {
        return selectRealm().isPermitted( subjectPrincipal, permissions );
    }

    public boolean isPermittedAll( PrincipalCollection subjectPrincipal, String... permissions )
    {
        return selectRealm().isPermittedAll( subjectPrincipal, permissions );
    }

    public boolean isPermittedAll( PrincipalCollection subjectPrincipal, Collection<Permission> permissions )
    {
        return selectRealm().isPermittedAll( subjectPrincipal, permissions );
    }

    // Plexus Lifecycle

    public void initialize()
        throws InitializationException
    {
        /*
         * 
         * We are setting our implementation to the realm for the security manager so that we can delegate to
         * any realm setup we wish to construct.
         * 
         * 
         */
        
        Realm realm = selectRealm();
        
        ( (AuthorizingRealm) realm ).setAuthorizationCache( new HashtableCache( null ) );               
        
        setRealm( realm );
        
        if ( rememberMeManager != null )
        {
            setRememberMeManager( rememberMeManager );
        }
    }
}
