package org.sonatype.spice.interactive.interpolation;

public class Variable {
	public static final String FILE = "file";
	public static final String NUMBER = "number";
	public static final String STRING = "string";
	public static final String PASSWORD = "password";

	private String name;
	private String description;
	private String defaultValue;
	private String userValue;
	private String type;

	public Variable(String[] values) {
		switch (values.length) {
			case 4:
				type = values[3];
			case 3:
				description = values[2];
			case 2:
				defaultValue = values[1];
			case 1:
				name = values[0];
		}
	}

	public Variable(String name, String defaultValue, String description, String type) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.description = description;
		this.type = type;
	}

	/**
	 * The name of the variable to be replaced in the file
	 */
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getValue() {
		return userValue;
	}

	public void setValue(String value) {
		this.userValue = value;
	}

	public String getType() {
		return type;
	}
}
