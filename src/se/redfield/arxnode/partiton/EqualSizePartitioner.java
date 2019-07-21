package se.redfield.arxnode.partiton;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeLogger;

public class EqualSizePartitioner extends Partitioner {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(EqualSizePartitioner.class);

	private List<Partition> partitions;
	private long partitionSize;

	public EqualSizePartitioner(int partsNum) {
		super(partsNum);
	}

	@Override
	protected void init(BufferedDataTable source) {
		partitionSize = source.size() / partsNum;
		partitions = new ArrayList<>();
		for (int i = 0; i < partsNum; i++) {
			partitions.add(new Partition(createData(source)));
		}
	}

	@Override
	protected Partition findTarget(DataRow row, long index) {
		int idx = Math.min((int) (index / partitionSize), partitions.size() - 1);
		return partitions.get(idx);
	}

	@Override
	protected List<Partition> getResult() {
		return partitions;
	}
}
