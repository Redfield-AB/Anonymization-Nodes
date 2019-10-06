/*
 * Copyright (c) 2019 Redfield AB.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *  
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
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
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(ColumnsConfig.class);

	public static final String CONFIG_KEY = "columns";

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
