package se.redfield.arxnode.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import se.redfield.arxnode.Utils;
import se.redfield.arxnode.config.pmodels.PrivacyModelConfig;
import se.redfield.arxnode.config.pmodels.PrivacyModelsConfig;

public class Config extends SettingsModelConfig {
	private static final NodeLogger logger = NodeLogger.getLogger(Config.class);

	public static final String CONFIG_PRIVACY_MODELS = "privacy_models";

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

	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		logger.debug("Config.load");
		super.load(settings);
		privacyModelConfig = PrivacyModelsConfig.load(settings);
	}

	public void save(NodeSettingsWO settings) {
		logger.debug("Config.save");
		super.save(settings);
		privacyModelConfig.save(settings);
	}

	public void initColumns(DataTableSpec spec) {
		logger.debug("Config.initColumns");
		for (int j = 0; j < spec.getColumnNames().length; j++) {
			String name = spec.getColumnNames()[j];
			ColumnConfig c = columnsConfig.getColumn(name);
			c.setIndex(j);
			c.setDataType(Utils.knimeToArxType(spec.getColumnSpec(j).getType()));
		}
	}

	public void validate(NodeSettingsRO settings) throws InvalidSettingsException {
		logger.debug("Config.validate");
		Config tmp = new Config();
		tmp.load(settings);
		tmp.validate();
	}

	@Override
	public void validate() throws InvalidSettingsException {
		super.validate();
		privacyModelConfig.validate();
	}

	public Collection<ColumnConfig> getColumns() {
		return columnsConfig.getColumns().values();
	}

	public List<PrivacyModelConfig> getPrivacyModels() {
		return privacyModelConfig.getModels();
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
	protected Collection<SettingsModelConfig> getChildred() {
		return Arrays.asList(columnsConfig, anonymizationConfig, subsetConfig);
	}
}
