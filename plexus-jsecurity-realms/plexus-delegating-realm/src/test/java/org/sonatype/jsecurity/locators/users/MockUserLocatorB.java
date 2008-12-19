package org.sonatype.jsecurity.locators.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.jsecurity.crypto.hash.Hash;

@Component(role=PlexusUserLocator.class, hint="MockUserLocatorB")
public class MockUserLocatorB
    extends AbstractTestUserLocator
{

    public String getSource()
    {
        return "MockUserLocatorB";
    }

    public Set<PlexusUser> listUsers()
    {
        Set<PlexusUser> users = new HashSet<PlexusUser>();
        
        PlexusUser a = new PlexusUser();
        a.setName( "Brenda D. Burton" );
        a.setEmailAddress( "bburton@sonatype.org" );
        a.setSource( this.getSource() );
        a.setUserId( "bburton" );
        a.addRole( this.createFakeRole( "Role1" ) );
        a.addRole( this.createFakeRole( "Role2" ) );
        a.addRole( this.createFakeRole( "Role3" ) );
        
        PlexusUser b = new PlexusUser();
        b.setName( "Julian R. Blevins" );
        b.setEmailAddress( "jblevins@sonatype.org" );
        b.setSource( this.getSource() );
        b.setUserId( "jblevins" );
        b.addRole( this.createFakeRole( "Role2" ) );
        b.addRole( this.createFakeRole( "Role3" ) );
        
        PlexusUser c = new PlexusUser();
        c.setName( "Kathryn J. Simmons" );
        c.setEmailAddress( "ksimmons@sonatype.org" );
        c.setSource( this.getSource() );
        c.setUserId( "ksimmons" );
        c.addRole( this.createFakeRole( "Role1" ) );
        c.addRole( this.createFakeRole( "Role2" ) );

        PlexusUser d = new PlexusUser();
        d.setName( "Florence T. Dahmen" );
        d.setEmailAddress( "fdahmen@sonatype.org" );
        d.setSource( this.getSource() );
        d.setUserId( "fdahmen" );
        d.addRole( this.createFakeRole( "Role4" ) );
        d.addRole( this.createFakeRole( "Role2" ) );
        
        PlexusUser e = new PlexusUser();
        e.setName( "Jill  Codar" );
        e.setEmailAddress( "jcodar@sonatype.org" );
        e.setSource( this.getSource() );
        e.setUserId( "jcodar" );
        e.addRole( this.createFakeRole( "Role1" ) );
        e.addRole( this.createFakeRole( "Role2" ) );
        e.addRole( this.createFakeRole( "Role3" ) );
        
//        PlexusUser f = new PlexusUser();
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

    public Set<PlexusRole> getUsersAdditinalRoles( String userId )
    {
        
        Map<String, Set<PlexusRole>> userToRoleMap = new HashMap<String, Set<PlexusRole>>();
        
        Set<PlexusRole> roles1 = new HashSet<PlexusRole>();
        
        roles1.add( new PlexusRole("ExtraRole1", "ExtraRole1", this.getSource()) );
        roles1.add( new PlexusRole("ExtraRole2", "ExtraRole2", this.getSource()) );
        userToRoleMap.put( "jcoder", roles1 );
        
        
        
        return userToRoleMap.get( userId );
    }

}