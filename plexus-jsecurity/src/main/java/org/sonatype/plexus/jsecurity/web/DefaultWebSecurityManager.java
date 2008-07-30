package org.sonatype.plexus.jsecurity.web;

import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.RememberMeManager;
import org.jsecurity.util.LifecycleUtils;
import org.sonatype.plexus.jsecurity.SecurityConfigurationProvider;

/**
 * A simple Plexus componentized WebSecurityManager.
 * 
 * @author cstamas
 * @plexus.component role="org.jsecurity.mgt.SecurityManager" role-hint="default-web"
 */
public class DefaultWebSecurityManager
    extends org.jsecurity.web.DefaultWebSecurityManager
    implements Initializable
{
    /**
     * @plexus.requirement
     */
    private SecurityConfigurationProvider securityConfigurationProvider;

    public DefaultWebSecurityManager()
    {
        // nothing
    }

    public void initialize()
        throws InitializationException
    {
        RememberMeManager rmm = securityConfigurationProvider.getRememberMeManager();

        if ( rmm != null )
        {
            setRememberMeManager( rmm );
        }

        List<Realm> realms = securityConfigurationProvider.getRealms();

        setRealms( realms );

        // init the realms
        for ( Realm realm : realms )
        {
            LifecycleUtils.init( realm );
        }
    }
}
