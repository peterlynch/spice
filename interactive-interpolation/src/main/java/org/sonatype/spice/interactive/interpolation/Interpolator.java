package org.sonatype.spice.interactive.interpolation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interpolator {
	private Map<String, Variable> variables = null;
	private File userValues;
	private File settingsXml;
	private Properties userFilledValues;

	public Interpolator(File pathToSettingsXML, File pathToUserValues) {
		if (pathToSettingsXML == null)
			new IllegalArgumentException("Path to settings.xml can't be null");
		if (!(pathToSettingsXML.exists() && pathToSettingsXML.isFile() && pathToSettingsXML.canRead()))
			new IllegalStateException("The file " + pathToSettingsXML.getAbsolutePath() + " cannot be read.");

		settingsXml = pathToSettingsXML;
		userValues = pathToUserValues;
		userFilledValues = loadUserValues();
		variables = extractVariables(readXML());
	}

	public Collection<Variable> getVariables() {
		return variables.values();
	}

	public void replaceVariables() {
		// save the new values into the user file
		saveUserValues();
		
		if (variables == null)
			loadUserValues();
		// replace the values in the XML file 
		writeXML(replaceVariable(variables, readXML()));
	}

	private String expandDefaultValue(String value) {
		if (value == null)
			return null;
		Pattern p = Pattern.compile("@([^@]*)@");
		Matcher m = p.matcher(value);
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			String newValue = System.getProperty(m.group(1));
			if (newValue != null)
				m.appendReplacement(sb, newValue);	
		}
		m.appendTail(sb);
		return sb.toString();
	}
	
	private Map<String, Variable> extractVariables(final StringBuffer in) {
		Map<String, Variable> vars = new HashMap<String, Variable>();
		Pattern p = Pattern.compile("%%([^%]*)%%");
		Matcher m = p.matcher(in);
		while(m.find()) {
			String[] segments = m.group(1).split("\\|");
			if (vars.get(segments[0]) != null)
				continue;
	
			String defaultValue = null;
			if (segments.length > 1)
				defaultValue = expandDefaultValue(segments[1]);
			
			Variable newVar = new Variable(segments[0], defaultValue, (segments.length > 2 && segments[2] != null) ? segments[2] : null );
			vars.put(segments[0],newVar);
			String persistedValue = userFilledValues.getProperty(segments[0]);
			if (persistedValue != null)
				newVar.setValue(persistedValue);
			else 
				newVar.setValue(defaultValue);
		}
		return vars;
	}
	

	private StringBuffer replaceVariable(final Map<String, Variable> replaceMap, final StringBuffer in) {
		Pattern p = Pattern.compile("%%([^%]*)%%");
		Matcher m = p.matcher(in);
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			String newValue = replaceMap.get(m.group(1).split("\\|")[0]).getValue();
			if (newValue != null)
				m.appendReplacement(sb, Matcher.quoteReplacement(newValue));	
		}
		m.appendTail(sb);
		return sb;
	}
	
	//save the user values into a property file
	public void saveUserValues() {
		if (userValues == null)
			return;
		if (!userValues.exists()) {
            File m2 = userValues.getParentFile();
            if ( !m2.exists() && !m2.mkdirs() )
                return;
		}
		
		//Store the new values for the variables in the property file
		for (Variable variable : variables.values()) {
			if (variable.getValue() != null)
				userFilledValues.setProperty(variable.getName(), variable.getValue());
		}
		
		//Save
		try {
			OutputStream out = null;
			try {
				out = new BufferedOutputStream(new FileOutputStream(userValues));
				userFilledValues.store(out, null);
			} finally {
				if (out != null)
					out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//load the user values into a property file
	private Properties loadUserValues() {
		if (userValues == null)
			return new Properties();
		Properties values = new Properties();
		try {
			InputStream is = null;
			try {
				is = new BufferedInputStream(new FileInputStream(userValues));
				values.load(is);
			} finally {
				if (is != null)
					is.close();
			}
		} catch (IOException e) {
			return new Properties();
		}
		return values;
	}

	private StringBuffer readXML() {
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
	
	private void writeXML(StringBuffer b) {
		BufferedWriter writer = null;
		try {
			try {
				writer = new BufferedWriter(new FileWriter(settingsXml));
				writer.append(b);
			} finally {
				if (writer != null)
					writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
