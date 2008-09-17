package org.sonatype.jsecurity.realms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.StringUtils;
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
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.cache.HashtableCache;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.subject.PrincipalCollection;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.PasswordGenerator;

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
    
    /**
     * @plexus.requirement
     */
    private PasswordGenerator pwGenerator;
    
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
        
        CUser user = configuration.readUser( upToken.getUsername() );
        
        if ( user == null )
        {
            throw new AccountException( "User '" + upToken.getUsername() + "' cannot be retrieved." );
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
        
        CUser user = configuration.readUser( username );
        
        if ( user == null )
        {
            throw new AuthorizationException( "User '" + username + "' cannot be retrieved." );
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
                CRole role = configuration.readRole( roleId );
                
                if ( role != null )
                {
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
            }
        }
        
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo( roleIds );
        info.setObjectPermissions( permissions );

        return info;
    }
    
    protected Set<Permission> getPermissions( String privilegeId )
    {
        CPrivilege privilege = configuration.readPrivilege( privilegeId );
        
        if ( privilege != null )
        {
            String permissionString = configuration.getPrivilegeProperty( privilege, "permission" );
            
            if ( StringUtils.isEmpty( permissionString ) )
            {
                permissionString = "*:*";
            }

            Permission permission = new WildcardPermission( permissionString );
            
            return Collections.singleton( permission );
        }       

        return Collections.emptySet();
    }
    
    public void clearCache()
    {
        getAuthorizationCache().clear();
        configuration.clearCache();
    }
    
    public void changePassword( String username, String oldPassword, String newPassword )
    {
        CUser user = configuration.readUser( username );
        
        if ( user != null )
        {
            String validate = pwGenerator.hashPassword( oldPassword );
            
            if ( !validate.equals( user.getPassword() ) )
            {
                // Invalid
            }
            else
            {
                user.setPassword( pwGenerator.hashPassword( newPassword ) );
                
                configuration.updateUser( user );
                
                configuration.save();
            }
        }
    }
    
    public void forgotPassword( String username, String email )
    {
        CUser user = configuration.readUser( username );
        
        if ( user != null && user.getEmail().equals( email ) )
        {
            resetPassword( username );
        }
    }
    
    public void forgotUsername( String email )
    {
        List<CUser> users = configuration.listUsers();
        
        List<String> userIds = new ArrayList<String>();
        for ( CUser user : users )
        {
            if ( user.getEmail().equals( email ) )
            {
                userIds.add( user.getId() );
            }
        }
        
        if ( userIds.size() > 0 )
        {
            //TODO Notify user by email
        }
    }
    
    public void resetPassword( String username )
    {
        CUser user = configuration.readUser( username );
        
        if ( user != null )
        {
            String password = pwGenerator.generatePassword( 10, 10 );
            
            user.setPassword( pwGenerator.hashPassword( password ) );
            
            configuration.updateUser( user );
            
            configuration.save();
            
            // TODO Notify user by email
        }
    }
    
    protected ConfigurationManager getConfigurationManager()
    {
        return configuration;
    }
}
