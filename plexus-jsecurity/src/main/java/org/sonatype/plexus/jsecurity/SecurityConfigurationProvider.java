package org.sonatype.plexus.jsecurity;

import java.util.List;

import org.jsecurity.realm.Realm;
import org.jsecurity.subject.RememberMeManager;

/**
 * The implementation of this interface should provide realms needed to be added to SecurityManager.
 * 
 * @author cstamas
 */
public interface SecurityConfigurationProvider
{
    List<Realm> getRealms();

    RememberMeManager getRememberMeManager();
}
