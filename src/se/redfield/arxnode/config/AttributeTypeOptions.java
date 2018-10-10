package se.redfield.arxnode.config;

import org.deidentifier.arx.AttributeType;

public enum AttributeTypeOptions {
	IDENTIFYING_ATTRIBUTE(AttributeType.IDENTIFYING_ATTRIBUTE), //
	QUASI_IDENTIFYING_ATTRIBUTE(AttributeType.QUASI_IDENTIFYING_ATTRIBUTE), //
	SENSITIVE_ATTRIBUTE(AttributeType.SENSITIVE_ATTRIBUTE), //
	INSENSITIVE_ATTRIBUTE(AttributeType.INSENSITIVE_ATTRIBUTE);

	private AttributeType type;

	AttributeTypeOptions(AttributeType type) {
		this.type = type;
	}

	public AttributeType getType() {
		return type;
	}

	public static String[] stringValues() {
		String[] result = new String[values().length];
		for (int i = 0; i < result.length; i++) {
			result[i] = values()[i].name();
		}
		return result;
	}

}
