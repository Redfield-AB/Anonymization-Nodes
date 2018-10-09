package se.redfield.arxnode.config;

import org.deidentifier.arx.AttributeType;

public class ColumnConfig {

	private String name;
	private int index;
	private String hierarchyFile;
	private AttributeType attrType;

	public ColumnConfig(String name, int index) {
		this.name = name;
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	public void setHierarchyFile(String hierarchyFile) {
		this.hierarchyFile = hierarchyFile;
	}

	public String getHierarchyFile() {
		if (hierarchyFile == null) {
			hierarchyFile = "";
		}
		return hierarchyFile;
	}

	public AttributeType getAttrType() {
		return attrType;
	}

	public void setAttrType(AttributeType attrType) {
		this.attrType = attrType;
	}
}
