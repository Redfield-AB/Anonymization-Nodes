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

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public abstract class ListSettingsModelConfig<T extends SettingsModelConfig> implements SettingsModelConfig {

	protected List<T> children;

	@Override
	public List<T> getChildred() {
		if (children == null) {
			children = new ArrayList<>();
		}
		return children;
	}

	@Override
	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		getChildred().clear();
		for (String index : settings) {
			T child = createChild();
			child.load(settings.getNodeSettings(index));
			getChildred().add(child);
		}
	}

	@Override
	public void save(NodeSettingsWO settings) {
		int index = 0;
		for (SettingsModelConfig c : getChildred()) {
			NodeSettingsWO child = settings.addNodeSettings(String.valueOf(index++));
			c.save(child);
		}
	}

	protected abstract T createChild();
}
