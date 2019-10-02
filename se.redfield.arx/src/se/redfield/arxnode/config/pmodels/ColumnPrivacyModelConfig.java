package se.redfield.arxnode.config.pmodels;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public abstract class ColumnPrivacyModelConfig extends AbstractPrivacyModelConfig {

	public static final String CONFIG_COLUMN = "column";

	private String column;

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	protected abstract String getToStringPrefix();

	@Override
	public String toString() {
		return getToStringPrefix() + " for [" + column + "]";
	}

	@Override
	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		super.load(settings);
		column = settings.getString(CONFIG_COLUMN);
	}

	@Override
	public void save(NodeSettingsWO settings) {
		super.save(settings);
		settings.addString(CONFIG_COLUMN, column);
	}
}
