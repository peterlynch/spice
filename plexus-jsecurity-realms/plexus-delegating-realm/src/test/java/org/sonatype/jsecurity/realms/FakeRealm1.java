package org.sonatype.jsecurity.realms;

import java.util.Collections;

import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.SimpleAuthorizationInfo;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.subject.PrincipalCollection;

/**
 * @plexus.component role="org.jsecurity.realm.Realm" role-hint="FakeRealm1"
 */
public class FakeRealm1
    extends
    AuthorizingRealm
{    
    @Override
    public String getName()
    {
        return FakeRealm1.class.getName();
    }
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection arg0 )
    {
        
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo( Collections.singleton( "role" ) );
        
        Permission permission = new WildcardPermission( "test:perm" );
        
        info.setObjectPermissions( Collections.singleton( permission ) );

        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {
        UsernamePasswordToken upToken = ( UsernamePasswordToken ) token;
        
        return new SimpleAuthenticationInfo( upToken.getUsername(), "password" , getName() );
    }
}
