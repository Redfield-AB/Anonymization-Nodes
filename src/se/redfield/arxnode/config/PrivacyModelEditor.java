package se.redfield.arxnode.config;

import javax.swing.JComponent;

public interface PrivacyModelEditor {
	public JComponent getComponent();

	public void readFromComponent(PrivacyModelConfig target);
}
