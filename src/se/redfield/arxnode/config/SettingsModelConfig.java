package se.redfield.arxnode.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
		getModels().forEach(s -> s.saveSettingsTo(settings));
		for (SettingsModelConfig c : getChildred()) {
			NodeSettingsWO child = settings.addNodeSettings(c.getKey());
			c.save(child);
		}
	}

	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		for (SettingsModel s : getModels()) {
			s.loadSettingsFrom(settings);
		}
		for (SettingsModelConfig c : getChildred()) {
			NodeSettingsRO child = settings.getNodeSettings(c.getKey());
			c.load(child);
		}
	}

	public void validate() throws InvalidSettingsException {
		for (SettingsModelConfig c : getChildred()) {
			c.validate();
		}
	}

	protected Collection<? extends SettingsModelConfig> getChildred() {
		return Collections.emptyList();
	}

	protected List<SettingsModel> getModels() {
		if (settingsModels == null) {
			settingsModels = new ArrayList<>();
		}
		return settingsModels;
	}

	public abstract String getKey();
}
