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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.StringValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeLogger;

/**
 * Grouping partitioner for a String columns. Finds all distinct values and
 * creates partition for each value.
 *
 */
public class StringColumnPartitioner extends ColumnPartitioner {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(StringColumnPartitioner.class);

	private Map<String, Partition> partitions;

	public StringColumnPartitioner(String column, int partsNum) {
		super(column, partsNum);
	}

	@Override
	protected void init(BufferedDataTable source) {
		super.init(source);
		partitions = new HashMap<>();
		for (DataRow r : source) {
			String val = getValue(r);
			if (!partitions.containsKey(val)) {
				partitions.put(val, new Partition(createData(source)));
			}
			if (partitions.size() > partsNum) {
				throw new IllegalArgumentException(
						String.format("Unable to partition table by [%s]. Column has more than %d distinct values",
								column, partsNum));
			}
		}
	}

	/**
	 * Gets String cell values from a given row.
	 * 
	 * @param row Input row.
	 * @return String value.
	 */
	private String getValue(DataRow row) {
		DataCell cell = row.getCell(columnIndex);
		if (cell.isMissing()) {
			return "?";
		}
		return ((StringValue) row.getCell(columnIndex)).getStringValue();
	}

	@Override
	protected Partition findTarget(DataRow row, long index) {
		return partitions.get(getValue(row));
	}

	@Override
	protected List<Partition> getResult() {
		return partitions.entrySet().stream().map(e -> fillCriteria(e.getValue(), e.getKey()))
				.collect(Collectors.toList());
	}

	/**
	 * Fills partitioning criteria fields for a given partition.
	 * 
	 * @param p   Partition.
	 * @param key Cell value of the partition.
	 * @return Partition.
	 */
	private Partition fillCriteria(Partition p, String key) {
		p.getInfo().setCriteria(String.format("%s is %s", column, key));
		return p;
	}

}
