package org.sonatype.spice.interactive.interpolation.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.Map.Entry;

import org.sonatype.spice.interactive.interpolation.Interpolator;
import org.sonatype.spice.interactive.interpolation.Variable;


import junit.framework.TestCase;

public class TestUserValuePersistence extends TestCase {
	public void testUserValuePersistence() throws IOException {
		File userStorage = File.createTempFile(getName(), ".properties");
		File settingsFile = new File("/Users/Pascal/dev/s2n/s2-aggregator/onboarding/com.sonatype.settings.interpolation/src/com/sonatype/internal/settings/interpolation/test/file.txt");
		
		Interpolator i = new Interpolator(settingsFile, userStorage);
		Collection<Variable> vars = i.getVariables();
		for (Variable variable : vars) {
			if (variable.getName().equals("file"))
				variable.setValue("NEWVALUE");
		}

		i.replaceVariables();
		i.saveUserValues();
		
		Properties p = new Properties();
		FileInputStream is = null;
		try {
			is = new FileInputStream(userStorage);
			p.load(is);
			for (Entry<Object, Object> property : p.entrySet()) {
				assertTrue(property.getValue().equals("NEWVALUE"));
			}
		} finally {
			is.close();
			userStorage.delete();
		}
	}
	
//	public void testReplacementFromFile() {
//		File userStorage = new File("/Users/Pascal/dev/s2n/s2-aggregator/onboarding/com.sonatype.settings.interpolation/src/com/sonatype/internal/settings/interpolation/test/default-values.properties");
//		File settingsFile = new File("/Users/Pascal/dev/s2n/s2-aggregator/onboarding/com.sonatype.settings.interpolation/src/com/sonatype/internal/settings/interpolation/test/file.txt");
//		
//		Interpolator i = new Interpolator(settingsFile, userStorage);
//		i.replaceVariables();
//	}
	//Count the number of vars
	//Verify persisted
	//validate the new contnet of the file
	
	//Reset default
	//expansion of java.properties
	
	//check simple replacement without loading values
}
