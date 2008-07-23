package org.sonatype.plexus.jsecurity;

import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jsecurity.realm.Realm;

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
     * @plexus.requirement role="org.jsecurity.realm.Realm"
     */
    private List<Realm> realms;

    public void initialize()
        throws InitializationException
    {
        setRealms( realms );
    }
}
