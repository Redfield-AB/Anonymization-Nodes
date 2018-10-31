package se.redfield.arxnode.partiton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.deidentifier.arx.Data.DefaultData;
import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.Pair;

public class EqualSizePartitioner extends Partitioner {
	private static final NodeLogger logger = NodeLogger.getLogger(EqualSizePartitioner.class);

	private List<DefaultData> partitions;
	private long partitionSize;

	public EqualSizePartitioner(int partsNum) {
		super(partsNum);
	}

	@Override
	protected void init(BufferedDataTable source) {
		partitionSize = source.size() / partsNum;
		partitions = new ArrayList<>();
		for (int i = 0; i < partsNum; i++) {
			partitions.add(createData(source));
		}
	}

	@Override
	protected DefaultData findTarget(DataRow row, long index) {
		int idx = Math.min((int) (index / partitionSize), partitions.size() - 1);
		return partitions.get(idx);
	}

	@Override
	protected List<Pair<DefaultData, PartitionInfo>> getResult() {
		return partitions.stream().map(d -> new Pair<>(d, new PartitionInfo(d.getHandle().getNumRows(), "")))
				.collect(Collectors.toList());
	}
}
