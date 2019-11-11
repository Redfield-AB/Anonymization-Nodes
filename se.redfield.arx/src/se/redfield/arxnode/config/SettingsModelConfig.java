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
import java.util.Collections;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;

/**
 * Class for creating hierarchical node configs. Each config could hold nested
 * {@link SettingsModelConfig} instances as well as {@link SettingsModel}
 * instances
 *
 */
/**
 * @author ajbond
 *
 */
public interface SettingsModelConfig {
	static final NodeLogger logger = NodeLogger.getLogger(SettingsModelConfig.class);

	/**
	 * Performs saving. Stores each of settings models to a provided settings.
	 * Nested settings are created for each of the children.
	 * 
	 * @param settings node settings
	 */
	default void save(NodeSettingsWO settings) {
		getModels().forEach(s -> s.saveSettingsTo(settings));
		for (SettingsModelConfig c : getChildred()) {
			NodeSettingsWO child = settings.addNodeSettings(c.getKey());
			c.save(child);
		}
	}

	/**
	 * Performs loading. Settings models are read from the provided settings.
	 * Children are read from the appropriate nested entries.
	 * 
	 * @param settings node settings
	 * @throws InvalidSettingsException
	 */
	default void load(NodeSettingsRO settings) throws InvalidSettingsException {
		for (SettingsModel s : getModels()) {
			try {
				s.loadSettingsFrom(settings);
			} catch (InvalidSettingsException e) {
				logger.warn(e.getMessage(), e);
			}
		}
		for (SettingsModelConfig c : getChildred()) {
			try {
				NodeSettingsRO child = settings.getNodeSettings(c.getKey());
				c.load(child);
			} catch (InvalidSettingsException e) {
				logger.warn(e.getMessage(), e);
			}

		}
	}

	/**
	 * Performs validation. Calls validation for each of the children recursively.
	 * 
	 * @throws InvalidSettingsException
	 */
	default void validate() throws InvalidSettingsException {
		for (SettingsModelConfig c : getChildred()) {
			c.validate();
		}
	}

	/**
	 * Returns list of nested {@link SettingsModelConfig} objects.
	 * 
	 * @return
	 */
	default Collection<? extends SettingsModelConfig> getChildred() {
		return Collections.emptyList();
	}

	/**
	 * Returns list of {@link SettingsModel} objects.
	 * 
	 * @return
	 */
	default List<SettingsModel> getModels() {
		return Collections.emptyList();
	}

	/**
	 * Returns key under which this config will be stored.
	 * 
	 * @return
	 */
	public String getKey();
}
