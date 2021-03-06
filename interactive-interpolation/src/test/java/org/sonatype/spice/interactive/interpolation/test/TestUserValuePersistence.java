package org.sonatype.spice.interactive.interpolation.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Properties;

import junit.framework.TestCase;

import org.sonatype.spice.interactive.interpolation.Interpolator;
import org.sonatype.spice.interactive.interpolation.Variable;

public class TestUserValuePersistence extends TestCase {
	
	public void testUserValuePersistence() throws IOException, URISyntaxException {
		File userStorage = File.createTempFile(getName(), ".properties");
        userStorage.deleteOnExit();
		File toReplaceInto = new File(new File(getClass().getResource("/interactive").toURI()), "file.txt");
		
		Interpolator i = new Interpolator(toReplaceInto, userStorage);
		Collection<Variable> vars = i.getVariables();
		assertEquals(3, vars.size());
		for (Variable variable : vars) {
			if (variable.getName().equals("file"))
            {
				variable.setValue("c:\\foo\\bar");
            }
			if (variable.getName().equals("varContainingXMLChars"))
            {
				variable.setValue("<Value with XML characters>");
            }
		}

		i.replaceVariables();
		i.saveUserValues();
		
		assertTrue(readXML(toReplaceInto).toString().contains("&lt;Value with XML characters&gt"));
		assertPropertyFileContains(userStorage, "file", "c:\\foo\\bar");
	}
	
	public void testExtractVariable() throws URISyntaxException {
		System.setProperty("my.sys.prop", "ANOTHERVALUE");
		File toReplaceInto = new File(new File(getClass().getResource("/extractVariable").toURI()), "file.txt");
		Interpolator i = new Interpolator(toReplaceInto, null);
		assertEquals(9, i.getVariables().size());
		Collection<Variable> v = i.getVariables();
		for (Variable variable : v) {
			if(variable.getName().equals("file")) {
				assertEquals("descriptionVar1", variable.getDescription());
				assertEquals("defaultValue", variable.getDefaultValue());
			}
			if (variable.getName().equals("formatted")) {
				assertNull(variable.getDescription());
				assertNull(variable.getDefaultValue());
			}
			if (variable.getName().equals("varSysProperty")) {
				assertEquals("ANOTHERVALUE/foobar", variable.getDefaultValue());
			}
			if (variable.getName().equals("varWithNoDefaultNoDescription")) {
				assertNull(variable.getDescription());
				assertNull(variable.getDefaultValue());
			}
			if (variable.getName().equals("varWithDefaultNoDescription")) {
				assertNull(variable.getDescription());
				assertEquals("default", variable.getDefaultValue());
				assertEquals("default", variable.getValue());
			}
			if (variable.getName().equals("varWithNoDefaultButWithDescription")) {
				assertEquals("description", variable.getDescription());
			}
			if (variable.getName().equals("pwdVar")) {
				assertEquals("default", variable.getDefaultValue());
				assertEquals("description", variable.getDescription());
				assertEquals("password", variable.getType());
			}
			if (variable.getName().equals("pwdVar2")) {
				assertEquals("password", variable.getType());
			}
		}
	}
	
    public void testReplacementFromFile()
        throws Exception
    {
		File toReplaceInto = new File(new File(getClass().getResource("/prefilled").toURI()), "file.txt");
		File userStorage = new File(new File(getClass().getResource("/prefilled").toURI()), "values.properties");
		
		Interpolator i = new Interpolator(toReplaceInto, userStorage);
		
		i.replaceVariables();
		
		//Verify that unused values don't get lost
		assertPropertyFileContains(userStorage, "var2", "formatted");
		assertPropertyFileContains(userStorage, "notUsed", "valueNotUsed");
		assertPropertyFileContains(userStorage, "file", "FileName");
	
		String modified = readXML(toReplaceInto).toString();
		assertTrue(modified.contains("%%formatted%%"));
		assertFalse(modified.contains("%%file"));
	}
	
	private void assertPropertyFileContains(File f, String key, String value) {
		Properties p = new Properties();
		try {
            FileInputStream is = new FileInputStream( f );
			try {
				p.load(is);
				assertEquals(value, p.getProperty(key));
			} finally {
                if ( is != null )
                {
                    is.close();
                }
			}
		} catch (IOException e) {
            throw new RuntimeException( "Expected value " + value + "could not be found in " + f.getAbsolutePath(), e );
		}
	}

    private StringBuffer readXML( File settingsXml )
        throws IOException
    {
        StringBuffer buffer = new StringBuffer( (int) settingsXml.length() );
        BufferedReader reader = new BufferedReader( new FileReader( settingsXml ) );
        try
        {
            String line = null;
            while ( ( line = reader.readLine() ) != null )
            {
                buffer.append( line ).append( '\n' );
            }
        }
        finally
        {
            if ( reader != null )
            {
                reader.close();
            }
        }
        return buffer;
    }
}
