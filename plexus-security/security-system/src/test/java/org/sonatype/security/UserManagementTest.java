package org.sonatype.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserSearchCriteria;

public class UserManagementTest
    extends AbstractSecurityTest
{

    public void testAllUsers()
        throws Exception
    {
        SecuritySystem securitySystem = this.getSecuritySystem();

        Set<User> users = securitySystem.listUsers();
        Assert.assertFalse( users.isEmpty() );

        // 2 different jcoders
        Assert.assertEquals( 10, users.size() );

        // put users in map for easy search
        Map<String, User> userMap = this.getMapFromSet( users );

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

        // we just need to check to make sure there are 2 jcoders with the correct source (the counts are already
        // checked above)
        this.verify2Jcoders( users );
    }

    public void testSearchWithCriteria()
        throws Exception
    {
        SecuritySystem securitySystem = this.getSecuritySystem();
        
        UserSearchCriteria criteria = new UserSearchCriteria();

        criteria.setUserId( "pperalez" );
        Set<User> users = securitySystem.searchUsers( criteria );
        Assert.assertEquals( 1, users.size() );
        Assert.assertEquals( "pperalez", users.iterator().next().getUserId() );

        criteria.setUserId( "ppera" );
        users = securitySystem.searchUsers( criteria );
        Assert.assertEquals( 1, users.size() );
        Assert.assertEquals( "pperalez", users.iterator().next().getUserId() );

        criteria.setUserId( "ppera" );
        criteria.setSource( "MockUserManagerB" );
        users = securitySystem.searchUsers( criteria );
        Assert.assertEquals( 0, users.size() );

        criteria.setUserId( "ksim" );
        users = securitySystem.searchUsers( criteria );
        Assert.assertEquals( 1, users.size() );
        Assert.assertEquals( "ksimmons", users.iterator().next().getUserId() );

        criteria.setUserId( "jcod" );
        criteria.setSource( null );
        users = securitySystem.searchUsers( criteria );
        Assert.assertEquals( 3, users.size() );

        // put users in map for easy search
        Map<String, User> userMap = this.getMapFromSet( users );

        Assert.assertTrue( userMap.containsKey( "jcodar" ) );

        // we just need to check to make sure there are 2 jcoders with the correct source (the counts are already
        // checked above)
        this.verify2Jcoders( users );

    }

    private Map<String, User> getMapFromSet( Set<User> users )
    {
        Map<String, User> userMap = new HashMap<String, User>();
        for ( User user : users )
        {
            userMap.put( user.getUserId(), user );
        }
        return userMap;
    }

    private void verify2Jcoders( Set<User> users )
    {
        Map<String, User> jcoders = new HashMap<String, User>();
        for ( User user : users )
        {
            if ( user.getUserId().equals( "jcoder" ) )
            {
                jcoders.put( user.getSource(), user );
            }
        }
        Assert.assertEquals( 2, jcoders.size() );
        Assert.assertTrue( jcoders.containsKey( "MockUserManagerA" ) );
        Assert.assertTrue( jcoders.containsKey( "MockUserManagerB" ) );
    }

}
