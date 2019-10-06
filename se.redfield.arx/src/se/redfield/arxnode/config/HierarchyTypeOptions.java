/*
 * Copyright (c) 2019 Redfield AB.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *  
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
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
