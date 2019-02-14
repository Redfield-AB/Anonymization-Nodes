package se.redfield.arxnode.config;

import org.deidentifier.arx.AttributeType.MicroAggregationFunction;
import org.deidentifier.arx.DataType;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class TransformationConfig implements SettingsModelConfig {
	private static final NodeLogger logger = NodeLogger.getLogger(TransformationConfig.class);
	private static final String CONFIG_KEY = "transformation";
	private static final String CONFIG_MODE = "mode";
	private static final String CONFIG_MIN_LEVEL = "minLevel";
	private static final String CONFIG_MAX_LEVEL = "maxLevel";
	private static final String CONFIG_MA_FUNC = "microaggregationFunction";
	private static final String CONFIG_IGNORE_MISSING = "ignoreMissingData";

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

	@Override
	public String getKey() {
		return CONFIG_KEY;
	}

	@Override
	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		mode = Mode.fromName(settings.getString(CONFIG_MODE, null));
		microaggregationFunc = MicroaggregationFunction.fromName(settings.getString(CONFIG_MA_FUNC, null));
		ignoreMissingData = settings.getBoolean(CONFIG_IGNORE_MISSING, true);
		if (settings.containsKey(CONFIG_MIN_LEVEL)) {
			minGeneralization = settings.getInt(CONFIG_MIN_LEVEL);
		}
		if (settings.containsKey(CONFIG_MAX_LEVEL)) {
			maxGeneralization = settings.getInt(CONFIG_MAX_LEVEL);
		}
	}

	@Override
	public void save(NodeSettingsWO settings) {
		settings.addString(CONFIG_MODE, mode.title);
		settings.addString(CONFIG_MA_FUNC, microaggregationFunc.title);
		settings.addBoolean(CONFIG_IGNORE_MISSING, ignoreMissingData);
		if (minGeneralization != null) {
			settings.addInt(CONFIG_MIN_LEVEL, minGeneralization);
		}
		if (maxGeneralization != null) {
			settings.addInt(CONFIG_MAX_LEVEL, maxGeneralization);
		}
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

		public static Mode fromName(String name) {
			try {
				return valueOf(name);
			} catch (IllegalArgumentException e) {
				for (Mode opt : values()) {
					if (opt.title.equalsIgnoreCase(name)) {
						return opt;
					}
				}
			}
			return GENERALIZATION;
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

		public static MicroaggregationFunction[] values(DataType<?> type) {
			if (type == DataType.STRING) {
				return new MicroaggregationFunction[] { MODE };
			}
			return values();
		}

		public static MicroaggregationFunction fromName(String name) {
			try {
				return valueOf(name);
			} catch (IllegalArgumentException e) {
				for (MicroaggregationFunction opt : values()) {
					if (opt.title.equalsIgnoreCase(name)) {
						return opt;
					}
				}
			}
			return ARITHMETIC_MEAN;
		}
	}
}
