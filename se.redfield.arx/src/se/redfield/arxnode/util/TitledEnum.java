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
package se.redfield.arxnode.util;

import org.apache.commons.lang.StringUtils;
import org.knime.core.node.NodeLogger;

/**
 * Base interface for a enums with title. Provides a method to convert string
 * representation to a enum instance.
 *
 */
public interface TitledEnum {
	static final NodeLogger logger = NodeLogger.getLogger(TitledEnum.class);

	String getTitle();

	String name();

	/**
	 * Method to fetch enum instance from a string representation. Tries to match a
	 * given string as an index, then name, then a title of the enum object.
	 * 
	 * @param <E>    Enum class.
	 * @param values Enum values.
	 * @param str    String representation.
	 * @param def    Default values to be used if instance for a given string is not
	 *               found.
	 * @return enum instance.
	 */
	static <E extends TitledEnum> E fromString(E[] values, String str, E def) {
		if (!StringUtils.isEmpty(str)) {
			try {
				int index = Integer.parseInt(str);
				return values[index];
			} catch (Exception e) {
				// ignore
			}
			for (E val : values) {
				if (str.equals(val.name()) || str.equals(val.getTitle())) {
					return val;
				}
			}
		}
		logger.warn(String.format("Value for '%s' not found. Using default: %s", str, def));
		return def;
	}

}
