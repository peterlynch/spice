package org.sonatype.jsecurity.locators.users;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;

public class PlexusRoleManagerTest
    extends PlexusTestCase
{

    private PlexusRoleManager getRoleManager()
        throws Exception
    {
        return (PlexusRoleManager) this.lookup( PlexusRoleManager.class );
    }

    public void testGetAll()
        throws Exception
    {
        PlexusRoleManager roleManager = this.getRoleManager();
        Set<PlexusRole> roles = roleManager.listRoles( PlexusRoleManager.SOURCE_ALL );
        Assert.assertFalse( roles.isEmpty() );

        Map<String, PlexusRole> roleMap = this.getMapFromList( roles );

        Assert.assertTrue( roleMap.containsKey( "role123" ) );
        Assert.assertTrue( roleMap.containsKey( "role124" ) );
        Assert.assertTrue( roleMap.containsKey( "role125" ) );
        Assert.assertTrue( roleMap.containsKey( "role126" ) );

        Assert.assertTrue( roleMap.containsKey( "role23" ) );
        Assert.assertTrue( roleMap.containsKey( "role24" ) );
        Assert.assertTrue( roleMap.containsKey( "role25" ) );
        Assert.assertTrue( roleMap.containsKey( "role26" ) );
    }

    private Map<String, PlexusRole> getMapFromList( Set<PlexusRole> roles )
    {
        Map<String, PlexusRole> roleMap = new HashMap<String, PlexusRole>();
        for ( PlexusRole role : roles )
        {
            roleMap.put( role.getRoleId(), role );
        }
        return roleMap;
    }

}
