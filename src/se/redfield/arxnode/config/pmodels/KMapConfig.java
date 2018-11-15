package se.redfield.arxnode.config.pmodels;

import java.util.Collection;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.KMap;
import org.deidentifier.arx.criteria.KMap.CellSizeEstimator;
import org.deidentifier.arx.criteria.PrivacyCriterion;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.ui.pmodels.KMapEditor;
import se.redfield.arxnode.ui.pmodels.PrivacyModelEditor;

public class KMapConfig implements PrivacyModelConfig {

	private int k;
	private EstimatorOption estimator;
	private double significanceLevel;
	private PopulationConfig population;

	public KMapConfig() {
		k = 2;
		estimator = EstimatorOption.NONE;
		significanceLevel = 0;
		population = new PopulationConfig();
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

	public PopulationConfig getPopulation() {
		return population;
	}

	public void setPopulation(PopulationConfig population) {
		this.population = population;
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
		return new KMap(k, significanceLevel, population.getPopulationModel(), estimator.getEstimator());
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

	public enum EstimatorOption {
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
	}
}
