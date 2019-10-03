package se.redfield.arxnode.config;

import java.util.Arrays;
import java.util.List;

import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.time.localdate.LocalDateValue;
import org.knime.core.data.time.localdatetime.LocalDateTimeValue;

import se.redfield.arxnode.util.TitledEnum;

public enum HierarchyTypeOptions implements TitledEnum {
	DATE("Use dates (for dates)", LocalDateTimeValue.class, LocalDateValue.class), //
	INTERVAL("Use intervals (for values with ratio scale)", DoubleValue.class, LocalDateTimeValue.class,
			LocalDateValue.class), //
	ORDER("Use ordering (e.g. for variables with ordinal scale)", DataValue.class), //
	MASKING("Use masking (e.g. for alphanumeric strings)", DataValue.class);
	private String title;
	private List<Class<? extends DataValue>> dataClasses;

	@SafeVarargs
	private HierarchyTypeOptions(String title, Class<? extends DataValue>... classes) {
		this.title = title;
		this.dataClasses = Arrays.asList(classes);
	}

	public boolean isCompatible(DataType type) {
		for (Class<? extends DataValue> c : dataClasses) {
			if (type.isCompatible(c)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getTitle() {
		return title;
	}

	public static HierarchyTypeOptions fromName(String str) {
		return TitledEnum.fromString(values(), str, MASKING);
	}
}
