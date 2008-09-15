package org.sonatype.jsecurity.realms;

import org.jsecurity.realm.Realm;

/**
 * A simple extensions of the jsecurity Realm interface, exposing a clear cache
 * method that will force the realm to lookup its data again
 */
public interface MutableRealm
    extends Realm
{
    String ROLE = MutableRealm.class.getName();
    
    /**
     * Clear jsecurity caches so that realm will be queried again for authorization data
     */
    void clearCache();
}
