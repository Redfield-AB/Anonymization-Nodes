package se.redfield.arxnode.config.pmodels;

import java.util.Collection;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.PrivacyCriterion;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.ui.pmodels.DPresenceEditor;
import se.redfield.arxnode.ui.pmodels.PrivacyModelEditor;

public class DPresenceConfig implements PrivacyModelConfig {

	private double dMin;
	private double dMax;

	public DPresenceConfig() {
		dMin = 0;
		dMax = 0;
	}

	public double getdMin() {
		return dMin;
	}

	public void setdMin(double dMin) {
		this.dMin = dMin;
	}

	public double getdMax() {
		return dMax;
	}

	public void setdMax(double dMax) {
		this.dMax = dMax;
	}

	@Override
	public PrivacyModelEditor createEditor(Collection<ColumnConfig> columns) {
		return new DPresenceEditor(this);
	}

	@Override
	public PrivacyCriterion createCriterion(Data data, Config config) {
		return new DPresence(dMin, dMax, config.getSubsetConfig().createDataSubset(data));
	}

	@Override
	public String getName() {
		return '\u03B4' + "-Presence";
	}

	@Override
	public String toString() {
		return String.format("(%.3f, %.3f)-Presence", dMin, dMax);
	}

}
