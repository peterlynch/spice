package org.sonatype.jsecurity.locators.users;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;

@Component(role=PlexusUserLocator.class, hint="MockUserLocatorA")
public class MockUserLocatorA
    extends AbstractTestUserLocator
{

    public String getSource()
    {
        return "MockUserLocatorA";
    }

    public Set<PlexusUser> listUsers()
    {
        Set<PlexusUser> users = new HashSet<PlexusUser>();
        
        PlexusUser a = new PlexusUser();
        a.setName( "Joe Coder" );
        a.setEmailAddress( "jcoder@sonatype.org" );
        a.setSource( this.getSource() );
        a.setUserId( "jcoder" );
        a.addRole( this.createFakeRole( "Role1" ) );
        a.addRole( this.createFakeRole( "Role2" ) );
        a.addRole( this.createFakeRole( "Role3" ) );
        
        PlexusUser b = new PlexusUser();
        b.setName( "Christine H. Dugas" );
        b.setEmailAddress( "cdugas@sonatype.org" );
        b.setSource( this.getSource() );
        b.setUserId( "cdugas" );
        b.addRole( this.createFakeRole( "Role2" ) );
        b.addRole( this.createFakeRole( "Role3" ) );
        
        PlexusUser c = new PlexusUser();
        c.setName( "Patricia P. Peralez" );
        c.setEmailAddress( "pperalez@sonatype.org" );
        c.setSource( this.getSource() );
        c.setUserId( "pperalez" );
        c.addRole( this.createFakeRole( "Role1" ) );
        c.addRole( this.createFakeRole( "Role2" ) );

        PlexusUser d = new PlexusUser();
        d.setName( "Danille S. Knudsen" );
        d.setEmailAddress( "dknudsen@sonatype.org" );
        d.setSource( this.getSource() );
        d.setUserId( "dknudsen" );
        d.addRole( this.createFakeRole( "Role4" ) );
        d.addRole( this.createFakeRole( "Role2" ) );
        
        users.add( a );
        users.add( b );
        users.add( c );
        users.add( d );
        
        return users;
    }

    @Override
    public boolean isPrimary()
    {
        return true;
    }

    public Set<PlexusRole> getUsersAdditinalRoles( String userId )
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    

}