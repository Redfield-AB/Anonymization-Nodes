package se.redfield.arxnode.partiton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.time.localdatetime.LocalDateTimeValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;

import se.redfield.arxnode.Utils;
import se.redfield.arxnode.anonymize.Anonymizer;
import se.redfield.arxnode.config.Config;

public abstract class Partitioner {
	private static final NodeLogger logger = NodeLogger.getLogger(Partitioner.class);

	protected int partsNum;

	public Partitioner(int partsNum) {
		this.partsNum = partsNum;
	}

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

	private List<String> readRow(DataRow row, boolean omitMissing) {
		List<String> result = new ArrayList<>();
		for (DataCell cell : row) {
			if (cell.isMissing()) {
				if (omitMissing) {
					return null;
				} else {
					throw new RuntimeException("Table contains missing value at row: " + row.getKey());
				}
			}
			result.add(Utils.toString(cell));
		}
		return result;
	}

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

	protected abstract void init(BufferedDataTable source);

	protected abstract Partition findTarget(DataRow row, long index);

	protected abstract List<Partition> getResult();

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

	public static BufferedDataTable[] test(BufferedDataTable table, Config config, ExecutionContext exec) {
		Partitioner p = Partitioner.createPartitioner(5, "age", table);
		Utils.time();
		List<Partition> list = p.partition(table, false);
		Utils.time("partition");
		for (Partition pair : list) {
			logger.info(pair.getInfo().getRows() + " " + pair.getInfo().getCriteria());
			Iterator<String[]> iterator = pair.getData().getHandle().iterator();
			while (iterator.hasNext()) {
				String[] strings = (String[]) iterator.next();
				logger.info(strings[pair.getData().getHandle().getColumnIndexOf("age")]);
			}

		}
		return null;
	}
}
