package org.sonatype.jsecurity.realms;

import org.jsecurity.realm.Realm;

public interface RealmSelector
{
    Realm selectRealm( RealmCriteria criteria );
}
