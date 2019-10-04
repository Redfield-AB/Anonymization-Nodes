package se.redfield.arxnode.ui.pmodels;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.pmodels.AbstractPrivacyModelConfig;
import se.redfield.arxnode.config.pmodels.KMapConfig;
import se.redfield.arxnode.config.pmodels.KMapConfig.EstimatorOption;

public class KMapEditor implements PrivacyModelEditor {
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(KMapEditor.class);

	private JPanel panel;
	private JSpinner kInput;
	private JComboBox<EstimatorOption> cbEstimator;
	private JLabel significanceLabel;
	private JSpinner significanceInput;

	public KMapEditor(KMapConfig source) {
		kInput = new JSpinner(new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1));
		cbEstimator = new JComboBox<>(EstimatorOption.values());
		cbEstimator.addActionListener(e -> onEstimatorChanged());
		significanceLabel = new JLabel("Significance level:");
		significanceInput = new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.001));

		panel = new JPanel(new FormLayout("p:n, 5:n, p:n, p:g", "p:n, 5:n, p:n, 5:n, p:n"));
		CellConstraints cc = new CellConstraints();
		panel.add(new JLabel("K:"), cc.rc(1, 1));
		panel.add(kInput, cc.rc(1, 3));
		panel.add(new JLabel("Estimator:"), cc.rc(3, 1));
		panel.add(cbEstimator, cc.rc(3, 3));
		panel.add(significanceLabel, cc.rc(5, 1));
		panel.add(significanceInput, cc.rc(5, 3));

		kInput.setValue(source.getK());
		cbEstimator.setSelectedItem(source.getEstimator());
		significanceInput.setValue(source.getSignificanceLevel());
	}

	private void onEstimatorChanged() {
		boolean visible = cbEstimator.getSelectedItem() != EstimatorOption.NONE;
		significanceInput.setVisible(visible);
		significanceLabel.setVisible(visible);
	}

	@Override
	public JComponent getComponent() {
		return panel;
	}

	@Override
	public void readFromComponent(AbstractPrivacyModelConfig target) {
		KMapConfig c = (KMapConfig) target;
		c.setK((int) kInput.getValue());
		c.setEstimator((EstimatorOption) cbEstimator.getSelectedItem());
		c.setSignificanceLevel((double) significanceInput.getValue());
	}

	@Override
	public void validate() throws InvalidSettingsException {
		// nothing to validate
	}
}
