package org.sonatype.spice.interactive.interpolation;

public class Variable {
	private String name;
	private String description;
	private String defaultValue;
	private String userValue;
	
	public Variable(String[] values) {
		switch (values.length) {
			case 3:
				description = values[2];
			case 2:
				defaultValue = values[1];
			case 1:
				name = values[0];
		}
	}
	
	public Variable(String name, String defaultValue, String description) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.description = description;
	}
	
	/**
	 * The name of the variable to be replaced in the file
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Return the description of the variable or null
	 */
	public String getDescription() {
		return description;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public String getValue() {
		return userValue;
	}

	/**
	 * Change the value for this variable.
	 */
	public void setValue(String value) {
		this.userValue = value;
	}
}
