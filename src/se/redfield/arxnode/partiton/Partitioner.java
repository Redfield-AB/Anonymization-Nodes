package se.redfield.arxnode.partiton;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.Pair;

import se.redfield.arxnode.Utils;
import se.redfield.arxnode.config.Config;

public abstract class Partitioner {
	private static final NodeLogger logger = NodeLogger.getLogger(Partitioner.class);

	protected int partsNum;

	public Partitioner(int partsNum) {
		this.partsNum = partsNum;
	}

	public List<Pair<DefaultData, PartitionInfo>> partition(BufferedDataTable source) {
		init(source);
		long index = 0;
		for (DataRow row : source) {
			DefaultData current = findTarget(row, index++);
			// logger.info("adding row");
			current.add(
					row.stream().map(cell -> cell.toString()).collect(Collectors.toList()).toArray(new String[] {}));
		}
		return getResult();
	}

	protected DefaultData createData(BufferedDataTable inTable) {
		DefaultData defData = Data.create();
		String[] columnNames = inTable.getDataTableSpec().getColumnNames();
		defData.add(columnNames);
		for (int i = 0; i < columnNames.length; i++) {
			DataType type = inTable.getDataTableSpec().getColumnSpec(columnNames[i]).getType();
			defData.getDefinition().setDataType(columnNames[i], Utils.knimeToArxType(type));
		}

		return defData;
	}

	protected abstract void init(BufferedDataTable source);

	protected abstract DefaultData findTarget(DataRow row, long index);

	protected abstract List<Pair<DefaultData, PartitionInfo>> getResult();

	public static Partitioner createPartitioner(int partsNum, String column, BufferedDataTable table) {
		if (partsNum > 1) {
			if (StringUtils.isEmpty(column)) {
				return new EqualSizePartitioner(partsNum);
			}

			Class<? extends DataCell> cellClass = table.getDataTableSpec().getColumnSpec(column).getType()
					.getCellClass();
			if (StringValue.class.isAssignableFrom(cellClass)) {
				return new StringColumnPartitioner(column, partsNum);
			} else {
				logger.warn("Unknown cell class: " + cellClass.getName());
			}
		}
		return new SingleTablePartitoner();
	}

	public static BufferedDataTable[] test(BufferedDataTable table, Config config, ExecutionContext exec) {
		Partitioner p = Partitioner.createPartitioner(5, "familystate", table);
		Utils.time();
		List<Pair<DefaultData, PartitionInfo>> list = p.partition(table);
		Utils.time("partition");
		for (Pair<DefaultData, PartitionInfo> pair : list) {
			logger.info(pair.getSecond().getRows() + " " + pair.getSecond().getCriteria());
		}
		return null;
	}
}
