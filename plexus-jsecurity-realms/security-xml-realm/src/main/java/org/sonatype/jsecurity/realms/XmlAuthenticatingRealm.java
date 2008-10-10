package org.sonatype.jsecurity.realms;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
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
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.PrincipalCollection;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;

@Component( role = Realm.class, hint = "XmlAuthenticatingRealm" )
public class XmlAuthenticatingRealm
    extends AuthorizingRealm
    implements Initializable, Realm
{
    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager configuration;

    @Override
    public String getName()
    {
        return XmlAuthenticatingRealm.class.getName();
    }

    public void initialize()
        throws InitializationException
    {
        setCredentialsMatcher( new Sha1CredentialsMatcher() );
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;

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
            return new SimpleAuthenticationInfo( upToken.getUsername(), user.getPassword().toCharArray(), getName() );
        }
        else if ( CUser.STATUS_DISABLED.equals( user.getStatus() ) )
        {
            throw new DisabledAccountException( "User '" + upToken.getUsername() + "' is disabled." );
        }
        else
        {
            throw new AccountException( "User '" + upToken.getUsername() + "' is in illegal status '"
                + user.getStatus() + "'." );
        }
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection arg0 )
    {
        return null;
    }

    protected ConfigurationManager getConfigurationManager()
    {
        return configuration;
    }
}
