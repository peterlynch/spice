/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
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

    // public void testExternalRolesForUserInListSourceAll()
    // throws Exception
    // {
    // PlexusUserManager userManager = this.getUserManager();
    //
    // Set<PlexusUser> users = userManager.listUsers( PlexusUserManager.SOURCE_ALL );
    // // put users in map for easy search
    // Map<String, PlexusUser> userMap = this.getMapFromSet( users );
    // Assert.assertTrue( userMap.containsKey( "jcoder" ) );
    // PlexusUser jcoder = userMap.get( "jcoder" );
    //
    // this.checkJCodersRoles( jcoder );
    // }
    //
    // public void testExternalRolesForUserInListSingleSource()
    // throws Exception
    // {
    // PlexusUserManager userManager = this.getUserManager();
    //
    // Set<PlexusUser> users = userManager.listUsers( "MockUserLocatorA" );
    // // put users in map for easy search
    // Map<String, PlexusUser> userMap = this.getMapFromSet( users );
    // Assert.assertTrue( userMap.containsKey( "jcoder" ) );
    // PlexusUser jcoder = userMap.get( "jcoder" );
    //
    // this.checkJCodersRoles( jcoder );
    // }
    //
    // public void testExternalRolesForUser()
    // throws Exception
    // {
    // PlexusUserManager userManager = this.getUserManager();
    // PlexusUser jcoder = userManager.getUser( "jcoder" );
    //
    // this.checkJCodersRoles( jcoder );
    // }
    //
    // public void testExternalRolesForUserSearchSingleSource()
    // throws Exception
    // {
    // PlexusUserManager userManager = this.getUserManager();
    //
    // Set<PlexusUser> users = userManager.searchUserById( "MockUserLocatorA", "jcoder" );
    // // put users in map for easy search
    // Map<String, PlexusUser> userMap = this.getMapFromSet( users );
    // Assert.assertTrue( userMap.containsKey( "jcoder" ) );
    // PlexusUser jcoder = userMap.get( "jcoder" );
    //
    // this.checkJCodersRoles( jcoder );
    // }
    //
    // public void testExternalRolesForUserSearchSourceAll()
    // throws Exception
    // {
    // PlexusUserManager userManager = this.getUserManager();
    //
    // Set<PlexusUser> users = userManager.searchUserById( PlexusUserManager.SOURCE_ALL, "jcoder" );
    // // put users in map for easy search
    // Map<String, PlexusUser> userMap = this.getMapFromSet( users );
    // Assert.assertTrue( userMap.containsKey( "jcoder" ) );
    // PlexusUser jcoder = userMap.get( "jcoder" );
    //
    // this.checkJCodersRoles( jcoder );
    // }

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

    public void testSearchWithCriteria()
        throws Exception
    {

        PlexusUserSearchCriteria criteria = new PlexusUserSearchCriteria();
        
        PlexusUserManager userManager = this.getUserManager();
        criteria.setUserId( "pperalez" );
        Set<PlexusUser> users = userManager.searchUsers( criteria, PlexusUserManager.SOURCE_ALL );
        Assert.assertEquals( 1, users.size() );
        Assert.assertEquals( "pperalez", users.iterator().next().getUserId() );

        criteria.setUserId( "ppera" );
        users = userManager.searchUsers( criteria, PlexusUserManager.SOURCE_ALL );
        Assert.assertEquals( 1, users.size() );
        Assert.assertEquals( "pperalez", users.iterator().next().getUserId() );

        criteria.setUserId( "ppera" );
        users = userManager.searchUsers( criteria, "MockUserLocatorB" );
        Assert.assertEquals( 0, users.size() );

        criteria.setUserId( "ksim" );
        users = userManager.searchUsers( criteria, "MockUserLocatorB" );
        Assert.assertEquals( 1, users.size() );
        Assert.assertEquals( "ksimmons", users.iterator().next().getUserId() );

        criteria.setUserId( "jcod" );
        users = userManager.searchUsers( criteria, PlexusUserManager.SOURCE_ALL );
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
