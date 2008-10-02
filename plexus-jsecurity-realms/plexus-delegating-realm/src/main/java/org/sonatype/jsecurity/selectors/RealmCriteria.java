package org.sonatype.jsecurity.selectors;

import org.jsecurity.realm.Realm;

public class RealmCriteria
{
    private String name;

    public void setName( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public boolean matches( Realm realm )
    {
        if ( realm != null && ( name == null || realm.getName().equals( name ) ) )
        {
            return true;
        }

        return false;
    }
}
