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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelLong;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.time.util.SettingsModelDateTime;

import se.redfield.arxnode.util.TitledEnum;

public class PseudoAnonymizerNodeConfig implements SettingsModelConfig {

	public static final String KEY_COLUMNS = "columns";
	public static final String KEY_SALTING_MODE = "saltingMode";
	public static final String KEY_USE_SEED = "useSeed";
	public static final String KEY_SEED = "seed";
	public static final String KEY_SALT_COLUMN = "saltColumn";
	public static final String KEY_TIMESTAMP = "timestamp";
	public static final String KEY_TIMESTAMP_AUTO = "autoTimestamp";
	public static final String KEY_DEBUG = "debug";
	public static final String KEY_REPLACE_MODE = "outputMode";

	private SettingsModelFilterString columnFilter;
	private SettingsModelString saltingModeModel;
	private SettingsModelBoolean useSeed;
	private SettingsModelLong randomSeed;
	private SettingsModelColumnName saltColumn;
	private SettingsModelDateTime timestamp;
	private SettingsModelBoolean autoTimestamp;
	private SettingsModelBoolean debugMode;
	private SettingsModelString replaceModeModel;

	public PseudoAnonymizerNodeConfig() {
		columnFilter = new SettingsModelFilterString(KEY_COLUMNS);
		saltingModeModel = new SettingsModelString(KEY_SALTING_MODE, SaltingMode.NONE.getTitle());
		useSeed = new SettingsModelBoolean(KEY_USE_SEED, false);
		randomSeed = new SettingsModelLong(KEY_SEED, new Random().nextInt());
		saltColumn = new SettingsModelColumnName(KEY_SALT_COLUMN, "");
		timestamp = new SettingsModelDateTime(KEY_TIMESTAMP, LocalDateTime.now());
		autoTimestamp = new SettingsModelBoolean(KEY_TIMESTAMP_AUTO, false);
		debugMode = new SettingsModelBoolean(KEY_DEBUG, false);
		replaceModeModel = new SettingsModelString(KEY_REPLACE_MODE, ReplaceMode.REPLACE.getTitle());

		useSeed.setEnabled(false);
		randomSeed.setEnabled(false);
		saltColumn.setEnabled(false);
		timestamp.setEnabled(false);
		autoTimestamp.setEnabled(false);

		saltingModeModel.addChangeListener(e -> {
			SaltingMode mode = getSaltingMode();
			useSeed.setEnabled(mode == SaltingMode.RANDOM);
			randomSeed.setEnabled(mode == SaltingMode.RANDOM && useSeed.getBooleanValue());
			saltColumn.setEnabled(mode == SaltingMode.COLUMN);
			timestamp.setEnabled(mode == SaltingMode.TIMESTAMP && !autoTimestamp.getBooleanValue());
			autoTimestamp.setEnabled(mode == SaltingMode.TIMESTAMP);
		});

		useSeed.addChangeListener(e -> {
			randomSeed.setEnabled(getSaltingMode() == SaltingMode.RANDOM && useSeed.getBooleanValue());
		});
		autoTimestamp.addChangeListener(e -> {
			timestamp.setEnabled(getSaltingMode() == SaltingMode.TIMESTAMP && !autoTimestamp.getBooleanValue());
		});
	}

	public SettingsModelFilterString getColumnFilter() {
		return columnFilter;
	}

	public List<String> getSelectedColumns() {
		return columnFilter.getIncludeList();
	}

	public SettingsModelString getSaltingModeModel() {
		return saltingModeModel;
	}

	public SaltingMode getSaltingMode() {
		return SaltingMode.fromString(saltingModeModel.getStringValue());
	}

	public void setSaltingMode(SaltingMode mode) {
		saltingModeModel.setStringValue(mode.getTitle());
	}

	public SettingsModelBoolean getUseSeed() {
		return useSeed;
	}

	public SettingsModelLong getRandomSeed() {
		return randomSeed;
	}

	public SettingsModelColumnName getSaltColumn() {
		return saltColumn;
	}

	public SettingsModelDateTime getTimestamp() {
		return timestamp;
	}

	public SettingsModelBoolean getAutoTimestamp() {
		return autoTimestamp;
	}

	public SettingsModelBoolean getDebugMode() {
		return debugMode;
	}

	public SettingsModelString getReplaceModeModel() {
		return replaceModeModel;
	}

	public ReplaceMode getReplaceMode() {
		return ReplaceMode.fromString(replaceModeModel.getStringValue());
	}

	public void setReplaceMode(ReplaceMode mode) {
		replaceModeModel.setStringValue(mode.getTitle());
	}

	@Override
	public List<SettingsModel> getModels() {
		return Arrays.asList(columnFilter, saltingModeModel, useSeed, randomSeed, saltColumn, timestamp, autoTimestamp,
				debugMode, replaceModeModel);
	}

	@Override
	public String getKey() {
		return null;
	}

	@Override
	public void validate() throws InvalidSettingsException {
		SaltingMode mode = getSaltingMode();
		if (mode == SaltingMode.COLUMN) {
			if (StringUtils.isEmpty(saltColumn.getStringValue()) && !saltColumn.useRowID()) {
				throw new InvalidSettingsException("Salting column is not selected");
			}
		}
	}

	public enum SaltingMode implements TitledEnum {
		NONE("None"), RANDOM("Random"), COLUMN("Column"), TIMESTAMP("Timestamp");

		private String title;

		private SaltingMode(String title) {
			this.title = title;
		}

		@Override
		public String getTitle() {
			return title;
		}

		public static SaltingMode fromString(String str) {
			return TitledEnum.fromString(values(), str, NONE);
		}
	}

	public enum ReplaceMode implements TitledEnum {
		REPLACE("Replace"), APPEND("Append");

		private String title;

		private ReplaceMode(String title) {
			this.title = title;
		}

		@Override
		public String getTitle() {
			return title;
		}

		public static ReplaceMode fromString(String str) {
			return TitledEnum.fromString(values(), str, REPLACE);
		}
	}
}
