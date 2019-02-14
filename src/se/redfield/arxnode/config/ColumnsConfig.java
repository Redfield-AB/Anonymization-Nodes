package se.redfield.arxnode.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;

import se.redfield.arxnode.Utils;

public class ColumnsConfig implements SettingsModelConfig {
	private static final NodeLogger logger = NodeLogger.getLogger(ColumnsConfig.class);

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
	public Collection<? extends SettingsModelConfig> getChildred() {
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

	public void configure(DataTableSpec spec) {
		Set<String> availableColumns = new TreeSet<>();
		for (int j = 0; j < spec.getColumnNames().length; j++) {
			String name = spec.getColumnNames()[j];
			availableColumns.add(name);
			ColumnConfig c = getColumn(name);
			c.setIndex(j);
			c.setDataType(Utils.knimeToArxType(spec.getColumnSpec(j).getType()));
		}

		Set<String> missingColumns = columns.keySet().stream().filter(name -> !availableColumns.contains(name))
				.collect(Collectors.toSet());
		missingColumns.forEach(name -> columns.remove(name));
	}
}
