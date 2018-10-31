package se.redfield.arxnode.partiton;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.Data.DefaultData;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.Pair;

public class DoubleColumnPartitioner extends ColumnPartitioner {
	private static final NodeLogger logger = NodeLogger.getLogger(DoubleColumnPartitioner.class);

	private Double min;
	private Double max;
	private double intervalLength;
	private List<DefaultData> partitions;

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
		logger.info("min: " + min);
		logger.info("max: " + max);
		intervalLength = (max - min) / (partsNum);
		logger.info("interval length = " + intervalLength);
		partitions = new ArrayList<>();
		for (int i = 0; i < partsNum; i++) {
			partitions.add(createData(source));
		}
	}

	private Double getValue(DataRow row) {
		DataCell cell = row.getCell(columnIndex);
		if (cell.isMissing()) {
			return null;
		}
		return ((DoubleValue) row.getCell(columnIndex)).getDoubleValue();
	}

	@Override
	protected DefaultData findTarget(DataRow row, long index) {
		Double val = getValue(row);
		int idx = 0;
		if (val != null) {
			idx = Math.min((int) Math.floor((val - min) / intervalLength), partitions.size() - 1);
		}
		return partitions.get(idx);
	}

	@Override
	protected List<Pair<DefaultData, PartitionInfo>> getResult() {
		List<Pair<DefaultData, PartitionInfo>> result = new ArrayList<>();
		int index = 0;
		for (DefaultData data : partitions) {
			String criteria = String.format("%s in [%.2f, %.2f%s", column, (min + intervalLength * index),
					(min + intervalLength * (index + 1)), index < partsNum - 1 ? ")" : "]");
			PartitionInfo info = new PartitionInfo(data.getHandle().getNumRows(), criteria);
			result.add(new Pair<DefaultData, PartitionInfo>(data, info));
			index++;
		}
		return result;
	}
}
