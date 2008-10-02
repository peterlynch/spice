package org.sonatype.jsecurity.realms.validator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.jsecurity.model.io.xpp3.SecurityConfigurationXpp3Reader;

public class DefaultConfigurationValidatorTest
    extends PlexusTestCase
{
    protected ConfigurationValidator configurationValidator;

    public void setUp()
        throws Exception
    {
        super.setUp();

        this.configurationValidator = ( ConfigurationValidator ) lookup( ConfigurationValidator.class );
    }

    protected Configuration getConfigurationFromStream( InputStream is )
        throws Exception
    {
        SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();

        Reader fr = new InputStreamReader( is );

        return reader.read( fr );
    }

    public void testBad1()
        throws Exception
    {
        ValidationResponse response = configurationValidator.validateModel( new ValidationRequest(
            getConfigurationFromStream( getClass().getResourceAsStream(
                "/org/sonatype/jsecurity/configuration/validator/security-bad1.xml" ) ) ) );

        assertFalse( response.isValid() );
        
        assertFalse( response.isModified() );

        // emails are not longer unique!
        assertEquals( 11, response.getValidationErrors().size() );
        
        assertEquals( 0, response.getValidationWarnings().size() );
    }

    public void testBad2()
        throws Exception
    {
        ValidationResponse response = configurationValidator.validateModel( new ValidationRequest(
            getConfigurationFromStream( getClass().getResourceAsStream(
                "/org/sonatype/jsecurity/configuration/validator/security-bad2.xml" ) ) ) );

        assertFalse( response.isValid() );
        
        assertTrue( response.isModified() );

        assertEquals( 3, response.getValidationWarnings().size() );
        
        assertEquals( 11, response.getValidationErrors().size() );
    }
    
    public void testBad3()
        throws Exception
    {
        ValidationResponse response = configurationValidator.validateModel( new ValidationRequest(
            getConfigurationFromStream( getClass().getResourceAsStream(
                "/org/sonatype/jsecurity/configuration/validator/security-bad3.xml" ) ) ) );
    
        assertFalse( response.isValid() );
        
        assertTrue( response.isModified() );
    
        assertEquals( 3, response.getValidationWarnings().size() );
        
        assertEquals( 4, response.getValidationErrors().size() );
    }
    
    public void testRoles()
        throws Exception
    {
        ValidationContext context = new ValidationContext();
        
        CPrivilege priv = new CPrivilege();
        priv.setId( "priv" );
        priv.setName( "priv" );
        priv.setType( "invalid" );        
        context.addExistingPrivilegeIds();        
        context.getExistingPrivilegeIds().add( "priv" );
        
        CRole role1 = new CRole();
        role1.setId( "role1" );
        role1.setName( "role1" );
        role1.setDescription( "desc" );
        role1.setSessionTimeout( 50 );
        role1.addPrivilege( priv.getId() );
        role1.addRole( "role2" );        
        ArrayList<String> containedRoles = new ArrayList<String>();
        containedRoles.add( "role2" );        
        context.addExistingRoleIds();
        context.getExistingRoleIds().add( "role1" );
        context.getRoleContainmentMap().put( "role1", containedRoles );
        
        CRole role2 = new CRole();
        role2.setId( "role2" );
        role2.setName( "role2" );
        role2.setDescription( "desc" );
        role2.setSessionTimeout( 50 );
        role2.addPrivilege( priv.getId() );
        role2.addRole( "role3" );
        containedRoles = new ArrayList<String>();
        containedRoles.add( "role3" );        
        context.addExistingRoleIds();
        context.getExistingRoleIds().add( "role2" );
        context.getRoleContainmentMap().put( "role2", containedRoles );
        
        CRole role3 = new CRole();
        role3.setId( "role3" );
        role3.setName( "role3" );
        role3.setDescription( "desc" );
        role3.setSessionTimeout( 50 );
        role3.addPrivilege( priv.getId() );
        role3.addRole( "role1" );
        containedRoles = new ArrayList<String>();
        containedRoles.add( "role1" );        
        context.addExistingRoleIds();
        context.getExistingRoleIds().add( "role3" );
        context.getRoleContainmentMap().put( "role3", containedRoles );
        
        ValidationResponse vr = configurationValidator.validateRoleContainment( context );
        
        assertFalse( vr.isValid() );
        assertEquals( vr.getValidationErrors().size(), 3);
        
    }
}
