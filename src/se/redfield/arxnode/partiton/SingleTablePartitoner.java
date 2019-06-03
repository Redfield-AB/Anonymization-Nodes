package se.redfield.arxnode.partiton;

import java.util.Arrays;
import java.util.List;

import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;

public class SingleTablePartitoner extends Partitioner {

	private Partition data;

	public SingleTablePartitoner() {
		super(1);
	}

	@Override
	protected void init(BufferedDataTable source) {
		data = new Partition(createData(source));
	}

	@Override
	protected Partition findTarget(DataRow row, long index) {
		return data;
	}

	@Override
	protected List<Partition> getResult() {
		return Arrays.asList(data);
	}

}
