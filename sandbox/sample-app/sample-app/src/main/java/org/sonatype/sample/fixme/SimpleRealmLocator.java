package org.sonatype.sample.fixme;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.jsecurity.realm.Realm;
import org.sonatype.jsecurity.locators.RealmLocator;

@Component( role = RealmLocator.class )
public class SimpleRealmLocator
    implements RealmLocator
{
    @Requirement( role = Realm.class )
    private List<Realm> realms;

    public List<Realm> getRealms()
    {   
        return this.realms;
    }

}
