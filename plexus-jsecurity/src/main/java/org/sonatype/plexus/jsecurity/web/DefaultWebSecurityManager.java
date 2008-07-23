package org.sonatype.plexus.jsecurity.web;

import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.RememberMeManager;

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
     * @plexus.requirement role="org.jsecurity.realm.Realm"
     */
    private List<Realm> realms;

    /**
     * @plexus.requirement role="org.jsecurity.subject.RememberMeManager" role-hint="default-web"
     */
    private RememberMeManager webRememberMeManager;

    public void initialize()
        throws InitializationException
    {
        setRememberMeManager( webRememberMeManager );

        setRealms( realms );
    }
}
