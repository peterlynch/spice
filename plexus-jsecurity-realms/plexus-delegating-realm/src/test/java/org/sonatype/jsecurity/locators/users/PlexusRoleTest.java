package org.sonatype.jsecurity.locators.users;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;

public class PlexusRoleTest
    extends PlexusTestCase
{

    public void testCompareDifferentId()
    {
        PlexusRole roleA = new PlexusRole();
        roleA.setName( "ID1" );
        roleA.setRoleId( "ID1" );
        roleA.setSource( "source" );

        PlexusRole roleB = new PlexusRole();
        roleB.setName( "ID2" );
        roleB.setRoleId( "ID2" );
        roleB.setSource( "source" );
        
        Assert.assertEquals( -1, roleA.compareTo( roleB ) );
        Assert.assertEquals( 1, roleB.compareTo( roleA ) );

    }
    
    public void testCompareDifferentSource()
    {
        PlexusRole roleA = new PlexusRole();
        roleA.setName( "ID1" );
        roleA.setRoleId( "ID1" );
        roleA.setSource( "source1" );

        PlexusRole roleB = new PlexusRole();
        roleB.setName( "ID1" );
        roleB.setRoleId( "ID1" );
        roleB.setSource( "source2" );
        
        Assert.assertEquals( -1, roleA.compareTo( roleB ) );
        Assert.assertEquals( 1, roleB.compareTo( roleA ) );

    }

}
