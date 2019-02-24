package se.redfield.arxnode.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;

import se.redfield.arxnode.config.pmodels.AbstractPrivacyModelConfig;
import se.redfield.arxnode.config.pmodels.PrivacyModelsConfig;

public class Config implements SettingsModelConfig {
	private static final NodeLogger logger = NodeLogger.getLogger(Config.class);

	private PrivacyModelsConfig privacyModelConfig;
	private AnonymizationConfig anonymizationConfig;
	private SubsetConfig subsetConfig;
	private ColumnsConfig columnsConfig;

	public Config() {
		privacyModelConfig = new PrivacyModelsConfig();
		anonymizationConfig = new AnonymizationConfig();
		subsetConfig = new SubsetConfig();
		columnsConfig = new ColumnsConfig();
	}

	public void initColumns(DataTableSpec spec) {
		logger.debug("Config.initColumns");
		columnsConfig.configure(spec);
	}

	public void validate(NodeSettingsRO settings) throws InvalidSettingsException {
		logger.debug("Config.validate");
		Config tmp = new Config();
		tmp.load(settings);
		tmp.validate();
	}

	public Collection<ColumnConfig> getColumns() {
		return columnsConfig.getColumns().values();
	}

	public List<AbstractPrivacyModelConfig> getPrivacyModels() {
		return privacyModelConfig.getPrivacyModels();
	}

	public PrivacyModelsConfig getPrivacyModelConfig() {
		return privacyModelConfig;
	}

	public AnonymizationConfig getAnonymizationConfig() {
		return anonymizationConfig;
	}

	public SubsetConfig getSubsetConfig() {
		return subsetConfig;
	}

	@Override
	public String getKey() {
		return "config";
	}

	@Override
	public Collection<SettingsModelConfig> getChildred() {
		return Arrays.asList(columnsConfig, anonymizationConfig, subsetConfig, privacyModelConfig);
	}
}
