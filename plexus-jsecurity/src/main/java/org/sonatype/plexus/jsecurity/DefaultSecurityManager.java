package org.sonatype.plexus.jsecurity;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jsecurity.realm.Realm;
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
        if ( securityConfigurationProvider.getRememberMeManager() != null )
        {
            setRememberMeManager( securityConfigurationProvider.getRememberMeManager() );
        }

        setRealms( securityConfigurationProvider.getRealms() );

        // init the realms
        for ( Realm realm : getRealms() )
        {
            LifecycleUtils.init( realm );
        }
    }
}
