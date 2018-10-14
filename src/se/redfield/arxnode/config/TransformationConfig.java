package se.redfield.arxnode.config;

import org.deidentifier.arx.AttributeType.MicroAggregationFunction;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.google.gson.Gson;

public class TransformationConfig {

	private Mode mode;
	private Integer minGeneralization;
	private Integer maxGeneralization;
	private MicroaggregationFunction microaggregationFunc;
	private boolean ignoreMissingData;

	public TransformationConfig() {
		mode = Mode.GENERALIZATION;
		minGeneralization = null;
		maxGeneralization = null;
		microaggregationFunc = MicroaggregationFunction.ARITHMETIC_MEAN;
		ignoreMissingData = true;
	}

	public void save(NodeSettingsWO settings, String key) {
		String json = new Gson().toJson(this);
		settings.addString(key, json);
	}

	public void load(NodeSettingsRO settings, String key) {
		TransformationConfig config = new Gson().fromJson(settings.getString(key, null), TransformationConfig.class);
		if (config != null) {
			mode = config.mode;
			minGeneralization = config.minGeneralization;
			maxGeneralization = config.maxGeneralization;
			microaggregationFunc = config.microaggregationFunc;
			ignoreMissingData = config.ignoreMissingData;
		}
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public Integer getMinGeneralization() {
		return minGeneralization;
	}

	public void setMinGeneralization(Integer minGeneralization) {
		this.minGeneralization = minGeneralization;
	}

	public Integer getMaxGeneralization() {
		return maxGeneralization;
	}

	public void setMaxGeneralization(Integer maxGeneralization) {
		this.maxGeneralization = maxGeneralization;
	}

	public MicroaggregationFunction getMicroaggregationFunc() {
		return microaggregationFunc;
	}

	public void setMicroaggregationFunc(MicroaggregationFunction microaggregationFunc) {
		this.microaggregationFunc = microaggregationFunc;
	}

	public boolean isIgnoreMissingData() {
		return ignoreMissingData;
	}

	public void setIgnoreMissingData(boolean ignoreMissingData) {
		this.ignoreMissingData = ignoreMissingData;
	}

	public static enum Mode {
		GENERALIZATION("Generalization"), //
		MICROAGGREGATION("Microaggregation"), //
		CLUSTERING_AND_MICROAGGREGATION("Clustering and microaggregation");

		private String title;

		Mode(String title) {
			this.title = title;
		}

		@Override
		public String toString() {
			return title;
		}
	}

	public static enum MicroaggregationFunction {
		ARITHMETIC_MEAN("Arithmetic mean"), //
		GEOMETRIC_MEAN("Geometric mean"), //
		MEDIAN("Median"), //
		INTERVAL("Interval"), //
		MODE("Mode");

		private String title;

		private MicroaggregationFunction(String title) {
			this.title = title;
		}

		@Override
		public String toString() {
			return title;
		}

		public MicroAggregationFunction createFunction(boolean ignore) {
			switch (this) {
			case ARITHMETIC_MEAN:
				return MicroAggregationFunction.createArithmeticMean(ignore);
			case GEOMETRIC_MEAN:
				return MicroAggregationFunction.createGeometricMean(ignore);
			case INTERVAL:
				return MicroAggregationFunction.createInterval(ignore);
			case MEDIAN:
				return MicroAggregationFunction.createMedian(ignore);
			case MODE:
				return MicroAggregationFunction.createMode(ignore);
			}
			return null;
		}
	}
}
