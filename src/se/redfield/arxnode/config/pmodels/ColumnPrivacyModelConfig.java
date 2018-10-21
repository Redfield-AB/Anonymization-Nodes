package se.redfield.arxnode.config.pmodels;

public abstract class ColumnPrivacyModelConfig implements PrivacyModelConfig {

	private String column;

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}
}
