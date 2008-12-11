package org.sonatype.jsecurity.realms;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Requirement;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.credential.Sha1CredentialsMatcher;
import org.jsecurity.authz.AuthorizationException;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.SimpleAuthorizationInfo;
import org.jsecurity.cache.HashtableCache;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.PrincipalCollection;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.model.CUserRoleMapping;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleException;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleMappingException;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;

public abstract class AbstractXmlAuthorizingRealm
    extends AuthorizingRealm
    implements Realm
{
    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager configuration;

    public AbstractXmlAuthorizingRealm()
    {
        setCredentialsMatcher( new Sha1CredentialsMatcher() );
        setAuthorizationCache( new HashtableCache( null ) );
    }

    @Override
    public String getName()
    {
        return AbstractXmlAuthorizingRealm.class.getName();
    }

    @Override
    public boolean supports( AuthenticationToken token )
    {
        return false;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {
        return null;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {
        if ( principals == null )
        {
            throw new AuthorizationException( "Cannot authorize with no principals." );
        }

        String username = (String) principals.iterator().next();

        SecurityUser user;
        try
        {
            user = configuration.readUser( username );
        }
        catch ( NoSuchUserException e )
        {
            throw new AuthorizationException( "User '" + username + "' cannot be retrieved.", e );
        }

        LinkedList<String> rolesToProcess = new LinkedList<String>();
        Set<String> roles = user.getRoles();
        
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

    protected abstract Set<Permission> getPermissions( String privilegeId );

    protected ConfigurationManager getConfigurationManager()
    {
        return configuration;
    }
}
