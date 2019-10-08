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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import se.redfield.arxnode.config.SettingsModelConfig;
import se.redfield.arxnode.util.InterfaceAdapter;

public class PrivacyModelsConfig implements SettingsModelConfig {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(PrivacyModelsConfig.class);
	public static final String CONFIG_KEY = "privacyModels";
	public static final String CONFIG_JSON = "json";

	private List<AbstractPrivacyModelConfig> models = new ArrayList<>();

	public List<AbstractPrivacyModelConfig> getPrivacyModels() {
		return models;
	}

	@Override
	public Collection<? extends SettingsModelConfig> getChildred() {
		return models.stream().filter(m -> m.getIndex() > -1).collect(Collectors.toList());
	}

	@Override
	public void save(NodeSettingsWO settings) {
		SettingsModelConfig.super.save(settings);
		String json = createGson().toJson(
				models.stream().filter(m -> m.getIndex() == -1).collect(Collectors.toList()).toArray(),
				AbstractPrivacyModelConfig[].class);
		settings.addString(CONFIG_JSON, json);
	}

	@Override
	public void validate() throws InvalidSettingsException {
		if (models.isEmpty()) {
			throw new InvalidSettingsException("No privacy models selected");
		}

		Set<String> implicitModels = new HashSet<>();
		for (AbstractPrivacyModelConfig m : models) {
			if (m.isImplicit()) {
				if (implicitModels.contains(m.getName())) {
					throw new InvalidSettingsException(
							"You must not add more than one instance of the " + m.getName() + " model");
				}
				implicitModels.add(m.getName());
			}
		}
	}

	@Override
	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		models.clear();
		for (String key : settings.keySet()) {
			if (key.contains("-")) {
				AbstractPrivacyModelConfig model = AbstractPrivacyModelConfig.newInstance(key);
				if (model != null) {
					model.load(settings.getNodeSettings(key));
					models.add(model);
				}
			}
		}
		loadJson(settings);
	}

	private void loadJson(NodeSettingsRO settings) {
		String json = settings.getString(CONFIG_JSON, null);
		AbstractPrivacyModelConfig[] result = createGson().fromJson(json, AbstractPrivacyModelConfig[].class);
		if (result != null) {
			Collections.addAll(models, result);
		}
	}

	private static Gson createGson() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(AbstractPrivacyModelConfig.class,
				new InterfaceAdapter<AbstractPrivacyModelConfig>(KAnonymityConfig.class, DPresenceConfig.class,
						LDiversityConfig.class, TClosenessConfig.class, KMapConfig.class));
		return builder.create();
	}

	@Override
	public String getKey() {
		return CONFIG_KEY;
	}

}
