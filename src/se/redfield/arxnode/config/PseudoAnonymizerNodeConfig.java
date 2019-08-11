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

	private SettingsModelFilterString columnFilter;
	private SettingsModelString saltingModeModel;
	private SettingsModelBoolean useSeed;
	private SettingsModelLong randomSeed;
	private SettingsModelColumnName saltColumn;
	private SettingsModelDateTime timestamp;
	private SettingsModelBoolean autoTimestamp;
	private SettingsModelBoolean debugMode;

	public PseudoAnonymizerNodeConfig() {
		columnFilter = new SettingsModelFilterString(KEY_COLUMNS);
		saltingModeModel = new SettingsModelString(KEY_SALTING_MODE, SaltingMode.NONE.getTitle());
		useSeed = new SettingsModelBoolean(KEY_USE_SEED, false);
		randomSeed = new SettingsModelLong(KEY_SEED, new Random().nextInt());
		saltColumn = new SettingsModelColumnName(KEY_SALT_COLUMN, "");
		timestamp = new SettingsModelDateTime(KEY_TIMESTAMP, LocalDateTime.now());
		autoTimestamp = new SettingsModelBoolean(KEY_TIMESTAMP_AUTO, false);
		debugMode = new SettingsModelBoolean(KEY_DEBUG, false);

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

	@Override
	public List<SettingsModel> getModels() {
		return Arrays.asList(columnFilter, saltingModeModel, useSeed, randomSeed, saltColumn, timestamp, autoTimestamp,
				debugMode);
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

	public static enum SaltingMode implements TitledEnum {
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
}
