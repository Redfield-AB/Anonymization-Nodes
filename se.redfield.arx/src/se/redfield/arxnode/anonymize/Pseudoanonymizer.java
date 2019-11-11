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

/**
 * Class performing pseudoanonymization - hashing selected columns with
 * different salting modes.
 *
 */
public class Pseudoanonymizer {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(Pseudoanonymizer.class);

	private PseudoAnonymizerNodeConfig config;
	private ColumnRearranger dataTableRearranger;
	private DataTableSpec hashesTableSpec;
	private SaltProvider saltProvider;

	/**
	 * Creates instance
	 * 
	 * @param config node config
	 * @param spec   input data tabple spec
	 */
	public Pseudoanonymizer(PseudoAnonymizerNodeConfig config, DataTableSpec spec) {
		this.config = config;
		this.dataTableRearranger = createDataTableRearranger(spec);
		this.hashesTableSpec = createHashesTableSpec(spec);
		this.saltProvider = createSaltProvider(spec);
	}

	/**
	 * Creates salt provider
	 * 
	 * @param spec input data table spec
	 * @return {@link SaltProvider} instance according to the salting type specified
	 *         in node config
	 */
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

	/**
	 * Creates column rearranger instance.
	 * 
	 * @param spec Input table spec.
	 * @return Column rearranger.
	 */
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

	/**
	 * Creates output hashes table spec. This table contains only selected columns
	 * alongside with hashed values.
	 * 
	 * @param spec Input data table spec.
	 * @return Output hashes table spec.
	 */
	private DataTableSpec createHashesTableSpec(DataTableSpec spec) {
		List<String> includeList = config.getSelectedColumns();
		List<DataColumnSpec> newSpecs = new ArrayList<>();

		for (String name : includeList) {
			newSpecs.add(spec.getColumnSpec(name));
			newSpecs.add(new DataColumnSpecCreator(name + "_anonymized", StringCell.TYPE).createSpec());
		}

		return new DataTableSpec(newSpecs.toArray(new DataColumnSpec[] {}));
	}

	/**
	 * Creates output hashes table.
	 * 
	 * @param inTable  Input data table.
	 * @param outTable Ouput data table.
	 * @param exec     Execution context.
	 * @return Hashes output table.
	 */
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

	/**
	 * @return dataTableRearranger
	 */
	public ColumnRearranger getDataTableRearranger() {
		return dataTableRearranger;
	}

	/**
	 * @return Hashes Table Spec.
	 */
	public DataTableSpec getHashesTableSpec() {
		return hashesTableSpec;
	}

	/**
	 * Performs anonymization.
	 * 
	 * @param exec    Execution context.
	 * @param inTable Input data table.
	 * @return Ouput tables. One is data table with selected columns replaced with
	 *         hashes, second one is hashes table containing only selected columns
	 *         alongside with hashes
	 * @throws CanceledExecutionException
	 */
	public BufferedDataTable[] process(ExecutionContext exec, BufferedDataTable inTable)
			throws CanceledExecutionException {
		BufferedDataTable outTable = exec.createColumnRearrangeTable(inTable, getDataTableRearranger(), exec);
		return new BufferedDataTable[] { outTable, createHashesTable(inTable, outTable, exec) };
	}

	/**
	 * Interface for salting provider.
	 */
	private interface SaltProvider {
		/**
		 * @param row Data table row.
		 * @return Salt value for the provided row.
		 */
		public String getSalt(DataRow row);
	}

	/**
	 * Salt provider class for random salting mode.
	 *
	 */
	private class RandomSaltProvider implements SaltProvider {

		private Random random;

		/**
		 * @param config node config
		 */
		public RandomSaltProvider(PseudoAnonymizerNodeConfig config) {
			this.random = new Random();
			if (config.getUseSeed().getBooleanValue()) {
				random.setSeed(config.getRandomSeed().getLongValue());
			}
		}

		/**
		 * @return Random long salt value.
		 */
		@Override
		public String getSalt(DataRow row) {
			return String.valueOf(random.nextLong());
		}

	}

	/**
	 * Salt provider class for column mode. Only used when a regular column is
	 * selected.
	 */
	private class ColumnSaltProvider implements SaltProvider {
		private int saltIdx;

		/**
		 * @param config Node config.
		 * @param spec   Input table spec.
		 */
		public ColumnSaltProvider(PseudoAnonymizerNodeConfig config, DataTableSpec spec) {
			saltIdx = spec.findColumnIndex(config.getSaltColumn().getStringValue());
		}

		/**
		 * @return Salt value from the selected column.
		 */
		@Override
		public String getSalt(DataRow row) {
			return Utils.toString(row.getCell(saltIdx));
		}
	}

	/**
	 * Salt provider class for column mode when RowId column is selected
	 *
	 */
	private class RowIdSaltProvider implements SaltProvider {

		/**
		 * @return RowId value for the provided row.
		 */
		@Override
		public String getSalt(DataRow row) {
			return row.getKey().toString();
		}

	}

	/**
	 * Salt provider class for timistamp salting mode.
	 *
	 */
	private class TimestampSaltProvider implements SaltProvider {

		private String salt;

		/**
		 * @param config Node config.
		 */
		public TimestampSaltProvider(PseudoAnonymizerNodeConfig config) {
			LocalDateTime timestamp = config.getAutoTimestamp().getBooleanValue() ? LocalDateTime.now()
					: config.getTimestamp().getLocalDateTime();
			salt = timestamp.format(DateTimeFormatter.ISO_DATE_TIME);
		}

		/**
		 * @return Timestamp value.
		 */
		@Override
		public String getSalt(DataRow row) {
			return salt;
		}

	}
}
