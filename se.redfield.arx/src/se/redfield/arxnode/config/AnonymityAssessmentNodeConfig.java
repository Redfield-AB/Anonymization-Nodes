package se.redfield.arxnode.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;

import se.redfield.arxnode.config.pmodels.PopulationConfig;

public class AnonymityAssessmentNodeConfig implements SettingsModelConfig {

	public static final String KEY_COLUMNS = "columns";
	public static final String KEY_RISK_THRESHOLD = "riskThreshold";

	private SettingsModelFilterString columnFilter;
	private SettingsModelDoubleBounded riskThreshold;

	private PopulationConfig population;

	public AnonymityAssessmentNodeConfig() {
		columnFilter = new SettingsModelFilterString(KEY_COLUMNS);
		riskThreshold = new SettingsModelDoubleBounded(KEY_RISK_THRESHOLD, 0.1, 0, 1);

		population = new PopulationConfig();
	}

	public SettingsModelFilterString getColumnFilter() {
		return columnFilter;
	}

	public SettingsModelDoubleBounded getRiskThreshold() {
		return riskThreshold;
	}

	public PopulationConfig getPopulation() {
		return population;
	}

	public void removeMissingColumns(DataTableSpec spec) {
		List<String> filtered = columnFilter.getIncludeList().stream().filter(spec::containsName)
				.collect(Collectors.toList());
		columnFilter.setIncludeList(filtered);
	}

	@Override
	public List<SettingsModel> getModels() {
		return Arrays.asList(columnFilter, riskThreshold);
	}

	@Override
	public Collection<? extends SettingsModelConfig> getChildred() {
		return Arrays.asList(population);
	}

	@Override
	public String getKey() {
		return null;
	}

}
