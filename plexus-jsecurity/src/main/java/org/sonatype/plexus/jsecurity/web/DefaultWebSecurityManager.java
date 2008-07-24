package org.sonatype.plexus.jsecurity.web;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
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

    public void initialize()
        throws InitializationException
    {
        if ( securityConfigurationProvider.getRememberMeManager() != null )
        {
            setRememberMeManager( securityConfigurationProvider.getRememberMeManager() );
        }

        setRealms( securityConfigurationProvider.getRealms() );
    }
}
