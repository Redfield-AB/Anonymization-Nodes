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

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSelector;
import org.deidentifier.arx.DataSubset;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import se.redfield.arxnode.util.TitledEnum;

public class SubsetConfig implements SettingsModelConfig {
	private static final NodeLogger logger = NodeLogger.getLogger(SubsetConfig.class);

	private static final String CONFIG_KEY = "subset";
	private static final String CONFIG_MODE = "mode";
	private static final String CONFIG_PROBABILITY = "probability";
	private static final String CONFIG_QUERY = "query";

	private SettingsModelString mode;
	private SettingsModelDouble probability;
	private SettingsModelString query;

	public SubsetConfig() {
		mode = new SettingsModelString(CONFIG_MODE, SamplingMode.NONE.name());
		probability = new SettingsModelDouble(CONFIG_PROBABILITY, 0.1);
		query = new SettingsModelString(CONFIG_QUERY, "");

		probability.setEnabled(false);
		query.setEnabled(false);

		mode.addChangeListener(e -> {
			SamplingMode m = SamplingMode.fromString(mode.getStringValue());
			probability.setEnabled(m == SamplingMode.RANDOM);
			query.setEnabled(m == SamplingMode.QUERY);
		});
	}

	@Override
	public List<SettingsModel> getModels() {
		return Arrays.asList(mode, probability, query);
	}

	public SettingsModelString getMode() {
		return mode;
	}

	public SettingsModelDouble getProbability() {
		return probability;
	}

	public SettingsModelString getQuery() {
		return query;
	}

	public DataSubset createDataSubset(Data data) {
		SamplingMode m = SamplingMode.fromString(this.mode.getStringValue());
		switch (m) {
		case ALL:
			return DataSubset.create(data, data);
		case QUERY:
			try {
				DataSelector selector = DataSelector.create(data, query.getStringValue());
				selector.build();
				return DataSubset.create(data, selector);
			} catch (Exception e) {
				logger.error("Failed to create query: " + e.getMessage(), e);
			}
			break;
		case RANDOM:
			Set<Integer> subsetIndices = new HashSet<>();
			Random random = new SecureRandom();
			int records = data.getHandle().getNumRows();
			for (int i = 0; i < records; ++i) {
				if (random.nextDouble() < probability.getDoubleValue()) {
					subsetIndices.add(i);
				}
			}
			return DataSubset.create(records, subsetIndices);
		default:
			break;
		}
		return null;
	}

	@Override
	public String getKey() {
		return CONFIG_KEY;
	}

	public enum SamplingMode implements TitledEnum {
		NONE("None"), ALL("All"), RANDOM("Random selection"), QUERY("Query selection");

		private String title;

		private SamplingMode(String title) {
			this.title = title;
		}

		@Override
		public String getTitle() {
			return title;
		}

		public static SamplingMode fromString(String str) {
			return TitledEnum.fromString(values(), str, NONE);
		}

	}
}
