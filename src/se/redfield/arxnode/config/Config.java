package se.redfield.arxnode.config;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import se.redfield.arxnode.config.pmodels.PrivacyModelConfig;
import se.redfield.arxnode.config.pmodels.PrivacyModelsConfig;

public class Config {
	private static final NodeLogger logger = NodeLogger.getLogger(Config.class);

	public static final String CONFIG_HIERARCHY_FILE_PREFIX = "hierarchy_file_";
	public static final String CONFIG_HIERARCHY_ATTR_TYPE_PREFIX = "hierarchy_attr_type_";
	public static final String CONFIG_WEIGHT_PREFIX = "attr_weight_";
	public static final String CONFIG_KANONYMITY_FACTOR_KEY = "k_anonymity_factor";
	public static final String CONFIG_PRIVACY_MODELS = "privacy_models";
	public static final String CONFIG_TRANSFORMATION_SETTINGS_PREFIX = "CONFIG_TRANSFORMATION_SETTINGS_";

	public static final int DEFAULT_KANONYMITY_FACTOR = 3;

	static final String INTERNALS_POSTFIX = "_Internals";

	private Map<String, ColumnConfig> columns;

	private Map<String, SettingsModelString> hierarchySettings;
	private Map<String, SettingsModelString> attrTypeSettings;
	private Map<String, SettingsModelDoubleBounded> weightSettings;
	private Map<String, TransformationConfig> transformationSettings;

	private PrivacyModelsConfig privacyModelConfig;
	private AnonymizationConfig anonymizationConfig;
	private SubsetConfig subsetConfig;

	public Config() {
		this(null);
	}

	public Config(Map<String, ColumnConfig> columns) {
		hierarchySettings = new HashMap<>();
		attrTypeSettings = new HashMap<>();
		weightSettings = new HashMap<>();
		transformationSettings = new HashMap<>();
		privacyModelConfig = new PrivacyModelsConfig();
		anonymizationConfig = new AnonymizationConfig();
		subsetConfig = new SubsetConfig();
		this.columns = new HashMap<>();
		if (columns != null) {
			columns.values().forEach(c -> {
				this.columns.put(c.getName(), new ColumnConfig(c.getName(), c.getIndex(), c.getDataType()));
			});
		}
	}

	public void load(NodeSettingsRO settings) {
		logger.debug("Config.load");
		hierarchySettings.clear();
		attrTypeSettings.clear();
		weightSettings.clear();
		transformationSettings.clear();
		settings.keySet().forEach(key -> {
			if (key.endsWith(INTERNALS_POSTFIX)) {
				// ignore
				return;
			}
			SettingsModel model = null;
			if (key.startsWith(CONFIG_HIERARCHY_FILE_PREFIX)) {
				model = getHierarchySetting(extractColumnName(key, CONFIG_HIERARCHY_FILE_PREFIX));
			} else if (key.startsWith(CONFIG_HIERARCHY_ATTR_TYPE_PREFIX)) {
				model = getAttrTypeSetting(extractColumnName(key, CONFIG_HIERARCHY_ATTR_TYPE_PREFIX));
			} else if (key.startsWith(CONFIG_WEIGHT_PREFIX)) {
				model = getWeightSetting(extractColumnName(key, CONFIG_WEIGHT_PREFIX));
			} else if (key.startsWith(CONFIG_TRANSFORMATION_SETTINGS_PREFIX)) {
				getTransformationConfig(extractColumnName(key, CONFIG_TRANSFORMATION_SETTINGS_PREFIX)).load(settings,
						key);
			}

			if (model != null) {
				try {
					model.loadSettingsFrom(settings);
				} catch (InvalidSettingsException e) {
					logger.warn(e.getMessage(), e);
				}
			}
		});
		privacyModelConfig = PrivacyModelsConfig.load(settings);
		anonymizationConfig.load(settings);
		subsetConfig.load(settings);

		columns.values().forEach(c -> readColumnSettings(c));
	}

	private String extractColumnName(String key, String prefix) {
		return key.substring(prefix.length());
	}

	public void save(NodeSettingsWO settings) {
		logger.debug("Config.save");
		hierarchySettings.values().forEach(v -> v.saveSettingsTo(settings));
		attrTypeSettings.values().forEach(v -> v.saveSettingsTo(settings));
		weightSettings.values().forEach(v -> v.saveSettingsTo(settings));

		privacyModelConfig.save(settings);
		anonymizationConfig.save(settings);
		subsetConfig.save(settings);

		transformationSettings
				.forEach((key, config) -> config.save(settings, CONFIG_TRANSFORMATION_SETTINGS_PREFIX + key));
	}

