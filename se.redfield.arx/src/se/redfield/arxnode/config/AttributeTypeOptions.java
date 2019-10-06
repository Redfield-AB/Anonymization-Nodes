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

import org.deidentifier.arx.AttributeType;

import se.redfield.arxnode.util.TitledEnum;

public enum AttributeTypeOptions implements TitledEnum {
	IDENTIFYING_ATTRIBUTE(AttributeType.IDENTIFYING_ATTRIBUTE, "Identifying"), //
	QUASI_IDENTIFYING_ATTRIBUTE(AttributeType.QUASI_IDENTIFYING_ATTRIBUTE, "Quasi-identifying"), //
	SENSITIVE_ATTRIBUTE(AttributeType.SENSITIVE_ATTRIBUTE, "Sensitive"), //
	INSENSITIVE_ATTRIBUTE(AttributeType.INSENSITIVE_ATTRIBUTE, "Insensitive");

	private AttributeType type;
	private String title;

	AttributeTypeOptions(AttributeType type, String title) {
		this.type = type;
		this.title = title;
	}

	public AttributeType getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public static String[] stringValues() {
		String[] result = new String[values().length];
		for (int i = 0; i < result.length; i++) {
			result[i] = values()[i].title;
		}
		return result;
	}

	public static AttributeTypeOptions fromName(String name) {
		return TitledEnum.fromString(values(), name, IDENTIFYING_ATTRIBUTE);
	}
}
