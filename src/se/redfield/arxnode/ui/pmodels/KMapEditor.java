package se.redfield.arxnode.ui.pmodels;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import se.redfield.arxnode.config.pmodels.KMapConfig;
import se.redfield.arxnode.config.pmodels.PrivacyModelConfig;

public class KMapEditor implements PrivacyModelEditor {

	private JPanel panel;
	private JSpinner kInput;

	public KMapEditor(KMapConfig source) {
		kInput = new JSpinner(new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1));

		panel = new JPanel();
		panel.add(new JLabel("K:"));
		panel.add(kInput);

		kInput.setValue(source.getK());
	}

	@Override
	public JComponent getComponent() {
		return panel;
	}

	@Override
	public void readFromComponent(PrivacyModelConfig target) {
		KMapConfig c = (KMapConfig) target;
		c.setK((int) kInput.getValue());
	}

}
