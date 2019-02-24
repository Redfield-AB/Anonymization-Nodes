package se.redfield.arxnode.config.pmodels;

import se.redfield.arxnode.util.TitledEnum;

public enum LDiversityVariant implements TitledEnum {
	DISTINCT("Distinct-l-diversity", false, true), //
	SHANNON_ENTROPY("Shannon-entropy-l-diversity", false, false), //
	GRASSBERGER_ENTROPY("Grassberger-entropy-l-diversity", false, false), //
	RECURSIVE("Recursive-(c,l)-diversity", true, true);

	private String title;
	private boolean hasC;
	private boolean intParam;

	private LDiversityVariant(String title, boolean hasC, boolean intParam) {
		this.title = title;
		this.hasC = hasC;
		this.intParam = intParam;
	}

	public boolean isHasC() {
		return hasC;
	}

	public boolean isIntParam() {
		return intParam;
	}

	@Override
	public String toString() {
		return title;
	}

	@Override
	public String getTitle() {
		return title;
	}

	public static LDiversityVariant fromString(String str) {
		return TitledEnum.fromString(values(), str, DISTINCT);
	}
}
