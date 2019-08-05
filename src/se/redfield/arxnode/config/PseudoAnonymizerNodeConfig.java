package se.redfield.arxnode.config;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelLong;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import se.redfield.arxnode.util.TitledEnum;

public class PseudoAnonymizerNodeConfig implements SettingsModelConfig {

	public static final String KEY_COLUMNS = "columns";
	public static final String KEY_SALTING_MODE = "saltingMode";
	public static final String KEY_SEED = "seed";
	public static final String KEY_SALT_COLUMN = "saltColumn";
	public static final String KEY_DEBUG = "debug";

	private SettingsModelFilterString columnFilter;
	private SettingsModelString saltingModeModel;
	private SettingsModelLong randomSeed;
	private SettingsModelString saltColumn;
	private SettingsModelBoolean debugMode;

	public PseudoAnonymizerNodeConfig() {
		columnFilter = new SettingsModelFilterString(KEY_COLUMNS);
		saltingModeModel = new SettingsModelString(KEY_SALTING_MODE, SaltingMode.NONE.getTitle());
		randomSeed = new SettingsModelLong(KEY_SEED, new Random().nextInt());
		saltColumn = new SettingsModelString(KEY_SALT_COLUMN, "");
		debugMode = new SettingsModelBoolean(KEY_DEBUG, false);

		randomSeed.setEnabled(false);
		saltColumn.setEnabled(false);

		saltingModeModel.addChangeListener(e -> {
			SaltingMode mode = getSaltingMode();
			randomSeed.setEnabled(mode == SaltingMode.RANDOM);
			saltColumn.setEnabled(mode == SaltingMode.COLUMN);
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

	public SettingsModelLong getRandomSeed() {
		return randomSeed;
	}

	public SettingsModelString getSaltColumn() {
		return saltColumn;
	}

	public SettingsModelBoolean getDebugMode() {
		return debugMode;
	}

	@Override
	public List<SettingsModel> getModels() {
		return Arrays.asList(columnFilter, saltingModeModel, randomSeed, saltColumn, debugMode);
	}

	@Override
	public String getKey() {
		return null;
	}

	@Override
	public void validate() throws InvalidSettingsException {
		SaltingMode mode = getSaltingMode();
		if (mode == SaltingMode.COLUMN) {
			if (StringUtils.isEmpty(saltColumn.getStringValue())) {
				throw new InvalidSettingsException("Salting column is not selected");
			}
		}
	}

	public static enum SaltingMode implements TitledEnum {
		NONE("None"), RANDOM("Random"), COLUMN("Column");

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
