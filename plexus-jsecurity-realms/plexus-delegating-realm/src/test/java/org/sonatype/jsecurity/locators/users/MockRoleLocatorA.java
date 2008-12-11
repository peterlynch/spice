package org.sonatype.jsecurity.locators.users;

import java.util.HashSet;
import java.util.Set;

public class MockRoleLocatorA extends AbstractTestRoleLocator
{

    public String getSource()
    {
        return "MockRoleLocatorA";
    }

    public Set<String> listRoleIds()
    {
        Set<String> ids = new HashSet<String>();
        ids.add( "role123" );
        ids.add( "role124" );
        ids.add( "role125" );
        ids.add( "role126" );
        return ids;
    }   

}
