package se.redfield.arxnode.config.pmodels;

import java.util.Collection;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.PrivacyCriterion;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.ui.pmodels.KAnonymityEditor;
import se.redfield.arxnode.ui.pmodels.PrivacyModelEditor;

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
	public PrivacyModelEditor createEditor(Collection<ColumnConfig> columns) {
		return new KAnonymityEditor(this);
	}

	@Override
	public PrivacyCriterion createCriterion(Data data, Config config) {
		return new KAnonymity(factor);
	}

	@Override
	public String getName() {
		return "k-Anonymity";
	}

	@Override
	public String toString() {
		return factor + "-Anonymity";
	}
}
