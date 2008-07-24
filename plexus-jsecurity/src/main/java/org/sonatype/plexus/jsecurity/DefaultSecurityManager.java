package org.sonatype.plexus.jsecurity;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

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

    public void initialize()
        throws InitializationException
    {
        setRealms( securityConfigurationProvider.getRealms() );

        if ( securityConfigurationProvider.getRememberMeManager() != null )
        {
            setRememberMeManager( securityConfigurationProvider.getRememberMeManager() );
        }
    }
}
