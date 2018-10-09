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
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class Config {
	private static final NodeLogger logger = NodeLogger.getLogger(Config.class);

	public static final String CONFIG_HIERARCHY_FILE_PREFIX = "hierarchy_file_";
	public static final String CONFIG_HIERARCHY_ATTR_TYPE_PREFIX = "hierarchy_attr_type_";
	public static final String CONFIG_KANONYMITY_FACTOR_KEY = "k_anonymity_factor";
	public static final String CONFIG_PRIVACY_MODELS = "privacy_models";

	public static final int DEFAULT_KANONYMITY_FACTOR = 3;

	static final String INTERNALS_POSTFIX = "_Internals";

	private Map<String, ColumnConfig> columns;

	private Map<String, SettingsModelString> hierarchySettings;
	private Map<String, SettingsModelString> attrTypeSettings;
	private SettingsModelIntegerBounded kAnonymityFactorSetting = new SettingsModelIntegerBounded(
			CONFIG_KANONYMITY_FACTOR_KEY, DEFAULT_KANONYMITY_FACTOR, 1, Integer.MAX_VALUE);;
	private PrivacyModelsConfig privacyModelConfig;

	public Config() {
		hierarchySettings = new HashMap<>();
		attrTypeSettings = new HashMap<>();
		privacyModelConfig = new PrivacyModelsConfig();
	}

	public void load(NodeSettingsRO settings) {
		hierarchySettings.clear();
		attrTypeSettings.clear();
		settings.keySet().forEach(key -> {
			if (key.endsWith(INTERNALS_POSTFIX)) {
				// ignore
				return;
			}
			if (key.startsWith(CONFIG_HIERARCHY_FILE_PREFIX)) {
				hierarchySettings.put(extractColumnName(key, CONFIG_HIERARCHY_FILE_PREFIX),
						loadModelString(key, settings));
			}
			if (key.startsWith(CONFIG_HIERARCHY_ATTR_TYPE_PREFIX)) {
				attrTypeSettings.put(extractColumnName(key, CONFIG_HIERARCHY_ATTR_TYPE_PREFIX),
						loadModelString(key, settings));
			}
		});
		privacyModelConfig = PrivacyModelsConfig.load(settings);

		try {
			kAnonymityFactorSetting.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private String extractColumnName(String key, String prefix) {
		return key.substring(prefix.length());
	}

	private SettingsModelString loadModelString(String key, NodeSettingsRO settings) {
		SettingsModelString model = new SettingsModelString(key, "");
		try {
			model.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			logger.warn(e.getMessage(), e);
		}
		return model;
	}

	public void save(NodeSettingsWO settings) {
		hierarchySettings.values().forEach(v -> v.saveSettingsTo(settings));
		attrTypeSettings.values().forEach(v -> v.saveSettingsTo(settings));

		privacyModelConfig.save(settings);
		kAnonymityFactorSetting.saveSettingsTo(settings);
	}

	public void initColumns(DataTableSpec spec) {
		columns = new HashMap<String, ColumnConfig>();
		for (int j = 0; j < spec.getColumnNames().length; j++) {
			// logger.warn(spec.getColumnNames()[j]);
			String name = spec.getColumnNames()[j];
			ColumnConfig c = new ColumnConfig(name, j);

			SettingsModelString hierarchy = hierarchySettings.get(name);
			if (hierarchy != null && hierarchy.getStringValue().length() > 0) {
				c.setHierarchyFile(hierarchy.getStringValue());
			}

			SettingsModelString attrType = attrTypeSettings.get(name);
			if (attrType != null) {
				AttributeTypeOptions option = AttributeTypeOptions.valueOf(attrType.getStringValue());
				if (option != null) {
					c.setAttrType(option.getType());
				}
			}

			columns.put(name, c);
		}
	}

	public Map<String, ColumnConfig> getColumns() {
		return columns;
	}

	public int getKAnonymityFactor() {
		return kAnonymityFactorSetting.getIntValue();
	}

	public List<PrivacyModelConfig> getPrivacyModels() {
		return privacyModelConfig.getModels();
	}

	public DataTableSpec createOutDataTableSpec() {
		DataColumnSpec[] outColSpecs = new DataColumnSpec[columns.size()];
		columns.values().forEach(c -> {
			outColSpecs[c.getIndex()] = new DataColumnSpecCreator(c.getName(), StringCell.TYPE).createSpec();
		});
		return new DataTableSpec(outColSpecs);
	}

	public void validate(NodeSettingsRO settings) throws InvalidSettingsException {
		for (String key : settings.keySet()) {
			if (key.startsWith(CONFIG_HIERARCHY_FILE_PREFIX)) {
				String path = settings.getString(key, "");
				if (!StringUtils.isEmpty(path) && !new File(path).exists()) {
					throw new InvalidSettingsException("File " + path + " not found");
				}
			}
		}
		kAnonymityFactorSetting.validateSettings(settings);
	}
}
