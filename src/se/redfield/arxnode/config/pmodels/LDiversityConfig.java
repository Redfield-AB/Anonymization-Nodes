package se.redfield.arxnode.config.pmodels;

import java.util.Collection;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity.EntropyEstimator;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.ui.pmodels.LDiversityEditor;
import se.redfield.arxnode.ui.pmodels.PrivacyModelEditor;

public class LDiversityConfig extends ColumnPrivacyModelConfig {

	public static final String CONFIG_INT_L = "intL";
	public static final String CONFIG_DOUBLE_L = "doubleL";
	public static final String CONFIG_C = "c";
	public static final String CONFIG_VARIANT = "variant";

	private int intL;
	private double doubleL;
	private double c;
	private LDiversityVariant variant;

	public LDiversityConfig() {
		intL = 1;
		doubleL = 1;
		c = 0.001;
		variant = LDiversityVariant.DISTINCT;
	}

	public int getIntL() {
		return intL;
	}

	public void setIntL(int intL) {
		this.intL = intL;
	}

	public double getDoubleL() {
		return doubleL;
	}

	public void setDoubleL(double doubleL) {
		this.doubleL = doubleL;
	}

	public double getC() {
		return c;
	}

	public void setC(double c) {
		this.c = c;
	}

	public LDiversityVariant getVariant() {
		return variant;
	}

	public void setVariant(LDiversityVariant variant) {
		this.variant = variant;
	}

	@Override
	public PrivacyModelEditor createEditor(Collection<ColumnConfig> columns) {
		return new LDiversityEditor(this, columns);
	}

	@Override
	public PrivacyCriterion createCriterion(Data data, Config config) {
		switch (variant) {
		case DISTINCT:
			return new DistinctLDiversity(getColumn(), intL);
		case GRASSBERGER_ENTROPY:
			return new EntropyLDiversity(getColumn(), doubleL, EntropyEstimator.GRASSBERGER);
		case SHANNON_ENTROPY:
			return new EntropyLDiversity(getColumn(), doubleL, EntropyEstimator.GRASSBERGER);
		case RECURSIVE:
			return new RecursiveCLDiversity(getColumn(), c, intL);
		}
		return null;
	}

	@Override
	public String getName() {
		return '\u2113' + "-Diversity";
	}

	@Override
	protected String getToStringPrefix() {
		switch (variant) {
		case DISTINCT:
			return String.format("Distinct-%d--diversity", intL);
		case GRASSBERGER_ENTROPY:
			return String.format("Grassberger-entropy-%d-diversity", (int) doubleL);
		case SHANNON_ENTROPY:
			return String.format("Shannon-entropy-%d-diversity", (int) doubleL);
		case RECURSIVE:
			return String.format("Recursive-(%.3f, %d)-diversity", c, intL);
		}
		return getName();
	}

	@Override
	public void save(NodeSettingsWO settings) {
		super.save(settings);
		settings.addInt(CONFIG_INT_L, intL);
		settings.addDouble(CONFIG_DOUBLE_L, doubleL);
		settings.addDouble(CONFIG_C, c);
		settings.addString(CONFIG_VARIANT, variant.getTitle());
	}

	@Override
	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		super.load(settings);
		intL = settings.getInt(CONFIG_INT_L);
		doubleL = settings.getDouble(CONFIG_DOUBLE_L);
		c = settings.getDouble(CONFIG_C);
		variant = LDiversityVariant.fromString(settings.getString(CONFIG_VARIANT));
	}
}
