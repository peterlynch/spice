package org.sonatype.jsecurity.realms.validator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.codehaus.plexus.PlexusTestCase;
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

        this.configurationValidator = ( ConfigurationValidator ) lookup( ConfigurationValidator.ROLE );
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
}
