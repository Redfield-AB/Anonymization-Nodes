package se.redfield.arxnode.config;

import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.PrivacyCriterion;

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
	public PrivacyModelEditor createEditor() {
		return new DPresenceEditor(this);
	}

	@Override
	public PrivacyCriterion createCriterion() {
		return new DPresence(dMin, dMax, null);
	}

	@Override
	public String toString() {
		return String.format("D-Presence [dMin=%.2f, dMax=%.2f]", dMin, dMax);
	}

}
