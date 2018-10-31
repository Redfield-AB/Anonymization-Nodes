package se.redfield.arxnode.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class AnonymizationConfig {
	private static final NodeLogger logger = NodeLogger.getLogger(AnonymizationConfig.class);

	private static final String CONFIG_HEURISTIC_SEARCH_ENABLED = "CONFIG_HEURISTIC_SEARCH_ENABLED";
	private static final String CONFIG_SEARCH_STEPS_LIMIT_ENABLED = "CONFIG_SEARCH_STEPS_LIMIT_ENABLED";
	private static final String CONFIG_SEARCH_TIME_LIMIT_ENABLED = "CONFIG_SEARCH_TIME_LIMIT_ENABLED";
	private static final String CONFIG_SEARCH_STEPS_LIMIT = "CONFIG_SEARCH_STEPS_LIMIT";
	private static final String CONFIG_SEARCH_TIME_LIMIT = "CONFIG_SEARCH_TIME_LIMIT";
	private static final String CONFIG_SUPPRESSION_LIMIT = "CONFIG_SUPPRESSION_LIMIT";
	private static final String CONFIG_PRACTIVAL_MONOTONICITY = "CONFIG_PRACTIVAL_MONOTONICITY";
	private static final String CONFIG_PRECOMPUTATION_ENABLED = "CONFIG_PRECOMPUTATION_ENABLED";
	private static final String CONFIG_PRECOMPUTATION_THRESHOLD = "CONFIG_PRECOMPUTATION_THRESHOLD";
	private static final String CONFIG_NUM_OF_THREADS = "CONFIG_NUM_OF_THREADS";
	private static final String CONFIG_PARTITIONS_SINGLE_OPTIMUM = "CONFIG_PARTITIONS_SINGLE_OPTIMUM";
	private static final String CONFIG_PARTITIONS_GROUP_BY_ENABLED = "CONFIG_PARTITIONS_GROUP_BY_ENABLED";
	private static final String CONFIG_PARTITIONS_GROUP_BY_COLUMN = "CONFIG_PARTITIONS_GROUP_BY_COLUMN";

	private SettingsModelBoolean heuristicSearchEnabled;
	private SettingsModelBoolean limitSearchSteps;
	private SettingsModelBoolean limitSearchTime;

	private SettingsModelIntegerBounded searchStepsLimit;
	private SettingsModelIntegerBounded searchTimeLimit;

	private SettingsModelDoubleBounded suppresionLimit;
	private SettingsModelBoolean practivalMonotonicity;

	private SettingsModelBoolean precomputationEnabled;
	private SettingsModelDoubleBounded precomputationThreshold;

	private SettingsModelIntegerBounded numOfThreads;
	private SettingsModelBoolean partitionsSingleOptimum;
	private SettingsModelBoolean partitionsGroupByEnabled;
	private SettingsModelString partitionsGroupByColumn;

	private List<SettingsModel> settingsModels;

	public AnonymizationConfig() {
		heuristicSearchEnabled = new SettingsModelBoolean(CONFIG_HEURISTIC_SEARCH_ENABLED, false);

		limitSearchSteps = new SettingsModelBoolean(CONFIG_SEARCH_STEPS_LIMIT_ENABLED, false);
		limitSearchTime = new SettingsModelBoolean(CONFIG_SEARCH_TIME_LIMIT_ENABLED, false);

		searchStepsLimit = new SettingsModelIntegerBounded(CONFIG_SEARCH_STEPS_LIMIT, 1000, 1, Integer.MAX_VALUE);
		searchTimeLimit = new SettingsModelIntegerBounded(CONFIG_SEARCH_TIME_LIMIT, 1000, 1, Integer.MAX_VALUE);

		suppresionLimit = new SettingsModelDoubleBounded(CONFIG_SUPPRESSION_LIMIT, 0, 0, 1);
		practivalMonotonicity = new SettingsModelBoolean(CONFIG_PRACTIVAL_MONOTONICITY, false);

		precomputationEnabled = new SettingsModelBoolean(CONFIG_PRECOMPUTATION_ENABLED, false);
		precomputationThreshold = new SettingsModelDoubleBounded(CONFIG_PRECOMPUTATION_THRESHOLD, 0, 0, 1);

		numOfThreads = new SettingsModelIntegerBounded(CONFIG_NUM_OF_THREADS, 1, 1, 20);
		partitionsSingleOptimum = new SettingsModelBoolean(CONFIG_PARTITIONS_SINGLE_OPTIMUM, true);

		partitionsGroupByEnabled = new SettingsModelBoolean(CONFIG_PARTITIONS_GROUP_BY_ENABLED, false);
		partitionsGroupByColumn = new SettingsModelString(CONFIG_PARTITIONS_GROUP_BY_COLUMN, "");

		settingsModels = new ArrayList<>(Arrays.asList(heuristicSearchEnabled, limitSearchSteps, limitSearchTime,
				searchStepsLimit, searchTimeLimit, suppresionLimit, practivalMonotonicity, precomputationEnabled,
				precomputationThreshold, numOfThreads, partitionsSingleOptimum, partitionsGroupByEnabled,
				partitionsGroupByColumn));

		limitSearchSteps.setEnabled(false);
		limitSearchTime.setEnabled(false);
		searchStepsLimit.setEnabled(false);
		searchTimeLimit.setEnabled(false);
		practivalMonotonicity.setEnabled(false);
		precomputationThreshold.setEnabled(false);
		partitionsSingleOptimum.setEnabled(false);
		partitionsGroupByEnabled.setEnabled(false);
		partitionsGroupByColumn.setEnabled(false);

		addEnabledListener(heuristicSearchEnabled, limitSearchSteps, limitSearchTime);
		addEnabledListener(limitSearchSteps, searchStepsLimit);
		addEnabledListener(limitSearchTime, searchTimeLimit);
		addEnabledListener(precomputationEnabled, precomputationThreshold);
		addEnabledListener(partitionsGroupByEnabled, partitionsGroupByColumn);
		suppresionLimit.addChangeListener(e -> {
			practivalMonotonicity.setEnabled(suppresionLimit.getDoubleValue() > 0);
			if (!practivalMonotonicity.isEnabled()) {
				practivalMonotonicity.setBooleanValue(false);
			}
		});
		numOfThreads.addChangeListener(e -> {
			boolean enabled = numOfThreads.getIntValue() > 1;
			partitionsSingleOptimum.setEnabled(enabled);
			partitionsGroupByEnabled.setEnabled(enabled);
		});
	}

	private void addEnabledListener(SettingsModelBoolean source, SettingsModel... targets) {
		source.addChangeListener(e -> {
			boolean enabled = source.getBooleanValue();
			for (int i = 0; i < targets.length; i++) {
				targets[i].setEnabled(enabled);
			}
		});
	}

	public void save(NodeSettingsWO settings) {
		settingsModels.forEach(s -> s.saveSettingsTo(settings));
	}

	public void load(NodeSettingsRO settings) {
		settingsModels.forEach(s -> {
			try {
				s.loadSettingsFrom(settings);
			} catch (InvalidSettingsException e) {
				logger.debug(e.getMessage(), e);
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

	public SettingsModelBoolean getPrecomputationEnabled() {
		return precomputationEnabled;
	}

	public SettingsModelDoubleBounded getPrecomputationThreshold() {
		return precomputationThreshold;
	}

	public SettingsModelIntegerBounded getNumOfThreads() {
		return numOfThreads;
	}

	public SettingsModelBoolean getPartitionsSingleOptimum() {
		return partitionsSingleOptimum;
	}

	public SettingsModelBoolean getPartitionsGroupByEnabled() {
		return partitionsGroupByEnabled;
	}

	public SettingsModelString getPartitionsGroupByColumn() {
		return partitionsGroupByColumn;
	}
}
