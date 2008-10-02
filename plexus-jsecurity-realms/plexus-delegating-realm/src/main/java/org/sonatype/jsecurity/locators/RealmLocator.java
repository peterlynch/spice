package org.sonatype.jsecurity.locators;

import java.util.List;

import org.jsecurity.realm.Realm;

public interface RealmLocator
{
    List<Realm> getRealms();
}
