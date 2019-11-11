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

/**
 * Base class for all privacy models configs.
 *
 */
public abstract class AbstractPrivacyModelConfig implements SettingsModelConfig, Serializable {
	private static final long serialVersionUID = -6856452803075188914L;
	private static final NodeLogger logger = NodeLogger.getLogger(AbstractPrivacyModelConfig.class);

	private transient int index;

	public AbstractPrivacyModelConfig() {
		index = -1;
	}

	/**
	 * Returns the index under which this privacy model will be stored in node
	 * config. If the index is < 0 the model will be saved as a part of json string
	 * 
	 * @return index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Sets the index.
	 * 
	 * @param index Index
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return The property indicates if the privacy model is implicit (not
	 *         associated with any attributes)
	 */
	public boolean isImplicit() {
		return true;
	}

	@Override
	public String getKey() {
		return index + "-" + getClass().getSimpleName();
	}

	/**
	 * Creates new instance based of a config key.
	 * 
	 * @param configKey String in format "[index]-[class name]"
	 * @return New instance.
	 */
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

	/**
	 * Assign the smallest one unique index to the model based on existing models.
	 * 
	 * @param models Existing models
	 */
	public void assignIndex(List<AbstractPrivacyModelConfig> models) {
		if (index == -1) {
			index = models.stream().map(AbstractPrivacyModelConfig::getIndex).filter(i -> i > -1).sorted().reduce(0,
					(acc, i) -> acc.equals(i) ? acc + 1 : acc);
		}
	}

	/**
	 * Creates editor component for the model.
	 * 
	 * @param columns Columns configs.
	 * @return Editor component.
	 */
	public abstract PrivacyModelEditor createEditor(Collection<ColumnConfig> columns);

	/**
	 * Creates {@link PrivacyCriterion} instance.
	 * 
	 * @param data   Data to be used.
	 * @param config Node config.
	 * @return
	 */
	public abstract PrivacyCriterion createCriterion(Data data, Config config);

	/**
	 * @return Model display name.
	 */
	public abstract String getName();
}
