package se.redfield.arxnode.ui.pmodels;

import javax.swing.JComponent;

import se.redfield.arxnode.config.pmodels.AbstractPrivacyModelConfig;

public interface PrivacyModelEditor {
	public JComponent getComponent();

	public void readFromComponent(AbstractPrivacyModelConfig target);
}
