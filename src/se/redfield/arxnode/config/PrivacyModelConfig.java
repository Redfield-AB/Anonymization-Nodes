package se.redfield.arxnode.config;

import org.deidentifier.arx.criteria.PrivacyCriterion;

public interface PrivacyModelConfig {

	public PrivacyModelEditor createEditor();

	public PrivacyCriterion createCriterion();
}
