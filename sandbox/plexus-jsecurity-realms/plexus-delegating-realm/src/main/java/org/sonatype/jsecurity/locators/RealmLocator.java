package org.sonatype.jsecurity.locators;

import java.util.List;

import org.jsecurity.realm.Realm;

public interface RealmLocator
{
    String ROLE = RealmLocator.class.getName();
    
    List<Realm> getRealms();
}
