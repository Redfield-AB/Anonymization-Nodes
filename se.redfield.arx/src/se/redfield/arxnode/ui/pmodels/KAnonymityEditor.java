package se.redfield.arxnode.ui.pmodels;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.knime.core.node.InvalidSettingsException;

import se.redfield.arxnode.config.pmodels.AbstractPrivacyModelConfig;
import se.redfield.arxnode.config.pmodels.KAnonymityConfig;

public class KAnonymityEditor implements PrivacyModelEditor {

	private JPanel editPanel;
	private JSpinner factorInput;

	public KAnonymityEditor(KAnonymityConfig source) {
		factorInput = new JSpinner(new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1));

		editPanel = new JPanel();
		editPanel.add(new JLabel("K:"));
		editPanel.add(factorInput);

		factorInput.setValue(source.getFactor());
	}

	@Override
	public JComponent getComponent() {
		return editPanel;
	}

	@Override
	public void readFromComponent(AbstractPrivacyModelConfig target) {
		((KAnonymityConfig) target).setFactor((int) factorInput.getValue());
	}

	@Override
	public void validate() throws InvalidSettingsException {

	}
}
