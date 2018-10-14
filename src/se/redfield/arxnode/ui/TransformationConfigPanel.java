package se.redfield.arxnode.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.knime.core.node.NodeLogger;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.redfield.arxnode.config.TransformationConfig;
import se.redfield.arxnode.config.TransformationConfig.MicroaggregationFunction;
import se.redfield.arxnode.config.TransformationConfig.Mode;

public class TransformationConfigPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final NodeLogger logger = NodeLogger.getLogger(TransformationConfigPanel.class);

	private TransformationConfig config;
	private JPanel generalizationPanel;
	private JPanel microaggregationPanel;

	public TransformationConfigPanel(TransformationConfig config) {
		this.config = config;
		initUI();
	}

	private void initUI() {
		generalizationPanel = createGeneralizationPanel();
		microaggregationPanel = createMicroaggregationPanel();
		JComboBox<Mode> cbMode = new JComboBox<>(Mode.values());
		cbMode.addActionListener(e -> {
			onModeSelected((Mode) cbMode.getSelectedItem());
		});
		cbMode.setSelectedItem(config.getMode());

		setLayout(new FormLayout("p:n, 5:n, p:n, f:p:g", "p:n, 5:n, f:p:n, 5:n, f:p:n"));
		CellConstraints cc = new CellConstraints();
		add(new JLabel("Mode"), cc.rc(1, 1));
		add(cbMode, cc.rc(1, 3));
		add(generalizationPanel, cc.rcw(3, 1, 4));
		add(microaggregationPanel, cc.rcw(5, 1, 4));
		setBorder(BorderFactory.createTitledBorder("Transformation"));
	}

	private void onModeSelected(Mode mode) {
		config.setMode(mode);

		boolean generalization = mode == Mode.GENERALIZATION;
		generalizationPanel.setVisible(generalization);
		microaggregationPanel.setVisible(!generalization);
	}

	private JPanel createGeneralizationPanel() {
		JCheckBox cbMin = new JCheckBox("Minimum");
		JCheckBox cbMax = new JCheckBox("Maximum");
		JSpinner minInput = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		JSpinner maxInput = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));

		cbMin.addActionListener(new MinMaxCheckboxListener(minInput, config::setMinGeneralization));
		cbMax.addActionListener(new MinMaxCheckboxListener(maxInput, config::setMaxGeneralization));
		minInput.addChangeListener(e -> config.setMinGeneralization((Integer) minInput.getValue()));
		maxInput.addChangeListener(e -> config.setMaxGeneralization((Integer) maxInput.getValue()));

		setGeneralizationValue(cbMin, minInput, config.getMinGeneralization());
		setGeneralizationValue(cbMax, maxInput, config.getMaxGeneralization());

		JPanel panel = new JPanel(new FormLayout("p:n, 5:n, p:n, 5:n, p:n, 5:n, p:n", "p:n"));
		CellConstraints cc = new CellConstraints();
		panel.add(cbMin, cc.rc(1, 1));
		panel.add(minInput, cc.rc(1, 3));
		panel.add(cbMax, cc.rc(1, 5));
		panel.add(maxInput, cc.rc(1, 7));

		return panel;
	}

	private void setGeneralizationValue(JCheckBox cb, JSpinner input, Integer value) {
		if (value == null) {
			cb.setSelected(false);
			input.setEnabled(false);
		} else {
			cb.setSelected(true);
			input.setValue(value);
		}
	}

	private JPanel createMicroaggregationPanel() {
		JComboBox<MicroaggregationFunction> cbFunc = new JComboBox<>(MicroaggregationFunction.values());
		cbFunc.setSelectedItem(config.getMicroaggregationFunc());
		cbFunc.addActionListener(
				e -> config.setMicroaggregationFunc((MicroaggregationFunction) cbFunc.getSelectedItem()));

		JCheckBox cbIgnore = new JCheckBox("Ignore missing data");
		cbIgnore.setSelected(config.isIgnoreMissingData());
		cbIgnore.addActionListener(e -> config.setIgnoreMissingData(cbIgnore.isSelected()));

		CellConstraints cc = new CellConstraints();
		JPanel panel = new JPanel(new FormLayout("p:n, 5:n, p:n, 5:n, p:n", "p:n"));
		panel.add(new JLabel("Function"), cc.rc(1, 1));
		panel.add(cbFunc, cc.rc(1, 3));
		panel.add(cbIgnore, cc.rc(1, 5));
		return panel;
	}

	private class MinMaxCheckboxListener implements ActionListener {

		private JSpinner input;
		private Consumer<Integer> setter;

		public MinMaxCheckboxListener(JSpinner input, Consumer<Integer> setter) {
			this.input = input;
			this.setter = setter;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean selected = ((JCheckBox) e.getSource()).isSelected();
			input.setEnabled(selected);
			setter.accept(selected ? (Integer) input.getValue() : null);
		}

	}
}
