package se.redfield.arxnode.config.pmodels;

import java.util.Collection;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity.EntropyEstimator;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.ui.pmodels.LDiversityEditor;
import se.redfield.arxnode.ui.pmodels.PrivacyModelEditor;

public class LDiversityConfig extends ColumnPrivacyModelConfig {

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
}
