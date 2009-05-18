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
package org.sonatype.security.locators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserStatus;

@Component(role=UserManager.class, hint="MockUserLocatorB")
public class MockUserManagerB
    extends AbstractTestUserManager
{

    public String getSource()
    {
        return "MockUserLocatorB";
    }

    public Set<User> listUsers()
    {
        Set<User> users = new HashSet<User>();
        
        DefaultUser a = new DefaultUser();
        a.setName( "Brenda D. Burton" );
        a.setEmailAddress( "bburton@sonatype.org" );
        a.setSource( this.getSource() );
        a.setUserId( "bburton" );
        a.setStatus( UserStatus.active );
        a.addRole( this.createFakeRole( "RoleA" ) );
        a.addRole( this.createFakeRole( "RoleB" ) );
        a.addRole( this.createFakeRole( "RoleC" ) );
        
        DefaultUser b = new DefaultUser();
        b.setName( "Julian R. Blevins" );
        b.setEmailAddress( "jblevins@sonatype.org" );
        b.setSource( this.getSource() );
        b.setUserId( "jblevins" );
        b.setStatus( UserStatus.active );
        b.addRole( this.createFakeRole( "RoleA" ) );
        b.addRole( this.createFakeRole( "RoleB" ) );
        
        DefaultUser c = new DefaultUser();
        c.setName( "Kathryn J. Simmons" );
        c.setEmailAddress( "ksimmons@sonatype.org" );
        c.setSource( this.getSource() );
        c.setUserId( "ksimmons" );
        c.setStatus( UserStatus.active );
        c.addRole( this.createFakeRole( "RoleA" ) );
        c.addRole( this.createFakeRole( "RoleB" ) );

        DefaultUser d = new DefaultUser();
        d.setName( "Florence T. Dahmen" );
        d.setEmailAddress( "fdahmen@sonatype.org" );
        d.setSource( this.getSource() );
        d.setUserId( "fdahmen" );
        d.setStatus( UserStatus.active );
        d.addRole( this.createFakeRole( "RoleA" ) );
        d.addRole( this.createFakeRole( "RoleB" ) );
        
        DefaultUser e = new DefaultUser();
        e.setName( "Jill  Codar" );
        e.setEmailAddress( "jcodar@sonatype.org" );
        e.setSource( this.getSource() );
        e.setUserId( "jcodar" );
        e.setStatus( UserStatus.active );
        
//        DefaultUser f = new DefaultUser();
//        f.setName( "Joe Coder" );
//        f.setEmailAddress( "jcoder@sonatype.org" );
//        f.setSource( this.getSource() );
//        f.setUserId( "jcoder" );
//        f.addRole( this.createFakeRole( "Role1" ) );
//        f.addRole( this.createFakeRole( "Role2" ) );
//        f.addRole( this.createFakeRole( "Role3" ) );
        
        users.add( a );
        users.add( b );
        users.add( c );
        users.add( d );
        users.add( e );
//        users.add( f );
        
        return users;
    }

    public Set<Role> getUsersAdditinalRoles( String userId )
    {
        
        Map<String, Set<Role>> userToRoleMap = new HashMap<String, Set<Role>>();
        
        Set<Role> roles = new HashSet<Role>();
        
        Role role1 = new Role();
        role1.setSource( this.getSource() );
        role1.setName( "ExtraRole1" );
        role1.setRoleId( "ExtraRole1" );
        
        Role role2 = new Role();
        role2.setSource( this.getSource() );
        role2.setName( "ExtraRole2" );
        role2.setRoleId( "ExtraRole2" );
        
        roles.add( role1 );
        roles.add( role2 );
        userToRoleMap.put( "jcoder", roles );
        
        return userToRoleMap.get( userId );
    }

    public String getAuthenticationRealmName()
    {
        return null;
    }

}
