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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.MissingValue;
import org.knime.core.data.MissingValueException;
import org.knime.core.data.StringValue;
import org.knime.core.data.time.localdatetime.LocalDateTimeValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeLogger;

import se.redfield.arxnode.Utils;
import se.redfield.arxnode.anonymize.Anonymizer;

/**
 * Class for splitting one {@link DataTable} into multiple partitions converted
 * into arx {@link Data}
 *
 */
public abstract class Partitioner {
	private static final NodeLogger logger = NodeLogger.getLogger(Partitioner.class);

	protected int partsNum;

	/**
	 * @param partsNum Max number of partitions.
	 */
	public Partitioner(int partsNum) {
		this.partsNum = partsNum;
	}

	/**
	 * Performs partitioning.
	 * 
	 * @param source      Input data table.
	 * @param omitMissing Determines how to handle missing cells. When true rows
	 *                    containing missing cells are skipped. Otherwise exception
	 *                    will be thrown.
	 * @return Partitions.
	 */
	public List<Partition> partition(BufferedDataTable source, boolean omitMissing) {
		init(source);
		long index = 0;

		for (DataRow row : source) {
			Partition current = findTarget(row, index++);

			List<String> strings = readRow(row, omitMissing);
			if (strings != null) {
				strings.add(row.getKey().getString());
				current.getData().add(strings.toArray(new String[] {}));
			} else {
				current.getInfo().getOmittedRows().add(row.getKey().getString());
			}
		}
		List<Partition> result = getResult();
		for (Partition p : result) {
			p.getInfo().setRows(p.getData().getHandle().getNumRows());
		}
		return result;
	}

	/**
	 * Reads row into list of Strings.
	 * 
	 * @param row         Input data row.
	 * @param omitMissing Determines how to handle missing cells. When true rows
	 *                    containing missing cells are skipped. Otherwise exception
	 *                    will be thrown.
	 * @return List of string values from the row.
	 */
	private List<String> readRow(DataRow row, boolean omitMissing) {
		List<String> result = new ArrayList<>();
		for (DataCell cell : row) {
			if (cell.isMissing()) {
				if (omitMissing) {
					return null;
				} else {
					throw new MissingValueException((MissingValue) cell,
							"Table contains missing value at row: " + row.getKey());
				}
			}
			result.add(Utils.toString(cell));
		}
		return result;
	}

	/**
	 * Creates {@link DefaultData} instance from {@link BufferedDataTable}
	 * 
	 * @param inTable Input table.
	 * @return Arx data object.
	 */
	protected DefaultData createData(BufferedDataTable inTable) {
		DefaultData defData = Data.create();
		String[] columnNames = inTable.getDataTableSpec().getColumnNames();
		defData.add((String[]) ArrayUtils.add(columnNames, Anonymizer.ROW_KEY));

		defData.getDefinition().setDataType(Anonymizer.ROW_KEY, org.deidentifier.arx.DataType.STRING);
		defData.getDefinition().setAttributeType(Anonymizer.ROW_KEY, AttributeType.INSENSITIVE_ATTRIBUTE);

		for (int i = 0; i < columnNames.length; i++) {
			DataType type = inTable.getDataTableSpec().getColumnSpec(columnNames[i]).getType();
			defData.getDefinition().setDataType(columnNames[i], Utils.knimeToArxType(type));
		}

		return defData;
	}

	/**
	 * Method called before partitioning starts.
	 * 
	 * @param source Input table.
	 */
	protected abstract void init(BufferedDataTable source);

	/**
	 * Finds a partition for a given row.
	 * 
	 * @param row   Input row.
	 * @param index Row index.
	 * @return Partition.
	 */
	protected abstract Partition findTarget(DataRow row, long index);

	/**
	 * @return Partitions.
	 */
	protected abstract List<Partition> getResult();

	/**
	 * Factory method for creating partitioner based on selected column type and
	 * partitions count.
	 * 
	 * @param partsNum Partitions count.
	 * @param column   Column name specified if 'Group by column' mode is used.
	 * @param table    Input table.
	 * @return Partitioner instance.
	 */
	public static Partitioner createPartitioner(int partsNum, String column, BufferedDataTable table) {
		if (partsNum > 1) {
			if (StringUtils.isEmpty(column)) {
				return new EqualSizePartitioner(partsNum);
			}

			Class<? extends DataCell> cellClass = table.getDataTableSpec().getColumnSpec(column).getType()
					.getCellClass();
			if (LocalDateTimeValue.class.isAssignableFrom(cellClass)) {
				return new LocalDateTimeColumnPartitioner(column, partsNum);
			}
			if (StringValue.class.isAssignableFrom(cellClass)) {
				return new StringColumnPartitioner(column, partsNum);
			}
			if (DoubleValue.class.isAssignableFrom(cellClass)) {
				return new DoubleColumnPartitioner(column, partsNum);
			}
			logger.warn("Unknown cell class: " + cellClass.getName());

		}
		return new SingleTablePartitoner();
	}

}
