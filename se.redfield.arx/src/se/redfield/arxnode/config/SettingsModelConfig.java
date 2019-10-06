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

public interface SettingsModelConfig {
	static final NodeLogger logger = NodeLogger.getLogger(SettingsModelConfig.class);

	default void save(NodeSettingsWO settings) {
		getModels().forEach(s -> s.saveSettingsTo(settings));
		for (SettingsModelConfig c : getChildred()) {
			NodeSettingsWO child = settings.addNodeSettings(c.getKey());
			c.save(child);
		}
	}

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

	default void validate() throws InvalidSettingsException {
		for (SettingsModelConfig c : getChildred()) {
			c.validate();
		}
	}

	default Collection<? extends SettingsModelConfig> getChildred() {
		return Collections.emptyList();
	}

	default List<SettingsModel> getModels() {
		return Collections.emptyList();
	}

	public String getKey();
}
