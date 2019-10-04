package se.redfield.arxnode.config.pmodels;

import java.util.Collection;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.KMap;
import org.deidentifier.arx.criteria.KMap.CellSizeEstimator;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.ui.pmodels.KMapEditor;
import se.redfield.arxnode.ui.pmodels.PrivacyModelEditor;
import se.redfield.arxnode.util.TitledEnum;

public class KMapConfig extends AbstractPrivacyModelConfig {
	private static final long serialVersionUID = -8105941374251980610L;
	public static final String CONFIG_K = "k";
	public static final String CONFIG_ESTIMATOR = "estimator";
	public static final String CONFIG_SIGNIFICANCE_LEVEL = "significanceLevel";

	private int k;
	private EstimatorOption estimator;
	private double significanceLevel;

	public KMapConfig() {
		k = 2;
		estimator = EstimatorOption.NONE;
		significanceLevel = 0;
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

	public EstimatorOption getEstimator() {
		return estimator;
	}

	public void setEstimator(EstimatorOption estimator) {
		this.estimator = estimator;
	}

	public double getSignificanceLevel() {
		return significanceLevel;
	}

	public void setSignificanceLevel(double significanceLevel) {
		this.significanceLevel = significanceLevel;
	}

	@Override
	public PrivacyModelEditor createEditor(Collection<ColumnConfig> columns) {
		return new KMapEditor(this);
	}

	@Override
	public PrivacyCriterion createCriterion(Data data, Config config) {
		if (estimator == EstimatorOption.NONE) {
			return new KMap(k, config.getSubsetConfig().createDataSubset(data));
		}
		return new KMap(k, significanceLevel, config.getAnonymizationConfig().getPopulation().getPopulationModel(),
				estimator.getEstimator());
	}

	@Override
	public String getName() {
		return "k-Map";
	}

	@Override
	public String toString() {
		String result = String.format("%d-Map", k);
		if (estimator != EstimatorOption.NONE) {
			result += String.format(" (%s/%.3f)", estimator, significanceLevel);
		}
		return result;
	}

	@Override
	public void save(NodeSettingsWO settings) {
		super.save(settings);
		settings.addInt(CONFIG_K, k);
		settings.addDouble(CONFIG_SIGNIFICANCE_LEVEL, significanceLevel);
		settings.addString(CONFIG_ESTIMATOR, estimator.getTitle());
	}

	@Override
	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		super.load(settings);
		k = settings.getInt(CONFIG_K);
		significanceLevel = settings.getDouble(CONFIG_SIGNIFICANCE_LEVEL);
		estimator = EstimatorOption.fromString(settings.getString(CONFIG_ESTIMATOR));
	}

	public enum EstimatorOption implements TitledEnum {
		NONE("None"), //
		POISSON(CellSizeEstimator.POISSON), //
		ZERO_TRUNCATED_POISSON(CellSizeEstimator.ZERO_TRUNCATED_POISSON);

		private CellSizeEstimator estimator;
		private String title;

		private EstimatorOption(CellSizeEstimator estimator) {
			this.estimator = estimator;
			this.title = estimator.toString();
		}

		private EstimatorOption(String title) {
			this.title = title;
			this.estimator = null;
		}

		public CellSizeEstimator getEstimator() {
			return estimator;
		}

		@Override
		public String toString() {
			return title;
		}

		@Override
		public String getTitle() {
			return title;
		}

		public static EstimatorOption fromString(String str) {
			return TitledEnum.fromString(values(), str, NONE);
		}
	}
}
