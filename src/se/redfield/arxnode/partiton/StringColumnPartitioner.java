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

public class StringColumnPartitioner extends ColumnPartitioner {
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

	private Partition fillCriteria(Partition p, String key) {
		p.getInfo().setCriteria(String.format("%s is %s", column, key));
		return p;
	}

}
