package org.sonatype.plexus.jsecurity;

import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.RememberMeManager;
import org.jsecurity.util.LifecycleUtils;

/**
 * A simple Plexus componentized WebSecurityManager.
 * 
 * @author cstamas
 * @plexus.component role="org.jsecurity.mgt.SecurityManager" role-hint="default"
 */
public class DefaultSecurityManager
    extends org.jsecurity.mgt.DefaultSecurityManager
    implements Initializable
{
    /**
     * @plexus.requirement
     */
    private SecurityConfigurationProvider securityConfigurationProvider;

    public DefaultSecurityManager()
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
