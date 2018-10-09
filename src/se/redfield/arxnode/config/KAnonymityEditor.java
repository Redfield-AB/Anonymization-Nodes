package se.redfield.arxnode.config;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class KAnonymityEditor implements PrivacyModelEditor {

	private JPanel editPanel;
	private JSpinner factorInput;

	public KAnonymityEditor(KAnonymityConfig source) {
		factorInput = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));

		editPanel = new JPanel();
		editPanel.add(new JLabel("K-Anonymity factor"));
		editPanel.add(factorInput);

		factorInput.setValue(source.getFactor());
	}

	@Override
	public JComponent getComponent() {
		return editPanel;
	}

	@Override
	public void readFromComponent(PrivacyModelConfig target) {
		((KAnonymityConfig) target).setFactor((int) factorInput.getValue());
	}

}
