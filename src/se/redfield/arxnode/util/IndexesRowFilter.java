package se.redfield.arxnode.util;

import java.util.Set;

import org.knime.base.node.preproc.filter.row.rowfilter.AbstractRowFilter;
import org.knime.base.node.preproc.filter.row.rowfilter.EndOfTableException;
import org.knime.base.node.preproc.filter.row.rowfilter.IncludeFromNowOn;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class IndexesRowFilter extends AbstractRowFilter {

	private Set<Long> indexes;

	public IndexesRowFilter(Set<Long> indexes) {
		this.indexes = indexes;
	}

	@Override
	public boolean matches(DataRow row, long rowIndex) throws EndOfTableException, IncludeFromNowOn {
		return indexes.contains(rowIndex);
	}

	@Override
	public void loadSettingsFrom(NodeSettingsRO cfg) throws InvalidSettingsException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void saveSettings(NodeSettingsWO cfg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DataTableSpec configure(DataTableSpec inSpec) throws InvalidSettingsException {
		throw new UnsupportedOperationException();
	}

}
