package se.redfield.arxnode.config;

import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.metric.Metric.AggregateFunction;
import org.deidentifier.arx.metric.MetricConfiguration;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import se.redfield.arxnode.util.TitledEnum;

public class MetricConfig implements SettingsModelConfig {

	public static final String CONFIG_KEY = "utilityMeasure";
	public static final String CONFIG_MEASURE = "measure";
	public static final String CONFIG_MONOTONIC = "monotonic";
	public static final String CONFIG_GC_FACTOR = "gsFactor";
	public static final String CONFIG_PRECOMPUTED = "precomputationEnabled";
	public static final String CONFIG_PRECOMPUTATION_THRESHOLD = "precomputationThreshold";
	public static final String CONFGI_AGGREGATE_FUNCTION = "aggregateFunction";

	private MetricDescriptionWrap measure;
	private MetricConfiguration configuration;

	public MetricConfig() {
		measure = MetricDescriptionWrap.fromString("Loss");
		configuration = new MetricConfiguration(false, 0.5, false, 0.1, AggregateFunction.ARITHMETIC_MEAN);
	}

	@Override
	public void save(NodeSettingsWO settings) {
		SettingsModelConfig.super.save(settings);
		settings.addString(CONFIG_MEASURE, measure.toString());
		settings.addBoolean(CONFIG_MONOTONIC, configuration.isMonotonic());
		settings.addDouble(CONFIG_GC_FACTOR, configuration.getGsFactor());
		settings.addBoolean(CONFIG_PRECOMPUTED, configuration.isPrecomputed());
		settings.addDouble(CONFIG_PRECOMPUTATION_THRESHOLD, configuration.getPrecomputationThreshold());
		settings.addString(CONFGI_AGGREGATE_FUNCTION, configuration.getAggregateFunction().toString());
	}

	@Override
	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		SettingsModelConfig.super.load(settings);
		measure = MetricDescriptionWrap.fromString(settings.getString(CONFIG_MEASURE, ""));
		configuration.setMonotonic(settings.getBoolean(CONFIG_MONOTONIC, false));
		configuration.setGsFactor(settings.getDouble(CONFIG_GC_FACTOR, 0.5));
		configuration.setPrecomputed(settings.getBoolean(CONFIG_PRECOMPUTED, false));
		configuration.setPrecomputationThreshold(settings.getDouble(CONFIG_PRECOMPUTATION_THRESHOLD, 0.1));
		configuration.setAggregateFunction(
				AggregateFunctionOptions.fromString(settings.getString(CONFGI_AGGREGATE_FUNCTION, "")).getFunc());
	}

	public MetricDescriptionWrap getMeasure() {
		return measure;
	}

	public void setMeasure(MetricDescriptionWrap measure) {
		this.measure = measure;
	}

	public MetricConfiguration getConfiguration() {
		return configuration;
	}

	public Metric<?> createMetric() {
		return measure.getDescription().createInstance(configuration);
	}

	@Override
	public String getKey() {
		return CONFIG_KEY;
	}

	public enum AggregateFunctionOptions implements TitledEnum {
		SUM(AggregateFunction.SUM), MAX(AggregateFunction.MAXIMUM), ARITHMETIC_MEAN(AggregateFunction.ARITHMETIC_MEAN),
		GEOMETRIC_MEAN(AggregateFunction.GEOMETRIC_MEAN), RANK(AggregateFunction.RANK);

		private AggregateFunction func;

		AggregateFunctionOptions(AggregateFunction func) {
			this.func = func;
		}

		public AggregateFunction getFunc() {
			return func;
		}

		@Override
		public String getTitle() {
			return func.toString();
		}

		@Override
		public String toString() {
			return func.toString();
		}

		public static AggregateFunctionOptions fromString(String str) {
			return TitledEnum.fromString(values(), str, GEOMETRIC_MEAN);
		}
	}
}
