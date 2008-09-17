package org.sonatype.jsecurity.web;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authz.AuthorizationException;
import org.jsecurity.authz.Permission;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.PrincipalCollection;
import org.jsecurity.web.DefaultWebSecurityManager;
import org.sonatype.jsecurity.locators.RememberMeLocator;
import org.sonatype.jsecurity.realms.DefaultPlexusSecurity;
import org.sonatype.jsecurity.realms.MutableRealm;
import org.sonatype.jsecurity.realms.PlexusSecurity;
import org.sonatype.jsecurity.selectors.RealmCriteria;
import org.sonatype.jsecurity.selectors.RealmSelector;

/**
 * @plexus.component role="org.sonatype.jsecurity.realms.PlexusSecurity" role-hint="web"
 * 
 * Currently only supports a single child realm.  Plan on implementing mulitiple child realms
 * and a seperation of authentication/authorization realms
 */
//@Component(role = PlexusSecurity.class)
public class WebPlexusSecurity
    extends DefaultWebSecurityManager
    implements PlexusSecurity, Realm, Initializable
{    
    //@Requirement
    /** @plexus.requirement */
    private RememberMeLocator rememberMeLocator;

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
    
    public void clearCache( String realmName )
    {
        RealmCriteria criteria = new RealmCriteria();
        criteria.setName( realmName );
        
        Realm realm = realmSelector.selectRealm( criteria );
        
        if ( realm != null && MutableRealm.class.isAssignableFrom( realm.getClass() ) )
        {
            ( ( MutableRealm ) realm ).clearCache();
        }
    }
    
    public void clearCache( Set<String> realmNames )
    {
        if ( realmNames != null )
        {
            for ( String realmName : realmNames )
            {
                clearCache( realmName );
            }
        }
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
        
        setRealm( selectRealm() );
        
        setRememberMeManager( rememberMeLocator.getRememberMeManager() );
    }
}
