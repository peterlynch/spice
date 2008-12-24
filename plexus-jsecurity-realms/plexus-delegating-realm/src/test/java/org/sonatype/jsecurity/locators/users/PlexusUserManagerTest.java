package org.sonatype.jsecurity.locators.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;

public class PlexusUserManagerTest
    extends PlexusTestCase
{

    private PlexusUserManager getUserManager()
        throws Exception
    {
        return (PlexusUserManager) this.lookup( PlexusUserManager.class );
    }

    public void testAllUsers()
        throws Exception
    {
        PlexusUserManager userManager = this.getUserManager();
        Set<PlexusUser> users = userManager.listUsers( PlexusUserManager.SOURCE_ALL );
        Assert.assertFalse( users.isEmpty() );

        Assert.assertEquals( 9, users.size() );

        // put users in map for easy search
        Map<String, PlexusUser> userMap = this.getMapFromSet( users );

        // now check all of the users
        Assert.assertTrue( userMap.containsKey( "jcoder" ) );
        Assert.assertTrue( userMap.containsKey( "cdugas" ) );
        Assert.assertTrue( userMap.containsKey( "pperalez" ) );
        Assert.assertTrue( userMap.containsKey( "dknudsen" ) );

        Assert.assertTrue( userMap.containsKey( "bburton" ) );
        Assert.assertTrue( userMap.containsKey( "jblevins" ) );
        Assert.assertTrue( userMap.containsKey( "ksimmons" ) );
        Assert.assertTrue( userMap.containsKey( "fdahmen" ) );
        Assert.assertTrue( userMap.containsKey( "jcodar" ) );
    }

//    public void testExternalRolesForUserInListSourceAll()
//        throws Exception
//    {
//        PlexusUserManager userManager = this.getUserManager();
//
//        Set<PlexusUser> users = userManager.listUsers( PlexusUserManager.SOURCE_ALL );
//        // put users in map for easy search
//        Map<String, PlexusUser> userMap = this.getMapFromSet( users );
//        Assert.assertTrue( userMap.containsKey( "jcoder" ) );
//        PlexusUser jcoder = userMap.get( "jcoder" );
//
//        this.checkJCodersRoles( jcoder );
//    }
//
//    public void testExternalRolesForUserInListSingleSource()
//        throws Exception
//    {
//        PlexusUserManager userManager = this.getUserManager();
//
//        Set<PlexusUser> users = userManager.listUsers( "MockUserLocatorA" );
//        // put users in map for easy search
//        Map<String, PlexusUser> userMap = this.getMapFromSet( users );
//        Assert.assertTrue( userMap.containsKey( "jcoder" ) );
//        PlexusUser jcoder = userMap.get( "jcoder" );
//
//        this.checkJCodersRoles( jcoder );
//    }
//
//    public void testExternalRolesForUser()
//        throws Exception
//    {
//        PlexusUserManager userManager = this.getUserManager();
//        PlexusUser jcoder = userManager.getUser( "jcoder" );
//
//        this.checkJCodersRoles( jcoder );
//    }
//
//    public void testExternalRolesForUserSearchSingleSource()
//        throws Exception
//    {
//        PlexusUserManager userManager = this.getUserManager();
//
//        Set<PlexusUser> users = userManager.searchUserById( "MockUserLocatorA", "jcoder" );
//        // put users in map for easy search
//        Map<String, PlexusUser> userMap = this.getMapFromSet( users );
//        Assert.assertTrue( userMap.containsKey( "jcoder" ) );
//        PlexusUser jcoder = userMap.get( "jcoder" );
//
//        this.checkJCodersRoles( jcoder );
//    }
//
//    public void testExternalRolesForUserSearchSourceAll()
//        throws Exception
//    {
//        PlexusUserManager userManager = this.getUserManager();
//
//        Set<PlexusUser> users = userManager.searchUserById( PlexusUserManager.SOURCE_ALL, "jcoder" );
//        // put users in map for easy search
//        Map<String, PlexusUser> userMap = this.getMapFromSet( users );
//        Assert.assertTrue( userMap.containsKey( "jcoder" ) );
//        PlexusUser jcoder = userMap.get( "jcoder" );
//
//        this.checkJCodersRoles( jcoder );
//    }

    private void checkJCodersRoles( PlexusUser jcoder )
    {
        List<String> roleIds = new ArrayList<String>();
        for ( PlexusRole role : jcoder.getRoles() )
        {
            roleIds.add( role.getRoleId() );
        }

        Assert.assertTrue( roleIds.contains( "Role1" ) );
        Assert.assertTrue( roleIds.contains( "Role2" ) );
        Assert.assertTrue( roleIds.contains( "Role3" ) );
        Assert.assertTrue( roleIds.contains( "ExtraRole1" ) );
        Assert.assertTrue( roleIds.contains( "ExtraRole1" ) );

        Assert.assertEquals( "RoleIds: " + roleIds, 5, jcoder.getRoles().size() );
    }

    public void testSearch()
        throws Exception
    {
        PlexusUserManager userManager = this.getUserManager();
        Set<PlexusUser> users = userManager.searchUserById( "pperalez", PlexusUserManager.SOURCE_ALL );
        Assert.assertEquals( 1, users.size() );
        Assert.assertEquals( "pperalez", users.iterator().next().getUserId() );

        users = userManager.searchUserById( "ppera", PlexusUserManager.SOURCE_ALL );
        Assert.assertEquals( 1, users.size() );
        Assert.assertEquals( "pperalez", users.iterator().next().getUserId() );

        users = userManager.searchUserById("ppera",  "MockUserLocatorB" );
        Assert.assertEquals( 0, users.size() );

        users = userManager.searchUserById( "ksim", "MockUserLocatorB" );
        Assert.assertEquals( 1, users.size() );
        Assert.assertEquals( "ksimmons", users.iterator().next().getUserId() );

        users = userManager.searchUserById( "jcod", PlexusUserManager.SOURCE_ALL );
        Assert.assertEquals( 2, users.size() );

        // put users in map for easy search
        Map<String, PlexusUser> userMap = this.getMapFromSet( users );

        Assert.assertTrue( userMap.containsKey( "jcodar" ) );
        Assert.assertTrue( userMap.containsKey( "jcoder" ) );

    }

    private Map<String, PlexusUser> getMapFromSet( Set<PlexusUser> users )
    {
        Map<String, PlexusUser> userMap = new HashMap<String, PlexusUser>();
        for ( PlexusUser plexusUser : users )
        {
            userMap.put( plexusUser.getUserId(), plexusUser );
        }
        return userMap;
    }

}
