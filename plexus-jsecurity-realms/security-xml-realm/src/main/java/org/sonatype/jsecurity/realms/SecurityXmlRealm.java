package org.sonatype.jsecurity.realms;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jsecurity.authc.AccountException;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.DisabledAccountException;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authc.credential.Sha1CredentialsMatcher;
import org.jsecurity.authz.AuthorizationException;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.SimpleAuthorizationInfo;
import org.jsecurity.cache.HashtableCache;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.subject.PrincipalCollection;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleException;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;

/**
 * @plexus.component role="org.jsecurity.realm.Realm" role-hint="SecurityXmlRealm"
 *
 */
public class SecurityXmlRealm
    extends AuthorizingRealm
        implements Initializable, MutableRealm
{
    /**
     * @plexus.requirement
     */
    private ConfigurationManager configuration;
    
    @Override
    public String getName()
    {
        return SecurityXmlRealm.class.getName();
    }
    
    public void initialize()
        throws InitializationException
    {
        setCredentialsMatcher( new Sha1CredentialsMatcher() );
        setAuthorizationCache( new HashtableCache( null ) );
    }
    
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {
        UsernamePasswordToken upToken = ( UsernamePasswordToken ) token;
        
        CUser user;
        try
        {
            user = configuration.readUser( upToken.getUsername() );
        }
        catch ( NoSuchUserException e )
        {
            throw new AccountException( "User '" + upToken.getUsername() + "' cannot be retrieved.", e );
        }
        
        if ( user.getPassword() == null )
        {
            throw new AccountException( "User '" + upToken.getUsername() + "' has no password, cannot authenticate." );
        }
        
        if ( CUser.STATUS_ACTIVE.equals( user.getStatus() ) )
        {
            return new SimpleAuthenticationInfo( upToken.getUsername(), user.getPassword().toCharArray() , getName() );
        }
        else if ( CUser.STATUS_DISABLED.equals( user.getStatus() ) )
        {
            throw new DisabledAccountException( "User '" + upToken.getUsername() + "' is disabled." );
        }
        else
        {
            throw new AccountException( "User '" + upToken.getUsername() + "' is in illegal status '" + user.getStatus() + "'." );
        }
    }
    
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {
        if ( principals == null )
        {
            throw new AuthorizationException( "Cannot authorize with no principals." );
        }
        
        String username = (String) principals.fromRealm( getName() ).iterator().next();
        
        CUser user;
        try
        {
            user = configuration.readUser( username );
        }
        catch ( NoSuchUserException e )
        {
            throw new AuthorizationException( "User '" + username + "' cannot be retrieved.", e );
        }
        
        LinkedList<String> rolesToProcess = new LinkedList<String>();
        List<String> roles = user.getRoles();
        if ( roles != null )
        {
            rolesToProcess.addAll( roles );
        }
        
        Set<String> roleIds = new LinkedHashSet<String>();
        Set<Permission> permissions = new LinkedHashSet<Permission>();
        while ( !rolesToProcess.isEmpty() )
        {
            String roleId = rolesToProcess.removeFirst();
            if ( !roleIds.contains( roleId ) )
            {                
                CRole role;
                try
                {
                    role = configuration.readRole( roleId );
                    roleIds.add( roleId );
                    
                    // process the roles this role has
                    rolesToProcess.addAll( role.getRoles() );
    
                    // add the permissions this role has
                    List<String> privilegeIds = role.getPrivileges();
                    for ( String privilegeId : privilegeIds )
                    {
                        Set<Permission> set = getPermissions( privilegeId );
                        permissions.addAll( set );
                    }
                }
                catch ( NoSuchRoleException e )
                {
                    // skip
                }
            }
        }
        
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo( roleIds );
        info.setObjectPermissions( permissions );

        return info;
    }
    
    protected Set<Permission> getPermissions( String privilegeId )
    {
        return Collections.emptySet();
    }
    
    public void clearCache()
    {
        getAuthorizationCache().clear();
        configuration.clearCache();
    }
    
    protected ConfigurationManager getConfigurationManager()
    {
        return configuration;
    }
}
