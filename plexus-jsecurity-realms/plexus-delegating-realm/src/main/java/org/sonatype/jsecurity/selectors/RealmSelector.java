package org.sonatype.jsecurity.selectors;

import java.util.List;

import org.jsecurity.realm.Realm;

public interface RealmSelector
{
    Realm selectRealm( RealmCriteria criteria );

    List<Realm> selectAllRealms();
}
