package se.redfield.arxnode.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;

public abstract class SettingsModelConfig {
	private static final NodeLogger logger = NodeLogger.getLogger(SettingsModelConfig.class);

	private List<SettingsModel> settingsModels;

	protected void addModels(SettingsModel... models) {
		if (settingsModels == null) {
			settingsModels = new ArrayList<>();
		}
		settingsModels.addAll(Arrays.asList(models));
	}

	public void save(NodeSettingsWO settings) {
		settingsModels.forEach(s -> s.saveSettingsTo(settings));
	}

	public void load(NodeSettingsRO settings) {
		settingsModels.forEach(s -> {
			try {
				s.loadSettingsFrom(settings);
			} catch (InvalidSettingsException e) {
				logger.debug(e.getMessage(), e);
			}
		});
	}
}
