package org.sonatype.jsecurity.realms.tools;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;

public class ObjectClonerTest
    extends PlexusTestCase
{
    public void testUserClone()
        throws Exception
    {
        CUser user = new CUser();
        user.setEmail( "email" );
        user.setId( "id" );
        user.setName( "name" );
        user.setPassword( "password" );
        user.setStatus( "status" );
        
        List<String> roles = new ArrayList<String>();
        
        roles.add( "role1" );
        roles.add( "role2" );
        
        user.setRoles( roles );
        
        CUser cloned = ObjectCloner.clone( user );
        
        assertTrue( cloned != null );
        assertTrue( cloned != user );
        assertTrue( cloned.getEmail().equals( user.getEmail() ) );
        assertTrue( cloned.getId().equals( user.getId() ) );
        assertTrue( cloned.getName().equals( user.getName() ) );
        assertTrue( cloned.getPassword().equals( user.getPassword() ) );
        assertTrue( cloned.getStatus().equals( user.getStatus() ) );
        
        assertTrue( cloned.getRoles() != null );
        assertTrue( cloned.getRoles() != user.getRoles() );
        assertTrue( cloned.getRoles().size() == 2 );
        assertTrue( cloned.getRoles().get( 0 ).equals( "role1" ) );
        assertTrue( cloned.getRoles().get( 1 ).equals( "role2" ) );
    }
    
    public void testRoleClone()
        throws Exception
    {
        CRole role = new CRole();
        role.setDescription( "description" );
        role.setId( "id" );
        role.setName( "name" );
        role.setSessionTimeout( 60 );
        
        List<String> roles = new ArrayList<String>();
        
        roles.add( "role1" );
        roles.add( "role2" );
        
        role.setRoles( roles );
        
        List<String> privs = new ArrayList<String>();
        
        privs.add( "priv1" );
        privs.add( "priv2" );
        
        role.setPrivileges( privs );
        
        CRole cloned = ObjectCloner.clone( role );
        
        assertTrue( cloned != null );
        assertTrue( cloned != role );
        assertTrue( cloned.getDescription().equals( role.getDescription() ) );
        assertTrue( cloned.getId().equals( role.getId() ) );
        assertTrue( cloned.getName().equals( role.getName() ) );
        assertTrue( cloned.getSessionTimeout() == role.getSessionTimeout() );
        
        assertTrue( cloned.getRoles() != null );
        assertTrue( cloned.getRoles() != role.getRoles() );
        assertTrue( cloned.getRoles().size() == 2 );
        assertTrue( cloned.getRoles().get( 0 ).equals( "role1" ) );
        assertTrue( cloned.getRoles().get( 1 ).equals( "role2" ) );
        
        assertTrue( cloned.getPrivileges() != null );
        assertTrue( cloned.getPrivileges() != role.getPrivileges() );
        assertTrue( cloned.getPrivileges().size() == 2 );
        assertTrue( cloned.getPrivileges().get( 0 ).equals( "priv1" ) );
        assertTrue( cloned.getPrivileges().get( 1 ).equals( "priv2" ) );
    }
    
    public void testPrivilegeClone()
        throws Exception
    {
        CPrivilege priv = new CPrivilege();
        priv.setDescription( "description" );
        priv.setId( "id" );
        priv.setName( "name" );
        priv.setType( "type" );
        
        List<CProperty> props = new ArrayList<CProperty>();
        
        CProperty prop1 = new CProperty();
        prop1.setKey( "key1" );
        prop1.setValue( "value1" );
        
        props.add( prop1 );
        
        CProperty prop2 = new CProperty();
        prop2.setKey( "key2" );
        prop2.setValue( "value2" );
        
        props.add( prop2 );
        
        priv.setProperties( props );
        
        CPrivilege cloned = ObjectCloner.clone( priv );
        
        assertTrue( cloned != null );
        assertTrue( cloned != priv );
        assertTrue( cloned.getDescription().equals( priv.getDescription() ) );
        assertTrue( cloned.getId().equals( priv.getId() ) );
        assertTrue( cloned.getName().equals( priv.getName() ) );
        assertTrue( cloned.getType().equals( priv.getType() ) );
        
        assertTrue( cloned.getProperties() != null );
        assertTrue( cloned.getProperties() != priv.getProperties() );
        assertTrue( cloned.getProperties().size() == 2 );
        assertTrue( cloned.getProperties().get( 0 ) != prop1 );
        assertTrue( ( ( CProperty) cloned.getProperties().get( 0 ) ).getKey().equals( "key1" ) );
        assertTrue( ( ( CProperty) cloned.getProperties().get( 0 ) ).getValue().equals( "value1" ) );
        assertTrue( cloned.getProperties().get( 1 ) != prop2 );
        assertTrue( ( ( CProperty) cloned.getProperties().get( 1 ) ).getKey().equals( "key2" ) );
        assertTrue( ( ( CProperty) cloned.getProperties().get( 1 ) ).getValue().equals( "value2" ) );
    }
}
