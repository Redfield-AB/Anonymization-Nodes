package se.redfield.arxnode.anonymize;

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

public abstract class Pseudoanonymizer {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(Pseudoanonymizer.class);

	private ColumnRearranger columnRearranger;

	public Pseudoanonymizer(PseudoAnonymizerNodeConfig config, DataTableSpec spec) {
		this.columnRearranger = createRearranger(config, spec);
	}

	private ColumnRearranger createRearranger(PseudoAnonymizerNodeConfig config, DataTableSpec spec) {
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
				List<DataCell> newCells = new ArrayList<DataCell>();
				for (int i = 0; i < indexes.length; i++) {
					String value = getSaltedValue(row.getCell(indexes[i]), row);

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

	protected abstract String getSaltedValue(DataCell cell, DataRow row);

	public ColumnRearranger getColumnRearranger() {
		return columnRearranger;
	}

	public BufferedDataTable process(ExecutionContext exec, BufferedDataTable inTable)
			throws CanceledExecutionException {
		return exec.createColumnRearrangeTable(inTable, getColumnRearranger(), exec);
	}

	public static Pseudoanonymizer create(PseudoAnonymizerNodeConfig config, DataTableSpec spec) {
		switch (config.getSaltingMode()) {
		case NONE:
			return new NoSaltPseudoanonymizer(config, spec);
		case RANDOM:
			return new RandomSaltPseudoanonymizer(config, spec);
		case COLUMN:
			return new ColumnSaltPseudoanonymizer(config, spec);
		}
		return null;
	}

	private static class NoSaltPseudoanonymizer extends Pseudoanonymizer {

		public NoSaltPseudoanonymizer(PseudoAnonymizerNodeConfig config, DataTableSpec spec) {
			super(config, spec);
		}

		@Override
		protected String getSaltedValue(DataCell cell, DataRow row) {
			return Utils.toString(cell);
		}

	}

	private static class RandomSaltPseudoanonymizer extends Pseudoanonymizer {

		private Random random;

		public RandomSaltPseudoanonymizer(PseudoAnonymizerNodeConfig config, DataTableSpec spec) {
			super(config, spec);
			this.random = new Random(config.getRandomSeed().getLongValue());
		}

		@Override
		protected String getSaltedValue(DataCell cell, DataRow row) {
			return Utils.toString(cell) + random.nextLong();
		}

	}

	private static class ColumnSaltPseudoanonymizer extends Pseudoanonymizer {

		private int saltIdx;

		public ColumnSaltPseudoanonymizer(PseudoAnonymizerNodeConfig config, DataTableSpec spec) {
			super(config, spec);
			saltIdx = spec.findColumnIndex(config.getSaltColumn().getStringValue());
		}

		@Override
		protected String getSaltedValue(DataCell cell, DataRow row) {
			return Utils.toString(cell) + Utils.toString(row.getCell(saltIdx));
		}

	}
}
