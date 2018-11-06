package se.redfield.arxnode.config.pmodels;

import java.util.Collection;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.KMap;
import org.deidentifier.arx.criteria.PrivacyCriterion;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.ui.pmodels.KMapEditor;
import se.redfield.arxnode.ui.pmodels.PrivacyModelEditor;

public class KMapConfig implements PrivacyModelConfig {

	private int k;

	public KMapConfig() {
		k = 2;
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

	@Override
	public PrivacyModelEditor createEditor(Collection<ColumnConfig> columns) {
		return new KMapEditor(this);
	}

	@Override
	public PrivacyCriterion createCriterion(Data data) {
		return new KMap(k, null);
	}

	@Override
	public String getName() {
		return "k-Map";
	}

	@Override
	public String toString() {
		return String.format("%d-Map", k);
	}
}
