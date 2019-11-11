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

import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeLogger;

/**
 * Grouping partitioner for numeric columns. Calculates min and max values of
 * the column and then splits this range into specified number of intervals.
 * Each of the intervals corresponds to one partition.
 *
 */
public class DoubleColumnPartitioner extends ColumnPartitioner {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(DoubleColumnPartitioner.class);

	protected Double min;
	protected Double max;
	protected double intervalLength;
	private List<Partition> partitions;

	/**
	 * @param column   Grouping column name.
	 * @param partsNum Number of partitions.
	 */
	public DoubleColumnPartitioner(String column, int partsNum) {
		super(column, partsNum);
	}

	@Override
	protected void init(BufferedDataTable source) {
		super.init(source);
		for (DataRow r : source) {
			Double val = getValue(r);
			if (val != null) {
				if (min == null || val < min) {
					min = val;
				}
				if (max == null || val > max) {
					max = val;
				}
			}
		}
		intervalLength = (max - min) / (partsNum);
		partitions = new ArrayList<>();
		for (int i = 0; i < partsNum; i++) {
			partitions.add(new Partition(createData(source)));
		}
	}

	/**
	 * Reads cell value from a given row.
	 * 
	 * @param row Input row.
	 * @return Double value.
	 */
	protected Double getValue(DataRow row) {
		DataCell cell = row.getCell(columnIndex);
		if (cell.isMissing()) {
			return null;
		}
		return ((DoubleValue) row.getCell(columnIndex)).getDoubleValue();
	}

	@Override
	protected Partition findTarget(DataRow row, long index) {
		Double val = getValue(row);
		int idx = 0;
		if (val != null) {
			idx = Math.min((int) Math.floor((val - min) / intervalLength), partitions.size() - 1);
		}
		return partitions.get(idx);
	}

	@Override
	protected List<Partition> getResult() {
		int index = 0;
		for (Partition p : partitions) {
			String criteria = createCriteria(index++);
			p.getInfo().setCriteria(criteria);
		}
		return partitions;
	}

	/**
	 * Creates human readable criteria for a given partition
	 * 
	 * @param index partition index.
	 * @return String criteria.
	 */
	protected String createCriteria(int index) {
		return String.format("%s in [%.2f, %.2f%s", column, (min + intervalLength * index),
				(min + intervalLength * (index + 1)), index < partsNum - 1 ? ")" : "]");
	}
}
