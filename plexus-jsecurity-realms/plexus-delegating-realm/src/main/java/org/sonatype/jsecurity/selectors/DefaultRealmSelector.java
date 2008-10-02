package org.sonatype.jsecurity.selectors;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.jsecurity.realm.Realm;
import org.sonatype.jsecurity.locators.RealmLocator;

@Component( role = RealmSelector.class )
public class DefaultRealmSelector
    implements RealmSelector
{
    @Requirement
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

    public List<Realm> selectAllRealms()
    {
        return this.realmLocator.getRealms();
    }
}
