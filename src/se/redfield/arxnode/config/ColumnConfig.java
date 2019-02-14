package se.redfield.arxnode.config;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.DataType;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class ColumnConfig implements SettingsModelConfig {

	private static final String CONFIG_HIERARCHY_FILE = "hierarchyFile";
	private static final String CONFIG_ATTR_TYPE = "type";
	private static final String CONFIG_WEIGHT = "weight";

	private String name;
	private int index;
	private DataType<?> dataType;
	private AttributeType attrType;
	private TransformationConfig transformationConfig;

	private SettingsModelString hierarchyFileModel;
	private SettingsModelString attrTypeModel;
	private SettingsModelDoubleBounded weightModel;

	public ColumnConfig(String name) {
		this(name, 0, null);
	}

	public ColumnConfig(String name, int index, DataType<?> dataType) {
		this.name = name;
		this.index = index;
		this.dataType = dataType;
		this.attrType = AttributeType.IDENTIFYING_ATTRIBUTE;
		this.transformationConfig = new TransformationConfig();

		hierarchyFileModel = new SettingsModelString(CONFIG_HIERARCHY_FILE, "");
		attrTypeModel = new SettingsModelString(CONFIG_ATTR_TYPE,
				AttributeTypeOptions.IDENTIFYING_ATTRIBUTE.getTitle());
		weightModel = new SettingsModelDoubleBounded(CONFIG_WEIGHT, 0.5, 0, 1);

		attrTypeModel.addChangeListener(l -> {
			AttributeTypeOptions option = AttributeTypeOptions.fromName(attrTypeModel.getStringValue());
			attrType = option.getType();
		});
	}

	@Override
	public List<SettingsModel> getModels() {
		return Arrays.asList(hierarchyFileModel, attrTypeModel, weightModel);
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public DataType<?> getDataType() {
		return dataType;
	}

	public void setDataType(DataType<?> dataType) {
		this.dataType = dataType;
	}

	public String getHierarchyFile() {
		return hierarchyFileModel.getStringValue();
	}

	public SettingsModelString getHierarchyFileModel() {
		return hierarchyFileModel;
	}

	public AttributeType getAttrType() {
		return attrType;
	}

	public SettingsModelString getAttrTypeModel() {
		return attrTypeModel;
	}

	public double getWeight() {
		return weightModel.getDoubleValue();
	}

	public SettingsModelDoubleBounded getWeightModel() {
		return weightModel;
	}

	public TransformationConfig getTransformationConfig() {
		return transformationConfig;
	}

	@Override
	public String getKey() {
		return name;
	}

	@Override
	public Collection<? extends SettingsModelConfig> getChildred() {
		return Arrays.asList(transformationConfig);
	}

	@Override
	public void validate() throws InvalidSettingsException {
		SettingsModelConfig.super.validate();

		if (attrType == AttributeType.QUASI_IDENTIFYING_ATTRIBUTE) {
			String path = getHierarchyFile();
			if (StringUtils.isEmpty(path)) {
				throw new InvalidSettingsException(
						"Hierarcy file not set for quasi-identifying attribute '" + name + "'");
			}
			if (!new File(path).exists()) {
				throw new InvalidSettingsException("File " + path + " not found");
			}
		}

	}
}
