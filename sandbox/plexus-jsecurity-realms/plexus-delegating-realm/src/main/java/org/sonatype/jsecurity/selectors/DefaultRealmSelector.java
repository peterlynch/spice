package org.sonatype.jsecurity.selectors;

import org.jsecurity.realm.Realm;
import org.sonatype.jsecurity.locators.RealmLocator;

/**
 * @plexus.component
 *
 */
public class DefaultRealmSelector
    implements RealmSelector
{
    /**
     * @plexus.requirement role-hint="PropertyFileRealmLocator"
     */
    private RealmLocator realmLocator;
    
    public Realm selectRealm( RealmCriteria criteria )
    {
        // Later we can do some filtering based upon criteria, for now just return the first one
        return this.realmLocator.getRealms().get( 0 );
    }
}
