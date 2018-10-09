package se.redfield.arxnode.config;

import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.PrivacyCriterion;

public class KAnonymityConfig implements PrivacyModelConfig {

	private int factor;

	public KAnonymityConfig() {
		factor = 1;
	}

	public int getFactor() {
		return factor;
	}

	public void setFactor(int factor) {
		this.factor = factor;
	}

	@Override
	public PrivacyModelEditor createEditor() {
		return new KAnonymityEditor(this);
	}

	@Override
	public PrivacyCriterion createCriterion() {
		return new KAnonymity(factor);
	}

	@Override
	public String toString() {
		return "K-Anonymity[factor=" + factor + "]";
	}
}
