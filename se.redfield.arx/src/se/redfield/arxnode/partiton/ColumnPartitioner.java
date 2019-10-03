package se.redfield.arxnode.partiton;

import org.knime.core.node.BufferedDataTable;

public abstract class ColumnPartitioner extends Partitioner {

	protected String column;
	protected int columnIndex;

	public ColumnPartitioner(String column, int partsNum) {
		super(partsNum);
		this.column = column;
	}

	@Override
	protected void init(BufferedDataTable source) {
		columnIndex = source.getDataTableSpec().findColumnIndex(column);
	}
}
