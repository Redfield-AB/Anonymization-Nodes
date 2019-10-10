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
package se.redfield.arxnode.anonymize;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;

import se.redfield.arxnode.Utils;
import se.redfield.arxnode.config.PseudoAnonymizerNodeConfig;

public class Pseudoanonymizer {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(Pseudoanonymizer.class);

	private PseudoAnonymizerNodeConfig config;
	private ColumnRearranger dataTableRearranger;
	private DataTableSpec hashesTableSpec;
	private SaltProvider saltProvider;

	public Pseudoanonymizer(PseudoAnonymizerNodeConfig config, DataTableSpec spec) {
		this.config = config;
		this.dataTableRearranger = createDataTableRearranger(spec);
		this.hashesTableSpec = createHashesTableSpec(spec);
		this.saltProvider = createSaltProvider(spec);
	}

	private SaltProvider createSaltProvider(DataTableSpec spec) {
		switch (config.getSaltingMode()) {
		case RANDOM:
			return new RandomSaltProvider(config);
		case COLUMN:
			if (config.getSaltColumn().useRowID()) {
				return new RowIdSaltProvider();
			} else {
				return new ColumnSaltProvider(config, spec);
			}
		case TIMESTAMP:
			return new TimestampSaltProvider(config);
		case NONE:
			break;
		}
		return row -> "";
	}

	private ColumnRearranger createDataTableRearranger(DataTableSpec spec) {
		List<String> includeList = config.getSelectedColumns();
		int[] indexes = new int[includeList.size()];
		List<DataColumnSpec> newSpecs = new ArrayList<>();

		for (int i = 0; i < includeList.size(); i++) {
			String name = includeList.get(i);
			indexes[i] = spec.findColumnIndex(name);
			newSpecs.add(new DataColumnSpecCreator(name, StringCell.TYPE).createSpec());
		}

		boolean debugMode = config.getDebugMode().getBooleanValue();
		CellFactory factory = new AbstractCellFactory(newSpecs.toArray(new DataColumnSpec[] {})) {
			@Override
			public DataCell[] getCells(DataRow row) {
				List<DataCell> newCells = new ArrayList<>();
				String salt = saltProvider.getSalt(row);

				for (int i = 0; i < indexes.length; i++) {
					String value = Utils.toString(row.getCell(indexes[i])) + salt;

					if (debugMode) {
						newCells.add(new StringCell(value));
					} else {
						String hash = DigestUtils.shaHex(value);
						newCells.add(new StringCell(hash));
					}
				}
				return newCells.toArray(new DataCell[] {});
			}
		};

		ColumnRearranger rearranger = new ColumnRearranger(spec);
		rearranger.replace(factory, indexes);
		return rearranger;
	}

	private DataTableSpec createHashesTableSpec(DataTableSpec spec) {
		List<String> includeList = config.getSelectedColumns();
		List<DataColumnSpec> newSpecs = new ArrayList<>();

		for (String name : includeList) {
			newSpecs.add(spec.getColumnSpec(name));
			newSpecs.add(new DataColumnSpecCreator(name + "_anonymized", StringCell.TYPE).createSpec());
		}

		return new DataTableSpec(newSpecs.toArray(new DataColumnSpec[] {}));
	}

	private BufferedDataTable createHashesTable(BufferedDataTable inTable, BufferedDataTable outTable,
			ExecutionContext exec) {
		BufferedDataContainer container = exec.createDataContainer(hashesTableSpec);

		List<String> includeList = config.getSelectedColumns();
		int[] indexes = new int[includeList.size()];

		for (int i = 0; i < includeList.size(); i++) {
			String name = includeList.get(i);
			indexes[i] = inTable.getDataTableSpec().findColumnIndex(name);
		}

		CloseableRowIterator inIter = inTable.iterator();
		CloseableRowIterator outIter = outTable.iterator();

		while (inIter.hasNext() && outIter.hasNext()) {
			DataRow inRow = inIter.next();
			DataRow outRow = outIter.next();

			List<DataCell> cells = new ArrayList<>();

			for (int i = 0; i < indexes.length; i++) {
				cells.add(inRow.getCell(indexes[i]));
				cells.add(outRow.getCell(indexes[i]));
			}

			DataRow combinedRow = new DefaultRow(inRow.getKey(), cells.toArray(new DataCell[] {}));
			container.addRowToTable(combinedRow);
		}
		inIter.close();
		outIter.close();
		container.close();
		return container.getTable();
	}

	public ColumnRearranger getDataTableRearranger() {
		return dataTableRearranger;
	}

	public DataTableSpec getHashesTableSpec() {
		return hashesTableSpec;
	}

	public BufferedDataTable[] process(ExecutionContext exec, BufferedDataTable inTable)
			throws CanceledExecutionException {
		BufferedDataTable outTable = exec.createColumnRearrangeTable(inTable, getDataTableRearranger(), exec);
		return new BufferedDataTable[] { outTable, createHashesTable(inTable, outTable, exec) };
	}

	private interface SaltProvider {
		public String getSalt(DataRow row);
	}

	private class RandomSaltProvider implements SaltProvider {

		private Random random;

		public RandomSaltProvider(PseudoAnonymizerNodeConfig config) {
			this.random = new Random();
			if (config.getUseSeed().getBooleanValue()) {
				random.setSeed(config.getRandomSeed().getLongValue());
			}
		}

		@Override
		public String getSalt(DataRow row) {
			return String.valueOf(random.nextLong());
		}

	}

	private class ColumnSaltProvider implements SaltProvider {
		private int saltIdx;

		public ColumnSaltProvider(PseudoAnonymizerNodeConfig config, DataTableSpec spec) {
			saltIdx = spec.findColumnIndex(config.getSaltColumn().getStringValue());
		}

		@Override
		public String getSalt(DataRow row) {
			return Utils.toString(row.getCell(saltIdx));
		}
	}

	private class RowIdSaltProvider implements SaltProvider {

		@Override
		public String getSalt(DataRow row) {
			return row.getKey().toString();
		}

	}

	private class TimestampSaltProvider implements SaltProvider {

		private String salt;

		public TimestampSaltProvider(PseudoAnonymizerNodeConfig config) {
			LocalDateTime timestamp = config.getAutoTimestamp().getBooleanValue() ? LocalDateTime.now()
					: config.getTimestamp().getLocalDateTime();
			salt = timestamp.format(DateTimeFormatter.ISO_DATE_TIME);
		}

		@Override
		public String getSalt(DataRow row) {
			return salt;
		}

	}
}
