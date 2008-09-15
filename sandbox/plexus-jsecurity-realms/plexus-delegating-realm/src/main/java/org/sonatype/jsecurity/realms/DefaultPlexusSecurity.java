package org.sonatype.jsecurity.realms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authz.AuthorizationException;
import org.jsecurity.authz.Permission;
import org.jsecurity.mgt.DefaultSecurityManager;
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
    //@Requirement
    private RememberMeManager rememberMeManager;

    //@Requirement
    /** @plexus.requirement */
    private RealmSelector realmSelector;
    
    public Realm selectRealm( RealmCriteria criteria )
    {
        return realmSelector.selectRealm( criteria );
    }
    
    public Realm selectRealm( String realmName )
    {
        RealmCriteria criteria = new RealmCriteria();
        criteria.setName( realmName );
        
        return selectRealm( criteria );
    }
    
    public Realm selectRealm()
    {
        return selectRealm( new RealmCriteria() );
    }

    // JSecurity Realm Implementation

    public String getName()
    {
        return DefaultPlexusSecurity.class.getName();
    }

    // Authentication
    
    public AuthenticationInfo getAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {
        if ( UsernamePasswordRealmToken.class.isAssignableFrom( token.getClass() ) )
        {
            for ( String realmName : ( ( UsernamePasswordRealmToken) token ).getRealmNames() )
            {
                AuthenticationInfo ai = selectRealm( realmName ).getAuthenticationInfo( token );
                
                if ( ai != null )
                {
                    return ai;
                }
            }
            
            return null;
        }
        else
        {
            return selectRealm().getAuthenticationInfo( token );
        }
    }

    public boolean supports( AuthenticationToken token )
    {
        if ( UsernamePasswordRealmToken.class.isAssignableFrom( token.getClass() ) )
        {
            for ( String realmName : ( ( UsernamePasswordRealmToken) token ).getRealmNames() )
            {
                if ( selectRealm( realmName ).supports( token ) )
                {
                    return true;
                }
            }
            
            return false;
        }
        else
        {
            return selectRealm().supports( token );
        }
    }

    // Authorization
    
    public void checkPermission( PrincipalCollection subjectPrincipal, String permission )
        throws AuthorizationException
    {
        for ( String realmName : subjectPrincipal.getRealmNames() )
        {
            selectRealm( realmName ).checkPermission( subjectPrincipal, permission );
        }
    }

    public void checkPermission( PrincipalCollection subjectPrincipal, Permission permission )
        throws AuthorizationException
    {
        for ( String realmName : subjectPrincipal.getRealmNames() )
        {
            selectRealm( realmName ).checkPermission( subjectPrincipal, permission );
        }
    }

    public void checkPermissions( PrincipalCollection subjectPrincipal, String... permissions )
        throws AuthorizationException
    {
        for ( String realmName : subjectPrincipal.getRealmNames() )
        {
            selectRealm( realmName ).checkPermissions( subjectPrincipal, permissions );
        }        
    }

    public void checkPermissions( PrincipalCollection subjectPrincipal, Collection<Permission> permissions )
        throws AuthorizationException
    {
        for ( String realmName : subjectPrincipal.getRealmNames() )
        {
            selectRealm( realmName ).checkPermissions( subjectPrincipal, permissions );
        }
    }

    public void checkRole( PrincipalCollection subjectPrincipal, String roleIdentifier )
        throws AuthorizationException
    {
        for ( String realmName : subjectPrincipal.getRealmNames() )
        {
            selectRealm( realmName ).checkRole( subjectPrincipal, roleIdentifier );
        }
    }

    public void checkRoles( PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers )
        throws AuthorizationException
    {
        for ( String realmName : subjectPrincipal.getRealmNames() )
        {
            selectRealm( realmName ).checkRoles( subjectPrincipal, roleIdentifiers );
        }
    }

    public boolean hasAllRoles( PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers )
    {
        Set<String> hasRoleList = new HashSet<String>();
        
        for ( String realmName : subjectPrincipal.getRealmNames() )
        {
            int i = 0;
            
            for ( Iterator<String> iter = roleIdentifiers.iterator() ; iter.hasNext() ; i++ )
            {
                String roleIdentifier = iter.next();
                
                if ( selectRealm( realmName ).hasRole( subjectPrincipal, roleIdentifier ) )
                {
                    hasRoleList.add( roleIdentifier );
                }
            }
        }
        
        return hasRoleList.size() == roleIdentifiers.size();
    }

    public boolean hasRole( PrincipalCollection subjectPrincipal, String roleIdentifier )
    {
        for ( String realmName : subjectPrincipal.getRealmNames() )
        {
            if ( selectRealm( realmName ).hasRole( subjectPrincipal, roleIdentifier ) )
            {
                return true;
            }
        }
        
        return false;
    }

    public boolean[] hasRoles( PrincipalCollection subjectPrincipal, List<String> roleIdentifiers )
    {
        boolean[] combinedResult = new boolean[ roleIdentifiers.size() ];
        
        for ( String realmName : subjectPrincipal.getRealmNames() )
        {
            boolean[] result = selectRealm( realmName ).hasRoles( subjectPrincipal, roleIdentifiers );
            
            for ( int i = 0 ; i < combinedResult.length ; i++ )
            {
                combinedResult[i] = combinedResult[i] | result[i];
            }
        }
        
        return combinedResult;
    }

    public boolean isPermitted( PrincipalCollection principals, String permission )
    {
        for ( String realmName : principals.getRealmNames() )
        {
            if ( selectRealm( realmName ).isPermitted( principals, permission ) )
            {
                return true;
            }
        }

        return false;
    }

    public boolean isPermitted( PrincipalCollection subjectPrincipal, Permission permission )
    {
        for ( String realmName : subjectPrincipal.getRealmNames() )
        {
            if ( selectRealm( realmName ).isPermitted( subjectPrincipal, permission ) )
            {
                return true;
            }
        }

        return false;
    }

    public boolean[] isPermitted( PrincipalCollection subjectPrincipal, String... permissions )
    {
        boolean[] combinedResult = new boolean[ permissions.length ];
        
        for ( String realmName : subjectPrincipal.getRealmNames() )
        {
            boolean[] result = selectRealm( realmName ).isPermitted( subjectPrincipal, permissions );
            
            for ( int i = 0 ; i < combinedResult.length ; i++ )
            {
                combinedResult[i] = combinedResult[i] | result[i];
            }
        }
        
        return combinedResult;
    }

    public boolean[] isPermitted( PrincipalCollection subjectPrincipal, List<Permission> permissions )
    {
        boolean[] combinedResult = new boolean[ permissions.size() ];
        
        for ( String realmName : subjectPrincipal.getRealmNames() )
        {
            boolean[] result = selectRealm( realmName ).isPermitted( subjectPrincipal, permissions );
            
            for ( int i = 0 ; i < combinedResult.length ; i++ )
            {
                combinedResult[i] = combinedResult[i] | result[i];
            }
        }
        
        return combinedResult;
    }

    public boolean isPermittedAll( PrincipalCollection subjectPrincipal, String... permissions )
    {
        Set<String> isPermittedList = new HashSet<String>();
        
        for ( String realmName : subjectPrincipal.getRealmNames() )
        {            
            for ( int i = 0 ; i < permissions.length ; i++ )
            {   
                if ( selectRealm( realmName ).isPermitted( subjectPrincipal, permissions[i] ) )
                {
                    isPermittedList.add( permissions[i] );
                }
            }
        }
        
        return isPermittedList.size() == permissions.length;
    }

    public boolean isPermittedAll( PrincipalCollection subjectPrincipal, Collection<Permission> permissions )
    {
        Set<Permission> isPermittedList = new HashSet<Permission>();
        
        for ( String realmName : subjectPrincipal.getRealmNames() )
        {            
            int i = 0;
            
            for ( Iterator<Permission> iter = permissions.iterator() ; iter.hasNext() ; i++ )
            {   
                Permission perm = iter.next();
                
                if ( selectRealm( realmName ).isPermitted( subjectPrincipal, perm ) )
                {
                    isPermittedList.add( perm );
                }
            }
        }
        
        return isPermittedList.size() == permissions.size();
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
        
        setRealm( this );
        
        if ( rememberMeManager != null )
        {
            setRememberMeManager( rememberMeManager );
        }
    }
}
