package se.redfield.arxnode.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class HierarchyExpandConfig implements SettingsModelConfig {

	private static final String CONFIG_COLUMN = "column";
	private static final String CONFIG_FILE = "file";

	private SettingsModelColumnName column;
	private SettingsModelString file;

	public HierarchyExpandConfig() {
		column = new SettingsModelColumnName(CONFIG_COLUMN, "");
		file = new SettingsModelString(CONFIG_FILE, "");
	}

	public SettingsModelColumnName getColumnSetting() {
		return column;
	}

	public String getColumnName() {
		return column.getColumnName();
	}

	public SettingsModelString getFile() {
		return file;
	}

	@Override
	public List<SettingsModel> getModels() {
		return Arrays.asList(column, file);
	}

	@Override
	public String getKey() {
		return null;
	}

	@Override
	public void validate() throws InvalidSettingsException {
		SettingsModelConfig.super.validate();
		if (StringUtils.isEmpty(column.getStringValue())) {
			throw new InvalidSettingsException("Target column is not selected");
		}
		String path = file.getStringValue();
		if (StringUtils.isEmpty(path)) {
			throw new InvalidSettingsException("Hierarchy file is not set");
		}
		if (!new File(path).exists()) {
			throw new InvalidSettingsException("Hierarchy file does not exist");
		}
		try {
			HierarchyBuilder<?> h = HierarchyBuilder.create(path);
			if (!(h instanceof HierarchyBuilderIntervalBased)) {
				throw new InvalidSettingsException("Only Interval based hierarchies supported");
			}
		} catch (IOException e) {
			throw new InvalidSettingsException(e);
		}
	}
}
