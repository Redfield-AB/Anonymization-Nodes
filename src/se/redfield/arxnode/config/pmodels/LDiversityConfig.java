package se.redfield.arxnode.config.pmodels;

import java.util.Collection;

import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity.EntropyEstimator;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;

import se.redfield.arxnode.config.ColumnConfig;
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
	public PrivacyCriterion createCriterion() {
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
	public String toString() {
		String suffix = "";
		switch (variant) {
		case DISTINCT:
			suffix = "l=" + intL;
			break;
		case GRASSBERGER_ENTROPY:
		case SHANNON_ENTROPY:
			suffix = String.format("l=%.2f", doubleL);
			break;
		case RECURSIVE:
			suffix = String.format("l=%d, c=%.3f", intL, c);
			break;
		}
		return variant.toString() + " for [" + getColumn() + "] with " + suffix;
	}
}
