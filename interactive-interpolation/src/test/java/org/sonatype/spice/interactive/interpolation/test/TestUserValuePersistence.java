package org.sonatype.spice.interactive.interpolation.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.sonatype.spice.interactive.interpolation.Interpolator;
import org.sonatype.spice.interactive.interpolation.Variable;

public class TestUserValuePersistence extends TestCase {
	
	public void testUserValuePersistence() throws IOException, URISyntaxException {
		File userStorage = File.createTempFile(getName(), ".properties");
		File toReplaceInto = new File(new File(getClass().getResource("/interactive").toURI()), "file.txt");
		
		Interpolator i = new Interpolator(toReplaceInto, userStorage);
		Collection<Variable> vars = i.getVariables();
		for (Variable variable : vars) {
			if (variable.getName().equals("file"))
				variable.setValue("NEWVALUE");
		}

		i.replaceVariables();
		i.saveUserValues();
		
		assertPropertyFileContains(userStorage, "file", "NEWVALUE");
	}
	
	public void testExtractVariable() throws URISyntaxException {
		System.setProperty("my.sys.prop", "ANOTHERVALUE");
		File toReplaceInto = new File(new File(getClass().getResource("/extractVariable").toURI()), "file.txt");
		Interpolator i = new Interpolator(toReplaceInto, null);
		assertEquals(3, i.getVariables().size());
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
		}
	}
	
	public void testReplacementFromFile() throws URISyntaxException {
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
		FileInputStream is = null;
		try {
			try {
				is = new FileInputStream(f);
				p.load(is);
				assertEquals(value, p.getProperty(key));
			} finally {
				is.close();
			}
		} catch (IOException e) {
			fail("Expected value " + value + "could not be found in "
					+ f.getAbsolutePath());
		}
	}
	
	private StringBuffer readXML(File settingsXml) {
		StringBuffer buffer = new StringBuffer((int) settingsXml.length());
		BufferedReader reader = null;
		try {
			try {
				reader = new BufferedReader(new FileReader(settingsXml));
				String line = null;
				while ((line = reader.readLine()) != null) {
					buffer.append(line).append('\n');
				}
			} finally {
				if (reader != null)
					reader.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}
}
