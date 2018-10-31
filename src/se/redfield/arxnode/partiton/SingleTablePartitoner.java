package se.redfield.arxnode.partiton;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.Data.DefaultData;
import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.util.Pair;

public class SingleTablePartitoner extends Partitioner {

	private DefaultData data;

	public SingleTablePartitoner() {
		super(1);
	}

	@Override
	protected void init(BufferedDataTable source) {
		data = createData(source);
	}

	@Override
	protected DefaultData findTarget(DataRow row, long index) {
		return data;
	}

	@Override
	protected List<Pair<DefaultData, PartitionInfo>> getResult() {
		List<Pair<DefaultData, PartitionInfo>> result = new ArrayList<>();
		result.add(new Pair<DefaultData, PartitionInfo>(data, new PartitionInfo(data.getHandle().getNumRows(), "")));
		return result;
	}

}
