package se.redfield.arxnode.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.deidentifier.arx.AttributeType;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;

import se.redfield.arxnode.config.pmodels.AbstractPrivacyModelConfig;
import se.redfield.arxnode.config.pmodels.PrivacyModelsConfig;
import se.redfield.arxnode.nodes.ArxPortObjectSpec;

public class Config implements SettingsModelConfig {
	private static final NodeLogger logger = NodeLogger.getLogger(Config.class);

	private PrivacyModelsConfig privacyModelConfig;
	private AnonymizationConfig anonymizationConfig;
	private SubsetConfig subsetConfig;
	private ColumnsConfig columnsConfig;

	private ArxPortObjectSpec overrides;

	public Config() {
		privacyModelConfig = new PrivacyModelsConfig();
		anonymizationConfig = new AnonymizationConfig();
		subsetConfig = new SubsetConfig();
		columnsConfig = new ColumnsConfig();
	}

	public void configure(DataTableSpec spec, ArxPortObjectSpec overrides) {
		columnsConfig.configure(spec);
		this.overrides = overrides;
		processOverrides();
	}

	private void processOverrides() {
		columnsConfig.getColumns().values().forEach(c -> c.setHierarchyOverriden(false));
		if (overrides != null) {
			for (String column : overrides.getHierarchies()) {
				ColumnConfig c = columnsConfig.getColumn(column);
				if (c != null) {
					c.setHierarchyOverriden(true);
				}
			}
		}
	}

	@Override
	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		SettingsModelConfig.super.load(settings);
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

	public List<ColumnConfig> getOutputColumns() {
		boolean omitIdentifying = getAnonymizationConfig().getOmitIdentifyingColumns().getBooleanValue();
		return getColumns().stream().sorted()
				.filter(c -> !(omitIdentifying && c.getAttrType() == AttributeType.IDENTIFYING_ATTRIBUTE))
				.collect(Collectors.toList());
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
