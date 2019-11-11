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

import java.util.Arrays;
import java.util.List;

import org.deidentifier.arx.Data;
import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;

/**
 * Single table partitioner. Creates only a single partitioner. Used when
 * partitioning is disabled just for converting {@link BufferedDataTable} into
 * {@link Data}.
 *
 */
public class SingleTablePartitoner extends Partitioner {

	private Partition data;

	public SingleTablePartitoner() {
		super(1);
	}

	@Override
	protected void init(BufferedDataTable source) {
		data = new Partition(createData(source));
	}

	@Override
	protected Partition findTarget(DataRow row, long index) {
		return data;
	}

	@Override
	protected List<Partition> getResult() {
		return Arrays.asList(data);
	}

}
