package org.sonatype.buup.cfgfiles;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.codehaus.plexus.util.FileUtils;

public class PropertiesFileTest
    extends TestCase
{
    private PropertiesFile propertiesFile;

    protected PropertiesFile prepareCase( String caseNum )
        throws IOException
    {
        File basedir = new File( "target/test-classes/plexus/" + caseNum );

        File source = new File( basedir, "plexus.properties" );

        FileUtils.copyFile( new File( "src/test/resources/plexus/" + caseNum + "/plexus.properties" ), source );

        propertiesFile = new DefaultPropertiesFile( source );

        return propertiesFile;
    }

    protected void validateCase( String caseNum )
        throws IOException
    {
        File source = new File( "target/test-classes/plexus/" + caseNum + "/plexus.properties" );

        File expectedResult = new File( "src/test/resources/plexus/" + caseNum + "/plexus.properties.result" );

        assertEquals( "Result does not match!", FileUtils.fileRead( expectedResult ), FileUtils.fileRead( source ) );
    }

    public void testCase01()
        throws Exception
    {
        PropertiesFile editor = prepareCase( "c01" );

        // just a check did we read all
        assertEquals( "The props file contents does not match!", 12, editor.getAllKeyValuePairs().size() );

        // "change" the property
        editor.setProperty( "application-host", "192.168.0.1" );
        // "change" the property
        editor.setProperty( "webapp-context-path", "/nexus-changed" );
        // "remove" the property
        editor.removeProperty( "runtime-tmp" );
        // "add" new property
        editor.setProperty( "foo", "bar" );

        editor.save();

        validateCase( "c01" );
    }
}
