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
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;

import se.redfield.arxnode.Utils;
import se.redfield.arxnode.config.PseudoAnonymizerNodeConfig;
import se.redfield.arxnode.config.PseudoAnonymizerNodeConfig.ReplaceMode;

public class Pseudoanonymizer {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(Pseudoanonymizer.class);

	private ColumnRearranger columnRearranger;
	private SaltProvider saltProvider;

	public Pseudoanonymizer(PseudoAnonymizerNodeConfig config, DataTableSpec spec) {
		this.columnRearranger = createRearranger(config, spec);
		this.saltProvider = createSaltProvider(config, spec);
	}

	private SaltProvider createSaltProvider(PseudoAnonymizerNodeConfig config, DataTableSpec spec) {
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

	private ColumnRearranger createRearranger(PseudoAnonymizerNodeConfig config, DataTableSpec spec) {
		List<String> includeList = config.getSelectedColumns();
		int[] indexes = new int[includeList.size()];
		List<DataColumnSpec> newSpecs = new ArrayList<>();

		for (int i = 0; i < includeList.size(); i++) {
			String name = includeList.get(i);
			indexes[i] = spec.findColumnIndex(name);

			String newName = name;
			if (config.getReplaceMode() == ReplaceMode.APPEND) {
				newName += "-anonymized";
			}
			newSpecs.add(new DataColumnSpecCreator(newName, StringCell.TYPE).createSpec());
		}

		boolean debugMode = config.getDebugMode().getBooleanValue();
		CellFactory factory = new AbstractCellFactory(newSpecs.toArray(new DataColumnSpec[] {})) {
			@Override
			public DataCell[] getCells(DataRow row) {
				List<DataCell> newCells = new ArrayList<DataCell>();
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
		if (config.getReplaceMode() == ReplaceMode.REPLACE) {
			rearranger.replace(factory, indexes);
		} else {
			rearranger.append(factory);
		}
		return rearranger;
	}

	public ColumnRearranger getColumnRearranger() {
		return columnRearranger;
	}

	public BufferedDataTable process(ExecutionContext exec, BufferedDataTable inTable)
			throws CanceledExecutionException {
		return exec.createColumnRearrangeTable(inTable, getColumnRearranger(), exec);
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
