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
package se.redfield.arxnode.config.pmodels;

import se.redfield.arxnode.util.TitledEnum;

public enum LDiversityVariant implements TitledEnum {
	DISTINCT("Distinct-l-diversity", false, true), //
	SHANNON_ENTROPY("Shannon-entropy-l-diversity", false, false), //
	GRASSBERGER_ENTROPY("Grassberger-entropy-l-diversity", false, false), //
	RECURSIVE("Recursive-(c,l)-diversity", true, true);

	private String title;
	private boolean hasC;
	private boolean intParam;

	private LDiversityVariant(String title, boolean hasC, boolean intParam) {
		this.title = title;
		this.hasC = hasC;
		this.intParam = intParam;
	}

	public boolean isHasC() {
		return hasC;
	}

	public boolean isIntParam() {
		return intParam;
	}

	@Override
	public String toString() {
		return title;
	}

	@Override
	public String getTitle() {
		return title;
	}

	public static LDiversityVariant fromString(String str) {
		return TitledEnum.fromString(values(), str, DISTINCT);
	}
}
