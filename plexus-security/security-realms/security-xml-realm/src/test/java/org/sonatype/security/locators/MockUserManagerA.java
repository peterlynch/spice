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

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserStatus;

@Component(role=UserManager.class, hint="MockUserManagerA")
public class MockUserManagerA
    extends AbstractTestUserManager
{

    public static final String SOURCE = "MockUserManagerA";
    
    public String getSource()
    {
        return SOURCE;
    }

    public Set<User> listUsers()
    {
        Set<User> users = new HashSet<User>();
        
        DefaultUser a = new DefaultUser();
        a.setName( "Joe Coder" );
        a.setEmailAddress( "jcoder@sonatype.org" );
        a.setSource( this.getSource() );
        a.setUserId( "jcoder" );
        a.setStatus( UserStatus.active );
        a.addRole( this.createFakeRole( "RoleA" ) );
        a.addRole( this.createFakeRole( "RoleB" ) );
        a.addRole( this.createFakeRole( "RoleC" ) );
        
        DefaultUser b = new DefaultUser();
        b.setName( "Christine H. Dugas" );
        b.setEmailAddress( "cdugas@sonatype.org" );
        b.setSource( this.getSource() );
        b.setUserId( "cdugas" );
        b.setStatus( UserStatus.active );
        b.addRole( this.createFakeRole( "RoleA" ) );
        b.addRole( this.createFakeRole( "RoleB" ) );
        b.addRole( this.createFakeRole( "Role1" ) );
        
        DefaultUser c = new DefaultUser();
        c.setName( "Patricia P. Peralez" );
        c.setEmailAddress( "pperalez@sonatype.org" );
        c.setSource( this.getSource() );
        c.setUserId( "pperalez" );
        c.setStatus( UserStatus.active );

        DefaultUser d = new DefaultUser();
        d.setName( "Danille S. Knudsen" );
        d.setEmailAddress( "dknudsen@sonatype.org" );
        d.setSource( this.getSource() );
        d.setUserId( "dknudsen" );
        d.setStatus( UserStatus.active );
        
        users.add( a );
        users.add( b );
        users.add( c );
        users.add( d );
        
        return users;
    }

    public String getAuthenticationRealmName()
    {
        return null;
    }
  
}