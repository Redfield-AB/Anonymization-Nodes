package se.redfield.arxnode.partiton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.deidentifier.arx.Data.DefaultData;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.StringValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.Pair;

public class StringColumnPartitioner extends ColumnPartitioner {
	private static final NodeLogger logger = NodeLogger.getLogger(StringColumnPartitioner.class);

	private Map<String, DefaultData> partitions;

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
				partitions.put(val, createData(source));
			}
			if (partitions.size() > partsNum) {
				throw new IllegalArgumentException(
						String.format("Unable to partition table by [%s]. Column has more than %d distinct values",
								column, partsNum));
			}
		}
	}

	private String getValue(DataRow row) {
		DataCell cell = row.getCell(columnIndex);
		if (cell.isMissing()) {
			return "?";
		}
		return ((StringValue) row.getCell(columnIndex)).getStringValue();
	}

	@Override
	protected DefaultData findTarget(DataRow row, long index) {
		return partitions.get(getValue(row));
	}

	@Override
	protected List<Pair<DefaultData, PartitionInfo>> getResult() {
		return partitions.entrySet().stream().map(e -> new Pair<>(e.getValue(), createInfo(e)))
				.collect(Collectors.toList());
	}

	private PartitionInfo createInfo(Entry<String, DefaultData> entry) {
		return new PartitionInfo(entry.getValue().getHandle().getNumRows(),
				String.format("%s is %s", column, entry.getKey()));

	}

}
