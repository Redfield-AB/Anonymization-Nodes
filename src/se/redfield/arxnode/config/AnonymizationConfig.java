package se.redfield.arxnode.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class AnonymizationConfig implements SettingsModelConfig {
	private static final NodeLogger logger = NodeLogger.getLogger(AnonymizationConfig.class);

	public static final String CONFIG_KEY = "anonymization";

	public static final String CONFIG_HEURISTIC_SEARCH_ENABLED = "search.heuristicEnabled";
	public static final String CONFIG_SEARCH_STEPS_LIMIT_ENABLED = "search.limitSteps";
	public static final String CONFIG_SEARCH_TIME_LIMIT_ENABLED = "search.limitTime";
	public static final String CONFIG_SEARCH_STEPS_LIMIT = "search.stepsLimit";
	public static final String CONFIG_SEARCH_TIME_LIMIT = "search.timeLimit";
	public static final String CONFIG_SUPPRESSION_LIMIT = "suppressionLimit";
	public static final String CONFIG_PRACTIVAL_MONOTONICITY = "assumeMonotonicity";
	public static final String CONFIG_RISK_THRESHOLD = "riskThreshold";
	public static final String CONFIG_ADD_CLASS_COLUMN = "addClassColumn";
	public static final String CONFIG_OMIT_MISSING_VALUES = "omitMissingValues";
	public static final String CONFIG_OMIT_IDENTIFYING_COLUMNS = "omitIdentifyingColumns";
	public static final String CONFIG_NUM_OF_THREADS = "partition.numOfThreads";
	public static final String CONFIG_PARTITIONS_SINGLE_OPTIMUM = "partition.singleOptimum";
	public static final String CONFIG_PARTITIONS_GROUP_BY_ENABLED = "partition.group";
	public static final String CONFIG_PARTITIONS_GROUP_BY_COLUMN = "partition.groupBy";

	private SettingsModelBoolean heuristicSearchEnabled;
	private SettingsModelBoolean limitSearchSteps;
	private SettingsModelBoolean limitSearchTime;

	private SettingsModelIntegerBounded searchStepsLimit;
	private SettingsModelIntegerBounded searchTimeLimit;

	private SettingsModelDoubleBounded suppresionLimit;
	private SettingsModelBoolean practivalMonotonicity;

	private SettingsModelDoubleBounded riskThreshold;
	private SettingsModelBoolean addClassColumn;
	private SettingsModelBoolean omitMissingValues;
	private SettingsModelBoolean omitIdentifyingColumns;

	private SettingsModelIntegerBounded numOfThreads;
	private SettingsModelBoolean partitionsGroupByEnabled;
	private SettingsModelString partitionsGroupByColumn;

	private MetricConfig measure;

	public AnonymizationConfig() {
		heuristicSearchEnabled = new SettingsModelBoolean(CONFIG_HEURISTIC_SEARCH_ENABLED, false);

		limitSearchSteps = new SettingsModelBoolean(CONFIG_SEARCH_STEPS_LIMIT_ENABLED, false);
		limitSearchTime = new SettingsModelBoolean(CONFIG_SEARCH_TIME_LIMIT_ENABLED, false);

		searchStepsLimit = new SettingsModelIntegerBounded(CONFIG_SEARCH_STEPS_LIMIT, 1000, 1, Integer.MAX_VALUE);
		searchTimeLimit = new SettingsModelIntegerBounded(CONFIG_SEARCH_TIME_LIMIT, 1000, 1, Integer.MAX_VALUE);

		suppresionLimit = new SettingsModelDoubleBounded(CONFIG_SUPPRESSION_LIMIT, 0, 0, 1);
		practivalMonotonicity = new SettingsModelBoolean(CONFIG_PRACTIVAL_MONOTONICITY, false);

		riskThreshold = new SettingsModelDoubleBounded(CONFIG_RISK_THRESHOLD, 0.1, 0, 1);
		addClassColumn = new SettingsModelBoolean(CONFIG_ADD_CLASS_COLUMN, false);
		omitMissingValues = new SettingsModelBoolean(CONFIG_OMIT_MISSING_VALUES, false);
		omitIdentifyingColumns = new SettingsModelBoolean(CONFIG_OMIT_IDENTIFYING_COLUMNS, false);

		numOfThreads = new SettingsModelIntegerBounded(CONFIG_NUM_OF_THREADS, 1, 1, 20);

		partitionsGroupByEnabled = new SettingsModelBoolean(CONFIG_PARTITIONS_GROUP_BY_ENABLED, false);
		partitionsGroupByColumn = new SettingsModelString(CONFIG_PARTITIONS_GROUP_BY_COLUMN, "");

		limitSearchSteps.setEnabled(false);
		limitSearchTime.setEnabled(false);
		searchStepsLimit.setEnabled(false);
		searchTimeLimit.setEnabled(false);
		practivalMonotonicity.setEnabled(false);
		partitionsGroupByEnabled.setEnabled(false);
		partitionsGroupByColumn.setEnabled(false);

		addEnabledListener(heuristicSearchEnabled, limitSearchSteps, limitSearchTime);
		addEnabledListener(limitSearchSteps, searchStepsLimit);
		addEnabledListener(limitSearchTime, searchTimeLimit);
		addEnabledListener(partitionsGroupByEnabled, partitionsGroupByColumn);
		suppresionLimit.addChangeListener(e -> {
			practivalMonotonicity.setEnabled(suppresionLimit.getDoubleValue() > 0);
			if (!practivalMonotonicity.isEnabled()) {
				practivalMonotonicity.setBooleanValue(false);
			}
		});
		numOfThreads.addChangeListener(e -> {
			boolean enabled = numOfThreads.getIntValue() > 1;
			partitionsGroupByEnabled.setEnabled(enabled);
		});

		measure = new MetricConfig();
	}

	@Override
	public List<SettingsModel> getModels() {
		return Arrays.asList(heuristicSearchEnabled, limitSearchSteps, limitSearchTime, searchStepsLimit,
				searchTimeLimit, suppresionLimit, practivalMonotonicity, riskThreshold, addClassColumn,
				omitMissingValues, omitIdentifyingColumns, numOfThreads, partitionsGroupByEnabled,
				partitionsGroupByColumn);
	}

	@Override
	public Collection<? extends SettingsModelConfig> getChildred() {
		return Arrays.asList(measure);
	}

	private void addEnabledListener(SettingsModelBoolean source, SettingsModel... targets) {
		source.addChangeListener(e -> {
			boolean enabled = source.getBooleanValue();
			for (int i = 0; i < targets.length; i++) {
				targets[i].setEnabled(enabled);
			}
		});
	}

	public SettingsModelBoolean getHeuristicSearchEnabled() {
		return heuristicSearchEnabled;
	}

	public SettingsModelBoolean getLimitSearchSteps() {
		return limitSearchSteps;
	}

	public SettingsModelBoolean getLimitSearchTime() {
		return limitSearchTime;
	}

	public SettingsModelIntegerBounded getSearchStepsLimit() {
		return searchStepsLimit;
	}

	public SettingsModelIntegerBounded getSearchTimeLimit() {
		return searchTimeLimit;
	}

	public SettingsModelDoubleBounded getSuppresionLimit() {
		return suppresionLimit;
	}

	public SettingsModelBoolean getPractivalMonotonicity() {
		return practivalMonotonicity;
	}

	public SettingsModelDoubleBounded getRiskThreshold() {
		return riskThreshold;
	}

	public SettingsModelBoolean getAddClassColumn() {
		return addClassColumn;
	}

	public SettingsModelBoolean getOmitMissingValues() {
		return omitMissingValues;
	}

	public SettingsModelBoolean getOmitIdentifyingColumns() {
		return omitIdentifyingColumns;
	}

	public SettingsModelIntegerBounded getNumOfThreads() {
		return numOfThreads;
	}

	public SettingsModelBoolean getPartitionsGroupByEnabled() {
		return partitionsGroupByEnabled;
	}

	public SettingsModelString getPartitionsGroupByColumn() {
		return partitionsGroupByColumn;
	}

	public MetricConfig getMeasure() {
		return measure;
	}

	@Override
	public String getKey() {
		return CONFIG_KEY;
	}
}
