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
package se.redfield.arxnode.config.pmodels;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.knime.core.node.NodeLogger;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.config.SettingsModelConfig;
import se.redfield.arxnode.ui.pmodels.PrivacyModelEditor;

public abstract class AbstractPrivacyModelConfig implements SettingsModelConfig, Serializable {
	private static final long serialVersionUID = -6856452803075188914L;
	private static final NodeLogger logger = NodeLogger.getLogger(AbstractPrivacyModelConfig.class);

	private transient int index;

	public AbstractPrivacyModelConfig() {
		index = -1;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isImplicit() {
		return true;
	}

	@Override
	public String getKey() {
		return index + "-" + getClass().getSimpleName();
	}

	public static AbstractPrivacyModelConfig newInstance(String configKey) {
		try {
			String[] arr = configKey.split("-");
			int index = Integer.parseInt(arr[0]);
			String className = arr[1];
			Class<?> clazz = Class.forName("se.redfield.arxnode.config.pmodels." + className);
			AbstractPrivacyModelConfig instance = (AbstractPrivacyModelConfig) clazz.newInstance();
			instance.setIndex(index);
			return instance;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public void assignIndex(List<AbstractPrivacyModelConfig> models) {
		if (index == -1) {
			index = models.stream().map(AbstractPrivacyModelConfig::getIndex).filter(i -> i > -1).sorted().reduce(0,
					(acc, i) -> acc.equals(i) ? acc + 1 : acc);
		}
	}

	public abstract PrivacyModelEditor createEditor(Collection<ColumnConfig> columns);

	public abstract PrivacyCriterion createCriterion(Data data, Config config);

	public abstract String getName();
}
