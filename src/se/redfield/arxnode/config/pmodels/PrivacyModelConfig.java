package se.redfield.arxnode.config.pmodels;

import java.util.Collection;

import org.deidentifier.arx.criteria.PrivacyCriterion;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.ui.pmodels.PrivacyModelEditor;

public interface PrivacyModelConfig {

	public PrivacyModelEditor createEditor(Collection<ColumnConfig> columns);

	public PrivacyCriterion createCriterion();

}
