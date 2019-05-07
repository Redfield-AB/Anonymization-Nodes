package se.redfield.arxnode.config;

import org.knime.core.data.DataValue;
import org.knime.core.data.DoubleValue;

import se.redfield.arxnode.util.TitledEnum;

public enum HierarchyTypeOptions implements TitledEnum {
	// DATE("Use dates (for dates)"), //
	INTERVAL("Use intervals (for values with ratio scale)", DoubleValue.class), //
	// ORDER("Use ordering (e.g. for variables with ordinal scale)"), //
	MASKING("Use masking (e.g. for alphanumeric strings)", DataValue.class);
	private String title;
	private Class<? extends DataValue> dataClass;

	private HierarchyTypeOptions(String title, Class<? extends DataValue> dataClass) {
		this.title = title;
		this.dataClass = dataClass;
	}

	public Class<? extends DataValue> getDataClass() {
		return dataClass;
	}

	@Override
	public String getTitle() {
		return title;
	}

	public static HierarchyTypeOptions fromName(String str) {
		return TitledEnum.fromString(values(), str, MASKING);
	}
}
