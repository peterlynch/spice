package org.sonatype.plexus.security;

import org.jsecurity.realm.Realm;

public interface RealmSelector
{
    Realm selectRealm( RealmCriteria criteria );
}
