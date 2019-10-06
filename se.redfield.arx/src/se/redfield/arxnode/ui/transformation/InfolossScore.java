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
package se.redfield.arxnode.ui.transformation;

import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.metric.InformationLoss;

public class InfolossScore implements Comparable<InfolossScore> {

	private String value;
	private double relative;

	public InfolossScore(String value, double relative) {
		this.value = value;
		this.relative = relative;
	}

	public String getValue() {
		return value;
	}

	public double getRelative() {
		return relative;
	}

	public double getRelativePercent() {
		return relative * 100d;
	}

	@Override
	public int compareTo(InfolossScore o) {
		return Double.compare(relative, o.relative);
	}

	@Override
	public String toString() {
		return String.format("%s [%.2f%%]", value, relative * 100d);
	}

	public static InfolossScore createFrom(ARXLattice lattice, InformationLoss<?> infoloss) {
		return new InfolossScore(infoloss.toString(),
				infoloss.relativeTo(lattice.getLowestScore(), lattice.getHighestScore()));
	}

}
