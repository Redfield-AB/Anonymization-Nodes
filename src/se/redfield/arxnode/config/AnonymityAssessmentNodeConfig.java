package se.redfield.arxnode.config;

import java.util.Arrays;
import java.util.List;

import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;

public class AnonymityAssessmentNodeConfig implements SettingsModelConfig {

	public static final String KEY_COLUMNS = "columns";

	private SettingsModelFilterString columnFilter;

	public AnonymityAssessmentNodeConfig() {
		columnFilter = new SettingsModelFilterString(KEY_COLUMNS);
	}

	public SettingsModelFilterString getColumnFilter() {
		return columnFilter;
	}

	@Override
	public List<SettingsModel> getModels() {
		return Arrays.asList(columnFilter);
	}

	@Override
	public String getKey() {
		return null;
	}

}
