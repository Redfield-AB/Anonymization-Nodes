package se.redfield.arxnode.ui.pmodels;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.pmodels.AbstractPrivacyModelConfig;
import se.redfield.arxnode.config.pmodels.DPresenceConfig;

public class DPresenceEditor implements PrivacyModelEditor {

	JPanel panel;
	JSpinner dMinInput;
	JSpinner dMaxInput;

	public DPresenceEditor(DPresenceConfig source) {
		dMinInput = new JSpinner(createSpinnerModel());
		dMinInput.setValue(source.getdMin());
		dMaxInput = new JSpinner(createSpinnerModel());
		dMaxInput.setValue(source.getdMax());

		CellConstraints cc = new CellConstraints();
		panel = new JPanel(new FormLayout("p:n, 5:n, f:70:n, p:g", "p:n, 5:n, p:n"));
		panel.add(new JLabel("Lower"), cc.rc(1, 1));
		panel.add(dMinInput, cc.rc(1, 3));
		panel.add(new JLabel("Upper"), cc.rc(3, 1));
		panel.add(dMaxInput, cc.rc(3, 3));

	}

	private SpinnerModel createSpinnerModel() {
		return new SpinnerNumberModel(0.0, 0.0, 1.0, 0.001);
	}

	@Override
	public JComponent getComponent() {
		return panel;
	}

	@Override
	public void readFromComponent(AbstractPrivacyModelConfig target) {
		DPresenceConfig c = (DPresenceConfig) target;
		c.setdMin((double) dMinInput.getValue());
		c.setdMax((double) dMaxInput.getValue());
	}

}
