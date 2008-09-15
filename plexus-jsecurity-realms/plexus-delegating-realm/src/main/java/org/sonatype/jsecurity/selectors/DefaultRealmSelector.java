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
     * @plexus.requirement
     */
    private RealmLocator realmLocator;
    
    public Realm selectRealm( RealmCriteria criteria )
    {
        for ( Realm realm : this.realmLocator.getRealms() )
        {
            if ( criteria.matches( realm ) )
            {
                return realm;
            }
        }
        
        return null;
    }
}
