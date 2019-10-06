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
package se.redfield.arxnode.partiton;

import org.knime.core.node.BufferedDataTable;

public abstract class ColumnPartitioner extends Partitioner {

	protected String column;
	protected int columnIndex;

	public ColumnPartitioner(String column, int partsNum) {
		super(partsNum);
		this.column = column;
	}

	@Override
	protected void init(BufferedDataTable source) {
		columnIndex = source.getDataTableSpec().findColumnIndex(column);
	}
}
