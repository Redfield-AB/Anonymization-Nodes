package se.redfield.arxnode.config;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.DataType;

import se.redfield.arxnode.Utils;

public class ColumnConfig {

	private String name;
	private int index;
	private DataType<?> dataType;
	private String hierarchyFile;
	private AttributeType attrType;
	private double weight;
	private TransformationConfig transformationConfig;

	public ColumnConfig(String name, int index, org.knime.core.data.DataType dataType) {
		this(name, index, Utils.knimeToArxType(dataType));
	}

	public ColumnConfig(String name, int index, DataType<?> dataType) {
		this.name = name;
		this.index = index;
		this.dataType = dataType;
		this.hierarchyFile = "";
		this.attrType = AttributeType.IDENTIFYING_ATTRIBUTE;
		this.weight = 0.5;
		this.transformationConfig = new TransformationConfig();
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	public DataType<?> getDataType() {
		return dataType;
	}

	public void setHierarchyFile(String hierarchyFile) {
		this.hierarchyFile = hierarchyFile;
	}

	public String getHierarchyFile() {
		return hierarchyFile;
	}

	public AttributeType getAttrType() {
		return attrType;
	}

	public void setAttrType(AttributeType attrType) {
		this.attrType = attrType;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public TransformationConfig getTransformationConfig() {
		return transformationConfig;
	}

	public void setTransformationConfig(TransformationConfig transformationConfig) {
		if (transformationConfig != null) {
			this.transformationConfig = transformationConfig;
		}
	}
}