	public void initColumns(DataTableSpec spec) {
		logger.debug("Config.initColumns");
		columns.clear();
		for (int j = 0; j < spec.getColumnNames().length; j++) {
			String name = spec.getColumnNames()[j];
			ColumnConfig c = new ColumnConfig(name, j, spec.getColumnSpec(j).getType());
			readColumnSettings(c);
			columns.put(name, c);
		}
	}

	private void readColumnSettings(ColumnConfig c) {
		SettingsModelString hierarchy = hierarchySettings.get(c.getName());
		if (hierarchy != null && hierarchy.getStringValue().length() > 0) {
			c.setHierarchyFile(hierarchy.getStringValue());
		}

		readAttType(c);

		SettingsModelDoubleBounded weight = weightSettings.get(c.getName());
		if (weight != null) {
			c.setWeight(weight.getDoubleValue());
		}

		c.setTransformationConfig(getTransformationConfig(c.getName()));
	}

	private void readAttType(ColumnConfig c) {
		try {
			AttributeTypeOptions option = AttributeTypeOptions
					.fromName(attrTypeSettings.get(c.getName()).getStringValue());
			c.setAttrType(option.getType());
		} catch (Exception e) {
			// ignore
		}
	}

	public DataTableSpec createOutDataTableSpec() {
		DataColumnSpec[] outColSpecs = new DataColumnSpec[columns.size()];
		columns.values().forEach(c -> {
			outColSpecs[c.getIndex()] = new DataColumnSpecCreator(c.getName(), StringCell.TYPE).createSpec();
		});
		return new DataTableSpec(outColSpecs);
	}

	public void validate(NodeSettingsRO settings) throws InvalidSettingsException {
		logger.debug("Config.validate");
		for (String key : settings.keySet()) {
			if (key.endsWith(INTERNALS_POSTFIX)) {
				// ignore
				continue;
			}
			if (key.startsWith(CONFIG_HIERARCHY_FILE_PREFIX)) {
				String path = settings.getString(key, "");
				if (!StringUtils.isEmpty(path) && !new File(path).exists()) {
					throw new InvalidSettingsException("File " + path + " not found");
				}
			}
			if (key.startsWith(CONFIG_HIERARCHY_ATTR_TYPE_PREFIX)) {
				AttributeTypeOptions opt = AttributeTypeOptions.fromName(settings.getString(key));
				if (opt == AttributeTypeOptions.QUASI_IDENTIFYING_ATTRIBUTE) {
					String name = extractColumnName(key, CONFIG_HIERARCHY_ATTR_TYPE_PREFIX);
					String hierarchyFile = settings.getString(CONFIG_HIERARCHY_FILE_PREFIX + name, "");
					if (StringUtils.isEmpty(hierarchyFile)) {
						throw new InvalidSettingsException(
								"Hierarcy file not set for quasi-identifying attribute '" + name + "'");
					}
				}
			}
		}
		PrivacyModelsConfig.load(settings).validate();
	}

	public Map<String, ColumnConfig> getColumns() {
		return columns;
	}

	public List<PrivacyModelConfig> getPrivacyModels() {
		return privacyModelConfig.getModels();
	}

	public PrivacyModelsConfig getPrivacyModelConfig() {
		return privacyModelConfig;
	}

	public SettingsModelString getHierarchySetting(String name) {
		if (!hierarchySettings.containsKey(name)) {
			hierarchySettings.put(name, new SettingsModelString(CONFIG_HIERARCHY_FILE_PREFIX + name, ""));
		}
		return hierarchySettings.get(name);
	}

	public SettingsModelString getAttrTypeSetting(String name) {
		if (!attrTypeSettings.containsKey(name)) {
			SettingsModelString s = new SettingsModelString(CONFIG_HIERARCHY_ATTR_TYPE_PREFIX + name, "");
			s.addChangeListener(e -> {
				ColumnConfig c = columns.get(name);
				if (c != null) {
					readAttType(c);
				}
			});
			attrTypeSettings.put(name, s);
		}
		return attrTypeSettings.get(name);
	}

	public SettingsModelDoubleBounded getWeightSetting(String name) {
		if (!weightSettings.containsKey(name)) {
			weightSettings.put(name, new SettingsModelDoubleBounded(CONFIG_WEIGHT_PREFIX + name, 0.5, 0, 1));
		}
		return weightSettings.get(name);
	}

	public AnonymizationConfig getAnonymizationConfig() {
		return anonymizationConfig;
	}

	public SubsetConfig getSubsetConfig() {
		return subsetConfig;
	}

	public TransformationConfig getTransformationConfig(String name) {
		if (!transformationSettings.containsKey(name)) {
			transformationSettings.put(name, new TransformationConfig());
		}
		return transformationSettings.get(name);
	}
}
