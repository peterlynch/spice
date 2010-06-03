package org.sonatype.spice.interactive.interpolation.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.sonatype.spice.interactive.interpolation.Interpolator;

public class TestUserValuePersistence extends TestCase {
//	public void testUserValuePersistence() throws IOException {
//		File userStorage = File.createTempFile(getName(), ".properties");
//		File settingsFile = new File("/Users/Pascal/dev/s2n/s2-aggregator/onboarding/com.sonatype.settings.interpolation/src/com/sonatype/internal/settings/interpolation/test/file.txt");
//		
//		Interpolator i = new Interpolator(settingsFile, userStorage);
//		Collection<Variable> vars = i.getVariables();
//		for (Variable variable : vars) {
//			if (variable.getName().equals("file"))
//				variable.setValue("NEWVALUE");
//		}
//
//		i.replaceVariables();
//		i.saveUserValues();
//		
//		Properties p = new Properties();
//		FileInputStream is = null;
//		try {
//			is = new FileInputStream(userStorage);
//			p.load(is);
//			for (Entry<Object, Object> property : p.entrySet()) {
//				assertTrue(property.getValue().equals("NEWVALUE"));
//			}
//		} finally {
//			is.close();
//			userStorage.delete();
//		}
//	}
	
	public void testReplacementFromFile() throws URISyntaxException {
		File toReplaceInto = new File(new File(getClass().getResource("/prefilled").toURI()), "file.txt");
		File userStorage = new File(new File(getClass().getResource("/prefilled").toURI()), "values.properties");
		
		Interpolator i = new Interpolator(toReplaceInto, userStorage);
		i.replaceVariables();
		assertPropertyFileContains(userStorage, "var2", "formatted");
		assertPropertyFileContains(userStorage, "notUsed", "valueNotUsed");
		assertPropertyFileContains(userStorage, "file", "FileName");
	}
	//Count the number of vars
	//Verify persisted
	//validate the new contnet of the file
	
	//Reset default
	//expansion of java.properties
	//check simple replacement without loading values
	
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
}
