package se.redfield.arxnode.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;

public class ColumnsConfig extends SettingsModelConfig {

	private static final String CONFIG_KEY = "columns";

	private Map<String, ColumnConfig> columns;

	public ColumnsConfig() {
		columns = new HashMap<>();
	}

	@Override
	public String getKey() {
		return CONFIG_KEY;
	}

	@Override
	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		for (String name : settings.keySet()) {
			getColumn(name).load(settings.getNodeSettings(name));
		}
	}

	@Override
	protected Collection<? extends SettingsModelConfig> getChildred() {
		return columns.values();
	}

	public ColumnConfig getColumn(String name) {
		if (!columns.containsKey(name)) {
			columns.put(name, new ColumnConfig(name));
		}
		return columns.get(name);
	}

	public Map<String, ColumnConfig> getColumns() {
		return columns;
	}
}
