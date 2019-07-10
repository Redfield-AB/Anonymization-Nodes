package se.redfield.arxnode.config;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.DataType;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.FileUtil;

public class ColumnConfig implements SettingsModelConfig, Comparable<ColumnConfig> {
	private static final NodeLogger logger = NodeLogger.getLogger(ColumnConfig.class);

	public static final String CONFIG_HIERARCHY_FILE = "hierarchyFile";
	public static final String CONFIG_ATTR_TYPE = "type";
	public static final String CONFIG_WEIGHT = "weight";
	public static final String CONFIG_HIERARCHY_OVERRIDEN = "hierarchyOverriden";

	private String name;
	private int index;
	private DataType<?> dataType;
	private AttributeType attrType;
	private TransformationConfig transformationConfig;

	private SettingsModelString hierarchyFileModel;
	private SettingsModelString attrTypeModel;
	private SettingsModelDoubleBounded weightModel;

	private SettingsModelBoolean hierarchyOverriden;

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

		this.hierarchyOverriden = new SettingsModelBoolean(CONFIG_HIERARCHY_OVERRIDEN, false);
	}

	@Override
	public List<SettingsModel> getModels() {
		return Arrays.asList(hierarchyFileModel, attrTypeModel, weightModel, hierarchyOverriden);
	}

	@Override
	public String getKey() {
		return name;
	}

	@Override
	public Collection<? extends SettingsModelConfig> getChildred() {
		return Arrays.asList(transformationConfig);
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

	public File getHierarchyFile() throws InvalidPathException, MalformedURLException {
		String filename = hierarchyFileModel.getStringValue();
		if (StringUtils.isEmpty(filename)) {
			return null;
		}
		return FileUtil.getFileFromURL(FileUtil.toURL(hierarchyFileModel.getStringValue()));
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

	public boolean isHierarchyOverriden() {
		return hierarchyOverriden.getBooleanValue();
	}

	public void setHierarchyOverriden(boolean hierarchyOverriden) {
		this.hierarchyOverriden.setBooleanValue(hierarchyOverriden);
		hierarchyFileModel.setEnabled(!hierarchyOverriden);
	}

	@Override
	public int compareTo(ColumnConfig o) {
		return Integer.compare(index, o.index);
	}
}
