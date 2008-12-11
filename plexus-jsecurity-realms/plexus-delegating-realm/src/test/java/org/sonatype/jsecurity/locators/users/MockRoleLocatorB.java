package org.sonatype.jsecurity.locators.users;

import java.util.HashSet;
import java.util.Set;

public class MockRoleLocatorB extends AbstractTestRoleLocator
{

    public String getSource()
    {
        return "MockRoleLocatorB";
    }

    public Set<String> listRoleIds()
    {
        Set<String> ids = new HashSet<String>();
        ids.add( "role23" );
        ids.add( "role24" );
        ids.add( "role25" );
        ids.add( "role26" );
        return ids;
    }

}
